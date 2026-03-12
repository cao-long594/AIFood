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
import com.example.food.db.AppDatabase;
import com.example.food.db.FoodSeedImporter;
import com.example.food.db.dao.FoodDao;
import com.example.food.db.entity.Food;
import com.example.food.db.entity.MealRecord;
import com.example.food.ui.foodbank.FoodAdapter;
import com.example.food.ui.foodbank.FoodAdapter.OnFoodClickListener;
import com.example.food.utils.DateUtils;
import com.example.food.utils.NutritionCalculator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddActivity extends AppCompatActivity implements OnFoodClickListener {

    private RecyclerView rvFoodList;
    private FoodAdapter foodAdapter;
    private SearchView searchView;
    private TextView emptyStateTextView;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private AppDatabase database;
    private FoodDao foodDao;

    private int mealType;
    private long selectedDate;
    private List<Food> allFoods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meal_add);

        mealType = getIntent().getIntExtra("mealType", -1);
        selectedDate = getIntent().getLongExtra("selectedDate", 0);

        initViews();
        initDatabase();
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
        rvFoodList.setAdapter(foodAdapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void bindHeader() {
        titleTextView.setText(String.format(Locale.CHINA, "添加到%s", MealRecord.getMealTypeName(mealType)));
        Date date = new Date(selectedDate);
        subtitleTextView.setText(DateUtils.formatDate(date, "M月d日") + " " + DateUtils.getWeekDay(date));
    }

    private void initDatabase() {
        database = AppDatabase.getInstance(this);
        foodDao = database.foodDao();
    }

    private void loadAllFoods() {
        new Thread(() -> {
            FoodSeedImporter.ensureImported(this, database);
            allFoods = foodDao.getAllFoods();
            runOnUiThread(() -> updateFoodList(allFoods, false));
        }).start();
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

        new Thread(() -> {
            FoodSeedImporter.ensureImported(this, database);
            List<Food> filteredFoods = foodDao.searchFoods(keyword.trim());
            runOnUiThread(() -> updateFoodList(filteredFoods, true));
        }).start();
    }

    private void updateFoodList(List<Food> foods, boolean isSearchMode) {
        foodAdapter.setData(foods);
        boolean isEmpty = foods == null || foods.isEmpty();
        emptyStateTextView.setVisibility(isEmpty ? android.view.View.VISIBLE : android.view.View.GONE);
        if (isSearchMode && isEmpty) {
            emptyStateTextView.setText("没有找到匹配的食物，换个关键词试试");
        } else if (isEmpty) {
            emptyStateTextView.setText("食物库还是空的，先去添加几个常吃食物吧");
        }
    }

    @Override
    public void onFoodClick(Food food) {
        showAmountInputDialog(food);
    }

    private void showAmountInputDialog(Food food) {
        TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setHint("输入重量（克）");
        inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        TextInputEditText input = new TextInputEditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText("100");
        inputLayout.addView(input);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("记录 " + food.getName())
                .setMessage("输入本次食用重量后会直接添加到当前餐次")
                .setView(inputLayout)
                .setNegativeButton("取消", null)
                .setPositiveButton("添加", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String amountStr = input.getText() == null ? "" : input.getText().toString().trim();
            if (amountStr.isEmpty()) {
                inputLayout.setError("请输入食物重量");
                return;
            }
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    inputLayout.setError("重量需要大于0");
                    return;
                }
                inputLayout.setError(null);
                dialog.dismiss();
                saveMealRecord(food, amount);
            } catch (NumberFormatException e) {
                inputLayout.setError("请输入有效数字");
            }
        }));
        dialog.show();
    }

    private void saveMealRecord(Food food, double amount) {
        new Thread(() -> {
            try {
                NutritionCalculator.NutritionResult nutrition =
                        NutritionCalculator.calculateNutrition(food, amount);

                MealRecord record = new MealRecord(
                        food.getId(),
                        food.getName(),
                        amount,
                        new Date(selectedDate),
                        new Date(),
                        mealType,
                        nutrition.calories,
                        nutrition.carbohydrate,
                        nutrition.protein,
                        nutrition.fat,
                        nutrition.saturatedFat,
                        nutrition.monounsaturatedFat,
                        nutrition.polyunsaturatedFat
                );

                database.mealRecordDao().insert(record);
                runOnUiThread(() -> {
                    Toast.makeText(this, "已添加到" + MealRecord.getMealTypeName(mealType), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}

