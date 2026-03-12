package com.example.food.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FatCircleView extends View {

    // 三种脂肪的颜色与标识文字
    private static final int COLOR_SATURATED = 0xFFF83E04;    // 饱和脂肪-红色
    private static final int COLOR_MONO = 0xFFA9DB1B;         // 单不饱和脂肪-绿色
    private static final int COLOR_POLY = 0xFF124faa;         // 多不饱和脂肪-蓝色
    private static final int COLOR_LABEL_TEXT = 0xFF333333;   // 标识文字颜色
    private static final String LABEL_SATURATED = "饱和";
    private static final String LABEL_MONO = "单";
    private static final String LABEL_POLY = "多";

    // 画笔：环形、中心文字、标识文字、延伸线
    private Paint ringPaint;
    private Paint centerTextPaint;
    private Paint labelTextPaint;
    private Paint linePaint;
    private Shader lineShader; // 渐变着色器

    // 数据与布局参数
    private float saturatedFat = 0f;
    private float monoFat = 0f;
    private float polyFat = 0f;
    private float totalFat = 0f;
    private List<RingSegment> segments = new ArrayList<>();
    private RectF ringRectF;
    private float ringRadius = 200f;    // 环形外半径
    private float ringWidth = 30f;      // 环形宽度
    private float centerX;
    private float centerY;
    private float labelOffset = 60f;    // 延伸线的延伸距离（增加长度，从40f调整为60f）

    public FatCircleView(Context context) {
        super(context);
        init();
    }

    public FatCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FatCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化画笔
     */
    private void init() {
        // 环形画笔
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(ringWidth);
        
        // 中心文字画笔（显示"总占比"等）
        centerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerTextPaint.setColor(Color.BLACK);
        centerTextPaint.setTextSize(40f);
        centerTextPaint.setTextAlign(Paint.Align.CENTER);
        
        // 标识文字画笔
        labelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelTextPaint.setColor(COLOR_LABEL_TEXT);
        labelTextPaint.setTextSize(40f);
        labelTextPaint.setTextAlign(Paint.Align.LEFT); // 文字左对齐（便于延伸线右侧显示）
        
        // 延伸线画笔 - 基本设置，具体颜色和渐变在绘制时设置
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeCap(Paint.Cap.ROUND); // 预设圆角端点，使线条看起来更平滑
        
        ringRectF = new RectF();
        segments = new ArrayList<>(3); // 预分配空间，提高性能
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 确保视图尺寸有效
        if (w > 0 && h > 0) {
            centerX = w / 2f;
            centerY = h / 2f - 40f;
            
            // 自适应环形半径，确保环形完全显示在视图内
            float availableSize = Math.min(w, h) * 0.7f; // 留出30%的边距
            ringRadius = Math.min(ringRadius, availableSize / 2f - ringWidth);
            
            ringRectF.left = centerX - ringRadius;
            ringRectF.top = centerY - ringRadius;
            ringRectF.right = centerX + ringRadius;
            ringRectF.bottom = centerY + ringRadius;
        }
    }

    /**
     * 设置脂肪数据，触发重绘
     */
    public void setFatData(float saturated, float mono, float poly) {
        // 检查并过滤非法值（负数）
        this.saturatedFat = Math.max(0f, saturated);
        this.monoFat = Math.max(0f, mono);
        this.polyFat = Math.max(0f, poly);
        this.totalFat = this.saturatedFat + this.monoFat + this.polyFat;
        buildSegments();
        invalidate();
    }

    /**
     * 环形分段数据模型（含延伸线坐标）
     */
    private static class RingSegment {
        float angle;
        int color;
        String label;
        float startX, startY; // 延伸线起点（环形边缘）
        float midX, midY;     // 延伸线中点（45度转折处）
        float endX, endY;     // 延伸线终点（文字位置）

        public RingSegment(float angle, int color, String label,
                           float startX, float startY, float midX, float midY, float endX, float endY) {
            this.angle = angle;
            this.color = color;
            this.label = label;
            this.startX = startX;
            this.startY = startY;
            this.midX = midX;
            this.midY = midY;
            this.endX = endX;
            this.endY = endY;
        }
    }

    /**
     * 构建环形分段数据（含角度、颜色、标识文字、延伸线坐标）
     */
    private void buildSegments() {
        segments.clear();
        if (totalFat <= 0) {
            // 当没有数据时，不显示任何分段，避免绘制无效内容
            return;
        }

        // 计算各分段的角度、颜色、标识文字
        float startAngle = -90f; // 从顶部开始（0度为右侧，-90度为顶部）

        // 添加间隔后的总可用角度
        final float segmentGap = 2f;
        int validSegmentCount = 0;

        // 计算有效分段数量
        if (saturatedFat > 0) validSegmentCount++;
        if (monoFat > 0) validSegmentCount++;
        if (polyFat > 0) validSegmentCount++;

        // 计算扣除间隔后的可用角度
        float availableAngle = 360f;
        if (validSegmentCount > 1) {
            availableAngle -= segmentGap * (validSegmentCount - 1);
        }

        // 饱和脂肪分段
        if (saturatedFat > 0) {
            float saturatedAngle = (saturatedFat / totalFat) * availableAngle;
            segments.add(calcSegment(startAngle, saturatedAngle, COLOR_SATURATED, LABEL_SATURATED));
            startAngle += saturatedAngle + segmentGap;
        }

        // 单不饱和脂肪分段
        if (monoFat > 0) {
            float monoAngle = (monoFat / totalFat) * availableAngle;
            segments.add(calcSegment(startAngle, monoAngle, COLOR_MONO, LABEL_MONO));
            startAngle += monoAngle + segmentGap;
        }

        // 多不饱和脂肪分段
        if (polyFat > 0) {
            float polyAngle = (polyFat / totalFat) * availableAngle;
            segments.add(calcSegment(startAngle, polyAngle, COLOR_POLY, LABEL_POLY));
        }
    }

    /**
     * 计算单个环形分段的延伸线坐标
     */
    private RingSegment calcSegment(float startAngle, float angle, int color, String label) {
        if (angle <= 0 || label == null) {
            return new RingSegment(0f, color, label, 0, 0, 0, 0, 0, 0);
        }

        // 计算延伸线的起点（环形边缘的中点）
        float midAngle = startAngle + angle / 2;
        float startX = (float) (centerX + Math.cos(Math.toRadians(midAngle)) * ringRadius);
        float startY = (float) (centerY + Math.sin(Math.toRadians(midAngle)) * ringRadius);

        // 计算45度方向短延展线的终点（转折点）
        float shortExtension = 40f; // 短延展线长度
        float midX, midY;
        if (midAngle > -90 && midAngle < 90) {
            // 右半部分：向右上方或右下方45度
            if (startY < centerY) {
                // 上半部分：向右上方45度
                midX = startX + shortExtension * (float)Math.cos(Math.toRadians(45));
                midY = startY - shortExtension * (float)Math.sin(Math.toRadians(45));
            } else {
                // 下半部分：向右下方45度
                midX = startX + shortExtension * (float)Math.cos(Math.toRadians(45));
                midY = startY + shortExtension * (float)Math.sin(Math.toRadians(45));
            }
        } else {
            // 左半部分：向左上方或左下方45度
            if (startY < centerY) {
                // 上半部分：向左上方45度
                midX = startX - shortExtension * (float)Math.cos(Math.toRadians(45));
                midY = startY - shortExtension * (float)Math.sin(Math.toRadians(45));
            } else {
                // 下半部分：向左下方45度
                midX = startX - shortExtension * (float)Math.cos(Math.toRadians(45));
                midY = startY + shortExtension * (float)Math.sin(Math.toRadians(45));
            }
        }

        // 计算最终的延伸线终点（水平方向）
        float longExtension = 120f; // 长延展线长度
        float endX, endY;
        if (midAngle > -90 && midAngle < 90) {
            // 右半部分：水平向右
            endX = midX + longExtension;
            endY = midY;
        } else {
            // 左半部分：水平向左
            endX = midX - longExtension;
            endY = midY;
        }

        return new RingSegment(angle, color, label, startX, startY, midX, midY, endX, endY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 确保canvas不为null
        if (canvas == null) return;

        drawRing(canvas);      // 绘制环形
        drawCenterText(canvas); // 绘制中心文字
        drawLabelsAndLines(canvas); // 绘制延伸线和标识文字
    }

    /**
     * 绘制环形（保证第一个与最后一个圆弧之间也有间隔）
     */
    private void drawRing(Canvas canvas) {
        if (segments.isEmpty()) return;

        float currentStartAngle = -90f;
        final float segmentGap = 2f; // 每个间隔的角度（可调整）
        List<RingSegment> validSegments = new ArrayList<>();

        // 第一步：筛选有效圆弧（角度>0），避免无效分段影响计算
        for (RingSegment segment : segments) {
            if (segment.angle > 0) {
                validSegments.add(segment);
            }
        }
        if (validSegments.isEmpty()) return;

        int segmentCount = validSegments.size(); // 有效圆弧数量（如3个）
        float gapTotal = segmentCount * segmentGap; // 所有间隔的总角度（3个间隔→3×2=6°）
        float arcTotal = 0f; // 所有有效圆弧的总角度

        // 计算所有有效圆弧的总角度
        for (RingSegment segment : validSegments) {
            arcTotal += segment.angle;
        }

        // 第二步：处理总角度溢出（圆弧+间隔不能超过360°，否则会重叠）
        float totalRequired = arcTotal + gapTotal;
        float scale = 1f;
        if (totalRequired > 360f) {
            // 按比例缩小圆弧角度和间隔（保证总角度=360°）
            scale = 360f / totalRequired;
        }
        float scaledGap = segmentGap * scale; // 缩放后的单个间隔角度

        // 第三步：绘制（每个圆弧后都跟一个间隔，闭环处理）
        for (RingSegment segment : validSegments) {
            // 缩放后的圆弧角度（避免总角度溢出）
            float scaledArcAngle = segment.angle * scale;

            // 绘制当前圆弧
            ringPaint.setColor(segment.color);
            canvas.drawArc(ringRectF, currentStartAngle, scaledArcAngle, false, ringPaint);

            // 关键：圆弧绘制完成后，跳过一个间隔（包括最后一个圆弧）
            currentStartAngle += scaledArcAngle + scaledGap;
        }
    }
    /**
     * 绘制中心文字
     */
    private void drawCenterText(Canvas canvas) {
        String text = "脂肪比例";
        Paint.FontMetrics metrics = centerTextPaint.getFontMetrics();
        if (metrics != null) {
            float baseline = centerY - (metrics.top + metrics.bottom) / 2;
            canvas.drawText(text, centerX, baseline, centerTextPaint);
        } else {
            // 降级方案：如果无法获取FontMetrics，则直接绘制
            canvas.drawText(text, centerX, centerY, centerTextPaint);
        }
    }

    /**
     * 绘制延伸线和标识文字
     */
    private void drawLabelsAndLines(Canvas canvas) {
        if (segments.isEmpty()) return;
        
        // 小圆点画笔（使用线段颜色，但设置为填充样式）
        Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStrokeCap(Paint.Cap.ROUND);
        
        for (RingSegment segment : segments) {
            if (segment.angle <= 0 || segment.label == null) continue;
            
            // 设置当前分段的颜色
            dotPaint.setColor(segment.color);
            
            // 优化小圆点绘制 - 调整小圆点位置，使其与圆弧边缘有一定距离
            dotPaint.setStyle(Paint.Style.FILL); // 填充模式
            // 添加阴影（x偏移，y偏移，模糊半径，阴影颜色）
            dotPaint.setShadowLayer(2f, 0f, 1f, Color.argb(50, 0, 0, 0));
            linePaint.setStrokeWidth(3f); // 线条宽度（根据UI调整）
            float dotRadius = linePaint.getStrokeWidth() * 1.2f; // 小圆点半径=线条宽度的1.2倍（更协调）
            
            // 计算小圆点与圆弧边缘的距离（从圆弧边缘向外偏移一定距离）
            float dotOffset = 30f; // 小圆点偏移距离，增大以显示更明显的距离
            
            // 计算从圆心到小圆点的方向向量
            float dx = segment.startX - centerX;
            float dy = segment.startY - centerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            // 计算向外偏移后的小圆点位置
            float dotX = segment.startX + (dx / distance) * dotOffset;
            float dotY = segment.startY + (dy / distance) * dotOffset;
            
            // 核心：设置线性渐变（从偏移后的小圆点位置到文字位置）
            lineShader = new LinearGradient(
                dotX, dotY,                    // 渐变起点（偏移后的小圆点位置）
                segment.endX, segment.endY,    // 渐变终点（文字位置）
                segment.color,                 // 起始颜色（与圆弧一致）
                Color.argb(100, Color.red(segment.color), Color.green(segment.color), Color.blue(segment.color)), // 终点颜色（半透明）
                Shader.TileMode.CLAMP
            );
            linePaint.setShader(lineShader);
            linePaint.setStrokeCap(Paint.Cap.ROUND); // 线条两端圆角（关键）
            
            canvas.drawCircle(dotX, dotY, dotRadius, dotPaint);
            
            // 添加白色描边，与填充色形成对比，更突出
            dotPaint.setStyle(Paint.Style.STROKE);
            dotPaint.setStrokeWidth(1f);
            dotPaint.setColor(Color.WHITE); // 白色描边
            dotPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT); // 移除描边的阴影
            canvas.drawCircle(dotX, dotY, dotRadius + 0.5f, dotPaint);
            
            // 重新计算从偏移后的小圆点开始的45度短延展线
            float shortExtension = 40f; // 短延展线长度
            float newMidX, newMidY;
            
            // 计算方向：从圆心到小圆点的角度
            float angle = (float) Math.toDegrees(Math.atan2(dotY - centerY, dotX - centerX));
            
            if (angle > -90 && angle < 90) {
                // 右半部分：向右上方或右下方45度
                if (dotY < centerY) {
                    // 上半部分：向右上方45度
                    newMidX = dotX + shortExtension * (float)Math.cos(Math.toRadians(45));
                    newMidY = dotY - shortExtension * (float)Math.sin(Math.toRadians(45));
                } else {
                    // 下半部分：向右下方45度
                    newMidX = dotX + shortExtension * (float)Math.cos(Math.toRadians(45));
                    newMidY = dotY + shortExtension * (float)Math.sin(Math.toRadians(45));
                }
            } else {
                // 左半部分：向左上方或左下方45度
                if (dotY < centerY) {
                    // 上半部分：向左上方45度
                    newMidX = dotX - shortExtension * (float)Math.cos(Math.toRadians(45));
                    newMidY = dotY - shortExtension * (float)Math.sin(Math.toRadians(45));
                } else {
                    // 下半部分：向左下方45度
                    newMidX = dotX - shortExtension * (float)Math.cos(Math.toRadians(45));
                    newMidY = dotY + shortExtension * (float)Math.sin(Math.toRadians(45));
                }
            }
            
            // 重新计算水平长延展线的终点
            float longExtension = 120f; // 长延展线长度
            float newEndX, newEndY;
            if (angle > -90 && angle < 90) {
                // 右半部分：水平向右
                newEndX = newMidX + longExtension;
                newEndY = newMidY;
            } else {
                // 左半部分：水平向左
                newEndX = newMidX - longExtension;
                newEndY = newMidY;
            }
            
            // 绘制45度短延展线 - 从偏移后的小圆点位置开始
            canvas.drawLine(dotX, dotY, newMidX, newMidY, linePaint);
            
            // 绘制水平长延展线
            canvas.drawLine(newMidX, newMidY, newEndX, newEndY, linePaint);
            
            // 更新endX和endY用于绘制文字
            segment.endX = newEndX;
            segment.endY = newEndY;
            
            // 根据位置设置文字对齐方式和绘制位置
            if (segment.endX > centerX) {
                // 右侧，左对齐，增加与延伸线的间距
                labelTextPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(segment.label, segment.endX + 8f, segment.endY, labelTextPaint);
            } else {
                // 左侧，右对齐，增加与延伸线的间距
                labelTextPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(segment.label, segment.endX - 8f, segment.endY, labelTextPaint);
            }
        }
    }
}