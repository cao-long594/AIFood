package com.example.food.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryRecordAdapter extends RecyclerView.Adapter<HistoryRecordAdapter.HistoryRecordViewHolder> {

    private final List<HistoryViewModel.HistoryRecordItem> items = new ArrayList<>();

    @NonNull
    @Override
    public HistoryRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_record, parent, false);
        return new HistoryRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryRecordViewHolder holder, int position) {
        HistoryViewModel.HistoryRecordItem item = items.get(position);
        holder.mealTypeTextView.setText(item.mealTypeLabel);
        holder.foodNameTextView.setText(item.foodName);
        holder.timeTextView.setText(item.timeLabel);
        holder.calorieTextView.setText(String.format(Locale.CHINA, "%.0f kcal", item.calories));
        holder.carbTextView.setText(String.format(Locale.CHINA, "碳水 %.0f g", item.carbohydrate));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<HistoryViewModel.HistoryRecordItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class HistoryRecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView mealTypeTextView;
        private final TextView foodNameTextView;
        private final TextView timeTextView;
        private final TextView calorieTextView;
        private final TextView carbTextView;

        HistoryRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            mealTypeTextView = itemView.findViewById(R.id.tv_history_meal_type);
            foodNameTextView = itemView.findViewById(R.id.tv_history_food_name);
            timeTextView = itemView.findViewById(R.id.tv_history_time);
            calorieTextView = itemView.findViewById(R.id.tv_history_calories);
            carbTextView = itemView.findViewById(R.id.tv_history_carbs);
        }
    }
}
