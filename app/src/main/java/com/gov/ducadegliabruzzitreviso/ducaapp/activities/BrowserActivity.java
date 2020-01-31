package com.gov.ducadegliabruzzitreviso.ducaapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.gov.ducadegliabruzzitreviso.ducaapp.R;

/**
 * Activity for the Browser used to access SOS Studio website.
 *
 * @author Riccardo De Zen
 */
public class BrowserActivity extends AppCompatActivity {
    private WebView webView;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        Intent intent = getIntent();
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        webView = (WebView) findViewById(R.id.webView_SOS);
        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl(intent.getStringExtra("URL"));

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (url.equals("http://myduca.it/sos/")) finish();
            progressBar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }
    }
}