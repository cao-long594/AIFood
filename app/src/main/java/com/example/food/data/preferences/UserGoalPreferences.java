package com.example.food.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.food.model.UserGoal;
import com.example.food.utils.Constants;

public class UserGoalPreferences {

    private final SharedPreferences preferences;

    public UserGoalPreferences(Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public UserGoal loadUserGoal() {
        return new UserGoal(
                preferences.getFloat(Constants.PREF_CALORIES_GOAL, (float) Constants.DEFAULT_CALORIES_GOAL),
                preferences.getFloat(Constants.PREF_CARBOHYDRATE_GOAL, (float) Constants.DEFAULT_CARBOHYDRATE_GOAL),
                preferences.getFloat(Constants.PREF_PROTEIN_GOAL, (float) Constants.DEFAULT_PROTEIN_GOAL),
                preferences.getFloat(Constants.PREF_FAT_GOAL, (float) Constants.DEFAULT_FAT_GOAL),
                preferences.getFloat(Constants.PREF_WATER_GOAL, (float) Constants.DEFAULT_WATER_GOAL)
        );
    }

    public void saveUserGoal(UserGoal goal) {
        if (goal == null) {
            return;
        }
        preferences.edit()
                .putFloat(Constants.PREF_CALORIES_GOAL, (float) goal.getCaloriesGoal())
                .putFloat(Constants.PREF_CARBOHYDRATE_GOAL, (float) goal.getCarbohydrateGoal())
                .putFloat(Constants.PREF_PROTEIN_GOAL, (float) goal.getProteinGoal())
                .putFloat(Constants.PREF_FAT_GOAL, (float) goal.getFatGoal())
                .putFloat(Constants.PREF_WATER_GOAL, (float) goal.getWaterGoal())
                .apply();
    }

    public double loadWaterGoal() {
        return preferences.getFloat(Constants.PREF_WATER_GOAL, (float) Constants.DEFAULT_WATER_GOAL);
    }

    public void saveWaterGoal(double goal) {
        preferences.edit()
                .putFloat(Constants.PREF_WATER_GOAL, (float) goal)
                .apply();
    }
}