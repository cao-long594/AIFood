package com.example.food.data.repository;

import android.content.Context;

import com.example.food.core.concurrent.AppExecutors;
import com.example.food.db.AppDatabase;
import com.example.food.db.FoodSeedImporter;
import com.example.food.db.dao.FoodDao;
import com.example.food.db.entity.Food;

import java.util.List;
import java.util.Locale;

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
            List<Food> foods = foodDao.getAllActiveFoods();
            AppExecutors.runOnMain(() -> callback.onResult(foods));
        });
    }

    public void searchFoods(String keyword, Callback<List<Food>> callback) {
        AppExecutors.runOnIo(() -> {
            FoodSeedImporter.ensureImported(appContext, database);
            List<Food> foods = (keyword == null || keyword.trim().isEmpty())
                    ? foodDao.getAllActiveFoods()
                    : foodDao.searchActiveFoods(keyword.trim());
            AppExecutors.runOnMain(() -> callback.onResult(foods));
        });
    }

    public void getFoodById(int foodId, Callback<Food> callback) {
        AppExecutors.runOnIo(() -> {
            Food food = foodDao.getFoodById(foodId);
            AppExecutors.runOnMain(() -> callback.onResult(food));
        });
    }

    public void findBestMatchByName(String name, Callback<Food> callback) {
        AppExecutors.runOnIo(() -> {
            FoodSeedImporter.ensureImported(appContext, database);
            String normalizedName = normalizeName(name);
            Food exactMatch = null;
            Food containsMatch = null;

            if (!normalizedName.isEmpty()) {
                List<Food> foods = foodDao.getAllFoods();
                for (Food food : foods) {
                    String foodName = normalizeName(food.getName());
                    if (foodName.equals(normalizedName)) {
                        exactMatch = food;
                        break;
                    }
                    if (containsMatch == null
                            && (foodName.contains(normalizedName) || normalizedName.contains(foodName))) {
                        containsMatch = food;
                    }
                }
            }

            Food result = exactMatch != null ? exactMatch : containsMatch;
            AppExecutors.runOnMain(() -> callback.onResult(result));
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

    public void insertFoodReturningId(Food food, Callback<Long> callback) {
        AppExecutors.runOnIo(() -> {
            long id = foodDao.insert(food);
            AppExecutors.runOnMain(() -> callback.onResult(id));
        });
    }

    public void updateFood(Food food, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            foodDao.update(food);
            AppExecutors.runOnMain(onComplete);
        });
    }

    // ========== 用户可见范围查询 ==========

    public void loadAllFoodsForUser(int userId, Callback<List<Food>> callback) {
        AppExecutors.runOnIo(() -> {
            FoodSeedImporter.ensureImported(appContext, database);
            List<Food> foods = foodDao.getFoodsForUser(userId);
            AppExecutors.runOnMain(() -> callback.onResult(foods));
        });
    }

    public void searchFoodsForUser(String keyword, int userId, Callback<List<Food>> callback) {
        AppExecutors.runOnIo(() -> {
            FoodSeedImporter.ensureImported(appContext, database);
            List<Food> foods = (keyword == null || keyword.trim().isEmpty())
                    ? foodDao.getFoodsForUser(userId)
                    : foodDao.searchFoodsForUser(keyword.trim(), userId);
            AppExecutors.runOnMain(() -> callback.onResult(foods));
        });
    }

    public void findBestMatchByNameForUser(String name, int userId, Callback<Food> callback) {
        AppExecutors.runOnIo(() -> {
            FoodSeedImporter.ensureImported(appContext, database);
            String normalizedName = normalizeName(name);
            Food exactMatch = null;
            Food containsMatch = null;

            if (!normalizedName.isEmpty()) {
                List<Food> foods = foodDao.getFoodsForUser(userId);
                for (Food food : foods) {
                    String foodName = normalizeName(food.getName());
                    if (foodName.equals(normalizedName)) {
                        exactMatch = food;
                        break;
                    }
                    if (containsMatch == null
                            && (foodName.contains(normalizedName) || normalizedName.contains(foodName))) {
                        containsMatch = food;
                    }
                }
            }

            Food result = exactMatch != null ? exactMatch : containsMatch;
            AppExecutors.runOnMain(() -> callback.onResult(result));
        });
    }

    // ========== 管理员操作 ==========

    public void promoteToPublic(int foodId, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            foodDao.setVisibilityPublic(foodId);
            AppExecutors.runOnMain(onComplete);
        });
    }

    public void demoteToAdminOnly(int foodId, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            foodDao.setVisibilityPrivate(foodId);
            AppExecutors.runOnMain(onComplete);
        });
    }

    public void softDelete(int foodId, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            foodDao.softDelete(foodId);
            AppExecutors.runOnMain(onComplete);
        });
    }

    public void restoreFood(int foodId, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            foodDao.restoreFood(foodId);
            AppExecutors.runOnMain(onComplete);
        });
    }

    public void getFoodsByUser(int userId, Callback<List<Food>> callback) {
        AppExecutors.runOnIo(() -> {
            List<Food> foods = foodDao.getFoodsByUser(userId);
            AppExecutors.runOnMain(() -> callback.onResult(foods));
        });
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }
}
