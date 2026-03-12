package com.example.food.ui.meal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.food.R;
import com.example.food.utils.DateUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MealFragment extends Fragment {

    private TextView tvCurrentDate;
    private TextView tvSelectedDaySummary;
    private Date selectedDate;
    private ViewPager2 viewPager2;
    private ViewPager2 vpCalendar;
    private TabLayout tabLayout;
    private MealAdapter pagerAdapter;
    private CalendarAdapter calendarPagerAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.meal_fragment, container, false);
        initViews(root);
        setupViewPager();
        return root;
    }

    private void initViews(View root) {
        tvCurrentDate = root.findViewById(R.id.tv_current_date);
        tvSelectedDaySummary = root.findViewById(R.id.tv_selected_day_summary);

        vpCalendar = root.findViewById(R.id.vp_calendar);
        vpCalendar.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        Calendar initialCalendar = Calendar.getInstance();
        selectedDate = initialCalendar.getTime();
        calendarPagerAdapter = new CalendarAdapter(getContext(), initialCalendar);
        vpCalendar.setAdapter(calendarPagerAdapter);
        int initialPosition = calendarPagerAdapter.getCurrentPosition();
        vpCalendar.setCurrentItem(initialPosition, false);
        updateDateDisplay();

        calendarPagerAdapter.setOnDateSelectedListener(this::onDateSelected);
        vpCalendar.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                calendarPagerAdapter.setCurrentPosition(position);
            }
        });

        viewPager2 = root.findViewById(R.id.viewPager2);
        tabLayout = root.findViewById(R.id.tabLayout);
        root.findViewById(R.id.floating_card).setOnClickListener(v -> openMealAddActivity());
    }

    private void setupViewPager() {
        pagerAdapter = new MealAdapter(this, selectedDate);
        viewPager2.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            String[] tabTitles = {"早餐", "午餐", "下午加餐", "晚餐", "睡前"};
            tab.setText(tabTitles[position]);
        }).attach();
    }

    private void openMealAddActivity() {
        int currentMealType = pagerAdapter == null
                ? com.example.food.db.entity.MealRecord.MEAL_TYPE_BREAKFAST
                : pagerAdapter.getMealTypeByPosition(viewPager2.getCurrentItem());

        Intent intent = new Intent(getContext(), AddActivity.class);
        intent.putExtra("mealType", currentMealType);
        intent.putExtra("selectedDate", selectedDate.getTime());
        startActivityForResult(intent, 100);
    }

    private void onDateSelected(Calendar selectedCalendar) {
        selectedDate = selectedCalendar.getTime();
        updateDateDisplay();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == requireActivity().RESULT_OK && pagerAdapter != null) {
            pagerAdapter.refreshAllFragments();
        }
    }

    private void updateDateDisplay() {
        if (selectedDate == null) {
            return;
        }
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy年M月", Locale.getDefault());
        tvCurrentDate.setText(monthFormat.format(selectedDate));
        tvSelectedDaySummary.setText(
                DateUtils.formatDate(selectedDate, "M月d日") + " " + DateUtils.getWeekDay(selectedDate)
        );

        if (pagerAdapter != null) {
            pagerAdapter.updateSelectedDate(selectedDate);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pagerAdapter != null) {
            pagerAdapter.refreshAllFragments();
        }
    }
}

