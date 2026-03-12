package com.example.food.ui.food;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food.R;
import com.example.food.db.AppDatabase;
import com.example.food.db.dao.FoodDao;
import com.example.food.db.entity.Food;
import com.example.food.utils.Constants;

public class AddFoodActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText caloriesEditText;
    private EditText carbsEditText;
    private EditText proteinEditText;
    private EditText fatEditText;
    private EditText saturatedFatEditText;
    private EditText monoUnsaturatedFatEditText;
    private EditText polyUnsaturatedFatEditText;
    private AppDatabase database;
    private FoodDao foodDao;
    private int foodId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_add);

        if (getIntent().hasExtra("food_id")) {
            foodId = getIntent().getIntExtra("food_id", -1);
        }

        initViews();
        initDatabase();

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
    }

    private void initDatabase() {
        database = AppDatabase.getInstance(this);
        foodDao = database.foodDao();
    }

    private void loadFoodData() {
        new Thread(() -> {
            Food food = foodDao.getFoodById(foodId);
            runOnUiThread(() -> {
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

                RadioGroup unitRadioGroup = findViewById(R.id.rg_unit);
                if (food.getUnit() == Constants.UNIT_GRAM) {
                    unitRadioGroup.check(R.id.rb_gram);
                } else {
                    unitRadioGroup.check(R.id.rb_milliliter);
                }
            });
        }).start();
    }

    private void saveFood() {
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "\u8bf7\u8f93\u5165\u98df\u7269\u540d\u79f0", Toast.LENGTH_SHORT).show();
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

            RadioGroup unitRadioGroup = findViewById(R.id.rg_unit);
            int unit = unitRadioGroup.getCheckedRadioButtonId() == R.id.rb_gram
                    ? Constants.UNIT_GRAM
                    : Constants.UNIT_MILLILITER;
            int unitAmount = 100;

            Food food;
            if (foodId != -1) {
                food = new Food(
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
                        unitAmount
                );

                new Thread(() -> {
                    foodDao.update(food);
                    runOnUiThread(() -> {
                        Toast.makeText(AddFoodActivity.this, "\u66f4\u65b0\u6210\u529f", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }).start();
            } else {
                food = new Food(
                        name,
                        calories,
                        carbs,
                        protein,
                        fat,
                        saturatedFat,
                        monoUnsaturatedFat,
                        polyUnsaturatedFat,
                        unit,
                        unitAmount
                );

                new Thread(() -> {
                    foodDao.insert(food);
                    runOnUiThread(() -> {
                        Toast.makeText(AddFoodActivity.this, "\u6dfb\u52a0\u6210\u529f", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }).start();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "\u8bf7\u8f93\u5165\u6709\u6548\u7684\u6570\u503c", Toast.LENGTH_SHORT).show();
        }
    }

    private double parseDouble(String text, double defaultValue) {
        if (text.isEmpty()) {
            return defaultValue;
        }
        return Double.parseDouble(text);
    }
}

