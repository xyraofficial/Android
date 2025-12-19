package com.xyra.termux;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import android.content.Context;

public class MainActivity extends Activity {
    private WebView webView;
    private float touchStartY = 0;
    private boolean isRefreshing = false;
    private Handler handler = new Handler();
    private FloatingRefreshButton floatingButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        FrameLayout container = new FrameLayout(this);
        
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://xyra-termux.vercel.app/");
        
        container.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        
        floatingButton = new FloatingRefreshButton(this);
        floatingButton.setRefreshListener(new FloatingRefreshButton.RefreshListener() {
            public void onRefresh() {
                if (webView != null && !isRefreshing) {
                    isRefreshing = true;
                    webView.reload();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            isRefreshing = false;
                        }
                    }, 2000);
                }
            }
        });
        
        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(70, 70);
        buttonParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.RIGHT;
        buttonParams.rightMargin = 20;
        buttonParams.bottomMargin = 100;
        container.addView(floatingButton, buttonParams);
        
        setContentView(container);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (webView == null) {
            return super.onTouchEvent(event);
        }
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartY = event.getY();
                break;
                
            case MotionEvent.ACTION_UP:
                float touchEndY = event.getY();
                float deltaY = touchEndY - touchStartY;
                
                if (deltaY > 100 && !isRefreshing && isWebViewAtTop()) {
                    isRefreshing = true;
                    webView.reload();
                    
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            isRefreshing = false;
                        }
                    }, 2000);
                }
                break;
        }
        
        return super.onTouchEvent(event);
    }

    private boolean isWebViewAtTop() {
        return webView.getScrollY() == 0;
    }

    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

class FloatingRefreshButton extends View {
    private float touchOffsetX = 0;
    private float touchOffsetY = 0;
    private boolean isDragging = false;
    private boolean isHidden = false;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private RefreshListener refreshListener;
    private Handler handler = new Handler();
    private int lastX = 0;
    private int lastY = 0;
    private static final int PADDING = 20;
    private static final int HIDE_OFFSET = 40;
    private int buttonSize = 70;

    public interface RefreshListener {
        void onRefresh();
    }

    public FloatingRefreshButton(Context context) {
        super(context);
        setClickable(true);
        
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        
        Paint shadowPaint = new Paint();
        shadowPaint.setColor(Color.argb(60, 0, 0, 0));
        shadowPaint.setStyle(Paint.Style.FILL);
        RectF shadowRect = new RectF(2, 2, getWidth() - 2, getHeight() - 2);
        canvas.drawRoundRect(shadowRect, getWidth() / 2, getHeight() / 2, shadowPaint);
        
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#00D9FF"));
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);
        RectF bgRect = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(bgRect, getWidth() / 2, getHeight() / 2, bgPaint);
        
        Paint iconPaint = new Paint();
        iconPaint.setColor(Color.WHITE);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeWidth(2.2f);
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);
        iconPaint.setAntiAlias(true);
        
        float arrowRadius = 11;
        RectF arcRect = new RectF(
            centerX - arrowRadius, 
            centerY - arrowRadius, 
            centerX + arrowRadius, 
            centerY + arrowRadius
        );
        
        canvas.drawArc(arcRect, 45, 270, false, iconPaint);
        
        float arrowX = centerX + arrowRadius + 2;
        float arrowY = centerY - arrowRadius - 2;
        canvas.drawLine(arrowX, arrowY, arrowX - 6, arrowY + 5, iconPaint);
        canvas.drawLine(arrowX, arrowY, arrowX + 1, arrowY + 6, iconPaint);
        
        float bottomArrowX = centerX - arrowRadius - 2;
        float bottomArrowY = centerY + arrowRadius + 2;
        canvas.drawLine(bottomArrowX, bottomArrowY, bottomArrowX + 6, bottomArrowY - 5, iconPaint);
        canvas.drawLine(bottomArrowX, bottomArrowY, bottomArrowX - 1, bottomArrowY - 6, iconPaint);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getRawX();
        float eventY = event.getRawY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDragging = false;
                touchOffsetX = eventX - getX();
                touchOffsetY = eventY - getY();
                lastX = (int) getX();
                lastY = (int) getY();
                return true;
                
            case MotionEvent.ACTION_MOVE:
                float newX = eventX - touchOffsetX;
                float newY = eventY - touchOffsetY;
                
                isDragging = true;
                
                if (newX < -HIDE_OFFSET) {
                    newX = -HIDE_OFFSET;
                    isHidden = true;
                } else if (newX > screenWidth - buttonSize + HIDE_OFFSET) {
                    newX = screenWidth - buttonSize + HIDE_OFFSET;
                    isHidden = true;
                } else {
                    isHidden = false;
                }
                
                if (newY < 0) newY = 0;
                if (newY > screenHeight - buttonSize) {
                    newY = screenHeight - buttonSize;
                }
                
                setX(newX);
                setY(newY);
                lastX = (int) newX;
                lastY = (int) newY;
                return true;
                
            case MotionEvent.ACTION_UP:
                if (!isDragging) {
                    handleClick();
                } else {
                    snapToEdge();
                }
                isDragging = false;
                return true;
        }
        
        return super.onTouchEvent(event);
    }

    private void handleClick() {
        if (refreshListener != null) {
            refreshListener.onRefresh();
        }
    }

    private void snapToEdge() {
        float currentX = getX();
        float targetX;
        float currentY = getY();
        
        if (currentX < screenWidth / 2) {
            if (currentX < -HIDE_OFFSET / 2) {
                targetX = -HIDE_OFFSET;
                isHidden = true;
            } else {
                targetX = PADDING;
                isHidden = false;
            }
        } else {
            if (currentX > screenWidth - buttonSize + HIDE_OFFSET / 2) {
                targetX = screenWidth - buttonSize + HIDE_OFFSET;
                isHidden = true;
            } else {
                targetX = screenWidth - buttonSize - PADDING;
                isHidden = false;
            }
        }
        
        animateTo(targetX, currentY);
    }

    private void animateTo(float targetX, float targetY) {
        float currentX = getX();
        float currentY = getY();
        int steps = 10;
        
        for (int i = 0; i <= steps; i++) {
            final float interpX = currentX + (targetX - currentX) * i / steps;
            final float interpY = currentY + (targetY - currentY) * i / steps;
            final int index = i;
            
            handler.postDelayed(new Runnable() {
                public void run() {
                    setX(interpX);
                    setY(interpY);
                }
            }, index * 20);
        }
    }
}
