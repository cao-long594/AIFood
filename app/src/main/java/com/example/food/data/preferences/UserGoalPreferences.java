package com.example.food.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.food.model.UserGoal;
import com.example.food.utils.Constants;

/**
 * 用户目标偏好存储
 * 每个用户独立存储，通过 userId 区分
 */
public class UserGoalPreferences {

    private final SharedPreferences preferences;
    private final int userId;

    public UserGoalPreferences(Context context) {
        this(context, -1);
    }

    public UserGoalPreferences(Context context, int userId) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        this.userId = userId;
    }

    private String k(String key) {
        return userId > 0 ? "user_" + userId + "_" + key : key;
    }

    public UserGoal loadUserGoal() {
        return new UserGoal(
                preferences.getFloat(k(Constants.PREF_CALORIES_GOAL), (float) Constants.DEFAULT_CALORIES_GOAL),
                preferences.getFloat(k(Constants.PREF_CARBOHYDRATE_GOAL), (float) Constants.DEFAULT_CARBOHYDRATE_GOAL),
                preferences.getFloat(k(Constants.PREF_PROTEIN_GOAL), (float) Constants.DEFAULT_PROTEIN_GOAL),
                preferences.getFloat(k(Constants.PREF_FAT_GOAL), (float) Constants.DEFAULT_FAT_GOAL),
                preferences.getFloat(k(Constants.PREF_WATER_GOAL), (float) Constants.DEFAULT_WATER_GOAL)
        );
    }

    public void saveUserGoal(UserGoal goal) {
        if (goal == null) {
            return;
        }
        preferences.edit()
                .putFloat(k(Constants.PREF_CALORIES_GOAL), (float) goal.getCaloriesGoal())
                .putFloat(k(Constants.PREF_CARBOHYDRATE_GOAL), (float) goal.getCarbohydrateGoal())
                .putFloat(k(Constants.PREF_PROTEIN_GOAL), (float) goal.getProteinGoal())
                .putFloat(k(Constants.PREF_FAT_GOAL), (float) goal.getFatGoal())
                .putFloat(k(Constants.PREF_WATER_GOAL), (float) goal.getWaterGoal())
                .apply();
    }

    public double loadWaterGoal() {
        return preferences.getFloat(k(Constants.PREF_WATER_GOAL), (float) Constants.DEFAULT_WATER_GOAL);
    }

    public void saveWaterGoal(double goal) {
        preferences.edit()
                .putFloat(k(Constants.PREF_WATER_GOAL), (float) goal)
                .apply();
    }
}
