package com.magd.multicrop.render.handle;



import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;

import com.magd.multicrop.render.handle.HandleDrawerHelper;
import com.magd.multicrop.enums.HandleType;
import com.magd.multicrop.utils.DimensUtils;



// =========================================================
// HandleDrawer
// =========================================================

// Rendering helper responsible for drawing ONE handle.

// IMPORTANT:
    // This is NOT a View.

// Why?
    // Because creating 8 Views × every CropArea
    // becomes extremely expensive with many crop areas.

// Instead:
    // CropArea handles ALL rendering itself
    // and delegates handle drawing here.

// =========================================================
// HANDLE TYPES:
    // Corners:
        // TOP_LEFT
        // TOP_RIGHT
        // BOTTOM_LEFT
        // BOTTOM_RIGHT

    // Edge handles:
        // TOP
        // BOTTOM
        // LEFT
        // RIGHT

// =========================================================
// VISUAL STYLE:

// Corner handles:
    // Drawn as "L" shape.

// Edge handles:
    // Drawn as centered bar.

// =========================================================
// ALPHA:
    // Handles use alpha = 0.75

// =========================================================

public class HandleDrawer {
    
    
    
    
    
    // =========================================================
    // CONSTANTS
    // =========================================================
    private static final float DEFAULT_ALPHA = 0.75f; // Handle default alpha.
    private static final int DEFAULT_COLOR = 0xFFFFFFFF; // Handle default color.
    
    private static final float DEFAULT_HANDLE_SIZE_DP = 24f; // Handle default visible size in dp.
    private static final float DEFAULT_MIN_HIT_SIZE_DP = 48f; // Minimum touch target size in dp.
    
    
    
    
    
    // =========================================================
    // VARIABLES
    // =========================================================
    private float alpha; // Current alpha [0..1].
    private int color; // Current color.
    
    private float handleSize_px; // Visible handle size in px.
    private float hitSize_px;    // Touch target size in px.
    private float halfHitSize_px;// Cached half hit size in px.
    
    private final HandleType handleType; // Handle type.
    private final Paint paint; // Drawing paint.
    
    
    
    
    
    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    
    // Default constructor.
    // Uses all defaults: WHITE, alpha 0.75, 24dp handle size.
    public HandleDrawer (HandleType handleType) {
        this (DEFAULT_ALPHA, DEFAULT_COLOR, DEFAULT_HANDLE_SIZE_DP, handleType);
    }
    
    // Convenience constructor: custom color, everything else default.
    public HandleDrawer (@ColorInt int color, HandleType handleType) {
        this (DEFAULT_ALPHA, color, DEFAULT_HANDLE_SIZE_DP, handleType);
    }
    
    // Full constructor.
    // Parameters:
    // alpha -> handle alpha [0..1]
    // color -> handle stroke color
    // handleSize_dp -> visible handle size in dp
    // handleType -> which handle this draws
    public HandleDrawer (@FloatRange (from = 0.0, to = 1.0) float alpha, @ColorInt int color, float handleSize_dp, HandleType handleType) {
        this.alpha = alpha;
        this.color = color;
        this.handleSize_px = DimensUtils.dpToPx (handleSize_dp);
        this.handleType = handleType;
        
        // Touch target is always at least DEFAULT_MIN_HIT_SIZE_DP.
        // Why?
            // Finger touch is imprecise.
            // Small hit targets create bad UX.
        // Example:
            // Visible handle = 24dp
            // Touch area = 48dp minimum
        // Why 48dp?
            // Material Design minimum touch target spec.
            // Comfortable for average finger size.
            // Anything below starts to create misses.
        this.hitSize_px = Math.max ( this.handleSize_px, DimensUtils.dpToPx (DEFAULT_MIN_HIT_SIZE_DP) );
        this.halfHitSize_px = this.hitSize_px / 2f;
        
        // Create paint.
        paint = new Paint (Paint.ANTI_ALIAS_FLAG);
        paint.setColor (color);
        paint.setStyle (Paint.Style.STROKE);
        
        // Stroke width scales with handle size.
        // Prevents:
            // tiny weak handles
            // OR
            // massive ugly handles
        paint.setStrokeWidth (
            Math.max (
                DimensUtils.dpToPx (1.5f), // Never thinner than 1.5 dp.
                handleSize_px / 7f
            )
        );
        
        paint.setAlpha ( (int) (255f * alpha) ); // Convert [0..1] float to [0..255] int.
        paint.setStrokeCap (Paint.Cap.SQUARE);
    }
    
    
    
    
    
    // =========================================================
    // PUBLIC METHODS
    // =========================================================
    
    // ======= Getters =======
    public float getAlpha() { return alpha; } // Returns current alpha.
    public int getColor() { return color; } // Returns current color.
    public float getHandleSize_px() { return handleSize_px; } // Returns visible handle size in px.
    public float getHandleSize_dp() { return DimensUtils.pxToDp (handleSize_px); } // Returns visible handle size in dp.
    public float getHitSize_px() { return hitSize_px; } // Returns touch target size in px.
    public float getHalfHitSize_px() { return halfHitSize_px; } // Returns half touch target size in px.
    public HandleType getHandleType() { return handleType; } // Returns handle type.
    
    
    
    
    
    // ======= Setters =======
    public void setAlpha (@FloatRange (from = 0.0, to = 1.0) float alpha) {
        this.alpha = alpha;
        paint.setAlpha ( (int) (255f * alpha) ); // Sync paint immediately.
    }
    
    public void setColor (@ColorInt int color) {
        this.color = color;
        paint.setColor (color);
        paint.setAlpha ( (int) (255f * this.alpha) ); // Re-apply alpha after setColor (setColor resets alpha).
    }
    
    public void setHandleSize_dp (float handleSize_dp) { setHandleSize_px ( DimensUtils.dpToPx (handleSize_dp) ); }

    public void setHandleSize_px (float handleSize_px) {
        this.handleSize_px = handleSize_px;
        this.hitSize_px = Math.max ( this.handleSize_px, DimensUtils.dpToPx (DEFAULT_MIN_HIT_SIZE_DP) );
        this.halfHitSize_px = this.hitSize_px / 2f;
        
        // Update stroke width to match new size.
        paint.setStrokeWidth (
            Math.max (
                DimensUtils.dpToPx (1.5f),
                handleSize_px / 7f
            )
        );
    }
    
    
    
    // ======= Draw =======
    
    // Draws handle centered at cx/cy.
    // cx/cy:
        // Handle center coordinates.
        // Canvas coordinates are LOCAL to CropArea.
    public void draw (Canvas canvas, float cx, float cy) {
        switch (handleType) {
            // Corners
            case TOP_LEFT:
                HandleDrawerHelper.drawTopLeft (paint, handleSize_px, canvas, cx, cy);
                break;
            
            case TOP_RIGHT:
                HandleDrawerHelper.drawTopRight (paint, handleSize_px, canvas, cx, cy);
                break;
            
            case BOTTOM_LEFT:
                HandleDrawerHelper.drawBottomLeft (paint, handleSize_px, canvas, cx, cy);
                break;
            
            case BOTTOM_RIGHT:
                HandleDrawerHelper.drawBottomRight (paint, handleSize_px, canvas, cx, cy);
                break;
            
            
            
            // Edges
            case TOP:
            case BOTTOM:
                HandleDrawerHelper.drawHorizontalBar (paint, handleSize_px, canvas, cx, cy);
                break;
            
            case LEFT:
            case RIGHT:
                HandleDrawerHelper.drawVerticalBar (paint, handleSize_px, canvas, cx, cy);
                break;
        }
    }
    
    
    
    
    
    // ======= Hit Testing =======
    
    // Returns hit area for touch detection.
    // IMPORTANT:
        // Touch area intentionally larger than visible handle.
        // Matches Material Design minimum touch target spec.
    public RectF getHitRect (float cx, float cy) {
        return new RectF (
            cx - halfHitSize_px,
            cy - halfHitSize_px,
            cx + halfHitSize_px,
            cy + halfHitSize_px
        );
    }
    
    
    
    
    
}


