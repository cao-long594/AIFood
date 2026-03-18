package com.example.food.ui.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.food.R;

import java.util.Locale;

public class FatCompositionRingView extends View {

    private static final float DEFAULT_STROKE_DP = 24f;
    private static final float DEFAULT_GAP_ANGLE = 4f;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    private final int saturatedColor;
    private final int monoColor;
    private final int polyColor;
    private final int trackColor;
    private final Bitmap centerIcon;

    private float strokeWidthPx;
    private float gapAngle = DEFAULT_GAP_ANGLE;
    private float saturated = 0f;
    private float mono = 0f;
    private float poly = 0f;
    private float legendStartX;

    public FatCompositionRingView(Context context) {
        this(context, null);
    }

    public FatCompositionRingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FatCompositionRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        saturatedColor = ContextCompat.getColor(context, R.color.saturated_fat);
        monoColor = ContextCompat.getColor(context, R.color.mono_unsaturated_fat);
        polyColor = ContextCompat.getColor(context, R.color.poly_unsaturated_fat);
        trackColor = ContextCompat.getColor(context, R.color.home_ring_track);
        strokeWidthPx = dpToPx(DEFAULT_STROKE_DP);
        centerIcon = BitmapFactory.decodeResource(getResources(), R.drawable.meal_ic_fat);
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

        centerOuterPaint.setColor(0xFFF7E9CD);
        centerInnerPaint.setColor(0xFFFFC25C);

        markerPaint.setStyle(Paint.Style.FILL);

        titlePaint.setColor(ContextCompat.getColor(getContext(), R.color.home_text_primary));
        titlePaint.setTextSize(dpToPx(14f));
        titlePaint.setFakeBoldText(true);

        subtitlePaint.setColor(ContextCompat.getColor(getContext(), R.color.home_text_secondary));
        subtitlePaint.setTextSize(dpToPx(12f));

        valuePaint.setTextSize(dpToPx(17f));
        valuePaint.setFakeBoldText(true);
    }

    public void setComposition(double saturated, double mono, double poly) {
        this.saturated = Math.max(0f, (float) saturated);
        this.mono = Math.max(0f, (float) mono);
        this.poly = Math.max(0f, (float) poly);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float horizontalInset = dpToPx(8f);
        float verticalInset = dpToPx(14f);
        float ringDiameter = Math.min(h - verticalInset * 2f, w * 0.43f);

        float left = horizontalInset + strokeWidthPx / 2f;
        float top = (h - ringDiameter) / 2f;
        arcBounds.set(left, top, left + ringDiameter - strokeWidthPx, top + ringDiameter - strokeWidthPx);
        legendStartX = arcBounds.right + dpToPx(24f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(arcBounds, -90, 360, false, trackPaint);

        float total = saturated + mono + poly;
        if (total > 0f) {
            drawSegment(canvas, -90f, 360f * (saturated / total), saturatedColor);
            float startMono = -90f + 360f * (saturated / total) + gapAngle;
            drawSegment(canvas, startMono, 360f * (mono / total) - gapAngle, monoColor);
            float startPoly = startMono + 360f * (mono / total);
            drawSegment(canvas, startPoly + gapAngle, 360f * (poly / total) - gapAngle, polyColor);
        }

        drawCenterIcon(canvas);
        drawLegend(canvas, total);
    }

    private void drawLegend(Canvas canvas, float total) {
        float top = dpToPx(42f);
        float rowHeight = dpToPx(58f);

        drawLegendRow(canvas, top,
                saturatedColor,
                "饱和脂肪酸",
                "建议 <10% 总热量",
                saturated,
                total);

        drawLegendRow(canvas, top + rowHeight,
                monoColor,
                "单不饱和脂肪酸",
                "橄榄油・坚果",
                mono,
                total);

        drawLegendRow(canvas, top + rowHeight * 2f,
                polyColor,
                "多不饱和脂肪酸",
                "Omega-3 ・ Omega-6",
                poly,
                total);
    }

    private void drawLegendRow(Canvas canvas,
                               float y,
                               int color,
                               String title,
                               String subtitle,
                               float grams,
                               float total) {
        markerPaint.setColor(color);
        float markerSize = dpToPx(10f);
        RectF markerRect = new RectF(legendStartX, y - markerSize, legendStartX + markerSize, y);
        canvas.drawRoundRect(markerRect, markerSize / 2f, markerSize / 2f, markerPaint);

        float textX = legendStartX + dpToPx(16f);
        canvas.drawText(title, textX, y - dpToPx(1f), titlePaint);
        canvas.drawText(subtitle, textX, y + dpToPx(20f), subtitlePaint);

        int percent = total <= 0f ? 0 : Math.round((grams / total) * 100f);
        String percentText = String.format(Locale.CHINA, "%d%%", percent);
        String gramText = String.format(Locale.CHINA, "%.1fg", grams);

        valuePaint.setColor(color);
        float right = getWidth() - dpToPx(8f);
        float percentWidth = valuePaint.measureText(percentText);
        canvas.drawText(percentText, right - percentWidth, y - dpToPx(1f), valuePaint);

        float gramWidth = subtitlePaint.measureText(gramText);
        canvas.drawText(gramText, right - gramWidth, y + dpToPx(20f), subtitlePaint);
    }

    private void drawSegment(Canvas canvas, float startAngle, float sweepAngle, int color) {
        if (sweepAngle <= 0f) {
            return;
        }
        segmentPaint.setColor(color);
        canvas.drawArc(arcBounds, startAngle, sweepAngle, false, segmentPaint);
    }

    private void drawCenterIcon(Canvas canvas) {
        float centerX = arcBounds.centerX();
        float centerY = arcBounds.centerY();
        float outerRadius = arcBounds.width() * 0.17f;
        float innerRadius = outerRadius * 0.55f;

        canvas.drawCircle(centerX, centerY, outerRadius, centerOuterPaint);
        canvas.drawCircle(centerX, centerY, innerRadius, centerInnerPaint);

        if (centerIcon == null) {
            return;
        }
        float iconSize = innerRadius * 1.2f;
        RectF dst = new RectF(
                centerX - iconSize / 2f,
                centerY - iconSize / 2f,
                centerX + iconSize / 2f,
                centerY + iconSize / 2f
        );
        canvas.drawBitmap(centerIcon, null, dst, null);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
