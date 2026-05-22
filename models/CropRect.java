package com.magd.multicrop.models;



import android.graphics.RectF;

import com.magd.multicrop.regions.CropRegion; // REMOVE TO ISOLATE CLASS.



// =========================================================
// CropRect
// =========================================================

// Core geometry model used by the entire cropper system.

// =========================================================
// COORDINATE SPACE:

// Uses IMAGE SPACE coordinates.
    // (0,0) = top-left of source image.
    // Values match source bitmap pixels 1:1.

// =========================================================
// VALIDATION STRATEGY:

// Individual setters (setLeft, setTop, setRight, setBottom)
// do NOT validate, because sequential resize operations
// produce transient invalid states per frame.
// Validation only fires on the bulk set() method.

// =========================================================

public class CropRect {



    // =========================================================
    // CONSTANTS
    // =========================================================
    private static final float MIN_WIDTH  = 10f; // Minimum allowed crop width.
    private static final float MIN_HEIGHT = 10f; // Minimum allowed crop height.



    // =========================================================
    // VARIABLES
    // =========================================================
    private float left;   // Left edge.
    private float top;    // Top edge.
    private float right;  // Right edge.
    private float bottom; // Bottom edge.



    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    public CropRect (CropRegion cR)                                   { this ( cR.getBounds() ); } // REMOVE TO ISOLATE CLASS.
    public CropRect (RectF rectF)                                     { set (rectF.left, rectF.top, rectF.right, rectF.bottom); }
    public CropRect (float left, float top, float right, float bottom) { set (left, top, right, bottom); }



    // =========================================================
    // PUBLIC METHODS
    // =========================================================

    // ======= Getters =======
    public static float getMinWidth()  { return MIN_WIDTH;  }
    public static float getMinHeight() { return MIN_HEIGHT; }

    public float getLeft()   { return left;   }
    public float getTop()    { return top;    }
    public float getRight()  { return right;  }
    public float getBottom() { return bottom; }



    // ======= Individual setters (no validation — safe for per-frame resize) =======
    public void setLeft   (float left)   { this.left   = left;   }
    public void setTop    (float top)    { this.top    = top;    }
    public void setRight  (float right)  { this.right  = right;  }
    public void setBottom (float bottom) { this.bottom = bottom; }

    // Bulk set — validation fires here.
    public void set (CropRect other) { set (other.left, other.top, other.right, other.bottom); }
    public void set (float left, float top, float right, float bottom) {
        validateBounds (left, top, right, bottom);
        this.left   = left;
        this.top    = top;
        this.right  = right;
        this.bottom = bottom;
    }



    // ======= Geometry =======
    public float width()   { return right  - left;        }
    public float height()  { return bottom - top;         }
    public float centerX() { return (left  + right)  / 2f; }
    public float centerY() { return (top   + bottom) / 2f; }

    public boolean contains (float x, float y) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    public CropRect copy()    { return new CropRect (left, top, right, bottom); }
    public RectF    toRectF() { return new RectF    (left, top, right, bottom); }



    // ======= Movement =======

    // Moves the entire rect by (dx, dy).
    // Clamped so it never exits (0, 0, maxWidth, maxHeight).
    public void offset (float dx, float dy, float maxWidth, float maxHeight) {
        float l = left   + dx;
        float t = top    + dy;
        float r = right  + dx;
        float b = bottom + dy;

        // Clamp horizontally.
        if (l < 0)         { r -= l;          l = 0;        }
        if (r > maxWidth)  { l -= r - maxWidth; r = maxWidth; }

        // Clamp vertically.
        if (t < 0)         { b -= t;           t = 0;         }
        if (b > maxHeight) { t -= b - maxHeight; b = maxHeight; }

        set (l, t, r, b);
    }

    // Clamps rect inside (0, 0, maxWidth, maxHeight) and enforces minimum size.
    public void clampInside (float maxWidth, float maxHeight) {
        if (left   < 0)         left   = 0;
        if (top    < 0)         top    = 0;
        if (right  > maxWidth)  right  = maxWidth;
        if (bottom > maxHeight) bottom = maxHeight;

        if (width()  < MIN_WIDTH)  right  = left + MIN_WIDTH;
        if (height() < MIN_HEIGHT) bottom = top  + MIN_HEIGHT;

        if (right  > maxWidth)  { right  = maxWidth;  left = right  - MIN_WIDTH;  }
        if (bottom > maxHeight) { bottom = maxHeight;  top = bottom - MIN_HEIGHT; }
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    // Validates that right > left and bottom > top.
    // Not called from individual setters — only from bulk set().
    // See class-level comment for the reason why.
    private static void validateBounds (float left, float top, float right, float bottom) {
        String error = "";

        if (right <= left) {
            error += (
                "Invalid CropRect: right must be greater than left.\n"
                +
                "left = " + left + ", right = " + right
            );
        }

        if (bottom <= top) {
            error += (
                "\n\n"
                +
                "Invalid CropRect: bottom must be greater than top.\n"
                +
                "top = " + top + ", bottom = " + bottom
            );
        }

        if ( ! error.isEmpty() ) throw new IllegalArgumentException (error);
    }



}
