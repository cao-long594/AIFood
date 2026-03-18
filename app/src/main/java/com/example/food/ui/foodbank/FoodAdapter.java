package com.example.food.ui.foodbank;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.db.entity.Food;
import com.example.food.utils.Constants;
import com.example.food.utils.FoodCategoryHelper;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FOOD = 1;

    private final Context context;
    private final List<ListItem> displayItems = new ArrayList<>();
    private final List<Food> cachedAllFoods = new ArrayList<>();
    private final Map<String, Boolean> groupExpandedState = new HashMap<>();
    private final Collator chineseCollator = Collator.getInstance(Locale.CHINA);

    private OnFoodClickListener foodClickListener;
    private OnDeleteClickListener deleteClickListener;
    private OnHeaderClickListener headerClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private boolean currentSearchMode = false;

    public interface OnFoodClickListener {
        void onFoodClick(Food food);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Food food, int position);
    }

    public interface OnHeaderClickListener {
        void onHeaderClick(String category);
    }

    public FoodAdapter(Context context) {
        this.context = context;
        initGroupState();
    }

    public void setOnFoodClickListener(OnFoodClickListener listener) {
        this.foodClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        this.headerClickListener = listener;
    }

    public void setGroups(List<FoodGroup> groups) {
        List<ListItem> newItems = new ArrayList<>();
        if (groups != null) {
            for (FoodGroup group : groups) {
                appendGroup(newItems, group);
            }
        }
        submitDisplayItems(newItems);
        clearSelectionSilently();
    }

    public void setFlatFoods(List<Food> foods) {
        List<ListItem> newItems = new ArrayList<>();
        if (foods != null) {
            for (Food food : foods) {
                newItems.add(ListItem.food(food));
            }
        }
        submitDisplayItems(newItems);
        clearSelectionSilently();
    }

    public void setData(List<Food> allFoods, boolean searchMode) {
        currentSearchMode = searchMode;
        if (searchMode) {
            setFlatFoods(sortFoodsByName(allFoods));
            return;
        }

        cachedAllFoods.clear();
        if (allFoods != null) {
            cachedAllFoods.addAll(allFoods);
        }
        setGroups(buildGroups(cachedAllFoods));
    }

    public void toggleGroup(String category) {
        if (currentSearchMode || category == null || category.trim().isEmpty()) {
            return;
        }
        boolean expanded = groupExpandedState.getOrDefault(category, true);
        groupExpandedState.put(category, !expanded);
        setGroups(buildGroups(cachedAllFoods));
    }

    private void initGroupState() {
        for (String category : FoodCategoryHelper.getCategoryOrder()) {
            if (!groupExpandedState.containsKey(category)) {
                groupExpandedState.put(category, true);
            }
        }
    }

    private void appendGroup(List<ListItem> target, FoodGroup group) {
        HeaderData headerData = new HeaderData(
                group.getCategory(),
                group.getTitle(),
                group.getCount(),
                group.isExpanded(),
                true
        );
        target.add(ListItem.header(headerData));
        if (group.isExpanded()) {
            for (Food food : group.getFoods()) {
                target.add(ListItem.food(food));
            }
        }
    }

    private void submitDisplayItems(List<ListItem> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(displayItems, newItems));
        displayItems.clear();
        displayItems.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    private List<FoodGroup> buildGroups(List<Food> foods) {
        Map<String, List<Food>> grouped = new HashMap<>();
        for (String category : FoodCategoryHelper.getCategoryOrder()) {
            grouped.put(category, new ArrayList<>());
        }

        if (foods != null) {
            for (Food food : foods) {
                String category = FoodCategoryHelper.resolveCategory(food);
                if (!grouped.containsKey(category)) {
                    category = FoodCategoryHelper.CATEGORY_CARB;
                }
                grouped.get(category).add(food);
            }
        }

        List<FoodGroup> groups = new ArrayList<>();
        for (String category : FoodCategoryHelper.getCategoryOrder()) {
            boolean expanded = groupExpandedState.getOrDefault(category, true);
            groups.add(new FoodGroup(
                    category,
                    FoodCategoryHelper.getDisplayName(category),
                    sortFoodsByName(grouped.get(category)),
                    expanded
            ));
        }
        return groups;
    }

    private List<Food> sortFoodsByName(List<Food> source) {
        List<Food> sorted = new ArrayList<>();
        if (source != null) {
            sorted.addAll(source);
        }
        sorted.sort((left, right) -> {
            if (left == right) {
                return 0;
            }
            if (left == null) {
                return 1;
            }
            if (right == null) {
                return -1;
            }

            String leftName = left.getName();
            String rightName = right.getName();
            boolean leftEmpty = leftName == null || leftName.trim().isEmpty();
            boolean rightEmpty = rightName == null || rightName.trim().isEmpty();

            if (leftEmpty && rightEmpty) {
                return Integer.compare(left.getId(), right.getId());
            }
            if (leftEmpty) {
                return 1;
            }
            if (rightEmpty) {
                return -1;
            }

            int compareResult = chineseCollator.compare(leftName.trim(), rightName.trim());
            if (compareResult != 0) {
                return compareResult;
            }
            return Integer.compare(left.getId(), right.getId());
        });
        return sorted;
    }

    public void clearSelection() {
        if (selectedPosition != RecyclerView.NO_POSITION) {
            int previous = selectedPosition;
            selectedPosition = RecyclerView.NO_POSITION;
            notifyItemChanged(previous);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    private void clearSelectionSilently() {
        selectedPosition = RecyclerView.NO_POSITION;
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_food_category_header, parent, false);
            return new HeaderViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = displayItems.get(position);
        if (item.type == TYPE_HEADER) {
            bindHeader((HeaderViewHolder) holder, item.headerData);
            return;
        }

        bindFood((FoodViewHolder) holder, item.food, position);
    }

    private void bindHeader(HeaderViewHolder holder, HeaderData headerData) {
        if (headerData == null) {
            holder.titleTextView.setText("");
            holder.arrowImageView.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.titleTextView.setText(String.format(Locale.CHINA, "%s(%d)", headerData.title, headerData.count));
        int backgroundColor = headerData.expanded
                ? ContextCompat.getColor(context, R.color.home_surface_alt)
                : ContextCompat.getColor(context, R.color.home_surface);
        holder.cardView.setCardBackgroundColor(backgroundColor);

        if (headerData.collapsible) {
            holder.arrowImageView.setVisibility(View.VISIBLE);
            holder.arrowImageView.setImageResource(
                    headerData.expanded ? R.drawable.meal_ic_chevron_up : R.drawable.meal_ic_chevron_down
            );
            holder.itemView.setOnClickListener(v -> {
                if (headerClickListener != null) {
                    headerClickListener.onHeaderClick(headerData.category);
                }
            });
        } else {
            holder.arrowImageView.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
        }
    }

    private void bindFood(FoodViewHolder holder, Food food, int position) {
        boolean isSelected = position == selectedPosition;

        holder.nameTextView.setText(food.getName() != null ? food.getName() : "未知食物");
        holder.caloriesTextView.setText(String.format(Locale.CHINA, "%.0f", food.getCalories()));
        holder.calorieUnitTextView.setText(buildCalorieUnitText(food));
        holder.nutritionTextView.setText(String.format(
                Locale.CHINA,
                "碳水 %.1fg  蛋白 %.1fg  脂肪 %.1fg",
                food.getCarbohydrate(),
                food.getProtein(),
                food.getFat()
        ));
        holder.fatRatioTextView.setText(buildFatRatioText(food));

        int cardColor = isSelected
                ? ContextCompat.getColor(context, R.color.home_surface_alt)
                : ContextCompat.getColor(context, R.color.home_surface);
        holder.cardView.setCardBackgroundColor(cardColor);
        holder.deleteButton.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.deleteButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.primary_color)
        ));

        holder.itemView.setOnClickListener(v -> {
            if (isSelected) {
                clearSelection();
                return;
            }
            if (foodClickListener != null) {
                foodClickListener.onFoodClick(food);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (deleteClickListener == null) {
                return false;
            }
            int previous = selectedPosition;
            selectedPosition = position;
            if (previous != RecyclerView.NO_POSITION && previous != position) {
                notifyItemChanged(previous);
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
        return displayItems.size();
    }

    private String buildFatRatioText(Food food) {
        double saturated = safeNonNegative(food.getSaturatedFat());
        double mono = safeNonNegative(food.getMonounsaturatedFat());
        double poly = safeNonNegative(food.getPolyunsaturatedFat());

        if (saturated == 0 && mono == 0 && poly == 0) {
            return "脂肪构成 0:0:0";
        }

        if (saturated > 0) {
            return "脂肪构成 1:" + formatRatioValue(mono / saturated) + ":" + formatRatioValue(poly / saturated);
        }

        if (mono >= poly) {
            double ratioPoly = mono == 0 ? 0 : poly / mono;
            return "脂肪构成 0:1:" + formatRatioValue(ratioPoly);
        }

        double ratioMono = poly == 0 ? 0 : mono / poly;
        return "脂肪构成 0:" + formatRatioValue(ratioMono) + ":1";
    }

    private String formatRatioValue(double value) {
        double rounded = Math.round(value * 10.0d) / 10.0d;
        if (Math.abs(rounded - Math.rint(rounded)) < 1e-6) {
            return String.format(Locale.CHINA, "%.0f", rounded);
        }
        return String.format(Locale.CHINA, "%.1f", rounded);
    }

    private double safeNonNegative(double value) {
        return Math.max(0d, value);
    }

    private String buildCalorieUnitText(Food food) {
        int unitAmount = food.getUnitAmount() > 0 ? food.getUnitAmount() : 100;
        String unitLabel = food.getUnit() == Constants.UNIT_MILLILITER ? "ml" : "g";
        return String.format(Locale.CHINA, "kcal/%d%s", unitAmount, unitLabel);
    }

    private static class HeaderData {
        final String category;
        final String title;
        final int count;
        final boolean expanded;
        final boolean collapsible;

        HeaderData(String category, String title, int count, boolean expanded, boolean collapsible) {
            this.category = category;
            this.title = title == null ? "" : title;
            this.count = Math.max(0, count);
            this.expanded = expanded;
            this.collapsible = collapsible;
        }
    }

    public static class FoodSection {
        public final String title;
        public final List<Food> foods;

        public FoodSection(String title, List<Food> foods) {
            this.title = title == null ? "" : title;
            this.foods = foods == null ? Collections.emptyList() : new ArrayList<>(foods);
        }
    }

    private static class ListItem {
        final int type;
        final HeaderData headerData;
        final Food food;

        private ListItem(int type, HeaderData headerData, Food food) {
            this.type = type;
            this.headerData = headerData;
            this.food = food;
        }

        static ListItem header(HeaderData headerData) {
            return new ListItem(TYPE_HEADER, headerData, null);
        }

        static ListItem food(Food food) {
            return new ListItem(TYPE_FOOD, null, food);
        }
    }

    private static class DiffCallback extends DiffUtil.Callback {
        private final List<ListItem> oldItems;
        private final List<ListItem> newItems;

        DiffCallback(List<ListItem> oldItems, List<ListItem> newItems) {
            this.oldItems = oldItems;
            this.newItems = newItems;
        }

        @Override
        public int getOldListSize() {
            return oldItems.size();
        }

        @Override
        public int getNewListSize() {
            return newItems.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            ListItem oldItem = oldItems.get(oldItemPosition);
            ListItem newItem = newItems.get(newItemPosition);
            if (oldItem.type != newItem.type) {
                return false;
            }
            if (oldItem.type == TYPE_HEADER) {
                return oldItem.headerData != null && newItem.headerData != null
                        && oldItem.headerData.category.equals(newItem.headerData.category);
            }
            return oldItem.food != null && newItem.food != null && oldItem.food.getId() == newItem.food.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ListItem oldItem = oldItems.get(oldItemPosition);
            ListItem newItem = newItems.get(newItemPosition);
            if (oldItem.type != newItem.type) {
                return false;
            }
            if (oldItem.type == TYPE_HEADER) {
                return oldItem.headerData != null && newItem.headerData != null
                        && oldItem.headerData.count == newItem.headerData.count
                        && oldItem.headerData.expanded == newItem.headerData.expanded
                        && oldItem.headerData.title.equals(newItem.headerData.title);
            }
            if (oldItem.food == null || newItem.food == null) {
                return false;
            }
            return oldItem.food.getId() == newItem.food.getId()
                    && Double.compare(oldItem.food.getCalories(), newItem.food.getCalories()) == 0
                    && Double.compare(oldItem.food.getCarbohydrate(), newItem.food.getCarbohydrate()) == 0
                    && Double.compare(oldItem.food.getProtein(), newItem.food.getProtein()) == 0
                    && Double.compare(oldItem.food.getFat(), newItem.food.getFat()) == 0;
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView titleTextView;
        final ImageView arrowImageView;
        final CardView cardView;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_category_title);
            arrowImageView = itemView.findViewById(R.id.iv_category_arrow);
            cardView = itemView.findViewById(R.id.card_category_header);
        }
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;
        final TextView caloriesTextView;
        final TextView calorieUnitTextView;
        final TextView nutritionTextView;
        final TextView fatRatioTextView;
        final Button deleteButton;
        final CardView cardView;

        FoodViewHolder(@NonNull View itemView) {
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