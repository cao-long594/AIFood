package com.example.food.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.food.db.dao.FoodDao;
import com.example.food.db.dao.MealRecordDao;
import com.example.food.db.dao.WaterRecordDao;
import com.example.food.db.entity.Food;
import com.example.food.db.entity.MealRecord;
import com.example.food.db.entity.WaterRecord;

@Database(entities = {Food.class, MealRecord.class, WaterRecord.class}, version = 2, exportSchema = false)
@TypeConverters(DateTypeConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "food_app_database";
    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract FoodDao foodDao();

    public abstract MealRecordDao mealRecordDao();

    public abstract WaterRecordDao waterRecordDao();
}
