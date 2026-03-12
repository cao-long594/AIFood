package com.example.food.ui.history;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.food.db.AppDatabase;
import com.example.food.db.dao.MealRecordDao;
import com.example.food.db.entity.MealRecord;
import com.example.food.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HistoryViewModel {

    public enum HistoryViewType {
        DAY,
        WEEK,
        MONTH
    }

    public interface OnHistoryStateLoadedListener {
        void onStateLoaded(HistoryUiState state);
    }

    private final MealRecordDao mealRecordDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public HistoryViewModel(Context context) {
        this.mealRecordDao = AppDatabase.getInstance(context.getApplicationContext()).mealRecordDao();
    }

    public Date resolveAnchorDate(long anchorDateMillis) {
        return DateUtils.getDateStart(new Date(anchorDateMillis));
    }

    public void loadHistoryState(Date anchorDate,
                                 HistoryViewType viewType,
                                 OnHistoryStateLoadedListener listener) {
        final Date safeAnchorDate = DateUtils.getDateStart(anchorDate == null ? new Date() : anchorDate);

        new Thread(() -> {
            DateRange visibleRange = calculateRange(safeAnchorDate, viewType);
            List<MealRecord> records = mealRecordDao.getRecordsByDateRange(visibleRange.startInclusive, visibleRange.endExclusive);

            HistoryUiState state = new HistoryUiState(
                    safeAnchorDate,
                    viewType,
                    visibleRange,
                    buildDisplayTitle(safeAnchorDate, viewType, visibleRange),
                    buildEmptyMessage(viewType),
                    buildRecordItems(records),
                    buildChartPoints(records, visibleRange, viewType, ChartMetric.CALORIES),
                    buildChartPoints(records, visibleRange, viewType, ChartMetric.CARBOHYDRATE)
            );

            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onStateLoaded(state);
                }
            });
        }).start();
    }

    private DateRange calculateRange(Date anchorDate, HistoryViewType viewType) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtils.getDateStart(anchorDate));

        if (viewType == HistoryViewType.DAY) {
            Date start = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return new DateRange(start, calendar.getTime());
        }

        if (viewType == HistoryViewType.WEEK) {
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int offset = dayOfWeek == Calendar.SUNDAY ? -6 : Calendar.MONDAY - dayOfWeek;
            calendar.add(Calendar.DAY_OF_MONTH, offset);
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

    private String buildDisplayTitle(Date anchorDate, HistoryViewType viewType, DateRange range) {
        if (viewType == HistoryViewType.DAY) {
            return DateUtils.formatDate(anchorDate, "yyyy年M月d日");
        }
        if (viewType == HistoryViewType.WEEK) {
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(range.endExclusive);
            endCalendar.add(Calendar.DAY_OF_MONTH, -1);
            return DateUtils.formatDate(range.startInclusive, "yyyy年M月d日")
                    + "至"
                    + DateUtils.formatDate(endCalendar.getTime(), "M月d日");
        }
        return DateUtils.formatDate(anchorDate, "yyyy年M月");
    }

    private String buildEmptyMessage(HistoryViewType viewType) {
        if (viewType == HistoryViewType.DAY) {
            return "当天暂无记录";
        }
        if (viewType == HistoryViewType.WEEK) {
            return "本周暂无记录";
        }
        return "本月暂无记录";
    }

    private List<HistoryRecordItem> buildRecordItems(List<MealRecord> records) {
        List<HistoryRecordItem> items = new ArrayList<>();
        for (MealRecord record : records) {
            items.add(new HistoryRecordItem(
                    MealRecord.getMealTypeName(record.getMealType()),
                    record.getFoodName(),
                    DateUtils.formatDate(record.getDate(), "M月d日 HH:mm"),
                    record.getCalories(),
                    record.getCarbohydrate()
            ));
        }
        return items;
    }

    private List<ChartPoint> buildChartPoints(List<MealRecord> records,
                                              DateRange range,
                                              HistoryViewType viewType,
                                              ChartMetric metric) {
        if (viewType == HistoryViewType.DAY) {
            return buildDayChartPoints(records, metric);
        }
        if (viewType == HistoryViewType.WEEK) {
            return buildContinuousDateChartPoints(records, range, "M/d", metric);
        }
        return buildContinuousDateChartPoints(records, range, "d", metric);
    }

    private List<ChartPoint> buildDayChartPoints(List<MealRecord> records, ChartMetric metric) {
        int[] mealTypes = {
                MealRecord.MEAL_TYPE_BREAKFAST,
                MealRecord.MEAL_TYPE_LUNCH,
                MealRecord.MEAL_TYPE_AFTERNOON_SNACK,
                MealRecord.MEAL_TYPE_DINNER,
                MealRecord.MEAL_TYPE_BEDTIME
        };
        Map<Integer, Double> aggregates = new LinkedHashMap<>();
        for (int mealType : mealTypes) {
            aggregates.put(mealType, 0d);
        }

        for (MealRecord record : records) {
            if (!aggregates.containsKey(record.getMealType())) {
                aggregates.put(record.getMealType(), 0d);
            }
            aggregates.put(record.getMealType(), aggregates.get(record.getMealType()) + extractMetric(record, metric));
        }

        List<ChartPoint> points = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : aggregates.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            points.add(new ChartPoint(shortenMealLabel(MealRecord.getMealTypeName(entry.getKey())), entry.getValue()));
        }
        return points;
    }

    private List<ChartPoint> buildContinuousDateChartPoints(List<MealRecord> records,
                                                            DateRange range,
                                                            String labelPattern,
                                                            ChartMetric metric) {
        Map<Long, Double> aggregates = new LinkedHashMap<>();
        Calendar cursor = Calendar.getInstance();
        cursor.setTime(range.startInclusive);

        while (cursor.getTime().before(range.endExclusive)) {
            Date currentDate = DateUtils.getDateStart(cursor.getTime());
            aggregates.put(currentDate.getTime(), 0d);
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (MealRecord record : records) {
            long key = DateUtils.getDateStart(record.getDate()).getTime();
            if (aggregates.containsKey(key)) {
                aggregates.put(key, aggregates.get(key) + extractMetric(record, metric));
            }
        }

        List<ChartPoint> points = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : aggregates.entrySet()) {
            points.add(new ChartPoint(
                    DateUtils.formatDate(new Date(entry.getKey()), labelPattern),
                    entry.getValue()
            ));
        }
        return points;
    }

    private double extractMetric(MealRecord record, ChartMetric metric) {
        if (metric == ChartMetric.CALORIES) {
            return record.getCalories();
        }
        return record.getCarbohydrate();
    }

    private String shortenMealLabel(String mealName) {
        if ("早餐".equals(mealName)) {
            return "早";
        }
        if ("午餐".equals(mealName)) {
            return "午";
        }
        if ("下午加餐".equals(mealName)) {
            return "加";
        }
        if ("晚餐".equals(mealName)) {
            return "晚";
        }
        if ("睡前餐".equals(mealName)) {
            return "夜";
        }
        return mealName;
    }

    private enum ChartMetric {
        CALORIES,
        CARBOHYDRATE
    }

    public static class HistoryUiState {
        public final Date anchorDate;
        public final HistoryViewType viewType;
        public final DateRange visibleRange;
        public final String displayTitle;
        public final String emptyMessage;
        public final List<HistoryRecordItem> records;
        public final List<ChartPoint> calorieChartPoints;
        public final List<ChartPoint> carbChartPoints;

        public HistoryUiState(Date anchorDate,
                              HistoryViewType viewType,
                              DateRange visibleRange,
                              String displayTitle,
                              String emptyMessage,
                              List<HistoryRecordItem> records,
                              List<ChartPoint> calorieChartPoints,
                              List<ChartPoint> carbChartPoints) {
            this.anchorDate = anchorDate;
            this.viewType = viewType;
            this.visibleRange = visibleRange;
            this.displayTitle = displayTitle;
            this.emptyMessage = emptyMessage;
            this.records = records;
            this.calorieChartPoints = calorieChartPoints;
            this.carbChartPoints = carbChartPoints;
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

    public static class HistoryRecordItem {
        public final String mealTypeLabel;
        public final String foodName;
        public final String timeLabel;
        public final double calories;
        public final double carbohydrate;

        public HistoryRecordItem(String mealTypeLabel,
                                 String foodName,
                                 String timeLabel,
                                 double calories,
                                 double carbohydrate) {
            this.mealTypeLabel = mealTypeLabel;
            this.foodName = foodName;
            this.timeLabel = timeLabel;
            this.calories = calories;
            this.carbohydrate = carbohydrate;
        }
    }

    public static class ChartPoint {
        public final String label;
        public final double value;

        public ChartPoint(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }
}
