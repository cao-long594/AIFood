package com.example.food.ui.meal;


import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private Context context;
    private Calendar baseCalendar;
    private OnDateSelectedListener onDateSelectedListener;
    private int currentPosition;
    private static final int MAX_PAGE_COUNT = 10000; // 减小页面总数
    private static final int INITIAL_POSITION = MAX_PAGE_COUNT / 2;

    // 构造函数
    public CalendarAdapter(Context context, Calendar initialCalendar) {
        this.context = context;
        this.baseCalendar = (Calendar) initialCalendar.clone();
        this.currentPosition = INITIAL_POSITION;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载日历页面布局
        View view = LayoutInflater.from(context).inflate(R.layout.meal_item_calendar, parent, false);
        return new CalendarViewHolder(view);
    }

    // 修改onBindViewHolder中的日期计算
    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        // 计算当前页面对应的日期（相对于基准日历的偏移）
        Calendar weekCalendar = getCalendarForPosition(position);
        // 设置为周一
        Calendar weekMonday = (Calendar) weekCalendar.clone();
        weekMonday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // 生成当前日期所在周的日历数据（周一至周日）
        List<Integer> dateList = generateWeekDateList(weekMonday);

        // 设置GridView适配器 - 修正参数
        MonthCalendarAdapter adapter = new MonthCalendarAdapter(
                context,
                dateList,
                weekMonday,
                onDateSelectedListener
        );
        holder.gvMonthCalendar.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return MAX_PAGE_COUNT; // 返回足够多的页面数
    }


    /**
     * 新增方法：根据position获取对应的周日历
     */
    private Calendar getCalendarForPosition(int position) {
        Calendar calendar = (Calendar) baseCalendar.clone();
        // 计算相对于初始位置的周偏移
        int weekOffset = position - INITIAL_POSITION;
        calendar.add(Calendar.WEEK_OF_YEAR, weekOffset);
        return calendar;
    }
    /**
     * 生成单个日期所在周的日历数据（周一至周日）
     */
    // 修改generateWeekDateList方法，确保正确生成周一至周日
    private List<Integer> generateWeekDateList(Calendar calendar) {
        List<Integer> dateList = new ArrayList<>();
        Calendar weekCalendar = (Calendar) calendar.clone();

        // 设置为周一
        weekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // 生成7天日期
        for (int i = 0; i < 7; i++) {
            dateList.add(weekCalendar.get(Calendar.DAY_OF_MONTH));
            weekCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dateList;
    }

    /**
     * 获取当前页面显示的日历对象（具体日期）
     */
// 修改后（正确）
// 修改getCurrentPageCalendar方法
    public Calendar getCurrentPageCalendar() {
        return getCalendarForPosition(currentPosition);
    }

    /**
     * 获取当前选中的位置
     */
    public int getCurrentPosition() {
        return currentPosition;
    }
    
    /**
     * 更新ViewPager2当前选中的位置
     */
    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    // 日历页面ViewHolder
    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        GridView gvMonthCalendar;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            gvMonthCalendar = itemView.findViewById(R.id.gv_month_calendar);
        }
    }

    /**
     * 单个日期所在周的GridView适配器
     */
    // 修改MonthCalendarAdapter构造函数
    private static class MonthCalendarAdapter extends BaseAdapter {
        private Context context;
        private List<Integer> dateList;
        private Calendar weekCalendar; // 当前周的周一
        private OnDateSelectedListener onDateSelectedListener;
        private int todayPosition = -1; // 今天在当前星期的位置
        private int selectedPosition = -1; // 当前选中的位置

        // 修正构造函数：移除weekOffset参数
        public MonthCalendarAdapter(Context context, List<Integer> dateList, Calendar weekCalendar, OnDateSelectedListener listener) {
            this.context = context;
            this.dateList = dateList;
            this.weekCalendar = weekCalendar;
            this.onDateSelectedListener = listener;

            Calendar today = Calendar.getInstance();
            Calendar monday = (Calendar) weekCalendar.clone();

            // 查找今天在当前位置的位置
            for (int i = 0; i < 7; i++) {
                if (isSameDay(monday, today)) {
                    todayPosition = i;
                    selectedPosition = i; // 强制选中今天
                    break;
                }
                monday.add(Calendar.DAY_OF_MONTH, 1);
            }

            // 如果找到今天，立即回调选中事件
            if (todayPosition != -1 && onDateSelectedListener != null) {
                Calendar selectedCalendar = (Calendar) weekCalendar.clone();
                selectedCalendar.add(Calendar.DAY_OF_MONTH, todayPosition);
                onDateSelectedListener.onDateSelected(selectedCalendar);
            }
        }
        // 辅助方法：判断是否为同一天
        private boolean isSameDay(Calendar cal1, Calendar cal2) {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
        }

        @Override
        public int getCount() {
            return dateList.size();
        }

        @Override
        public Object getItem(int position) {
            return dateList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tvDay;
            if (convertView == null) {
                tvDay = new TextView(context);
                // 计算列宽以匹配星期标题
                int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                int padding = dp2px(context, 10); // 两侧各10dp内边距
                int columnWidth = (screenWidth - 10 * padding) / 7;
                tvDay.setLayoutParams(new GridView.LayoutParams(columnWidth, columnWidth));
                tvDay.setGravity(Gravity.CENTER);
                tvDay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            } else {
                tvDay = (TextView) convertView;
            }

            Integer day = dateList.get(position);
            if (day == null || day <= 0) {
                tvDay.setText("");
                tvDay.setBackgroundResource(0);
            } else {
                tvDay.setText(String.valueOf(day));

                // 计算当前单元格对应的日期
                Calendar currentDate = (Calendar) weekCalendar.clone();
                currentDate.add(Calendar.DAY_OF_MONTH, position);

                // 关键修复：设置时间为中午12点，避免时区问题导致的日期偏移
                currentDate.set(Calendar.HOUR_OF_DAY, 12);
                currentDate.set(Calendar.MINUTE, 0);
                currentDate.set(Calendar.SECOND, 0);
                currentDate.set(Calendar.MILLISECOND, 0);

                // 判断是否为今天 - 同样修复今天的判断
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 12); // 同样设置今天为中午12点
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                boolean isToday = isSameDay(currentDate, today);
                boolean isSelected = (position == selectedPosition);

                // 设置不同状态的样式
                if (isSelected) {
                    tvDay.setBackgroundResource(R.drawable.meal_calendar_circle_selected);
                    tvDay.setTextColor(context.getResources().getColor(R.color.white));
                } else if (isToday) {
                    tvDay.setBackgroundResource(R.drawable.meal_calendar_circle_today);
                    tvDay.setTextColor(context.getResources().getColor(R.color.black));
                } else {
                    tvDay.setBackgroundResource(R.drawable.meal_calendar_circle_unselected);
                    tvDay.setTextColor(context.getResources().getColor(R.color.black));
                }

                // 日期点击事件
                tvDay.setOnClickListener(v -> {
                    selectedPosition = position;
                    notifyDataSetChanged();

                    // 回调选中的日期
                    if (onDateSelectedListener != null) {
                        Calendar selectedCalendar = (Calendar) weekCalendar.clone();
                        selectedCalendar.add(Calendar.DAY_OF_MONTH, position);
                        // 关键修复：设置时间为中午12点
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, 12);
                        selectedCalendar.set(Calendar.MINUTE, 0);
                        selectedCalendar.set(Calendar.SECOND, 0);
                        selectedCalendar.set(Calendar.MILLISECOND, 0);

                        // 添加调试日志
                        Log.d("CalendarDebug", "Clicked position: " + position);
                        Log.d("CalendarDebug", "Clicked day: " + day);
                        Log.d("CalendarDebug", "Calculated date: " + selectedCalendar.getTime());

                        onDateSelectedListener.onDateSelected(selectedCalendar);
                    }
                });
            }
            return tvDay;
        }

        // dp转px方法保持不变
        private static int dp2px(Context context, float dpValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }
    }

    // 日期选中回调接口
    public interface OnDateSelectedListener {
        void onDateSelected(Calendar selectedCalendar);
    }

    // 设置日期选中回调
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.onDateSelectedListener = listener;
    }
}