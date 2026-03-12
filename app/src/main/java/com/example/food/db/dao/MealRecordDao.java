package com.example.food.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.food.db.entity.MealRecord;

import java.util.Date;
import java.util.List;

/**
 * 楗璁板綍DAO鎺ュ彛
 * 鎻愪緵瀵归ギ椋熻褰曡〃鐨勫鍒犳敼鏌ユ搷浣? */
@Dao
public interface MealRecordDao {
    /**
     * 鎻掑叆楗璁板綍
     * @param record 楗璁板綍瀵硅薄
     * @return 鎻掑叆鐨勮褰旾D
     */
    @Insert
    long insert(MealRecord record);

    /**
     * 鏇存柊楗璁板綍
     * @param record 楗璁板綍瀵硅薄
     */
    @Update
    void update(MealRecord record);

    /**
     * 鍒犻櫎楗璁板綍
     * @param record 楗璁板綍瀵硅薄
     */
    @Delete
    void delete(MealRecord record);


    /**
     * 淇敼涓烘煡璇㈡寚瀹氭棩鏈熷綋澶╃殑鎵€鏈夎褰?     */
    @Query("SELECT * FROM meal_records WHERE date >= :startOfDay AND date < :endOfDay ORDER BY mealType ASC")
    List<MealRecord> getRecordsByDate(Date startOfDay, Date endOfDay);

    /**
     * 查询指定时间区间内的记录（左闭右开）
     */
    @Query("SELECT * FROM meal_records WHERE date >= :startInclusive AND date < :endExclusive ORDER BY date ASC, mealType ASC")
    List<MealRecord> getRecordsByDateRange(Date startInclusive, Date endExclusive);
}

