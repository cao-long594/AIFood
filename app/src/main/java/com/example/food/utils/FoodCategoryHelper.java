package com.example.food.utils;

import com.example.food.db.entity.Food;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class FoodCategoryHelper {

    public static final String CATEGORY_CARB = "carb";
    public static final String CATEGORY_PROTEIN = "protein";
    public static final String CATEGORY_FAT = "fat";
    public static final String CATEGORY_FRUIT = "fruit";

    private static final List<String> CATEGORY_ORDER = Arrays.asList(
            CATEGORY_CARB,
            CATEGORY_PROTEIN,
            CATEGORY_FAT,
            CATEGORY_FRUIT
    );

    private static final Set<String> VALID_CATEGORIES = new HashSet<>(CATEGORY_ORDER);

    private static final List<String> FRUIT_KEYWORDS = Arrays.asList(
            "水果", "果", "苹果", "香蕉", "梨", "桃", "橙", "橘", "柚", "柠檬", "青柠", "葡萄", "提子", "草莓", "蓝莓", "树莓",
            "黑莓", "芒果", "菠萝", "凤梨", "西瓜", "哈密瓜", "甜瓜", "木瓜", "火龙果", "猕猴桃", "奇异果", "榴莲", "荔枝", "龙眼",
            "桂圆", "樱桃", "车厘子", "石榴", "山竹", "百香果", "枣", "柿子", "柑", "橙子", "柚子", "李", "李子", "杏", "杏子", "梅",
            "杨梅", "桑葚", "无花果", "牛油果", "鳄梨", "fruit", "apple", "banana", "pear", "orange", "grape",
            "strawberry", "blueberry", "mango", "pineapple", "watermelon", "kiwi", "avocado", "cherry", "lemon"
    );

    private FoodCategoryHelper() {
    }

    public static List<String> getCategoryOrder() {
        return CATEGORY_ORDER;
    }

    public static String getDisplayName(String category) {
        String normalized = normalizeCategory(category);
        switch (normalized) {
            case CATEGORY_PROTEIN:
                return "蛋白";
            case CATEGORY_FAT:
                return "脂肪";
            case CATEGORY_FRUIT:
                return "水果";
            case CATEGORY_CARB:
            default:
                return "碳水";
        }
    }

    public static String normalizeCategory(String category) {
        if (category == null) {
            return "";
        }
        return category.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isValidCategory(String category) {
        return VALID_CATEGORIES.contains(normalizeCategory(category));
    }

    public static String resolveCategory(Food food) {
        if (food == null) {
            return CATEGORY_CARB;
        }

        String manualCategory = normalizeCategory(food.getCategory());
        if (isValidCategory(manualCategory)) {
            return manualCategory;
        }

        String name = safeLower(food.getName());
        for (String keyword : FRUIT_KEYWORDS) {
            if (!keyword.isEmpty() && name.contains(keyword.toLowerCase(Locale.ROOT))) {
                return CATEGORY_FRUIT;
            }
        }

        double carb = Math.max(0d, food.getCarbohydrate());
        double protein = Math.max(0d, food.getProtein());
        double fat = Math.max(0d, food.getFat());

        if (carb >= protein && carb >= fat) {
            return CATEGORY_CARB;
        }
        if (protein >= fat) {
            return CATEGORY_PROTEIN;
        }
        return CATEGORY_FAT;
    }

    private static String safeLower(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
