package com.magd.multicrop.utils;

import com.magd.multicrop.central.RtVault;

// =========================================================
// DimensUtils
// =========================================================

// Utility helpers for dimension conversions.

// IMPORTANT:
    // Android internally renders using px.
    // Designers/devs usually think in dp.


// =========================================================

public final class DimensUtils {
    
    
    
    
    
    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private DimensUtils() {}
    
    
    
    
    
    // =========================================================
    // PUBLIC METHODS
    // =========================================================
    public static float dpToPx (float dp) { return dp * RtVault.getDensity(); } // px = dp * density
    public static float pxToDp (float px) { return px / RtVault.getDensity(); } // dp = px / density
    
    
    
    
    
}



