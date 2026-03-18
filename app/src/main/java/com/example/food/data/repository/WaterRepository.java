package com.example.food.data.repository;

import android.content.Context;

import com.example.food.core.concurrent.AppExecutors;
import com.example.food.db.AppDatabase;
import com.example.food.db.dao.WaterRecordDao;
import com.example.food.db.entity.WaterRecord;

import java.util.Date;
import java.util.List;

public class WaterRepository {

    public interface Callback<T> {
        void onResult(T data);
    }

    public static class WaterDayData {
        public final List<WaterRecord> records;
        public final double totalAmount;

        public WaterDayData(List<WaterRecord> records, double totalAmount) {
            this.records = records;
            this.totalAmount = totalAmount;
        }
    }

    private final WaterRecordDao waterRecordDao;

    public WaterRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context.getApplicationContext());
        this.waterRecordDao = database.waterRecordDao();
    }

    public void loadByDate(Date startDate, Date endDate, Callback<WaterDayData> callback) {
        AppExecutors.runOnIo(() -> {
            List<WaterRecord> records = waterRecordDao.getRecordsByDate(startDate, endDate);
            double totalAmount = waterRecordDao.getTotalAmountByDate(startDate, endDate);
            AppExecutors.runOnMain(() -> callback.onResult(new WaterDayData(records, totalAmount)));
        });
    }

    public void insert(double amount, Date time, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            waterRecordDao.insert(new WaterRecord(amount, time));
            AppExecutors.runOnMain(onComplete);
        });
    }

    public void delete(WaterRecord record, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            waterRecordDao.delete(record);
            AppExecutors.runOnMain(onComplete);
        });
    }
}