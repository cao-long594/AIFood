package com.example.food.ui.meal;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.food.db.entity.MealRecord;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MealAdapter extends FragmentStateAdapter {

    private static final int[] MEAL_TYPES = {
            MealRecord.MEAL_TYPE_BREAKFAST,
            MealRecord.MEAL_TYPE_LUNCH,
            MealRecord.MEAL_TYPE_AFTERNOON_SNACK,
            MealRecord.MEAL_TYPE_DINNER,
            MealRecord.MEAL_TYPE_BEDTIME
    };

    private Date selectedDate;
    private final Map<Integer, MealShowFragment> fragmentMap = new HashMap<>();

    public MealAdapter(@NonNull FragmentActivity fragmentActivity, Date selectedDate) {
        super(fragmentActivity);
        this.selectedDate = selectedDate;
    }

    public MealAdapter(@NonNull Fragment fragment, Date selectedDate) {
        super(fragment);
        this.selectedDate = selectedDate;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int mealType = getMealTypeByPosition(position);
        MealShowFragment fragment = MealShowFragment.newInstance(mealType, selectedDate);
        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return MEAL_TYPES.length;
    }

    public int getMealTypeByPosition(int position) {
        if (position < 0 || position >= MEAL_TYPES.length) {
            return MealRecord.MEAL_TYPE_BREAKFAST;
        }
        return MEAL_TYPES[position];
    }

    public MealShowFragment getFragment(int position) {
        return fragmentMap.get(position);
    }

    public void updateSelectedDate(Date newDate) {
        this.selectedDate = newDate;
        for (MealShowFragment fragment : fragmentMap.values()) {
            if (fragment != null) {
                fragment.updateSelectedDate(newDate);
            }
        }
    }

    public void refreshAllFragments() {
        for (MealShowFragment fragment : fragmentMap.values()) {
            if (fragment != null) {
                fragment.loadAddedFoods();
            }
        }
    }
}

