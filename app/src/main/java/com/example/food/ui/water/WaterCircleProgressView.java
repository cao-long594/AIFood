package com.example.food.ui.water;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.example.food.R;

public class WaterCircleProgressView extends View {

    private final RectF arcBounds = new RectF();
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float maxProgress = 2000f;
    private float currentProgress = 0f;
    private float animatedProgress = 0f;
    private float strokeWidth;
    private int glowColor;
    private ValueAnimator progressAnimator;

    public WaterCircleProgressView(Context context) {
        super(context);
        init(null);
    }

    public WaterCircleProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WaterCircleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        int trackColor = Color.parseColor("#E9ECF6");
        int progressColor = Color.parseColor("#6B7CFF");
        glowColor = Color.parseColor("#2A6B7CFF");
        strokeWidth = dpToPx(18f);

        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WaterCircleProgressView);
            maxProgress = typedArray.getFloat(R.styleable.WaterCircleProgressView_maxProgress, maxProgress);
            trackColor = typedArray.getColor(R.styleable.WaterCircleProgressView_waterBgColor, trackColor);
            progressColor = typedArray.getColor(R.styleable.WaterCircleProgressView_waterProgressColor, progressColor);
            glowColor = typedArray.getColor(R.styleable.WaterCircleProgressView_borderColor, glowColor);
            strokeWidth = typedArray.getDimension(R.styleable.WaterCircleProgressView_borderWidth, strokeWidth);
            typedArray.recycle();
        }

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setColor(trackColor);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setColor(progressColor);

        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        glowPaint.setStrokeWidth(strokeWidth + dpToPx(6f));
        glowPaint.setColor(glowColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float inset = glowPaint.getStrokeWidth() / 2f + dpToPx(4f);
        arcBounds.set(inset, inset, w - inset, h - inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(arcBounds, -90f, 360f, false, trackPaint);

        float sweepAngle = maxProgress <= 0f ? 0f : Math.min(animatedProgress / maxProgress, 1f) * 360f;
        if (sweepAngle > 0f) {
            canvas.drawArc(arcBounds, -90f, sweepAngle, false, glowPaint);
            canvas.drawArc(arcBounds, -90f, sweepAngle, false, progressPaint);
        }
    }

    public void setCurrentProgress(float currentProgress) {
        this.currentProgress = Math.max(0f, Math.min(currentProgress, maxProgress));
        startProgressAnimation();
    }

    public float getCurrentProgress() {
        return currentProgress;
    }

    public float getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(float maxProgress) {
        if (maxProgress <= 0f) {
            return;
        }
        this.maxProgress = maxProgress;
        if (currentProgress > maxProgress) {
            currentProgress = maxProgress;
        }
        if (animatedProgress > maxProgress) {
            animatedProgress = maxProgress;
        }
        invalidate();
    }

    public float getAnimatedProgress() {
        return animatedProgress;
    }

    public void setAnimatedProgress(float animatedProgress) {
        this.animatedProgress = animatedProgress;
        invalidate();
    }

    private void startProgressAnimation() {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
        progressAnimator = ValueAnimator.ofFloat(animatedProgress, currentProgress);
        progressAnimator.setDuration(650L);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> setAnimatedProgress((float) animation.getAnimatedValue()));
        progressAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}