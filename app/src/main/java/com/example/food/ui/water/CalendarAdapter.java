package com.example.food.ui.water;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.food.R;
import com.example.food.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * 日历网格适配器
 * 用于在GridView中显示日历日期，并处理日期的选中状态和高亮显示
 */
public class CalendarAdapter extends BaseAdapter {
    private Context context;
    private List<Date> weekDates; // 改为：只存本周7天
    private Date selectedDate;
    private Date today;
    private Set<Integer> hasRecordsDays; // 有饮水记录的日期集合
    private OnDateSelectedListener onDateSelectedListener;
    
    /**
     * 日期选择监听器接口
     */
    public interface OnDateSelectedListener {
        void onDateSelected(Date date);
    }

    /**
     * 构造方法修改：不再需要Calendar，直接传入「选中日期」（用于确定显示哪一周）
     */
    public CalendarAdapter(Context context, Date selectedDate) {
        this.context = context;
        this.today = new Date();
        this.selectedDate = selectedDate != null ? selectedDate : today;
        this.hasRecordsDays = new HashSet<>();
        // 初始化：获取选中日期所在周的7天
        this.weekDates = DateUtils.getWeekDates(this.selectedDate);
    }

    /**
     * 设置日期选择监听器
     * @param listener 监听器对象
     */
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.onDateSelectedListener = listener;
    }
    
    /**
     * 关键：更新显示的周（上一周/下一周切换时调用）
     * @param targetDate 目标周内的任意日期（用于确定要显示哪一周）
     */
    public void updateWeek(Date targetDate) {
        this.selectedDate = targetDate;
        this.weekDates = DateUtils.getWeekDates(targetDate); // 重新获取目标周的7天
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        return weekDates != null ? weekDates.size() : 0; // 固定返回7
    }

    @Override
    public Object getItem(int position) {
        return weekDates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_calendar_date, parent, false);
            holder = new ViewHolder();
            holder.dateTextView = convertView.findViewById(R.id.tv_calendar_date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Date date = weekDates.get(position);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 设置日期文本（只显示日期号）
        holder.dateTextView.setText(String.valueOf(day));

        // 判断状态
        boolean isSelected = DateUtils.isSameDay(date, selectedDate);
        boolean isToday = DateUtils.isSameDay(date, today);

        // 样式逻辑不变（选中/今日/普通日期区分）
        if (isSelected) {
            holder.dateTextView.setBackgroundResource(R.drawable.water_3_calendar_selected);
            holder.dateTextView.setTextColor(context.getResources().getColor(android.R.color.white));
        } else if (isToday) {
            holder.dateTextView.setBackgroundResource(R.drawable.water_3_calendar_today);
            holder.dateTextView.setTextColor(context.getResources().getColor(android.R.color.black));
        } else {
            holder.dateTextView.setBackgroundResource(R.drawable.water_3_calendar_unselected);
            holder.dateTextView.setTextColor(context.getResources().getColor(android.R.color.black));
        }

        // 暂时不显示标记点，因为新的布局文件中没有dotView
        // 后续可以根据需要在布局中添加标记点视图
        
        // 设置点击事件
        holder.dateTextView.setOnClickListener(v -> {
            if (onDateSelectedListener != null) {
                onDateSelectedListener.onDateSelected(date);
            }
        });

        return convertView;
    }

    /**
     * 设置选中的日期
     * @param date 选中的日期
     */
    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        notifyDataSetChanged();
    }


    /**
     * 获取指定位置的日期
     * @param position 位置
     * @return 日期对象
     */
    public Date getDateAtPosition(int position) {
        if (weekDates != null && position >= 0 && position < weekDates.size()) {
            return weekDates.get(position);
        }
        return null;
    }

    /**
     * 内部ViewHolder类，用于缓存视图组件
     */
    private static class ViewHolder {
        TextView dateTextView;
    }
}