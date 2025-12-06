package com.ionic.cordova.insetinjector;

import android.os.Build;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowInsets;
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

    @Override
    protected void pluginInitialize() {
        isEdgeToEdge = preferences.getBoolean("AndroidEdgeToEdge", false) && Build.VERSION.SDK_INT >= 35;
        View webView = this.webView.getView();
        ViewCompat.setOnApplyWindowInsetsListener((View) webView.getParent() , (v, insets) -> setupSafeAreaInsets(insets));
    }

    private WindowInsetsCompat setupSafeAreaInsets(WindowInsetsCompat windowInsetsCompat) {
        Insets insets = windowInsetsCompat
                .getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());

        float density = this.cordova.getActivity().getResources().getDisplayMetrics().density;
        float top = insets.top / density;
        float bottom = insets.bottom / density;
        float left = insets.left / density;
        float right = insets.right / density;

        if (!isEdgeToEdge) {
            boolean isStatusBarVisible = isStatusBarVisible(this.webView);
            if (!isStatusBarVisible) {
                injectSafeAreaCSS(top, 0, 0, 0);
            } else {
                injectSafeAreaCSS(0, 0, 0, 0);
            }
        } else {
            injectSafeAreaCSS(top, right, bottom, left);
        }

        return windowInsetsCompat;
    }

    private void injectSafeAreaCSS(float top, float right, float bottom, float left) {
        String js = getCssInsetJsString("top", top)
                + getCssInsetJsString("right", right)
                + getCssInsetJsString("bottom", bottom)
                + getCssInsetJsString("left", left);

        this.cordova.getActivity().runOnUiThread(() -> webView.loadUrl("javascript:" + js));
    }

    private String getCssInsetJsString(String inset, float pixels) {
        return String.format("document.documentElement.style.setProperty('--safe-area-inset-%s', '%spx');",
                inset,
                pixels);
    }

    private void initialSetup() {
        View decorView = this.cordova.getActivity().getWindow().getDecorView();
        WindowInsets currentInsets = decorView.getRootWindowInsets();

        if (currentInsets != null) {
            Log.d("InsetInjector", "Insets have been injected");
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(currentInsets, decorView);
            setupSafeAreaInsets(insetsCompat);
        }
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
