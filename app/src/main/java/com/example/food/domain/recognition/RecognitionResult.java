package com.example.food.domain.recognition;

import java.util.ArrayList;
import java.util.List;

public class RecognitionResult {
    private String canonicalFoodName;
    private NutritionPer100g nutritionPer100g;
    private double estimatedAmountGram;
    private String sourceProvider;
    private double confidence;
    private List<FoodCandidate> topK = new ArrayList<>();
    private List<String> possibleIngredients = new ArrayList<>();

    public String getCanonicalFoodName() {
        return canonicalFoodName;
    }

    public NutritionPer100g getNutritionPer100g() {
        return nutritionPer100g;
    }

    public double getEstimatedAmountGram() {
        return estimatedAmountGram;
    }

    public String getSourceProvider() {
        return sourceProvider;
    }

    public double getConfidence() {
        return confidence;
    }

    public List<FoodCandidate> getTopK() {
        return topK == null ? new ArrayList<>() : topK;
    }

    public List<String> getPossibleIngredients() {
        return possibleIngredients == null ? new ArrayList<>() : possibleIngredients;
    }
}
