package com.viewset.immersion;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface OnBarListener {
    void onBarChange(BarProperties barProperties);
}
