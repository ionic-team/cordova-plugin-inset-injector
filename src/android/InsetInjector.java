package org.apache.cordova.insetInjector;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class InsetInjector extends CordovaPlugin {
    private boolean isEdgeToEdge = false;

    @Override
    protected void pluginInitialize() {
        isEdgeToEdge = preferences.getBoolean("AndroidEdgeToEdge", false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM;
        View decorView = this.cordova.getActivity().getWindow().getDecorView();
        ViewCompat.setOnApplyWindowInsetsListener(decorView, (v, insets) -> setupSafeAreaInsets(insets));
    }

    private WindowInsetsCompat setupSafeAreaInsets(WindowInsetsCompat insetsCompat) {
        if (!isEdgeToEdge){
            injectSafeAreaCSS(0, 0, 0, 0);
        }else{
            androidx.core.graphics.Insets insets = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars());
            androidx.core.graphics.Insets displayCutout = insetsCompat.getInsets(WindowInsetsCompat.Type.displayCutout());
    
            int top = Math.max(insets.top, displayCutout.top);
            int bottom = Math.max(insets.bottom, displayCutout.bottom);
            int left = Math.max(insets.left, displayCutout.left);
            int right = Math.max(insets.right, displayCutout.right);
    
            injectSafeAreaCSS(top, right, bottom, left);
        }

        return WindowInsetsCompat.CONSUMED;
    }

    private void injectSafeAreaCSS(int top, int right, int bottom, int left) {
        Activity activity = this.cordova.getActivity();
        float density = activity.getResources().getDisplayMetrics().density;
        int topPx = (int) (top / density);
        int rightPx = (int) (right / density);
        int bottomPx = (int) (bottom / density);
        int leftPx = (int) (left / density);

        String js = getCssInsetJsString("TOP", topPx)
                + getCssInsetJsString("RIGHT", rightPx)
                + getCssInsetJsString("BOTTOM", bottomPx)
                + getCssInsetJsString("LEFT", leftPx);
                
        activity.runOnUiThread(() -> webView.loadUrl("javascript:" + js));
    }

    private String getCssInsetJsString(String inset, int size) {
        return "document.documentElement.style.setProperty('--safe-area-inset-" + inset.toLowerCase() + "', '" + size
                + "px');";
    }

    private void initialSetup() {
        View decorView = this.cordova.getActivity().getWindow().getDecorView();
        WindowInsets currentInsets = decorView.getRootWindowInsets();

        Log.d("InsetInjector", "InsetInjector ready");

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
