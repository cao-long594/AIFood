package com.example.food.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.food.db.entity.Food;

import java.util.List;

@Dao
public interface FoodDao {

    @Insert
    long insert(Food food);

    @Update
    void update(Food food);

    @Delete
    void delete(Food food);

    @Query("DELETE FROM foods WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM foods ORDER BY name ASC")
    List<Food> getAllFoods();

    @Query("SELECT * FROM foods WHERE id = :id")
    Food getFoodById(int id);

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :keyword || '%' ORDER BY name ASC")
    List<Food> searchFoods(String keyword);

    @Query("SELECT f.* FROM foods f INNER JOIN (SELECT foodId, MAX(created_at) AS last_used FROM meal_records GROUP BY foodId ORDER BY last_used DESC LIMIT :limit) recent ON recent.foodId = f.id ORDER BY recent.last_used DESC")
    List<Food> getRecentFoods(int limit);

    @Query("SELECT * FROM foods WHERE category = :category ORDER BY name ASC")
    List<Food> getFoodsByCategory(String category);

    @Query("UPDATE foods SET category = :category WHERE id = :foodId")
    void updateFoodCategory(int foodId, String category);

    @Query("SELECT name FROM foods")
    List<String> getAllFoodNames();

    @Query("UPDATE foods SET unit = CASE WHEN unit = 1 THEN 0 WHEN unit = 2 THEN 1 ELSE unit END")
    void migrateUnitValuesToV2();

    // ========== 用户可见范围查询 ==========

    @Query("SELECT * FROM foods WHERE existStatus = 1 AND (visibilityStatus = 1 OR (visibilityStatus = 2 AND userId = :userId)) ORDER BY name ASC")
    List<Food> getFoodsForUser(int userId);

    @Query("SELECT * FROM foods WHERE existStatus = 1 AND (visibilityStatus = 1 OR (visibilityStatus = 2 AND userId = :userId)) AND name LIKE '%' || :keyword || '%' ORDER BY name ASC")
    List<Food> searchFoodsForUser(String keyword, int userId);

    @Query("SELECT f.* FROM foods f INNER JOIN (SELECT foodId, MAX(created_at) AS last_used FROM meal_records GROUP BY foodId ORDER BY last_used DESC LIMIT :limit) recent ON recent.foodId = f.id WHERE f.existStatus = 1 AND (f.visibilityStatus = 1 OR (f.visibilityStatus = 2 AND f.userId = :userId)) ORDER BY recent.last_used DESC")
    List<Food> getRecentFoodsForUser(int limit, int userId);

    @Query("SELECT * FROM foods WHERE existStatus = 1 AND category = :category AND (visibilityStatus = 1 OR (visibilityStatus = 2 AND userId = :userId)) ORDER BY name ASC")
    List<Food> getFoodsByCategoryForUser(String category, int userId);

    // ========== 管理员操作（基于存在状态和可见性状态） ==========

    @Query("UPDATE foods SET visibilityStatus = 1 WHERE id = :foodId")
    void setVisibilityPublic(int foodId);

    @Query("UPDATE foods SET visibilityStatus = 2 WHERE id = :foodId")
    void setVisibilityPrivate(int foodId);

    @Query("UPDATE foods SET existStatus = 2 WHERE id = :foodId")
    void softDelete(int foodId);

    @Query("UPDATE foods SET existStatus = 1 WHERE id = :foodId")
    void restoreFood(int foodId);

    @Query("SELECT * FROM foods WHERE existStatus = 1 ORDER BY name ASC")
    List<Food> getAllActiveFoods();

    @Query("SELECT * FROM foods WHERE existStatus = 1 AND name LIKE '%' || :keyword || '%' ORDER BY name ASC")
    List<Food> searchActiveFoods(String keyword);

    @Query("SELECT * FROM foods WHERE userId = :userId AND existStatus = 1 ORDER BY name ASC")
    List<Food> getFoodsByUser(int userId);

    @Query("SELECT * FROM foods WHERE source = :source AND existStatus = 1 ORDER BY name ASC")
    List<Food> getFoodsBySource(String source);

    @Query("SELECT u.username FROM foods f JOIN users u ON f.userId = u.id WHERE f.id = :foodId")
    String getFoodOwnerUsername(int foodId);
}
