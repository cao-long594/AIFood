package com.example.food.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.data.preferences.OperationLogManager;
import com.example.food.data.repository.FoodRepository;
import com.example.food.db.entity.Food;
import com.example.food.ui.foodbank.AddFoodActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员 - 食物库管理页面
 * 支持按可见性/来源筛选，升级/降级公开状态，编辑/删除食物
 */
public class FoodManagementFragment extends Fragment {

    private RecyclerView rvFoods;
    private SearchView searchView;
    private TextView tvEmpty;
    private Spinner spinnerVisibility, spinnerSource;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAddFood;

    private FoodRepository foodRepository;
    private FoodManagementAdapter foodAdapter;
    private OperationLogManager logManager;
    private List<Food> foodList = new ArrayList<>();
    private List<Food> filteredList = new ArrayList<>();

    private String visibilityFilter = "全部";
    private String sourceFilter = "全部";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_foods, container, false);
        rvFoods = root.findViewById(R.id.rv_foods);
        searchView = root.findViewById(R.id.search_view);
        tvEmpty = root.findViewById(R.id.tv_empty);
        spinnerVisibility = root.findViewById(R.id.spinner_visibility);
        spinnerSource = root.findViewById(R.id.spinner_source);
        fabAddFood = root.findViewById(R.id.fab_add_food);

        foodRepository = new FoodRepository(requireContext());
        logManager = new OperationLogManager(requireContext());

        rvFoods.setLayoutManager(new LinearLayoutManager(getContext()));
        foodAdapter = new FoodManagementAdapter(filteredList, new FoodManagementAdapter.OnActionListener() {
            @Override public void onPromote(Food food) { promoteToPublic(food); }
            @Override public void onDemote(Food food) { demoteToPrivate(food); }
            @Override public void onEdit(Food food) { editFood(food); }
            @Override public void onDelete(Food food) { confirmDeleteFood(food); }
        });
        foodAdapter.setOnHeaderClickListener(cat -> foodAdapter.toggleGroup(cat));
        rvFoods.setAdapter(foodAdapter);

        // 可见性筛选
        ArrayAdapter<String> visAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new String[]{"全部", "公开", "非公开"});
        visAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVisibility.setAdapter(visAdapter);
        spinnerVisibility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                visibilityFilter = visAdapter.getItem(pos);
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // 来源筛选
        ArrayAdapter<String> srcAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new String[]{"全部", "系统导入", "用户分享"});
        srcAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSource.setAdapter(srcAdapter);
        spinnerSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                sourceFilter = srcAdapter.getItem(pos);
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilters();
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilters();
                return true;
            }
        });

        fabAddFood.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), com.example.food.ui.foodbank.AddFoodActivity.class);
            startActivity(intent);
        });

        loadAllFoods();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllFoods();
    }

    private void loadAllFoods() {
        foodRepository.loadAllFoods(foods -> {
            foodList = foods != null ? foods : new ArrayList<>();
            applyFilters();
        });
    }

    private void applyFilters() {
        String query = searchView.getQuery() != null ? searchView.getQuery().toString().trim() : "";
        filteredList = new ArrayList<>();

        for (Food f : foodList) {
            // 搜索过滤
            if (!query.isEmpty() && !f.getName().toLowerCase().contains(query.toLowerCase())) continue;
            // 可见性过滤（基于 visibilityStatus）
            if ("公开".equals(visibilityFilter) && f.getVisibilityStatus() != 1) continue;
            if ("非公开".equals(visibilityFilter) && f.getVisibilityStatus() == 1) continue;
            // 来源过滤
            if ("系统导入".equals(sourceFilter) && !"SYSTEM".equals(f.getSource())) continue;
            if ("用户分享".equals(sourceFilter) && !"USER".equals(f.getSource())) continue;

            filteredList.add(f);
        }

        boolean isSearch = query != null && !query.isEmpty();
        // 有文字搜索时用平铺模式，仅筛选时保留分类折叠
        foodAdapter.setData(filteredList, isSearch);
        tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void promoteToPublic(Food food) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("设为公开食物")
                .setMessage("将 \"" + food.getName() + "\" 设为公开后，所有用户均可查看。")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定升级", (d, w) -> {
                    foodRepository.promoteToPublic(food.getId(), () -> {
                        Toast.makeText(getContext(), "已设为公开食物", Toast.LENGTH_SHORT).show();
                        logFoodAction("food_promote", "升级公开食物 " + food.getName(), food.getId());
                        loadAllFoods();
                    });
                }).show();
    }

    private void demoteToPrivate(Food food) {
        boolean isSystemFood = "SYSTEM".equals(food.getSource());
        String msg = isSystemFood
                ? "将 \"" + food.getName() + "\" 设为私密。"
                : "将 \"" + food.getName() + "\" 设为私密。";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("设为私密")
                .setMessage(msg)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定降级", (d, w) -> {
                    foodRepository.demoteToAdminOnly(food.getId(), () -> {
                        Toast.makeText(getContext(), "已设为私密", Toast.LENGTH_SHORT).show();
                        logFoodAction("food_demote", "降级私密食物 " + food.getName(), food.getId());
                        loadAllFoods();
                    });
                }).show();
    }

    private void logFoodAction(String action, String desc, int foodId) {
        try {
            JSONObject undoData = new JSONObject();
            undoData.put("foodId", foodId);
            logManager.addLog(action, desc, undoData.toString());
        } catch (Exception ignored) {}
    }

    private void editFood(Food food) {
        Intent intent = new Intent(getContext(), AddFoodActivity.class);
        intent.putExtra("food_id", food.getId());
        startActivity(intent);
    }

    private void confirmDeleteFood(Food food) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("确认删除")
                .setMessage("确定要删除 \"" + food.getName() + "\" 吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (d, w) -> {
                    String deletedName = food.getName();
                    foodRepository.softDelete(food.getId(), () -> {
                        Toast.makeText(getContext(), "食物已删除", Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject undoData = new JSONObject();
                            undoData.put("foodId", food.getId());
                            logManager.addLog("food_delete", "删除食物 " + deletedName, undoData.toString());
                        } catch (Exception ignored) {}
                        loadAllFoods();
                    });
                }).show();
    }
}
