package com.example.food.ui.water;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.db.dao.WaterRecordDao;
import com.example.food.db.entity.WaterRecord;
import com.example.food.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 饮水记录适配器
 * 用于RecyclerView展示饮水记录列表
 */
public class WaterAdapter extends RecyclerView.Adapter<WaterAdapter.ViewHolder> {

    private Context context;
    private List<WaterRecord> waterRecords = new ArrayList<>();
    private WaterRecordDao waterRecordDao;
    private OnItemDeleteListener onItemDeleteListener;

    /**
     * 删除记录监听器接口
     */
    public interface OnItemDeleteListener {
        /**
         * 当记录被删除时调用
         */
        void onItemDeleted();
    }

    public WaterAdapter(Context context) {
        this.context = context;
    }

    /**
     * 设置删除监听器
     * @param listener 删除监听器
     */
    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.onItemDeleteListener = listener;
    }

    /**
     * 设置WaterRecordDao实例
     * @param dao WaterRecordDao实例
     */
    public void setWaterRecordDao(WaterRecordDao dao) {
        this.waterRecordDao = dao;
    }

    /**
     * 设置新的饮水记录数据
     * @param records 新的饮水记录列表
     */
    public void setData(List<WaterRecord> records) {
        // 创建新的列表副本，避免引用问题
        this.waterRecords.clear();
        if (records != null && !records.isEmpty()) {
            this.waterRecords.addAll(records);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_water, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaterRecord record = waterRecords.get(position);
        // 使用24小时制时间格式，与设计稿保持一致
        holder.timeTextView.setText(DateUtils.formatDate(record.getTime(), DateUtils.DATE_FORMAT_HM));
        // 设置水量文本，使用蓝色字体
        holder.amountTextView.setText(String.format("%.0fml", record.getAmount()));
        
        // 设置长按事件
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(record, position);
            return true;
        });
    }

    /**
     * 显示删除确认对话框
     */
    private void showDeleteDialog(WaterRecord record, int position) {
        new AlertDialog.Builder(context)
                .setTitle("删除记录")
                .setMessage("确定要删除这条饮水记录吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    deleteWaterRecord(record, position);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 删除饮水记录
     */
    private void deleteWaterRecord(WaterRecord record, int position) {
        // 确保waterRecordDao不为null
        if (waterRecordDao == null) {
            Toast.makeText(context, "数据库初始化失败，无法删除记录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new Thread(() -> {
            waterRecordDao.delete(record);
            // 在主线程更新UI
            ((android.app.Activity) context).runOnUiThread(() -> {
                waterRecords.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                
                // 通知Fragment更新进度条
                if (onItemDeleteListener != null) {
                    onItemDeleteListener.onItemDeleted();
                }
            });
        }).start();
    }

    @Override
    public int getItemCount() {
        return waterRecords.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView amountTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.tv_water_time);
            amountTextView = itemView.findViewById(R.id.tv_water_amount);
        }
    }
}