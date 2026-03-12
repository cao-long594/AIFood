package com.example.food.db.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 食物营养实体类
 * 存储食物的名称及各种营养成分信息
 */
@Entity(tableName = "foods")
public class Food implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name; // 食物名称
    private double calories; // 卡路里(kcal)
    private double carbohydrate; // 碳水化合物(g)
    private double protein; // 蛋白质(g)
    private double fat; // 脂肪(g)
    private double saturatedFat; // 饱和脂肪(g)
    private double monounsaturatedFat; // 单不饱和脂肪(g)
    private double polyunsaturatedFat; // 多不饱和脂肪(g)
    private int unit; // 计量单位(1:克, 2:毫升)
    private int unitAmount; // 每单位数量(如100克/100毫升)

    // 构造函数
    public Food(String name, double calories, double carbohydrate, double protein, 
                double fat, double saturatedFat, double monounsaturatedFat, 
                double polyunsaturatedFat, int unit, int unitAmount) {
        this.name = name;
        this.calories = calories;
        this.carbohydrate = carbohydrate;
        this.protein = protein;
        this.fat = fat;
        this.saturatedFat = saturatedFat;
        this.monounsaturatedFat = monounsaturatedFat;
        this.polyunsaturatedFat = polyunsaturatedFat;
        this.unit = unit;
        this.unitAmount = unitAmount;
    }

    @Ignore
    public Food(int id, String name, double calories, double carbohydrate,
                double protein, double fat, double saturatedFat,
                double monounsaturatedFat, double polyunsaturatedFat,
                int unit, int unitAmount) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.carbohydrate = carbohydrate;
        this.protein = protein;
        this.fat = fat;
        this.saturatedFat = saturatedFat;
        this.monounsaturatedFat = monounsaturatedFat;
        this.polyunsaturatedFat = polyunsaturatedFat;
        this.unit = unit;
        this.unitAmount = unitAmount;
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(double carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getSaturatedFat() {
        return saturatedFat;
    }

    public void setSaturatedFat(double saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    public double getMonounsaturatedFat() {
        return monounsaturatedFat;
    }

    public void setMonounsaturatedFat(double monounsaturatedFat) {
        this.monounsaturatedFat = monounsaturatedFat;
    }

    public double getPolyunsaturatedFat() {
        return polyunsaturatedFat;
    }

    public void setPolyunsaturatedFat(double polyunsaturatedFat) {
        this.polyunsaturatedFat = polyunsaturatedFat;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public int getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(int unitAmount) {
        this.unitAmount = unitAmount;
    }

    // Parcelable 实现
    protected Food(Parcel in) {
        id = in.readInt();
        name = in.readString();
        calories = in.readDouble();
        carbohydrate = in.readDouble();
        protein = in.readDouble();
        fat = in.readDouble();
        saturatedFat = in.readDouble();
        monounsaturatedFat = in.readDouble();
        polyunsaturatedFat = in.readDouble();
        unit = in.readInt();
        unitAmount = in.readInt();
    }

    public static final Creator<Food> CREATOR = new Creator<Food>() {
        @Override
        public Food createFromParcel(Parcel in) {
            return new Food(in);
        }

        @Override
        public Food[] newArray(int size) {
            return new Food[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeDouble(calories);
        dest.writeDouble(carbohydrate);
        dest.writeDouble(protein);
        dest.writeDouble(fat);
        dest.writeDouble(saturatedFat);
        dest.writeDouble(monounsaturatedFat);
        dest.writeDouble(polyunsaturatedFat);
        dest.writeInt(unit);
        dest.writeInt(unitAmount);
    }

    @Override
    public String toString() {
        return name;
    }
}