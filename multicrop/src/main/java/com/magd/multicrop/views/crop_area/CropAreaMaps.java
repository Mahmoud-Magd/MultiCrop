package com.magd.multicrop.views.crop_area;



import com.magd.multicrop.enums.HandleType;
import com.magd.multicrop.enums.TouchTarget;



// =========================================================
// CropAreaMaps
// =========================================================

// Static mapping helpers between HandleType and TouchTarget.

// =========================================================
// WHY THIS EXISTS:

// CropArea needs to convert between HandleType (which handle
// was drawn / hit-tested) and TouchTarget (what the touch
// event means logically).

// Extracted from CropArea to keep that class under 300 lines.

// =========================================================

final class CropAreaMaps {



    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private CropAreaMaps() {}



    // =========================================================
    // PUBLIC METHODS
    // =========================================================

    // Maps HandleType → TouchTarget.
    static TouchTarget toTouchTarget (HandleType type) {
        switch (type) {
            case TOP_LEFT:     return TouchTarget.TOP_LEFT;
            case TOP:          return TouchTarget.TOP;
            case TOP_RIGHT:    return TouchTarget.TOP_RIGHT;
            case RIGHT:        return TouchTarget.RIGHT;
            case BOTTOM_RIGHT: return TouchTarget.BOTTOM_RIGHT;
            case BOTTOM:       return TouchTarget.BOTTOM;
            case BOTTOM_LEFT:  return TouchTarget.BOTTOM_LEFT;
            case LEFT:         return TouchTarget.LEFT;
            default:           return TouchTarget.NONE;
        }
    }

    // Maps TouchTarget → HandleType.
    // Returns null for NONE and BODY (not handle targets).
    static HandleType toHandleType (TouchTarget target) {
        switch (target) {
            case TOP_LEFT:     return HandleType.TOP_LEFT;
            case TOP:          return HandleType.TOP;
            case TOP_RIGHT:    return HandleType.TOP_RIGHT;
            case RIGHT:        return HandleType.RIGHT;
            case BOTTOM_RIGHT: return HandleType.BOTTOM_RIGHT;
            case BOTTOM:       return HandleType.BOTTOM;
            case BOTTOM_LEFT:  return HandleType.BOTTOM_LEFT;
            case LEFT:         return HandleType.LEFT;
            default:           return null;
        }
    }



}
