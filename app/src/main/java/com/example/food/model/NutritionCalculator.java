package com.example.food.model;

import com.example.food.db.entity.Food;
import com.example.food.db.entity.MealRecord;

import java.util.List;

/**
 * 营养计算工具类
 * 用于计算各种营养摄入数据
 */
public class NutritionCalculator {
    // 常量定义
    public static final double CALORIES_PER_GRAM_CARBOHYDRATE = 4.0; // 每克碳水提供4卡路里
    public static final double CALORIES_PER_GRAM_PROTEIN = 4.0; // 每克蛋白质提供4卡路里
    public static final double CALORIES_PER_GRAM_FAT = 9.0; // 每克脂肪提供9卡路里

    /**
     * 根据食物和摄入量计算实际摄入的营养成分
     * @param food 食物对象
     * @param amount 摄入量(克或毫升)
     * @return 计算后的营养数据
     */
    public static NutritionData calculateNutrition(Food food, double amount) {
        if (food == null) {
            return new NutritionData();
        }

        // 计算比例
        double ratio = amount / food.getUnitAmount();

        // 计算实际摄入量
        double calories = food.getCalories() * ratio;
        double carbohydrate = food.getCarbohydrate() * ratio;
        double protein = food.getProtein() * ratio;
        double fat = food.getFat() * ratio;
        double saturatedFat = food.getSaturatedFat() * ratio;
        double monounsaturatedFat = food.getMonounsaturatedFat() * ratio;
        double polyunsaturatedFat = food.getPolyunsaturatedFat() * ratio;

        return new NutritionData(calories, carbohydrate, protein, fat, 
                                saturatedFat, monounsaturatedFat, polyunsaturatedFat);
    }

    /**
     * 根据饮食记录列表计算总营养摄入
     * @param records 饮食记录列表
     * @param foods 对应的食物列表
     * @return 总营养数据
     */
    public static NutritionData calculateTotalNutrition(List<MealRecord> records, List<Food> foods) {
        NutritionData total = new NutritionData();

        if (records == null || foods == null || records.isEmpty() || foods.size() != records.size()) {
            return total;
        }

        for (int i = 0; i < records.size(); i++) {
            MealRecord record = records.get(i);
            Food food = foods.get(i);
            NutritionData data = calculateNutrition(food, record.getAmount());
            total.add(data);
        }

        return total;
    }

    /**
     * 计算营养素热量占比
     * @param nutritionData 营养数据
     * @return 营养素热量分布
     */
    public static NutrientDistribution calculateNutrientDistribution(NutritionData nutritionData) {
        double totalCalories = nutritionData.getCalories();
        if (totalCalories <= 0) {
            return new NutrientDistribution(0, 0, 0);
        }

        // 计算各营养素提供的热量
        double carbCalories = nutritionData.getCarbohydrate() * CALORIES_PER_GRAM_CARBOHYDRATE;
        double proteinCalories = nutritionData.getProtein() * CALORIES_PER_GRAM_PROTEIN;
        double fatCalories = nutritionData.getFat() * CALORIES_PER_GRAM_FAT;

        // 计算占比
        double carbPercentage = (carbCalories / totalCalories) * 100;
        double proteinPercentage = (proteinCalories / totalCalories) * 100;
        double fatPercentage = (fatCalories / totalCalories) * 100;

        return new NutrientDistribution(carbPercentage, proteinPercentage, fatPercentage);
    }

    /**
     * 计算脂肪类型分布
     * @param nutritionData 营养数据
     * @return 脂肪类型分布
     */
    public static FatDistribution calculateFatDistribution(NutritionData nutritionData) {
        double totalFat = nutritionData.getFat();
        if (totalFat <= 0) {
            return new FatDistribution(0, 0, 0);
        }

        double saturatedPercentage = (nutritionData.getSaturatedFat() / totalFat) * 100;
        double monounsaturatedPercentage = (nutritionData.getMonounsaturatedFat() / totalFat) * 100;
        double polyunsaturatedPercentage = (nutritionData.getPolyunsaturatedFat() / totalFat) * 100;

        return new FatDistribution(saturatedPercentage, monounsaturatedPercentage, polyunsaturatedPercentage);
    }

    /**
     * 营养数据内部类
     */
    public static class NutritionData {
        private double calories; // 卡路里
        private double carbohydrate; // 碳水化合物
        private double protein; // 蛋白质
        private double fat; // 脂肪
        private double saturatedFat; // 饱和脂肪
        private double monounsaturatedFat; // 单不饱和脂肪
        private double polyunsaturatedFat; // 多不饱和脂肪

        public NutritionData() {
            this(0, 0, 0, 0, 0, 0, 0);
        }

        public NutritionData(double calories, double carbohydrate, double protein, double fat,
                           double saturatedFat, double monounsaturatedFat, double polyunsaturatedFat) {
            this.calories = calories;
            this.carbohydrate = carbohydrate;
            this.protein = protein;
            this.fat = fat;
            this.saturatedFat = saturatedFat;
            this.monounsaturatedFat = monounsaturatedFat;
            this.polyunsaturatedFat = polyunsaturatedFat;
        }

        // 添加另一个营养数据
        public void add(NutritionData other) {
            this.calories += other.calories;
            this.carbohydrate += other.carbohydrate;
            this.protein += other.protein;
            this.fat += other.fat;
            this.saturatedFat += other.saturatedFat;
            this.monounsaturatedFat += other.monounsaturatedFat;
            this.polyunsaturatedFat += other.polyunsaturatedFat;
        }

        // Getter方法
        public double getCalories() { return calories; }
        public double getCarbohydrate() { return carbohydrate; }
        public double getProtein() { return protein; }
        public double getFat() { return fat; }
        public double getSaturatedFat() { return saturatedFat; }
        public double getMonounsaturatedFat() { return monounsaturatedFat; }
        public double getPolyunsaturatedFat() { return polyunsaturatedFat; }
    }

    /**
     * 营养素分布内部类
     */
    public static class NutrientDistribution {
        private double carbohydratePercentage; // 碳水占比
        private double proteinPercentage; // 蛋白质占比
        private double fatPercentage; // 脂肪占比

        public NutrientDistribution(double carbohydratePercentage, double proteinPercentage, double fatPercentage) {
            this.carbohydratePercentage = carbohydratePercentage;
            this.proteinPercentage = proteinPercentage;
            this.fatPercentage = fatPercentage;
        }

        // Getter方法
        public double getCarbohydratePercentage() { return carbohydratePercentage; }
        public double getProteinPercentage() { return proteinPercentage; }
        public double getFatPercentage() { return fatPercentage; }
    }

    /**
     * 脂肪分布内部类
     */
    public static class FatDistribution {
        private double saturatedPercentage; // 饱和脂肪占比
        private double monounsaturatedPercentage; // 单不饱和脂肪占比
        private double polyunsaturatedPercentage; // 多不饱和脂肪占比

        public FatDistribution(double saturatedPercentage, double monounsaturatedPercentage, double polyunsaturatedPercentage) {
            this.saturatedPercentage = saturatedPercentage;
            this.monounsaturatedPercentage = monounsaturatedPercentage;
            this.polyunsaturatedPercentage = polyunsaturatedPercentage;
        }

        // Getter方法
        public double getSaturatedPercentage() { return saturatedPercentage; }
        public double getMonounsaturatedPercentage() { return monounsaturatedPercentage; }
        public double getPolyunsaturatedPercentage() { return polyunsaturatedPercentage; }
    }
}