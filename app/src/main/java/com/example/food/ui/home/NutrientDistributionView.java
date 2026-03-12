package com.example.food.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.food.R;

public class NutrientDistributionView extends View {

    private static final float DEFAULT_STROKE_DP = 18f;
    private static final float DEFAULT_GAP_ANGLE = 4f;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    private final int proteinColor;
    private final int carbColor;
    private final int fatColor;
    private final int trackColor;

    private float strokeWidthPx;
    private float gapAngle = DEFAULT_GAP_ANGLE;
    private float carb = 0f;
    private float protein = 0f;
    private float fat = 0f;

    public NutrientDistributionView(Context context) {
        this(context, null);
    }

    public NutrientDistributionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NutrientDistributionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        proteinColor = ContextCompat.getColor(context, R.color.protein_color);
        carbColor = ContextCompat.getColor(context, R.color.carb_color);
        fatColor = ContextCompat.getColor(context, R.color.fat_color);
        trackColor = ContextCompat.getColor(context, R.color.home_ring_track);
        strokeWidthPx = dpToPx(DEFAULT_STROKE_DP);
        initPaints();
    }

    private void initPaints() {
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeWidth(strokeWidthPx);
        trackPaint.setColor(trackColor);

        segmentPaint.setStyle(Paint.Style.STROKE);
        segmentPaint.setStrokeCap(Paint.Cap.ROUND);
        segmentPaint.setStrokeWidth(strokeWidthPx);
    }

    public void setDistribution(double carb, double protein, double fat) {
        this.carb = Math.max(0f, (float) carb);
        this.protein = Math.max(0f, (float) protein);
        this.fat = Math.max(0f, (float) fat);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float size = Math.min(w, h);
        float inset = strokeWidthPx / 2f + dpToPx(10);
        float left = (w - size) / 2f + inset;
        float top = (h - size) / 2f + inset;
        float right = (w + size) / 2f - inset;
        float bottom = (h + size) / 2f - inset;
        arcBounds.set(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(arcBounds, -90, 360, false, trackPaint);

        float total = carb + protein + fat;
        if (total <= 0f) {
            return;
        }

        int segmentCount = 0;
        if (carb > 0f) segmentCount++;
        if (protein > 0f) segmentCount++;
        if (fat > 0f) segmentCount++;

        float totalGap = segmentCount > 1 ? gapAngle * (segmentCount - 1) : 0f;
        float availableAngle = 360f - totalGap;
        float startAngle = -90f;

        if (carb > 0f) {
            float sweep = availableAngle * (carb / total);
            drawSegment(canvas, startAngle, sweep, carbColor);
            startAngle += sweep + gapAngle;
        }
        if (protein > 0f) {
            float sweep = availableAngle * (protein / total);
            drawSegment(canvas, startAngle, sweep, proteinColor);
            startAngle += sweep + gapAngle;
        }
        if (fat > 0f) {
            float sweep = availableAngle * (fat / total);
            drawSegment(canvas, startAngle, sweep, fatColor);
        }
    }

    private void drawSegment(Canvas canvas, float startAngle, float sweepAngle, int color) {
        if (sweepAngle <= 0f) {
            return;
        }
        segmentPaint.setColor(color);
        canvas.drawArc(arcBounds, startAngle, sweepAngle, false, segmentPaint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}

