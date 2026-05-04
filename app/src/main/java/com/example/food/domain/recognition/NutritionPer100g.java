package com.example.food.domain.recognition;

public class NutritionPer100g {
    private double calories;
    private double carbohydrate;
    private double protein;
    private double fat;
    private double saturatedFat;
    private double monounsaturatedFat;
    private double polyunsaturatedFat;

    public NutritionPer100g() {
    }

    public double getCalories() {
        return calories;
    }

    public double getCarbohydrate() {
        return carbohydrate;
    }

    public double getProtein() {
        return protein;
    }

    public double getFat() {
        return fat;
    }

    public double getSaturatedFat() {
        return saturatedFat;
    }

    public double getMonounsaturatedFat() {
        return monounsaturatedFat;
    }

    public double getPolyunsaturatedFat() {
        return polyunsaturatedFat;
    }
}
