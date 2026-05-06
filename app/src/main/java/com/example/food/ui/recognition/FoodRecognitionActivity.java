package com.example.food.ui.recognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.food.R;
import com.example.food.data.preferences.UserSessionPreferences;
import com.example.food.data.repository.FoodRepository;
import com.example.food.data.repository.MealRepository;
import com.example.food.db.entity.Food;
import com.example.food.db.entity.MealRecord;
import com.example.food.domain.recognition.FoodCandidate;
import com.example.food.domain.recognition.FoodRecognitionClient;
import com.example.food.domain.recognition.HttpFoodRecognitionClient;
import com.example.food.domain.recognition.RecognitionDraft;
import com.example.food.domain.recognition.RecognitionResult;
import com.example.food.domain.service.NutritionService;
import com.example.food.model.NutritionCalculator;
import com.example.food.utils.Constants;
import com.example.food.utils.FoodCategoryHelper;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class FoodRecognitionActivity extends AppCompatActivity {
    private static final int[] MEAL_TYPES = {
            MealRecord.MEAL_TYPE_BREAKFAST,
            MealRecord.MEAL_TYPE_LUNCH,
            MealRecord.MEAL_TYPE_AFTERNOON_SNACK,
            MealRecord.MEAL_TYPE_DINNER,
            MealRecord.MEAL_TYPE_BEDTIME
    };

    private PreviewView previewView;
    private ProgressBar progressBar;
    private TextView statusTextView;
    private TextView summaryTextView;
    private TextView topCandidatesTextView;
    private EditText foodNameEditText;
    private EditText amountEditText;
    private Spinner mealTypeSpinner;
    private LinearLayout confirmationPanel;
    private Button captureButton;
    private Button retakeButton;
    private Button saveButton;

    private ImageCapture imageCapture;
    private RecognitionDraft currentDraft;
    private long selectedDate;
    private int initialMealType;

    private FoodRepository foodRepository;
    private MealRepository mealRepository;
    private FoodRecognitionClient recognitionClient;

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    startCamera();
                } else {
                    showStatus(getString(R.string.recognition_permission_denied));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_recognition);

        initialMealType = getIntent().getIntExtra("mealType", MealRecord.MEAL_TYPE_BREAKFAST);
        selectedDate = getIntent().getLongExtra("selectedDate", System.currentTimeMillis());

        foodRepository = new FoodRepository(this);
        mealRepository = new MealRepository(this);
        recognitionClient = new HttpFoodRecognitionClient();

        initViews();
        bindMealTypeSpinner();
        requestCameraIfNeeded();
    }

    private void initViews() {
        previewView = findViewById(R.id.preview_view);
        progressBar = findViewById(R.id.progress_bar);
        statusTextView = findViewById(R.id.tv_status);
        summaryTextView = findViewById(R.id.tv_recognition_summary);
        topCandidatesTextView = findViewById(R.id.tv_top_candidates);
        foodNameEditText = findViewById(R.id.et_food_name);
        amountEditText = findViewById(R.id.et_amount);
        mealTypeSpinner = findViewById(R.id.sp_meal_type);
        confirmationPanel = findViewById(R.id.confirmation_panel);
        captureButton = findViewById(R.id.btn_capture);
        retakeButton = findViewById(R.id.btn_retake);
        saveButton = findViewById(R.id.btn_save);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        captureButton.setOnClickListener(v -> takePhoto());
        retakeButton.setOnClickListener(v -> resetForRetake());
        saveButton.setOnClickListener(v -> saveCurrentDraft());
    }

    private void bindMealTypeSpinner() {
        String[] mealNames = new String[MEAL_TYPES.length];
        int selectedIndex = 0;
        for (int i = 0; i < MEAL_TYPES.length; i++) {
            mealNames[i] = MealRecord.getMealTypeName(MEAL_TYPES[i]);
            if (MEAL_TYPES[i] == initialMealType) {
                selectedIndex = i;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                mealNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealTypeSpinner.setAdapter(adapter);
        mealTypeSpinner.setSelection(selectedIndex);
    }

    private void requestCameraIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
            return;
        }
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                );
            } catch (ExecutionException | InterruptedException | IllegalArgumentException e) {
                showStatus(getString(R.string.recognition_camera_unavailable));
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            showStatus(getString(R.string.recognition_camera_unavailable));
            return;
        }

        setLoading(true, getString(R.string.recognition_taking_photo));
        File photoFile = new File(getCacheDir(), "food_recognition_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        recognizePhoto(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        setLoading(false, exception.getMessage());
                    }
                });
    }

    private void recognizePhoto(File photoFile) {
        setLoading(true, getString(R.string.recognition_calling_api));
        recognitionClient.recognize(photoFile, new FoodRecognitionClient.Callback() {
            @Override
            public void onSuccess(RecognitionResult result) {
                setLoading(false, null);
                RecognitionDraft draft = RecognitionDraft.fromResult(result);
                if (draft == null) {
                    showStatus(getString(R.string.recognition_camera_unavailable));
                    return;
                }
                bindDraft(draft, result);
            }

            @Override
            public void onError(String message) {
                setLoading(false, message);
            }
        });
    }

    private void bindDraft(RecognitionDraft draft, RecognitionResult result) {
        currentDraft = draft;
        foodNameEditText.setText(draft.getCanonicalFoodName());
        amountEditText.setText(String.format(Locale.CHINA, "%.0f", draft.getEstimatedAmountGram()));

        NutritionCalculator.NutritionData nutrition = draft.getEstimatedNutrition();
        summaryTextView.setText(getString(
                R.string.recognition_summary,
                draft.getCanonicalFoodName(),
                draft.getEstimatedAmountGram(),
                nutrition.getCalories(),
                Math.max(0d, draft.getConfidence()) * 100d
        ));
        topCandidatesTextView.setText(getString(R.string.recognition_top_candidates, formatCandidates(result)));

        confirmationPanel.setVisibility(View.VISIBLE);
        captureButton.setVisibility(View.GONE);
        statusTextView.setVisibility(View.GONE);
    }

    private String formatCandidates(RecognitionResult result) {
        StringBuilder builder = new StringBuilder();
        for (FoodCandidate candidate : result.getTopK()) {
            if (candidate == null || candidate.getName() == null || candidate.getName().trim().isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("、");
            }
            builder.append(candidate.getName().trim());
        }
        return builder.length() == 0 ? result.getCanonicalFoodName() : builder.toString();
    }

    private void resetForRetake() {
        currentDraft = null;
        confirmationPanel.setVisibility(View.GONE);
        captureButton.setVisibility(View.VISIBLE);
        statusTextView.setVisibility(View.GONE);
        setLoading(false, null);
    }

    private void saveCurrentDraft() {
        if (currentDraft == null) {
            return;
        }

        String foodName = foodNameEditText.getText() == null
                ? ""
                : foodNameEditText.getText().toString().trim();
        if (foodName.isEmpty()) {
            Toast.makeText(this, R.string.recognition_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountEditText.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.recognition_amount_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        if (amount <= 0) {
            Toast.makeText(this, R.string.recognition_amount_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true, null);
        UserSessionPreferences session = new UserSessionPreferences(this);

        // 根据登录状态选择查找范围
        FoodRepository.Callback<Food> findCallback = food -> {
            if (food != null) {
                insertMealRecord(food, amount, selectedMealType());
                return;
            }

            Food recognizedFood = buildFoodForSave(foodName);
            // 非管理员用户标记为私有食物 + 用户分享来源
            if (session.isLoggedIn() && !session.isAdmin()) {
                recognizedFood.setUserId(session.getUserId());
                recognizedFood.setSource("USER");
                recognizedFood.setSourceUserName(session.getUsername());
                recognizedFood.setVisibilityStatus(2); // 默认为私密
            } else {
                recognizedFood.setSource("SYSTEM");
                recognizedFood.setVisibilityStatus(1);
            }
            foodRepository.insertFoodReturningId(recognizedFood, id -> {
                recognizedFood.setId(id.intValue());
                insertMealRecord(recognizedFood, amount, selectedMealType());
            });
        };

        if (session.isLoggedIn() && !session.isAdmin()) {
            foodRepository.findBestMatchByNameForUser(foodName, session.getUserId(), findCallback);
        } else {
            foodRepository.findBestMatchByName(foodName, findCallback);
        }
    }

    private Food buildFoodForSave(String foodName) {
        Food draftFood = currentDraft.getNormalizedFoodPer100g();
        Food food = new Food(
                foodName,
                draftFood.getCalories(),
                draftFood.getCarbohydrate(),
                draftFood.getProtein(),
                draftFood.getFat(),
                draftFood.getSaturatedFat(),
                draftFood.getMonounsaturatedFat(),
                draftFood.getPolyunsaturatedFat(),
                Constants.UNIT_GRAM,
                100,
                null
        );
        food.setCategory(FoodCategoryHelper.resolveCategory(food));
        return food;
    }

    private int selectedMealType() {
        int position = mealTypeSpinner.getSelectedItemPosition();
        if (position < 0 || position >= MEAL_TYPES.length) {
            return initialMealType;
        }
        return MEAL_TYPES[position];
    }

    private void insertMealRecord(Food food, double amount, int mealType) {
        NutritionCalculator.NutritionData nutrition = NutritionService.calculateByAmount(food, amount);
        MealRecord record = new MealRecord(
                food.getId(),
                food.getName(),
                amount,
                new Date(selectedDate),
                new Date(),
                mealType,
                nutrition.getCalories(),
                nutrition.getCarbohydrate(),
                nutrition.getProtein(),
                nutrition.getFat(),
                nutrition.getSaturatedFat(),
                nutrition.getMonounsaturatedFat(),
                nutrition.getPolyunsaturatedFat()
        );

        UserSessionPreferences sessionPrefs = new UserSessionPreferences(this);
        if (sessionPrefs.isLoggedIn()) {
            record.setUserId(sessionPrefs.getUserId());
        }

        mealRepository.insert(record, () -> {
            setLoading(false, null);
            Toast.makeText(
                    this,
                    getString(R.string.recognition_save_success, MealRecord.getMealTypeName(mealType)),
                    Toast.LENGTH_SHORT
            ).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    private void setLoading(boolean loading, String message) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        captureButton.setEnabled(!loading);
        retakeButton.setEnabled(!loading);
        saveButton.setEnabled(!loading);
        if (message == null || message.trim().isEmpty()) {
            statusTextView.setVisibility(View.GONE);
            return;
        }
        showStatus(message);
    }

    private void showStatus(String message) {
        statusTextView.setText(message);
        statusTextView.setVisibility(View.VISIBLE);
    }
}
