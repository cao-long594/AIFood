package com.example.food.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateUtils {

    // 保留有实际引用的格式常量；DATE_FORMAT_YMD / YMD_HM / MD / CHINESE 均无引用已删除
    public static final String DATE_FORMAT_HM = "HH:mm";

    // 星期名称，供 getWeekDay() 使用
    private static final String[] WEEKDAYS = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    private DateUtils() {
    }

    public static String formatDate(Date date, String format) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(format, Locale.CHINA).format(date);
    }

    public static Date getTodayStart() {
        return getDateStart(new Date());
    }

    public static Date getTodayEnd() {
        return getDateEnd(new Date());
    }

    public static Date getDateStart(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getDateEnd(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static String getWeekDay(Date date) {
        if (date == null) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return WEEKDAYS[calendar.get(Calendar.DAY_OF_WEEK) - 1];
    }

    public static Date getWeekStart() {
        return getWeekStart(new Date());
    }

    public static Date getWeekEnd() {
        return getWeekEnd(new Date());
    }

    public static Date getWeekStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDateStart(date));
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int offset = dayOfWeek == Calendar.SUNDAY ? -6 : Calendar.MONDAY - dayOfWeek;
        calendar.add(Calendar.DAY_OF_MONTH, offset);
        return getDateStart(calendar.getTime());
    }

    public static Date getWeekEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getWeekStart(date));
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        return getDateEnd(calendar.getTime());
    }

    public static Date getMonthStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDateStart(date));
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return getDateStart(calendar.getTime());
    }

    public static Date getMonthStart() {
        return getMonthStart(new Date());
    }

    public static Date getMonthEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getMonthStart(date));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return getDateEnd(calendar.getTime());
    }

    public static Date getMonthEnd() {
        return getMonthEnd(new Date());
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isSameMonth(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    public static boolean isSameWeek(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return isSameDay(getWeekStart(date1), getWeekStart(date2));
    }

    public static Calendar[] getMonthDates(int year, int month) {
        Calendar firstDay = Calendar.getInstance();
        firstDay.clear();
        firstDay.set(year, month, 1, 0, 0, 0);
        firstDay.setFirstDayOfWeek(Calendar.MONDAY);

        int dayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
        int offset = dayOfWeek == Calendar.SUNDAY ? 6 : dayOfWeek - Calendar.MONDAY;
        firstDay.add(Calendar.DAY_OF_MONTH, -offset);

        Calendar[] dates = new Calendar[42];
        for (int i = 0; i < dates.length; i++) {
            dates[i] = (Calendar) firstDay.clone();
            firstDay.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dates;
    }

    public static int getDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, 1, 0, 0, 0);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static Date getPreviousMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTime();
    }

    public static Date getNextMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    public static Date getPreviousWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        return calendar.getTime();
    }

    public static Date getNextWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        return calendar.getTime();
    }

    public static String formatYearMonth(int year, int month) {
        return year + "\u5e74" + (month + 1) + "\u6708";
    }

    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH);
    }

    public static Date createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTime();
    }

    public static List<Date> getWeekDates(Date date) {
        List<Date> weekDates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getWeekStart(date));
        for (int i = 0; i < 7; i++) {
            weekDates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return weekDates;
    }
}