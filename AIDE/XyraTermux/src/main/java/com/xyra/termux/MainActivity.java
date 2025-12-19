package com.xyra.termux;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class MainActivity extends Activity {
    private WebView webView;
    private float touchStartY = 0;
    private boolean isRefreshing = false;

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
        
        setContentView(container);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (webView == null) return super.onTouchEvent(event);
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartY = event.getY();
                break;
                
            case MotionEvent.ACTION_UP:
                float touchEndY = event.getY();
                float deltaY = touchEndY - touchStartY;
                
                // If swiped down more than 100px and not already refreshing
                if (deltaY > 100 && !isRefreshing && isWebViewAtTop()) {
                    isRefreshing = true;
                    webView.reload();
                    // Reset after 2 seconds
                    webView.postDelayed(() -> isRefreshing = false, 2000);
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
