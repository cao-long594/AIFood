package com.example.food.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;

import java.util.Date;

/**
 * 饮食记录实体类
 * 记录用户每餐的食物摄入信息
 */
@Entity(tableName = "meal_records",
        foreignKeys = @ForeignKey(
            entity = Food.class,
            parentColumns = "id",
            childColumns = "foodId",
            onDelete = ForeignKey.CASCADE
        ),
        indices = {
            @Index(value = "foodId"),
            @Index(value = {"date", "mealType"})
        })
public class MealRecord {
    // 餐次类型常量定义
    public static final int MEAL_TYPE_BREAKFAST = 1;          // 早餐
    public static final int MEAL_TYPE_MORNING_SNACK = 2;      // 上午加餐
    public static final int MEAL_TYPE_LUNCH = 3;              // 中餐
    public static final int MEAL_TYPE_AFTERNOON_SNACK = 4;    // 下午加餐
    public static final int MEAL_TYPE_DINNER = 5;             // 晚餐
    public static final int MEAL_TYPE_EVENING_SNACK = 6;      // 晚上加餐
    public static final int MEAL_TYPE_BEDTIME = 7;            // 睡前餐

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int foodId; // 食物ID
    private String foodName; // 食物名称
    private double amount; // 摄入量(克或毫升)
    private Date date; // 记录日期
    @ColumnInfo(name = "created_at")
    private Date createdAt; // 创建时间
    private int mealType; // 餐次类型，使用常量表示
    
    // 营养成分字段
    private double calories; // 卡路里
    private double carbohydrate; // 碳水化合物
    private double protein; // 蛋白质
    private double fat; // 脂肪
    private double saturatedFat; // 饱和脂肪
    private double monounsaturatedFat; // 单不饱和脂肪
    private double polyunsaturatedFat; // 多不饱和脂肪

    // 构造函数
    public MealRecord(int foodId, String foodName, double amount, Date date, Date createdAt, int mealType,
                     double calories, double carbohydrate, double protein, double fat, 
                     double saturatedFat, double monounsaturatedFat, double polyunsaturatedFat) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.amount = amount;
        this.date = date;
        this.createdAt = createdAt;
        this.mealType = mealType;
        this.calories = calories;
        this.carbohydrate = carbohydrate;
        this.protein = protein;
        this.fat = fat;
        this.saturatedFat = saturatedFat;
        this.monounsaturatedFat = monounsaturatedFat;
        this.polyunsaturatedFat = polyunsaturatedFat;
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getMealType() {
        return mealType;
    }

    public void setMealType(int mealType) {
        this.mealType = mealType;
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

    /**
     * 根据餐次类型获取对应的餐次名称
     * @param mealType 餐次类型
     * @return 餐次名称
     */
    public static String getMealTypeName(int mealType) {
        switch (mealType) {
            case MEAL_TYPE_BREAKFAST:
                return "早餐";
            case MEAL_TYPE_MORNING_SNACK:
                return "上午加餐";
            case MEAL_TYPE_LUNCH:
                return "中餐";
            case MEAL_TYPE_AFTERNOON_SNACK:
                return "下午加餐";
            case MEAL_TYPE_DINNER:
                return "晚餐";
            case MEAL_TYPE_EVENING_SNACK:
                return "晚上加餐";
            case MEAL_TYPE_BEDTIME:
                return "睡前餐";
            default:
                return "未知餐次";
        }
    }
}