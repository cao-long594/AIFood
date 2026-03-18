package com.example.food.ui.meal;

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
import com.example.food.data.repository.FoodRepository;
import com.example.food.data.repository.MealRepository;
import com.example.food.db.entity.MealRecord;
import com.example.food.domain.service.NutritionService;
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
    private TextView emptyFoodsTextView;

    private final List<MealRecord> addedRecords = new ArrayList<>();
    private FoodAdapter addedFoodAdapter;
    private double lastTodayTotalCalories = 0;

    private MealRepository mealRepository;
    private FoodRepository foodRepository;

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
        mealRepository = new MealRepository(requireContext());
        foodRepository = new FoodRepository(requireContext());
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
        if (mealRepository == null || selectedDate == null) {
            return;
        }

        Date startOfDay = DateUtils.getDateStart(selectedDate);
        Date endOfDay = DateUtils.getDateEnd(selectedDate);

        mealRepository.getRecordsByDateRange(startOfDay, endOfDay, allRecords -> {
            List<MealRecord> filteredRecords = new ArrayList<>();
            double todayTotalCalories = 0;

            for (MealRecord record : allRecords) {
                todayTotalCalories += record.getCalories();
                if (record.getMealType() == mealType) {
                    filteredRecords.add(record);
                }
            }

            final double totalCalories = todayTotalCalories;
            lastTodayTotalCalories = totalCalories;
            if (!isAdded()) {
                return;
            }
            addedFoodAdapter.submitRecords(filteredRecords);
            addedRecords.clear();
            addedRecords.addAll(filteredRecords);
            emptyFoodsTextView.setVisibility(filteredRecords.isEmpty() ? View.VISIBLE : View.GONE);
            updateNutritionData(totalCalories);
        });
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

        double caloriesPercentage = todayTotalCalories > 0 ? (currentMealCalories / todayTotalCalories) * 100 : 0;
        addedFoodAdapter.updateSummary(totalCarbs, totalProtein, totalFat, currentMealCalories,
                caloriesPercentage, getRecommendedPercentageByMealType(mealType));
    }

    private String getRecommendedPercentageByMealType(int mealType) {
        switch (mealType) {
            case MealRecord.MEAL_TYPE_BREAKFAST:
                return getString(R.string.meal_ratio_breakfast);
            case MealRecord.MEAL_TYPE_LUNCH:
            case MealRecord.MEAL_TYPE_DINNER:
                return getString(R.string.meal_ratio_main);
            case MealRecord.MEAL_TYPE_MORNING_SNACK:
            case MealRecord.MEAL_TYPE_BEDTIME:
            case MealRecord.MEAL_TYPE_EVENING_SNACK:
                return getString(R.string.meal_ratio_snack_small);
            case MealRecord.MEAL_TYPE_AFTERNOON_SNACK:
                return getString(R.string.meal_ratio_snack_medium);
            default:
                return getString(R.string.meal_ratio_default);
        }
    }

    private void showEditAmountDialog(MealRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.meal_edit_amount_title);

        EditText input = new EditText(getContext());
        input.setText(String.format(Locale.CHINA, "%.0f", record.getAmount()));
        input.setHint(R.string.meal_edit_amount_hint);
        input.setPadding(50, 40, 50, 40);
        builder.setView(input);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String amountStr = input.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), R.string.meal_edit_amount_error_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                updateMealRecord(record, amount);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), R.string.meal_edit_amount_error_number, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void showDeleteConfirmDialog(MealRecord record) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.meal_delete_title)
                .setMessage(getString(R.string.meal_delete_message, record.getFoodName()))
                .setPositiveButton(R.string.meal_delete_action, (dialog, which) -> deleteMealRecord(record))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateMealRecord(MealRecord record, double newAmount) {
        foodRepository.getFoodById(record.getFoodId(), food -> {
            if (food == null) {
                Toast.makeText(getContext(), R.string.meal_edit_fail, Toast.LENGTH_SHORT).show();
                return;
            }

            com.example.food.model.NutritionCalculator.NutritionData nutrition = NutritionService.calculateByAmount(food, newAmount);
            record.setAmount(newAmount);
            record.setCalories(nutrition.getCalories());
            record.setCarbohydrate(nutrition.getCarbohydrate());
            record.setProtein(nutrition.getProtein());
            record.setFat(nutrition.getFat());
            record.setSaturatedFat(nutrition.getSaturatedFat());
            record.setMonounsaturatedFat(nutrition.getMonounsaturatedFat());
            record.setPolyunsaturatedFat(nutrition.getPolyunsaturatedFat());

            mealRepository.update(record, () -> {
                if (isAdded()) {
                    Toast.makeText(getContext(), R.string.meal_edit_success, Toast.LENGTH_SHORT).show();
                    loadAddedFoods();
                }
            });
        });
    }

    private void deleteMealRecord(MealRecord record) {
        List<MealRecord> current = new ArrayList<>(addedRecords);
        current.remove(record);
        addedRecords.clear();
        addedRecords.addAll(current);
        addedFoodAdapter.submitRecords(current);
        emptyFoodsTextView.setVisibility(current.isEmpty() ? View.VISIBLE : View.GONE);
        lastTodayTotalCalories = Math.max(0, lastTodayTotalCalories - record.getCalories());
        updateNutritionData(lastTodayTotalCalories);

        mealRepository.delete(record, () -> {
            if (isAdded()) {
                Toast.makeText(getContext(), R.string.meal_delete_success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateSelectedDate(Date newDate) {
        this.selectedDate = newDate;
        loadAddedFoods();
    }
}

