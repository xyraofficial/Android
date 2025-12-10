package com.xyra.ai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class ThinkingAnimationView extends View {
    
    private Paint dotPaint;
    private Paint glowPaint;
    private Paint textPaint;
    
    private int animationFrame = 0;
    private Handler handler;
    private boolean isAnimating = false;
    
    private static final int DOT_COUNT = 4;
    private static final float DOT_RADIUS = 6f;
    private static final float DOT_SPACING = 24f;
    
    private int[] gradientColors = {0xFF6366F1, 0xFF8B5CF6, 0xFFA855F7, 0xFFEC4899};
    
    private String[] thinkingSymbols = {"🧠", "✨", "💭", "⚡"};
    private int currentSymbol = 0;
    
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
        glowPaint.setAlpha(80);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        handler = new Handler();
    }
    
    public void startAnimation() {
        if (isAnimating) return;
        isAnimating = true;
        animationFrame = 0;
        runAnimation();
    }
    
    public void stopAnimation() {
        isAnimating = false;
        handler.removeCallbacksAndMessages(null);
    }
    
    private void runAnimation() {
        if (!isAnimating) return;
        
        animationFrame++;
        if (animationFrame % 8 == 0) {
            currentSymbol = (currentSymbol + 1) % thinkingSymbols.length;
        }
        
        invalidate();
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runAnimation();
            }
        }, 100);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float centerY = getHeight() / 2f;
        float startX = 40f;
        
        canvas.drawText(thinkingSymbols[currentSymbol], 20f, centerY + 10f, textPaint);
        
        for (int i = 0; i < DOT_COUNT; i++) {
            float x = startX + 30f + (i * DOT_SPACING);
            
            float phase = (animationFrame + i * 3) % 20;
            float scale = 0.5f + 0.5f * (float) Math.sin(phase * Math.PI / 10);
            float yOffset = -8f * (float) Math.sin(phase * Math.PI / 10);
            
            int colorIndex = (animationFrame / 4 + i) % gradientColors.length;
            dotPaint.setColor(gradientColors[colorIndex]);
            
            glowPaint.setColor(gradientColors[colorIndex]);
            glowPaint.setAlpha(60);
            canvas.drawCircle(x, centerY + yOffset, DOT_RADIUS * scale * 1.8f, glowPaint);
            
            canvas.drawCircle(x, centerY + yOffset, DOT_RADIUS * scale, dotPaint);
        }
        
        textPaint.setColor(0xFFE5E7EB);
        textPaint.setTextSize(14f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Sedang berpikir", startX + 30f + (DOT_COUNT * DOT_SPACING) + 10f, centerY + 5f, textPaint);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 280;
        int height = 48;
        setMeasuredDimension(width, height);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}
