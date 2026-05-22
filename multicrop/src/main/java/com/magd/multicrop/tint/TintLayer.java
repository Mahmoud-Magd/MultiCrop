package com.magd.multicrop.tint;



import android.graphics.BlendMode;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.RequiresApi;



// =========================================================
// TintLayer
// =========================================================

// Represents a single compositing layer in the tint stack.

// =========================================================
// ARCHITECTURE:

// Instead of one hardcoded tint color, the system supports
// a stack of TintLayers (managed by TintLayerStack).

// Each layer has:
    // color -> ARGB tint color
    // alpha -> independent layer opacity [0..1]
    // blendMode -> how this layer composites onto layers below it
    // visible -> whether this layer participates in rendering

// =========================================================
// BLEND MODE SUPPORT:
    // On Android API 29+:
        // Uses BlendMode for full compositing support.
        // Supports: MULTIPLY, SCREEN, OVERLAY, DARKEN,
            // LIGHTEN, COLOR_DODGE, COLOR_BURN, etc.
    
    // On Android API < 29:
        // Falls back to PorterDuff.Mode (SRC_OVER by default).
        // Subclass or extend if legacy blend modes are needed.

// =========================================================
// LAYER ORDERING:
    // Layers are drawn bottom-to-top.
    // Layer 0 = bottom (drawn first).
    // Layer N = top (drawn last).

// =========================================================
// USE CASES:
    // Layer 0: standard dark tint (60% black)
    // Layer 1: red highlight over a selected region (multiply)
    // Layer 2: focus vignette (radial gradient — future)
    // Layer 3: glow overlay (screen blend — future)

// =========================================================

public class TintLayer {
    
    
    
    
    
    // =========================================================
    // CONSTANTS
    // =========================================================
    private static final float DEFAULT_ALPHA = 1.0f; // Default: fully opaque layer.
    private static final int DEFAULT_COLOR = 0x99000000; // Default: 60% black.
    private static final boolean DEFAULT_VISIBLE = true; // Default: visible.
    
    
    
    
    
    // =========================================================
    // VARIABLES
    // =========================================================
    private float alpha; // Layer opacity [0..1], independent of color alpha.
    private int color; // Layer tint color (ARGB).
    private boolean visible; // Whether this layer renders.
    
    // Blend mode (API 29+).
    // null = SRC_OVER (standard alpha compositing).
    private Object blendMode; // Stored as Object to avoid API level compile issues.
    
    
    
    
    
    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    
    // Default constructor.
    // Creates standard 60% black tint, fully opaque, SRC_OVER.
    public TintLayer() {
        this (DEFAULT_ALPHA, DEFAULT_COLOR, DEFAULT_VISIBLE);
    }
    
    // Convenience constructor: custom color.
    public TintLayer (@ColorInt int color) {
        this (DEFAULT_ALPHA, color, DEFAULT_VISIBLE);
    }
    
    // Convenience constructor: custom color + alpha.
    public TintLayer (@FloatRange (from = 0.0, to = 1.0) float alpha, @ColorInt int color) {
        this (alpha, color, DEFAULT_VISIBLE);
    }
    
    // Full constructor.
    public TintLayer (@FloatRange (from = 0.0, to = 1.0) float alpha, @ColorInt int color, boolean visible) {
        this.alpha = alpha;
        this.color = color;
        this.visible = visible;
        this.blendMode = null; // Default: SRC_OVER.
    }
    
    
    
    
    
    // =========================================================
    // PUBLIC METHODS
    // =========================================================
    
    // ======= Getters =======
    public float getAlpha() { return alpha; } // Returns layer alpha [0..1].
    public int getColor() { return color; } // Returns layer color.
    public boolean isVisible() { return visible; } // Returns whether layer is active.
    
    
    
    // ======= Setters =======
    public void setAlpha (@FloatRange (from = 0.0, to = 1.0) float alpha) { this.alpha = alpha; } // Sets layer alpha.
    public void setColor (@ColorInt int color) { this.color = color; } // Sets layer color.
    public void setVisible (boolean visible) { this.visible = visible; } // Shows or hides layer.
    
    // Sets blend mode (API 29+).
    @RequiresApi (api = Build.VERSION_CODES.Q)
    // it's a compile-time lint annotation.
    // It tells Android Studio
        // "warn the caller if they call this without checking SDK_INT >= 29 first."
    // The APK doesn't change.
    // The annotation itself doesn't check anything at runtime
    // that's what the
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        // inside buildPaint() does.
    public void setBlendMode (BlendMode blendMode) {
        this.blendMode = blendMode;
    }
    
    
    
    
    
    // ======= Paint =======
    
    // Builds and returns a Paint configured for this layer.
    //
    // Called by the tint compositor when drawing this layer.
    // Alpha from both the color channel and layer alpha are combined.
    public Paint buildPaint() {
        Paint paint = new Paint (Paint.ANTI_ALIAS_FLAG);
        paint.setStyle (Paint.Style.FILL);
        
        // Extract color components.
        int r = Color.red (color);
        int g = Color.green (color);
        int b = Color.blue (color);

        // Combine color alpha with layer alpha.
        int colorAlpha = Color.alpha (color);
        int finalAlpha = (int) (colorAlpha * alpha);
        
        paint.setARGB (finalAlpha, r, g, b);
        
        // Apply blend mode on supported API.
        if (blendMode != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            paint.setBlendMode ( (BlendMode) blendMode );
        }
        
        return paint;
    }
    
    
    
    // ======= Copy =======
    
    // Returns a deep copy of this layer.
    // Used by undo/redo history and layer duplication.
    public TintLayer copy() {
        TintLayer copy = new TintLayer (alpha, color, visible);
        copy.blendMode = this.blendMode;
        return copy;
    }
    
    
    
    
    
}


