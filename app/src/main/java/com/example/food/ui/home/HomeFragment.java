package com.example.food.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.food.R;
import com.example.food.model.NutritionCalculator;
import com.example.food.model.UserGoal;
import com.example.food.ui.common.SelectedDateViewModel;
import com.example.food.ui.history.HistoryActivity;
import com.example.food.utils.DateUtils;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private final DecimalFormat numberFormat = new DecimalFormat("#,##0");
    private final DecimalFormat ratioFormat = new DecimalFormat("0.0");
    private final ActivityResultLauncher<Intent> historyPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    applyHistorySelection(result.getData());
                }
            }
    );

    private TextView dateTextView;
    private TextView caloriesTextView;
    private TextView calorieTargetTextView;
    private TextView remainingCaloriesTextView;
    private TextView carbPercentageTextView;
    private TextView proteinPercentageTextView;
    private TextView fatPercentageTextView;
    private TextView carbRefRangeTextView;
    private TextView proteinRefRangeTextView;
    private TextView fatRefRangeTextView;
    private TextView carbCaloriesTextView;
    private TextView proteinCaloriesTextView;
    private TextView fatCaloriesTextView;
    private TextView carbIntakeGoalTextView;
    private TextView proteinIntakeGoalTextView;
    private TextView fatIntakeGoalTextView;
    private TextView progressPercentageTextView;
    private TextView completionLabelTextView;
    private TextView fatRatioTextView;

    private TextView tabDayTextView;
    private TextView tabWeekTextView;
    private TextView tabMonthTextView;

    private View dayContentLayout;
    private View periodContentLayout;
    private View carbGoalCard;
    private View proteinGoalCard;
    private View fatGoalCard;

    private ProgressBar mainCalorieProgressBar;
    private ProgressBar carbProgressBar;
    private ProgressBar proteinProgressBar;
    private ProgressBar fatProgressBar;

    private NutrientDistributionView nutrientDistributionView;
    private FatCompositionRingView fatCompositionRingView;
    private CombinedChart caloriesChart;
    private CombinedChart carbChart;
    private CombinedChart proteinChart;
    private CombinedChart fatChart;

    private HomeViewModel homeViewModel;
    // ── Task 1: shared date ViewModel ─────────────────────────────────────────
    private SelectedDateViewModel selectedDateViewModel;
    // ─────────────────────────────────────────────────────────────────────────
    private UserGoal userGoal;
    private Date selectedDate;
    private HomeViewModel.TimeGranularity granularity = HomeViewModel.TimeGranularity.DAY;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(root);
        initViewModel();
        return root;
    }

    private void initViews(View root) {
        caloriesTextView = root.findViewById(R.id.tv_calories_consumed);
        calorieTargetTextView = root.findViewById(R.id.tv_calorie_target);
        remainingCaloriesTextView = root.findViewById(R.id.tv_remaining_calories);

        carbIntakeGoalTextView = root.findViewById(R.id.tv_carb_intake_goal);
        proteinIntakeGoalTextView = root.findViewById(R.id.tv_protein_intake_goal);
        fatIntakeGoalTextView = root.findViewById(R.id.tv_fat_intake_goal);

        carbPercentageTextView = root.findViewById(R.id.tv_carb_percentage);
        proteinPercentageTextView = root.findViewById(R.id.tv_protein_percentage);
        fatPercentageTextView = root.findViewById(R.id.tv_fat_percentage);
        carbRefRangeTextView = root.findViewById(R.id.tv_carb_ref_range);
        proteinRefRangeTextView = root.findViewById(R.id.tv_protein_ref_range);
        fatRefRangeTextView = root.findViewById(R.id.tv_fat_ref_range);
        carbCaloriesTextView = root.findViewById(R.id.tv_carb_calories);
        proteinCaloriesTextView = root.findViewById(R.id.tv_protein_calories);
        fatCaloriesTextView = root.findViewById(R.id.tv_fat_calories);
        progressPercentageTextView = root.findViewById(R.id.tv_completion_percentage);
        completionLabelTextView = root.findViewById(R.id.tv_completion_label);
        fatRatioTextView = root.findViewById(R.id.tv_fat_ratio);

        mainCalorieProgressBar = root.findViewById(R.id.progress_calorie_main);
        carbProgressBar = root.findViewById(R.id.progress_carb);
        proteinProgressBar = root.findViewById(R.id.progress_protein);
        fatProgressBar = root.findViewById(R.id.progress_fat);

        carbGoalCard = root.findViewById(R.id.card_carb_goal);
        proteinGoalCard = root.findViewById(R.id.card_protein_goal);
        fatGoalCard = root.findViewById(R.id.card_fat_goal);

        nutrientDistributionView = root.findViewById(R.id.nutrient_distribution_view);
        fatCompositionRingView = root.findViewById(R.id.view_fat_component);
        caloriesChart = root.findViewById(R.id.chart_calories);
        carbChart = root.findViewById(R.id.chart_carb);
        proteinChart = root.findViewById(R.id.chart_protein);
        fatChart = root.findViewById(R.id.chart_fat);

        dayContentLayout = root.findViewById(R.id.layout_day_content);
        periodContentLayout = root.findViewById(R.id.layout_period_content);

        tabDayTextView = root.findViewById(R.id.tv_tab_day);
        tabWeekTextView = root.findViewById(R.id.tv_tab_week);
        tabMonthTextView = root.findViewById(R.id.tv_tab_month);

        // Default to today — may be overridden by shared ViewModel in initViewModel()
        selectedDate = DateUtils.getDateStart(new Date());
        dateTextView = root.findViewById(R.id.tv_current_date);

        View dateCard = root.findViewById(R.id.date_card);
        dateCard.setOnClickListener(v -> openHistoryScreen());
        dateTextView.setOnClickListener(v -> openHistoryScreen());
        calorieTargetTextView.setOnClickListener(v -> showCaloriesGoalInfo());

        carbGoalCard.setOnClickListener(v -> showMacroGoalEditDialog(MetricType.CARB));
        proteinGoalCard.setOnClickListener(v -> showMacroGoalEditDialog(MetricType.PROTEIN));
        fatGoalCard.setOnClickListener(v -> showMacroGoalEditDialog(MetricType.FAT));

        tabDayTextView.setOnClickListener(v -> changeGranularity(HomeViewModel.TimeGranularity.DAY));
        tabWeekTextView.setOnClickListener(v -> changeGranularity(HomeViewModel.TimeGranularity.WEEK));
        tabMonthTextView.setOnClickListener(v -> changeGranularity(HomeViewModel.TimeGranularity.MONTH));

        setupCombinedChart(caloriesChart);
        setupCombinedChart(carbChart);
        setupCombinedChart(proteinChart);
        setupCombinedChart(fatChart);
    }

    private void initViewModel() {
        homeViewModel = new HomeViewModel(requireContext());
        userGoal = homeViewModel.getUserGoal();

        // ── Task 1: init shared date ViewModel ────────────────────────────────
        selectedDateViewModel = new ViewModelProvider(requireActivity())
                .get(SelectedDateViewModel.class);

        // If another fragment already owns a date, adopt it; otherwise seed with today.
        Date sharedDate = selectedDateViewModel.getSelectedDate().getValue();
        if (sharedDate != null) {
            selectedDate = DateUtils.getDateStart(sharedDate);
        } else {
            selectedDateViewModel.setSelectedDate(selectedDate);
        }

        // Observe future changes pushed by MealFragment (or any other fragment).
        selectedDateViewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            if (date == null) return;
            Date normalized = DateUtils.getDateStart(date);
            // Guard: skip if this is our own push or date hasn't actually changed.
            if (DateUtils.isSameDay(selectedDate, normalized)) return;
            selectedDate = normalized;
            loadUiState();
        });
        // ──────────────────────────────────────────────────────────────────────

        loadUiState();
    }

    private void changeGranularity(HomeViewModel.TimeGranularity target) {
        if (granularity == target) {
            return;
        }
        granularity = target;
        loadUiState();
    }

    private void loadUiState() {
        homeViewModel.loadHomeUiState(selectedDate, granularity, this::renderState);
    }

    private void renderState(HomeViewModel.HomeUiState state) {
        userGoal = state.userGoal;
        dateTextView.setText(state.displayTitle);
        updateTabStyles(state.granularity);

        if (state.granularity == HomeViewModel.TimeGranularity.DAY) {
            dayContentLayout.setVisibility(View.VISIBLE);
            periodContentLayout.setVisibility(View.GONE);
            renderDayState(state);
        } else {
            dayContentLayout.setVisibility(View.GONE);
            periodContentLayout.setVisibility(View.VISIBLE);
            renderPeriodState(state);
        }
    }

    private void renderDayState(HomeViewModel.HomeUiState state) {
        NutritionCalculator.NutritionData nutritionData = state.dayNutritionData;
        double caloriesGoal = calculateCaloriesGoalFromMacros(userGoal);
        double currentCalories = nutritionData.getCalories();
        double remainingCalories = caloriesGoal - currentCalories;

        caloriesTextView.setText(formatNumber(currentCalories));
        calorieTargetTextView.setText(String.format(Locale.CHINA, "/ %s kcal", formatNumber(caloriesGoal)));
        remainingCaloriesTextView.setText(buildCalorieStatusText(remainingCalories));

        carbIntakeGoalTextView.setText(formatIntakeGoal(nutritionData.getCarbohydrate(), userGoal.getCarbohydrateGoal()));
        proteinIntakeGoalTextView.setText(formatIntakeGoal(nutritionData.getProtein(), userGoal.getProteinGoal()));
        fatIntakeGoalTextView.setText(formatIntakeGoal(nutritionData.getFat(), userGoal.getFatGoal()));

        double carbCalories = nutritionData.getCarbohydrate() * NutritionCalculator.CALORIES_PER_GRAM_CARBOHYDRATE;
        double proteinCalories = nutritionData.getProtein() * NutritionCalculator.CALORIES_PER_GRAM_PROTEIN;
        double fatCalories = nutritionData.getFat() * NutritionCalculator.CALORIES_PER_GRAM_FAT;

        int[] displayedPercentages = buildDisplayedMacroPercentages(currentCalories, carbCalories, proteinCalories, fatCalories);

        carbPercentageTextView.setText(formatPercent(displayedPercentages[0]));
        proteinPercentageTextView.setText(formatPercent(displayedPercentages[1]));
        fatPercentageTextView.setText(formatPercent(displayedPercentages[2]));

        carbRefRangeTextView.setText(R.string.home_macro_target_carb);
        proteinRefRangeTextView.setText(R.string.home_macro_target_protein);
        fatRefRangeTextView.setText(R.string.home_macro_target_fat);

        carbCaloriesTextView.setText(formatKcalLabel(carbCalories));
        proteinCaloriesTextView.setText(formatKcalLabel(proteinCalories));
        fatCaloriesTextView.setText(formatKcalLabel(fatCalories));

        carbProgressBar.setProgress(clampPercent(calculateGoalProgress(nutritionData.getCarbohydrate(), userGoal.getCarbohydrateGoal())));
        proteinProgressBar.setProgress(clampPercent(calculateGoalProgress(nutritionData.getProtein(), userGoal.getProteinGoal())));
        fatProgressBar.setProgress(clampPercent(calculateGoalProgress(nutritionData.getFat(), userGoal.getFatGoal())));

        nutrientDistributionView.setDistribution(displayedPercentages[0], displayedPercentages[1], displayedPercentages[2]);
        updateCalorieProgressCard(currentCalories, caloriesGoal);
        updateFatCompositionCard(state.fatRatioData);
    }

    private int[] buildDisplayedMacroPercentages(double totalCalories, double carbCalories, double proteinCalories, double fatCalories) {
        if (totalCalories <= 0) {
            return new int[]{0, 0, 0};
        }

        int carbPercent = clampPercent(Math.round((carbCalories / totalCalories) * 100f));
        int proteinPercent = clampPercent(Math.round((proteinCalories / totalCalories) * 100f));
        if (carbPercent + proteinPercent > 100) {
            proteinPercent = Math.max(0, 100 - carbPercent);
        }
        int fatPercent = Math.max(0, 100 - carbPercent - proteinPercent);
        return new int[]{carbPercent, proteinPercent, fatPercent};
    }

    private void renderPeriodState(HomeViewModel.HomeUiState state) {
        if (state.periodSeries == null) {
            return;
        }

        renderCombined(caloriesChart, state.periodSeries, state.granularity, MetricType.CALORIES, ContextCompat.getColor(requireContext(), R.color.primary_color));
        renderCombined(carbChart, state.periodSeries, state.granularity, MetricType.CARB, ContextCompat.getColor(requireContext(), R.color.carb_color));
        renderCombined(proteinChart, state.periodSeries, state.granularity, MetricType.PROTEIN, ContextCompat.getColor(requireContext(), R.color.protein_color));
        renderCombined(fatChart, state.periodSeries, state.granularity, MetricType.FAT, ContextCompat.getColor(requireContext(), R.color.fat_color));
    }

    private void updateTabStyles(HomeViewModel.TimeGranularity current) {
        tabDayTextView.setBackgroundResource(current == HomeViewModel.TimeGranularity.DAY
                ? R.drawable.home_granularity_selected_bg
                : R.drawable.home_granularity_unselected_bg);
        tabWeekTextView.setBackgroundResource(current == HomeViewModel.TimeGranularity.WEEK
                ? R.drawable.home_granularity_selected_bg
                : R.drawable.home_granularity_unselected_bg);
        tabMonthTextView.setBackgroundResource(current == HomeViewModel.TimeGranularity.MONTH
                ? R.drawable.home_granularity_selected_bg
                : R.drawable.home_granularity_unselected_bg);

        int selectedColor = ContextCompat.getColor(requireContext(), R.color.white);
        int normalColor = ContextCompat.getColor(requireContext(), R.color.home_text_secondary);
        tabDayTextView.setTextColor(current == HomeViewModel.TimeGranularity.DAY ? selectedColor : normalColor);
        tabWeekTextView.setTextColor(current == HomeViewModel.TimeGranularity.WEEK ? selectedColor : normalColor);
        tabMonthTextView.setTextColor(current == HomeViewModel.TimeGranularity.MONTH ? selectedColor : normalColor);
    }

    private void updateCalorieProgressCard(double currentCalories, double targetCalories) {
        int progressPercentage = clampPercent(targetCalories <= 0 ? 0 : (currentCalories / targetCalories) * 100);
        mainCalorieProgressBar.setProgress(progressPercentage);
        progressPercentageTextView.setText(String.format(Locale.CHINA, "%d%%", progressPercentage));
        completionLabelTextView.setText(getString(R.string.home_completion_label));
    }

    private double calculateGoalProgress(double intake, double goal) {
        if (goal <= 0) {
            return 0;
        }
        return (intake / goal) * 100;
    }

    private double calculateCaloriesGoalFromMacros(UserGoal goal) {
        if (goal == null) {
            return 0;
        }
        return goal.getCarbohydrateGoal() * NutritionCalculator.CALORIES_PER_GRAM_CARBOHYDRATE
                + goal.getProteinGoal() * NutritionCalculator.CALORIES_PER_GRAM_PROTEIN
                + goal.getFatGoal() * NutritionCalculator.CALORIES_PER_GRAM_FAT;
    }

    private void syncCaloriesGoalToMacros() {
        if (userGoal == null) {
            return;
        }
        userGoal.setCaloriesGoal(calculateCaloriesGoalFromMacros(userGoal));
    }

    private void showCaloriesGoalInfo() {
        Toast.makeText(requireContext(), "\u603b\u70ed\u91cf\u4f1a\u968f\u78b3\u6c34\u3001\u86cb\u767d\u8d28\u3001\u8102\u80aa\u76ee\u6807\u81ea\u52a8\u8054\u52a8", Toast.LENGTH_SHORT).show();
    }

    private void showMacroGoalEditDialog(MetricType metricType) {
        if (getContext() == null || userGoal == null) {
            return;
        }

        String title = getMacroDialogTitle(metricType);
        String hint = getMacroDialogHint(metricType);
        double currentGoal = getCurrentMacroGoal(metricType);

        TextInputLayout inputLayout = new TextInputLayout(requireContext());
        inputLayout.setHint(hint);
        inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        TextInputEditText inputEditText = new TextInputEditText(requireContext());
        inputEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputEditText.setText(String.format(Locale.CHINA, "%.0f", currentGoal));
        inputLayout.addView(inputEditText);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(inputLayout)
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u4fdd\u5b58", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String inputValue = inputEditText.getText() == null ? "" : inputEditText.getText().toString().trim();
                    if (inputValue.isEmpty()) {
                        inputLayout.setError("\u76ee\u6807\u514b\u6570\u5fc5\u987b\u5927\u4e8e 0");
                        return;
                    }

                    try {
                        double newGoal = Double.parseDouble(inputValue);
                        if (newGoal <= 0) {
                            inputLayout.setError("\u76ee\u6807\u514b\u6570\u5fc5\u987b\u5927\u4e8e 0");
                            return;
                        }
                        inputLayout.setError(null);
                        applyMacroGoal(metricType, newGoal);
                        syncCaloriesGoalToMacros();
                        homeViewModel.saveUserGoal(userGoal);
                        loadUiState();
                        Toast.makeText(requireContext(), title + "\u5df2\u66f4\u65b0", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } catch (NumberFormatException exception) {
                        inputLayout.setError("\u8bf7\u8f93\u5165\u6709\u6548\u6570\u5b57");
                    }
                }));

        dialog.show();
    }

    private String getMacroDialogTitle(MetricType metricType) {
        switch (metricType) {
            case CARB:
                return "\u8bbe\u7f6e\u78b3\u6c34\u76ee\u6807";
            case PROTEIN:
                return "\u8bbe\u7f6e\u86cb\u767d\u76ee\u6807";
            case FAT:
                return "\u8bbe\u7f6e\u8102\u80aa\u76ee\u6807";
            default:
                return "\u8bbe\u7f6e\u76ee\u6807";
        }
    }

    private String getMacroDialogHint(MetricType metricType) {
        switch (metricType) {
            case CARB:
                return "璇疯緭鍏ョ⒊姘寸洰鏍?g)";
            case PROTEIN:
                return "璇疯緭鍏ヨ泲鐧界洰鏍?g)";
            case FAT:
                return "璇疯緭鍏ヨ剛鑲洰鏍?g)";
            default:
                return "璇疯緭鍏ョ洰鏍?g)";
        }
    }

    private double getCurrentMacroGoal(MetricType metricType) {
        switch (metricType) {
            case CARB:
                return userGoal.getCarbohydrateGoal();
            case PROTEIN:
                return userGoal.getProteinGoal();
            case FAT:
            default:
                return userGoal.getFatGoal();
        }
    }

    private void applyMacroGoal(MetricType metricType, double value) {
        switch (metricType) {
            case CARB:
                userGoal.setCarbohydrateGoal(value);
                break;
            case PROTEIN:
                userGoal.setProteinGoal(value);
                break;
            case FAT:
                userGoal.setFatGoal(value);
                break;
            default:
                break;
        }
    }

    private void updateFatCompositionCard(HomeViewModel.FatRatioData fatRatioData) {
        fatCompositionRingView.setComposition(fatRatioData.saturated, fatRatioData.mono, fatRatioData.poly);
        fatRatioTextView.setText(buildFatCompositionText(
                fatRatioData.saturated,
                fatRatioData.mono,
                fatRatioData.poly
        ));
    }

    private void setupCombinedChart(CombinedChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);
        chart.setDragEnabled(true);
        chart.setNoDataText("\u6682\u65e0\u6570\u636e");

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.home_text_secondary));
        xAxis.setTextSize(10f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.home_text_secondary));
        leftAxis.setTextSize(10f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ContextCompat.getColor(requireContext(), R.color.home_divider));

        chart.getAxisRight().setEnabled(false);
    }

    private void renderCombined(CombinedChart chart,
                                HomeViewModel.PeriodSeries periodSeries,
                                HomeViewModel.TimeGranularity granularity,
                                MetricType metricType,
                                int actualColor) {
        List<String> labels = new ArrayList<>();
        List<BarEntry> bars = new ArrayList<>();
        List<Entry> goals = new ArrayList<>();
        ArrayList<Integer> barColors = new ArrayList<>();

        double goalValue;
        switch (metricType) {
            case CALORIES:
                goalValue = periodSeries.goalCalories;
                break;
            case CARB:
                goalValue = periodSeries.goalCarb;
                break;
            case PROTEIN:
                goalValue = periodSeries.goalProtein;
                break;
            case FAT:
            default:
                goalValue = periodSeries.goalFat;
                break;
        }

        for (int i = 0; i < periodSeries.points.size(); i++) {
            HomeViewModel.DailyAggregatePoint point = periodSeries.points.get(i);
            labels.add(buildAxisLabel(point.date, granularity));
            bars.add(new BarEntry(i, extractMetricValue(point, metricType)));
            if (!point.isFuture) {
                goals.add(new Entry(i, (float) goalValue));
            }
            barColors.add(point.isFuture ? Color.TRANSPARENT : actualColor);
        }

        BarDataSet barDataSet = new BarDataSet(bars, "\u5b9e\u9645");
        barDataSet.setColors(barColors);
        barDataSet.setDrawValues(false);
        barDataSet.setHighLightAlpha(0);
        barDataSet.setHighLightColor(Color.parseColor("#171328"));

        LineDataSet lineDataSet = new LineDataSet(goals, "\u76ee\u6807");
        lineDataSet.setColor(Color.parseColor("#171328"));
        lineDataSet.setLineWidth(1.8f);
        lineDataSet.setCircleRadius(2.6f);
        lineDataSet.setCircleColor(Color.parseColor("#171328"));
        lineDataSet.setDrawValues(false);
        lineDataSet.setHighlightEnabled(false);

        CombinedData combinedData = new CombinedData();
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.55f);
        combinedData.setData(barData);
        combinedData.setData(new LineData(lineDataSet));

        chart.setData(combinedData);
        chart.setMarker(new PeriodChartMarkerView(requireContext(), periodSeries, (float) goalValue));
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                chart.invalidate();
            }

            @Override
            public void onNothingSelected() {
                chart.invalidate();
            }
        });
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setLabelCount(Math.min(labels.size(), 7), false);
        chart.getXAxis().setAxisMinimum(-0.5f);
        chart.getXAxis().setAxisMaximum(labels.size() - 0.5f);
        chart.invalidate();
    }

    private String buildAxisLabel(Date date, HomeViewModel.TimeGranularity granularity) {
        if (granularity == HomeViewModel.TimeGranularity.WEEK) {
            return DateUtils.formatDate(date, "M/d");
        }
        return DateUtils.formatDate(date, "d");
    }

    private void applyHistorySelection(Intent data) {
        long selectedMillis = data.getLongExtra(
                HistoryActivity.RESULT_ANCHOR_DATE_MILLIS,
                selectedDate == null ? System.currentTimeMillis() : selectedDate.getTime()
        );
        String viewType = data.getStringExtra(HistoryActivity.RESULT_VIEW_TYPE);
        selectedDate = DateUtils.getDateStart(new Date(selectedMillis));
        if (viewType != null) {
            try {
                granularity = HomeViewModel.TimeGranularity.valueOf(viewType);
            } catch (IllegalArgumentException ignored) {
                granularity = HomeViewModel.TimeGranularity.DAY;
            }
        }
        // ── Task 1: broadcast new date to other fragments ─────────────────────
        if (selectedDateViewModel != null) {
            selectedDateViewModel.setSelectedDate(selectedDate);
        }
        // ──────────────────────────────────────────────────────────────────────
        loadUiState();
    }

    private float extractMetricValue(HomeViewModel.DailyAggregatePoint point, MetricType metricType) {
        switch (metricType) {
            case CALORIES:
                return (float) point.actualCalories;
            case CARB:
                return (float) point.actualCarb;
            case PROTEIN:
                return (float) point.actualProtein;
            case FAT:
            default:
                return (float) point.actualFat;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUiState();
    }

    private void openHistoryScreen() {
        if (selectedDate == null) {
            return;
        }
        historyPickerLauncher.launch(
                HistoryActivity.createIntent(requireContext(), selectedDate.getTime(), granularity.name())
        );
    }

    private String formatNumber(double value) {
        return numberFormat.format(Math.round(value));
    }

    private String formatKcalLabel(double value) {
        return formatNumber(value) + " kcal";
    }

    private String formatIntakeGoal(double intake, double goal) {
        return formatNumber(intake) + "/" + formatNumber(goal) + "g";
    }

    private String formatPercent(int value) {
        return value + "%";
    }

    private int clampPercent(double value) {
        return (int) Math.max(0, Math.min(100, Math.round(value)));
    }

    private String buildCalorieStatusText(double remainingCalories) {
        if (remainingCalories > 0) {
            return "继续吃吧" + formatNumber(remainingCalories) + " kcal";
        }
        if (remainingCalories < 0) {
            return "超标太超标了" + formatNumber(Math.abs(remainingCalories)) + " kcal";
        }
        return "恰好达标，您真是古希腊掌管卡路里的神";
    }

    private String buildFatCompositionText(double saturated, double mono, double poly) {
        double safeSat = Math.max(0d, saturated);
        double safeMono = Math.max(0d, mono);
        double safePoly = Math.max(0d, poly);

        if (safeSat == 0d && safeMono == 0d && safePoly == 0d) {
            return "饱:单?多?0:0:0";
        }
        if (safeSat > 0d) {
            return "饱:单?多?1:" + formatRatioValue(safeMono / safeSat) + ":" + formatRatioValue(safePoly / safeSat);
        }
        if (safeMono >= safePoly) {
            double ratioPoly = safeMono == 0d ? 0d : safePoly / safeMono;
            return "饱:单?多?0:1:" + formatRatioValue(ratioPoly);
        }
        double ratioMono = safePoly == 0d ? 0d : safeMono / safePoly;
        return "饱:单?多?0:" + formatRatioValue(ratioMono) + ":1";
    }

    private String formatRatioValue(double value) {
        double rounded = Math.round(value * 10.0d) / 10.0d;
        if (Math.abs(rounded - Math.rint(rounded)) < 1e-6) {
            return String.format(Locale.CHINA, "%.0f", rounded);
        }
        return String.format(Locale.CHINA, "%.1f", rounded);
    }

    private enum MetricType {
        CALORIES,
        CARB,
        PROTEIN,
        FAT
    }
}