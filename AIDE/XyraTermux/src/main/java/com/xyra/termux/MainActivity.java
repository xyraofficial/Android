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
    private FrameLayout container;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        container = new FrameLayout(this);
        
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://xyra-termux.vercel.app/");
        
        container.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        
        floatingButton = new FloatingRefreshButton(this, container);
        floatingButton.setWebView(webView);
        floatingButton.setRefreshListener(new FloatingRefreshButton.RefreshListener() {
            public void run() {
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
        
        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(80, 80);
        buttonParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.RIGHT;
        buttonParams.rightMargin = 16;
        buttonParams.bottomMargin = 120;
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
    private float lastX = 0;
    private float lastY = 0;
    private float downX = 0;
    private float downY = 0;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private boolean isDragging = false;
    private boolean isHidden = false;
    private WebView webView;
    private RefreshListener refreshListener;
    private Handler handler = new Handler();
    private FrameLayout parentContainer;
    private int buttonSize = 80;
    private static final int PADDING = 16;
    private static final int HIDE_THRESHOLD = 25;

    public interface RefreshListener {
        void run();
    }

    public FloatingRefreshButton(Context context, FrameLayout parent) {
        super(context);
        setClickable(true);
        setFocusable(true);
        parentContainer = parent;
        
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        
        Paint shadowPaint = new Paint();
        shadowPaint.setColor(Color.argb(50, 0, 0, 0));
        shadowPaint.setStyle(Paint.Style.FILL);
        RectF shadowRect = new RectF(3, 3, getWidth() - 3, getHeight() - 3);
        canvas.drawRoundRect(shadowRect, 20, 20, shadowPaint);
        
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#00D9FF"));
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);
        RectF bgRect = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(bgRect, 20, 20, bgPaint);
        
        Paint iconPaint = new Paint();
        iconPaint.setColor(Color.WHITE);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeWidth(2);
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);
        iconPaint.setAntiAlias(true);
        
        canvas.drawCircle(centerX, centerY - 8, 10, iconPaint);
        canvas.drawLine(centerX, centerY + 5, centerX, centerY + 16, iconPaint);
        canvas.drawLine(centerX - 5, centerY + 10, centerX, centerY + 16, iconPaint);
        canvas.drawLine(centerX + 5, centerY + 10, centerX, centerY + 16, iconPaint);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getRawX();
        float eventY = event.getRawY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = eventX;
                downY = eventY;
                lastX = getX();
                lastY = getY();
                isDragging = false;
                return true;
                
            case MotionEvent.ACTION_MOVE:
                float deltaX = eventX - downX;
                float deltaY = eventY - downY;
                
                if (Math.abs(deltaX) > 8 || Math.abs(deltaY) > 8) {
                    isDragging = true;
                }
                
                if (isDragging) {
                    float newX = lastX + deltaX;
                    float newY = lastY + deltaY;
                    
                    if (newX < -HIDE_THRESHOLD) {
                        newX = -HIDE_THRESHOLD;
                        isHidden = true;
                    } else if (newX > screenWidth - buttonSize + HIDE_THRESHOLD) {
                        newX = screenWidth - buttonSize + HIDE_THRESHOLD;
                    } else {
                        isHidden = false;
                    }
                    
                    if (newY < 0) newY = 0;
                    if (newY > screenHeight - buttonSize) {
                        newY = screenHeight - buttonSize;
                    }
                    
                    ViewGroup.LayoutParams params = getLayoutParams();
                    if (params instanceof FrameLayout.LayoutParams) {
                        FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) params;
                        flp.leftMargin = (int) newX;
                        flp.topMargin = (int) newY;
                        flp.rightMargin = 0;
                        flp.bottomMargin = 0;
                        setLayoutParams(flp);
                    }
                }
                return true;
                
            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    snapToEdge();
                } else {
                    handleClick();
                }
                isDragging = false;
                return true;
        }
        
        return super.onTouchEvent(event);
    }

    private void handleClick() {
        if (refreshListener != null) {
            refreshListener.run();
        }
    }

    private void snapToEdge() {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (!(params instanceof FrameLayout.LayoutParams)) return;
        
        FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) params;
        float currentX = flp.leftMargin;
        float targetX;
        
        if (currentX < screenWidth / 2) {
            if (currentX < -HIDE_THRESHOLD / 2) {
                targetX = -HIDE_THRESHOLD;
                isHidden = true;
            } else {
                targetX = PADDING;
                isHidden = false;
            }
        } else {
            if (currentX > screenWidth - buttonSize - HIDE_THRESHOLD / 2) {
                targetX = screenWidth - buttonSize + HIDE_THRESHOLD;
                isHidden = true;
            } else {
                targetX = screenWidth - buttonSize - PADDING;
                isHidden = false;
            }
        }
        
        animateToX((int) targetX, flp.topMargin);
    }

    private void animateToX(final int targetX, final int currentY) {
        final ViewGroup.LayoutParams params = getLayoutParams();
        if (!(params instanceof FrameLayout.LayoutParams)) return;
        
        FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) params;
        final int startX = flp.leftMargin;
        final int steps = 12;
        
        for (int i = 0; i <= steps; i++) {
            final int step = i;
            handler.postDelayed(new Runnable() {
                public void run() {
                    int newX = startX + (int) ((targetX - startX) * step / (float) steps);
                    ViewGroup.LayoutParams p = getLayoutParams();
                    if (p instanceof FrameLayout.LayoutParams) {
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) p;
                        lp.leftMargin = newX;
                        lp.topMargin = currentY;
                        lp.rightMargin = 0;
                        lp.bottomMargin = 0;
                        setLayoutParams(lp);
                    }
                }
            }, i * 15);
        }
    }
}
