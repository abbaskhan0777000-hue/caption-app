package com.captionforge.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 101;
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;

    private File currentUploadFile = null;
    private FileOutputStream currentUploadStream = null;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (filePathCallback != null) {
                    Uri[] results = null;
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            results = new Uri[count];
                            for (int i = 0; i < count; i++) {
                                results[i] = data.getClipData().getItemAt(i).getUri();
                            }
                        } else if (data.getData() != null) {
                            results = new Uri[]{data.getData()};
                        }
                    }
                    filePathCallback.onReceiveValue(results);
                    filePathCallback = null;
                }
            }
    );

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
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // Intercept all web requests and stream directly from assets with accurate MIME types
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String path = request.getUrl().getPath();
                if (path == null || path.isEmpty() || path.equals("/") || path.equals("/index.html")) {
                    path = "index.html";
                } else if (path.startsWith("/")) {
                    path = path.substring(1);
                }

                // Handle both _next and next_assets mappings
                if (path.startsWith("_next/")) {
                    path = "next_assets/" + path.substring(6);
                }

                try {
                    InputStream is = getAssets().open(path);
                    String mime = "text/html";
                    if (path.endsWith(".css")) {
                        mime = "text/css";
                    } else if (path.endsWith(".js") || path.endsWith(".mjs")) {
                        mime = "application/javascript";
                    } else if (path.endsWith(".png")) {
                        mime = "image/png";
                    } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                        mime = "image/jpeg";
                    } else if (path.endsWith(".svg")) {
                        mime = "image/svg+xml";
                    } else if (path.endsWith(".woff2")) {
                        mime = "font/woff2";
                    } else if (path.endsWith(".woff")) {
                        mime = "font/woff";
                    } else if (path.endsWith(".ttf")) {
                        mime = "font/ttf";
                    } else if (path.endsWith(".json")) {
                        mime = "application/json";
                    }

                    WebResourceResponse response = new WebResourceResponse(mime, "UTF-8", is);
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Access-Control-Allow-Origin", "*");
                    response.setResponseHeaders(headers);
                    return response;
                } catch (Exception e) {
                    if (!path.contains(".")) {
                        try {
                            InputStream is = getAssets().open("index.html");
                            WebResourceResponse response = new WebResourceResponse("text/html", "UTF-8", is);
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Access-Control-Allow-Origin", "*");
                            response.setResponseHeaders(headers);
                            return response;
                        } catch (Exception ignored) {}
                    }
                    return super.shouldInterceptRequest(view, request);
                }
            }
        });

        // Attach JavaScript Native Bridge
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    filePickerLauncher.launch(intent);
                } catch (Exception e) {
                    MainActivity.this.filePathCallback = null;
                    return false;
                }
                return true;
            }
        });

        // Load studio with local origin
        webView.loadUrl("https://captionforge.local/");
    }

    public class WebAppInterface {
        @JavascriptInterface
        public boolean isNative() {
            return true;
        }

        @JavascriptInterface
        public boolean startVideoUpload(String filename) {
            try {
                if (currentUploadStream != null) {
                    currentUploadStream.close();
                }
                currentUploadFile = new File(getCacheDir(), "input_" + System.currentTimeMillis() + "_" + filename);
                currentUploadStream = new FileOutputStream(currentUploadFile);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @JavascriptInterface
        public boolean appendVideoChunk(String base64Chunk) {
            try {
                if (currentUploadStream != null) {
                    byte[] bytes = android.util.Base64.decode(base64Chunk, android.util.Base64.DEFAULT);
                    currentUploadStream.write(bytes);
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }

        @JavascriptInterface
        public String finishVideoUpload() {
            try {
                if (currentUploadStream != null) {
                    currentUploadStream.flush();
                    currentUploadStream.close();
                    currentUploadStream = null;
                }
                return currentUploadFile != null ? currentUploadFile.getAbsolutePath() : null;
            } catch (Exception e) {
                return null;
            }
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
