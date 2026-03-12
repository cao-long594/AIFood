package com.example.food.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * 饮水记录实体类
 * 记录用户的饮水情况
 */
@Entity(tableName = "water_records")
public class WaterRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private double amount; // 饮水量(毫升)
    private Date time; // 饮水时间
    private String waterType; // 水的类型，如矿泉水、绿茶等

    // 构造函数
    public WaterRecord(double amount, Date time) {
        this.amount = amount;
        this.time = time;
        this.waterType = "矿泉水"; // 默认类型
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getWaterType() {
        return waterType;
    }

    public void setWaterType(String waterType) {
        this.waterType = waterType;
    }
}