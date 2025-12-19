package com.xyra.termux;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
    private WebView webView;
    private View loadingScreen;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        FrameLayout mainContainer = new FrameLayout(this);
        mainContainer.setBackgroundColor(0xFF1a1a2e);
        
        webView = new WebView(this);
        webView.setBackgroundColor(0xFF1a1a2e);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (loadingScreen != null && loadingScreen.getVisibility() == View.VISIBLE) {
                    loadingScreen.setVisibility(View.GONE);
                }
            }
        });
        
        loadingScreen = getLayoutInflater().inflate(R.layout.loading_screen, null);
        
        mainContainer.addView(webView);
        mainContainer.addView(loadingScreen);
        
        webView.loadUrl("https://xyra-termux.vercel.app/");
        setContentView(mainContainer);
    }

    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
