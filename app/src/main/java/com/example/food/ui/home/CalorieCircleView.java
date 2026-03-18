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

import java.util.Locale;

public class CalorieCircleView extends View {

    // 榛樿鍊?
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

    // 缁樺埗鐩稿叧
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint glowPaint; // 鍙戝厜鏁堟灉鐢荤瑪
    private Paint frostedPaint; // 姣涚幓鐠冨～鍏呯敾绗?
    private Paint textPaint;
    private RectF arcRect;
    private RectF glowArcRect; // 鍙戝厜鍦嗙幆缁樺埗鍖哄煙

    // 灞炴€?
    private int maxCalories = DEFAULT_MAX_CALORIES;
    private int currentProgress = 0;
    private int animatedProgress = 0;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int progressColor = DEFAULT_PROGRESS_COLOR;
    private int textColor = DEFAULT_TEXT_COLOR;
    private float strokeWidth = DEFAULT_STROKE_WIDTH;

    // 鍙戝厜鏁堟灉灞炴€?
    private boolean isGlowEnabled = true;
    private float glowRadius = DEFAULT_GLOW_RADIUS;
    private int glowColor = DEFAULT_GLOW_COLOR;

    // 姣涚幓鐠冩晥鏋滃睘鎬?
    private boolean isFrostedEnabled = true;
    private float frostedAlpha = DEFAULT_FROSTED_ALPHA;
    private int frostedColor = DEFAULT_FROSTED_COLOR;
    private float frostedBlur = DEFAULT_FROSTED_BLUR;

    // 鍔ㄧ敾鐩稿叧
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
        // 浠嶺ML灞炴€ц幏鍙栧€?
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CalorieCircleView);
            backgroundColor = typedArray.getColor(R.styleable.CalorieCircleView_calorieBgColor, DEFAULT_BG_COLOR);
            progressColor = typedArray.getColor(R.styleable.CalorieCircleView_calorieProgressColor, DEFAULT_PROGRESS_COLOR);
            textColor = typedArray.getColor(R.styleable.CalorieCircleView_textColor, DEFAULT_TEXT_COLOR);
            strokeWidth = typedArray.getDimension(R.styleable.CalorieCircleView_strokeWidth, DEFAULT_STROKE_WIDTH);
            maxCalories = typedArray.getInt(R.styleable.CalorieCircleView_maxCalories, DEFAULT_MAX_CALORIES);
            animationDuration = typedArray.getInt(R.styleable.CalorieCircleView_animationDuration, (int) DEFAULT_ANIMATION_DURATION);

            // 鍙戝厜鏁堟灉灞炴€?
            isGlowEnabled = typedArray.getBoolean(R.styleable.CalorieCircleView_glowEnabled, true);
            glowRadius = typedArray.getDimension(R.styleable.CalorieCircleView_glowRadius, DEFAULT_GLOW_RADIUS);
            glowColor = typedArray.getColor(R.styleable.CalorieCircleView_glowColor, DEFAULT_GLOW_COLOR);

            // 姣涚幓鐠冩晥鏋滃睘鎬?
            isFrostedEnabled = typedArray.getBoolean(R.styleable.CalorieCircleView_frostedEnabled, true);
            frostedAlpha = typedArray.getFloat(R.styleable.CalorieCircleView_frostedAlpha, DEFAULT_FROSTED_ALPHA);
            frostedColor = typedArray.getColor(R.styleable.CalorieCircleView_frostedColor, DEFAULT_FROSTED_COLOR);
            frostedBlur = typedArray.getDimension(R.styleable.CalorieCircleView_frostedBlur, DEFAULT_FROSTED_BLUR);

            typedArray.recycle();
        }

        // 鍒濆鍖栬儗鏅渾鐜敾绗?
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        // 鍒濆鍖栬繘搴﹀渾鐜敾绗?
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // 鍒濆鍖栧彂鍏夋晥鏋滅敾绗?
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(strokeWidth + glowRadius / 4); // 缂╁皬鍙戝厜鍦嗙幆瀹藉害
        glowPaint.setColor(glowColor);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        // 璁剧疆妯＄硦鏁堟灉
        if (isGlowEnabled) {
            glowPaint.setMaskFilter(new BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.OUTER));
        }

        // 鍒濆鍖栨瘺鐜荤拑濉厖鐢荤瑪
        frostedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        frostedPaint.setStyle(Paint.Style.FILL);
        frostedPaint.setColor(frostedColor);
        frostedPaint.setAlpha((int) (frostedAlpha * 255));
        // 璁剧疆姣涚幓鐠冩ā绯婃晥鏋?
        if (isFrostedEnabled) {
            frostedPaint.setMaskFilter(new BlurMaskFilter(frostedBlur, BlurMaskFilter.Blur.INNER));
        }

        // 鍒濆鍖栨枃瀛楃敾绗?
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        // 鏂囧瓧娣诲姞杞诲井闃村奖锛屾彁鍗囧彲璇绘€?
        textPaint.setShadowLayer(2f, 0f, 1f, Color.parseColor("#40000000"));

        // 鍒濆鍖栫粯鍒跺尯鍩?
        arcRect = new RectF();
        glowArcRect = new RectF();

        // 鍒濆鍖栧姩鐢?
        setupAnimator();

        // 寮€鍚‖浠跺姞閫燂紝鎻愬崌妯＄硦鏁堟灉鎬ц兘
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

        // 璁＄畻涓诲渾鐜崐寰?
        float radius = Math.min(centerX, centerY) - strokeWidth / 2 - glowRadius;
        // 璁＄畻鍙戝厜鍦嗙幆鍗婂緞锛堟瘮涓诲渾鐜ぇ锛?
        float glowRadius = radius + this.glowRadius / 2;

        // 璁剧疆涓诲渾鐜粯鍒跺尯鍩?
        arcRect.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        // 璁剧疆鍙戝厜鍦嗙幆缁樺埗鍖哄煙
        glowArcRect.set(
                centerX - glowRadius,
                centerY - glowRadius,
                centerX + glowRadius,
                centerY + glowRadius
        );

        // 缁樺埗姣涚幓鐠冨～鍏咃紙濡傛灉鍚敤锛?
        if (isFrostedEnabled) {
            float frostedRadius = radius - strokeWidth / 2;
            canvas.drawCircle(centerX, centerY, frostedRadius, frostedPaint);
        }

        // 缁樺埗鑳屾櫙鍦嗙幆
        canvas.drawArc(arcRect, 0, 360, false, backgroundPaint);

        // 璁＄畻杩涘害瑙掑害
        float progress = Math.min((float) animatedProgress / maxCalories, 1f);
        float sweepAngle = 360 * progress;

        // 缁樺埗鍙戝厜鏁堟灉锛堝鏋滃惎鐢級
        if (isGlowEnabled && sweepAngle > 0) {
            canvas.drawArc(glowArcRect, -90, sweepAngle, false, glowPaint);
        }

        // 缁樺埗杩涘害鍦嗙幆
        if (sweepAngle > 0) {
            canvas.drawArc(arcRect, -90, sweepAngle, false, progressPaint);
        }

        // 缁樺埗鏂囧瓧
        drawText(canvas, centerX, centerY, radius, progress);
    }

    private void drawText(Canvas canvas, int centerX, int centerY, float radius, float progress) {
        // 鐧惧垎姣旀枃瀛楀ぇ灏?
        float percentageTextSize = radius * 0.45f;
        textPaint.setTextSize(percentageTextSize);

        // 缁樺埗鐧惧垎姣?
        float percentage = progress * 100;
        canvas.drawText(String.format(Locale.CHINA, "%.0f%%", percentage), centerX, centerY + percentageTextSize * 0.25f, textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // 纭繚鏄鏂瑰舰锛岄鐣欏彂鍏夋晥鏋滅殑绌洪棿
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    /**
     * 璁剧疆褰撳墠杩涘害锛堝甫鍔ㄧ敾鏁堟灉锛?
     */
    public void setCurrentProgress(int progress) {
        setCurrentProgress(progress, true);
    }

    /**
     * 璁剧疆褰撳墠杩涘害锛屽彲閫夋嫨鏄惁浣跨敤鍔ㄧ敾
     */
    public void setCurrentProgress(int progress, boolean withAnimation) {
        int targetProgress = Math.max(0, Math.min(progress, maxCalories)); // 闄愬埗鍦?-max涔嬮棿

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
     * 璁剧疆褰撳墠杩涘害锛屽彲鑷畾涔夊姩鐢绘椂闀?
     */
    public void setCurrentProgress(int progress, long duration) {
        int targetProgress = Math.max(0, Math.min(progress, maxCalories));

        progressAnimator.cancel();
        progressAnimator.setDuration(duration);
        progressAnimator.setIntValues(animatedProgress, targetProgress);
        progressAnimator.start();

        this.currentProgress = targetProgress;
    }

    // ------------------- 鍏紑鏂规硶 -------------------

    // 璁剧疆鏈€澶х儹閲?
    public void setMaxCalories(int maxCalories) {
        this.maxCalories = Math.max(1, maxCalories);
        invalidate();
    }

    // 璁剧疆鑳屾櫙棰滆壊
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }

    // 璁剧疆杩涘害棰滆壊锛堝悓鏃舵洿鏂板彂鍏夐鑹诧級
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        // 鑷姩璁剧疆鍙戝厜棰滆壊锛堣繘搴﹁壊鐨勫崐閫忔槑鐗堟湰锛?
        this.glowColor = Color.argb(128, Color.red(color), Color.green(color), Color.blue(color));
        glowPaint.setColor(glowColor);
        invalidate();
    }

    // 璁剧疆鏂囧瓧棰滆壊
    public void setTextColor(int color) {
        this.textColor = color;
        textPaint.setColor(color);
        invalidate();
    }

    // 璁剧疆鍦嗙幆瀹藉害
    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
        backgroundPaint.setStrokeWidth(width);
        progressPaint.setStrokeWidth(width);
        glowPaint.setStrokeWidth(width + glowRadius / 2);
        invalidate();
    }

    // 璁剧疆鍔ㄧ敾鏃堕暱
    public void setAnimationDuration(long duration) {
        this.animationDuration = duration;
        progressAnimator.setDuration(duration);
    }

    // 鍚敤/绂佺敤鍙戝厜鏁堟灉
    public void setGlowEnabled(boolean enabled) {
        isGlowEnabled = enabled;
        glowPaint.setMaskFilter(enabled ? new BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.OUTER) : null);
        invalidate();
    }

    // 璁剧疆鍙戝厜鏁堟灉鍙傛暟
    public void setGlowParams(float radius, int color) {
        this.glowRadius = radius;
        this.glowColor = color;
        glowPaint.setStrokeWidth(strokeWidth + radius / 4); // 缂╁皬鍙戝厜鍦嗙幆瀹藉害锛屼繚鎸佸厜鎰?
        glowPaint.setColor(color);
        glowPaint.setMaskFilter(new BlurMaskFilter(radius, BlurMaskFilter.Blur.OUTER));
        invalidate();
    }

    // 鍚敤/绂佺敤姣涚幓鐠冩晥鏋?
    public void setFrostedEnabled(boolean enabled) {
        isFrostedEnabled = enabled;
        frostedPaint.setMaskFilter(enabled ? new BlurMaskFilter(frostedBlur, BlurMaskFilter.Blur.INNER) : null);
        invalidate();
    }

    // 璁剧疆姣涚幓鐠冩晥鏋滃弬鏁?
    public void setFrostedParams(float alpha, int color, float blur) {
        this.frostedAlpha = alpha;
        this.frostedColor = color;
        this.frostedBlur = blur;
        frostedPaint.setColor(color);
        frostedPaint.setAlpha((int) (alpha * 255));
        frostedPaint.setMaskFilter(new BlurMaskFilter(blur, BlurMaskFilter.Blur.INNER));
        invalidate();
    }

    // 鑾峰彇褰撳墠杩涘害鐧惧垎姣?
    public float getProgressPercentage() {
        return Math.min((float) currentProgress / maxCalories, 1f);
    }

    // 鑾峰彇鍓╀綑杩涘害
    public int getRemainingProgress() {
        return Math.max(0, maxCalories - currentProgress);
    }

    // 鑾峰彇鍔ㄧ敾涓殑褰撳墠杩涘害鍊?
    public int getAnimatedProgress() {
        return animatedProgress;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 閲婃斁璧勬簮
        if (progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator.removeAllUpdateListeners();
        }
        // 娓呴櫎mask filter閬垮厤鍐呭瓨娉勬紡
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
