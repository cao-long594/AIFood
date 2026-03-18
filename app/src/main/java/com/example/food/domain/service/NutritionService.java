package com.example.food.domain.service;

import com.example.food.db.entity.Food;
import com.example.food.model.NutritionCalculator;

public final class NutritionService {

    private NutritionService() {
    }

    public static NutritionCalculator.NutritionData calculateByAmount(Food food, double amount) {
        if (food == null || amount <= 0) {
            return new NutritionCalculator.NutritionData();
        }

        int unitAmount = food.getUnitAmount() > 0 ? food.getUnitAmount() : 100;
        double ratio = amount / unitAmount;
        return new NutritionCalculator.NutritionData(
                food.getCalories() * ratio,
                food.getCarbohydrate() * ratio,
                food.getProtein() * ratio,
                food.getFat() * ratio,
                food.getSaturatedFat() * ratio,
                food.getMonounsaturatedFat() * ratio,
                food.getPolyunsaturatedFat() * ratio
        );
    }
}