package com.example.food.ui.history;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.data.repository.HistoryRepository;
import com.example.food.utils.DateUtils;
import com.example.food.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    public static final String EXTRA_ANCHOR_DATE_MILLIS = "extra_anchor_date_millis";
    public static final String EXTRA_INITIAL_VIEW = "extra_initial_view";
    public static final String RESULT_ANCHOR_DATE_MILLIS = "result_anchor_date_millis";
    public static final String RESULT_VIEW_TYPE = "result_view_type";

    private static final int MONTHS_BEFORE = 36;
    private static final int MONTHS_AFTER = 36;
    private static final int YEARS_BEFORE = 12;
    private static final int YEARS_AFTER = 12;

    private TextView titleTextView;
    private TextView rangeTextView;
    private TextView dayTabTextView;
    private TextView weekTabTextView;
    private TextView monthTabTextView;
    private RecyclerView contentRecyclerView;

    private HistoryRepository historyRepository;
    private Date selectedDate;
    private Date todayDate;
    private HistoryViewModel.HistoryViewType currentViewType;

    private LinearLayoutManager layoutManager;
    private MonthSectionAdapter dayAdapter;
    private MonthSectionAdapter weekAdapter;
    private YearSectionAdapter monthAdapter;

    // 修复问题3：使用单线程后台执行器，将重型计算移离主线程
    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor();

    public static Intent createIntent(Context context, long anchorDateMillis) {
        return createIntent(context, anchorDateMillis, HistoryViewModel.HistoryViewType.DAY.name());
    }

    public static Intent createIntent(Context context, long anchorDateMillis, String initialView) {
        Intent intent = new Intent(context, HistoryActivity.class);
        intent.putExtra(EXTRA_ANCHOR_DATE_MILLIS, anchorDateMillis);
        intent.putExtra(EXTRA_INITIAL_VIEW, initialView);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyRepository = new HistoryRepository();
        selectedDate = historyRepository.resolveAnchorDate(
                getIntent().getLongExtra(EXTRA_ANCHOR_DATE_MILLIS, System.currentTimeMillis())
        );
        todayDate = DateUtils.getTodayStart();
        currentViewType = historyRepository.resolveViewType(getIntent().getStringExtra(EXTRA_INITIAL_VIEW));

        initViews();
        bindActions();
        render(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 修复问题3：页面销毁时关闭后台线程池，避免内存泄漏
        bgExecutor.shutdown();
    }

    private void initViews() {
        titleTextView = findViewById(R.id.tv_history_title);
        rangeTextView = findViewById(R.id.tv_history_range);
        dayTabTextView = findViewById(R.id.tv_history_tab_day);
        weekTabTextView = findViewById(R.id.tv_history_tab_week);
        monthTabTextView = findViewById(R.id.tv_history_tab_month);
        contentRecyclerView = findViewById(R.id.rv_history_content);

        titleTextView.setText(getString(R.string.history_title));

        layoutManager = new LinearLayoutManager(this);
        contentRecyclerView.setLayoutManager(layoutManager);

        dayAdapter = new MonthSectionAdapter(this, HistoryViewModel.HistoryViewType.DAY, date -> {
            selectedDate = DateUtils.getDateStart(date);
            deliverSelection(selectedDate, HistoryViewModel.HistoryViewType.DAY);
        });

        weekAdapter = new MonthSectionAdapter(this, HistoryViewModel.HistoryViewType.WEEK, date -> {
            selectedDate = DateUtils.getDateStart(date);
            deliverSelection(selectedDate, HistoryViewModel.HistoryViewType.WEEK);
        });

        monthAdapter = new YearSectionAdapter(this, monthStart -> {
            selectedDate = DateUtils.getMonthStart(monthStart);
            deliverSelection(selectedDate, HistoryViewModel.HistoryViewType.MONTH);
        });
    }

    private void bindActions() {
        findViewById(R.id.btn_history_back).setOnClickListener(v -> finish());
        dayTabTextView.setOnClickListener(v -> switchViewType(HistoryViewModel.HistoryViewType.DAY));
        weekTabTextView.setOnClickListener(v -> switchViewType(HistoryViewModel.HistoryViewType.WEEK));
        monthTabTextView.setOnClickListener(v -> switchViewType(HistoryViewModel.HistoryViewType.MONTH));
    }

    private void switchViewType(HistoryViewModel.HistoryViewType targetType) {
        if (currentViewType == targetType) {
            return;
        }
        currentViewType = targetType;
        render(true);
    }

    /**
     * 修复问题3：将 buildMonthSections / buildYearSections 移至后台线程。
     * 这两个方法会构建 73 个月份 × 42 个 CalendarCell 共约 3000+ 对象，
     * 并伴有大量 Calendar 日期运算，在主线程执行会导致明显卡顿。
     * 计算完成后通过 runOnUiThread 将结果回传主线程更新适配器。
     */
    private void render(boolean scrollToSelection) {
        rangeTextView.setText(historyRepository.buildSelectionSummary(selectedDate, currentViewType));
        updateTabStyles();

        // 捕获当前状态快照传入后台线程，避免线程间状态竞争
        final HistoryViewModel.HistoryViewType type = currentViewType;
        final Date date = selectedDate;
        final Date today = todayDate;

        bgExecutor.execute(() -> {
            if (type == HistoryViewModel.HistoryViewType.DAY) {
                final HistoryViewModel.MonthSectionsResult result = historyRepository.buildMonthSections(
                        date, today, HistoryViewModel.HistoryViewType.DAY, MONTHS_BEFORE, MONTHS_AFTER);
                runOnUiThread(() -> {
                    dayAdapter.submit(result.sections);
                    setAdapterIfNeeded(dayAdapter);
                    if (scrollToSelection) scrollToSection(result.selectedSectionIndex);
                });
            } else if (type == HistoryViewModel.HistoryViewType.WEEK) {
                final HistoryViewModel.MonthSectionsResult result = historyRepository.buildMonthSections(
                        date, today, HistoryViewModel.HistoryViewType.WEEK, MONTHS_BEFORE, MONTHS_AFTER);
                runOnUiThread(() -> {
                    weekAdapter.submit(result.sections);
                    setAdapterIfNeeded(weekAdapter);
                    if (scrollToSelection) scrollToSection(result.selectedSectionIndex);
                });
            } else {
                final HistoryViewModel.YearSectionsResult result = historyRepository.buildYearSections(
                        date, today, YEARS_BEFORE, YEARS_AFTER);
                runOnUiThread(() -> {
                    monthAdapter.submit(result.sections);
                    setAdapterIfNeeded(monthAdapter);
                    if (scrollToSelection) scrollToSection(result.selectedSectionIndex);
                });
            }
        });
    }

    private void updateTabStyles() {
        updateSingleTab(dayTabTextView, currentViewType == HistoryViewModel.HistoryViewType.DAY);
        updateSingleTab(weekTabTextView, currentViewType == HistoryViewModel.HistoryViewType.WEEK);
        updateSingleTab(monthTabTextView, currentViewType == HistoryViewModel.HistoryViewType.MONTH);
    }

    private void updateSingleTab(TextView tabView, boolean selected) {
        tabView.setBackgroundResource(selected ? R.drawable.history_tab_selected_bg : R.drawable.history_tab_unselected_bg);
        tabView.setTextColor(getColor(selected ? R.color.home_text_primary : R.color.home_text_secondary));
    }

    private void setAdapterIfNeeded(RecyclerView.Adapter<?> adapter) {
        if (contentRecyclerView.getAdapter() != adapter) {
            contentRecyclerView.setAdapter(adapter);
        }
    }

    private void scrollToSection(int position) {
        // 修复问题4：移除重复的私有 dpToPx()，改用 DisplayUtils.dp2px()
        contentRecyclerView.post(() -> layoutManager.scrollToPositionWithOffset(
                Math.max(0, position), DisplayUtils.dp2px(this, 8)));
    }

    private void deliverSelection(Date resultDate, HistoryViewModel.HistoryViewType viewType) {
        Intent result = new Intent();
        result.putExtra(RESULT_ANCHOR_DATE_MILLIS, resultDate.getTime());
        result.putExtra(RESULT_VIEW_TYPE, viewType.name());
        setResult(RESULT_OK, result);
        finish();
    }

    private interface OnDateClickListener {
        void onDateClick(Date date);
    }

    private interface OnMonthClickListener {
        void onMonthClick(Date monthStart);
    }

    private static class MonthSectionAdapter extends RecyclerView.Adapter<MonthSectionAdapter.MonthSectionViewHolder> {

        private final Context context;
        private final HistoryViewModel.HistoryViewType mode;
        private final OnDateClickListener onDateClickListener;
        private final List<HistoryViewModel.MonthSection> items = new ArrayList<>();

        MonthSectionAdapter(Context context,
                            HistoryViewModel.HistoryViewType mode,
                            OnDateClickListener onDateClickListener) {
            this.context = context;
            this.mode = mode;
            this.onDateClickListener = onDateClickListener;
        }

        void submit(List<HistoryViewModel.MonthSection> newItems) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return items.size();
                }

                @Override
                public int getNewListSize() {
                    return newItems.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return items.get(oldItemPosition).monthStart.getTime()
                            == newItems.get(newItemPosition).monthStart.getTime();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return isMonthSectionContentSame(items.get(oldItemPosition), newItems.get(newItemPosition));
                }
            });

            items.clear();
            items.addAll(newItems);
            diffResult.dispatchUpdatesTo(this);
        }

        private boolean isMonthSectionContentSame(HistoryViewModel.MonthSection oldSection,
                                                  HistoryViewModel.MonthSection newSection) {
            if (!oldSection.title.equals(newSection.title) || oldSection.cells.size() != newSection.cells.size()) {
                return false;
            }

            for (int i = 0; i < oldSection.cells.size(); i++) {
                HistoryViewModel.CalendarCell oldCell = oldSection.cells.get(i);
                HistoryViewModel.CalendarCell newCell = newSection.cells.get(i);
                if (oldCell.dayOfMonth != newCell.dayOfMonth
                        || oldCell.inCurrentMonth != newCell.inCurrentMonth
                        || oldCell.today != newCell.today
                        || oldCell.selectedDate != newCell.selectedDate
                        || oldCell.inCurrentWeek != newCell.inCurrentWeek
                        || oldCell.inSelectedWeek != newCell.inSelectedWeek
                        || oldCell.past != newCell.past
                        || oldCell.future != newCell.future) {
                    return false;
                }
            }
            return true;
        }

        @NonNull
        @Override
        public MonthSectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_month_section, parent, false);
            return new MonthSectionViewHolder(view, context, mode, onDateClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull MonthSectionViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class MonthSectionViewHolder extends RecyclerView.ViewHolder {
            private final Context context;
            private final HistoryViewModel.HistoryViewType mode;
            private final OnDateClickListener onDateClickListener;
            private final TextView monthTitleTextView;
            private final GridLayout gridLayout;
            private final WeekBandOverlayView weekOverlayView;
            private final List<DayCellHolder> dayCells = new ArrayList<>(42);

            MonthSectionViewHolder(@NonNull View itemView,
                                   Context context,
                                   HistoryViewModel.HistoryViewType mode,
                                   OnDateClickListener onDateClickListener) {
                super(itemView);
                this.context = context;
                this.mode = mode;
                this.onDateClickListener = onDateClickListener;
                monthTitleTextView = itemView.findViewById(R.id.tv_section_month_title);
                gridLayout = itemView.findViewById(R.id.grid_section_dates);
                weekOverlayView = itemView.findViewById(R.id.view_week_overlay);
                initDateCells();
            }

            private void initDateCells() {
                gridLayout.removeAllViews();
                for (int i = 0; i < 42; i++) {
                    View cellView = LayoutInflater.from(context).inflate(R.layout.item_history_day_cell, gridLayout, false);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                            GridLayout.spec(i / 7, 1f),
                            GridLayout.spec(i % 7, 1f)
                    );
                    params.width = 0;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    // 修复问题4：移除重复的私有 dpToPx()，改用 DisplayUtils.dp2px()
                    int margin = DisplayUtils.dp2px(context, 2);
                    params.setMargins(margin, margin, margin, margin);
                    gridLayout.addView(cellView, params);
                    dayCells.add(new DayCellHolder(cellView));
                }
            }

            void bind(HistoryViewModel.MonthSection section) {
                monthTitleTextView.setText(section.title);
                for (int i = 0; i < section.cells.size() && i < dayCells.size(); i++) {
                    DayCellHolder holder = dayCells.get(i);
                    HistoryViewModel.CalendarCell cell = section.cells.get(i);
                    holder.bind(context, mode, cell);
                    holder.root.setEnabled(cell.inCurrentMonth);
                    if (cell.inCurrentMonth) {
                        holder.root.setOnClickListener(v -> onDateClickListener.onDateClick(cell.date));
                    } else {
                        holder.root.setOnClickListener(null);
                    }
                }
                updateWeekOverlay(section);
            }

            private void updateWeekOverlay(HistoryViewModel.MonthSection section) {
                if (mode != HistoryViewModel.HistoryViewType.WEEK) {
                    weekOverlayView.setSegments(new ArrayList<>());
                    return;
                }

                gridLayout.post(() -> weekOverlayView.setSegments(buildWeekBandSegments(section.cells)));
            }

            private List<WeekBandOverlayView.BandSegment> buildWeekBandSegments(List<HistoryViewModel.CalendarCell> cells) {
                List<WeekBandOverlayView.BandSegment> segments = new ArrayList<>();
                for (int row = 0; row < 6; row++) {
                    int rowStart = row * 7;
                    Range currentRange = findRange(cells, rowStart, true);
                    Range selectedRange = findRange(cells, rowStart, false);

                    if (currentRange != null && selectedRange != null
                            && currentRange.start == selectedRange.start
                            && currentRange.end == selectedRange.end) {
                        WeekBandOverlayView.BandSegment segment = createBandSegment(rowStart, currentRange, true, true);
                        if (segment != null) {
                            segments.add(segment);
                        }
                        continue;
                    }

                    if (currentRange != null) {
                        WeekBandOverlayView.BandSegment current = createBandSegment(rowStart, currentRange, true, false);
                        if (current != null) {
                            segments.add(current);
                        }
                    }
                    if (selectedRange != null) {
                        WeekBandOverlayView.BandSegment selected = createBandSegment(rowStart, selectedRange, false, true);
                        if (selected != null) {
                            segments.add(selected);
                        }
                    }
                }
                return segments;
            }

            private Range findRange(List<HistoryViewModel.CalendarCell> cells, int rowStart, boolean current) {
                int first = -1;
                int last = -1;
                for (int col = 0; col < 7; col++) {
                    HistoryViewModel.CalendarCell cell = cells.get(rowStart + col);
                    boolean matches = cell.inCurrentMonth && (current ? cell.inCurrentWeek : cell.inSelectedWeek);
                    if (matches) {
                        if (first < 0) {
                            first = col;
                        }
                        last = col;
                    }
                }
                if (first < 0) {
                    return null;
                }
                return new Range(first, last);
            }

            private WeekBandOverlayView.BandSegment createBandSegment(int rowStart,
                                                                      Range range,
                                                                      boolean drawCurrentFill,
                                                                      boolean drawSelectedStroke) {
                View startView = gridLayout.getChildAt(rowStart + range.start);
                View endView = gridLayout.getChildAt(rowStart + range.end);
                if (startView == null || endView == null) {
                    return null;
                }

                float centerY = (startView.getTop() + startView.getBottom()) / 2f;
                // 修复问题4：移除重复的私有 dpToPx()，改用 DisplayUtils.dp2px()
                float halfHeight = DisplayUtils.dp2px(context, 12);
                RectF rect = new RectF(
                        startView.getLeft(),
                        centerY - halfHeight,
                        endView.getRight(),
                        centerY + halfHeight
                );
                return new WeekBandOverlayView.BandSegment(rect, drawCurrentFill, drawSelectedStroke);
            }

            static class Range {
                final int start;
                final int end;

                Range(int start, int end) {
                    this.start = start;
                    this.end = end;
                }
            }
        }

        static class DayCellHolder {
            private final View root;
            private final View weekBand;
            private final TextView dayTextView;
            private final View todayDot;

            DayCellHolder(View root) {
                this.root = root;
                this.weekBand = root.findViewById(R.id.view_week_band);
                this.dayTextView = root.findViewById(R.id.tv_day_number);
                this.todayDot = root.findViewById(R.id.view_today_dot);
            }

            void bind(Context context,
                      HistoryViewModel.HistoryViewType mode,
                      HistoryViewModel.CalendarCell cell) {
                dayTextView.setText(cell.inCurrentMonth ? String.valueOf(cell.dayOfMonth) : "");
                weekBand.setVisibility(View.GONE);

                if (mode == HistoryViewModel.HistoryViewType.WEEK) {
                    dayTextView.setBackgroundResource(android.R.color.transparent);
                    if (!cell.inCurrentMonth) {
                        dayTextView.setTextColor(context.getColor(R.color.history_future_text));
                        dayTextView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                    } else if (cell.today) {
                        dayTextView.setTextColor(context.getColor(R.color.primary_color));
                        dayTextView.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
                    } else {
                        dayTextView.setTextColor(resolveDayTextColor(context, cell));
                        dayTextView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                    }
                } else if (!cell.inCurrentMonth) {
                    dayTextView.setBackgroundResource(android.R.color.transparent);
                    dayTextView.setTextColor(context.getColor(R.color.history_future_text));
                    dayTextView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                } else if (cell.today) {
                    dayTextView.setBackgroundResource(R.drawable.history_day_today_bg);
                    dayTextView.setTextColor(context.getColor(android.R.color.white));
                    dayTextView.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
                } else if (cell.selectedDate) {
                    dayTextView.setBackgroundResource(R.drawable.history_day_selected_bg);
                    dayTextView.setTextColor(resolveDayTextColor(context, cell));
                    dayTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                } else {
                    dayTextView.setBackgroundResource(android.R.color.transparent);
                    dayTextView.setTextColor(resolveDayTextColor(context, cell));
                    dayTextView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                }

                todayDot.setVisibility(View.GONE);
            }

            private int resolveDayTextColor(Context context, HistoryViewModel.CalendarCell cell) {
                if (cell.future) {
                    return context.getColor(R.color.history_future_text);
                }
                return context.getColor(android.R.color.black);
            }
        }
    }

    private static class YearSectionAdapter extends RecyclerView.Adapter<YearSectionAdapter.YearSectionViewHolder> {

        private final Context context;
        private final OnMonthClickListener onMonthClickListener;
        private final List<HistoryViewModel.YearSection> items = new ArrayList<>();

        YearSectionAdapter(Context context, OnMonthClickListener onMonthClickListener) {
            this.context = context;
            this.onMonthClickListener = onMonthClickListener;
        }

        void submit(List<HistoryViewModel.YearSection> newItems) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return items.size();
                }

                @Override
                public int getNewListSize() {
                    return newItems.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return items.get(oldItemPosition).year == newItems.get(newItemPosition).year;
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return isYearSectionContentSame(items.get(oldItemPosition), newItems.get(newItemPosition));
                }
            });

            items.clear();
            items.addAll(newItems);
            diffResult.dispatchUpdatesTo(this);
        }

        private boolean isYearSectionContentSame(HistoryViewModel.YearSection oldSection,
                                                 HistoryViewModel.YearSection newSection) {
            if (oldSection.year != newSection.year || oldSection.months.size() != newSection.months.size()) {
                return false;
            }

            for (int i = 0; i < oldSection.months.size(); i++) {
                HistoryViewModel.MonthCell oldCell = oldSection.months.get(i);
                HistoryViewModel.MonthCell newCell = newSection.months.get(i);
                if (!oldCell.label.equals(newCell.label)
                        || oldCell.selected != newCell.selected
                        || oldCell.currentMonth != newCell.currentMonth
                        || oldCell.past != newCell.past
                        || oldCell.future != newCell.future
                        || oldCell.monthStart.getTime() != newCell.monthStart.getTime()) {
                    return false;
                }
            }
            return true;
        }

        @NonNull
        @Override
        public YearSectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_year_section, parent, false);
            return new YearSectionViewHolder(view, context, onMonthClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull YearSectionViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class YearSectionViewHolder extends RecyclerView.ViewHolder {
            private final Context context;
            private final OnMonthClickListener onMonthClickListener;
            private final TextView yearTitleTextView;
            private final GridLayout monthGridLayout;
            private final List<MonthCellViewHolder> monthCellViews = new ArrayList<>(12);

            YearSectionViewHolder(@NonNull View itemView,
                                  Context context,
                                  OnMonthClickListener onMonthClickListener) {
                super(itemView);
                this.context = context;
                this.onMonthClickListener = onMonthClickListener;
                yearTitleTextView = itemView.findViewById(R.id.tv_year_title);
                monthGridLayout = itemView.findViewById(R.id.grid_year_months);
                initMonthCells();
            }

            private void initMonthCells() {
                monthGridLayout.removeAllViews();
                for (int i = 0; i < 12; i++) {
                    View monthCellView = LayoutInflater.from(context)
                            .inflate(R.layout.item_history_month_cell, monthGridLayout, false);
                    TextView monthTextView = monthCellView.findViewById(R.id.tv_month_label);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                            GridLayout.spec(i / 3, 1f),
                            GridLayout.spec(i % 3, 1f)
                    );
                    params.width = 0;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    // 修复问题4：移除重复的私有 dpToPx()，改用 DisplayUtils.dp2px()
                    int horizontalMargin = DisplayUtils.dp2px(context, 6);
                    int verticalMargin = DisplayUtils.dp2px(context, 8);
                    params.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
                    monthGridLayout.addView(monthCellView, params);
                    monthCellViews.add(new MonthCellViewHolder(monthCellView, monthTextView));
                }
            }

            void bind(HistoryViewModel.YearSection section) {
                yearTitleTextView.setText(section.year + "\u5e74");
                for (int i = 0; i < section.months.size() && i < monthCellViews.size(); i++) {
                    MonthCellViewHolder holder = monthCellViews.get(i);
                    TextView monthTextView = holder.labelTextView;
                    HistoryViewModel.MonthCell monthCell = section.months.get(i);
                    monthTextView.setText(monthCell.label);

                    if (monthCell.currentMonth) {
                        monthTextView.setBackgroundResource(R.drawable.history_month_current_circle_bg);
                        monthTextView.setTextColor(context.getColor(android.R.color.white));
                        monthTextView.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
                    } else if (monthCell.selected) {
                        monthTextView.setBackgroundResource(R.drawable.history_month_selected_circle_bg);
                        monthTextView.setTextColor(resolveMonthTextColor(context, monthCell));
                        monthTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    } else {
                        monthTextView.setBackgroundResource(android.R.color.transparent);
                        monthTextView.setTextColor(resolveMonthTextColor(context, monthCell));
                        monthTextView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                    }

                    holder.rootView.setOnClickListener(v -> onMonthClickListener.onMonthClick(monthCell.monthStart));
                }
            }

            private int resolveMonthTextColor(Context context, HistoryViewModel.MonthCell monthCell) {
                if (monthCell.future) {
                    return context.getColor(R.color.history_future_text);
                }
                return context.getColor(android.R.color.black);
            }

            static class MonthCellViewHolder {
                final View rootView;
                final TextView labelTextView;

                MonthCellViewHolder(View rootView, TextView labelTextView) {
                    this.rootView = rootView;
                    this.labelTextView = labelTextView;
                }
            }
        }
    }
}