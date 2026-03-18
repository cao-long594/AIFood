package com.example.food.utils;

/**
 * 常量定义类
 * 存储应用中使用的各种常量
 */
public class Constants {

    // 餐次类型常量
    public static final String MEAL_TYPE_BREAKFAST = "breakfast";         // 早餐
    public static final String MEAL_TYPE_MORNING_SNACK = "morning_snack"; // 上午加餐
    public static final String MEAL_TYPE_LUNCH = "lunch";                 // 午餐
    public static final String MEAL_TYPE_AFTERNOON_SNACK = "afternoon_snack"; // 下午加餐
    public static final String MEAL_TYPE_DINNER = "dinner";               // 晚餐
    public static final String MEAL_TYPE_EVENING_SNACK = "evening_snack"; // 晚上加餐
    public static final String MEAL_TYPE_BEDTIME = "bedtime";             // 睡前餐

    // 计量单位常量
    public static final int UNIT_GRAM = 0;       // 克
    public static final int UNIT_MILLILITER = 1; // 毫升

    // 统计周期常量
    public static final int PERIOD_DAY = 1;   // 日
    public static final int PERIOD_WEEK = 2;  // 周
    public static final int PERIOD_MONTH = 3; // 月

    // 数据库名称
    public static final String DATABASE_NAME = "food_app_database";

    // SharedPreferences 键名
    public static final String PREFS_NAME = "food_app_prefs";
    public static final String PREF_CALORIES_GOAL = "calories_goal";
    public static final String PREF_CARBOHYDRATE_GOAL = "carbohydrate_goal";
    public static final String PREF_PROTEIN_GOAL = "protein_goal";
    public static final String PREF_FAT_GOAL = "fat_goal";
    public static final String PREF_WATER_GOAL = "water_goal";
    public static final String PREF_DEFAULT_FOODS_IMPORTED = "default_foods_imported_v1";
    public static final String PREF_UNIT_VALUE_MIGRATED = "food_unit_migrated_v2";

    // 默认目标值
    public static final double DEFAULT_CALORIES_GOAL = 2000.0;
    public static final double DEFAULT_CARBOHYDRATE_GOAL = 250.0;
    public static final double DEFAULT_PROTEIN_GOAL = 60.0;
    public static final double DEFAULT_FAT_GOAL = 60.0;
    public static final double DEFAULT_WATER_GOAL = 2000.0;

    // 营养素参考占比
    public static final double CARBOHYDRATE_REF_MIN = 45.0; // 碳水参考最小值(%)
    public static final double CARBOHYDRATE_REF_MAX = 65.0; // 碳水参考最大值(%)
    public static final double PROTEIN_REF_MIN = 15.0;      // 蛋白质参考最小值(%)
    public static final double PROTEIN_REF_MAX = 25.0;      // 蛋白质参考最大值(%)
    public static final double FAT_REF_MIN = 20.0;          // 脂肪参考最小值(%)
    public static final double FAT_REF_MAX = 35.0;          // 脂肪参考最大值(%)

    // 颜色常量
    public static final int COLOR_CARBOHYDRATE = 0xFF4CAF50;       // 碳水颜色（绿色）
    public static final int COLOR_PROTEIN = 0xFF2196F3;             // 蛋白质颜色（蓝色）
    public static final int COLOR_FAT = 0xFFFF9800;                 // 脂肪颜色（橙色）
    public static final int COLOR_SATURATED_FAT = 0xFFF44336;      // 饱和脂肪颜色（红色）
    public static final int COLOR_MONOUNSATURATED_FAT = 0xFF9C27B0; // 单不饱和脂肪颜色（紫色）
    public static final int COLOR_POLYUNSATURATED_FAT = 0xFF00BCD4; // 多不饱和脂肪颜色（青色）
    public static final int COLOR_WATER = 0xFF2196F3;               // 水颜色（蓝色）

    // 动画持续时间
    public static final int ANIMATION_DURATION = 1000; // 1秒

    // 请求码
    public static final int REQUEST_ADD_FOOD = 1001;
    public static final int REQUEST_EDIT_FOOD = 1002;
    public static final int REQUEST_ADD_MEAL = 1003;
    public static final int REQUEST_SET_GOALS = 1004;

    // 结果码
    public static final int RESULT_ADD_SUCCESS = 2001;
    public static final int RESULT_EDIT_SUCCESS = 2002;
    public static final int RESULT_DELETE_SUCCESS = 2003;
    public static final int RESULT_GOALS_UPDATED = 2004;
}