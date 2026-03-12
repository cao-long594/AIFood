package com.example.food.ui.foodbank;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.db.AppDatabase;
import com.example.food.db.FoodSeedImporter;
import com.example.food.db.dao.FoodDao;
import com.example.food.db.entity.Food;
import com.example.food.ui.food.AddFoodActivity;

import java.util.List;

public class FoodBankFragment extends Fragment {

    private RecyclerView foodRecyclerView;
    private SearchView searchView;
    private TextView emptyStateTextView;
    private FoodAdapter foodAdapter;
    private AppDatabase database;
    private FoodDao foodDao;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_foodbank, container, false);
        initViews(root);
        initDatabase();
        loadFoods();

        root.findViewById(R.id.fragment_container).setOnClickListener(v -> {
            if (foodAdapter != null) {
                foodAdapter.clearSelection();
            }
        });

        foodRecyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && foodAdapter != null && foodAdapter.getSelectedPosition() != -1) {
                foodAdapter.clearSelection();
                return true;
            }
            return false;
        });

        return root;
    }

    private void initViews(View root) {
        foodRecyclerView = root.findViewById(R.id.rv_food);
        searchView = root.findViewById(R.id.search_view);
        emptyStateTextView = root.findViewById(R.id.tv_empty_state);

        foodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        foodAdapter = new FoodAdapter(requireContext());
        foodRecyclerView.setAdapter(foodAdapter);

        foodAdapter.setOnFoodClickListener(food -> {
            Intent intent = new Intent(getContext(), AddFoodActivity.class);
            intent.putExtra("food_id", food.getId());
            startActivity(intent);
        });

        foodAdapter.setOnDeleteClickListener((food, position) -> deleteFood(food.getId()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFoods(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFoods(newText);
                return true;
            }
        });

        root.findViewById(R.id.btn_add_food).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddFoodActivity.class);
            startActivity(intent);
        });
    }

    private void initDatabase() {
        database = AppDatabase.getInstance(requireContext());
        foodDao = database.foodDao();
    }

    private void loadFoods() {
        new Thread(() -> {
            FoodSeedImporter.ensureImported(requireContext(), database);
            List<Food> foods = foodDao.getAllFoods();
            runOnUiThreadIfActive(() -> updateFoodList(foods, false));
        }).start();
    }

    private void searchFoods(String query) {
        new Thread(() -> {
            FoodSeedImporter.ensureImported(requireContext(), database);
            List<Food> foods;
            if (query == null || query.trim().isEmpty()) {
                foods = foodDao.getAllFoods();
            } else {
                foods = foodDao.searchFoods(query.trim());
            }
            boolean isSearchMode = query != null && !query.trim().isEmpty();
            runOnUiThreadIfActive(() -> updateFoodList(foods, isSearchMode));
        }).start();
    }

    private void updateFoodList(List<Food> foods, boolean isSearchMode) {
        foodAdapter.setData(foods);
        boolean isEmpty = foods == null || foods.isEmpty();
        emptyStateTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isSearchMode && isEmpty) {
            emptyStateTextView.setText("没有找到匹配的食物，换个关键词试试");
        } else if (isEmpty) {
            emptyStateTextView.setText("还没有食物，点击右下角先添加一个吧");
        }
    }

    private void deleteFood(int foodId) {
        new Thread(() -> {
            foodDao.deleteById(foodId);
            runOnUiThreadIfActive(() -> {
                Toast.makeText(getContext(), "食物已删除", Toast.LENGTH_SHORT).show();
                String query = searchView.getQuery().toString();
                searchFoods(query);
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        String query = searchView.getQuery().toString();
        searchFoods(query);
    }

    private void runOnUiThreadIfActive(Runnable action) {
        if (!isAdded() || getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            if (!isAdded() || getView() == null) {
                return;
            }
            action.run();
        });
    }
}

