package com.example.food.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.food.R;
import com.example.food.model.NutritionCalculator;
import com.example.food.model.UserGoal;
import com.example.food.ui.history.HistoryActivity;
import com.example.food.utils.DateUtils;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private final DecimalFormat numberFormat = new DecimalFormat("#,##0");
    private final DecimalFormat ratioFormat = new DecimalFormat("0.0");

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

        carbRefRangeTextView.setText("闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂侀潧顦弲娑氬閻熸噴褰掓偐瀹割喖鍓伴梺缁樺笒閻忔岸濡甸崟顖氱闁糕剝銇炴竟鏇㈡⒒娴ｅ憡鎲稿┑顔炬暩缁瑩骞樺?5%-65%");
        proteinRefRangeTextView.setText("闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂侀潧顦弲娑氬閻熸噴褰掓偐瀹割喖鍓伴梺缁樺笒閻忔岸濡甸崟顖氱闁糕剝銇炴竟鏇㈡⒒娴ｅ憡鎲稿┑顔炬暩缁瑩骞樺?0%-35%");
        fatRefRangeTextView.setText("闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂侀潧顦弲娑氬閻熸噴褰掓偐瀹割喖鍓伴梺缁樺笒閻忔岸濡甸崟顖氱闁糕剝銇炴竟鏇㈡⒒娴ｅ憡鎲稿┑顔炬暩缁瑩骞樺?0%-35%");

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

        renderCombined(caloriesChart, state.periodSeries, state.granularity, MetricType.CALORIES, getResources().getColor(R.color.primary_color));
        renderCombined(carbChart, state.periodSeries, state.granularity, MetricType.CARB, getResources().getColor(R.color.carb_color));
        renderCombined(proteinChart, state.periodSeries, state.granularity, MetricType.PROTEIN, getResources().getColor(R.color.protein_color));
        renderCombined(fatChart, state.periodSeries, state.granularity, MetricType.FAT, getResources().getColor(R.color.fat_color));
    }

    private void updateTabStyles(HomeViewModel.TimeGranularity current) {
        tabDayTextView.setBackgroundResource(current == HomeViewModel.TimeGranularity.DAY ? R.drawable.home_granularity_selected_bg : R.drawable.home_granularity_unselected_bg);
        tabWeekTextView.setBackgroundResource(current == HomeViewModel.TimeGranularity.WEEK ? R.drawable.home_granularity_selected_bg : R.drawable.home_granularity_unselected_bg);
        tabMonthTextView.setBackgroundResource(current == HomeViewModel.TimeGranularity.MONTH ? R.drawable.home_granularity_selected_bg : R.drawable.home_granularity_unselected_bg);
    }

    private void updateCalorieProgressCard(double currentCalories, double targetCalories) {
        int progressPercentage = clampPercent(targetCalories <= 0 ? 0 : (currentCalories / targetCalories) * 100);
        mainCalorieProgressBar.setProgress(progressPercentage);
        progressPercentageTextView.setText(String.format(Locale.CHINA, "%d%%", progressPercentage));
        completionLabelTextView.setText("Complete");
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
        Toast.makeText(requireContext(), "Calories goal follows macro targets", Toast.LENGTH_SHORT).show();
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
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String inputValue = inputEditText.getText() == null ? "" : inputEditText.getText().toString().trim();
                    if (inputValue.isEmpty()) {
                        inputLayout.setError("Please enter a target value");
                        return;
                    }

                    try {
                        double newGoal = Double.parseDouble(inputValue);
                        if (newGoal <= 0) {
                            inputLayout.setError("闂傚倷鑳堕崕鐢稿疾閳哄懎绐楁俊銈呮噺閸嬪鏌ㄥ┑鍡╂Ц缂佲偓閸岀偞鍊甸柨婵嗛娴滄繃绻涢崼婵堝煟闁绘搩鍋婂畷鎯邦槼闁搞倕娲弻鏇㈠幢濡ゅ啰顔夐悗鍨緲鐎氼垶藝鏉堚晝纾?");
                            return;
                        }
                        inputLayout.setError(null);
                        applyMacroGoal(metricType, newGoal);
                        syncCaloriesGoalToMacros();
                        homeViewModel.saveUserGoal(userGoal);
                        loadUiState();
                        Toast.makeText(requireContext(), title + " updated", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } catch (NumberFormatException e) {
                        inputLayout.setError("Please enter a valid number");
                    }
                }));

        dialog.show();
        dialog.show();
    }

    private String getMacroDialogTitle(MetricType metricType) {
        switch (metricType) {
            case CARB:
                return "Set carb goal";
            case PROTEIN:
                return "Set protein goal";
            case FAT:
                return "Set fat goal";
            default:
                return "Set goal";
        }
    }

    private String getMacroDialogHint(MetricType metricType) {
        switch (metricType) {
            case CARB:
                return "缂傚倸鍊搁崐鐑芥嚄閸洖纾婚柟鐐綑閸ㄦ棃鏌﹀Ο渚Т闁哄绉归弻锟犲炊閵夈儳浠肩紓浣哄У濠㈡﹢鈥﹂崸妤佸殝闂傚牊绋戦～宥夋⒑缁嬪潡顎楅悗娑掓櫊婵＄敻宕熼姘辩潉闂佺鏈粙鎺楁偟椤忓牊鈷?g)";
            case PROTEIN:
                return "闂傚倸鍊搁崐鐑芥嚄閸洖纾婚柟鎹愵嚙閽冪喖鏌曟繛鐐珦闁轰礁瀚伴弻娑㈠Ψ閹存柨浜鹃梺鍝勵儏缁夊爼骞夐幖浣哥睄闁割偅绻勯悾楣冩⒑閸涘﹥澶勯柛銊ャ偢瀵偅绻濋崶銊у幗闂佹寧绻傞幊鎾垛偓姘嵆閺?g)";
            case FAT:
                return "闂傚倸鍊搁崐椋庣矆娓氣偓瀹曘儳鈧綆浜堕悞鑺ョ箾閸℃ɑ灏痪鍓ф嚀闇夐柣妯烘▕閸庡繒鐥幆褜鐓奸柡宀€鍠愬蹇斻偅閸愨晩鈧秹姊虹粙鍧楊€楅悗娑掓櫊婵＄敻宕熼姘辩潉闂佺鏈粙鎺楁偟椤忓牊鈷?g)";
            default:
                return "闂傚倸鍊搁崐鐑芥嚄閸洖纾块柣銏㈩焾閻ら箖鏌嶉崫鍕櫣缂佹劖顨嗘穱濠囧Χ閸涱喖娅ら梺?g)";
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
        if (fatRatioData.saturated <= 0) {
            fatRatioTextView.setText("婵?闂?婵?= 0:0:0");
        } else {
            fatRatioTextView.setText(String.format(Locale.CHINA,
                    "婵?闂?婵?= 1:%s:%s",
                    ratioFormat.format(fatRatioData.monoRatioToSat),
                    ratioFormat.format(fatRatioData.polyRatioToSat)));
        }
    }

    private void setupCombinedChart(CombinedChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);
        chart.setDragEnabled(true);
        chart.setNoDataText("No data");

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(11f);
        legend.setTextColor(getResources().getColor(R.color.home_text_secondary));
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getResources().getColor(R.color.home_text_secondary));
        xAxis.setTextSize(10f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(getResources().getColor(R.color.home_text_secondary));
        leftAxis.setTextSize(10f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(getResources().getColor(R.color.home_divider));

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
            goals.add(new Entry(i, (float) goalValue));
            barColors.add(point.isFuture ? Color.parseColor("#D7D3E5") : actualColor);
        }

        BarDataSet barDataSet = new BarDataSet(bars, "Actual");
        barDataSet.setColors(barColors);
        barDataSet.setDrawValues(false);

        LineDataSet lineDataSet = new LineDataSet(goals, "Goal");
        lineDataSet.setColor(Color.parseColor("#171328"));
        lineDataSet.setLineWidth(1.8f);
        lineDataSet.setCircleRadius(2.6f);
        lineDataSet.setCircleColor(Color.parseColor("#171328"));
        lineDataSet.setDrawValues(false);

        CombinedData combinedData = new CombinedData();
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.55f);
        combinedData.setData(barData);
        combinedData.setData(new LineData(lineDataSet));

        chart.setData(combinedData);
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
        startActivity(HistoryActivity.createIntent(requireContext(), selectedDate.getTime()));
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
            return "闂傚倸鍊风粈渚€骞栭位鍥敃閿曗偓閻ょ偓绻濇繝鍌滃闁哄拋浜弻锝夋偄閸濄儳鐓佸┑鐘灪閿氭い鏇秮瀹曟粍鎷呴搹鍦姽闂備礁婀遍崕銈夈€冮崨顓熸殰闂傚倷鐒︾€笛兾涙担鑲濇盯宕熼姘辨煣閻熸粎澧楃敮妤呭煕?" + formatNumber(remainingCalories) + " kcal";
        }
        if (remainingCalories < 0) {
            return "闂傚倷娴囬褍顫濋敃鍌︾稏濠㈣埖鍔栭崑銈夋煛閸モ晛小闁绘帒锕ラ妵鍕疀閹炬惌妫ら梺娲诲幗閹瑰洭寮婚悢铏圭＜闁靛繒濮甸悘鍫㈢磽娴ｆ彃浜?" + formatNumber(Math.abs(remainingCalories)) + " kcal";
        }
        return "Goal reached for today";
    }

    private enum MetricType {
        CALORIES,
        CARB,
        PROTEIN,
        FAT
    }
}



