package com.example.food.ui.meal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.food.R;
import com.example.food.db.entity.MealRecord;
import com.example.food.ui.common.SelectedDateViewModel;
import com.example.food.utils.DateUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MealFragment extends Fragment {

    private static final String[] TAB_TITLES = {"早餐", "午餐", "下午加餐", "晚餐", "睡前"};

    private static final String KEY_SELECTED_DATE = "meal_selected_date";
    private static final String KEY_CURRENT_TAB = "meal_current_tab";
    private static final String KEY_CALENDAR_COLLAPSED = "meal_calendar_collapsed";

    private static Integer sTabPositionForProcess = null;

    private TextView tvCurrentDate;
    private TextView tvSelectedDaySummary;
    private TextView tvToggleCalendar;
    private ImageView ivToggleCalendar;
    private View cardWeekCalendar;

    private Date selectedDate;
    private boolean isCalendarCollapsed = false;

    private ViewPager2 viewPager2;
    private ViewPager2 vpCalendar;
    private TabLayout tabLayout;
    private MealAdapter pagerAdapter;
    private CalendarAdapter calendarPagerAdapter;
    private SelectedDateViewModel selectedDateViewModel;

    private int currentTabPosition = -1;

    private final ActivityResultLauncher<Intent> addFoodLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && pagerAdapter != null) {
                    pagerAdapter.refreshAllFragments();
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.meal_fragment, container, false);
        restoreState(savedInstanceState);
        initSharedDateViewModel();
        initViews(root);
        setupViewPager();
        return root;
    }

    private void restoreState(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            long savedDate = savedInstanceState.getLong(KEY_SELECTED_DATE, -1L);
            if (savedDate > 0) {
                selectedDate = new Date(savedDate);
            }
            currentTabPosition = savedInstanceState.getInt(KEY_CURRENT_TAB, -1);
            isCalendarCollapsed = savedInstanceState.getBoolean(KEY_CALENDAR_COLLAPSED, false);
        }

        if (selectedDate == null) {
            selectedDate = Calendar.getInstance().getTime();
        }
    }

    private void initSharedDateViewModel() {
        selectedDateViewModel = new ViewModelProvider(requireActivity()).get(SelectedDateViewModel.class);
        Date sharedDate = selectedDateViewModel.getSelectedDate().getValue();
        if (sharedDate != null) {
            selectedDate = sharedDate;
        }
        selectedDateViewModel.setSelectedDate(selectedDate);

        // ── Task 1: observe future date changes pushed by HomeFragment ─────────
        selectedDateViewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            if (date == null) return;
            Date normalized = DateUtils.getDateStart(date);
            // Guard: skip if this is our own push or the day hasn't changed
            if (selectedDate != null && DateUtils.isSameDay(selectedDate, normalized)) return;

            selectedDate = normalized;
            // Scroll the calendar strip to the new date and refresh the meal list
            scrollCalendarToDate(selectedDate);
            updateDateDisplay();
        });
        // ──────────────────────────────────────────────────────────────────────
    }

    /**
     * Scrolls the week-calendar ViewPager2 to the page that contains {@code date}
     * and updates the selected-day highlight inside the adapter.
     * Safe to call at any time after the view is created.
     */
    private void scrollCalendarToDate(Date date) {
        if (vpCalendar == null || calendarPagerAdapter == null || date == null) return;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int position = calendarPagerAdapter.getPositionForDate(cal);
        vpCalendar.setCurrentItem(position, false);
        calendarPagerAdapter.setSelectedDateExternal(cal);
        calendarPagerAdapter.setCurrentPosition(position);
    }

    private void initViews(View root) {
        tvCurrentDate = root.findViewById(R.id.tv_current_date);
        tvSelectedDaySummary = root.findViewById(R.id.tv_selected_day_summary);
        tvToggleCalendar = root.findViewById(R.id.tv_toggle_calendar);
        ivToggleCalendar = root.findViewById(R.id.iv_toggle_calendar);
        cardWeekCalendar = root.findViewById(R.id.card_week_calendar);

        vpCalendar = root.findViewById(R.id.vp_calendar);
        vpCalendar.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        Calendar initialCalendar = Calendar.getInstance();
        initialCalendar.setTime(selectedDate);

        calendarPagerAdapter = new CalendarAdapter(getContext(), initialCalendar);
        vpCalendar.setAdapter(calendarPagerAdapter);
        vpCalendar.setCurrentItem(calendarPagerAdapter.getCurrentPosition(), false);

        updateDateDisplay();
        updateCalendarCollapsedUi(false, false);

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

        root.findViewById(R.id.fab_add_food).setOnClickListener(v -> openMealAddActivity());
        root.findViewById(R.id.btn_toggle_calendar).setOnClickListener(v -> toggleCalendarCollapse());
    }

    private void setupViewPager() {
        pagerAdapter = new MealAdapter(this, selectedDate);
        viewPager2.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> tab.setText(TAB_TITLES[position])).attach();

        applyInitialTabSelection();

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentTabPosition = position;
                sTabPositionForProcess = position;
            }
        });
    }

    private void toggleCalendarCollapse() {
        isCalendarCollapsed = !isCalendarCollapsed;
        updateCalendarCollapsedUi(true, true);
    }

    private void updateCalendarCollapsedUi(boolean fromUser, boolean animate) {
        tvToggleCalendar.setText(isCalendarCollapsed
                ? getString(R.string.meal_expand)
                : getString(R.string.meal_collapse));
        ivToggleCalendar.setImageResource(isCalendarCollapsed
                ? R.drawable.meal_ic_chevron_down
                : R.drawable.meal_ic_chevron_up);

        if (!animate || !fromUser) {
            cardWeekCalendar.setVisibility(isCalendarCollapsed ? View.GONE : View.VISIBLE);
            cardWeekCalendar.setAlpha(1f);
            cardWeekCalendar.setTranslationY(0f);
            return;
        }

        float shift = dpToPx(6f);
        cardWeekCalendar.animate().cancel();

        if (isCalendarCollapsed) {
            cardWeekCalendar.animate()
                    .alpha(0f)
                    .translationY(-shift)
                    .setDuration(180)
                    .withEndAction(() -> {
                        cardWeekCalendar.setVisibility(View.GONE);
                        cardWeekCalendar.setAlpha(1f);
                        cardWeekCalendar.setTranslationY(0f);
                    })
                    .start();
            return;
        }

        cardWeekCalendar.setVisibility(View.VISIBLE);
        cardWeekCalendar.setAlpha(0f);
        cardWeekCalendar.setTranslationY(-shift);
        cardWeekCalendar.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .start();
    }

    private void openMealAddActivity() {
        int currentMealType = pagerAdapter == null
                ? MealRecord.MEAL_TYPE_BREAKFAST
                : pagerAdapter.getMealTypeByPosition(viewPager2.getCurrentItem());

        Intent intent = new Intent(getContext(), AddActivity.class);
        intent.putExtra("mealType", currentMealType);
        intent.putExtra("selectedDate", selectedDate.getTime());
        addFoodLauncher.launch(intent);
    }

    private void onDateSelected(Calendar selectedCalendar) {
        selectedDate = selectedCalendar.getTime();
        updateDateDisplay();
        if (selectedDateViewModel != null) {
            selectedDateViewModel.setSelectedDate(selectedDate);
        }
    }

    private void updateDateDisplay() {
        if (selectedDate == null) {
            return;
        }

        tvCurrentDate.setText(DateUtils.formatDate(selectedDate, "yyyy年M月"));
        tvSelectedDaySummary.setText(String.format(
                Locale.CHINA,
                "%s | %s",
                DateUtils.formatDate(selectedDate, "M月d日"),
                DateUtils.getWeekDay(selectedDate)
        ));

        if (pagerAdapter != null) {
            pagerAdapter.updateSelectedDate(selectedDate);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void applyInitialTabSelection() {
        if (viewPager2 == null) {
            return;
        }

        int targetTab;
        if (currentTabPosition >= 0 && currentTabPosition < TAB_TITLES.length) {
            targetTab = currentTabPosition;
            sTabPositionForProcess = targetTab;
        } else if (sTabPositionForProcess != null
                && sTabPositionForProcess >= 0
                && sTabPositionForProcess < TAB_TITLES.length) {
            targetTab = sTabPositionForProcess;
            currentTabPosition = targetTab;
        } else {
            targetTab = resolveAutoTabByCurrentTime();
            currentTabPosition = targetTab;
            sTabPositionForProcess = targetTab;
        }

        viewPager2.setCurrentItem(targetTab, false);
    }

    private int resolveAutoTabByCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

        if (minutes < 12 * 60) {
            return 0;
        }
        if (minutes < 14 * 60) {
            return 1;
        }
        if (minutes < 17 * 60) {
            return 2;
        }
        if (minutes < 20 * 60) {
            return 3;
        }
        return 4;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (selectedDate != null) {
            outState.putLong(KEY_SELECTED_DATE, selectedDate.getTime());
        }

        int tabToSave = (viewPager2 != null) ? viewPager2.getCurrentItem() : currentTabPosition;
        outState.putInt(KEY_CURRENT_TAB, tabToSave);
        outState.putBoolean(KEY_CALENDAR_COLLAPSED, isCalendarCollapsed);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}