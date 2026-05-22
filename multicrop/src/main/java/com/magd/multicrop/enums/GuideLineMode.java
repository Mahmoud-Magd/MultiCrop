package com.magd.multicrop.enums;



// =========================================================
// GuideLineMode
// =========================================================
// Controls when the internal 3x3 crop guidelines are visible.
// The guidelines divide the CropArea into 9 equal regions.
//
// Modes:
    // OFF → Never visible.
    // ON → Always visible.
    // ON_TOUCH → Visible only while:
        // - Moving the CropArea
        // - Resizing the CropArea
        // - Touching inside the CropArea
// =========================================================

public enum GuideLineMode {
    
    
    
    // Never draw guidelines.
    OFF,
    
    // Always draw guidelines.
    ON,
    
    // Draw only during user interaction.
    ON_TOUCH
    
    
    
}


