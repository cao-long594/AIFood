package com.example.food.data.repository;

import android.content.Context;

import com.example.food.core.concurrent.AppExecutors;
import com.example.food.db.AppDatabase;
import com.example.food.db.FoodSeedImporter;
import com.example.food.db.dao.FoodDao;
import com.example.food.db.entity.Food;

import java.util.List;

public class FoodRepository {

    public interface Callback<T> {
        void onResult(T data);
    }

    private final Context appContext;
    private final AppDatabase database;
    private final FoodDao foodDao;

    public FoodRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.database = AppDatabase.getInstance(appContext);
        this.foodDao = database.foodDao();
    }

    public void loadAllFoods(Callback<List<Food>> callback) {
        AppExecutors.runOnIo(() -> {
            FoodSeedImporter.ensureImported(appContext, database);
            List<Food> foods = foodDao.getAllFoods();
            AppExecutors.runOnMain(() -> callback.onResult(foods));
        });
    }

    public void searchFoods(String keyword, Callback<List<Food>> callback) {
        AppExecutors.runOnIo(() -> {
            FoodSeedImporter.ensureImported(appContext, database);
            List<Food> foods = (keyword == null || keyword.trim().isEmpty())
                    ? foodDao.getAllFoods()
                    : foodDao.searchFoods(keyword.trim());
            AppExecutors.runOnMain(() -> callback.onResult(foods));
        });
    }

    public void getFoodById(int foodId, Callback<Food> callback) {
        AppExecutors.runOnIo(() -> {
            Food food = foodDao.getFoodById(foodId);
            AppExecutors.runOnMain(() -> callback.onResult(food));
        });
    }

    public void deleteById(int foodId, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            foodDao.deleteById(foodId);
            AppExecutors.runOnMain(onComplete);
        });
    }
    public void insertFood(Food food, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            foodDao.insert(food);
            AppExecutors.runOnMain(onComplete);
        });
    }

    public void updateFood(Food food, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            foodDao.update(food);
            AppExecutors.runOnMain(onComplete);
        });
    }
}
