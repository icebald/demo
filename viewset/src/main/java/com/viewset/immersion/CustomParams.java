package com.viewset.immersion;

import android.graphics.Color;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.RestrictTo;

import java.util.HashMap;
import java.util.Map;

@RestrictTo(RestrictTo.Scope.LIBRARY)
class CustomParams implements Cloneable {

    @ColorInt
    int statusBarColor = Color.TRANSPARENT;

    @ColorInt
    int navigationBarColor = Color.BLACK;

    int defaultNavigationBarColor = Color.BLACK;

    @FloatRange(from = 0f, to = 1f)
    float statusBarAlpha = 0.0f;

    @FloatRange(from = 0f, to = 1f)
    float statusBarTempAlpha = 0.0f;

    @FloatRange(from = 0f, to = 1f)
    float navigationBarAlpha = 0.0f;
    @FloatRange(from = 0f, to = 1f)
    float navigationBarTempAlpha = 0.0f;

    boolean fullScreen = false;

    boolean hideNavigationBar = false;

    BarHide barHide = BarHide.FLAG_SHOW_BAR;

    boolean statusBarDarkFont = false;

    boolean navigationBarDarkIcon = false;

    boolean autoStatusBarDarkModeEnable = false;

    boolean autoNavigationBarDarkModeEnable = false;

    @FloatRange(from = 0f, to = 1f)
    float autoStatusBarDarkModeAlpha = 0.0f;

    @FloatRange(from = 0f, to = 1f)
    float autoNavigationBarDarkModeAlpha = 0.0f;

    boolean statusBarColorEnabled = true;

    @ColorInt
    int statusBarColorTransform = Color.BLACK;

    @ColorInt
    int navigationBarColorTransform = Color.BLACK;

    Map<View, Map<Integer, Integer>> viewMap = new HashMap<>();

    @FloatRange(from = 0f, to = 1f)
    float viewAlpha = 0.0f;

    @ColorInt
    int contentColor = Color.TRANSPARENT;

    @ColorInt
    int contentColorTransform = Color.BLACK;

    @FloatRange(from = 0f, to = 1f)
    float contentAlpha = 0.0f;

    public boolean fits = false;

    View titleBarView;

    View statusBarView;

    boolean fitsLayoutOverlapEnable = true;

    @ColorInt
    int flymeOSStatusBarFontColor;
    @ColorInt
    int flymeOSStatusBarFontTempColor;

    boolean isSupportActionBar = false;

    boolean keyboardEnable = false;

    int keyboardMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;

    boolean navigationBarEnable = true;

    boolean navigationBarWithKitkatEnable = true;

    boolean navigationBarWithEMUI3Enable = true;

    boolean barEnable = true;

    OnNavigationBarListener onNavigationBarListener;

    OnBarListener onBarListener;

    @Override
    public CustomParams clone() {
        CustomParams customParams = null;
        try {
            customParams = (CustomParams) super.clone();
        } catch (CloneNotSupportedException ignored) {
        }
        return customParams;
    }
}
