package com.magd.multicrop.regions.children;



import android.graphics.Path;
import android.graphics.RectF;

import com.magd.multicrop.central.RtVault;
import com.magd.multicrop.regions.CropRegion;


// =========================================================
// OvalRegion
// =========================================================

// Oval (ellipse) region.

// =========================================================
// SHAPE:

    // An oval inscribed inside a bounding rectangle.
    // If width == height, it becomes a perfect circle.

// =========================================================
// COORDINATE SPACE:
    // All coordinates are in IMAGE SPACE.
    // (0,0) = top-left of source image.

// =========================================================
// HIT TESTING:

    // Uses ellipse equation for precision:
        // ((x - cx) / rx)^2 + ((y - cy) / ry)^2 <= 1

    // Where:
        // cx, cy = center of oval
        // rx = half-width
        // ry = half-height

// =========================================================

public class OvalRegion extends CropRegion {
    
    
    
    
    
    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    public OvalRegion (float imgW, float imgH) {
        float crpW = imgW * RtVault.R_C_WIDTH;
        float crpH = imgH * RtVault.R_C_HEIGHT;
        
        this.left = (imgW - crpW) / 2f;
        this.top = (imgH - crpH) / 2f;
        this.right = left + crpW;
        this.bottom = top + crpH;
    }
    
    public OvalRegion (float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
    
    public OvalRegion (RectF boundingRect) {
        this (boundingRect.left, boundingRect.top, boundingRect.right, boundingRect.bottom);
    }
    
    
    
    
    
    // =========================================================
    // STATIC FACTORY METHODS
    // =========================================================

    // Creates an OvalRegion from center point + radii.

    // Why static factory and NOT a constructor?
        // A constructor (float centerX, float centerY, float radiusX, float radiusY)
        // has the exact same signature as (float left, float top, float right, float bottom).
        // Java resolves overloads by parameter TYPES only — not by parameter names.
        // Two constructors with identical types = duplicate method = compile error.
        // Static factory methods have names, so they are always unambiguous.

    public static OvalRegion fromCenter (float centerX, float centerY, float radiusX, float radiusY) {
        return new OvalRegion (
            centerX - radiusX,
            centerY - radiusY,
            centerX + radiusX,
            centerY + radiusY
        );
    }
    
    
    
    
    
    // =========================================================
    // ABSTRACT IMPLEMENTATIONS
    // =========================================================

    // Builds an oval Path in image space.
    @Override
    public Path buildPath() {
        Path path = new Path();
        path.addOval (left, top, right, bottom, Path.Direction.CW);
        return path;
    }

    // Returns bounding box in image space.
    @Override
    public RectF getBounds() { return new RectF (left, top, right, bottom); }

    // Returns deep copy.
    @Override
    public OvalRegion copy() {
        OvalRegion copy = new OvalRegion (left, top, right, bottom);
        copy.setVisible ( isVisible() );
        return copy;
    }
    
    
    
    // ======= Booleans =======
    @Override
    public boolean isSymmetrical() { return true; } // Ovals are always simitrical.
    @Override
    public boolean isRegular() { return regularityScore() >= 0.95f; }
    
    
    
    // ======= Scores =======
    @Override
    public float symmetryScore() { return 1f; } // Ovals are always simitrical.
    @Override
    public float regularityScore() {
        float rx = radiusX();
        float ry = radiusY();
        
        if (rx <= 0 || ry <= 0) return 0f;
        
        float ratio = Math.min (rx, ry) / Math.max (rx, ry); // Circle = perfect regularity
        
        return clamp01 (ratio);
    }
    
    
    
    
    // =========================================================
    // PUBLIC METHODS
    // =========================================================
    
    // ======= Simple Math =======
    public float radiusX() { return width() / 2f; } // Returns horizontal radius.
    public float radiusY() { return height() / 2f; } // Returns vertical radius.



    



    // ======= Hit testing (precise ellipse equation) =======

    // Returns true if the point is inside the ellipse.
    //
    // Formula: ((x - cx) / rx)^2 + ((y - cy) / ry)^2 <= 1
    @Override
    public boolean contains (float imageX, float imageY) {
        float cx = centerX();
        float cy = centerY();
        float rx = radiusX();
        float ry = radiusY();

        if (rx <= 0 || ry <= 0) return false;

        float dx = (imageX - cx) / rx;
        float dy = (imageY - cy) / ry;

        return (dx * dx + dy * dy) <= 1f;
    }



    // ======= Utility =======
    public RectF toBoundingRectF() { return new RectF (left, top, right, bottom); }



}


