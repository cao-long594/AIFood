package com.example.food.ui.history;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    public static final String EXTRA_ANCHOR_DATE_MILLIS = "extra_anchor_date_millis";
    public static final String EXTRA_INITIAL_VIEW = "extra_initial_view";

    private TextView titleTextView;
    private TextView rangeTextView;
    private TextView dayTabTextView;
    private TextView weekTabTextView;
    private TextView monthTabTextView;
    private TextView emptyStateTextView;
    private RecyclerView recordsRecyclerView;
    private CardView calorieChartCard;
    private CardView carbChartCard;
    private BarChart calorieChart;
    private BarChart carbChart;

    private HistoryRecordAdapter recordAdapter;
    private HistoryViewModel historyViewModel;

    private Date anchorDate;
    private HistoryViewModel.HistoryViewType currentViewType = HistoryViewModel.HistoryViewType.DAY;

    public static Intent createIntent(Context context, long anchorDateMillis) {
        Intent intent = new Intent(context, HistoryActivity.class);
        intent.putExtra(EXTRA_ANCHOR_DATE_MILLIS, anchorDateMillis);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyViewModel = new HistoryViewModel(this);
        anchorDate = historyViewModel.resolveAnchorDate(
                getIntent().getLongExtra(EXTRA_ANCHOR_DATE_MILLIS, System.currentTimeMillis())
        );

        String initialView = getIntent().getStringExtra(EXTRA_INITIAL_VIEW);
        if (initialView != null) {
            try {
                currentViewType = HistoryViewModel.HistoryViewType.valueOf(initialView);
            } catch (IllegalArgumentException ignored) {
                currentViewType = HistoryViewModel.HistoryViewType.DAY;
            }
        }

        initViews();
        bindActions();
        loadHistoryState();
    }

    private void initViews() {
        titleTextView = findViewById(R.id.tv_history_title);
        rangeTextView = findViewById(R.id.tv_history_range);
        dayTabTextView = findViewById(R.id.tv_history_tab_day);
        weekTabTextView = findViewById(R.id.tv_history_tab_week);
        monthTabTextView = findViewById(R.id.tv_history_tab_month);
        emptyStateTextView = findViewById(R.id.tv_history_empty_state);
        recordsRecyclerView = findViewById(R.id.rv_history_records);
        calorieChartCard = findViewById(R.id.card_history_chart_calories);
        carbChartCard = findViewById(R.id.card_history_chart_carbs);
        calorieChart = findViewById(R.id.chart_history_calories);
        carbChart = findViewById(R.id.chart_history_carbs);

        titleTextView.setText("历史数据");

        recordAdapter = new HistoryRecordAdapter();
        recordsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recordsRecyclerView.setAdapter(recordAdapter);

        setupBarChart(calorieChart);
        setupBarChart(carbChart);
    }

    private void bindActions() {
        findViewById(R.id.btn_history_back).setOnClickListener(v -> finish());
        dayTabTextView.setOnClickListener(v -> updateViewType(HistoryViewModel.HistoryViewType.DAY));
        weekTabTextView.setOnClickListener(v -> updateViewType(HistoryViewModel.HistoryViewType.WEEK));
        monthTabTextView.setOnClickListener(v -> updateViewType(HistoryViewModel.HistoryViewType.MONTH));
    }

    private void updateViewType(HistoryViewModel.HistoryViewType targetType) {
        if (currentViewType == targetType) {
            return;
        }
        currentViewType = targetType;
        loadHistoryState();
    }

    private void loadHistoryState() {
        historyViewModel.loadHistoryState(anchorDate, currentViewType, this::renderState);
    }

    private void renderState(@NonNull HistoryViewModel.HistoryUiState state) {
        rangeTextView.setText(state.displayTitle);
        updateTabStyles(state.viewType);

        boolean hasRecords = !state.records.isEmpty();
        boolean hasChartData = !state.calorieChartPoints.isEmpty() && !state.carbChartPoints.isEmpty();

        emptyStateTextView.setVisibility(hasRecords ? View.GONE : View.VISIBLE);
        emptyStateTextView.setText(state.emptyMessage);

        calorieChartCard.setVisibility(hasChartData ? View.VISIBLE : View.GONE);
        carbChartCard.setVisibility(hasChartData ? View.VISIBLE : View.GONE);
        recordsRecyclerView.setVisibility(hasRecords ? View.VISIBLE : View.GONE);

        recordAdapter.submitList(state.records);

        if (hasChartData) {
            renderChart(calorieChart, state.calorieChartPoints, "热量", getColor(R.color.primary_color));
            renderChart(carbChart, state.carbChartPoints, "碳水", getColor(R.color.carb_color));
        } else {
            calorieChart.clear();
            carbChart.clear();
        }
    }

    private void updateTabStyles(HistoryViewModel.HistoryViewType activeType) {
        updateSingleTab(dayTabTextView, activeType == HistoryViewModel.HistoryViewType.DAY);
        updateSingleTab(weekTabTextView, activeType == HistoryViewModel.HistoryViewType.WEEK);
        updateSingleTab(monthTabTextView, activeType == HistoryViewModel.HistoryViewType.MONTH);
    }

    private void updateSingleTab(TextView tabView, boolean selected) {
        tabView.setBackgroundResource(selected
                ? R.drawable.history_tab_selected_bg
                : R.drawable.history_tab_unselected_bg);
        tabView.setTextColor(getColor(selected ? R.color.home_text_primary : R.color.home_text_secondary));
    }

    private void setupBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDragEnabled(false);
        chart.setNoDataText("暂无数据");
        chart.setFitBars(true);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getColor(R.color.home_text_secondary));
        xAxis.setTextSize(10f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(getColor(R.color.home_divider));
        leftAxis.setTextColor(getColor(R.color.home_text_secondary));
        leftAxis.setTextSize(10f);

        chart.getAxisRight().setEnabled(false);
    }

    private void renderChart(BarChart chart,
                             List<HistoryViewModel.ChartPoint> points,
                             String label,
                             int color) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {
            HistoryViewModel.ChartPoint point = points.get(i);
            entries.add(new BarEntry(i, (float) point.value));
            labels.add(point.label);
        }

        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setDrawValues(false);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.58f);

        chart.setData(data);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setLabelCount(Math.min(labels.size(), 7), false);
        chart.getXAxis().setAxisMinimum(-0.5f);
        chart.getXAxis().setAxisMaximum(labels.size() - 0.5f);
        chart.invalidate();
    }
}
