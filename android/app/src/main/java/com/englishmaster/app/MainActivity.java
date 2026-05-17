package com.englishmaster.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {

    private WebView webView;
    private TTSBridge ttsBridge;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Init TTS before WebView
        ttsBridge = new TTSBridge(this);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowFileAccessFromFileURLs(true);

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(android.webkit.PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        // Register TTS bridge - accessible as window.AndroidTTS in JavaScript
        webView.addJavascriptInterface(ttsBridge, "AndroidTTS");

        webView.loadUrl("file:///android_asset/public/index.html");
    }

    @Override
    public void onBackPressed() {
        // Let JS handle back button first (for panel navigation)
        webView.evaluateJavascript(
            "(function(){ if(typeof handleAndroidBack==='function') return handleAndroidBack(); return false; })()",
            value -> {
                if (!"true".equals(value)) {
                    // JS didn't handle it - use default
                    runOnUiThread(() -> {
                        if (webView.canGoBack()) {
                            webView.goBack();
                        } else {
                            finish();
                        }
                    });
                }
            }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsBridge != null) ttsBridge.shutdown();
    }

    // TTS Bridge class
    public static class TTSBridge implements TextToSpeech.OnInitListener {
        private TextToSpeech tts;
        private boolean ready = false;
        private final Handler mainHandler = new Handler(Looper.getMainLooper());

        public TTSBridge(Activity activity) {
            tts = new TextToSpeech(activity, this);
        }

        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                // Set English UK, fallback to US, fallback to default
                int result = tts.setLanguage(Locale.UK);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    result = tts.setLanguage(Locale.US);
                }
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.getDefault());
                }
                tts.setSpeechRate(0.85f);
                tts.setPitch(1.0f);
                ready = true;
            }
        }

        @JavascriptInterface
        public void speak(final String text) {
            speak(text, "en-GB");
        }

        @JavascriptInterface
        public void speak(final String text, final String lang) {
            if (text == null || text.trim().isEmpty()) return;
            mainHandler.post(() -> {
                if (tts == null || !ready) return;
                // Set language per call
                java.util.Locale locale;
                if ("en-US".equals(lang) || "en_US".equals(lang)) {
                    locale = java.util.Locale.US;
                } else if ("en-AU".equals(lang)) {
                    locale = new java.util.Locale("en", "AU");
                } else {
                    locale = java.util.Locale.UK; // default en-GB
                }
                int result = tts.setLanguage(locale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(java.util.Locale.US); // fallback
                }
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString());
            });
        }

        @JavascriptInterface
        public void stop() {
            mainHandler.post(() -> {
                if (tts != null) tts.stop();
            });
        }

        @JavascriptInterface
        public boolean isReady() {
            return ready;
        }

        public void shutdown() {
            if (tts != null) {
                tts.stop();
                tts.shutdown();
                tts = null;
            }
        }
    }
}
