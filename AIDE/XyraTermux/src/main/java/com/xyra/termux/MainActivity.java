package com.xyra.termux;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.Window;

public class MainActivity extends Activity {
    private WebView webView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        
        webView.setWebViewClient(new WebViewClient());
        
        injectRefreshScript();
        webView.loadUrl("https://xyra-termux.vercel.app/");
        
        setContentView(webView);
    }

    private void injectRefreshScript() {
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                
                String script = "javascript:" +
                    "(function() {" +
                    "  let touchStartY = 0;" +
                    "  let lastRefreshTime = 0;" +
                    "  " +
                    "  document.addEventListener('touchstart', function(e) {" +
                    "    touchStartY = e.touches[0].clientY;" +
                    "  }, false);" +
                    "  " +
                    "  document.addEventListener('touchmove', function(e) {" +
                    "    if (window.scrollY === 0) {" +
                    "      let currentY = e.touches[0].clientY;" +
                    "      let delta = currentY - touchStartY;" +
                    "      " +
                    "      if (delta > 80 && (Date.now() - lastRefreshTime) > 2000) {" +
                    "        lastRefreshTime = Date.now();" +
                    "        window.location.reload();" +
                    "      }" +
                    "    }" +
                    "  }, false);" +
                    "})();";
                
                view.loadUrl(script);
            }
        });
    }

    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
