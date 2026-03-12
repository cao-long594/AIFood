package com.example.food.ui.water;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.example.food.R;

/**
 * 自定义圆形进度视图，用于显示喝水进度
 * 当用户输入喝水量时，圆形会从底部到顶部逐步被蓝色波浪填充
 */
public class WaterCircleProgressView extends View {
    // 视图大小相关变量
    private int radius;
    private PointF center = new PointF();
    private Path clipPath = new Path();

    // 进度相关变量
    private float maxProgress = 2000; // 默认最大2000ml
    private float currentProgress = 0;
    private float animatedProgress = 0; // 用于动画的进度值
    private boolean isAnimating = false;

    // 画笔相关变量
    private Paint backgroundPaint;
    private Paint wavePaint;
    private Paint borderPaint;
    private int borderWidth = 2;

    // 波浪效果相关变量
    private Path wavePath = new Path();
    private float wavePhase = 0f; // 波浪相位，用于动画
    private float waveAmplitude = 25f; // 波浪幅度
    private float waveFrequency = 0.025f; // 波浪频率
    private ValueAnimator waveAnimator;

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

    /**
     * 初始化画笔和自定义属性
     */
    private void init(@Nullable AttributeSet attrs) {
        // 默认颜色值
        int bgColor = Color.parseColor("#E0E0E0");
        int progressColor = Color.parseColor("#4A90E2");
        int borderColor = Color.parseColor("#BDBDBD");

        // 获取自定义属性
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WaterCircleProgressView);
            maxProgress = typedArray.getFloat(R.styleable.WaterCircleProgressView_maxProgress, 2000);
            bgColor = typedArray.getColor(R.styleable.WaterCircleProgressView_waterBgColor, bgColor);
            progressColor = typedArray.getColor(R.styleable.WaterCircleProgressView_waterProgressColor, progressColor);
            borderColor = typedArray.getColor(R.styleable.WaterCircleProgressView_borderColor, borderColor);
            borderWidth = (int) typedArray.getDimension(R.styleable.WaterCircleProgressView_borderWidth, 2);
            typedArray.recycle();
        }

        // 初始化背景画笔
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(bgColor);
        backgroundPaint.setStyle(Paint.Style.FILL);

        // 初始化波浪画笔
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(progressColor);
        wavePaint.setStyle(Paint.Style.FILL);

        // 初始化边框画笔
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);

        // 启动波浪动画
        startWaveAnimation();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 强制设置为正方形，取宽高中的最小值
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 计算圆心和半径
        center.x = getWidth() / 2f;
        center.y = getHeight() / 2f;
        radius = Math.min(getWidth(), getHeight()) / 2 - borderWidth;

        // 创建圆形裁剪路径
        clipPath.reset();
        clipPath.addCircle(center.x, center.y, radius, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. 绘制背景圆形
        canvas.drawCircle(center.x, center.y, radius, backgroundPaint);

        // 2. 绘制波浪填充区域（从底部到顶部）
        if (animatedProgress > 0) {
            updateWavePath();

            // 保存画布状态并裁剪为圆形
            canvas.save();
            canvas.clipPath(clipPath);
            canvas.drawPath(wavePath, wavePaint);
            canvas.restore();
        }

        // 3. 绘制边框
        canvas.drawCircle(center.x, center.y, radius, borderPaint);

        // 4. 如果正在动画，继续刷新
        if (isAnimating) {
            invalidate();
        }
    }

    /**
     * 更新波浪路径，实现从底部到顶部的波浪填充效果
     */
    private void updateWavePath() {
        wavePath.rewind();

        float progressRatio = Math.min(animatedProgress / maxProgress, 1.0f);

        // 计算填充高度（从底部开始）
        float fillHeight = 2 * radius * progressRatio;
        float baselineY = center.y + radius - fillHeight;

        // 波浪路径起点
        wavePath.moveTo(center.x - radius, baselineY);

        // 生成波浪路径
        float startX = center.x - radius;
        for (float x = startX; x <= center.x + radius; x++) {
            float y = baselineY + waveAmplitude *
                    (float) Math.sin(waveFrequency * (x - startX) + wavePhase);
            wavePath.lineTo(x, y);
        }

        // 闭合路径形成填充区域
        wavePath.lineTo(center.x + radius, center.y + radius);
        wavePath.lineTo(center.x - radius, center.y + radius);
        wavePath.close();
    }

    /**
     * 启动波浪动画
     */
    private void startWaveAnimation() {
        waveAnimator = ValueAnimator.ofFloat(0, (float) (2 * Math.PI));
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.setDuration(2000);
        waveAnimator.setInterpolator(new LinearInterpolator());
        waveAnimator.addUpdateListener(animation -> {
            wavePhase = (float) animation.getAnimatedValue();
            invalidate();
        });
        waveAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 视图从窗口分离时停止动画，防止内存泄漏
        if (waveAnimator != null) {
            waveAnimator.cancel();
        }
    }

    /**
     * 设置当前进度（当前喝水量ml），带动画
     * @param currentProgress 当前进度值
     */
    public void setCurrentProgress(float currentProgress) {
        // 限制进度在0到maxProgress之间
        this.currentProgress = Math.max(0f, Math.min(currentProgress, maxProgress));
        startProgressAnimation();
    }

    /**
     * 获取当前进度
     * @return 当前进度值
     */
    public float getCurrentProgress() {
        return currentProgress;
    }

    /**
     * 获取最大进度
     * @return 最大进度值
     */
    public float getMaxProgress() {
        return maxProgress;
    }
    
    /**
     * 设置最大进度（目标喝水量ml）
     * @param maxProgress 最大进度值
     */
    public void setMaxProgress(float maxProgress) {
        if (maxProgress > 0) {
            this.maxProgress = maxProgress;
            // 如果当前进度超过新的最大进度，调整当前进度
            if (currentProgress > maxProgress) {
                currentProgress = maxProgress;
            }
            invalidate();
        }
    }

    /**
     * 启动进度动画
     */
    private void startProgressAnimation() {
        isAnimating = true;
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "animatedProgress", animatedProgress, currentProgress);
        animator.setDuration(1000); // 动画持续时间1秒
        animator.addUpdateListener(animation -> {
            animatedProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {}

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {}

            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {}
        });
        animator.start();
    }

}