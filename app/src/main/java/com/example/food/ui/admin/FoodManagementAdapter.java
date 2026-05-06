package com.example.food.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.db.entity.Food;
import com.example.food.ui.foodbank.FoodGroup;
import com.example.food.utils.FoodCategoryHelper;
import com.google.android.material.button.MaterialButton;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 管理员食物列表适配器
 * 支持碳水/蛋白/脂肪/水果分类折叠展开
 */
public class FoodManagementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FOOD = 1;

    private List<Object> displayItems = new ArrayList<>();
    private List<Food> cachedAllFoods = new ArrayList<>();
    private final Map<String, Boolean> groupExpandedState = new HashMap<>();
    private final Collator chineseCollator = Collator.getInstance(Locale.CHINA);
    private boolean searchMode = false;

    private OnActionListener listener;
    private OnHeaderClickListener headerClickListener;

    public interface OnActionListener {
        void onPromote(Food food);
        void onDemote(Food food);
        void onEdit(Food food);
        void onDelete(Food food);
    }

    public interface OnHeaderClickListener {
        void onHeaderClick(String category);
    }

    public FoodManagementAdapter(List<Food> foodList, OnActionListener listener) {
        this.listener = listener;
        initGroupState();
    }

    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        this.headerClickListener = listener;
    }

    public void setData(List<Food> allFoods) {
        setData(allFoods, false);
    }

    public void setData(List<Food> allFoods, boolean isSearchMode) {
        this.searchMode = isSearchMode;
        if (isSearchMode) {
            setFlatList(allFoods);
            return;
        }
        cachedAllFoods.clear();
        if (allFoods != null) cachedAllFoods.addAll(allFoods);
        buildGroupedList();
    }

    public void toggleGroup(String category) {
        if (searchMode || category == null) return;
        boolean expanded = groupExpandedState.getOrDefault(category, true);
        groupExpandedState.put(category, !expanded);
        buildGroupedList();
    }

    private void initGroupState() {
        for (String cat : FoodCategoryHelper.getCategoryOrder()) {
            if (!groupExpandedState.containsKey(cat)) {
                groupExpandedState.put(cat, true);
            }
        }
    }

    private void buildGroupedList() {
        Map<String, List<Food>> grouped = new HashMap<>();
        for (String cat : FoodCategoryHelper.getCategoryOrder()) {
            grouped.put(cat, new ArrayList<>());
        }
        for (Food f : cachedAllFoods) {
            String cat = FoodCategoryHelper.resolveCategory(f);
            if (!grouped.containsKey(cat)) cat = FoodCategoryHelper.CATEGORY_CARB;
            grouped.get(cat).add(f);
        }

        List<Object> items = new ArrayList<>();
        for (String cat : FoodCategoryHelper.getCategoryOrder()) {
            List<Food> foods = grouped.get(cat);
            foods.sort((a, b) -> {
                if (a == b) return 0;
                if (a == null) return 1;
                if (b == null) return -1;
                return chineseCollator.compare(
                        a.getName() != null ? a.getName() : "",
                        b.getName() != null ? b.getName() : "");
            });
            boolean expanded = groupExpandedState.getOrDefault(cat, true);
            HeaderData header = new HeaderData(cat, FoodCategoryHelper.getDisplayName(cat),
                    foods.size(), expanded, true);
            items.add(header);
            if (expanded) {
                items.addAll(foods);
            }
        }
        displayItems = items;
        notifyDataSetChanged();
    }

    private void setFlatList(List<Food> foods) {
        List<Object> items = new ArrayList<>();
        if (foods != null) items.addAll(foods);
        displayItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_food_category_header, parent, false);
            return new HeaderViewHolder(v);
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_food, parent, false);
        return new FoodViewHolder(v);
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position) instanceof HeaderData ? TYPE_HEADER : TYPE_FOOD;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderData header = (HeaderData) displayItems.get(position);
            HeaderViewHolder hh = (HeaderViewHolder) holder;
            hh.tvTitle.setText(header.title + " (" + header.count + ")");
            hh.itemView.setOnClickListener(v -> {
                if (headerClickListener != null) {
                    headerClickListener.onHeaderClick(header.category);
                } else {
                    toggleGroup(header.category);
                }
            });
            return;
        }

        Food food = (Food) displayItems.get(position);
        FoodViewHolder fh = (FoodViewHolder) holder;

        fh.tvFoodName.setText(food.getName());

        String categoryName = FoodCategoryHelper.getDisplayName(food.getCategory());
        fh.tvCategory.setText(categoryName);

        String ownerText = food.getUserId() != null && food.getUserId() > 0
                ? String.format(Locale.CHINA, "用户 #%d", food.getUserId())
                : (food.getVisibilityStatus() == 1 ? "公开" : "私密");
        fh.tvOwner.setText(ownerText);

        String sourceText = "USER".equals(food.getSource()) && food.getSourceUserName() != null
                ? "用户 " + food.getSourceUserName() + " 分享" : "系统导入";
        fh.tvSource.setText(sourceText);
        fh.tvSource.setVisibility(View.VISIBLE);

        boolean isPublic = food.getVisibilityStatus() == 1;
        fh.btnPromote.setVisibility(isPublic ? View.GONE : View.VISIBLE);
        fh.btnDemote.setVisibility(isPublic ? View.VISIBLE : View.GONE);

        fh.btnPromote.setOnClickListener(v -> { if (listener != null) listener.onPromote(food); });
        fh.btnDemote.setOnClickListener(v -> { if (listener != null) listener.onDemote(food); });
        fh.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(food); });
        fh.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(food); });
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    // === ViewHolders ===

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        HeaderViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_category_title);
        }
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvCategory, tvOwner, tvSource;
        MaterialButton btnPromote, btnDemote, btnEdit, btnDelete;
        FoodViewHolder(View v) {
            super(v);
            tvFoodName = v.findViewById(R.id.tv_food_name);
            tvCategory = v.findViewById(R.id.tv_category);
            tvOwner = v.findViewById(R.id.tv_owner);
            tvSource = v.findViewById(R.id.tv_source);
            btnPromote = v.findViewById(R.id.btn_promote);
            btnDemote = v.findViewById(R.id.btn_demote);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }

    // === Data classes ===

    static class HeaderData {
        final String category;
        final String title;
        final int count;
        final boolean expanded;
        final boolean collapsible;
        HeaderData(String category, String title, int count, boolean expanded, boolean collapsible) {
            this.category = category;
            this.title = title;
            this.count = count;
            this.expanded = expanded;
            this.collapsible = collapsible;
        }
    }
}
