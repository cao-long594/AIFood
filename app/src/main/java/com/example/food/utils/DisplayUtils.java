package com.example.food.utils;

import android.content.Context;

/**
 * 显示相关工具类
 * 提供屏幕密度转换、尺寸计算等UI相关工具方法
 */
public class DisplayUtils {
    
    /**
     * dp 转 px（适配不同屏幕密度）
     * @param context 上下文对象
     * @param dpValue dp值
     * @return 转换后的px值
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f); // 四舍五入避免精度丢失
    }
    
    /**
     * px 转 dp（适配不同屏幕密度）
     * @param context 上下文对象
     * @param pxValue px值
     * @return 转换后的dp值
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f); // 四舍五入避免精度丢失
    }
}