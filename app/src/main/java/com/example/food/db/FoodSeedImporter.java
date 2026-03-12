package com.example.food.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.food.R;
import com.example.food.db.dao.FoodDao;
import com.example.food.db.entity.Food;
import com.example.food.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FoodSeedImporter {

    private static final String TAG = "FoodSeedImporter";
    private static final Object LOCK = new Object();

    private FoodSeedImporter() {
    }

    public static void ensureImported(Context context, AppDatabase database) {
        if (context == null || database == null) {
            return;
        }

        synchronized (LOCK) {
            Context appContext = context.getApplicationContext();
            SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
            FoodDao foodDao = database.foodDao();

            if (!prefs.getBoolean(Constants.PREF_UNIT_VALUE_MIGRATED, false)) {
                foodDao.migrateUnitValuesToV2();
                prefs.edit().putBoolean(Constants.PREF_UNIT_VALUE_MIGRATED, true).apply();
            }

            if (prefs.getBoolean(Constants.PREF_DEFAULT_FOODS_IMPORTED, false)) {
                return;
            }

            try {
                List<Food> defaultFoods = readDefaultFoods(appContext);
                Set<String> existingNames = new HashSet<>();
                List<String> dbNames = foodDao.getAllFoodNames();
                if (dbNames != null) {
                    for (String name : dbNames) {
                        if (name != null) {
                            existingNames.add(name.trim());
                        }
                    }
                }

                database.runInTransaction(() -> {
                    for (Food food : defaultFoods) {
                        String name = food.getName() == null ? "" : food.getName().trim();
                        if (name.isEmpty() || existingNames.contains(name)) {
                            continue;
                        }
                        foodDao.insert(food);
                        existingNames.add(name);
                    }
                });

                prefs.edit().putBoolean(Constants.PREF_DEFAULT_FOODS_IMPORTED, true).apply();
            } catch (Exception e) {
                Log.e(TAG, "Failed to import default foods", e);
            }
        }
    }

    private static List<Food> readDefaultFoods(Context context) throws IOException, JSONException {
        String json = readRawJson(context, R.raw.default_foods);
        JSONArray array = new JSONArray(json);
        List<Food> foods = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            String name = item.optString("name", "").trim();
            if (name.isEmpty()) {
                continue;
            }

            int unit = item.optInt("unit", Constants.UNIT_GRAM);
            if (unit != Constants.UNIT_GRAM && unit != Constants.UNIT_MILLILITER) {
                unit = Constants.UNIT_GRAM;
            }
            int unitAmount = item.optInt("unitAmount", 100);
            if (unitAmount <= 0) {
                unitAmount = 100;
            }

            foods.add(new Food(
                    name,
                    item.optDouble("calories", 0),
                    item.optDouble("carbohydrate", 0),
                    item.optDouble("protein", 0),
                    item.optDouble("fat", 0),
                    item.optDouble("saturatedFat", 0),
                    item.optDouble("monounsaturatedFat", 0),
                    item.optDouble("polyunsaturatedFat", 0),
                    unit,
                    unitAmount
            ));
        }

        return foods;
    }

    private static String readRawJson(Context context, int rawResId) throws IOException {
        try (InputStream inputStream = context.getResources().openRawResource(rawResId);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toString(StandardCharsets.UTF_8.name());
        }
    }
}

