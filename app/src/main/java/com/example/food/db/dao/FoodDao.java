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
}
