package com.example.food.db.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "foods")
public class Food implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private double calories;
    private double carbohydrate;
    private double protein;
    private double fat;
    private double saturatedFat;
    private double monounsaturatedFat;
    private double polyunsaturatedFat;
    private int unit;
    private int unitAmount;
    private String category;

    @ColumnInfo(name = "userId")
    private Integer userId;  // null = 公共食物, non-null = 用户私有食物

    @ColumnInfo(name = "source")
    private String source;   // "SYSTEM" = 系统导入/管理员导入, "USER" = 用户分享

    @ColumnInfo(name = "sourceUserName")
    private String sourceUserName; // 分享食物的用户名, null 表示系统导入

    @ColumnInfo(name = "visibilityStatus")
    private int visibilityStatus = 1; // 1=公开, 2=私密

    @ColumnInfo(name = "existStatus")
    private int existStatus = 1; // 1=未删除, 2=已删除

    public Food(String name, double calories, double carbohydrate, double protein,
                double fat, double saturatedFat, double monounsaturatedFat,
                double polyunsaturatedFat, int unit, int unitAmount, String category) {
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
        this.category = category;
    }

    @Ignore
    public Food(String name, double calories, double carbohydrate, double protein,
                double fat, double saturatedFat, double monounsaturatedFat,
                double polyunsaturatedFat, int unit, int unitAmount) {
        this(name, calories, carbohydrate, protein, fat, saturatedFat, monounsaturatedFat,
                polyunsaturatedFat, unit, unitAmount, null);
    }

    @Ignore
    public Food(int id, String name, double calories, double carbohydrate,
                double protein, double fat, double saturatedFat,
                double monounsaturatedFat, double polyunsaturatedFat,
                int unit, int unitAmount, String category) {
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
        this.category = category;
    }

    @Ignore
    public Food(int id, String name, double calories, double carbohydrate,
                double protein, double fat, double saturatedFat,
                double monounsaturatedFat, double polyunsaturatedFat,
                int unit, int unitAmount) {
        this(id, name, calories, carbohydrate, protein, fat, saturatedFat,
                monounsaturatedFat, polyunsaturatedFat, unit, unitAmount, null);
    }

    @Ignore
    public Food(String name, double calories, double carbohydrate, double protein,
                double fat, double saturatedFat, double monounsaturatedFat,
                double polyunsaturatedFat, int unit, int unitAmount, String category, Integer userId) {
        this(name, calories, carbohydrate, protein, fat, saturatedFat, monounsaturatedFat,
                polyunsaturatedFat, unit, unitAmount, category);
        this.userId = userId;
    }

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSourceUserName() { return sourceUserName; }
    public void setSourceUserName(String sourceUserName) { this.sourceUserName = sourceUserName; }

    public int getVisibilityStatus() { return visibilityStatus; }
    public void setVisibilityStatus(int visibilityStatus) { this.visibilityStatus = visibilityStatus; }

    public int getExistStatus() { return existStatus; }
    public void setExistStatus(int existStatus) { this.existStatus = existStatus; }

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
        category = in.readString();
        int tmpUserId = in.readInt();
        userId = tmpUserId == -1 ? null : tmpUserId;
        source = in.readString();
        sourceUserName = in.readString();
        visibilityStatus = in.readInt();
        existStatus = in.readInt();
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
        dest.writeString(category);
        dest.writeInt(userId != null ? userId : -1);
        dest.writeString(source);
        dest.writeString(sourceUserName);
        dest.writeInt(visibilityStatus);
        dest.writeInt(existStatus);
    }

    @Override
    public String toString() {
        return name;
    }
}
