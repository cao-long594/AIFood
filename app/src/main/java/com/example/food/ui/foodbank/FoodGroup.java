package com.example.food.ui.foodbank;

import com.example.food.db.entity.Food;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FoodGroup {
    private final String category;
    private final String title;
    private final List<Food> foods;
    private boolean expanded;

    public FoodGroup(String category, String title, List<Food> foods, boolean expanded) {
        this.category = category == null ? "" : category;
        this.title = title == null ? "" : title;
        this.foods = foods == null ? new ArrayList<>() : new ArrayList<>(foods);
        this.expanded = expanded;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public List<Food> getFoods() {
        return Collections.unmodifiableList(foods);
    }

    public int getCount() {
        return foods.size();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
