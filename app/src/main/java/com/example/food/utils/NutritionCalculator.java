package com.example.food.utils;

import com.example.food.db.entity.Food;

public class NutritionCalculator {

    public static class NutritionResult {
        public double calories;
        public double carbohydrate;
        public double protein;
        public double fat;
        public double saturatedFat;
        public double monounsaturatedFat;
        public double polyunsaturatedFat;
    }

    public static NutritionResult calculateNutrition(Food food, double amount) {
        NutritionResult result = new NutritionResult();
        double ratio = amount / 100.0;

        result.calories = food.getCalories() * ratio;
        result.carbohydrate = food.getCarbohydrate() * ratio;
        result.protein = food.getProtein() * ratio;
        result.fat = food.getFat() * ratio;
        result.saturatedFat = food.getSaturatedFat() * ratio;
        result.monounsaturatedFat = food.getMonounsaturatedFat() * ratio;
        result.polyunsaturatedFat = food.getPolyunsaturatedFat() * ratio;

        return result;
    }
}