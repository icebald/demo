package com.viewset.immersion;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class BarProperties {
    private boolean portrait;
    private boolean landscapeLeft;
    private boolean landscapeRight;
    private boolean notchScreen;
    private boolean hasNavigationBar;
    private int statusBarHeight;
    private int navigationBarHeight;
    private int navigationBarWidth;
    private int notchHeight;
    private int actionBarHeight;

    public boolean isPortrait() {
        return portrait;
    }

    void setPortrait(boolean portrait) {
        this.portrait = portrait;
    }

    public boolean isLandscapeLeft() {
        return landscapeLeft;
    }

    void setLandscapeLeft(boolean landscapeLeft) {
        this.landscapeLeft = landscapeLeft;
    }

    public boolean isLandscapeRight() {
        return landscapeRight;
    }

    void setLandscapeRight(boolean landscapeRight) {
        this.landscapeRight = landscapeRight;
    }

    public boolean isNotchScreen() {
        return notchScreen;
    }

    void setNotchScreen(boolean notchScreen) {
        this.notchScreen = notchScreen;
    }

    public boolean hasNavigationBar() {
        return hasNavigationBar;
    }

    void setNavigationBar(boolean hasNavigationBar) {
        this.hasNavigationBar = hasNavigationBar;
    }

    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    void setStatusBarHeight(int statusBarHeight) {
        this.statusBarHeight = statusBarHeight;
    }

    public int getNavigationBarHeight() {
        return navigationBarHeight;
    }

    void setNavigationBarHeight(int navigationBarHeight) {
        this.navigationBarHeight = navigationBarHeight;
    }

    public int getNavigationBarWidth() {
        return navigationBarWidth;
    }

    void setNavigationBarWidth(int navigationBarWidth) {
        this.navigationBarWidth = navigationBarWidth;
    }

    public int getNotchHeight() {
        return notchHeight;
    }

    void setNotchHeight(int notchHeight) {
        this.notchHeight = notchHeight;
    }

    public int getActionBarHeight() {
        return actionBarHeight;
    }

    void setActionBarHeight(int actionBarHeight) {
        this.actionBarHeight = actionBarHeight;
    }
}
