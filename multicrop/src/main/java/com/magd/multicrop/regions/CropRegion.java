package com.magd.multicrop.regions;



import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.List;



// =========================================================
// CropRegion
// =========================================================

// Abstract base for ALL region types in the MultiCrop system.

// =========================================================
// ARCHITECTURE:

// Every region — rectangle, oval, polygon, freehand path,
// brush stroke, or future AI mask — extends this class.

// The entire system operates on CropRegion objects.
// Rendering, hit-testing, export, and boolean operations
// all work through the Path this region generates.

// SOURCE OF TRUTH RULE:
    // ✅ Region shape data (coords, points, strokes)
    // ❌ NOT rendered bitmaps
    // ❌ NOT cached screen pixels

// =========================================================
// PATH CONTRACT:

// Every subclass MUST implement buildPath().
// This method returns the canonical Path for this region.

// The path is used for:
    // - Tint hole punching
    // - Touch hit testing
    // - Boolean operations
    // - Export masking
    // - Preview rendering

// =========================================================
// COORDINATE SPACE:

// All region coordinates live in IMAGE SPACE.
    // (0,0) = top-left of source image
    // (imageWidth, imageHeight) = bottom-right of source image

// =========================================================
// SUBCLASSES (current):
    // RectangleRegion   -> axis-aligned rectangle
    // RoundedRectRegion -> rounded rectangle
    // OvalRegion        -> oval / ellipse
    // CircleRegion      -> perfect circle
    // PolygonRegion     -> arbitrary polygon (list of points)
    // FreePathRegion    -> freehand brush / pen path

// =========================================================

public abstract class CropRegion {



    // =========================================================
    // CONSTANTS
    // =========================================================

    // Maximum allowed regular polygon point count.
    // High counts visually become circles and are hard to edit.
    protected static final int MAX_REGULAR_POLYGON_POINTS = 32;
    protected static final int MIN_POLYGON_POINTS         = 3; // Polygons can't be less than a triangle.



    // =========================================================
    // VARIABLES
    // =========================================================
    private boolean visible = true; // Whether this region participates in rendering and export.

    protected float left;   // Left edge of bounding rect in image space.
    protected float top;    // Top edge of bounding rect in image space.
    protected float right;  // Right edge of bounding rect in image space.
    protected float bottom; // Bottom edge of bounding rect in image space.



    // =========================================================
    // ABSTRACT METHODS
    // =========================================================

    // Builds and returns the Path for this region in IMAGE SPACE.
    // Called by: PathBuilder, TintRenderer, BitmapExporter, hit-testing.
    public abstract Path buildPath();

    // Returns the axis-aligned bounding box in IMAGE SPACE.
    // Used for: quick hit-test rejection, export bounds, dirty-region invalidation.
    public abstract RectF getBounds();

    // Returns a deep copy of this region.
    // Required by: undo / redo history snapshots, non-destructive editing.
    public abstract CropRegion copy();



    // =========================================================
    // PUBLIC METHODS
    // =========================================================

    // ======= Getters =======
    public boolean isVisible() { return visible; }
    public float   getLeft()   { return left;    }
    public float   getTop()    { return top;     }
    public float   getRight()  { return right;   }
    public float   getBottom() { return bottom;  }

    // ======= Geometry =======
    public float width()   { return right  - left;         }
    public float height()  { return bottom - top;          }
    public float centerX() { return (left  + right)  / 2f; }
    public float centerY() { return (top   + bottom) / 2f; }



    // ======= Setters =======
    public void setVisible (boolean v)   { this.visible = v;    }
    public void setLeft    (float left)   { this.left   = left;  }
    public void setTop     (float top)    { this.top    = top;   }
    public void setRight   (float right)  { this.right  = right; }
    public void setBottom  (float bottom) { this.bottom = bottom; }

    public void setEdges (float left, float top, float right, float bottom) {
        this.left   = left;
        this.top    = top;
        this.right  = right;
        this.bottom = bottom;
    }

    public void setEdges (RectF rect) { setEdges (rect.left, rect.top, rect.right, rect.bottom); }



    // ======= Points (polygon subclasses override) =======
    public List <PointF> getPoints() { return null; }



    // ======= Hit testing =======

    // Default: bounding-box test.
    // Subclasses with complex shapes should override for precision.
    public boolean contains (float imageX, float imageY) {
        RectF bounds = getBounds();
        return bounds != null && bounds.contains (imageX, imageY);
    }



    // ======= Shape quality =======
    public boolean isRegular()     { return regularityScore() >= 0.95f; }
    public boolean isSymmetrical() { return symmetryScore()   >= 0.95f; }

    // 0.0 = irregular, 1.0 = perfectly regular.
    // Polygon-centric; override for non-polygon shapes.
    public float regularityScore() {
        RectF        b   = getBounds();
        List <PointF> pts = getPoints();

        if (b == null || pts == null || pts.size() < MIN_POLYGON_POINTS) return 0f;

        float cx = b.centerX();
        float cy = b.centerY();

        float avgRadius = 0f;
        for (PointF p : pts) avgRadius += distance (cx, cy, p.x, p.y);
        avgRadius /= pts.size();

        float maxDeviation = 0f;
        for (PointF p : pts) {
            float r = distance (cx, cy, p.x, p.y);
            maxDeviation = Math.max (maxDeviation, Math.abs (r - avgRadius));
        }

        return clamp01 ( 1f - maxDeviation / (avgRadius + 0.0001f) );
    }

    // 0.0 = asymmetric, 1.0 = perfectly symmetric.
    // Override for precise shapes.
    public float symmetryScore() {
        RectF b = getBounds();
        List <PointF> pts = getPoints();
        
        if (b == null || pts == null || pts.size() < MIN_POLYGON_POINTS) return 0f;
        
        float cx = b.centerX();
        float cy = b.centerY();
        
        float totalError = 0f;
        
        for (PointF p : pts) {
            // mirror X
            PointF mx = new PointF (
                2 * cx - p.x,
                p.y
            );
            
            // mirror Y
            PointF my = new PointF (
                p.x,
                2 * cy - p.y
            );
            
            float errX = nearestDistance (pts, mx);
            float errY = nearestDistance (pts, my);
            
            totalError += (errX + errY);
        }
        
        float avgError = totalError / (pts.size() * 2f);
        
        float score = 1f / (1f + avgError);
        
        return clamp01 (score);
    }
    



    // =========================================================
    // PROTECTED METHODS
    // =========================================================
    
    // Returns the distance from candidate to its closest point in the list.
    // Used by symmetryScore() to measure how close a mirror point
        // is to the nearest real vertex.
    // If the list is empty, returns Float.MAX_VALUE.
    protected float nearestDistance (List <PointF> points, PointF candidate) {
        if ( points == null || points.isEmpty() ) return Float.MAX_VALUE;
        
        float minDist = Float.MAX_VALUE;
        
        for (PointF p : points) {
            float d = distance (p, candidate);
            if (d < minDist) minDist = d;
        }
        
        return minDist;
    }
    
    // Clamps a value to [0, 1]. Used by regularity and symmetry scores.
    protected float clamp01 (float v) { return Math.max (0f, Math.min (1f, v)); }

    protected float distance (float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt (dx * dx + dy * dy);
    }

    protected float distance (PointF a, PointF b) { return distance (a.x, a.y, b.x, b.y); }



}
