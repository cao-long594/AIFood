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

public class FatCompositionRingView extends View {

    private static final float DEFAULT_STROKE_DP = 18f;
    private static final float DEFAULT_GAP_ANGLE = 6f;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    private final int saturatedColor;
    private final int monoColor;
    private final int polyColor;
    private final int trackColor;
    private final int lineColor;
    private final int textColor;
    private final Bitmap centerIcon;

    private float strokeWidthPx;
    private float gapAngle = DEFAULT_GAP_ANGLE;
    private float saturated = 0f;
    private float mono = 0f;
    private float poly = 0f;

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
        lineColor = 0xFFA8B3BF;
        textColor = ContextCompat.getColor(context, R.color.home_text_primary);
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

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dpToPx(1.4f));
        linePaint.setColor(lineColor);

        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(lineColor);

        textPaint.setColor(textColor);
        textPaint.setTextSize(dpToPx(17f));
        textPaint.setFakeBoldText(true);
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
        float sideInset = strokeWidthPx / 2f + dpToPx(28f);
        float topInset = strokeWidthPx / 2f + dpToPx(22f);
        float bottomInset = strokeWidthPx / 2f + dpToPx(50f);

        float availableWidth = Math.max(0f, w - sideInset * 2f);
        float availableHeight = Math.max(0f, h - topInset - bottomInset);
        float diameter = Math.max(0f, Math.min(availableWidth, availableHeight));

        float left = (w - diameter) / 2f;
        float top = topInset;
        float right = left + diameter;
        float bottom = top + diameter;
        arcBounds.set(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(arcBounds, -90, 360, false, trackPaint);

        float total = saturated + mono + poly;
        if (total <= 0f) {
            drawCenterIcon(canvas);
            return;
        }

        int segmentCount = 0;
        if (saturated > 0f) segmentCount++;
        if (mono > 0f) segmentCount++;
        if (poly > 0f) segmentCount++;

        float totalGap = segmentCount > 1 ? gapAngle * segmentCount : 0f;
        float availableAngle = 360f - totalGap;
        float startAngle = -90f;

        if (saturated > 0f) {
            float sweep = availableAngle * (saturated / total);
            drawSegment(canvas, startAngle, sweep, saturatedColor);
            drawLabel(canvas, startAngle + sweep / 2f, "\u9971");
            startAngle += sweep + gapAngle;
        }
        if (mono > 0f) {
            float sweep = availableAngle * (mono / total);
            drawSegment(canvas, startAngle, sweep, monoColor);
            drawLabel(canvas, startAngle + sweep / 2f, "\u5355");
            startAngle += sweep + gapAngle;
        }
        if (poly > 0f) {
            float sweep = availableAngle * (poly / total);
            drawSegment(canvas, startAngle, sweep, polyColor);
            drawLabel(canvas, startAngle + sweep / 2f, "\u591A");
        }

        drawCenterIcon(canvas);
    }

    private void drawSegment(Canvas canvas, float startAngle, float sweepAngle, int color) {
        if (sweepAngle <= 0f) {
            return;
        }
        segmentPaint.setColor(color);
        canvas.drawArc(arcBounds, startAngle, sweepAngle, false, segmentPaint);
    }

    private void drawLabel(Canvas canvas, float angle, String label) {
        float radians = (float) Math.toRadians(angle);
        float centerX = arcBounds.centerX();
        float centerY = arcBounds.centerY();
        float radius = arcBounds.width() / 2f;

        float anchorRadius = radius + strokeWidthPx / 2f + dpToPx(6f);
        float elbowRadius = anchorRadius + dpToPx(14f);

        float startX = centerX + (float) Math.cos(radians) * anchorRadius;
        float startY = centerY + (float) Math.sin(radians) * anchorRadius;
        float elbowX = centerX + (float) Math.cos(radians) * elbowRadius;
        float elbowY = centerY + (float) Math.sin(radians) * elbowRadius;

        boolean toRight = elbowX >= centerX;
        float endX = toRight ? getWidth() - dpToPx(34f) : dpToPx(34f);
        float minY = dpToPx(28f);
        float maxY = getHeight() - dpToPx(30f);
        float endY = clamp(elbowY, minY, maxY);
        float elbowClampedY = clamp(elbowY, minY, maxY);

        canvas.drawLine(startX, startY, elbowX, elbowClampedY, linePaint);
        canvas.drawLine(elbowX, elbowClampedY, endX, endY, linePaint);
        canvas.drawCircle(startX, startY, dpToPx(3f), dotPaint);

        float textWidth = textPaint.measureText(label);
        float textX = toRight ? endX + dpToPx(6f) : endX - textWidth - dpToPx(6f);
        float textY = clamp(endY - dpToPx(6f), dpToPx(22f), getHeight() - dpToPx(12f));
        canvas.drawText(label, textX, textY, textPaint);
    }

    private void drawCenterIcon(Canvas canvas) {
        if (centerIcon == null) {
            return;
        }
        float maxIconSize = arcBounds.width() * 0.40f;
        float scale = maxIconSize / Math.max(centerIcon.getWidth(), centerIcon.getHeight());
        float drawWidth = centerIcon.getWidth() * scale;
        float drawHeight = centerIcon.getHeight() * scale;
        float left = arcBounds.centerX() - drawWidth / 2f;
        float top = arcBounds.centerY() - drawHeight / 2f;
        RectF dst = new RectF(left, top, left + drawWidth, top + drawHeight);
        canvas.drawBitmap(centerIcon, null, dst, null);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
