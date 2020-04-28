package com.viewset.immersion;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RestrictTo;

import com.viewset.R;

import static com.viewset.immersion.Constants.IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW;
import static com.viewset.immersion.Constants.IMMERSION_MIUI_NAVIGATION_BAR_HIDE_SHOW;
import static com.viewset.immersion.Constants.IMMERSION_NAVIGATION_BAR_HEIGHT;
import static com.viewset.immersion.Constants.IMMERSION_NAVIGATION_BAR_HEIGHT_LANDSCAPE;
import static com.viewset.immersion.Constants.IMMERSION_NAVIGATION_BAR_WIDTH;
import static com.viewset.immersion.Constants.IMMERSION_STATUS_BAR_HEIGHT;

@RestrictTo(RestrictTo.Scope.LIBRARY)
class SystemConfig {
    private final int mStatusBarHeight;
    private final int mActionBarHeight;
    private final boolean mHasNavigationBar;
    private final int mNavigationBarHeight;
    private final int mNavigationBarWidth;
    private final boolean mInPortrait;
    private final float mSmallestWidthDp;

    SystemConfig(Activity activity) {
        Resources res = activity.getResources();
        mInPortrait = (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        mSmallestWidthDp = getSmallestWidthDp(activity);
        mStatusBarHeight = getInternalDimensionSize(activity, IMMERSION_STATUS_BAR_HEIGHT);
        mActionBarHeight = getActionBarHeight(activity);
        mNavigationBarHeight = getNavigationBarHeight(activity);
        mNavigationBarWidth = getNavigationBarWidth(activity);
        mHasNavigationBar = (mNavigationBarHeight > 0);
    }

    @TargetApi(14)
    private int getActionBarHeight(Activity activity) {
        int result = 0;
        View actionBar = activity.getWindow().findViewById(R.id.action_bar_container);
        if (actionBar != null) {
            result = actionBar.getMeasuredHeight();
        }
        if (result == 0) {
            TypedValue tv = new TypedValue();
            activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
            result = TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }
        return result;
    }

    @TargetApi(14)
    private int getNavigationBarHeight(Context context) {
        int result = 0;
        if (hasNavBar((Activity) context)) {
            String key;
            if (mInPortrait) {
                key = IMMERSION_NAVIGATION_BAR_HEIGHT;
            } else {
                key = IMMERSION_NAVIGATION_BAR_HEIGHT_LANDSCAPE;
            }
            return getInternalDimensionSize(context, key);
        }
        return result;
    }

    @TargetApi(14)
    private int getNavigationBarWidth(Context context) {
        int result = 0;
        if (hasNavBar((Activity) context)) {
            return getInternalDimensionSize(context, IMMERSION_NAVIGATION_BAR_WIDTH);
        }
        return result;
    }

    @TargetApi(14)
    private boolean hasNavBar(Activity activity) {
        if (Settings.Global.getInt(activity.getContentResolver(), IMMERSION_MIUI_NAVIGATION_BAR_HIDE_SHOW, 0) != 0) {
            return false;
        }
        if (OSUtils.isEMUI()) {
            if (OSUtils.isEMUI3_x()) {
                if (Settings.System.getInt(activity.getContentResolver(), IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW, 0) != 0) {
                    return false;
                }
            } else {
                if (Settings.Global.getInt(activity.getContentResolver(), IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW, 0) != 0) {
                    return false;
                }
            }
        }
        WindowManager windowManager = activity.getWindowManager();
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    private int getInternalDimensionSize(Context context, String key) {
        int result = 0;
        try {
            int resourceId = Resources.getSystem().getIdentifier(key, "dimen", "android");
            if (resourceId > 0) {
                int sizeOne = context.getResources().getDimensionPixelSize(resourceId);
                int sizeTwo = Resources.getSystem().getDimensionPixelSize(resourceId);

                if (sizeTwo >= sizeOne) {
                    return sizeTwo;
                } else {
                    float densityOne = context.getResources().getDisplayMetrics().density;
                    float densityTwo = Resources.getSystem().getDisplayMetrics().density;
                    float f = sizeOne * densityTwo / densityOne;
                    return (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
                }
            }
        } catch (Resources.NotFoundException ignored) {
            return 0;
        }
        return result;
    }

    @SuppressLint("NewApi")
    private float getSmallestWidthDp(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        float widthDp = metrics.widthPixels / metrics.density;
        float heightDp = metrics.heightPixels / metrics.density;
        return Math.min(widthDp, heightDp);
    }

    boolean isNavigationAtBottom() {
        return (mSmallestWidthDp >= 600 || mInPortrait);
    }

    int getStatusBarHeight() {
        return mStatusBarHeight;
    }

    int getActionBarHeight() {
        return mActionBarHeight;
    }

    boolean hasNavigationBar() {
        return mHasNavigationBar;
    }

    int getNavigationBarHeight() {
        return mNavigationBarHeight;
    }

    int getNavigationBarWidth() {
        return mNavigationBarWidth;
    }
}