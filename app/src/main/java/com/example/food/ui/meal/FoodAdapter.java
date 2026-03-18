package com.example.food.ui.meal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.db.entity.MealRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SUMMARY = 0;
    private static final int TYPE_FOOD = 1;

    private final List<MealRecord> records = new ArrayList<>();
    private final OnFoodClickListener listener;

    private double totalCarbs;
    private double totalProtein;
    private double totalFat;
    private double mealCalories;
    private double caloriesPercentage;
    private String recommendedPercentage = "閸欏倽鈧喛绱?%";

    public interface OnFoodClickListener {
        void onFoodClick(MealRecord record);
        void onFoodLongClick(MealRecord record);
    }

    public FoodAdapter(List<MealRecord> initialRecords, OnFoodClickListener listener) {
        if (initialRecords != null) {
            this.records.addAll(initialRecords);
        }
        this.listener = listener;
        setHasStableIds(true);
    }

    public void updateSummary(double carbs,
                              double protein,
                              double fat,
                              double calories,
                              double percentage,
                              String recommended) {
        this.totalCarbs = carbs;
        this.totalProtein = protein;
        this.totalFat = fat;
        this.mealCalories = calories;
        this.caloriesPercentage = percentage;
        this.recommendedPercentage = recommended == null ? "閸欏倽鈧喛绱?%" : recommended;

        if (getItemCount() > 0) {
            notifyItemChanged(0);
        }
    }

    public void submitRecords(List<MealRecord> newRecords) {
        List<MealRecord> oldList = new ArrayList<>(records);
        List<MealRecord> newList = newRecords == null ? new ArrayList<>() : new ArrayList<>(newRecords);

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(oldList, newList), false);

        records.clear();
        records.addAll(newList);

            diffResult.dispatchUpdatesTo(new OffsetListUpdateCallback(this, 1));
            if (getItemCount() > 0) {
                notifyItemChanged(0);
            }
    }

    public MealRecord getItem(int adapterPosition) {
        if (adapterPosition == RecyclerView.NO_POSITION || adapterPosition == 0) {
            return null;
        }
        int dataIndex = adapterPosition - 1;
        if (dataIndex < 0 || dataIndex >= records.size()) {
            return null;
        }
        return records.get(dataIndex);
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return Long.MIN_VALUE;
        }
        MealRecord record = getItem(position);
        if (record == null) {
            return RecyclerView.NO_ID;
        }
        return record.getId();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_SUMMARY : TYPE_FOOD;
    }

    @Override
    public int getItemCount() {
        return records.size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SUMMARY) {
            View view = inflater.inflate(R.layout.item_meal_summary_header, parent, false);
            return new SummaryViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_meal_food, parent, false);
            return new FoodViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SummaryViewHolder) {
            bindSummary((SummaryViewHolder) holder);
            return;
        }

        FoodViewHolder foodHolder = (FoodViewHolder) holder;
        MealRecord record = getItem(position);
        if (record == null) {
            return;
        }

        foodHolder.foodName.setText(record.getFoodName());
        foodHolder.foodAmount.setText(String.format(Locale.CHINA, "%.0fg", record.getAmount()));
        foodHolder.foodCalories.setText(String.format(Locale.CHINA, "%.0f", record.getCalories()));

        foodHolder.itemView.setOnClickListener(v -> {
            if (listener == null) return;

            int adapterPosition = foodHolder.getBindingAdapterPosition();
            MealRecord clickedRecord = getItem(adapterPosition);
            if (clickedRecord != null) {
                listener.onFoodClick(clickedRecord);
            }
        });

        foodHolder.itemView.setOnLongClickListener(v -> {
            if (listener == null) return false;

            int adapterPosition = foodHolder.getBindingAdapterPosition();
            MealRecord clickedRecord = getItem(adapterPosition);
            if (clickedRecord != null) {
                listener.onFoodLongClick(clickedRecord);
                return true;
            }
            return false;
        });
    }

    private void bindSummary(@NonNull SummaryViewHolder holder) {
        holder.carbsValue.setText(String.format(Locale.CHINA, "%.0f", totalCarbs));
        holder.proteinValue.setText(String.format(Locale.CHINA, "%.0f", totalProtein));
        holder.fatValue.setText(String.format(Locale.CHINA, "%.0f", totalFat));
        holder.caloriesValue.setText(String.format(Locale.CHINA, "%.0f kcal", mealCalories));
        holder.caloriesPercentage.setText(String.format(Locale.CHINA, "%.0f%%", caloriesPercentage));
        holder.recommendedCaloriesPercentage.setText(recommendedPercentage);
        holder.foodCount.setText(String.format(Locale.CHINA, "%d \u9879", records.size()));
    }

    private static class OffsetListUpdateCallback implements ListUpdateCallback {
        private final RecyclerView.Adapter<?> adapter;
        private final int offset;

        OffsetListUpdateCallback(RecyclerView.Adapter<?> adapter, int offset) {
            this.adapter = adapter;
            this.offset = offset;
        }

        @Override
        public void onInserted(int position, int count) {
            adapter.notifyItemRangeInserted(position + offset, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            adapter.notifyItemRangeRemoved(position + offset, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            adapter.notifyItemMoved(fromPosition + offset, toPosition + offset);
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            adapter.notifyItemRangeChanged(position + offset, count, payload);
        }
    }

    private static class DiffCallback extends DiffUtil.Callback {
        private final List<MealRecord> oldList;
        private final List<MealRecord> newList;

        DiffCallback(List<MealRecord> oldList, List<MealRecord> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            MealRecord oldItem = oldList.get(oldItemPosition);
            MealRecord newItem = newList.get(newItemPosition);
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            MealRecord oldItem = oldList.get(oldItemPosition);
            MealRecord newItem = newList.get(newItemPosition);

            return oldItem.getId() == newItem.getId()
                    && Objects.equals(oldItem.getFoodName(), newItem.getFoodName())
                    && Double.compare(oldItem.getAmount(), newItem.getAmount()) == 0
                    && Double.compare(oldItem.getCalories(), newItem.getCalories()) == 0
                    && Double.compare(oldItem.getCarbohydrate(), newItem.getCarbohydrate()) == 0
                    && Double.compare(oldItem.getProtein(), newItem.getProtein()) == 0
                    && Double.compare(oldItem.getFat(), newItem.getFat()) == 0;
        }
    }

    static class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView carbsValue;
        TextView proteinValue;
        TextView fatValue;
        TextView caloriesValue;
        TextView caloriesPercentage;
        TextView recommendedCaloriesPercentage;
        TextView foodCount;

        SummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            carbsValue = itemView.findViewById(R.id.carbs_value);
            proteinValue = itemView.findViewById(R.id.protein_value);
            fatValue = itemView.findViewById(R.id.fat_value);
            caloriesValue = itemView.findViewById(R.id.calories_value);
            caloriesPercentage = itemView.findViewById(R.id.calories_percentage);
            recommendedCaloriesPercentage = itemView.findViewById(R.id.recommended_calories_percentage);
            foodCount = itemView.findViewById(R.id.tv_food_count);
        }
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView foodName;
        TextView foodAmount;
        TextView foodCalories;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.tv_food_name);
            foodAmount = itemView.findViewById(R.id.tv_food_amount);
            foodCalories = itemView.findViewById(R.id.tv_food_calories);
        }
    }
}
