package com.example.food.ui.foodbank;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_add);

        if (getIntent().hasExtra("food_id")) {
            foodId = getIntent().getIntExtra("food_id", -1);
        }

        initViews();
        foodRepository = new FoodRepository(this);

        if (foodId != -1) {
            loadFoodData();
        }

        findViewById(R.id.btn_save).setOnClickListener(v -> saveFood());
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

    private void saveFood() {
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
                Food updatedFood = new Food(
                        foodId,
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

                foodRepository.updateFood(updatedFood, () -> {
                    Toast.makeText(AddFoodActivity.this, R.string.food_update_success, Toast.LENGTH_SHORT).show();
                    finish();
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