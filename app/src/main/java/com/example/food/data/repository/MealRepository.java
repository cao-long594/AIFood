package com.example.food.data.repository;

import android.content.Context;

import com.example.food.core.concurrent.AppExecutors;
import com.example.food.db.AppDatabase;
import com.example.food.db.dao.MealRecordDao;
import com.example.food.db.entity.MealRecord;

import java.util.Date;
import java.util.List;

public class MealRepository {

    public interface Callback<T> {
        void onResult(T data);
    }

    private final MealRecordDao mealRecordDao;

    public MealRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context.getApplicationContext());
        this.mealRecordDao = database.mealRecordDao();
    }

    public void getRecordsByDateRange(Date startInclusive, Date endExclusive, Callback<List<MealRecord>> callback) {
        AppExecutors.runOnIo(() -> {
            List<MealRecord> records = mealRecordDao.getRecordsByDateRange(startInclusive, endExclusive);
            AppExecutors.runOnMain(() -> callback.onResult(records));
        });
    }

    public void insert(MealRecord record, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            mealRecordDao.insert(record);
            AppExecutors.runOnMain(onComplete);
        });
    }

    public void update(MealRecord record, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            mealRecordDao.update(record);
            AppExecutors.runOnMain(onComplete);
        });
    }

    public void delete(MealRecord record, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            mealRecordDao.delete(record);
            AppExecutors.runOnMain(onComplete);
        });
    }
}