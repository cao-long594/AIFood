package com.example.food.ui.foodbank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.db.entity.Food;
import com.example.food.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    private final List<Food> foods = new ArrayList<>();
    private final Context context;
    private OnFoodClickListener listener;
    private OnDeleteClickListener deleteClickListener;
    private int selectedPosition = -1;
    private Food selectedFood = null;

    public interface OnFoodClickListener {
        void onFoodClick(Food food);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Food food, int position);
    }

    public FoodAdapter(Context context) {
        this.context = context;
    }

    public void setOnFoodClickListener(OnFoodClickListener listener) {
        this.listener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setData(List<Food> foods) {
        this.foods.clear();
        if (foods != null) {
            this.foods.addAll(foods);
        }
        resetSelection();
        notifyDataSetChanged();
    }

    public void clearSelection() {
        if (selectedPosition != -1) {
            int previous = selectedPosition;
            selectedPosition = -1;
            notifyItemChanged(previous);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    private void resetSelection() {
        selectedPosition = -1;
    }

    public void setSelectedFood(Food food) {
        this.selectedFood = food;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (position >= foods.size()) {
            return;
        }

        Food food = foods.get(position);
        boolean isSelected = position == selectedPosition;
        boolean isHighlighted = selectedFood != null && selectedFood.getId() == food.getId();
        boolean showDelete = isSelected && deleteClickListener != null;

        holder.nameTextView.setText(food.getName() != null ? food.getName() : "\u672a\u77e5\u98df\u7269");
        holder.caloriesTextView.setText(String.format(Locale.CHINA, "%.0f", food.getCalories()));
        holder.calorieUnitTextView.setText(buildCalorieUnitText(food));
        holder.nutritionTextView.setText(String.format(
                Locale.CHINA,
                "\u78b3\u6c34 %.1fg  \u86cb\u767d\u8d28 %.1fg  \u8102\u80aa %.1fg",
                food.getCarbohydrate(),
                food.getProtein(),
                food.getFat()
        ));
        holder.fatRatioTextView.setText(buildFatRatioText(food));

        int cardColor = isSelected || isHighlighted
                ? ContextCompat.getColor(context, R.color.home_surface_alt)
                : ContextCompat.getColor(context, R.color.home_surface);
        holder.cardView.setCardBackgroundColor(cardColor);
        holder.deleteButton.setVisibility(showDelete ? View.VISIBLE : View.GONE);
        holder.deleteButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_color)));

        holder.itemView.setOnClickListener(v -> {
            if (deleteClickListener == null) {
                if (listener != null) {
                    listener.onFoodClick(food);
                }
                return;
            }

            if (isSelected) {
                clearSelection();
            } else if (listener != null) {
                listener.onFoodClick(food);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (deleteClickListener == null) {
                return false;
            }
            int previousPos = selectedPosition;
            selectedPosition = position;
            if (previousPos != -1 && previousPos != position) {
                notifyItemChanged(previousPos);
            }
            notifyItemChanged(position);
            return true;
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(food, position);
            }
            clearSelection();
        });
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    private String buildFatRatioText(Food food) {
        double saturatedFat = food.getSaturatedFat();
        double monoFat = food.getMonounsaturatedFat();
        double polyFat = food.getPolyunsaturatedFat();
        double totalFat = saturatedFat + monoFat + polyFat;
        if (totalFat <= 0) {
            return "\u8102\u80aa\u6784\u6210 0:0:0";
        }

        int satPercent = (int) Math.round((saturatedFat / totalFat) * 100);
        int monoPercent = (int) Math.round((monoFat / totalFat) * 100);
        int polyPercent = Math.max(0, 100 - satPercent - monoPercent);
        return String.format(Locale.CHINA, "\u8102\u80aa\u6784\u6210 %d:%d:%d", satPercent, monoPercent, polyPercent);
    }

    private String buildCalorieUnitText(Food food) {
        int unitAmount = food.getUnitAmount() > 0 ? food.getUnitAmount() : 100;
        String unitLabel = food.getUnit() == Constants.UNIT_MILLILITER ? "ml" : "g";
        return String.format(Locale.CHINA, "kcal / %d%s", unitAmount, unitLabel);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView caloriesTextView;
        TextView calorieUnitTextView;
        TextView nutritionTextView;
        TextView fatRatioTextView;
        Button deleteButton;
        CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_food_name);
            caloriesTextView = itemView.findViewById(R.id.tv_calories);
            calorieUnitTextView = itemView.findViewById(R.id.tv_calorie_unit);
            nutritionTextView = itemView.findViewById(R.id.tv_nutrition);
            fatRatioTextView = itemView.findViewById(R.id.tv_fat_ratio);
            deleteButton = itemView.findViewById(R.id.btn_delete);
            cardView = itemView.findViewById(R.id.food_card);
        }
    }
}

