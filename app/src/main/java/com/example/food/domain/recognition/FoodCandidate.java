package com.example.food.domain.recognition;

import java.util.ArrayList;
import java.util.List;

public class FoodCandidate {
    private String name;
    private double confidence;
    private List<String> ingredients = new ArrayList<>();

    public FoodCandidate() {
    }

    public FoodCandidate(String name, double confidence, List<String> ingredients) {
        this.name = name;
        this.confidence = confidence;
        this.ingredients = ingredients == null ? new ArrayList<>() : ingredients;
    }

    public String getName() {
        return name;
    }

    public double getConfidence() {
        return confidence;
    }

    public List<String> getIngredients() {
        return ingredients;
    }
}
