package com.example.food.ui.history;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.food.R;
import com.example.food.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

public class WeekBandOverlayView extends View {

    public static class BandSegment {
        public final RectF rect;
        public final boolean drawCurrentFill;
        public final boolean drawSelectedStroke;

        public BandSegment(RectF rect, boolean drawCurrentFill, boolean drawSelectedStroke) {
            this.rect = rect;
            this.drawCurrentFill = drawCurrentFill;
            this.drawSelectedStroke = drawSelectedStroke;
        }
    }

    private final List<BandSegment> segments = new ArrayList<>();
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float cornerRadiusPx;

    public WeekBandOverlayView(Context context) {
        super(context);
        init();
    }

    public WeekBandOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeekBandOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(getContext().getColor(R.color.history_week_current_fill));

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(getContext().getColor(android.R.color.black));
        // 修复问题4：移除重复的私有 dpToPx()，改用 DisplayUtils.dp2px()
        strokePaint.setStrokeWidth(DisplayUtils.dp2px(getContext(), 1.5f));

        cornerRadiusPx = DisplayUtils.dp2px(getContext(), 12f);
    }

    public void setSegments(List<BandSegment> newSegments) {
        segments.clear();
        if (newSegments != null) {
            segments.addAll(newSegments);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (BandSegment segment : segments) {
            if (segment.drawCurrentFill) {
                canvas.drawRoundRect(segment.rect, cornerRadiusPx, cornerRadiusPx, fillPaint);
            }
            if (segment.drawSelectedStroke) {
                canvas.drawRoundRect(segment.rect, cornerRadiusPx, cornerRadiusPx, strokePaint);
            }
        }
    }
}