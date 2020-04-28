package com.viewset.immersion;

import androidx.annotation.RestrictTo;

import com.viewset.ui.VActivity;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ImmersionActivity extends VActivity {
    private Immersion mImmersion;

    public ImmersionActivity initImmersion() {
        mImmersion = new Immersion(this);
        return this;
    }

    public ImmersionActivity statusBarDarkFont(boolean isDark) {
        mImmersion.statusBarDarkFont(isDark);
        return this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mImmersion) {
            mImmersion.onResume();
        }
    }
}
