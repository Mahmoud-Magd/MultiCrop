package com.magd.multicrop.enums;



// =========================================================
// MaskMode
// =========================================================

// Controls how NON-CROPPED pixels are handled
// during MERGED export mode ONLY.

// IMPORTANT:
    // This enum affects ONLY pixels OUTSIDE crop areas.
    // Pixels INSIDE crop areas remain unchanged.

// =========================================================
// EXAMPLES:
    // TRANSPARENT:
        // Outside pixels become transparent.

    // BLACK:
        // Outside pixels become black.

// =========================================================

public enum MaskMode {
    
    
    
    
    
    // =========================================================
    // TRANSPARENT
    // =========================================================
    
    // Pixels outside crop areas become transparent.
    // Requires:
        // Bitmap.Config.ARGB_8888
    TRANSPARENT,
    
    
    
    
    
    // =========================================================
    // BLACK
    // =========================================================
    
    // Pixels outside crop areas become black.
    BLACK
    
    
    
    
    
}


