package com.example.food.ui.meal;

import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.data.repository.FoodRepository;
import com.example.food.data.repository.MealRepository;
import com.example.food.db.entity.Food;
import com.example.food.db.entity.MealRecord;
import com.example.food.domain.service.NutritionService;
import com.example.food.ui.foodbank.FoodAdapter;
import com.example.food.ui.foodbank.FoodAdapter.OnFoodClickListener;
import com.example.food.utils.DateUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddActivity extends AppCompatActivity implements OnFoodClickListener {

    private static final int DEFAULT_AMOUNT_GRAMS = 100;


    private RecyclerView rvFoodList;
    private FoodAdapter foodAdapter;
    private SearchView searchView;
    private TextView emptyStateTextView;
    private TextView titleTextView;
    private TextView subtitleTextView;

    private int mealType;
    private long selectedDate;
    private List<Food> allFoods = new ArrayList<>();

    private FoodRepository foodRepository;
    private MealRepository mealRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meal_add);

        mealType = getIntent().getIntExtra("mealType", -1);
        selectedDate = getIntent().getLongExtra("selectedDate", 0);

        foodRepository = new FoodRepository(this);
        mealRepository = new MealRepository(this);

        initViews();
        bindHeader();
        loadAllFoods();
        setupSearchView();
    }

    private void initViews() {
        rvFoodList = findViewById(R.id.rv_food_list);
        searchView = findViewById(R.id.search_view);
        emptyStateTextView = findViewById(R.id.tv_empty_state);
        titleTextView = findViewById(R.id.tv_page_title);
        subtitleTextView = findViewById(R.id.tv_page_subtitle);

        rvFoodList.setLayoutManager(new LinearLayoutManager(this));
        foodAdapter = new FoodAdapter(this);
        foodAdapter.setOnFoodClickListener(this);
        foodAdapter.setOnHeaderClickListener(foodAdapter::toggleGroup);
        rvFoodList.setAdapter(foodAdapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void bindHeader() {
        titleTextView.setText(getString(R.string.meal_add_title, MealRecord.getMealTypeName(mealType)));
        Date date = new Date(selectedDate);
        subtitleTextView.setText(getString(
                R.string.meal_add_subtitle,
                DateUtils.formatDate(date, "M\u6708d\u65e5"),
                DateUtils.getWeekDay(date)
        ));
    }

    private void loadAllFoods() {
        foodRepository.loadAllFoods(foods -> updateFoodList(foods, false));
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFoods(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFoods(newText);
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            updateFoodList(allFoods, false);
            return false;
        });
    }

    private void searchFoods(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            updateFoodList(allFoods, false);
            return;
        }

        foodRepository.searchFoods(keyword, foods -> updateFoodList(foods, true));
    }

    private void updateFoodList(List<Food> foods, boolean isSearchMode) {
        List<Food> safeFoods = foods == null ? new ArrayList<>() : foods;
        if (!isSearchMode) {
            allFoods = new ArrayList<>(safeFoods);
        }

        foodAdapter.setData(safeFoods, isSearchMode);

        boolean isEmpty = safeFoods.isEmpty();
        emptyStateTextView.setVisibility(isEmpty ? android.view.View.VISIBLE : android.view.View.GONE);
        if (isSearchMode && isEmpty) {
            emptyStateTextView.setText(getString(R.string.search_no_result));
        } else if (isEmpty) {
            emptyStateTextView.setText(getString(R.string.meal_add_empty_state));
        }
    }

    @Override
    public void onFoodClick(Food food) {
        showAmountInputDialog(food);
    }

    private void showAmountInputDialog(Food food) {
        TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setHint(getString(R.string.meal_add_amount_hint));
        inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        TextInputEditText input = new TextInputEditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(DEFAULT_AMOUNT_GRAMS));
        inputLayout.addView(input);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.meal_add_amount_title, food.getName()))
                .setMessage(getString(R.string.meal_add_amount_message))
                .setView(inputLayout)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String amountStr = input.getText() == null ? "" : input.getText().toString().trim();
            if (amountStr.isEmpty()) {
                inputLayout.setError(getString(R.string.meal_add_amount_error_empty));
                return;
            }
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    inputLayout.setError(getString(R.string.meal_add_amount_error_invalid));
                    return;
                }
                inputLayout.setError(null);
                dialog.dismiss();
                saveMealRecord(food, amount);
            } catch (NumberFormatException e) {
                inputLayout.setError(getString(R.string.meal_add_amount_error_number));
            }
        }));
        dialog.show();
    }

    private void saveMealRecord(Food food, double amount) {
        com.example.food.model.NutritionCalculator.NutritionData nutrition = NutritionService.calculateByAmount(food, amount);
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

        mealRepository.insert(record, () -> {
            Toast.makeText(this,
                    getString(R.string.meal_add_success, MealRecord.getMealTypeName(mealType)),
                    Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }
}
