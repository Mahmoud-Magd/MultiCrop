package com.magd.multicrop.render;



import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;

import com.magd.multicrop.enums.GuideLineMode;
import com.magd.multicrop.models.CropRect;
import com.magd.multicrop.utils.DimensUtils;



// =========================================================
// GuideLinesDrawer
// =========================================================

// Lightweight renderer responsible for drawing
// the internal 3x3 crop guidelines.

// IMPORTANT:
    // This is NOT a View.

// Why?
    // Because creating guideline Views for every CropArea
    // would create unnecessary:
        // ❌ layout passes
        // ❌ measure passes
        // ❌ overdraw
        // ❌ object count

// Instead:
    // CropArea handles rendering directly
    // and delegates ONLY drawing here.

// =========================================================
// GRID STRUCTURE:
    // 2 vertical lines
    // 2 horizontal lines

// Divides CropArea into:
    // 9 equal regions

// =========================================================
// VISIBILITY MODES:
    // OFF // Never visible.
    // ON // Always visible.
    // ON_TOUCH // Visible only during interaction.

// =========================================================
// DEFAULT_ALPHA:
    // Grid alpha = 0.75

// =========================================================

public class GuideLinesDrawer {
    
    
    
    
    
    // =========================================================
    // CONSTANTS
    // =========================================================
    private static final float DEFAULT_ALPHA = 0.75f; // Grid default alpha.
    private static final int DEFAULT_COLOR = 0xFFFFFFFF; // Grid default color.
    private static final float DEFAULT_STROKE_WIDTH_DP = 2f; // Grid default stroke width in dp.
    private static final GuideLineMode DEFAULT_MODE = GuideLineMode.ON_TOUCH; // Grid default mode.
    
    
    
    
    
    // =========================================================
    // VARIABLES
    // =========================================================
    private float alpha; // Grid alpha.
    private int color; // Grid color.
    private float strokeWidth_px; // Grid stroke width in px.
    private GuideLineMode guideLineMode; // Current guideline mode.
    
    private final Paint paint; // Drawing paint.
    
    
    
    
    
    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    
    // Default constructor.
    // Uses all defaults: WHITE, alpha 0.75, ON_TOUCH.
    public GuideLinesDrawer() {
        this (DEFAULT_ALPHA, DEFAULT_COLOR, DEFAULT_STROKE_WIDTH_DP, DEFAULT_MODE);
    }
    
    // Convenience constructor: custom mode, everything else default.
    public GuideLinesDrawer (GuideLineMode guideLineMode) {
        this (DEFAULT_ALPHA, DEFAULT_COLOR, DEFAULT_STROKE_WIDTH_DP, guideLineMode);
    }
    
    // Full constructor.
    public GuideLinesDrawer (@FloatRange (from = 0.0, to = 1.0) float alpha, @ColorInt int color, float strokeWidth_dp, GuideLineMode guideLineMode) {
        this.alpha = alpha;
        this.color = color;
        this.strokeWidth_px = DimensUtils.dpToPx (strokeWidth_dp);
        this.guideLineMode = guideLineMode;
        
        // Create paint.
        paint = new Paint (Paint.ANTI_ALIAS_FLAG);
        paint.setColor (color);
        paint.setStyle (Paint.Style.STROKE);
        paint.setStrokeWidth (this.strokeWidth_px);
        paint.setAlpha ( (int) (255f * alpha) ); // Convert [0..1] float to [0..255] int.
    }
    
    
    
    
    
    // =========================================================
    // PUBLIC METHODS
    // =========================================================
    
    // ======= Getters =======
    public float getAlpha() { return alpha; } // Returns current guideline alpha.
    public int getColor() { return color; } // Returns current guideline color.
    public float getStrokeWidth_px() { return strokeWidth_px; } // Returns current guideline stroke width in px.
    public float getStrokeWidth_dp() { return DimensUtils.pxToDp (strokeWidth_px); } // Returns current guideline stroke width in dp.
    public GuideLineMode getGuideLineMode() { return guideLineMode; } // Returns current guideline mode.
    
    
    
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
    
    public void setStrokeWidth_px (float strokeWidth_px) {
        this.strokeWidth_px = strokeWidth_px;
        paint.setStrokeWidth (strokeWidth_px);
    }
    
    public void setStrokeWidth_dp (float strokeWidth_dp) {
        this.strokeWidth_px = DimensUtils.dpToPx (strokeWidth_dp);
        paint.setStrokeWidth (this.strokeWidth_px);
    }
    
    public void setGuideLineMode (GuideLineMode guideLineMode) { this.guideLineMode = guideLineMode; }
    
    
    
    // ======= Draw =======
    
    // Draws guidelines onto the given canvas.
    // rect:
        // CropArea bounds in LOCAL coordinates.
    // isInteracting:
        // true while user is moving or resizing.
        // Used by ON_TOUCH mode to decide visibility.
    public void draw (Canvas canvas, CropRect rect, boolean isInteracting) {
        if ( ! shouldDraw (isInteracting) ) return; // Skip if guidelines should not appear.
        
        // Calculate third divisions.
        float thirdWidth = rect.width() / 3f;
        float thirdHeight = rect.height() / 3f;
        
        // Vertical line x positions.
        float x1 = rect.getLeft() + thirdWidth;
        float x2 = rect.getLeft() + (thirdWidth * 2f);
        
        // Horizontal line y positions.
        float y1 = rect.getTop() + thirdHeight;
        float y2 = rect.getTop() + (thirdHeight * 2f);
        
        // Draw vertical lines.
        canvas.drawLine (x1, rect.getTop(), x1, rect.getBottom(), paint);
        canvas.drawLine (x2, rect.getTop(), x2, rect.getBottom(), paint);
        
        // Draw horizontal lines.
        canvas.drawLine (rect.getLeft(), y1, rect.getRight(), y1, paint);
        canvas.drawLine (rect.getLeft(), y2, rect.getRight(), y2, paint);
    }
    
    
    
    
    
    // =========================================================
    // PRIVATE METHODS
    // =========================================================
    
    // Determines whether guidelines should draw.
    private boolean shouldDraw (boolean isInteracting) {
        switch (guideLineMode) {
            case ON: return true;
            case ON_TOUCH: return isInteracting;
            case OFF:
            default: return false;
        }
    }
    
    
    
    
    
}


