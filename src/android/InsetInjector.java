package com.ionic.cordova.insetinjector;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

public class InsetInjector extends CordovaPlugin {
    private boolean isEdgeToEdge = false;
    private boolean isFullScreen = false;

    @Override
    protected void pluginInitialize() {
        isEdgeToEdge = preferences.getBoolean("AndroidEdgeToEdge", false) && Build.VERSION.SDK_INT >= 35;
        isFullScreen = preferences.getBoolean("Fullscreen", false);
        setupInsetsListener(this.webView.getView());
    }

    private void setupInsetsListener(View v) {
        ViewCompat.setOnApplyWindowInsetsListener(v, (view, windowInsetsCompat) -> {
            Insets safeAreaInsets = calculateSafeAreaInsets(windowInsetsCompat);

            boolean isStatusBarVisible = isStatusBarVisible(this.webView);
            int marginTop = isStatusBarVisible && !isEdgeToEdge && !isFullScreen ? safeAreaInsets.top : 0;
            int marginBottom = !isEdgeToEdge && !isFullScreen ? safeAreaInsets.bottom : 0;
            int marginLeft = !isEdgeToEdge && !isFullScreen ? safeAreaInsets.left : 0;
            int marginRight = !isEdgeToEdge && !isFullScreen ? safeAreaInsets.right : 0;

            boolean keyboardVisible = windowInsetsCompat.isVisible(WindowInsetsCompat.Type.ime());

            if (keyboardVisible) {
                safeAreaInsets = Insets.of(safeAreaInsets.left, safeAreaInsets.top, safeAreaInsets.right, 0);

                Insets imeInsets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.ime());
                setViewMargins(v, Insets.of(marginLeft, marginTop, marginRight, imeInsets.bottom));
            } else {
                setViewMargins(v, Insets.of(marginLeft, marginTop, marginRight, marginBottom));
            }

            if (!isEdgeToEdge) {
                if (!isStatusBarVisible) {
                    injectSafeAreaCSS(Insets.of(0, safeAreaInsets.top, 0, 0));
                } else {
                    injectSafeAreaCSS(Insets.NONE);
                }
            } else {
                injectSafeAreaCSS(safeAreaInsets);
            }

            return WindowInsetsCompat.CONSUMED;
        });

    }

    private Insets calculateSafeAreaInsets(WindowInsetsCompat windowInsetsCompat) {
        return windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
    }


    private void setViewMargins(View v, Insets insets) {
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        mlp.setMargins(insets.left, insets.top, insets.right, insets.bottom);
        v.setLayoutParams(mlp);
    }

    private void injectSafeAreaCSS(Insets insets) {
        float density = this.cordova.getActivity().getResources().getDisplayMetrics().density;
        float top = insets.top / density;
        float bottom = insets.bottom / density;
        float left = insets.left / density;
        float right = insets.right / density;

        String js = getCssInsetJsString("top", top) + getCssInsetJsString("right", right) + getCssInsetJsString("bottom", bottom) + getCssInsetJsString("left", left);

        this.webView.getView().post(() -> {
            ((WebView) webView.getEngine().getView()).evaluateJavascript(js, null);
        });
    }

    private String getCssInsetJsString(String inset, float pixels) {
        return String.format("document.documentElement.style.setProperty('--safe-area-inset-%s', '%spx');", inset, pixels);
    }

    private void initialSetup() {
        View decorView = this.cordova.getActivity().getWindow().getDecorView();
        WindowInsets currentInsets = decorView.getRootWindowInsets();

        if (currentInsets != null) {
            Log.d("InsetInjector", "Insets have been injected");
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(currentInsets, decorView);
            Insets safeAreaInsets = calculateSafeAreaInsets(insetsCompat);
            injectSafeAreaCSS(safeAreaInsets);
        }
        this.webView.getView().post(() -> this.webView.getView().requestApplyInsets());
    }

    private boolean isStatusBarVisible(CordovaWebView webView) {
        ViewParent parent = webView.getView().getParent();
        if (!(parent instanceof FrameLayout)) {
            return false;
        }

        FrameLayout rootView = (FrameLayout) parent;
        for (int i = 0; i < rootView.getChildCount(); i++) {
            View child = rootView.getChildAt(i);
            Object tag = child.getTag();
            if ("statusBarView".equals(tag)) {
                return child.getVisibility() == View.VISIBLE;
            }
        }
        return false;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("_ready")) {
            initialSetup();
            return true;
        }
        return false;
    }
}
