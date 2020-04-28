package com.viewset.immersion;

import android.app.Application;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.RestrictTo;

import java.util.ArrayList;

import static com.viewset.immersion.Constants.IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW;
import static com.viewset.immersion.Constants.IMMERSION_MIUI_NAVIGATION_BAR_HIDE_SHOW;

@RestrictTo(RestrictTo.Scope.LIBRARY)
final class NavigationBarObserver extends ContentObserver {

    private ArrayList<OnNavigationBarListener> mListeners;
    private Application mApplication;
    private Boolean mIsRegister = false;

    static NavigationBarObserver getInstance() {
        return NavigationBarObserverInstance.INSTANCE;
    }

    private NavigationBarObserver() {
        super(new Handler(Looper.getMainLooper()));
    }

    void register(Application application) {
        this.mApplication = application;
        if (mApplication != null && mApplication.getContentResolver() != null && !mIsRegister) {
            Uri uri = null;
            if (OSUtils.isMIUI()) {
                uri = Settings.Global.getUriFor(IMMERSION_MIUI_NAVIGATION_BAR_HIDE_SHOW);
            } else if (OSUtils.isEMUI()) {
                if (OSUtils.isEMUI3_x()) {
                    uri = Settings.System.getUriFor(IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW);
                } else {
                    uri = Settings.Global.getUriFor(IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW);
                }
            }
            if (uri != null) {
                mApplication.getContentResolver().registerContentObserver(uri, true, this);
                mIsRegister = true;
            }
        }
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (mApplication != null && mApplication.getContentResolver() != null && mListeners != null && !mListeners.isEmpty()) {
            int show = 0;
            if (OSUtils.isMIUI()) {
                show = Settings.Global.getInt(mApplication.getContentResolver(), IMMERSION_MIUI_NAVIGATION_BAR_HIDE_SHOW, 0);
            } else if (OSUtils.isEMUI()) {
                if (OSUtils.isEMUI3_x()) {
                    show = Settings.System.getInt(mApplication.getContentResolver(), IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW, 0);
                } else {
                    show = Settings.Global.getInt(mApplication.getContentResolver(), IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW, 0);
                }
            }
            for (OnNavigationBarListener onNavigationBarListener : mListeners) {
                onNavigationBarListener.onNavigationBarChange(show != 1);
            }
        }
    }

    void addOnNavigationBarListener(OnNavigationBarListener listener) {
        if (listener == null) {
            return;
        }
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    void removeOnNavigationBarListener(OnNavigationBarListener listener) {
        if (listener == null || mListeners == null) {
            return;
        }
        mListeners.remove(listener);
    }

    private static class NavigationBarObserverInstance {
        private static final NavigationBarObserver INSTANCE = new NavigationBarObserver();
    }

}
