package com.example.food.db;

import androidx.room.TypeConverter;
import java.util.Date;

/**
 * 日期类型转换器
 * 用于Room数据库中Date类型与Long类型的相互转换
 */
public class DateTypeConverter {

    /**
     * 将Date对象转换为Long时间戳
     * @param date Date对象
     * @return 时间戳（毫秒）
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    /**
     * 将Long时间戳转换为Date对象
     * @param timestamp 时间戳（毫秒）
     * @return Date对象
     */
    @TypeConverter
    public static Date timestampToDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
}