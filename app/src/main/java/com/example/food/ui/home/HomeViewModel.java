package com.example.food.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.example.food.db.AppDatabase;
import com.example.food.db.dao.MealRecordDao;
import com.example.food.db.entity.MealRecord;
import com.example.food.model.NutritionCalculator;
import com.example.food.model.UserGoal;
import com.example.food.utils.Constants;
import com.example.food.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 濡絾鐗犻妴澶愬极閻楀牆绁﹂悘鐐插亰缁辨壆鎷归悢鑽ょ厬闁圭顦板Λ?闁?闁哄牆鐗愭禒娑㈠触閸絾鍎夐柛蹇曠帛閺嗙喖骞戦琛″亾?
 */
public class HomeViewModel {

    public enum TimeGranularity {
        DAY,
        WEEK,
        MONTH
    }

    public interface OnHomeUiStateLoadedListener {
        void onStateLoaded(HomeUiState state);
    }

    private final Context context;
    private final MealRecordDao mealRecordDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private UserGoal userGoal;
    private NutritionCalculator.NutritionData todayNutritionData;

    public HomeViewModel(Context context) {
        this.context = context.getApplicationContext();
        this.mealRecordDao = AppDatabase.getInstance(this.context).mealRecordDao();
        loadUserGoal();
        todayNutritionData = new NutritionCalculator.NutritionData();
    }

    public void loadHomeUiState(Date selectedDate,
                                TimeGranularity granularity,
                                OnHomeUiStateLoadedListener listener) {
        final Date safeSelectedDate = DateUtils.getDateStart(selectedDate == null ? new Date() : selectedDate);

        new Thread(() -> {
            DateRange range = calculateVisibleRange(safeSelectedDate, granularity);
            String displayTitle = buildDisplayTitle(safeSelectedDate, granularity, range);

            NutritionCalculator.NutritionData dayNutritionData = new NutritionCalculator.NutritionData();
            FatRatioData fatRatioData = new FatRatioData();
            PeriodSeries periodSeries = null;

            if (granularity == TimeGranularity.DAY) {
                List<MealRecord> dayRecords = mealRecordDao.getRecordsByDateRange(range.startInclusive, range.endExclusive);
                dayNutritionData = aggregateNutrition(dayRecords);
                fatRatioData = buildFatRatio(dayNutritionData);
                todayNutritionData = dayNutritionData;
            } else {
                List<MealRecord> records = mealRecordDao.getRecordsByDateRange(range.startInclusive, range.endExclusive);
                periodSeries = buildPeriodSeries(records, range);
            }

            HomeUiState state = new HomeUiState(
                    safeSelectedDate,
                    granularity,
                    range,
                    displayTitle,
                    dayNutritionData,
                    fatRatioData,
                    periodSeries,
                    userGoal
            );

            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onStateLoaded(state);
                }
            });
        }).start();
    }

    private DateRange calculateVisibleRange(Date selectedDate, TimeGranularity granularity) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtils.getDateStart(selectedDate));

        if (granularity == TimeGranularity.DAY) {
            Date start = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return new DateRange(start, calendar.getTime());
        }

        if (granularity == TimeGranularity.WEEK) {
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            Date start = DateUtils.getDateStart(calendar.getTime());
            calendar.setTime(start);
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            return new DateRange(start, calendar.getTime());
        }

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date start = DateUtils.getDateStart(calendar.getTime());
        calendar.setTime(start);
        calendar.add(Calendar.MONTH, 1);
        return new DateRange(start, calendar.getTime());
    }

    private String buildDisplayTitle(Date selectedDate, TimeGranularity granularity, DateRange range) {
        if (granularity == TimeGranularity.DAY) {
            return DateUtils.formatDate(selectedDate, "yyyy年M月d日");
        }
        if (granularity == TimeGranularity.WEEK) {
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(range.endExclusive);
            endCal.add(Calendar.DAY_OF_MONTH, -1);
            String startText = DateUtils.formatDate(range.startInclusive, "yyyy年M月d日");
            String endText = DateUtils.formatDate(endCal.getTime(), "M月d日");
            return startText + "至" + endText;
        }
        return DateUtils.formatDate(selectedDate, "yyyy年M月");
    }

    private NutritionCalculator.NutritionData aggregateNutrition(List<MealRecord> records) {
        NutritionCalculator.NutritionData total = new NutritionCalculator.NutritionData();
        if (records == null) {
            return total;
        }
        for (MealRecord record : records) {
            total.add(new NutritionCalculator.NutritionData(
                    record.getCalories(),
                    record.getCarbohydrate(),
                    record.getProtein(),
                    record.getFat(),
                    record.getSaturatedFat(),
                    record.getMonounsaturatedFat(),
                    record.getPolyunsaturatedFat()
            ));
        }
        return total;
    }

    private PeriodSeries buildPeriodSeries(List<MealRecord> records, DateRange range) {
        Map<Long, DailyAggregatePoint> dayMap = new HashMap<>();
        if (records != null) {
            for (MealRecord record : records) {
                long dayStart = DateUtils.getDateStart(record.getDate()).getTime();
                DailyAggregatePoint point = dayMap.get(dayStart);
                if (point == null) {
                    point = new DailyAggregatePoint(new Date(dayStart));
                    dayMap.put(dayStart, point);
                }
                point.actualCalories += record.getCalories();
                point.actualCarb += record.getCarbohydrate();
                point.actualProtein += record.getProtein();
                point.actualFat += record.getFat();
            }
        }

        List<DailyAggregatePoint> points = new ArrayList<>();
        Date todayStart = DateUtils.getDateStart(new Date());
        Calendar cursor = Calendar.getInstance();
        cursor.setTime(range.startInclusive);

        while (cursor.getTime().before(range.endExclusive)) {
            Date day = DateUtils.getDateStart(cursor.getTime());
            long key = day.getTime();
            DailyAggregatePoint point = dayMap.get(key);
            if (point == null) {
                point = new DailyAggregatePoint(day);
            }
            point.isFuture = day.after(todayStart);
            if (point.isFuture) {
                point.actualCalories = 0;
                point.actualCarb = 0;
                point.actualProtein = 0;
                point.actualFat = 0;
            }
            points.add(point);
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }

        return new PeriodSeries(
                points,
                calculateCaloriesGoal(userGoal),
                userGoal.getCarbohydrateGoal(),
                userGoal.getProteinGoal(),
                userGoal.getFatGoal()
        );
    }

    private FatRatioData buildFatRatio(NutritionCalculator.NutritionData data) {
        double saturated = data.getSaturatedFat();
        double mono = data.getMonounsaturatedFat();
        double poly = data.getPolyunsaturatedFat();

        FatRatioData ratioData = new FatRatioData();
        ratioData.saturated = saturated;
        ratioData.mono = mono;
        ratioData.poly = poly;

        if (saturated <= 0) {
            ratioData.monoRatioToSat = 0;
            ratioData.polyRatioToSat = 0;
        } else {
            ratioData.monoRatioToSat = mono / saturated;
            ratioData.polyRatioToSat = poly / saturated;
        }
        return ratioData;
    }

    private void loadUserGoal() {
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        double caloriesGoal = preferences.getFloat(Constants.PREF_CALORIES_GOAL, (float) Constants.DEFAULT_CALORIES_GOAL);
        double carbohydrateGoal = preferences.getFloat(Constants.PREF_CARBOHYDRATE_GOAL, (float) Constants.DEFAULT_CARBOHYDRATE_GOAL);
        double proteinGoal = preferences.getFloat(Constants.PREF_PROTEIN_GOAL, (float) Constants.DEFAULT_PROTEIN_GOAL);
        double fatGoal = preferences.getFloat(Constants.PREF_FAT_GOAL, (float) Constants.DEFAULT_FAT_GOAL);
        double waterGoal = preferences.getFloat(Constants.PREF_WATER_GOAL, (float) Constants.DEFAULT_WATER_GOAL);

        userGoal = new UserGoal(caloriesGoal, carbohydrateGoal, proteinGoal, fatGoal, waterGoal);
    }

    public void saveUserGoal(UserGoal goal) {
        goal.setCaloriesGoal(calculateCaloriesGoal(goal));
        this.userGoal = goal;
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(Constants.PREF_CALORIES_GOAL, (float) goal.getCaloriesGoal());
        editor.putFloat(Constants.PREF_CARBOHYDRATE_GOAL, (float) goal.getCarbohydrateGoal());
        editor.putFloat(Constants.PREF_PROTEIN_GOAL, (float) goal.getProteinGoal());
        editor.putFloat(Constants.PREF_FAT_GOAL, (float) goal.getFatGoal());
        editor.putFloat(Constants.PREF_WATER_GOAL, (float) goal.getWaterGoal());
        editor.apply();
    }

    private double calculateCaloriesGoal(UserGoal goal) {
        if (goal == null) {
            return 0;
        }
        return goal.getCarbohydrateGoal() * NutritionCalculator.CALORIES_PER_GRAM_CARBOHYDRATE
                + goal.getProteinGoal() * NutritionCalculator.CALORIES_PER_GRAM_PROTEIN
                + goal.getFatGoal() * NutritionCalculator.CALORIES_PER_GRAM_FAT;
    }


    public UserGoal getUserGoal() {
        return userGoal;
    }

    public NutritionCalculator.NutritionData getTodayNutritionData() {
        return todayNutritionData;
    }

    public static class HomeUiState {
        public final Date selectedDate;
        public final TimeGranularity granularity;
        public final DateRange visibleRange;
        public final String displayTitle;
        public final NutritionCalculator.NutritionData dayNutritionData;
        public final FatRatioData fatRatioData;
        public final PeriodSeries periodSeries;
        public final UserGoal userGoal;

        public HomeUiState(Date selectedDate,
                           TimeGranularity granularity,
                           DateRange visibleRange,
                           String displayTitle,
                           NutritionCalculator.NutritionData dayNutritionData,
                           FatRatioData fatRatioData,
                           PeriodSeries periodSeries,
                           UserGoal userGoal) {
            this.selectedDate = selectedDate;
            this.granularity = granularity;
            this.visibleRange = visibleRange;
            this.displayTitle = displayTitle;
            this.dayNutritionData = dayNutritionData;
            this.fatRatioData = fatRatioData;
            this.periodSeries = periodSeries;
            this.userGoal = userGoal;
        }
    }

    public static class DateRange {
        public final Date startInclusive;
        public final Date endExclusive;

        public DateRange(Date startInclusive, Date endExclusive) {
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
        }
    }

    public static class DailyAggregatePoint {
        public final Date date;
        public double actualCalories;
        public double actualCarb;
        public double actualProtein;
        public double actualFat;
        public boolean isFuture;

        public DailyAggregatePoint(Date date) {
            this.date = date;
        }
    }

    public static class PeriodSeries {
        public final List<DailyAggregatePoint> points;
        public final double goalCalories;
        public final double goalCarb;
        public final double goalProtein;
        public final double goalFat;

        public PeriodSeries(List<DailyAggregatePoint> points,
                            double goalCalories,
                            double goalCarb,
                            double goalProtein,
                            double goalFat) {
            this.points = points;
            this.goalCalories = goalCalories;
            this.goalCarb = goalCarb;
            this.goalProtein = goalProtein;
            this.goalFat = goalFat;
        }
    }

    public static class FatRatioData {
        public double saturated;
        public double mono;
        public double poly;
        public double monoRatioToSat;
        public double polyRatioToSat;

        public String buildRatioText() {
            if (saturated <= 0) {
                return "濡ゆ宕幏浼存晬濮橆剙绀嬪☉鎾崇▌缁辩増寰勫顐ょ憹 = 0闁?闁?";
            }
            return String.format(Locale.CHINA,
                    "濡ゆ宕幏浼存晬濮橆剙绀嬪☉鎾崇▌缁辩増寰勫顐ょ憹 = 1闁?.2f闁?.2f",
                    monoRatioToSat,
                    polyRatioToSat);
        }
    }
}



