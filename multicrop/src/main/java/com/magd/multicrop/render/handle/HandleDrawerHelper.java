package com.magd.multicrop.render.handle;



import android.graphics.Canvas;
import android.graphics.Paint;



public final class HandleDrawerHelper {
    
    
    
    
    
    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private HandleDrawerHelper() {}
    
    
    
    
    
    // =========================================================
    // PUBLIC METHODS
    // =========================================================
    
    // ======= Corners =======
    
    // Draws top-left L handle.
    public static void drawTopLeft (Paint paint, float handleSize, Canvas canvas, float cx, float cy) {
        float arm = handleSize * 0.6f;
        
        // Vertical arm.
        canvas.drawLine (
            cx,
            cy,
            cx,
            cy + arm,
            paint
        );
        
        // Horizontal arm.
        canvas.drawLine (
            cx,
            cy,
            cx + arm,
            cy,
            paint
        );
    }
    
    
    
    // Draws top-right L handle.
    public static void drawTopRight (Paint paint, float handleSize, Canvas canvas, float cx, float cy) {
        float arm = handleSize * 0.6f;
        
        canvas.drawLine (
            cx,
            cy,
            cx,
            cy + arm,
            paint
        );
        
        canvas.drawLine (
            cx,
            cy,
            cx - arm,
            cy,
            paint
        );
    }
    
    
    
    // Draws bottom-left L handle.
    public static void drawBottomLeft (Paint paint, float handleSize, Canvas canvas, float cx, float cy) {
        float arm = handleSize * 0.6f;
        
        canvas.drawLine (
            cx,
            cy,
            cx,
            cy - arm,
            paint
        );
        
        canvas.drawLine (
            cx,
            cy,
            cx + arm,
            cy,
            paint
        );
    }
    
    
    
    // Draws bottom-right L handle.
    public static void drawBottomRight (Paint paint, float handleSize, Canvas canvas, float cx, float cy) {
        float arm = handleSize * 0.6f;
        
        canvas.drawLine (
            cx,
            cy,
            cx,
            cy - arm,
            paint
        );
        
        canvas.drawLine (
            cx,
            cy,
            cx - arm,
            cy,
            paint
        );
    }
    
    
    
    
    
    // ======= Edges =======
    
    // Draws horizontal edge handle.
    public static void drawHorizontalBar (Paint paint, float handleSize, Canvas canvas, float cx, float cy) {
        float length = handleSize * 0.7f;
        
        canvas.drawLine (
            cx - (length / 2f),
            cy,
            cx + (length / 2f),
            cy,
            paint
        );
    }
    
    
    
    // Draws vertical edge handle.
    public static void drawVerticalBar (Paint paint, float handleSize, Canvas canvas, float cx, float cy) {
        float length = handleSize * 0.7f;
        
        canvas.drawLine (
            cx,
            cy - (length / 2f),
            cx,
            cy + (length / 2f),
            paint
        );
    }
    
    
    
    
    
}


