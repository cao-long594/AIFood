package com.example.food.ui.water;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.data.preferences.UserGoalPreferences;
import com.example.food.data.repository.WaterRepository;
import com.example.food.ui.common.SelectedDateViewModel;
import com.example.food.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WaterFragment extends Fragment {

    private WaterCircleProgressView waterProgressView;
    private RecyclerView waterRecyclerView;
    private EditText customAmountEditText;
    private EditText goalInputEditText;
    private Button addCustomButton;
    private Button btn150ml;
    private Button btn300ml;
    private Button btn500ml;
    private Button btn1000ml;
    private TextView currentAmountTextView;
    private TextView emptyStateTextView;

    private WaterAdapter waterAdapter;
    private double waterGoal;
    private WaterRepository waterRepository;
    private UserGoalPreferences userGoalPreferences;
    private SelectedDateViewModel selectedDateViewModel;
    private Date selectedDate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_water, container, false);
        waterRepository = new WaterRepository(requireContext());
        userGoalPreferences = new UserGoalPreferences(requireContext());
        initViews(root);
        loadWaterGoal();
        initSharedDateViewModel();
        return root;
    }

    private void initSharedDateViewModel() {
        selectedDateViewModel = new ViewModelProvider(requireActivity()).get(SelectedDateViewModel.class);
        selectedDateViewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            selectedDate = DateUtils.getDateStart(date == null ? new Date() : date);
            loadWaterRecords();
        });
    }

    private void initViews(View root) {
        waterRecyclerView = root.findViewById(R.id.rv_water);
        customAmountEditText = root.findViewById(R.id.et_custom_amount);
        goalInputEditText = root.findViewById(R.id.et_goal_input);
        addCustomButton = root.findViewById(R.id.btn_confirm);
        waterProgressView = root.findViewById(R.id.waterProgressView);
        currentAmountTextView = root.findViewById(R.id.tv_water_current_amount);
        emptyStateTextView = root.findViewById(R.id.tv_water_empty);

        btn150ml = root.findViewById(R.id.btn_150ml);
        btn300ml = root.findViewById(R.id.btn_300ml);
        btn500ml = root.findViewById(R.id.btn_500ml);
        btn1000ml = root.findViewById(R.id.btn_1000ml);

        applyQuickAmountStyle(btn150ml, 150);
        applyQuickAmountStyle(btn300ml, 300);
        applyQuickAmountStyle(btn500ml, 500);
        applyQuickAmountStyle(btn1000ml, 1000);

        waterRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        waterAdapter = new WaterAdapter(requireContext());
        waterAdapter.setRepository(waterRepository);
        waterAdapter.setOnItemDeleteListener(this::loadWaterRecords);
        waterRecyclerView.setAdapter(waterAdapter);

        goalInputEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveWaterGoalFromInput();
            }
        });
        goalInputEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                goalInputEditText.clearFocus();
                saveWaterGoalFromInput();
                return true;
            }
            return false;
        });

        btn150ml.setOnClickListener(v -> addWaterRecord(150));
        btn300ml.setOnClickListener(v -> addWaterRecord(300));
        btn500ml.setOnClickListener(v -> addWaterRecord(500));
        btn1000ml.setOnClickListener(v -> addWaterRecord(1000));

        addCustomButton.setOnClickListener(v -> submitCustomAmount());
        customAmountEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitCustomAmount();
                return true;
            }
            return false;
        });
    }

    private void submitCustomAmount() {
        try {
            String amountText = customAmountEditText.getText().toString().trim();
            if (amountText.isEmpty()) {
                Toast.makeText(getContext(), R.string.water_amount_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                Toast.makeText(getContext(), R.string.water_amount_invalid, Toast.LENGTH_SHORT).show();
                return;
            }

            addWaterRecord(amount);
            customAmountEditText.setText("");
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), R.string.water_amount_nan, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadWaterGoal() {
        waterGoal = userGoalPreferences.loadWaterGoal();
        goalInputEditText.setText(formatNumber(waterGoal));
        waterProgressView.setMaxProgress((float) waterGoal);
    }

    private void saveWaterGoal(double goal) {
        waterGoal = goal;
        userGoalPreferences.saveWaterGoal(goal);
    }

    private void saveWaterGoalFromInput() {
        try {
            String goalText = goalInputEditText.getText().toString().trim();
            if (goalText.isEmpty()) {
                goalInputEditText.setText(formatNumber(waterGoal));
                return;
            }

            double newGoal = Double.parseDouble(goalText);
            if (newGoal <= 0) {
                goalInputEditText.setText(formatNumber(waterGoal));
                Toast.makeText(getContext(), R.string.water_goal_invalid, Toast.LENGTH_SHORT).show();
                return;
            }

            saveWaterGoal(newGoal);
            loadWaterRecords();
        } catch (NumberFormatException e) {
            goalInputEditText.setText(formatNumber(waterGoal));
            Toast.makeText(getContext(), R.string.water_amount_nan, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadWaterRecords() {
        Date targetDate = selectedDate == null ? DateUtils.getTodayStart() : selectedDate;
        Date startDate = DateUtils.getDateStart(targetDate);
        Date endDate = DateUtils.getDateEnd(targetDate);

        waterRepository.loadByDate(startDate, endDate, waterDayData -> {
            waterAdapter.setData(waterDayData.records);
            emptyStateTextView.setVisibility(waterDayData.records.isEmpty() ? View.VISIBLE : View.GONE);
            waterRecyclerView.setVisibility(waterDayData.records.isEmpty() ? View.GONE : View.VISIBLE);
            goalInputEditText.setText(formatNumber(waterGoal));
            waterProgressView.setMaxProgress((float) waterGoal);
            waterProgressView.setCurrentProgress((float) waterDayData.totalAmount);
            updateSummary(waterDayData.totalAmount);
        });
    }

    private void updateSummary(double totalAmount) {
        currentAmountTextView.setText(formatNumber(totalAmount));
    }

    private void addWaterRecord(double amount) {
        waterRepository.insert(amount, buildRecordTime(), () -> {
            loadWaterRecords();
            Toast.makeText(getContext(), R.string.water_add_success, Toast.LENGTH_SHORT).show();
        });
    }

    private Date buildRecordTime() {
        Date baseDate = selectedDate == null ? DateUtils.getTodayStart() : selectedDate;
        if (DateUtils.isSameDay(baseDate, new Date())) {
            return new Date();
        }

        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(baseDate);
        Calendar nowCalendar = Calendar.getInstance();
        targetCalendar.set(Calendar.HOUR_OF_DAY, nowCalendar.get(Calendar.HOUR_OF_DAY));
        targetCalendar.set(Calendar.MINUTE, nowCalendar.get(Calendar.MINUTE));
        targetCalendar.set(Calendar.SECOND, nowCalendar.get(Calendar.SECOND));
        targetCalendar.set(Calendar.MILLISECOND, nowCalendar.get(Calendar.MILLISECOND));
        return targetCalendar.getTime();
    }

    private String formatNumber(double amount) {
        return String.format(Locale.CHINA, "%.0f", amount);
    }

    private void applyQuickAmountStyle(Button button, int amount) {
        String text = amount + "\nml";
        SpannableString styledText = new SpannableString(text);
        int lineBreakIndex = text.indexOf('\n');
        int unitStart = lineBreakIndex + 1;

        styledText.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.water_quick_amount_text)),
                0,
                lineBreakIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styledText.setSpan(
                new StyleSpan(Typeface.BOLD),
                0,
                lineBreakIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styledText.setSpan(
                new AbsoluteSizeSpan(18, true),
                0,
                lineBreakIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styledText.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.water_quick_unit_text)),
                unitStart,
                text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        styledText.setSpan(
                new AbsoluteSizeSpan(13, true),
                unitStart,
                text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        button.setText(styledText);
    }

    // ── Task 2: per-fragment status bar color ─────────────────────────────────

    @Override
    public void onResume() {
        super.onResume();
        loadWaterGoal();
        loadWaterRecords();
        applyWaterStatusBar();
    }

    @Override
    public void onPause() {
        super.onPause();
        restoreDefaultStatusBar();
    }

    /**
     * Reads the solid background color of this fragment's root view and applies
     * it to the Activity's status bar, then adjusts icon brightness accordingly.
     */
    private void applyWaterStatusBar() {
        if (getActivity() == null || getView() == null) return;

        Drawable background = getView().getBackground();
        if (!(background instanceof ColorDrawable)) return;   // non-solid backgrounds are left as-is

        int color = ((ColorDrawable) background).getColor();
        Window window = getActivity().getWindow();
        window.setStatusBarColor(color);

        // Dark icons on light background, light icons on dark background
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(isColorLight(color));
    }

    /**
     * Restores the status bar to the app-wide default (home_page_background)
     * when leaving this fragment.
     */
    private void restoreDefaultStatusBar() {
        if (getActivity() == null) return;
        Window window = getActivity().getWindow();
        window.setStatusBarColor(
                ContextCompat.getColor(requireContext(), R.color.home_page_background));
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(true); // matches android:windowLightStatusBar in theme
    }

    /**
     * Returns true if {@code color} is perceptually "light" (luminance > 50 %),
     * meaning dark status-bar icons should be used.
     */
    private boolean isColorLight(int color) {
        // Standard relative luminance weights (ITU-R BT.601)
        double luminance = (0.299 * Color.red(color)
                + 0.587 * Color.green(color)
                + 0.114 * Color.blue(color)) / 255.0;
        return luminance > 0.5;
    }

    // ──────────────────────────────────────────────────────────────────────────
}