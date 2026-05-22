package com.magd.multicrop.geometry;



import android.graphics.PointF;

import com.magd.multicrop.enums.HandleType;
import com.magd.multicrop.models.CropRect;



// =========================================================
// HandlePositionCalculator
// =========================================================

// Pure-static calculator that maps a CropRect to
// the 8 handle center positions.

// IMPORTANT:
    // This class is stateless.
    // Caller passes CropRect, receives PointF per handle.

// =========================================================
// COORDINATE SPACE:
    // All outputs are in CropArea LOCAL coordinates.
    // (0,0) = top-left of CropArea.
    // Matches CropRect coordinate space exactly.

// =========================================================
// HANDLE CENTER POSITIONS:

    //  TL ──────────── TC ──────────── TR
    //  │                                                        │
    //  ML                                                       MR
    //  │                                                        │
    //  BL ──────────── BC ──────────── BR

// =========================================================

public final class HandlePositionCalculator {
    
    
    
    
    
    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private HandlePositionCalculator() {}
    
    
    
    
    
    // =========================================================
    // PUBLIC METHODS
    // =========================================================
    
    // Returns the center point of the given handle for the given rect.
    
    // All coordinates are in CropArea local space.
    public static PointF getCenter (HandleType handleType, CropRect rect) {
        float l  = rect.getLeft();
        float t  = rect.getTop();
        float r  = rect.getRight();
        float b  = rect.getBottom();
        
        float cx = rect.centerX();
        float cy = rect.centerY();
        
        switch (handleType) {
            case TOP_LEFT: return new PointF (l, t);
            case TOP: return new PointF (cx, t);
            case TOP_RIGHT: return new PointF (r, t);
            
            case RIGHT: return new PointF (r, cy);
            
            case BOTTOM_RIGHT: return new PointF (r, b);
            case BOTTOM: return new PointF (cx, b);
            case BOTTOM_LEFT: return new PointF (l, b);
            
            case LEFT: return new PointF (l, cy);
            
            default: return new PointF (cx, cy);
        }
    }
    
    // Returns all 8 handle centers in a fixed-order array.
    
    // Order matches HandleType.values():
        // [0] TOP_LEFT
        // [1] TOP
        // [2] TOP_RIGHT
        
        // [3] RIGHT
        
        // [4] BOTTOM_RIGHT
        // [5] BOTTOM
        // [6] BOTTOM_LEFT
        
        // [7] LEFT
    public static PointF[] getAllCenters (CropRect rect) {
        HandleType[] types = HandleType.values();
        PointF[] centers = new PointF [types.length];
        for (int i = 0; i < types.length; i++) {
            centers [i] = getCenter (types [i], rect);
        }
        return centers;
    }
    
    
    
    // =========================================================
    // TOUCH HIT TESTING
    // =========================================================
    
    // Returns the HandleType whose hit area contains (touchX, touchY).
    // Returns null if no handle was touched.
    
    // hitRadius:
        // Half-size of the square hit area per handle.
        // Should match HandleDrawer.hitSize / 2.
    public static HandleType findTouchedHandle (
        float touchX,
        float touchY,
        CropRect rect,
        float hitRadius
    ) {
        HandleType[] types = HandleType.values();
        
        for (HandleType type : types) {
            PointF center = getCenter (type, rect);
            
            if (
                Math.abs (touchX - center.x) <= hitRadius
                &&
                Math.abs (touchY - center.y) <= hitRadius
            ) {
                return type;
            }
        }
        
        return null; // No handle hit.
    }
    
    
    
    
    
}


