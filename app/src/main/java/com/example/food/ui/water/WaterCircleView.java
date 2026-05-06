package com.example.food.ui.water;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 饮水环形进度圈
 * 白色底圈 + 绿色弧形进度
 */
public class WaterCircleView extends View {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private float progress = 0f; // 0~1

    public WaterCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        bgPaint.setColor(0xFFFFFFFF); // 纯白底色
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint.setColor(0xFF00B75E); // 绿色进度
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setProgress(float progress) {
        this.progress = Math.min(1f, Math.max(0f, progress));
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int size = Math.min(w, h);
        float stroke = size * 0.12f;
        bgPaint.setStrokeWidth(stroke);
        progressPaint.setStrokeWidth(stroke);
        float half = stroke / 2f;
        arcRect.set(half, half, size - half, size - half);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = Math.min(getWidth(), getHeight());
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = size / 2f - bgPaint.getStrokeWidth() / 2f;

        // 白色底圈
        canvas.drawCircle(cx, cy, radius, bgPaint);

        // 绿色进度弧
        float sweep = progress * 360f;
        if (sweep > 0) {
            canvas.drawArc(arcRect, -90, sweep, false, progressPaint);
        }
    }
}
