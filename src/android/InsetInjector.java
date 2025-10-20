package org.apache.cordova.InsetInjector;

import android.os.Build;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

public class InsetInjector extends CordovaPlugin {
    private boolean isEdgeToEdge = false;

    @Override
    protected void pluginInitialize() {
        isEdgeToEdge = preferences.getBoolean("AndroidEdgeToEdge", false) && Build.VERSION.SDK_INT >= 35;
        View webView = this.webView.getView();
        ViewCompat.setOnApplyWindowInsetsListener(webView, (v, insets) -> setupSafeAreaInsets(insets));
    }

    private WindowInsetsCompat setupSafeAreaInsets(WindowInsetsCompat windowInsetsCompat) {
        if (!isEdgeToEdge) {
            injectSafeAreaCSS(0, 0, 0, 0);
        } else {
            float density = this.cordova.getActivity().getResources().getDisplayMetrics().density;

            Insets insets = windowInsetsCompat
                    .getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());

            float top = insets.top / density;
            float bottom = insets.bottom / density;
            float left = insets.left / density;
            float right = insets.right / density;

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

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("_ready")) {
            initialSetup();
            return true;
        }
        return false;
    }
}
