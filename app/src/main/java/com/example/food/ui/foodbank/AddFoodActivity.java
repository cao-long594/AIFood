package com.example.food.ui.foodbank;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food.R;
import com.example.food.data.preferences.UserSessionPreferences;
import com.example.food.data.repository.FoodRepository;
import com.example.food.db.entity.Food;
import com.example.food.utils.Constants;
import com.example.food.utils.FoodCategoryHelper;

public class AddFoodActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText caloriesEditText;
    private EditText carbsEditText;
    private EditText proteinEditText;
    private EditText fatEditText;
    private EditText saturatedFatEditText;
    private EditText monoUnsaturatedFatEditText;
    private EditText polyUnsaturatedFatEditText;
    private RadioGroup unitRadioGroup;
    private RadioGroup categoryRadioGroup;

    private FoodRepository foodRepository;
    private int foodId = -1;
    private boolean isReadOnly = false;
    private android.view.View btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_add);

        if (getIntent().hasExtra("food_id")) {
            foodId = getIntent().getIntExtra("food_id", -1);
        }

        initViews();
        foodRepository = new FoodRepository(this);

        btnSave = findViewById(R.id.btn_save);

        if (foodId != -1) {
            loadFoodData();
        }

        btnSave.setOnClickListener(v -> saveFood());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
    }

    private void initViews() {
        nameEditText = findViewById(R.id.et_food_name);
        caloriesEditText = findViewById(R.id.et_calories);
        carbsEditText = findViewById(R.id.et_carbs);
        proteinEditText = findViewById(R.id.et_protein);
        fatEditText = findViewById(R.id.et_fat);
        saturatedFatEditText = findViewById(R.id.et_saturated_fat);
        monoUnsaturatedFatEditText = findViewById(R.id.et_mono_unsaturated_fat);
        polyUnsaturatedFatEditText = findViewById(R.id.et_poly_unsaturated_fat);
        unitRadioGroup = findViewById(R.id.rg_unit);
        categoryRadioGroup = findViewById(R.id.rg_category);
    }

    private void loadFoodData() {
        foodRepository.getFoodById(foodId, food -> {
            if (food == null) {
                return;
            }

            // 判断是否有修改权限：管理员可改所有，普通用户只能改自己未公开的私有食物
            UserSessionPreferences session = new UserSessionPreferences(this);
            if (session.isLoggedIn() && !session.isAdmin()) {
                boolean isOwner = food.getUserId() != null && food.getUserId() == session.getUserId();
                boolean isStillPrivate = food.getVisibilityStatus() == 2;
                isReadOnly = !(isOwner && isStillPrivate);
            }

            if (isReadOnly) {
                btnSave.setVisibility(android.view.View.GONE);
                setAllFieldsReadOnly(true);
            }

            nameEditText.setText(food.getName());
            caloriesEditText.setText(String.valueOf(food.getCalories()));
            carbsEditText.setText(String.valueOf(food.getCarbohydrate()));
            proteinEditText.setText(String.valueOf(food.getProtein()));
            fatEditText.setText(String.valueOf(food.getFat()));
            saturatedFatEditText.setText(String.valueOf(food.getSaturatedFat()));
            monoUnsaturatedFatEditText.setText(String.valueOf(food.getMonounsaturatedFat()));
            polyUnsaturatedFatEditText.setText(String.valueOf(food.getPolyunsaturatedFat()));

            if (food.getUnit() == Constants.UNIT_GRAM) {
                unitRadioGroup.check(R.id.rb_gram);
            } else {
                unitRadioGroup.check(R.id.rb_milliliter);
            }

            bindCategorySelection(food.getCategory());
        });
    }

    private void setAllFieldsReadOnly(boolean readOnly) {
        nameEditText.setEnabled(!readOnly);
        caloriesEditText.setEnabled(!readOnly);
        carbsEditText.setEnabled(!readOnly);
        proteinEditText.setEnabled(!readOnly);
        fatEditText.setEnabled(!readOnly);
        saturatedFatEditText.setEnabled(!readOnly);
        monoUnsaturatedFatEditText.setEnabled(!readOnly);
        polyUnsaturatedFatEditText.setEnabled(!readOnly);
        for (int i = 0; i < unitRadioGroup.getChildCount(); i++) {
            unitRadioGroup.getChildAt(i).setEnabled(!readOnly);
        }
        for (int i = 0; i < categoryRadioGroup.getChildCount(); i++) {
            categoryRadioGroup.getChildAt(i).setEnabled(!readOnly);
        }
    }

    private void saveFood() {
        if (isReadOnly) {
            Toast.makeText(this, "无修改权限", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.food_add_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double calories = parseDouble(caloriesEditText.getText().toString(), 0);
            double carbs = parseDouble(carbsEditText.getText().toString(), 0);
            double protein = parseDouble(proteinEditText.getText().toString(), 0);
            double fat = parseDouble(fatEditText.getText().toString(), 0);
            double saturatedFat = parseDouble(saturatedFatEditText.getText().toString(), 0);
            double monoUnsaturatedFat = parseDouble(monoUnsaturatedFatEditText.getText().toString(), 0);
            double polyUnsaturatedFat = parseDouble(polyUnsaturatedFatEditText.getText().toString(), 0);

            int unit = unitRadioGroup.getCheckedRadioButtonId() == R.id.rb_gram
                    ? Constants.UNIT_GRAM
                    : Constants.UNIT_MILLILITER;
            int unitAmount = 100;
            String category = selectedCategory();

            if (foodId != -1) {
                // 修改模式：先加载已有数据，再更新可编辑字段，保留其他字段
                foodRepository.getFoodById(foodId, existing -> {
                    if (existing == null) {
                        Toast.makeText(AddFoodActivity.this, "食物数据不存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    existing.setName(name);
                    existing.setCalories(calories);
                    existing.setCarbohydrate(carbs);
                    existing.setProtein(protein);
                    existing.setFat(fat);
                    existing.setSaturatedFat(saturatedFat);
                    existing.setMonounsaturatedFat(monoUnsaturatedFat);
                    existing.setPolyunsaturatedFat(polyUnsaturatedFat);
                    existing.setUnit(unit);
                    existing.setUnitAmount(unitAmount);
                    existing.setCategory(category);

                    foodRepository.updateFood(existing, () -> {
                        Toast.makeText(AddFoodActivity.this, R.string.food_update_success, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            } else {
                Food newFood = new Food(
                        name,
                        calories,
                        carbs,
                        protein,
                        fat,
                        saturatedFat,
                        monoUnsaturatedFat,
                        polyUnsaturatedFat,
                        unit,
                        unitAmount,
                        category
                );

                // 标记来源：非管理员=用户分享+私密，管理员=系统导入+公开
                UserSessionPreferences session = new UserSessionPreferences(this);
                if (session.isLoggedIn() && !session.isAdmin()) {
                    newFood.setUserId(session.getUserId());
                    newFood.setSource("USER");
                    newFood.setSourceUserName(session.getUsername());
                    newFood.setVisibilityStatus(2);
                } else {
                    newFood.setSource("SYSTEM");
                    newFood.setVisibilityStatus(1);
                }

                foodRepository.insertFood(newFood, () -> {
                    Toast.makeText(AddFoodActivity.this, R.string.food_add_success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.food_number_invalid, Toast.LENGTH_SHORT).show();
        }
    }

    private void bindCategorySelection(String category) {
        String normalized = FoodCategoryHelper.normalizeCategory(category);
        if (FoodCategoryHelper.CATEGORY_PROTEIN.equals(normalized)) {
            categoryRadioGroup.check(R.id.rb_category_protein);
            return;
        }
        if (FoodCategoryHelper.CATEGORY_FAT.equals(normalized)) {
            categoryRadioGroup.check(R.id.rb_category_fat);
            return;
        }
        if (FoodCategoryHelper.CATEGORY_FRUIT.equals(normalized)) {
            categoryRadioGroup.check(R.id.rb_category_fruit);
            return;
        }
        categoryRadioGroup.check(R.id.rb_category_carb);
    }

    private String selectedCategory() {
        int checkedId = categoryRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_category_protein) {
            return FoodCategoryHelper.CATEGORY_PROTEIN;
        }
        if (checkedId == R.id.rb_category_fat) {
            return FoodCategoryHelper.CATEGORY_FAT;
        }
        if (checkedId == R.id.rb_category_fruit) {
            return FoodCategoryHelper.CATEGORY_FRUIT;
        }
        return FoodCategoryHelper.CATEGORY_CARB;
    }

    private double parseDouble(String text, double defaultValue) {
        if (text.isEmpty()) {
            return defaultValue;
        }
        return Double.parseDouble(text);
    }
}