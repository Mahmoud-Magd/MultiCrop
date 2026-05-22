package com.magd.multicrop.render;



import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.magd.multicrop.tint.TintLayer;



// =========================================================
// TintRenderer
// =========================================================

// Draws the tint overlay onto a canvas.

// =========================================================
// RESPONSIBILITY:

// TintRenderer does ONE thing:
    // Takes a TintLayer + a mask Path
    // Draws the tint with holes punched where the mask is.

// It does NOT:
    // Manage layers
    // Know about regions
    // Know about bitmaps
    // Know about export

// =========================================================
// VISUAL RESULT:

    // Dark tint covers the ENTIRE canvas.
    // Transparent holes appear where crop regions are.

    // This is what shows the user:
        // ✅ "This area will be kept" (hole = image visible)
        // ❌ "This area will be removed" (tint = dark)

// =========================================================
// HOW HOLES ARE PUNCHED:

    // canvas.clipOutPath (maskPath)
        // Removes the mask area from the clip region.
        // Only pixels OUTSIDE the mask get painted.

    // Uses canvas.save() / canvas.restoreToCount()
        // Isolates clip state so it doesn't leak to other drawing.

    // Why clipOutPath and NOT PorterDuff.CLEAR?
        // clipOutPath is hardware-accelerated (API 26+).
        // PorterDuff.CLEAR requires LAYER_TYPE_SOFTWARE.
        // Software layers are slower and use more memory.

// =========================================================
// COORDINATE SPACE:
    // maskPath must be in SCREEN SPACE (not image space).
    // Caller transforms path from image space using
    // ZoomPanEngine.imageToScreen() before calling draw().

// =========================================================

public final class TintRenderer {
    
    
    
    
    
    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private TintRenderer() {}
    
    
    
    
    
    // =========================================================
    // PUBLIC METHODS
    // =========================================================
    
    // Draws tint onto canvas with transparent holes where maskPath is.
    
    // Parameters:
        // canvas -> target canvas
        // layer -> tint settings (color, alpha)
        // maskPath -> composited region mask in SCREEN SPACE
        // w -> canvas width in px
        // h -> canvas height in px
    public static void draw (
        Canvas canvas,
        TintLayer layer,
        Path maskPath,
        float w,
        float h
    ) {
        if ( ! layer.isVisible() ) return;
        
        int saveCount = canvas.save();
        
        // Punch holes — clip out the mask region.
        // Everything inside the mask path will NOT be painted.
        if ( maskPath != null && ! maskPath.isEmpty() ) {
            canvas.clipOutPath (maskPath);
        }
        
        // Fill the remaining (non-clipped) area with the tint.
        Paint paint = layer.buildPaint();
        canvas.drawRect (0f, 0f, w, h, paint);
        
        canvas.restoreToCount (saveCount);
    }
    
    
    
    
    
}


