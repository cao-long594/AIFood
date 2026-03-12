package com.example.food.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 日期处理工具类
 * 提供日期格式化、日期范围获取等功能
 */
public class DateUtils {
    // 日期格式
    public static final String DATE_FORMAT_YMD = "yyyy-MM-dd";
    public static final String DATE_FORMAT_YMD_HM = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_HM = "HH:mm";
    public static final String DATE_FORMAT_MD = "MM月dd日";
    public static final String DATE_FORMAT_CHINESE = "yyyy年MM月dd日";

    /**
     * 格式化日期
     * @param date 日期对象
     * @param format 格式字符串
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date, String format) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * 获取今天的开始时间（00:00:00）
     * @return 今天的开始时间
     */
    public static Date getTodayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取今天的结束时间（23:59:59）
     * @return 今天的结束时间
     */
    public static Date getTodayEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 获取指定日期的开始时间
     * @param date 指定日期
     * @return 开始时间
     */
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

    /**
     * 获取指定日期的结束时间
     * @param date 指定日期
     * @return 结束时间
     */
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

    /**
     * 获取指定日期是星期几
     * @param date 指定日期
     * @return 星期几（中文）
     */
    public static String getWeekDay(Date date) {
        if (date == null) {
            return "";
        }
        String[] weekdays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (day < 0) day = 0;
        return weekdays[day];
    }

    /**
     * 获取本周的开始日期（周一）
     * @return 本周开始日期
     */
    public static Date getWeekStart() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // 调整为周一为第一天
        if (dayOfWeek == Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -6);
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, 2 - dayOfWeek);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取本周的结束日期（周日）
     * @return 本周结束日期
     */
    public static Date getWeekEnd() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // 调整为周日为最后一天
        calendar.add(Calendar.DAY_OF_MONTH, 8 - dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 获取本月的开始日期
     * @return 本月开始日期
     */
    public static Date getMonthStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取本月的结束日期
     * @return 本月结束日期
     */
    public static Date getMonthEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 判断两个日期是否是同一天
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否是同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 获取指定年月的所有日期列表（包括前后月补充的日期）
     * @param year 年份
     * @param month 月份（0-11）
     * @return 日期列表
     */
    public static Calendar[] getMonthDates(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        
        // 获取当月第一天是星期几（0-6，0表示周日）
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (firstDayOfWeek == 0) firstDayOfWeek = 7; // 调整为周一为1，周日为7
        
        // 获取当月天数
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // 计算需要显示的总天数（42 = 6行 x 7列）
        int totalDays = 42;
        Calendar[] dates = new Calendar[totalDays];
        
        // 填充上个月的日期
        calendar.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek + 1);
        for (int i = 0; i < totalDays; i++) {
            dates[i] = (Calendar) calendar.clone();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return dates;
    }
    
    /**
     * 获取指定年月的第一天是星期几
     * @param year 年份
     * @param month 月份（0-11）
     * @return 星期几（1-7，1表示周一，7表示周日）
     */
    public static int getFirstDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return dayOfWeek == 0 ? 7 : dayOfWeek;
    }
    
    /**
     * 获取指定年月的天数
     * @param year 年份
     * @param month 月份（0-11）
     * @return 天数
     */
    public static int getDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 获取上一个月的日期
     * @param date 当前日期
     * @return 上一个月的日期
     */
    public static Date getPreviousMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTime();
    }
    
    /**
     * 获取下一个月的日期
     * @param date 当前日期
     * @return 下一个月的日期
     */
    public static Date getNextMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }
    
    /**
     * 获取上一周的日期
     * @param date 当前日期
     * @return 上一周的日期
     */
    public static Date getPreviousWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        return calendar.getTime();
    }
    
    /**
     * 获取下一周的日期
     * @param date 当前日期
     * @return 下一周的日期
     */
    public static Date getNextWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        return calendar.getTime();
    }
    
    /**
     * 格式化年月显示
     * @param year 年份
     * @param month 月份（0-11）
     * @return 格式化后的年月字符串
     */
    public static String formatYearMonth(int year, int month) {
        return year + "年" + (month + 1) + "月";
    }
    
    /**
     * 切换月份
     * @param calendar 日历对象
     * @param amount 切换数量（正数为前进，负数为后退）
     */
    public static void changeMonth(Calendar calendar, int amount) {
        calendar.add(Calendar.MONTH, amount);
    }
    
    /**
     * 切换周
     * @param calendar 日历对象
     * @param amount 切换数量（正数为前进，负数为后退）
     */
    public static void changeWeek(Calendar calendar, int amount) {
        calendar.add(Calendar.WEEK_OF_YEAR, amount);
    }
    
    /**
     * 获取指定日期的年份
     * @param date 日期
     * @return 年份
     */
    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }
    
    /**
     * 获取指定日期的月份（0-11）
     * @param date 日期
     * @return 月份
     */
    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH);
    }
    
    /**
     * 获取指定日期的日
     * @param date 日期
     * @return 日
     */
    public static int getDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 创建指定年月日的日期对象
     * @param year 年份
     * @param month 月份（0-11）
     * @param day 日
     * @return 日期对象
     */
    public static Date createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    /**
     * 获取指定日期所在周的7天（周一到周日）
     * @param date 指定日期
     * @return 周日期列表（共7个元素）
     */
    public static List<Date> getWeekDates(Date date) {
        List<Date> weekDates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        // 设置一周的第一天为周一（关键）
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        // 跳到本周一
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        // 循环添加周一到周日的7天
        for (int i = 0; i < 7; i++) {
            weekDates.add(new Date(calendar.getTimeInMillis()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return weekDates;
    }
    
    /**
     * 计算指定日期是本周的第几天（1=周一，7=周日）
     * @param date 指定日期
     * @return 周内天数（1-7）
     */
    public static int getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SUNDAY ? 7 : dayOfWeek - 1;
    }
}