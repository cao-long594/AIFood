package com.example.food.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import com.example.food.db.entity.WaterRecord;

import java.util.Date;
import java.util.List;

/**
 * 饮水记录DAO接口
 * 提供对饮水记录表的增删改查操作
 */
@Dao
public interface WaterRecordDao {
    /**
     * 插入饮水记录
     * @param record 饮水记录对象
     * @return 插入的记录ID
     */
    @Insert
    long insert(WaterRecord record);

    /**
     * 删除饮水记录
     * @param record 饮水记录对象
     */
    @Delete
    void delete(WaterRecord record);


    /**
     * 查询指定日期的所有饮水记录
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 饮水记录列表（按时间升序排列，最新记录在底部）
     */
    @Query("SELECT * FROM water_records WHERE time BETWEEN :startDate AND :endDate ORDER BY time ASC")
    List<WaterRecord> getRecordsByDate(Date startDate, Date endDate);

    /**
     * 查询指定日期的饮水总量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 饮水总量
     */
    @Query("SELECT SUM(amount) FROM water_records WHERE time BETWEEN :startDate AND :endDate")
    double getTotalAmountByDate(Date startDate, Date endDate);


}