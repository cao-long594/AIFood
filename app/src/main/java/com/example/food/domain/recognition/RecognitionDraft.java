package com.example.food.domain.recognition;

import com.example.food.db.entity.Food;
import com.example.food.domain.service.NutritionService;
import com.example.food.model.NutritionCalculator;
import com.example.food.utils.Constants;
import com.example.food.utils.FoodCategoryHelper;

public class RecognitionDraft {
    private final String canonicalFoodName;
    private final Food normalizedFoodPer100g;
    private final double estimatedAmountGram;
    private final NutritionCalculator.NutritionData estimatedNutrition;
    private final String sourceProvider;
    private final double confidence;

    public RecognitionDraft(String canonicalFoodName, Food normalizedFoodPer100g,
                            double estimatedAmountGram, String sourceProvider, double confidence) {
        this.canonicalFoodName = canonicalFoodName;
        this.normalizedFoodPer100g = normalizedFoodPer100g;
        this.estimatedAmountGram = estimatedAmountGram;
        this.sourceProvider = sourceProvider;
        this.confidence = confidence;
        this.estimatedNutrition = NutritionService.calculateByAmount(normalizedFoodPer100g, estimatedAmountGram);
    }

    public static RecognitionDraft fromResult(RecognitionResult result) {
        if (result == null || result.getNutritionPer100g() == null) {
            return null;
        }

        String name = safeName(result.getCanonicalFoodName());
        NutritionPer100g nutrition = result.getNutritionPer100g();
        double amount = result.getEstimatedAmountGram() > 0 ? result.getEstimatedAmountGram() : 100d;
        Food food = new Food(
                name,
                nutrition.getCalories(),
                nutrition.getCarbohydrate(),
                nutrition.getProtein(),
                nutrition.getFat(),
                nutrition.getSaturatedFat(),
                nutrition.getMonounsaturatedFat(),
                nutrition.getPolyunsaturatedFat(),
                Constants.UNIT_GRAM,
                100,
                null
        );
        food.setCategory(FoodCategoryHelper.resolveCategory(food));
        return new RecognitionDraft(name, food, amount, result.getSourceProvider(), result.getConfidence());
    }

    public String getCanonicalFoodName() {
        return canonicalFoodName;
    }

    public Food getNormalizedFoodPer100g() {
        return normalizedFoodPer100g;
    }

    public double getEstimatedAmountGram() {
        return estimatedAmountGram;
    }

    public NutritionCalculator.NutritionData getEstimatedNutrition() {
        return estimatedNutrition;
    }

    public String getSourceProvider() {
        return sourceProvider;
    }

    public double getConfidence() {
        return confidence;
    }

    private static String safeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "识别食物";
        }
        return name.trim();
    }
}
