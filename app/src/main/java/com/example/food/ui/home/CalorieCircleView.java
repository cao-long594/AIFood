package com.example.food.ui.home;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import com.example.food.R;

public class CalorieCircleView extends View {

    // 默认值
    private static final int DEFAULT_MAX_CALORIES = 3300;
    private static final int DEFAULT_BG_COLOR = Color.parseColor("#394298");
    private static final int DEFAULT_PROGRESS_COLOR = Color.parseColor("#4CAF50");
    private static final int DEFAULT_TEXT_COLOR = Color.parseColor("#333333");
    private static final float DEFAULT_STROKE_WIDTH = 25f;
    private static final long DEFAULT_ANIMATION_DURATION = 1500;
    private static final float DEFAULT_GLOW_RADIUS = 8f;
    private static final int DEFAULT_GLOW_COLOR = Color.parseColor("#804CAF50");
    private static final float DEFAULT_FROSTED_ALPHA = 0.3f;
    private static final int DEFAULT_FROSTED_COLOR = Color.parseColor("#FFFFFF");
    private static final float DEFAULT_FROSTED_BLUR = 5f;

    // 绘制相关
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint glowPaint; // 发光效果画笔
    private Paint frostedPaint; // 毛玻璃填充画笔
    private Paint textPaint;
    private RectF arcRect;
    private RectF glowArcRect; // 发光圆环绘制区域

    // 属性
    private int maxCalories = DEFAULT_MAX_CALORIES;
    private int currentProgress = 0;
    private int animatedProgress = 0;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int progressColor = DEFAULT_PROGRESS_COLOR;
    private int textColor = DEFAULT_TEXT_COLOR;
    private float strokeWidth = DEFAULT_STROKE_WIDTH;

    // 发光效果属性
    private boolean isGlowEnabled = true;
    private float glowRadius = DEFAULT_GLOW_RADIUS;
    private int glowColor = DEFAULT_GLOW_COLOR;

    // 毛玻璃效果属性
    private boolean isFrostedEnabled = true;
    private float frostedAlpha = DEFAULT_FROSTED_ALPHA;
    private int frostedColor = DEFAULT_FROSTED_COLOR;
    private float frostedBlur = DEFAULT_FROSTED_BLUR;

    // 动画相关
    private ValueAnimator progressAnimator;
    private long animationDuration = DEFAULT_ANIMATION_DURATION;

    public CalorieCircleView(Context context) {
        super(context);
        init(null);
    }

    public CalorieCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CalorieCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // 从XML属性获取值
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CalorieCircleView);
            backgroundColor = typedArray.getColor(R.styleable.CalorieCircleView_calorieBgColor, DEFAULT_BG_COLOR);
            progressColor = typedArray.getColor(R.styleable.CalorieCircleView_calorieProgressColor, DEFAULT_PROGRESS_COLOR);
            textColor = typedArray.getColor(R.styleable.CalorieCircleView_textColor, DEFAULT_TEXT_COLOR);
            strokeWidth = typedArray.getDimension(R.styleable.CalorieCircleView_strokeWidth, DEFAULT_STROKE_WIDTH);
            maxCalories = typedArray.getInt(R.styleable.CalorieCircleView_maxCalories, DEFAULT_MAX_CALORIES);
            animationDuration = typedArray.getInt(R.styleable.CalorieCircleView_animationDuration, (int) DEFAULT_ANIMATION_DURATION);

            // 发光效果属性
            isGlowEnabled = typedArray.getBoolean(R.styleable.CalorieCircleView_glowEnabled, true);
            glowRadius = typedArray.getDimension(R.styleable.CalorieCircleView_glowRadius, DEFAULT_GLOW_RADIUS);
            glowColor = typedArray.getColor(R.styleable.CalorieCircleView_glowColor, DEFAULT_GLOW_COLOR);

            // 毛玻璃效果属性
            isFrostedEnabled = typedArray.getBoolean(R.styleable.CalorieCircleView_frostedEnabled, true);
            frostedAlpha = typedArray.getFloat(R.styleable.CalorieCircleView_frostedAlpha, DEFAULT_FROSTED_ALPHA);
            frostedColor = typedArray.getColor(R.styleable.CalorieCircleView_frostedColor, DEFAULT_FROSTED_COLOR);
            frostedBlur = typedArray.getDimension(R.styleable.CalorieCircleView_frostedBlur, DEFAULT_FROSTED_BLUR);

            typedArray.recycle();
        }

        // 初始化背景圆环画笔
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        // 初始化进度圆环画笔
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // 初始化发光效果画笔
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(strokeWidth + glowRadius / 4); // 缩小发光圆环宽度
        glowPaint.setColor(glowColor);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        // 设置模糊效果
        if (isGlowEnabled) {
            glowPaint.setMaskFilter(new BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.OUTER));
        }

        // 初始化毛玻璃填充画笔
        frostedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        frostedPaint.setStyle(Paint.Style.FILL);
        frostedPaint.setColor(frostedColor);
        frostedPaint.setAlpha((int) (frostedAlpha * 255));
        // 设置毛玻璃模糊效果
        if (isFrostedEnabled) {
            frostedPaint.setMaskFilter(new BlurMaskFilter(frostedBlur, BlurMaskFilter.Blur.INNER));
        }

        // 初始化文字画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        // 文字添加轻微阴影，提升可读性
        textPaint.setShadowLayer(2f, 0f, 1f, Color.parseColor("#40000000"));

        // 初始化绘制区域
        arcRect = new RectF();
        glowArcRect = new RectF();

        // 初始化动画
        setupAnimator();

        // 开启硬件加速，提升模糊效果性能
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    private void setupAnimator() {
        progressAnimator = ValueAnimator.ofInt(0, 0);
        progressAnimator.setDuration(animationDuration);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            animatedProgress = (int) animation.getAnimatedValue();
            invalidate();
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // 计算主圆环半径
        float radius = Math.min(centerX, centerY) - strokeWidth / 2 - glowRadius;
        // 计算发光圆环半径（比主圆环大）
        float glowRadius = radius + this.glowRadius / 2;

        // 设置主圆环绘制区域
        arcRect.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        // 设置发光圆环绘制区域
        glowArcRect.set(
                centerX - glowRadius,
                centerY - glowRadius,
                centerX + glowRadius,
                centerY + glowRadius
        );

        // 绘制毛玻璃填充（如果启用）
        if (isFrostedEnabled) {
            float frostedRadius = radius - strokeWidth / 2;
            canvas.drawCircle(centerX, centerY, frostedRadius, frostedPaint);
        }

        // 绘制背景圆环
        canvas.drawArc(arcRect, 0, 360, false, backgroundPaint);

        // 计算进度角度
        float progress = Math.min((float) animatedProgress / maxCalories, 1f);
        float sweepAngle = 360 * progress;

        // 绘制发光效果（如果启用）
        if (isGlowEnabled && sweepAngle > 0) {
            canvas.drawArc(glowArcRect, -90, sweepAngle, false, glowPaint);
        }

        // 绘制进度圆环
        if (sweepAngle > 0) {
            canvas.drawArc(arcRect, -90, sweepAngle, false, progressPaint);
        }

        // 绘制文字
        drawText(canvas, centerX, centerY, radius, progress);
    }

    private void drawText(Canvas canvas, int centerX, int centerY, float radius, float progress) {
        // 百分比文字大小
        float percentageTextSize = radius * 0.45f;
        textPaint.setTextSize(percentageTextSize);

        // 绘制百分比
        float percentage = progress * 100;
        canvas.drawText(String.format("%.0f%%", percentage), centerX, centerY + percentageTextSize * 0.25f, textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // 确保是正方形，预留发光效果的空间
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    /**
     * 设置当前进度（带动画效果）
     */
    public void setCurrentProgress(int progress) {
        setCurrentProgress(progress, true);
    }

    /**
     * 设置当前进度，可选择是否使用动画
     */
    public void setCurrentProgress(int progress, boolean withAnimation) {
        int targetProgress = Math.max(0, Math.min(progress, maxCalories)); // 限制在0-max之间

        if (withAnimation) {
            progressAnimator.cancel();
            progressAnimator.setIntValues(animatedProgress, targetProgress);
            progressAnimator.start();
        } else {
            this.animatedProgress = targetProgress;
            invalidate();
        }

        this.currentProgress = targetProgress;
    }

    /**
     * 设置当前进度，可自定义动画时长
     */
    public void setCurrentProgress(int progress, long duration) {
        int targetProgress = Math.max(0, Math.min(progress, maxCalories));

        progressAnimator.cancel();
        progressAnimator.setDuration(duration);
        progressAnimator.setIntValues(animatedProgress, targetProgress);
        progressAnimator.start();

        this.currentProgress = targetProgress;
    }

    // ------------------- 公开方法 -------------------

    // 设置最大热量
    public void setMaxCalories(int maxCalories) {
        this.maxCalories = Math.max(1, maxCalories);
        invalidate();
    }

    // 设置背景颜色
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }

    // 设置进度颜色（同时更新发光颜色）
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        // 自动设置发光颜色（进度色的半透明版本）
        this.glowColor = Color.argb(128, Color.red(color), Color.green(color), Color.blue(color));
        glowPaint.setColor(glowColor);
        invalidate();
    }

    // 设置文字颜色
    public void setTextColor(int color) {
        this.textColor = color;
        textPaint.setColor(color);
        invalidate();
    }

    // 设置圆环宽度
    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
        backgroundPaint.setStrokeWidth(width);
        progressPaint.setStrokeWidth(width);
        glowPaint.setStrokeWidth(width + glowRadius / 2);
        invalidate();
    }

    // 设置动画时长
    public void setAnimationDuration(long duration) {
        this.animationDuration = duration;
        progressAnimator.setDuration(duration);
    }

    // 启用/禁用发光效果
    public void setGlowEnabled(boolean enabled) {
        isGlowEnabled = enabled;
        glowPaint.setMaskFilter(enabled ? new BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.OUTER) : null);
        invalidate();
    }

    // 设置发光效果参数
    public void setGlowParams(float radius, int color) {
        this.glowRadius = radius;
        this.glowColor = color;
        glowPaint.setStrokeWidth(strokeWidth + radius / 4); // 缩小发光圆环宽度，保持光感
        glowPaint.setColor(color);
        glowPaint.setMaskFilter(new BlurMaskFilter(radius, BlurMaskFilter.Blur.OUTER));
        invalidate();
    }

    // 启用/禁用毛玻璃效果
    public void setFrostedEnabled(boolean enabled) {
        isFrostedEnabled = enabled;
        frostedPaint.setMaskFilter(enabled ? new BlurMaskFilter(frostedBlur, BlurMaskFilter.Blur.INNER) : null);
        invalidate();
    }

    // 设置毛玻璃效果参数
    public void setFrostedParams(float alpha, int color, float blur) {
        this.frostedAlpha = alpha;
        this.frostedColor = color;
        this.frostedBlur = blur;
        frostedPaint.setColor(color);
        frostedPaint.setAlpha((int) (alpha * 255));
        frostedPaint.setMaskFilter(new BlurMaskFilter(blur, BlurMaskFilter.Blur.INNER));
        invalidate();
    }

    // 获取当前进度百分比
    public float getProgressPercentage() {
        return Math.min((float) currentProgress / maxCalories, 1f);
    }

    // 获取剩余进度
    public int getRemainingProgress() {
        return Math.max(0, maxCalories - currentProgress);
    }

    // 获取动画中的当前进度值
    public int getAnimatedProgress() {
        return animatedProgress;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 释放资源
        if (progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator.removeAllUpdateListeners();
        }
        // 清除mask filter避免内存泄漏
        backgroundPaint.setMaskFilter(null);
        progressPaint.setMaskFilter(null);
        glowPaint.setMaskFilter(null);
        frostedPaint.setMaskFilter(null);
        textPaint.setMaskFilter(null);
    }

    public int getMaxCalories() {
        return maxCalories;
    }
}