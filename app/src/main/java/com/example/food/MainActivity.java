package com.example.food;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.food.data.preferences.UserSessionPreferences;
import com.example.food.ui.home.HomeFragment;
import com.example.food.ui.meal.MealFragment;
import com.example.food.ui.foodbank.FoodBankFragment;
import com.example.food.ui.login.LoginActivity;
import com.example.food.ui.profile.ProfileFragment;
import com.example.food.ui.water.WaterFragment;
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
                return 5;
            }

            @Override
            public androidx.fragment.app.Fragment createFragment(int position) {
                switch (position) {
                    case 0: return new HomeFragment();
                    case 1: return new MealFragment();
                    case 2: return new WaterFragment();
                    case 3: return new FoodBankFragment();
                    case 4: return new ProfileFragment();
                    default: return new HomeFragment();
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
                    case 4:
                        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        UserSessionPreferences session = new UserSessionPreferences(this);
        session.clearSession();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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
            } else if (id == R.id.nav_profile) {
                viewPager.setCurrentItem(4, false);
                return true;
            }
            return false;
        });
    }
}
