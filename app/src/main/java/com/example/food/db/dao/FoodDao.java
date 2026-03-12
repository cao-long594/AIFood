package com.example.food.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.food.db.entity.Food;

import java.util.List;

/**
 * 椋熺墿DAO鎺ュ彛
 * 鎻愪緵瀵归鐗╄〃鐨勫鍒犳敼鏌ユ搷浣? */
@Dao
public interface FoodDao {
    /**
     * 鎻掑叆椋熺墿
     * @param food 椋熺墿瀵硅薄
     * @return 鎻掑叆鐨勯鐗㊣D
     */
    @Insert
    long insert(Food food);

    /**
     * 鏇存柊椋熺墿
     * @param food 椋熺墿瀵硅薄
     */
    @Update
    void update(Food food);

    /**
     * 鍒犻櫎椋熺墿
     * @param food 椋熺墿瀵硅薄
     */
    @Delete
    void delete(Food food);

    /**
     * 鍒犻櫎鎸囧畾ID鐨勯鐗?     * @param id 椋熺墿ID
     */
    @Query("DELETE FROM foods WHERE id = :id")
    void deleteById(int id);

    /**
     * 鏌ヨ鎵€鏈夐鐗?     * @return 椋熺墿鍒楄〃
     */
    @Query("SELECT * FROM foods ORDER BY name ASC")
    List<Food> getAllFoods();

    /**
     * 鏍规嵁ID鏌ヨ椋熺墿
     * @param id 椋熺墿ID
     * @return 椋熺墿瀵硅薄
     */
    @Query("SELECT * FROM foods WHERE id = :id")
    Food getFoodById(int id);

    /**
     * 鏍规嵁鍚嶇О妯＄硦鏌ヨ椋熺墿
     * @param keyword 鎼滅储鍏抽敭璇?     * @return 绗﹀悎鏉′欢鐨勯鐗╁垪琛?     */
    @Query("SELECT * FROM foods WHERE name LIKE '%' || :keyword || '%' ORDER BY name ASC")
    List<Food> searchFoods(String keyword);

    @Query("SELECT name FROM foods")
    List<String> getAllFoodNames();

    @Query("UPDATE foods SET unit = CASE WHEN unit = 1 THEN 0 WHEN unit = 2 THEN 1 ELSE unit END")
    void migrateUnitValuesToV2();
}

