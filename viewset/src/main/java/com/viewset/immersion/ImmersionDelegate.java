package com.viewset.immersion;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.view.Surface;

import androidx.annotation.RestrictTo;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

@RestrictTo(RestrictTo.Scope.LIBRARY)
class ImmersionDelegate implements Runnable {

    private Immersion mImmersion;
    private BarProperties mBarProperties;
    private OnBarListener mOnBarListener;
    private int mNotchHeight;

    ImmersionDelegate(Object o) {
        if (o instanceof Activity) {
            if (mImmersion == null) {
                mImmersion = new Immersion((Activity) o);
            }
        } else if (o instanceof Fragment) {
            if (mImmersion == null) {
                if (o instanceof DialogFragment) {
                    mImmersion = new Immersion((DialogFragment) o);
                } else {
                    mImmersion = new Immersion((Fragment) o);
                }
            }
        } else if (o instanceof android.app.Fragment) {
            if (mImmersion == null) {
                if (o instanceof android.app.DialogFragment) {
                    mImmersion = new Immersion((android.app.DialogFragment) o);
                } else {
                    mImmersion = new Immersion((android.app.Fragment) o);
                }
            }
        }
    }

    ImmersionDelegate(Activity activity, Dialog dialog) {
        if (mImmersion == null) {
            mImmersion = new Immersion(activity, dialog);
        }
    }

    public Immersion get() {
        return mImmersion;
    }

    void onActivityCreated(Configuration configuration) {
        barChanged(configuration);
    }

    void onResume() {
        if (mImmersion != null) {
            mImmersion.onResume();
        }
    }

    void onDestroy() {
        mBarProperties = null;
        if (mImmersion != null) {
            mImmersion.onDestroy();
            mImmersion = null;
        }
    }

    void onConfigurationChanged(Configuration newConfig) {
        if (mImmersion != null) {
            mImmersion.onConfigurationChanged(newConfig);
            barChanged(newConfig);
        }
    }

    private void barChanged(Configuration configuration) {
        if (mImmersion != null && mImmersion.initialized()) {
            mOnBarListener = mImmersion.getBarParams().onBarListener;
            if (mOnBarListener != null) {
                final Activity activity = mImmersion.getActivity();
                if (mBarProperties == null) {
                    mBarProperties = new BarProperties();
                }
                mBarProperties.setPortrait(configuration.orientation == Configuration.ORIENTATION_PORTRAIT);
                int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                if (rotation == Surface.ROTATION_90) {
                    mBarProperties.setLandscapeLeft(true);
                    mBarProperties.setLandscapeRight(false);
                } else if (rotation == Surface.ROTATION_270) {
                    mBarProperties.setLandscapeLeft(false);
                    mBarProperties.setLandscapeRight(true);
                } else {
                    mBarProperties.setLandscapeLeft(false);
                    mBarProperties.setLandscapeRight(false);
                }
                activity.getWindow().getDecorView().post(this);
            }
        }
    }

    @Override
    public void run() {
        if (mImmersion != null && mImmersion.getActivity() != null) {
            Activity activity = mImmersion.getActivity();
            SystemConfig systemConfig = new SystemConfig(activity);
            mBarProperties.setStatusBarHeight(systemConfig.getStatusBarHeight());
            mBarProperties.setNavigationBar(systemConfig.hasNavigationBar());
            mBarProperties.setNavigationBarHeight(systemConfig.getNavigationBarHeight());
            mBarProperties.setNavigationBarWidth(systemConfig.getNavigationBarWidth());
            mBarProperties.setActionBarHeight(systemConfig.getActionBarHeight());
            boolean notchScreen = NotchUtils.hasNotchScreen(activity);
            mBarProperties.setNotchScreen(notchScreen);
            if (notchScreen && mNotchHeight == 0) {
                mNotchHeight = NotchUtils.getNotchHeight(activity);
                mBarProperties.setNotchHeight(mNotchHeight);
            }
            mOnBarListener.onBarChange(mBarProperties);
        }
    }
}
