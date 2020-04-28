package com.viewset.immersion;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.viewset.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.viewset.immersion.Constants.FLAG_FITS_DEFAULT;
import static com.viewset.immersion.Constants.FLAG_FITS_STATUS;
import static com.viewset.immersion.Constants.FLAG_FITS_SYSTEM_WINDOWS;
import static com.viewset.immersion.Constants.FLAG_FITS_TITLE;
import static com.viewset.immersion.Constants.FLAG_FITS_TITLE_MARGIN_TOP;
import static com.viewset.immersion.Constants.IMMERSION_BOUNDARY_COLOR;
import static com.viewset.immersion.Constants.IMMERSION_ID_NAVIGATION_BAR_VIEW;
import static com.viewset.immersion.Constants.IMMERSION_ID_STATUS_BAR_VIEW;
import static com.viewset.immersion.Constants.IMMERSION_MIUI_NAVIGATION_BAR_DARK;
import static com.viewset.immersion.Constants.IMMERSION_MIUI_STATUS_BAR_DARK;

public final class Immersion implements ImmersionCallback {
    private Activity mActivity;
    private Fragment mSupportFragment;
    private android.app.Fragment mFragment;
    private Dialog mDialog;
    private Window mWindow;
    private ViewGroup mDecorView;
    private ViewGroup mContentView;
    private Immersion mParentBar;

    private boolean mIsActivity = false;
    private boolean mIsFragment = false;
    private boolean mIsDialogFragment = false;
    private boolean mIsDialog = false;
    private CustomParams mCustomParams;
    private SystemConfig mSystemConfig;
    private int mNavigationBarHeight = 0;
    private int mNavigationBarWidth = 0;
    private int mActionBarHeight = 0;
    private Map<String, CustomParams> mTagMap = new HashMap<>();
    private int mFitsStatusBarType = FLAG_FITS_DEFAULT;
    private boolean mInitialized = false;
    private boolean mIsActionBarBelowLOLLIPOP = false;

    private boolean mKeyboardTempEnable = false;

    private int mPaddingLeft = 0, mPaddingTop = 0, mPaddingRight = 0, mPaddingBottom = 0;

    public static Immersion with(@NonNull Activity activity) {
        return getRetriever().get(activity);
    }

    public static Immersion with(@NonNull Fragment fragment) {
        return getRetriever().get(fragment, false);
    }

    public static Immersion with(@NonNull Fragment fragment, boolean isOnly) {
        return getRetriever().get(fragment, isOnly);
    }

    public static Immersion with(@NonNull android.app.Fragment fragment) {
        return getRetriever().get(fragment, false);
    }

    public static Immersion with(@NonNull android.app.Fragment fragment, boolean isOnly) {
        return getRetriever().get(fragment, isOnly);
    }

    public static Immersion with(@NonNull DialogFragment dialogFragment) {
        return getRetriever().get(dialogFragment, false);
    }

    public static Immersion with(@NonNull android.app.DialogFragment dialogFragment) {
        return getRetriever().get(dialogFragment, false);
    }

    public static Immersion with(@NonNull Activity activity, @NonNull Dialog dialog) {
        return getRetriever().get(activity, dialog);
    }

    public static void destroy(@NonNull Fragment fragment) {
        getRetriever().destroy(fragment, false);
    }

    public static void destroy(@NonNull Fragment fragment, boolean isOnly) {
        getRetriever().destroy(fragment, isOnly);
    }

    public static void destroy(@NonNull Activity activity, @NonNull Dialog dialog) {
        getRetriever().destroy(activity, dialog);
    }

    public void init() {
        if (!mCustomParams.barEnable) {
            return;
        }

        updateBarParams();
        setBar();
        fitsWindows();
        transformView();
        mInitialized = true;
    }

    void onDestroy() {
        cancelListener();
        if (mIsDialog && mParentBar != null) {
            mParentBar.mCustomParams.keyboardEnable = mParentBar.mKeyboardTempEnable;
            if (mParentBar.mCustomParams.barHide != BarHide.FLAG_SHOW_BAR) {
                mParentBar.setBar();
            }
        }
        mInitialized = false;
    }

    void onResume() {
        if (!mIsFragment && mInitialized && mCustomParams != null) {
            if (OSUtils.isEMUI3_x() && mCustomParams.navigationBarWithEMUI3Enable) {
                init();
            } else {
                if (mCustomParams.barHide != BarHide.FLAG_SHOW_BAR) {
                    setBar();
                }
            }
        }
    }

    void onConfigurationChanged(Configuration newConfig) {
        if (OSUtils.isEMUI3_x()) {
            if (mInitialized && !mIsFragment && mCustomParams.navigationBarWithKitkatEnable) {
                init();
            } else {
                fitsWindows();
            }
        } else {
            fitsWindows();
        }
    }

    private void updateBarParams() {
        adjustDarkModeParams();
        updateBarConfig();
        if (mParentBar != null) {
            if (mIsFragment) {
                mParentBar.mCustomParams = mCustomParams;
            }
            if (mIsDialog) {
                if (mParentBar.mKeyboardTempEnable) {
                    mParentBar.mCustomParams.keyboardEnable = false;
                }
            }
        }
    }

    private void setBar() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !OSUtils.isEMUI3_x()) {
            fitsNotchScreen();
            uiFlags = initBarAboveLOLLIPOP(uiFlags);
            uiFlags = setStatusBarDarkFont(uiFlags);
            uiFlags = setNavigationIconDark(uiFlags);
        } else {
            initBarBelowLOLLIPOP();
        }
        uiFlags = hideBar(uiFlags);
        mDecorView.setSystemUiVisibility(uiFlags);
        setSpecialBarDarkMode();
        if (mCustomParams.onNavigationBarListener != null) {
            NavigationBarObserver.getInstance().register(mActivity.getApplication());
        }
    }

    private void setSpecialBarDarkMode() {
        if (OSUtils.isMIUI6Later()) {
            SpecialBarFontUtils.setMIUIBarDark(mWindow, IMMERSION_MIUI_STATUS_BAR_DARK, mCustomParams.statusBarDarkFont);
            if (mCustomParams.navigationBarEnable) {
                SpecialBarFontUtils.setMIUIBarDark(mWindow, IMMERSION_MIUI_NAVIGATION_BAR_DARK, mCustomParams.navigationBarDarkIcon);
            }
        }

        if (OSUtils.isFlymeOS4Later()) {
            if (mCustomParams.flymeOSStatusBarFontColor != 0) {
                SpecialBarFontUtils.setStatusBarDarkIcon(mActivity, mCustomParams.flymeOSStatusBarFontColor);
            } else {
                SpecialBarFontUtils.setStatusBarDarkIcon(mActivity, mCustomParams.statusBarDarkFont);
            }
        }
    }

    private void fitsNotchScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !mInitialized) {
            WindowManager.LayoutParams lp = mWindow.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            mWindow.setAttributes(lp);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int initBarAboveLOLLIPOP(int uiFlags) {
        if (!mInitialized) {
            mCustomParams.defaultNavigationBarColor = mWindow.getNavigationBarColor();
        }

        uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (mCustomParams.fullScreen && mCustomParams.navigationBarEnable) {
            uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (mSystemConfig.hasNavigationBar()) {
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        if (mCustomParams.statusBarColorEnabled) {
            mWindow.setStatusBarColor(ColorUtils.blendARGB(mCustomParams.statusBarColor,
                    mCustomParams.statusBarColorTransform, mCustomParams.statusBarAlpha));
        } else {
            mWindow.setStatusBarColor(ColorUtils.blendARGB(mCustomParams.statusBarColor,
                    Color.TRANSPARENT, mCustomParams.statusBarAlpha));
        }

        if (mCustomParams.navigationBarEnable) {
            mWindow.setNavigationBarColor(ColorUtils.blendARGB(mCustomParams.navigationBarColor,
                    mCustomParams.navigationBarColorTransform, mCustomParams.navigationBarAlpha));
        } else {
            mWindow.setNavigationBarColor(mCustomParams.defaultNavigationBarColor);
        }
        return uiFlags;
    }

    private void initBarBelowLOLLIPOP() {
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setupStatusBarView();
        if (mSystemConfig.hasNavigationBar() || OSUtils.isEMUI3_x()) {
            if (mCustomParams.navigationBarEnable && mCustomParams.navigationBarWithKitkatEnable) {
                mWindow.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            } else {
                mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
            if (mNavigationBarHeight == 0) {
                mNavigationBarHeight = mSystemConfig.getNavigationBarHeight();
            }
            if (mNavigationBarWidth == 0) {
                mNavigationBarWidth = mSystemConfig.getNavigationBarWidth();
            }
            setupNavBarView();
        }
    }

    private void setupStatusBarView() {
        View statusBarView = mDecorView.findViewById(IMMERSION_ID_STATUS_BAR_VIEW);
        if (statusBarView == null) {
            statusBarView = new View(mActivity);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    mSystemConfig.getStatusBarHeight());
            params.gravity = Gravity.TOP;
            statusBarView.setLayoutParams(params);
            statusBarView.setVisibility(View.VISIBLE);
            statusBarView.setId(IMMERSION_ID_STATUS_BAR_VIEW);
            mDecorView.addView(statusBarView);
        }
        if (mCustomParams.statusBarColorEnabled) {
            statusBarView.setBackgroundColor(ColorUtils.blendARGB(mCustomParams.statusBarColor,
                    mCustomParams.statusBarColorTransform, mCustomParams.statusBarAlpha));
        } else {
            statusBarView.setBackgroundColor(ColorUtils.blendARGB(mCustomParams.statusBarColor,
                    Color.TRANSPARENT, mCustomParams.statusBarAlpha));
        }
    }


    private void setupNavBarView() {
        View navigationBarView = mDecorView.findViewById(IMMERSION_ID_NAVIGATION_BAR_VIEW);
        if (navigationBarView == null) {
            navigationBarView = new View(mActivity);
            navigationBarView.setId(IMMERSION_ID_NAVIGATION_BAR_VIEW);
            mDecorView.addView(navigationBarView);
        }

        FrameLayout.LayoutParams params;
        if (mSystemConfig.isNavigationAtBottom()) {
            params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mSystemConfig.getNavigationBarHeight());
            params.gravity = Gravity.BOTTOM;
        } else {
            params = new FrameLayout.LayoutParams(mSystemConfig.getNavigationBarWidth(), FrameLayout.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.END;
        }
        navigationBarView.setLayoutParams(params);
        navigationBarView.setBackgroundColor(ColorUtils.blendARGB(mCustomParams.navigationBarColor,
                mCustomParams.navigationBarColorTransform, mCustomParams.navigationBarAlpha));

        if (mCustomParams.navigationBarEnable && mCustomParams.navigationBarWithKitkatEnable && !mCustomParams.hideNavigationBar) {
            navigationBarView.setVisibility(View.VISIBLE);
        } else {
            navigationBarView.setVisibility(View.GONE);
        }
    }


    private void adjustDarkModeParams() {
        if (mCustomParams.autoStatusBarDarkModeEnable && mCustomParams.statusBarColor != Color.TRANSPARENT) {
            boolean statusBarDarkFont = mCustomParams.statusBarColor > IMMERSION_BOUNDARY_COLOR;
            statusBarDarkFont(statusBarDarkFont, mCustomParams.autoStatusBarDarkModeAlpha);
        }
        if (mCustomParams.autoNavigationBarDarkModeEnable && mCustomParams.navigationBarColor != Color.TRANSPARENT) {
            boolean navigationBarDarkIcon = mCustomParams.navigationBarColor > IMMERSION_BOUNDARY_COLOR;
            navigationBarDarkIcon(navigationBarDarkIcon, mCustomParams.autoNavigationBarDarkModeAlpha);
        }
    }


    private int hideBar(int uiFlags) {
        switch (mCustomParams.barHide) {
            case FLAG_HIDE_BAR:
                uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.INVISIBLE;
                break;
            case FLAG_HIDE_STATUS_BAR:
                uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.INVISIBLE;
                break;
            case FLAG_HIDE_NAVIGATION_BAR:
                uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                break;
            case FLAG_SHOW_BAR:
                uiFlags |= View.SYSTEM_UI_FLAG_VISIBLE;
                break;
            default:
                break;
        }
        return uiFlags | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }


    private void fitsWindows() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !OSUtils.isEMUI3_x()) {
            fitsWindowsAboveLOLLIPOP();
        } else {
            fitsWindowsBelowLOLLIPOP();
        }
        fitsLayoutOverlap();
    }

    private void fitsWindowsBelowLOLLIPOP() {
        if (mCustomParams.isSupportActionBar) {
            mIsActionBarBelowLOLLIPOP = true;
            mContentView.post(this);
        } else {
            mIsActionBarBelowLOLLIPOP = false;
            postFitsWindowsBelowLOLLIPOP();
        }
    }

    @Override
    public void run() {
        postFitsWindowsBelowLOLLIPOP();
    }

    private void postFitsWindowsBelowLOLLIPOP() {
        updateBarConfig();
        fitsWindowsKITKAT();
        if (!mIsFragment && OSUtils.isEMUI3_x()) {
            fitsWindowsEMUI();
        }
    }

    private void fitsWindowsAboveLOLLIPOP() {
        updateBarConfig();
        if (checkFitsSystemWindows(mDecorView.findViewById(android.R.id.content))) {
            setPadding(0, 0, 0, 0);
            return;
        }
        int top = 0;
        if (mCustomParams.fits && mFitsStatusBarType == FLAG_FITS_SYSTEM_WINDOWS) {
            top = mSystemConfig.getStatusBarHeight();
        }
        if (mCustomParams.isSupportActionBar) {
            top = mSystemConfig.getStatusBarHeight() + mActionBarHeight;
        }
        setPadding(0, top, 0, 0);
    }


    private void fitsWindowsKITKAT() {
        if (checkFitsSystemWindows(mDecorView.findViewById(android.R.id.content))) {
            setPadding(0, 0, 0, 0);
            return;
        }
        int top = 0, right = 0, bottom = 0;
        if (mCustomParams.fits && mFitsStatusBarType == FLAG_FITS_SYSTEM_WINDOWS) {
            top = mSystemConfig.getStatusBarHeight();
        }
        if (mCustomParams.isSupportActionBar) {
            top = mSystemConfig.getStatusBarHeight() + mActionBarHeight;
        }
        if (mSystemConfig.hasNavigationBar() && mCustomParams.navigationBarEnable && mCustomParams.navigationBarWithKitkatEnable) {
            if (!mCustomParams.fullScreen) {
                if (mSystemConfig.isNavigationAtBottom()) {
                    bottom = mSystemConfig.getNavigationBarHeight();
                } else {
                    right = mSystemConfig.getNavigationBarWidth();
                }
            }
            if (mCustomParams.hideNavigationBar) {
                if (mSystemConfig.isNavigationAtBottom()) {
                    bottom = 0;
                } else {
                    right = 0;
                }
            } else {
                if (!mSystemConfig.isNavigationAtBottom()) {
                    right = mSystemConfig.getNavigationBarWidth();
                }
            }

        }
        setPadding(0, top, right, bottom);
    }

    private void fitsWindowsEMUI() {
        View navigationBarView = mDecorView.findViewById(IMMERSION_ID_NAVIGATION_BAR_VIEW);
        if (mCustomParams.navigationBarEnable && mCustomParams.navigationBarWithKitkatEnable) {
            if (navigationBarView != null) {
                EMUI3NavigationBarObserver.getInstance().addOnNavigationBarListener(this);
                EMUI3NavigationBarObserver.getInstance().register(mActivity.getApplication());
            }
        } else {
            EMUI3NavigationBarObserver.getInstance().removeOnNavigationBarListener(this);
            navigationBarView.setVisibility(View.GONE);
        }
    }

    private void updateBarConfig() {
        mSystemConfig = new SystemConfig(mActivity);
        if (!mInitialized || mIsActionBarBelowLOLLIPOP) {
            mActionBarHeight = mSystemConfig.getActionBarHeight();
        }
    }

    @Override
    public void onNavigationBarChange(boolean show) {
        View navigationBarView = mDecorView.findViewById(IMMERSION_ID_NAVIGATION_BAR_VIEW);
        if (navigationBarView != null) {
            mSystemConfig = new SystemConfig(mActivity);
            int bottom = mContentView.getPaddingBottom(), right = mContentView.getPaddingRight();
            if (!show) {
                navigationBarView.setVisibility(View.GONE);
                bottom = 0;
                right = 0;
            } else {
                navigationBarView.setVisibility(View.VISIBLE);
                if (checkFitsSystemWindows(mDecorView.findViewById(android.R.id.content))) {
                    bottom = 0;
                    right = 0;
                } else {
                    if (mNavigationBarHeight == 0) {
                        mNavigationBarHeight = mSystemConfig.getNavigationBarHeight();
                    }
                    if (mNavigationBarWidth == 0) {
                        mNavigationBarWidth = mSystemConfig.getNavigationBarWidth();
                    }
                    if (!mCustomParams.hideNavigationBar) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) navigationBarView.getLayoutParams();
                        if (mSystemConfig.isNavigationAtBottom()) {
                            params.gravity = Gravity.BOTTOM;
                            params.height = mNavigationBarHeight;
                            bottom = !mCustomParams.fullScreen ? mNavigationBarHeight : 0;
                            right = 0;
                        } else {
                            params.gravity = Gravity.END;
                            params.width = mNavigationBarWidth;
                            bottom = 0;
                            right = !mCustomParams.fullScreen ? mNavigationBarWidth : 0;
                        }
                        navigationBarView.setLayoutParams(params);
                    }
                }
            }
            setPadding(0, mContentView.getPaddingTop(), right, bottom);
        }
    }

    private int setStatusBarDarkFont(int uiFlags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mCustomParams.statusBarDarkFont) {
            return uiFlags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            return uiFlags;
        }
    }

    private int setNavigationIconDark(int uiFlags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mCustomParams.navigationBarDarkIcon) {
            return uiFlags | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        } else {
            return uiFlags;
        }
    }

    private void fitsLayoutOverlap() {
        int fixHeight = 0;
        if (mCustomParams.fitsLayoutOverlapEnable) {
            fixHeight = getStatusBarHeight(mActivity);
        }
        switch (mFitsStatusBarType) {
            case FLAG_FITS_TITLE:
                setTitleBar(mActivity, fixHeight, mCustomParams.titleBarView);
                break;
            case FLAG_FITS_TITLE_MARGIN_TOP:
                setTitleBarMarginTop(mActivity, fixHeight, mCustomParams.titleBarView);
                break;
            case FLAG_FITS_STATUS:
                setStatusBarView(mActivity, fixHeight, mCustomParams.statusBarView);
                break;
            default:
                break;
        }
    }

    private void transformView() {
        if (mCustomParams.viewMap.size() != 0) {
            Set<Map.Entry<View, Map<Integer, Integer>>> entrySet = mCustomParams.viewMap.entrySet();
            for (Map.Entry<View, Map<Integer, Integer>> entry : entrySet) {
                View view = entry.getKey();
                Map<Integer, Integer> map = entry.getValue();
                Integer colorBefore = mCustomParams.statusBarColor;
                Integer colorAfter = mCustomParams.statusBarColorTransform;
                for (Map.Entry<Integer, Integer> integerEntry : map.entrySet()) {
                    colorBefore = integerEntry.getKey();
                    colorAfter = integerEntry.getValue();
                }
                if (view != null) {
                    if (Math.abs(mCustomParams.viewAlpha - 0.0f) == 0) {
                        view.setBackgroundColor(ColorUtils.blendARGB(colorBefore, colorAfter, mCustomParams.statusBarAlpha));
                    } else {
                        view.setBackgroundColor(ColorUtils.blendARGB(colorBefore, colorAfter, mCustomParams.viewAlpha));
                    }
                }
            }
        }
    }

    private void cancelListener() {
        if (mActivity != null) {
            EMUI3NavigationBarObserver.getInstance().removeOnNavigationBarListener(this);
            NavigationBarObserver.getInstance().removeOnNavigationBarListener(mCustomParams.onNavigationBarListener);
        }
    }

    CustomParams getBarParams() {
        return mCustomParams;
    }

    private void setPadding(int left, int top, int right, int bottom) {
        if (mContentView != null) {
            mContentView.setPadding(left, top, right, bottom);
        }
        mPaddingLeft = left;
        mPaddingTop = top;
        mPaddingRight = right;
        mPaddingBottom = bottom;
    }

    int getPaddingLeft() {
        return mPaddingLeft;
    }

    int getPaddingTop() {
        return mPaddingTop;
    }

    int getPaddingRight() {
        return mPaddingRight;
    }

    int getPaddingBottom() {
        return mPaddingBottom;
    }

    Activity getActivity() {
        return mActivity;
    }

    Window getWindow() {
        return mWindow;
    }

    Fragment getSupportFragment() {
        return mSupportFragment;
    }

    android.app.Fragment getFragment() {
        return mFragment;
    }

    boolean isFragment() {
        return mIsFragment;
    }

    boolean isDialogFragment() {
        return mIsDialogFragment;
    }

    boolean initialized() {
        return mInitialized;
    }

    SystemConfig getBarConfig() {
        if (mSystemConfig == null) {
            mSystemConfig = new SystemConfig(mActivity);
        }
        return mSystemConfig;
    }

    int getActionBarHeight() {
        return mActionBarHeight;
    }

    private static boolean isSupportStatusBarDarkFont() {
        return OSUtils.isMIUI6Later() || OSUtils.isFlymeOS4Later()
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }

    private static boolean isSupportNavigationIconDark() {
        return OSUtils.isMIUI6Later() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static void setTitleBar(final Activity activity, int fixHeight, View... view) {
        if (activity == null) {
            return;
        }
        if (fixHeight < 0) {
            fixHeight = 0;
        }
        for (final View v : view) {
            if (v == null) {
                continue;
            }
            final int statusBarHeight = fixHeight;
            Integer fitsHeight = (Integer) v.getTag(R.id.immersion_fits_layout_overlap);
            if (fitsHeight == null) {
                fitsHeight = 0;
            }
            if (fitsHeight != statusBarHeight) {
                v.setTag(R.id.immersion_fits_layout_overlap, statusBarHeight);
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                if (layoutParams == null) {
                    layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT ||
                        layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    final ViewGroup.LayoutParams finalLayoutParams = layoutParams;
                    final Integer finalFitsHeight = fitsHeight;
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            finalLayoutParams.height = v.getHeight() + statusBarHeight - finalFitsHeight;
                            v.setPadding(v.getPaddingLeft(),
                                    v.getPaddingTop() + statusBarHeight - finalFitsHeight,
                                    v.getPaddingRight(),
                                    v.getPaddingBottom());
                            v.setLayoutParams(finalLayoutParams);
                        }
                    });
                } else {
                    layoutParams.height += statusBarHeight - fitsHeight;
                    v.setPadding(v.getPaddingLeft(), v.getPaddingTop() + statusBarHeight - fitsHeight,
                            v.getPaddingRight(), v.getPaddingBottom());
                    v.setLayoutParams(layoutParams);
                }
            }
        }
    }

    public static void setTitleBar(final Activity activity, View... view) {
        setTitleBar(activity, getStatusBarHeight(activity), view);
    }

    public static void setTitleBar(Fragment fragment, int fixHeight, View... view) {
        if (fragment == null) {
            return;
        }
        setTitleBar(fragment.getActivity(), fixHeight, view);
    }

    public static void setTitleBar(Fragment fragment, View... view) {
        if (fragment == null) {
            return;
        }
        setTitleBar(fragment.getActivity(), view);
    }

    public static void setTitleBar(android.app.Fragment fragment, int fixHeight, View... view) {
        if (fragment == null) {
            return;
        }
        setTitleBar(fragment.getActivity(), fixHeight, view);
    }

    public static void setTitleBar(android.app.Fragment fragment, View... view) {
        if (fragment == null) {
            return;
        }
        setTitleBar(fragment.getActivity(), view);
    }

    public static void setTitleBarMarginTop(Activity activity, int fixHeight, View... view) {
        if (activity == null) {
            return;
        }
        if (fixHeight < 0) {
            fixHeight = 0;
        }
        for (View v : view) {
            if (v == null) {
                continue;
            }
            Integer fitsHeight = (Integer) v.getTag(R.id.immersion_fits_layout_overlap);
            if (fitsHeight == null) {
                fitsHeight = 0;
            }
            if (fitsHeight != fixHeight) {
                v.setTag(R.id.immersion_fits_layout_overlap, fixHeight);
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                if (lp == null) {
                    lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) lp;
                layoutParams.setMargins(layoutParams.leftMargin,
                        layoutParams.topMargin + fixHeight - fitsHeight,
                        layoutParams.rightMargin,
                        layoutParams.bottomMargin);
                v.setLayoutParams(layoutParams);
            }
        }
    }

    public static void setTitleBarMarginTop(Activity activity, View... view) {
        setTitleBarMarginTop(activity, getStatusBarHeight(activity), view);
    }

    public static void setTitleBarMarginTop(Fragment fragment, int fixHeight, View... view) {
        if (fragment == null) {
            return;
        }
        setTitleBarMarginTop(fragment.getActivity(), fixHeight, view);
    }

    public static void setTitleBarMarginTop(Fragment fragment, View... view) {
        if (fragment == null) {
            return;
        }
        setTitleBarMarginTop(fragment.getActivity(), view);
    }

    public static void setTitleBarMarginTop(android.app.Fragment fragment, int fixHeight, View...
            view) {
        if (fragment == null) {
            return;
        }
        setTitleBarMarginTop(fragment.getActivity(), fixHeight, view);
    }

    public static void setTitleBarMarginTop(android.app.Fragment fragment, View... view) {
        if (fragment == null) {
            return;
        }
        setTitleBarMarginTop(fragment.getActivity(), view);
    }

    public static void setStatusBarView(Activity activity, int fixHeight, View... view) {
        if (activity == null) {
            return;
        }
        if (fixHeight < 0) {
            fixHeight = 0;
        }
        for (View v : view) {
            if (v == null) {
                continue;
            }
            Integer fitsHeight = (Integer) v.getTag(R.id.immersion_fits_layout_overlap);
            if (fitsHeight == null) {
                fitsHeight = 0;
            }
            if (fitsHeight != fixHeight) {
                v.setTag(R.id.immersion_fits_layout_overlap, fixHeight);
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                if (lp == null) {
                    lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                }
                lp.height = fixHeight;
                v.setLayoutParams(lp);
            }
        }
    }

    public static void setStatusBarView(Activity activity, View... view) {
        setStatusBarView(activity, getStatusBarHeight(activity), view);
    }

    public static void setStatusBarView(Fragment fragment, int fixHeight, View... view) {
        if (fragment == null) {
            return;
        }
        setStatusBarView(fragment.getActivity(), fixHeight, view);
    }

    public static void setStatusBarView(Fragment fragment, View... view) {
        if (fragment == null) {
            return;
        }
        setStatusBarView(fragment.getActivity(), view);
    }

    public static void setStatusBarView(android.app.Fragment fragment, int fixHeight, View...
            view) {
        if (fragment == null) {
            return;
        }
        setStatusBarView(fragment.getActivity(), fixHeight, view);
    }

    public static void setStatusBarView(android.app.Fragment fragment, View... view) {
        if (fragment == null) {
            return;
        }
        setStatusBarView(fragment.getActivity(), view);
    }

    public static void setFitsSystemWindows(Activity activity, boolean applySystemFits) {
        if (activity == null) {
            return;
        }
        setFitsSystemWindows(((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0), applySystemFits);
    }

    public static void setFitsSystemWindows(Activity activity) {
        setFitsSystemWindows(activity, true);
    }

    public static void setFitsSystemWindows(Fragment fragment, boolean applySystemFits) {
        if (fragment == null) {
            return;
        }
        setFitsSystemWindows(fragment.getActivity(), applySystemFits);
    }

    public static void setFitsSystemWindows(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        setFitsSystemWindows(fragment.getActivity());
    }

    public static void setFitsSystemWindows(android.app.Fragment fragment, boolean applySystemFits) {
        if (fragment == null) {
            return;
        }
        setFitsSystemWindows(fragment.getActivity(), applySystemFits);
    }

    public static void setFitsSystemWindows(android.app.Fragment fragment) {
        if (fragment == null) {
            return;
        }
        setFitsSystemWindows(fragment.getActivity());
    }

    private static void setFitsSystemWindows(View view, boolean applySystemFits) {
        if (view == null) {
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (viewGroup instanceof DrawerLayout) {
                setFitsSystemWindows(viewGroup.getChildAt(0), applySystemFits);
            } else {
                viewGroup.setFitsSystemWindows(applySystemFits);
                viewGroup.setClipToPadding(true);
            }
        } else {
            view.setFitsSystemWindows(applySystemFits);
        }
    }

    public static boolean checkFitsSystemWindows(View view) {
        if (view == null) {
            return false;
        }
        if (view.getFitsSystemWindows()) {
            return true;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0, count = viewGroup.getChildCount(); i < count; i++) {
                View childView = viewGroup.getChildAt(i);
                if (childView instanceof DrawerLayout) {
                    if (checkFitsSystemWindows(childView)) {
                        return true;
                    }
                }
                if (childView.getFitsSystemWindows()) {
                    return true;
                }
            }
        }
        return false;
    }


    @TargetApi(14)
    public static boolean hasNavigationBar(@NonNull Activity activity) {
        SystemConfig config = new SystemConfig(activity);
        return config.hasNavigationBar();
    }

    @TargetApi(14)
    public static boolean hasNavigationBar(@NonNull Fragment fragment) {
        if (fragment.getActivity() == null) {
            return false;
        }
        return hasNavigationBar(fragment.getActivity());
    }

    @TargetApi(14)
    public static boolean hasNavigationBar(@NonNull android.app.Fragment fragment) {
        if (fragment.getActivity() == null) {
            return false;
        }
        return hasNavigationBar(fragment.getActivity());
    }

    @TargetApi(14)
    public static int getNavigationBarHeight(@NonNull Activity activity) {
        SystemConfig config = new SystemConfig(activity);
        return config.getNavigationBarHeight();
    }

    @TargetApi(14)
    public static int getNavigationBarHeight(@NonNull Fragment fragment) {
        if (fragment.getActivity() == null) {
            return 0;
        }
        return getNavigationBarHeight(fragment.getActivity());
    }

    @TargetApi(14)
    public static int getNavigationBarHeight(@NonNull android.app.Fragment fragment) {
        if (fragment.getActivity() == null) {
            return 0;
        }
        return getNavigationBarHeight(fragment.getActivity());
    }

    @TargetApi(14)
    public static int getNavigationBarWidth(@NonNull Activity activity) {
        SystemConfig config = new SystemConfig(activity);
        return config.getNavigationBarWidth();
    }

    @TargetApi(14)
    public static int getNavigationBarWidth(@NonNull Fragment fragment) {
        if (fragment.getActivity() == null) {
            return 0;
        }
        return getNavigationBarWidth(fragment.getActivity());
    }

    @TargetApi(14)
    public static int getNavigationBarWidth(@NonNull android.app.Fragment fragment) {
        if (fragment.getActivity() == null) {
            return 0;
        }
        return getNavigationBarWidth(fragment.getActivity());
    }


    @TargetApi(14)
    public static boolean isNavigationAtBottom(@NonNull Activity activity) {
        SystemConfig config = new SystemConfig(activity);
        return config.isNavigationAtBottom();
    }

    @TargetApi(14)
    public static boolean isNavigationAtBottom(@NonNull Fragment fragment) {
        if (fragment.getActivity() == null) {
            return false;
        }
        return isNavigationAtBottom(fragment.getActivity());
    }

    @TargetApi(14)
    public static boolean isNavigationAtBottom(@NonNull android.app.Fragment fragment) {
        if (fragment.getActivity() == null) {
            return false;
        }
        return isNavigationAtBottom(fragment.getActivity());
    }

    @TargetApi(14)
    public static int getStatusBarHeight(@NonNull Activity activity) {
        SystemConfig config = new SystemConfig(activity);
        return config.getStatusBarHeight();
    }

    @TargetApi(14)
    public static int getStatusBarHeight(@NonNull Fragment fragment) {
        if (fragment.getActivity() == null) {
            return 0;
        }
        return getStatusBarHeight(fragment.getActivity());
    }

    @TargetApi(14)
    public static int getStatusBarHeight(@NonNull android.app.Fragment fragment) {
        if (fragment.getActivity() == null) {
            return 0;
        }
        return getStatusBarHeight(fragment.getActivity());
    }

    @TargetApi(14)
    public static int getActionBarHeight(@NonNull Activity activity) {
        SystemConfig config = new SystemConfig(activity);
        return config.getActionBarHeight();
    }

    @TargetApi(14)
    public static int getActionBarHeight(@NonNull Fragment fragment) {
        if (fragment.getActivity() == null) {
            return 0;
        }
        return getActionBarHeight(fragment.getActivity());
    }

    @TargetApi(14)
    public static int getActionBarHeight(@NonNull android.app.Fragment fragment) {
        if (fragment.getActivity() == null) {
            return 0;
        }
        return getActionBarHeight(fragment.getActivity());
    }


    public static boolean hasNotchScreen(@NonNull Activity activity) {
        return NotchUtils.hasNotchScreen(activity);
    }

    public static boolean hasNotchScreen(@NonNull Fragment fragment) {
        if (fragment.getActivity() == null) {
            return false;
        }
        return hasNotchScreen(fragment.getActivity());
    }

    public static boolean hasNotchScreen(@NonNull android.app.Fragment fragment) {
        if (fragment.getActivity() == null) {
            return false;
        }
        return hasNotchScreen(fragment.getActivity());
    }


    public static boolean hasNotchScreen(@NonNull View view) {
        return NotchUtils.hasNotchScreen(view);
    }

    public static int getNotchHeight(@NonNull Activity activity) {
        if (hasNotchScreen(activity)) {
            return NotchUtils.getNotchHeight(activity);
        } else {
            return 0;
        }
    }

    public static int getNotchHeight(@NonNull Fragment fragment) {
        if (fragment.getActivity() == null) {
            return 0;
        }
        return getNotchHeight(fragment.getActivity());
    }

    public static int getNotchHeight(@NonNull android.app.Fragment fragment) {
        if (fragment.getActivity() == null) {
            return 0;
        }
        return getNotchHeight(fragment.getActivity());
    }

    public static void hideStatusBar(@NonNull Window window) {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void showStatusBar(@NonNull Window window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    Immersion(Activity activity) {
        mIsActivity = true;
        mActivity = activity;
        initCommonParameter(mActivity.getWindow());
    }

    Immersion(Fragment fragment) {
        mIsFragment = true;
        mActivity = fragment.getActivity();
        mSupportFragment = fragment;
        checkInitWithActivity();
        initCommonParameter(mActivity.getWindow());
    }

    Immersion(android.app.Fragment fragment) {
        mIsFragment = true;
        mActivity = fragment.getActivity();
        mFragment = fragment;
        checkInitWithActivity();
        initCommonParameter(mActivity.getWindow());
    }

    Immersion(DialogFragment dialogFragment) {
        mIsDialog = true;
        mIsDialogFragment = true;
        mActivity = dialogFragment.getActivity();
        mSupportFragment = dialogFragment;
        mDialog = dialogFragment.getDialog();
        checkInitWithActivity();
        initCommonParameter(mDialog.getWindow());
    }

    Immersion(android.app.DialogFragment dialogFragment) {
        mIsDialog = true;
        mIsDialogFragment = true;
        mActivity = dialogFragment.getActivity();
        mFragment = dialogFragment;
        mDialog = dialogFragment.getDialog();
        checkInitWithActivity();
        initCommonParameter(mDialog.getWindow());
    }

    Immersion(Activity activity, Dialog dialog) {
        mIsDialog = true;
        mActivity = activity;
        mDialog = dialog;
        checkInitWithActivity();
        initCommonParameter(mDialog.getWindow());
    }

    private void checkInitWithActivity() {
        if (mParentBar == null) {
            mParentBar = with(mActivity);
        }
        if (mParentBar != null && !mParentBar.mInitialized) {
            mParentBar.init();
        }
    }

    private void initCommonParameter(Window window) {
        mWindow = window;
        mCustomParams = new CustomParams();
        mDecorView = (ViewGroup) mWindow.getDecorView();
        mContentView = mDecorView.findViewById(android.R.id.content);
    }

    public Immersion transparentStatusBar() {
        mCustomParams.statusBarColor = Color.TRANSPARENT;
        return this;
    }

    public Immersion transparentNavigationBar() {
        mCustomParams.navigationBarColor = Color.TRANSPARENT;
        mCustomParams.fullScreen = true;
        return this;
    }

    public Immersion transparentBar() {
        mCustomParams.statusBarColor = Color.TRANSPARENT;
        mCustomParams.navigationBarColor = Color.TRANSPARENT;
        mCustomParams.fullScreen = true;
        return this;
    }

    public Immersion statusBarColor(@ColorRes int statusBarColor) {
        return this.statusBarColorInt(ContextCompat.getColor(mActivity, statusBarColor));
    }

    public Immersion statusBarColor(@ColorRes int statusBarColor,
                                    @FloatRange(from = 0f, to = 1f) float alpha) {
        return this.statusBarColorInt(ContextCompat.getColor(mActivity, statusBarColor), alpha);
    }

    public Immersion statusBarColor(@ColorRes int statusBarColor,
                                    @ColorRes int statusBarColorTransform,
                                    @FloatRange(from = 0f, to = 1f) float alpha) {
        return this.statusBarColorInt(ContextCompat.getColor(mActivity, statusBarColor),
                ContextCompat.getColor(mActivity, statusBarColorTransform),
                alpha);
    }

    public Immersion statusBarColor(String statusBarColor) {
        return this.statusBarColorInt(Color.parseColor(statusBarColor));
    }

    public Immersion statusBarColor(String statusBarColor,
                                    @FloatRange(from = 0f, to = 1f) float alpha) {
        return this.statusBarColorInt(Color.parseColor(statusBarColor), alpha);
    }

    public Immersion statusBarColor(String statusBarColor,
                                    String statusBarColorTransform,
                                    @FloatRange(from = 0f, to = 1f) float alpha) {
        return this.statusBarColorInt(Color.parseColor(statusBarColor),
                Color.parseColor(statusBarColorTransform),
                alpha);
    }

    public Immersion statusBarColorInt(@ColorInt int statusBarColor) {
        mCustomParams.statusBarColor = statusBarColor;
        return this;
    }

    public Immersion statusBarColorInt(@ColorInt int statusBarColor,
                                       @FloatRange(from = 0f, to = 1f) float alpha) {
        mCustomParams.statusBarColor = statusBarColor;
        mCustomParams.statusBarAlpha = alpha;
        return this;
    }

    public Immersion statusBarColorInt(@ColorInt int statusBarColor,
                                       @ColorInt int statusBarColorTransform,
                                       @FloatRange(from = 0f, to = 1f) float alpha) {
        mCustomParams.statusBarColor = statusBarColor;
        mCustomParams.statusBarColorTransform = statusBarColorTransform;
        mCustomParams.statusBarAlpha = alpha;
        return this;
    }

    public Immersion navigationBarColor(@ColorRes int navigationBarColor) {
        return this.navigationBarColorInt(ContextCompat.getColor(mActivity, navigationBarColor));
    }

    public Immersion navigationBarColor(@ColorRes int navigationBarColor,
                                        @FloatRange(from = 0f, to = 1f) float navigationAlpha) {
        return this.navigationBarColorInt(ContextCompat.getColor(mActivity, navigationBarColor), navigationAlpha);
    }

    public Immersion navigationBarColor(@ColorRes int navigationBarColor,
                                        @ColorRes int navigationBarColorTransform,
                                        @FloatRange(from = 0f, to = 1f) float navigationAlpha) {
        return this.navigationBarColorInt(ContextCompat.getColor(mActivity, navigationBarColor),
                ContextCompat.getColor(mActivity, navigationBarColorTransform), navigationAlpha);
    }

    public Immersion navigationBarColor(String navigationBarColor) {
        return this.navigationBarColorInt(Color.parseColor(navigationBarColor));
    }

    public Immersion navigationBarColor(String navigationBarColor,
                                        @FloatRange(from = 0f, to = 1f) float navigationAlpha) {
        return this.navigationBarColorInt(Color.parseColor(navigationBarColor), navigationAlpha);
    }

    public Immersion navigationBarColor(String navigationBarColor,
                                        String navigationBarColorTransform,
                                        @FloatRange(from = 0f, to = 1f) float navigationAlpha) {
        return this.navigationBarColorInt(Color.parseColor(navigationBarColor),
                Color.parseColor(navigationBarColorTransform), navigationAlpha);
    }

    public Immersion navigationBarColorInt(@ColorInt int navigationBarColor) {
        mCustomParams.navigationBarColor = navigationBarColor;
        return this;
    }

    public Immersion navigationBarColorInt(@ColorInt int navigationBarColor,
                                           @FloatRange(from = 0f, to = 1f) float navigationAlpha) {
        mCustomParams.navigationBarColor = navigationBarColor;
        mCustomParams.navigationBarAlpha = navigationAlpha;
        return this;
    }

    public Immersion navigationBarColorInt(@ColorInt int navigationBarColor,
                                           @ColorInt int navigationBarColorTransform,
                                           @FloatRange(from = 0f, to = 1f) float navigationAlpha) {
        mCustomParams.navigationBarColor = navigationBarColor;
        mCustomParams.navigationBarColorTransform = navigationBarColorTransform;
        mCustomParams.navigationBarAlpha = navigationAlpha;
        return this;
    }

    public Immersion barColor(@ColorRes int barColor) {
        return this.barColorInt(ContextCompat.getColor(mActivity, barColor));
    }

    public Immersion barColor(@ColorRes int barColor, @FloatRange(from = 0f, to = 1f) float barAlpha) {
        return this.barColorInt(ContextCompat.getColor(mActivity, barColor), barColor);
    }

    public Immersion barColor(@ColorRes int barColor,
                              @ColorRes int barColorTransform,
                              @FloatRange(from = 0f, to = 1f) float barAlpha) {
        return this.barColorInt(ContextCompat.getColor(mActivity, barColor),
                ContextCompat.getColor(mActivity, barColorTransform), barAlpha);
    }

    public Immersion barColor(String barColor) {
        return this.barColorInt(Color.parseColor(barColor));
    }

    public Immersion barColor(String barColor, @FloatRange(from = 0f, to = 1f) float barAlpha) {
        return this.barColorInt(Color.parseColor(barColor), barAlpha);
    }

    public Immersion barColor(String barColor,
                              String barColorTransform,
                              @FloatRange(from = 0f, to = 1f) float barAlpha) {
        return this.barColorInt(Color.parseColor(barColor), Color.parseColor(barColorTransform), barAlpha);
    }

    public Immersion barColorInt(@ColorInt int barColor) {
        mCustomParams.statusBarColor = barColor;
        mCustomParams.navigationBarColor = barColor;
        return this;
    }

    public Immersion barColorInt(@ColorInt int barColor, @FloatRange(from = 0f, to = 1f) float barAlpha) {
        mCustomParams.statusBarColor = barColor;
        mCustomParams.navigationBarColor = barColor;
        mCustomParams.statusBarAlpha = barAlpha;
        mCustomParams.navigationBarAlpha = barAlpha;
        return this;
    }

    public Immersion barColorInt(@ColorInt int barColor,
                                 @ColorInt int barColorTransform,
                                 @FloatRange(from = 0f, to = 1f) float barAlpha) {
        mCustomParams.statusBarColor = barColor;
        mCustomParams.navigationBarColor = barColor;

        mCustomParams.statusBarColorTransform = barColorTransform;
        mCustomParams.navigationBarColorTransform = barColorTransform;

        mCustomParams.statusBarAlpha = barAlpha;
        mCustomParams.navigationBarAlpha = barAlpha;
        return this;
    }

    public Immersion statusBarColorTransform(@ColorRes int statusBarColorTransform) {
        return this.statusBarColorTransformInt(ContextCompat.getColor(mActivity, statusBarColorTransform));
    }

    public Immersion statusBarColorTransform(String statusBarColorTransform) {
        return this.statusBarColorTransformInt(Color.parseColor(statusBarColorTransform));
    }

    public Immersion statusBarColorTransformInt(@ColorInt int statusBarColorTransform) {
        mCustomParams.statusBarColorTransform = statusBarColorTransform;
        return this;
    }

    public Immersion navigationBarColorTransform(@ColorRes int navigationBarColorTransform) {
        return this.navigationBarColorTransformInt(ContextCompat.getColor(mActivity, navigationBarColorTransform));
    }

    public Immersion navigationBarColorTransform(String navigationBarColorTransform) {
        return this.navigationBarColorTransformInt(Color.parseColor(navigationBarColorTransform));
    }

    public Immersion navigationBarColorTransformInt(@ColorInt int navigationBarColorTransform) {
        mCustomParams.navigationBarColorTransform = navigationBarColorTransform;
        return this;
    }

    public Immersion barColorTransform(@ColorRes int barColorTransform) {
        return this.barColorTransformInt(ContextCompat.getColor(mActivity, barColorTransform));
    }

    public Immersion barColorTransform(String barColorTransform) {
        return this.barColorTransformInt(Color.parseColor(barColorTransform));
    }

    public Immersion barColorTransformInt(@ColorInt int barColorTransform) {
        mCustomParams.statusBarColorTransform = barColorTransform;
        mCustomParams.navigationBarColorTransform = barColorTransform;
        return this;
    }

    public Immersion addViewSupportTransformColor(View view) {
        return this.addViewSupportTransformColorInt(view, mCustomParams.statusBarColorTransform);
    }

    public Immersion addViewSupportTransformColor(View view, @ColorRes int viewColorAfterTransform) {
        return this.addViewSupportTransformColorInt(view, ContextCompat.getColor(mActivity, viewColorAfterTransform));
    }

    public Immersion addViewSupportTransformColor(View view, @ColorRes int viewColorBeforeTransform,
                                                  @ColorRes int viewColorAfterTransform) {
        return this.addViewSupportTransformColorInt(view,
                ContextCompat.getColor(mActivity, viewColorBeforeTransform),
                ContextCompat.getColor(mActivity, viewColorAfterTransform));
    }

    public Immersion addViewSupportTransformColor(View view, String viewColorAfterTransform) {
        return this.addViewSupportTransformColorInt(view, Color.parseColor(viewColorAfterTransform));
    }

    public Immersion addViewSupportTransformColor(View view, String viewColorBeforeTransform,
                                                  String viewColorAfterTransform) {
        return this.addViewSupportTransformColorInt(view,
                Color.parseColor(viewColorBeforeTransform),
                Color.parseColor(viewColorAfterTransform));
    }

    public Immersion addViewSupportTransformColorInt(View view, @ColorInt int viewColorAfterTransform) {
        if (view == null) {
            throw new IllegalArgumentException("invalid params.");
        }
        Map<Integer, Integer> map = new HashMap<>();
        map.put(mCustomParams.statusBarColor, viewColorAfterTransform);
        mCustomParams.viewMap.put(view, map);
        return this;
    }

    public Immersion addViewSupportTransformColorInt(View view, @ColorInt int viewColorBeforeTransform,
                                                     @ColorInt int viewColorAfterTransform) {
        if (view == null) {
            throw new IllegalArgumentException("invalid params.");
        }
        Map<Integer, Integer> map = new HashMap<>();
        map.put(viewColorBeforeTransform, viewColorAfterTransform);
        mCustomParams.viewMap.put(view, map);
        return this;
    }

    public Immersion viewAlpha(@FloatRange(from = 0f, to = 1f) float viewAlpha) {
        mCustomParams.viewAlpha = viewAlpha;
        return this;
    }

    public Immersion removeSupportView(View view) {
        if (view == null) {
            throw new IllegalArgumentException("invalid params.");
        }
        Map<Integer, Integer> map = mCustomParams.viewMap.get(view);
        if (map != null && map.size() != 0) {
            mCustomParams.viewMap.remove(view);
        }
        return this;
    }

    public Immersion removeSupportAllView() {
        if (mCustomParams.viewMap.size() != 0) {
            mCustomParams.viewMap.clear();
        }
        return this;
    }

    public Immersion fullScreen(boolean isFullScreen) {
        mCustomParams.fullScreen = isFullScreen;
        return this;
    }

    public Immersion statusBarAlpha(@FloatRange(from = 0f, to = 1f) float statusAlpha) {
        mCustomParams.statusBarAlpha = statusAlpha;
        mCustomParams.statusBarTempAlpha = statusAlpha;
        return this;
    }

    public Immersion navigationBarAlpha(@FloatRange(from = 0f, to = 1f) float navigationAlpha) {
        mCustomParams.navigationBarAlpha = navigationAlpha;
        mCustomParams.navigationBarTempAlpha = navigationAlpha;
        return this;
    }

    public Immersion barAlpha(@FloatRange(from = 0f, to = 1f) float barAlpha) {
        mCustomParams.statusBarAlpha = barAlpha;
        mCustomParams.statusBarTempAlpha = barAlpha;
        mCustomParams.navigationBarAlpha = barAlpha;
        mCustomParams.navigationBarTempAlpha = barAlpha;
        return this;
    }

    public Immersion autoDarkModeEnable(boolean isEnable) {
        return this.autoDarkModeEnable(isEnable, 0.2f);
    }

    public Immersion autoDarkModeEnable(boolean isEnable, @FloatRange(from = 0f, to = 1f) float autoDarkModeAlpha) {
        mCustomParams.autoStatusBarDarkModeEnable = isEnable;
        mCustomParams.autoStatusBarDarkModeAlpha = autoDarkModeAlpha;
        mCustomParams.autoNavigationBarDarkModeEnable = isEnable;
        mCustomParams.autoNavigationBarDarkModeAlpha = autoDarkModeAlpha;
        return this;
    }

    public Immersion autoStatusBarDarkModeEnable(boolean isEnable) {
        return this.autoStatusBarDarkModeEnable(isEnable, 0.2f);
    }

    public Immersion autoStatusBarDarkModeEnable(boolean isEnable, @FloatRange(from = 0f, to = 1f) float autoDarkModeAlpha) {
        mCustomParams.autoStatusBarDarkModeEnable = isEnable;
        mCustomParams.autoStatusBarDarkModeAlpha = autoDarkModeAlpha;
        return this;
    }

    public Immersion autoNavigationBarDarkModeEnable(boolean isEnable) {
        return this.autoNavigationBarDarkModeEnable(isEnable, 0.2f);
    }

    public Immersion autoNavigationBarDarkModeEnable(boolean isEnable, @FloatRange(from = 0f, to = 1f) float autoDarkModeAlpha) {
        mCustomParams.autoNavigationBarDarkModeEnable = isEnable;
        mCustomParams.autoNavigationBarDarkModeAlpha = autoDarkModeAlpha;
        return this;
    }

    public Immersion statusBarDarkFont(boolean isDarkFont) {
        return statusBarDarkFont(isDarkFont, 0.2f);
    }

    public Immersion statusBarDarkFont(boolean isDarkFont, @FloatRange(from = 0f, to = 1f) float statusAlpha) {
        mCustomParams.statusBarDarkFont = isDarkFont;
        if (isDarkFont && !isSupportStatusBarDarkFont()) {
            mCustomParams.statusBarAlpha = statusAlpha;
        } else {
            mCustomParams.flymeOSStatusBarFontColor = mCustomParams.flymeOSStatusBarFontTempColor;
            mCustomParams.statusBarAlpha = mCustomParams.statusBarTempAlpha;
        }
        return this;
    }

    public Immersion navigationBarDarkIcon(boolean isDarkIcon) {
        return navigationBarDarkIcon(isDarkIcon, 0.2f);
    }

    public Immersion navigationBarDarkIcon(boolean isDarkIcon, @FloatRange(from = 0f, to = 1f) float navigationAlpha) {
        mCustomParams.navigationBarDarkIcon = isDarkIcon;
        if (isDarkIcon && !isSupportNavigationIconDark()) {
            mCustomParams.navigationBarAlpha = navigationAlpha;
        } else {
            mCustomParams.navigationBarAlpha = mCustomParams.navigationBarTempAlpha;
        }
        return this;
    }

    public Immersion flymeOSStatusBarFontColor(@ColorRes int flymeOSStatusBarFontColor) {
        mCustomParams.flymeOSStatusBarFontColor = ContextCompat.getColor(mActivity, flymeOSStatusBarFontColor);
        mCustomParams.flymeOSStatusBarFontTempColor = mCustomParams.flymeOSStatusBarFontColor;
        return this;
    }

    public Immersion flymeOSStatusBarFontColor(String flymeOSStatusBarFontColor) {
        mCustomParams.flymeOSStatusBarFontColor = Color.parseColor(flymeOSStatusBarFontColor);
        mCustomParams.flymeOSStatusBarFontTempColor = mCustomParams.flymeOSStatusBarFontColor;
        return this;
    }

    public Immersion flymeOSStatusBarFontColorInt(@ColorInt int flymeOSStatusBarFontColor) {
        mCustomParams.flymeOSStatusBarFontColor = flymeOSStatusBarFontColor;
        mCustomParams.flymeOSStatusBarFontTempColor = mCustomParams.flymeOSStatusBarFontColor;
        return this;
    }

    public Immersion hideBar(BarHide barHide) {
        mCustomParams.barHide = barHide;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT || OSUtils.isEMUI3_x()) {
            mCustomParams.hideNavigationBar = (mCustomParams.barHide == BarHide.FLAG_HIDE_NAVIGATION_BAR) ||
                    (mCustomParams.barHide == BarHide.FLAG_HIDE_BAR);
        }
        return this;
    }

    public Immersion applySystemFits(boolean applySystemFits) {
        mCustomParams.fitsLayoutOverlapEnable = !applySystemFits;
        setFitsSystemWindows(mActivity, applySystemFits);
        return this;
    }

    public Immersion fitsSystemWindows(boolean fits) {
        mCustomParams.fits = fits;
        if (mCustomParams.fits) {
            if (mFitsStatusBarType == FLAG_FITS_DEFAULT) {
                mFitsStatusBarType = FLAG_FITS_SYSTEM_WINDOWS;
            }
        } else {
            mFitsStatusBarType = FLAG_FITS_DEFAULT;
        }
        return this;
    }

    public Immersion fitsSystemWindows(boolean fits, @ColorRes int contentColor) {
        return fitsSystemWindowsInt(fits, ContextCompat.getColor(mActivity, contentColor));
    }

    public Immersion fitsSystemWindows(boolean fits, @ColorRes int contentColor
            , @ColorRes int contentColorTransform, @FloatRange(from = 0f, to = 1f) float contentAlpha) {
        return fitsSystemWindowsInt(fits, ContextCompat.getColor(mActivity, contentColor),
                ContextCompat.getColor(mActivity, contentColorTransform), contentAlpha);
    }

    public Immersion fitsSystemWindowsInt(boolean fits, @ColorInt int contentColor) {
        return fitsSystemWindowsInt(fits, contentColor, Color.BLACK, 0);
    }

    public Immersion fitsSystemWindowsInt(boolean fits, @ColorInt int contentColor
            , @ColorInt int contentColorTransform, @FloatRange(from = 0f, to = 1f) float contentAlpha) {
        mCustomParams.fits = fits;
        mCustomParams.contentColor = contentColor;
        mCustomParams.contentColorTransform = contentColorTransform;
        mCustomParams.contentAlpha = contentAlpha;
        if (mCustomParams.fits) {
            if (mFitsStatusBarType == FLAG_FITS_DEFAULT) {
                mFitsStatusBarType = FLAG_FITS_SYSTEM_WINDOWS;
            }
        } else {
            mFitsStatusBarType = FLAG_FITS_DEFAULT;
        }
        mContentView.setBackgroundColor(ColorUtils.blendARGB(mCustomParams.contentColor,
                mCustomParams.contentColorTransform, mCustomParams.contentAlpha));
        return this;
    }

    public Immersion fitsLayoutOverlapEnable(boolean fitsLayoutOverlapEnable) {
        mCustomParams.fitsLayoutOverlapEnable = fitsLayoutOverlapEnable;
        return this;
    }

    public Immersion statusBarView(View view) {
        if (view == null) {
            return this;
        }
        mCustomParams.statusBarView = view;
        if (mFitsStatusBarType == FLAG_FITS_DEFAULT) {
            mFitsStatusBarType = FLAG_FITS_STATUS;
        }
        return this;
    }

    public Immersion statusBarView(@IdRes int viewId) {
        return statusBarView(mActivity.findViewById(viewId));
    }

    public Immersion statusBarView(@IdRes int viewId, View rootView) {
        return statusBarView(rootView.findViewById(viewId));
    }

    public Immersion titleBar(View view) {
        if (view == null) {
            return this;
        }
        return titleBar(view, true);
    }

    public Immersion titleBar(View view, boolean statusBarColorTransformEnable) {
        if (view == null) {
            return this;
        }
        if (mFitsStatusBarType == FLAG_FITS_DEFAULT) {
            mFitsStatusBarType = FLAG_FITS_TITLE;
        }
        mCustomParams.titleBarView = view;
        mCustomParams.statusBarColorEnabled = statusBarColorTransformEnable;
        return this;
    }

    public Immersion titleBar(@IdRes int viewId) {
        return titleBar(viewId, true);
    }

    public Immersion titleBar(@IdRes int viewId, boolean statusBarColorTransformEnable) {
        if (mSupportFragment != null && mSupportFragment.getView() != null) {
            return titleBar(mSupportFragment.getView().findViewById(viewId), statusBarColorTransformEnable);
        } else if (mFragment != null && mFragment.getView() != null) {
            return titleBar(mFragment.getView().findViewById(viewId), statusBarColorTransformEnable);
        } else {
            return titleBar(mActivity.findViewById(viewId), statusBarColorTransformEnable);
        }
    }

    public Immersion titleBar(@IdRes int viewId, View rootView) {
        return titleBar(rootView.findViewById(viewId), true);
    }

    public Immersion titleBar(@IdRes int viewId, View rootView, boolean statusBarColorTransformEnable) {
        return titleBar(rootView.findViewById(viewId), statusBarColorTransformEnable);
    }

    public Immersion titleBarMarginTop(@IdRes int viewId) {
        if (mSupportFragment != null && mSupportFragment.getView() != null) {
            return titleBarMarginTop(mSupportFragment.getView().findViewById(viewId));
        } else if (mFragment != null && mFragment.getView() != null) {
            return titleBarMarginTop(mFragment.getView().findViewById(viewId));
        } else {
            return titleBarMarginTop(mActivity.findViewById(viewId));
        }
    }

    public Immersion titleBarMarginTop(@IdRes int viewId, View rootView) {
        return titleBarMarginTop(rootView.findViewById(viewId));
    }

    public Immersion titleBarMarginTop(View view) {
        if (view == null) {
            return this;
        }
        if (mFitsStatusBarType == FLAG_FITS_DEFAULT) {
            mFitsStatusBarType = FLAG_FITS_TITLE_MARGIN_TOP;
        }
        mCustomParams.titleBarView = view;
        return this;
    }


    public Immersion supportActionBar(boolean isSupportActionBar) {
        mCustomParams.isSupportActionBar = isSupportActionBar;
        return this;
    }


    public Immersion statusBarColorTransformEnable(boolean statusBarColorTransformEnable) {
        mCustomParams.statusBarColorEnabled = statusBarColorTransformEnable;
        return this;
    }


    public Immersion reset() {
        mCustomParams = new CustomParams();
        mFitsStatusBarType = FLAG_FITS_DEFAULT;
        return this;
    }


    public Immersion addTag(String tag) {
        if (isEmpty(tag)) {
            throw new IllegalArgumentException("tag is null.");
        }
        CustomParams customParams = mCustomParams.clone();
        mTagMap.put(tag, customParams);
        return this;
    }


    public Immersion getTag(String tag) {
        if (isEmpty(tag)) {
            throw new IllegalArgumentException("tag is null.");
        }
        CustomParams customParams = mTagMap.get(tag);
        if (customParams != null) {
            mCustomParams = customParams.clone();
        }
        return this;
    }


    public Immersion keyboardEnable(boolean enable) {
        return keyboardEnable(enable, mCustomParams.keyboardMode);
    }


    public Immersion keyboardEnable(boolean enable, int keyboardMode) {
        mCustomParams.keyboardEnable = enable;
        mCustomParams.keyboardMode = keyboardMode;
        mKeyboardTempEnable = enable;
        return this;
    }


    public Immersion keyboardMode(int keyboardMode) {
        mCustomParams.keyboardMode = keyboardMode;
        return this;
    }

    public Immersion setOnNavigationBarListener(OnNavigationBarListener onNavigationBarListener) {
        if (onNavigationBarListener != null) {
            if (mCustomParams.onNavigationBarListener == null) {
                mCustomParams.onNavigationBarListener = onNavigationBarListener;
                NavigationBarObserver.getInstance().addOnNavigationBarListener(mCustomParams.onNavigationBarListener);
            }
        } else {
            if (mCustomParams.onNavigationBarListener != null) {
                NavigationBarObserver.getInstance().removeOnNavigationBarListener(mCustomParams.onNavigationBarListener);
                mCustomParams.onNavigationBarListener = null;
            }
        }
        return this;
    }


    public Immersion setOnBarListener(OnBarListener onBarListener) {
        if (onBarListener != null) {
            if (mCustomParams.onBarListener == null) {
                mCustomParams.onBarListener = onBarListener;
            }
        } else {
            if (mCustomParams.onBarListener != null) {
                mCustomParams.onBarListener = null;
            }
        }
        return this;
    }


    public Immersion navigationBarEnable(boolean navigationBarEnable) {
        mCustomParams.navigationBarEnable = navigationBarEnable;
        return this;
    }


    public Immersion navigationBarWithKitkatEnable(boolean navigationBarWithKitkatEnable) {
        mCustomParams.navigationBarWithKitkatEnable = navigationBarWithKitkatEnable;
        return this;
    }


    public Immersion navigationBarWithEMUI3Enable(boolean navigationBarWithEMUI3Enable) {
        if (OSUtils.isEMUI3_x()) {
            mCustomParams.navigationBarWithEMUI3Enable = navigationBarWithEMUI3Enable;
            mCustomParams.navigationBarWithKitkatEnable = navigationBarWithEMUI3Enable;
        }
        return this;
    }


    public Immersion barEnable(boolean barEnable) {
        mCustomParams.barEnable = barEnable;
        return this;
    }

    private static RequestManagerRetriever getRetriever() {
        return RequestManagerRetriever.getInstance();
    }

    private static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }
}
