package com.example.food.model;

/**
 * 用户目标设置模型
 * 存储用户的营养摄入目标
 */
public class UserGoal {
    private double caloriesGoal; // 卡路里目标(kcal)
    private double carbohydrateGoal; // 碳水化合物目标(g)
    private double proteinGoal; // 蛋白质目标(g)
    private double fatGoal; // 脂肪目标(g)
    private double waterGoal; // 饮水量目标(ml)

    // 构造函数
    public UserGoal(double caloriesGoal, double carbohydrateGoal, double proteinGoal, 
                   double fatGoal, double waterGoal) {
        this.caloriesGoal = caloriesGoal;
        this.carbohydrateGoal = carbohydrateGoal;
        this.proteinGoal = proteinGoal;
        this.fatGoal = fatGoal;
        this.waterGoal = waterGoal;
    }

    // 默认构造函数
    public UserGoal() {
        // 默认目标值
        this.caloriesGoal = 2000;
        this.carbohydrateGoal = 250;
        this.proteinGoal = 60;
        this.fatGoal = 60;
        this.waterGoal = 2000;
    }

    // Getter和Setter方法
    public double getCaloriesGoal() {
        return caloriesGoal;
    }

    public void setCaloriesGoal(double caloriesGoal) {
        this.caloriesGoal = caloriesGoal;
    }

    public double getCarbohydrateGoal() {
        return carbohydrateGoal;
    }

    public void setCarbohydrateGoal(double carbohydrateGoal) {
        this.carbohydrateGoal = carbohydrateGoal;
    }

    public double getProteinGoal() {
        return proteinGoal;
    }

    public void setProteinGoal(double proteinGoal) {
        this.proteinGoal = proteinGoal;
    }

    public double getFatGoal() {
        return fatGoal;
    }

    public void setFatGoal(double fatGoal) {
        this.fatGoal = fatGoal;
    }

    public double getWaterGoal() {
        return waterGoal;
    }

    public void setWaterGoal(double waterGoal) {
        this.waterGoal = waterGoal;
    }
}