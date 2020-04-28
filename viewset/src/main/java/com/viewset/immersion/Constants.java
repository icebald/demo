package com.viewset.immersion;

import androidx.annotation.RestrictTo;

import com.viewset.R;

@RestrictTo(RestrictTo.Scope.LIBRARY)
class Constants {
    static final int IMMERSION_ID_STATUS_BAR_VIEW = R.id.immersion_status_bar_view;
    
    static final int IMMERSION_ID_NAVIGATION_BAR_VIEW = R.id.immersion_navigation_bar_view;
    
    static final String IMMERSION_STATUS_BAR_HEIGHT = "status_bar_height";
    
    static final String IMMERSION_NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    
    static final String IMMERSION_NAVIGATION_BAR_HEIGHT_LANDSCAPE = "navigation_bar_height_landscape";
    
    static final String IMMERSION_NAVIGATION_BAR_WIDTH = "navigation_bar_width";
    
    static final String IMMERSION_MIUI_NAVIGATION_BAR_HIDE_SHOW = "force_fsg_nav_bar";
    
    static final String IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW = "navigationbar_is_min";
    
    static final String IMMERSION_MIUI_STATUS_BAR_DARK = "EXTRA_FLAG_STATUS_BAR_DARK_MODE";
    
    static final String IMMERSION_MIUI_NAVIGATION_BAR_DARK = "EXTRA_FLAG_NAVIGATION_BAR_DARK_MODE";

    static final int IMMERSION_BOUNDARY_COLOR = 0xFFBABABA;

    static final int FLAG_FITS_DEFAULT = 0X00;
    
    static final int FLAG_FITS_TITLE = 0X01;
    
    static final int FLAG_FITS_TITLE_MARGIN_TOP = 0X02;
    
    static final int FLAG_FITS_STATUS = 0X03;
    
    static final int FLAG_FITS_SYSTEM_WINDOWS = 0X04;
}
