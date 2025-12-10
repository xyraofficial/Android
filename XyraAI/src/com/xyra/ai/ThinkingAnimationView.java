package com.xyra.ai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class ThinkingAnimationView extends View {
    
    private Paint dotPaint;
    private Paint glowPaint;
    private Paint textPaint;
    private Paint bgPaint;
    
    private float animationProgress = 0f;
    private Handler handler;
    private boolean isAnimating = false;
    
    private static final int DOT_COUNT = 3;
    private static final float DOT_RADIUS = 5f;
    private static final float DOT_SPACING = 18f;
    
    private int primaryColor = 0xFF22C55E;
    private int secondaryColor = 0xFF10B981;
    private int tertiaryColor = 0xFF059669;
    
    private AccelerateDecelerateInterpolator interpolator;
    
    public ThinkingAnimationView(Context context) {
        super(context);
        init();
    }
    
    public ThinkingAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);
        
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(14f);
        textPaint.setColor(0xFF6B7280);
        
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(0xFFF3F4F6);
        
        handler = new Handler();
        interpolator = new AccelerateDecelerateInterpolator();
    }
    
    public void startAnimation() {
        if (isAnimating) return;
        isAnimating = true;
        animationProgress = 0f;
        runAnimation();
    }
    
    public void stopAnimation() {
        isAnimating = false;
        handler.removeCallbacksAndMessages(null);
    }
    
    private void runAnimation() {
        if (!isAnimating) return;
        
        animationProgress += 0.05f;
        if (animationProgress > 1f) {
            animationProgress = 0f;
        }
        
        invalidate();
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runAnimation();
            }
        }, 50);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float centerY = getHeight() / 2f;
        float startX = 20f;
        
        RectF bgRect = new RectF(0, 4, getWidth() - 20, getHeight() - 4);
        canvas.drawRoundRect(bgRect, 20, 20, bgPaint);
        
        for (int i = 0; i < DOT_COUNT; i++) {
            float x = startX + (i * DOT_SPACING);
            
            float dotPhase = (animationProgress + (i * 0.2f)) % 1f;
            float bounce = (float) Math.sin(dotPhase * Math.PI * 2);
            float scale = 0.7f + 0.3f * Math.abs(bounce);
            float yOffset = -6f * bounce;
            
            float alpha = 0.6f + 0.4f * Math.abs(bounce);
            
            int color;
            if (i == 0) color = primaryColor;
            else if (i == 1) color = secondaryColor;
            else color = tertiaryColor;
            
            glowPaint.setColor(color);
            glowPaint.setAlpha((int)(40 * alpha));
            canvas.drawCircle(x, centerY + yOffset, DOT_RADIUS * scale * 2f, glowPaint);
            
            dotPaint.setColor(color);
            dotPaint.setAlpha((int)(255 * alpha));
            canvas.drawCircle(x, centerY + yOffset, DOT_RADIUS * scale, dotPaint);
        }
        
        float textX = startX + (DOT_COUNT * DOT_SPACING) + 12f;
        canvas.drawText("XyraAI sedang berpikir...", textX, centerY + 5f, textPaint);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 260;
        int height = 40;
        setMeasuredDimension(width, height);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}
