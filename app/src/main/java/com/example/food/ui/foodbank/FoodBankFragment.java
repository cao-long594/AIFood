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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.data.repository.FoodRepository;
import com.example.food.db.entity.Food;

import java.util.ArrayList;
import java.util.List;

public class FoodBankFragment extends Fragment {

    private RecyclerView foodRecyclerView;
    private SearchView searchView;
    private TextView emptyStateTextView;
    private View addFoodFab;

    private FoodAdapter foodAdapter;
    private FoodRepository foodRepository;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_foodbank, container, false);
        foodRepository = new FoodRepository(requireContext());
        initViews(root);
        loadFoods();

        root.findViewById(R.id.fragment_container).setOnClickListener(v -> foodAdapter.clearSelection());

        foodRecyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    && foodAdapter.getSelectedPosition() != RecyclerView.NO_POSITION) {
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
        addFoodFab = root.findViewById(R.id.btn_add_food);

        foodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(200);
        animator.setRemoveDuration(200);
        animator.setMoveDuration(200);
        animator.setChangeDuration(200);
        foodRecyclerView.setItemAnimator(animator);

        foodAdapter = new FoodAdapter(requireContext());
        foodRecyclerView.setAdapter(foodAdapter);

        foodAdapter.setOnFoodClickListener(food -> {
            Intent intent = new Intent(getContext(), AddFoodActivity.class);
            intent.putExtra("food_id", food.getId());
            startActivity(intent);
        });

        foodAdapter.setOnDeleteClickListener((food, position) -> deleteFood(food.getId()));
        foodAdapter.setOnHeaderClickListener(foodAdapter::toggleGroup);

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

        addFoodFab.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddFoodActivity.class);
            startActivity(intent);
        });
    }

    private void loadFoods() {
        foodRepository.loadAllFoods(foods -> updateFoodList(foods, false));
    }

    private void searchFoods(String query) {
        String trimmed = query == null ? "" : query.trim();
        boolean isSearchMode = !trimmed.isEmpty();
        foodRepository.searchFoods(trimmed, foods -> updateFoodList(foods, isSearchMode));
    }

    private void updateFoodList(List<Food> foods, boolean isSearchMode) {
        List<Food> safeFoods = foods == null ? new ArrayList<>() : foods;
        foodAdapter.setData(safeFoods, isSearchMode);

        boolean isEmpty = safeFoods.isEmpty();
        emptyStateTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isSearchMode && isEmpty) {
            emptyStateTextView.setText(getString(R.string.search_no_result));
        } else if (isEmpty) {
            emptyStateTextView.setText(getString(R.string.foodbank_empty_state));
        }

        addFoodFab.setVisibility(safeFoods.size() > 5 ? View.VISIBLE : View.GONE);
    }

    private void deleteFood(int foodId) {
        foodRepository.deleteById(foodId, () -> {
            Toast.makeText(getContext(), R.string.food_deleted, Toast.LENGTH_SHORT).show();
            String query = searchView.getQuery().toString();
            searchFoods(query);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String query = searchView.getQuery().toString();
        searchFoods(query);
    }
}