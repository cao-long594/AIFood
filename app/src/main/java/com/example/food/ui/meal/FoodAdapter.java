package com.example.food.ui.meal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.db.entity.MealRecord;

import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    private final List<MealRecord> records;
    private final OnFoodClickListener listener;

    public interface OnFoodClickListener {
        void onFoodClick(MealRecord record);
        void onFoodLongClick(MealRecord record);
    }

    public FoodAdapter(List<MealRecord> records, OnFoodClickListener listener) {
        this.records = records;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealRecord record = records.get(position);
        holder.foodName.setText(record.getFoodName());
        holder.foodAmount.setText(String.format(Locale.CHINA, "%.0fg", record.getAmount()));
        holder.foodCalories.setText(String.format(Locale.CHINA, "%.0f kcal", record.getCalories()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodClick(record);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onFoodLongClick(record);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodName;
        TextView foodAmount;
        TextView foodCalories;

        public ViewHolder(View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.tv_food_name);
            foodAmount = itemView.findViewById(R.id.tv_food_amount);
            foodCalories = itemView.findViewById(R.id.tv_food_calories);
        }
    }
}
