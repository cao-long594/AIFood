package com.example.food.ui.meal;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;

import java.util.Calendar;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private static final int MAX_PAGE_COUNT = 10000;
    private static final int INITIAL_POSITION = MAX_PAGE_COUNT / 2;
    private static final String[] WEEK_LABELS = {"一", "二", "三", "四", "五", "六", "日"};

    private final Context context;
    private final Calendar baseCalendar;
    private Calendar selectedDate;
    private OnDateSelectedListener onDateSelectedListener;
    private int currentPosition;

    public CalendarAdapter(Context context, Calendar initialCalendar) {
        this.context = context;
        this.baseCalendar = (Calendar) initialCalendar.clone();
        this.selectedDate = (Calendar) initialCalendar.clone();
        this.currentPosition = INITIAL_POSITION;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.meal_item_calendar, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        Calendar weekMonday = getWeekMonday(position);
        holder.gvMonthCalendar.setAdapter(new WeekCalendarAdapter(weekMonday));
    }

    @Override
    public int getItemCount() {
        return MAX_PAGE_COUNT;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.onDateSelectedListener = listener;
        if (selectedDate != null && onDateSelectedListener != null) {
            onDateSelectedListener.onDateSelected((Calendar) selectedDate.clone());
        }
    }

    // ── Task 1: drive the calendar to a specific date from outside ─────────────

    /**
     * Returns the ViewPager2 page position that contains the week of {@code date}.
     * Pass the result to {@code vpCalendar.setCurrentItem(position, false)}.
     */
    public int getPositionForDate(Calendar date) {
        // Monday of the target date's week
        Calendar targetMonday = (Calendar) date.clone();
        int dow = targetMonday.get(Calendar.DAY_OF_WEEK);
        int off = (dow == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dow);
        targetMonday.add(Calendar.DAY_OF_MONTH, off);
        targetMonday = startOfDay(targetMonday);

        // Monday of the base calendar's week
        Calendar baseMonday = (Calendar) baseCalendar.clone();
        int bdow = baseMonday.get(Calendar.DAY_OF_WEEK);
        int boff = (bdow == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - bdow);
        baseMonday.add(Calendar.DAY_OF_MONTH, boff);
        baseMonday = startOfDay(baseMonday);

        long diffMs = targetMonday.getTimeInMillis() - baseMonday.getTimeInMillis();
        int weekDiff = (int) Math.round(diffMs / (7.0 * 24 * 60 * 60 * 1000));
        return INITIAL_POSITION + weekDiff;
    }

    /**
     * Programmatically select a date (e.g. when another fragment broadcasts a
     * date change). Refreshes the visible week so the new selection is highlighted.
     */
    public void setSelectedDateExternal(Calendar date) {
        this.selectedDate = (Calendar) date.clone();
        notifyDataSetChanged();
    }

    // ────────────────────────────────────────────────────────────────────────────

    private Calendar getWeekMonday(int position) {
        Calendar calendar = (Calendar) baseCalendar.clone();
        int weekOffset = position - INITIAL_POSITION;
        calendar.add(Calendar.WEEK_OF_YEAR, weekOffset);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int offsetToMonday = dayOfWeek == Calendar.SUNDAY ? -6 : (Calendar.MONDAY - dayOfWeek);
        calendar.add(Calendar.DAY_OF_MONTH, offsetToMonday);
        return startOfDay(calendar);
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        GridView gvMonthCalendar;

        CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            gvMonthCalendar = itemView.findViewById(R.id.gv_month_calendar);
        }
    }

    private class WeekCalendarAdapter extends BaseAdapter {

        private final Calendar monday;

        WeekCalendarAdapter(Calendar monday) {
            this.monday = (Calendar) monday.clone();
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public Object getItem(int position) {
            Calendar date = (Calendar) monday.clone();
            date.add(Calendar.DAY_OF_MONTH, position);
            return startOfDay(date);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DayCellHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.meal_calendar_day_item, parent, false);
                holder = new DayCellHolder(convertView);
                convertView.setTag(holder);
                convertView.setLayoutParams(new GridView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
            } else {
                holder = (DayCellHolder) convertView.getTag();
            }

            Calendar date = (Calendar) getItem(position);
            holder.weekdayView.setText(WEEK_LABELS[position]);
            holder.dayView.setText(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));

            boolean isSelected = isSameDay(date, selectedDate);
            boolean isToday = isSameDay(date, Calendar.getInstance());
            boolean isPastDay = isPastOfSelectedInSameWeek(date, selectedDate, monday);

            if (isSelected) {
                holder.dayView.setBackgroundResource(R.drawable.meal_calendar_circle_selected);
                holder.dayView.setTextColor(Color.WHITE);
                holder.weekdayView.setTextColor(ContextCompat.getColor(context, R.color.home_text_secondary));
            } else if (isToday) {
                holder.dayView.setBackgroundResource(R.drawable.meal_calendar_circle_today);
                holder.dayView.setTextColor(ContextCompat.getColor(context, R.color.home_text_primary));
                holder.weekdayView.setTextColor(ContextCompat.getColor(context, R.color.home_text_secondary));
            } else {
                holder.dayView.setBackgroundResource(R.drawable.meal_calendar_circle_unselected);
                holder.dayView.setTextColor(ContextCompat.getColor(context, R.color.home_text_secondary));
                holder.weekdayView.setTextColor(ContextCompat.getColor(context, R.color.home_text_tertiary));
            }

            holder.pastDotView.setVisibility(isPastDay ? View.VISIBLE : View.INVISIBLE);

            convertView.setOnClickListener(v -> {
                selectedDate = (Calendar) date.clone();
                if (onDateSelectedListener != null) {
                    onDateSelectedListener.onDateSelected((Calendar) selectedDate.clone());
                }
                notifyDataSetChanged();
            });

            return convertView;
        }

        private boolean isPastOfSelectedInSameWeek(Calendar candidate, Calendar selected, Calendar weekMonday) {
            if (candidate == null || selected == null || weekMonday == null) {
                return false;
            }
            Calendar selectedWeekMonday = (Calendar) selected.clone();
            int selectedDayOfWeek = selectedWeekMonday.get(Calendar.DAY_OF_WEEK);
            int selectedOffset = selectedDayOfWeek == Calendar.SUNDAY ? -6 : (Calendar.MONDAY - selectedDayOfWeek);
            selectedWeekMonday.add(Calendar.DAY_OF_MONTH, selectedOffset);
            selectedWeekMonday = startOfDay(selectedWeekMonday);

            if (!isSameDay(selectedWeekMonday, weekMonday)) {
                return false;
            }

            Calendar selectedStart = startOfDay((Calendar) selected.clone());
            Calendar candidateStart = startOfDay((Calendar) candidate.clone());
            return candidateStart.before(selectedStart);
        }

        private boolean isSameDay(Calendar left, Calendar right) {
            if (left == null || right == null) {
                return false;
            }
            return left.get(Calendar.YEAR) == right.get(Calendar.YEAR)
                    && left.get(Calendar.DAY_OF_YEAR) == right.get(Calendar.DAY_OF_YEAR);
        }
    }

    private Calendar startOfDay(Calendar value) {
        Calendar normalized = (Calendar) value.clone();
        normalized.set(Calendar.HOUR_OF_DAY, 0);
        normalized.set(Calendar.MINUTE, 0);
        normalized.set(Calendar.SECOND, 0);
        normalized.set(Calendar.MILLISECOND, 0);
        return normalized;
    }

    private static class DayCellHolder {
        final TextView weekdayView;
        final TextView dayView;
        final View pastDotView;

        DayCellHolder(View itemView) {
            weekdayView = itemView.findViewById(R.id.tv_weekday);
            dayView = itemView.findViewById(R.id.tv_day);
            pastDotView = itemView.findViewById(R.id.view_past_dot);
        }
    }

    public interface OnDateSelectedListener {
        void onDateSelected(Calendar selectedCalendar);
    }
}