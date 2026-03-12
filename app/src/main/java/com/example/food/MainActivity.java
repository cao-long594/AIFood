package com.example.food;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.food.ui.home.HomeFragment;
import com.example.food.ui.meal.MealFragment;
import com.example.food.ui.water.WaterFragment;
import com.example.food.ui.foodbank.FoodBankFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 主Activity
 * 包含底部导航栏和ViewPager2用于切换不同的Fragment
 */
public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupViewPager();
        setupBottomNavigation();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupViewPager() {
        // 创建ViewPager适配器
        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return 4; // 4个Fragment
            }

            @Override
            public androidx.fragment.app.Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new HomeFragment();
                    case 1:
                        return new MealFragment();
                    case 2:
                        return new WaterFragment();
                    case 3:
                        return new FoodBankFragment();
                    default:
                        return new HomeFragment();
                }
            }
        };

        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false); // 禁用滑动切换

        // 设置ViewPager页面变化监听器
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 根据当前页面设置底部导航栏选中项
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.nav_home);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.nav_meal);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.nav_water);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.nav_foodbank);
                        break;
                }
            }
        });
    }

    private void setupBottomNavigation() {
        // 设置底部导航栏选中项变化监听器
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                viewPager.setCurrentItem(0, false);
                return true;
            } else if (id == R.id.nav_meal) {
                viewPager.setCurrentItem(1, false);
                return true;
            } else if (id == R.id.nav_water) {
                viewPager.setCurrentItem(2, false);
                return true;
            } else if (id == R.id.nav_foodbank) {
                viewPager.setCurrentItem(3, false);
                return true;
            }
            return false;
        });
    }
}