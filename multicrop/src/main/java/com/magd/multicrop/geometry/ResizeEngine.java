package com.magd.multicrop.geometry;



import com.magd.multicrop.enums.HandleType;
import com.magd.multicrop.models.CropRect;



// =========================================================
// ResizeEngine
// =========================================================

// Pure-static engine that applies a (dx, dy) drag delta
// to a CropRect based on which HandleType is active.

// IMPORTANT:
    // This class is stateless.
    // It never stores any rect.
    // It never stores any touch state.
    // Caller owns the rect.
    // Caller owns the touch state.

// =========================================================
// AXIS CONSTRAINTS (mandatory):

    // Corner handles:
        // TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, TOP_RIGHT
        // Move on X + Y simultaneously.

    // Edge handles:
        // TOP, BOTTOM -> Y axis only.
        // LEFT, RIGHT -> X axis only.

// =========================================================
// SNAP BEHAVIOR:
    // If an edge comes within SNAP_THRESHOLD of the image boundary,
    // it snaps flush to that boundary.

// =========================================================
// MINIMUM SIZE:
    // Resize is rejected if result would be smaller than
    // CropRect.MIN_WIDTH
    // or
    // CropRect.MIN_HEIGHT.

// =========================================================

public final class ResizeEngine {
    
    
    
    
    
    // =========================================================
    // CONSTANTS
    // =========================================================
    private static final float SNAP_THRESHOLD = 8f; // Snap-to-edge threshold in px.
    
    
    
    
    
    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private ResizeEngine() {}
    
    
    
    
    
    // =========================================================
    // PUBLIC METHODS
    // =========================================================
    
    // Applies (dx, dy) delta to proposed rect for the given handle.
    
    // Parameters:
        // handleType   -> which handle is being dragged
        // dx           -> horizontal delta from finger-down position
        // dy           -> vertical delta from finger-down position
        // proposed     -> rect snapshot at finger-down (mutated in-place)
        // maxWidth     -> container width (CropArea width = image display width)
        // maxHeight    -> container height (CropArea height = image display height)
    
    // Returns:
        // true  -> resize applied successfully
        // false -> resize rejected (would violate minimum size)
    public static boolean apply (
        HandleType handleType,
        float dx,
        float dy,
        CropRect cRect,
        float maxWidth,
        float maxHeight
    ) {
        // Work on mutable copies of each edge.
        float newLeft = cRect.getLeft();
        float newTop = cRect.getTop();
        float newRight = cRect.getRight();
        float newBottom = cRect.getBottom();
        
        switch (handleType) {
        
            // ======= Corner handles: X + Y =======
            case TOP_LEFT:
                newLeft = clampLeft (newLeft + dx, maxWidth);
                newTop = clampTop (newTop + dy, maxHeight);
                break;
            
            case TOP_RIGHT:
                newRight = clampRight (newRight + dx, maxWidth);
                newTop = clampTop (newTop + dy, maxHeight);
                break;
            
            case BOTTOM_LEFT:
                newLeft = clampLeft (newLeft + dx, maxWidth);
                newBottom = clampBottom (newBottom + dy, maxHeight);
                break;
            
            case BOTTOM_RIGHT:
                newRight = clampRight (newRight + dx, maxWidth);
                newBottom = clampBottom (newBottom + dy, maxHeight);
                break;
            
            
            
            // ======= Edge handles: Y axis only =======
            case TOP:
                newTop = clampTop (newTop + dy, maxHeight);
                break;
            
            case BOTTOM:
                newBottom = clampBottom (newBottom + dy, maxHeight);
                break;
            
            
            
            // ======= Edge handles: X axis only =======
            case LEFT:
                newLeft = clampLeft (newLeft + dx, maxWidth);
                break;
            
            case RIGHT:
                newRight = clampRight (newRight + dx, maxWidth);
                break;
        }
        
        // Validate minimum size BEFORE committing.
        if ( (newRight - newLeft) < CropRect.getMinWidth() ) return false;
        if ( (newBottom - newTop) < CropRect.getMinHeight() ) return false;
        
        cRect.set (newLeft, newTop, newRight, newBottom);
        
        return true;
    }
    
    
    
    
    
    // =========================================================
    // PRIVATE METHODS
    // =========================================================
    
    // Clamps left edge: [0, maxWidth], with snap-to-left-boundary.
    private static float clampLeft (float v, float maxWidth) {
        if (v < SNAP_THRESHOLD) return 0f;
        return Math.max ( 0f, Math.min (v, maxWidth) );
    }
    
    // Clamps right edge: [0, maxWidth], with snap-to-right-boundary.
    private static float clampRight (float v, float maxWidth) {
        if ( (maxWidth - v) < SNAP_THRESHOLD ) return maxWidth;
        return Math.max ( 0f, Math.min (v, maxWidth) );
    }
    
    // Clamps top edge: [0, maxHeight], with snap-to-top-boundary.
    private static float clampTop (float v, float maxHeight) {
        if (v < SNAP_THRESHOLD) return 0f;
        return Math.max ( 0f, Math.min (v, maxHeight) );
    }
    
    // Clamps bottom edge: [0, maxHeight], with snap-to-bottom-boundary.
    private static float clampBottom (float v, float maxHeight) {
        if ( (maxHeight - v) < SNAP_THRESHOLD ) return maxHeight;
        return Math.max ( 0f, Math.min (v, maxHeight) );
    }
    
    
    
    
    
}


