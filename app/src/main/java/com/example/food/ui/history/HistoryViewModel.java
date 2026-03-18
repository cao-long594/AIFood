package com.example.food.ui.history;

import com.example.food.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryViewModel {

    public enum HistoryViewType {
        DAY,
        WEEK,
        MONTH
    }

    private static final String FORMAT_FULL_DAY = "yyyy年M月d日";
    private static final String FORMAT_MONTH_DAY = "M月d日";
    private static final String FORMAT_FULL_MONTH = "yyyy年M月";

    public Date resolveAnchorDate(long anchorDateMillis) {
        return DateUtils.getDateStart(new Date(anchorDateMillis));
    }

    public HistoryViewType resolveViewType(String value) {
        if (value == null || value.isEmpty()) {
            return HistoryViewType.DAY;
        }
        try {
            return HistoryViewType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return HistoryViewType.DAY;
        }
    }

    public String buildSelectionSummary(Date anchorDate, HistoryViewType viewType) {
        if (viewType == HistoryViewType.DAY) {
            return format(anchorDate, FORMAT_FULL_DAY);
        }
        if (viewType == HistoryViewType.WEEK) {
            Date weekStart = DateUtils.getWeekStart(anchorDate);
            Date weekEnd = DateUtils.getDateStart(DateUtils.getWeekEnd(anchorDate));
            String end = isSameYear(weekStart, weekEnd)
                    ? format(weekEnd, FORMAT_MONTH_DAY)
                    : format(weekEnd, FORMAT_FULL_DAY);
            return format(weekStart, FORMAT_FULL_DAY) + " 至 " + end;
        }
        return format(DateUtils.getMonthStart(anchorDate), FORMAT_FULL_MONTH);
    }

    public MonthSectionsResult buildMonthSections(Date selectedDate,
                                                  Date today,
                                                  HistoryViewType viewType,
                                                  int monthsBefore,
                                                  int monthsAfter) {
        Date selectedMonthStart = DateUtils.getMonthStart(selectedDate);
        List<MonthSection> sections = new ArrayList<>();
        int selectedIndex = 0;

        for (int offset = -monthsBefore; offset <= monthsAfter; offset++) {
            Date monthStart = shiftMonth(selectedMonthStart, offset);
            if (offset == 0) {
                selectedIndex = sections.size();
            }
            sections.add(buildMonthSection(monthStart, selectedDate, today, viewType));
        }
        return new MonthSectionsResult(sections, selectedIndex);
    }

    public YearSectionsResult buildYearSections(Date selectedDate,
                                                Date today,
                                                int yearsBefore,
                                                int yearsAfter) {
        int selectedYear = DateUtils.getYear(selectedDate);
        List<YearSection> sections = new ArrayList<>();
        int selectedIndex = 0;

        for (int year = selectedYear - yearsBefore; year <= selectedYear + yearsAfter; year++) {
            if (year == selectedYear) {
                selectedIndex = sections.size();
            }
            sections.add(buildYearSection(year, selectedDate, today));
        }
        return new YearSectionsResult(sections, selectedIndex);
    }

    private MonthSection buildMonthSection(Date monthStart,
                                           Date selectedDate,
                                           Date today,
                                           HistoryViewType viewType) {
        Calendar[] calendars = DateUtils.getMonthDates(DateUtils.getYear(monthStart), DateUtils.getMonth(monthStart));
        List<CalendarCell> cells = new ArrayList<>(calendars.length);
        boolean weekMode = viewType == HistoryViewType.WEEK;

        Date todayStart = DateUtils.getDateStart(today);

        for (Calendar calendar : calendars) {
            Date cellDate = DateUtils.getDateStart(calendar.getTime());
            boolean isToday = DateUtils.isSameDay(cellDate, todayStart);
            boolean isSelected = DateUtils.isSameDay(cellDate, selectedDate) && !isToday;
            boolean isSelectedWeek = weekMode && DateUtils.isSameWeek(cellDate, selectedDate);
            boolean isCurrentWeek = weekMode && DateUtils.isSameWeek(cellDate, todayStart);
            int compare = cellDate.compareTo(todayStart);

            cells.add(new CalendarCell(
                    cellDate,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    DateUtils.isSameMonth(cellDate, monthStart),
                    isToday,
                    isSelected,
                    isCurrentWeek,
                    isSelectedWeek,
                    compare < 0,
                    compare > 0
            ));
        }

        return new MonthSection(monthStart, format(monthStart, FORMAT_FULL_MONTH), cells);
    }

    private YearSection buildYearSection(int year, Date selectedDate, Date today) {
        List<MonthCell> months = new ArrayList<>(12);
        Date currentMonthStart = DateUtils.getMonthStart(today);
        Date selectedMonthStart = DateUtils.getMonthStart(selectedDate);

        for (int month = 0; month < 12; month++) {
            Date monthStart = DateUtils.createDate(year, month, 1);
            boolean isCurrentMonth = DateUtils.isSameMonth(monthStart, currentMonthStart);
            boolean isSelectedMonth = DateUtils.isSameMonth(monthStart, selectedMonthStart) && !isCurrentMonth;
            int compare = monthStart.compareTo(currentMonthStart);

            months.add(new MonthCell(
                    month,
                    monthStart,
                    (month + 1) + "月",
                    isSelectedMonth,
                    isCurrentMonth,
                    compare < 0,
                    compare > 0
            ));
        }
        return new YearSection(year, months);
    }

    private Date shiftMonth(Date origin, int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(origin);
        calendar.add(Calendar.MONTH, offset);
        return DateUtils.getMonthStart(calendar.getTime());
    }

    private String format(Date date, String pattern) {
        return new SimpleDateFormat(pattern, Locale.CHINA).format(date);
    }

    private boolean isSameYear(Date first, Date second) {
        return DateUtils.getYear(first) == DateUtils.getYear(second);
    }

    public static class MonthSectionsResult {
        public final List<MonthSection> sections;
        public final int selectedSectionIndex;

        public MonthSectionsResult(List<MonthSection> sections, int selectedSectionIndex) {
            this.sections = sections;
            this.selectedSectionIndex = selectedSectionIndex;
        }
    }

    public static class YearSectionsResult {
        public final List<YearSection> sections;
        public final int selectedSectionIndex;

        public YearSectionsResult(List<YearSection> sections, int selectedSectionIndex) {
            this.sections = sections;
            this.selectedSectionIndex = selectedSectionIndex;
        }
    }

    public static class MonthSection {
        public final Date monthStart;
        public final String title;
        public final List<CalendarCell> cells;

        public MonthSection(Date monthStart, String title, List<CalendarCell> cells) {
            this.monthStart = monthStart;
            this.title = title;
            this.cells = cells;
        }
    }

    public static class CalendarCell {
        public final Date date;
        public final int dayOfMonth;
        public final boolean inCurrentMonth;
        public final boolean today;
        public final boolean selectedDate;
        public final boolean inCurrentWeek;
        public final boolean inSelectedWeek;
        public final boolean past;
        public final boolean future;

        public CalendarCell(Date date,
                            int dayOfMonth,
                            boolean inCurrentMonth,
                            boolean today,
                            boolean selectedDate,
                            boolean inCurrentWeek,
                            boolean inSelectedWeek,
                            boolean past,
                            boolean future) {
            this.date = date;
            this.dayOfMonth = dayOfMonth;
            this.inCurrentMonth = inCurrentMonth;
            this.today = today;
            this.selectedDate = selectedDate;
            this.inCurrentWeek = inCurrentWeek;
            this.inSelectedWeek = inSelectedWeek;
            this.past = past;
            this.future = future;
        }
    }

    public static class YearSection {
        public final int year;
        public final List<MonthCell> months;

        public YearSection(int year, List<MonthCell> months) {
            this.year = year;
            this.months = months;
        }
    }

    public static class MonthCell {
        public final int month;
        public final Date monthStart;
        public final String label;
        public final boolean selected;
        public final boolean currentMonth;
        public final boolean past;
        public final boolean future;

        public MonthCell(int month,
                         Date monthStart,
                         String label,
                         boolean selected,
                         boolean currentMonth,
                         boolean past,
                         boolean future) {
            this.month = month;
            this.monthStart = monthStart;
            this.label = label;
            this.selected = selected;
            this.currentMonth = currentMonth;
            this.past = past;
            this.future = future;
        }
    }
}
