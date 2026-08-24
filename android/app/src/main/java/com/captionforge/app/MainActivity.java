package com.captionforge.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 101;
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        webView = new WebView(this);
        setContentView(webView);

        requestMediaPermissions();
        setupWebView();
    }

    private void requestMediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Attach JavaScript Native Bridge
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // Load the live studio app
        webView.loadUrl("https://caption-app.vercel.app");
    }

    public class WebAppInterface {
        @JavascriptInterface
        public boolean isNative() {
            return true;
        }

        @JavascriptInterface
        public void burnCaptions(String videoPath, String assContent) {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "⚡ Hardware GPU Rendering started...", Toast.LENGTH_SHORT).show();
            });

            NativeVideoEngine.burnCaptions(MainActivity.this, videoPath, assContent, new NativeVideoEngine.RenderCallback() {
                @Override
                public void onProgress(int percentage) {
                    runOnUiThread(() -> {
                        webView.evaluateJavascript("window.onNativeProgress && window.onNativeProgress(" + percentage + ");", null);
                    });
                }

                @Override
                public void onSuccess(String outputPath) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "✅ Saved to Gallery: " + outputPath, Toast.LENGTH_LONG).show();
                        webView.evaluateJavascript("window.onNativeSuccess && window.onNativeSuccess('" + outputPath + "');", null);
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "❌ Render error: " + errorMessage, Toast.LENGTH_LONG).show();
                        webView.evaluateJavascript("window.onNativeError && window.onNativeError('" + errorMessage.replace("'", "\\'") + "');", null);
                    });
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
