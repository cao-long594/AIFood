package com.example.food.utils;

/**
 * 甯搁噺瀹氫箟绫?
 * 瀛樺偍搴旂敤涓娇鐢ㄧ殑鍚勭甯搁噺
 */
public class Constants {
    // 椁愭绫诲瀷甯搁噺
    public static final String MEAL_TYPE_BREAKFAST = "breakfast"; // 鏃╅
    public static final String MEAL_TYPE_MORNING_SNACK = "morning_snack"; // 涓婂崍鍔犻
    public static final String MEAL_TYPE_LUNCH = "lunch"; // 鍗堥
    public static final String MEAL_TYPE_AFTERNOON_SNACK = "afternoon_snack"; // 涓嬪崍鍔犻
    public static final String MEAL_TYPE_DINNER = "dinner"; // 鏅氶
    public static final String MEAL_TYPE_EVENING_SNACK = "evening_snack"; // 鏅氫笂鍔犻
    public static final String MEAL_TYPE_BEDTIME = "bedtime"; // 鐫″墠椁?

    // 璁￠噺鍗曚綅甯搁噺
    public static final int UNIT_GRAM = 0; // 克
    public static final int UNIT_MILLILITER = 1; // 毫升

    // 缁熻鍛ㄦ湡甯搁噺
    public static final int PERIOD_DAY = 1; // 鏃?
    public static final int PERIOD_WEEK = 2; // 鍛?
    public static final int PERIOD_MONTH = 3; // 鏈?

    // 鏁版嵁搴撳悕绉?
    public static final String DATABASE_NAME = "food_app_database";

    // SharedPreferences 閿悕
    public static final String PREFS_NAME = "food_app_prefs";
    public static final String PREF_CALORIES_GOAL = "calories_goal";
    public static final String PREF_CARBOHYDRATE_GOAL = "carbohydrate_goal";
    public static final String PREF_PROTEIN_GOAL = "protein_goal";
    public static final String PREF_FAT_GOAL = "fat_goal";
    public static final String PREF_WATER_GOAL = "water_goal";
    public static final String PREF_DEFAULT_FOODS_IMPORTED = "default_foods_imported_v1";
    public static final String PREF_UNIT_VALUE_MIGRATED = "food_unit_migrated_v2";

    // 榛樿鐩爣鍊?
    public static final double DEFAULT_CALORIES_GOAL = 2000.0;
    public static final double DEFAULT_CARBOHYDRATE_GOAL = 250.0;
    public static final double DEFAULT_PROTEIN_GOAL = 60.0;
    public static final double DEFAULT_FAT_GOAL = 60.0;
    public static final double DEFAULT_WATER_GOAL = 2000.0;

    // 钀ュ吇绱犲弬鑰冨崰姣?
    public static final double CARBOHYDRATE_REF_MIN = 45.0; // 纰虫按鍙傝€冩渶灏忓€?%)
    public static final double CARBOHYDRATE_REF_MAX = 65.0; // 纰虫按鍙傝€冩渶澶у€?%)
    public static final double PROTEIN_REF_MIN = 15.0; // 铔嬬櫧璐ㄥ弬鑰冩渶灏忓€?%)
    public static final double PROTEIN_REF_MAX = 25.0; // 铔嬬櫧璐ㄥ弬鑰冩渶澶у€?%)
    public static final double FAT_REF_MIN = 20.0; // 鑴傝偑鍙傝€冩渶灏忓€?%)
    public static final double FAT_REF_MAX = 35.0; // 鑴傝偑鍙傝€冩渶澶у€?%)

    // 棰滆壊甯搁噺
    public static final int COLOR_CARBOHYDRATE = 0xFF4CAF50; // 纰虫按棰滆壊(缁胯壊)
    public static final int COLOR_PROTEIN = 0xFF2196F3; // 铔嬬櫧璐ㄩ鑹?钃濊壊)
    public static final int COLOR_FAT = 0xFFFF9800; // 鑴傝偑棰滆壊(姗欒壊)
    public static final int COLOR_SATURATED_FAT = 0xFFF44336; // 楗卞拰鑴傝偑棰滆壊(绾㈣壊)
    public static final int COLOR_MONOUNSATURATED_FAT = 0xFF9C27B0; // 鍗曚笉楗卞拰鑴傝偑棰滆壊(绱壊)
    public static final int COLOR_POLYUNSATURATED_FAT = 0xFF00BCD4; // 澶氫笉楗卞拰鑴傝偑棰滆壊(闈掕壊)
    public static final int COLOR_WATER = 0xFF2196F3; // 姘撮鑹?钃濊壊)

    // 鍔ㄧ敾鎸佺画鏃堕棿
    public static final int ANIMATION_DURATION = 1000; // 1绉?

    // 璇锋眰鐮?
    public static final int REQUEST_ADD_FOOD = 1001;
    public static final int REQUEST_EDIT_FOOD = 1002;
    public static final int REQUEST_ADD_MEAL = 1003;
    public static final int REQUEST_SET_GOALS = 1004;

    // 缁撴灉鐮?
    public static final int RESULT_ADD_SUCCESS = 2001;
    public static final int RESULT_EDIT_SUCCESS = 2002;
    public static final int RESULT_DELETE_SUCCESS = 2003;
    public static final int RESULT_GOALS_UPDATED = 2004;
}

