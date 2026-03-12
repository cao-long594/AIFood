package com.example.food.ui.meal;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.db.AppDatabase;
import com.example.food.db.entity.Food;
import com.example.food.db.entity.MealRecord;
import com.example.food.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealShowFragment extends Fragment {

    private static final String ARG_MEAL_TYPE = "meal_type";
    private static final String ARG_SELECTED_DATE = "selected_date";

    private int mealType;
    private Date selectedDate;

    private RecyclerView addedFoodRv;
    private TextView carbsValueTv;
    private TextView proteinValueTv;
    private TextView fatValueTv;
    private TextView caloriesValueTv;
    private TextView caloriesPercentageTv;
    private TextView recommendedCaloriesPercentageTv;
    private TextView emptyFoodsTextView;

    private AppDatabase database;
    private final List<MealRecord> addedRecords = new ArrayList<>();
    private FoodAdapter addedFoodAdapter;

    public static MealShowFragment newInstance(int mealType, Date selectedDate) {
        MealShowFragment fragment = new MealShowFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MEAL_TYPE, mealType);
        args.putLong(ARG_SELECTED_DATE, selectedDate.getTime());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mealType = getArguments().getInt(ARG_MEAL_TYPE);
            selectedDate = new Date(getArguments().getLong(ARG_SELECTED_DATE));
        }
        database = AppDatabase.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.meal_show, container, false);
        initViews(root);
        loadAddedFoods();
        return root;
    }

    private void initViews(View root) {
        carbsValueTv = root.findViewById(R.id.carbs_value);
        proteinValueTv = root.findViewById(R.id.protein_value);
        fatValueTv = root.findViewById(R.id.fat_value);
        caloriesValueTv = root.findViewById(R.id.calories_value);
        caloriesPercentageTv = root.findViewById(R.id.calories_percentage);
        recommendedCaloriesPercentageTv = root.findViewById(R.id.recommended_calories_percentage);
        emptyFoodsTextView = root.findViewById(R.id.tv_empty_foods);

        addedFoodRv = root.findViewById(R.id.added_food_rv);
        addedFoodAdapter = new FoodAdapter(addedRecords, new FoodAdapter.OnFoodClickListener() {
            @Override
            public void onFoodClick(MealRecord record) {
                showEditAmountDialog(record);
            }

            @Override
            public void onFoodLongClick(MealRecord record) {
                showDeleteConfirmDialog(record);
            }
        });
        addedFoodRv.setLayoutManager(new LinearLayoutManager(getContext()));
        addedFoodRv.setAdapter(addedFoodAdapter);
    }

    public void loadAddedFoods() {
        new Thread(() -> {
            try {
                Date startOfDay = DateUtils.getDateStart(selectedDate);
                Date endOfDay = DateUtils.getDateEnd(selectedDate);
                List<MealRecord> allRecords = database.mealRecordDao().getRecordsByDate(startOfDay, endOfDay);
                List<MealRecord> filteredRecords = new ArrayList<>();
                double todayTotalCalories = 0;

                for (MealRecord record : allRecords) {
                    todayTotalCalories += record.getCalories();
                    if (record.getMealType() == mealType) {
                        filteredRecords.add(record);
                    }
                }

                final double finalTodayTotalCalories = todayTotalCalories;
                runOnUiThreadIfActive(() -> {
                    addedRecords.clear();
                    addedRecords.addAll(filteredRecords);
                    addedFoodAdapter.notifyDataSetChanged();
                    emptyFoodsTextView.setVisibility(filteredRecords.isEmpty() ? View.VISIBLE : View.GONE);
                    updateNutritionData(finalTodayTotalCalories);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThreadIfActive(() ->
                        Toast.makeText(getContext(), "加载数据失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateNutritionData(double todayTotalCalories) {
        double totalCarbs = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double currentMealCalories = 0;

        for (MealRecord record : addedRecords) {
            totalCarbs += record.getCarbohydrate();
            totalProtein += record.getProtein();
            totalFat += record.getFat();
            currentMealCalories += record.getCalories();
        }

        carbsValueTv.setText(String.format(Locale.CHINA, "%.0f", totalCarbs));
        proteinValueTv.setText(String.format(Locale.CHINA, "%.0f", totalProtein));
        fatValueTv.setText(String.format(Locale.CHINA, "%.0f", totalFat));
        caloriesValueTv.setText(String.format(Locale.CHINA, "%.0f kcal", currentMealCalories));
        recommendedCaloriesPercentageTv.setText(getRecommendedPercentageByMealType(mealType));

        double caloriesPercentage = 0;
        if (todayTotalCalories > 0) {
            caloriesPercentage = (currentMealCalories / todayTotalCalories) * 100;
        }
        caloriesPercentageTv.setText(String.format(Locale.CHINA, "%.0f%%", caloriesPercentage));
    }

    private String getRecommendedPercentageByMealType(int mealType) {
        switch (mealType) {
            case MealRecord.MEAL_TYPE_BREAKFAST:
                return "参考：20%-25%";
            case MealRecord.MEAL_TYPE_LUNCH:
            case MealRecord.MEAL_TYPE_DINNER:
                return "参考：25%-30%";
            case MealRecord.MEAL_TYPE_MORNING_SNACK:
            case MealRecord.MEAL_TYPE_BEDTIME:
            case MealRecord.MEAL_TYPE_EVENING_SNACK:
                return "参考：5%-10%";
            case MealRecord.MEAL_TYPE_AFTERNOON_SNACK:
                return "参考：10%-15%";
            default:
                return "参考：0%";
        }
    }

    private void showEditAmountDialog(MealRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("修改食物重量");

        EditText input = new EditText(getContext());
        input.setText(String.format(Locale.CHINA, "%.0f", record.getAmount()));
        input.setHint("请输入重量（克）");
        input.setPadding(50, 40, 50, 40);
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String amountStr = input.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "请输入食物重量", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                updateMealRecord(record, amount);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "请输入有效数字", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showDeleteConfirmDialog(MealRecord record) {
        new AlertDialog.Builder(getContext())
                .setTitle("删除确认")
                .setMessage("确定要删除 " + record.getFoodName() + " 吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteMealRecord(record))
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateMealRecord(MealRecord record, double newAmount) {
        new Thread(() -> {
            try {
                Food food = database.foodDao().getFoodById(record.getFoodId());
                if (food != null) {
                    double ratio = newAmount / 100.0;
                    record.setAmount(newAmount);
                    record.setCalories(food.getCalories() * ratio);
                    record.setCarbohydrate(food.getCarbohydrate() * ratio);
                    record.setProtein(food.getProtein() * ratio);
                    record.setFat(food.getFat() * ratio);
                    record.setSaturatedFat(food.getSaturatedFat() * ratio);
                    record.setMonounsaturatedFat(food.getMonounsaturatedFat() * ratio);
                    record.setPolyunsaturatedFat(food.getPolyunsaturatedFat() * ratio);

                    database.mealRecordDao().update(record);
                    runOnUiThreadIfActive(() -> {
                        Toast.makeText(getContext(), "修改成功", Toast.LENGTH_SHORT).show();
                        loadAddedFoods();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void deleteMealRecord(MealRecord record) {
        new Thread(() -> {
            try {
                database.mealRecordDao().delete(record);
                runOnUiThreadIfActive(() -> {
                    Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                    loadAddedFoods();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void runOnUiThreadIfActive(Runnable action) {
        Activity activity = getActivity();
        if (!isAdded() || activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        activity.runOnUiThread(() -> {
            if (!isAdded() || getView() == null) {
                return;
            }
            action.run();
        });
    }

    public void updateSelectedDate(Date newDate) {
        this.selectedDate = newDate;
        loadAddedFoods();
    }
}

