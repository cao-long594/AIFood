package com.example.food.ui.home;

import android.content.Context;
import android.widget.TextView;

import com.example.food.R;
import com.example.food.utils.DateUtils;
import com.example.food.utils.DisplayUtils;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.Locale;

public class
PeriodChartMarkerView extends MarkerView {

    private final TextView valueTextView;
    private final TextView dateTextView;
    private final HomeViewModel.PeriodSeries periodSeries;
    private final float goalValue;

    public PeriodChartMarkerView(Context context,
                                 HomeViewModel.PeriodSeries periodSeries,
                                 float goalValue) {
        super(context, R.layout.view_chart_selection_marker);
        this.periodSeries = periodSeries;
        this.goalValue = goalValue;
        this.valueTextView = findViewById(R.id.tv_marker_value);
        this.dateTextView = findViewById(R.id.tv_marker_date);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int index = Math.round(e.getX());
        float actualValue = e.getY();
        valueTextView.setText(String.format(Locale.CHINA, "%.0f / %.0f", actualValue, goalValue));

        if (periodSeries != null && index >= 0 && index < periodSeries.points.size()) {
            dateTextView.setText(DateUtils.formatDate(periodSeries.points.get(index).date, "yyyy\u5e74M\u6708d\u65e5"));
        } else {
            dateTextView.setText("");
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
        float markerWidth = getWidth();
        float x = -(markerWidth / 2f);

        // 修复问题1：防止标记弹框超出图表左右边界被遮挡
        // 左侧边界：确保标记不超出图表左侧
        float leftEdge = DisplayUtils.dp2px(getContext(), 4f);
        if (posX + x < leftEdge) {
            x = leftEdge - posX;
        }
        // 右侧边界：确保标记不超出图表右侧
        else if (getChartView() != null) {
            float rightEdge = getChartView().getWidth() - DisplayUtils.dp2px(getContext(), 4f);
            if (posX + x + markerWidth > rightEdge) {
                x = rightEdge - posX - markerWidth;
            }
        }

        // 修复问题4：移除重复的私有 dpToPx()，改用 DisplayUtils.dp2px()
        float y = -posY + DisplayUtils.dp2px(getContext(), 6f);
        return new MPPointF(x, y);
    }
}
