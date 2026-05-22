package com.magd.multicrop.regions.children.polygon;



import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.magd.multicrop.regions.CropRegion;



// =========================================================
// PolygonRegion
// =========================================================

// Generic polygon crop region.

// =========================================================
// SUPPORTS:

    // Irregular polygons  — freely added points.
    // Regular polygons    — generated via PolygonGenerator:
        // 3  -> Triangle
        // 4  -> Square
        // 5  -> Pentagon
        // 6  -> Hexagon
        // 8  -> Octagon
        // 32 -> Almost circle

// =========================================================
// COORDINATE SPACE:
    // All points are stored in IMAGE SPACE.
    // (0,0) = top-left of source image.

// =========================================================
// RESIZE BEHAVIOR:

    // setEdges() scales all points proportionally to fit
    // the new bounding rect. Shape is preserved.

// =========================================================
// HIT TESTING:

    // Ray-casting algorithm.
    // Works for convex, concave, and complex polygons.

// =========================================================

public class PolygonRegion extends CropRegion {



    // =========================================================
    // VARIABLES
    // =========================================================
    private final List <PointF> points; // Ordered polygon points in IMAGE SPACE.



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    // Empty polygon — use addPoint() to build interactively.
    public PolygonRegion() {
        this.points = new ArrayList<>();
    }

    // From existing point list (deep copy).
    // Used for: deep copying, external geometry creation.
    public PolygonRegion (List <PointF> points) {
        this.points = new ArrayList<>();
        for (PointF p : points) this.points.add ( new PointF (p.x, p.y) );
        getBounds(); // Sync base class bounding fields.
    }

    // Creates a regular polygon centered in a square area.
    public PolygonRegion (int numOfPoints, float imgW) {
        this (numOfPoints, imgW, imgW);
    }

    // Creates a regular polygon centered in the image area.
    // Regular polygon generation delegated to PolygonGenerator.
    public PolygonRegion (int numOfPoints, float imgW, float imgH) {
        this.points = new ArrayList<>();

        if (numOfPoints < MIN_POLYGON_POINTS)         throw new IllegalArgumentException ("Polygon must contain at least 3 points.");
        if (numOfPoints > MAX_REGULAR_POLYGON_POINTS) numOfPoints = MAX_REGULAR_POLYGON_POINTS;

        PolygonGenerator.generate (points, numOfPoints, imgW, imgH);
        getBounds();
    }



    // =========================================================
    // ABSTRACT IMPLEMENTATIONS
    // =========================================================

    @Override
    public Path buildPath() {
        Path path = new Path();
        if ( ! isValid() ) return path;

        path.moveTo (points.get (0).x, points.get (0).y);
        for (int i = 1; i < points.size(); i++) {
            path.lineTo (points.get (i).x, points.get (i).y);
        }
        path.close();

        return path;
    }

    @Override
    public RectF getBounds() {
        if ( points.isEmpty() ) return null;

        float minX =  Float.MAX_VALUE;
        float minY =  Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (PointF p : points) {
            minX = Math.min (minX, p.x);
            minY = Math.min (minY, p.y);
            maxX = Math.max (maxX, p.x);
            maxY = Math.max (maxY, p.y);
        }

        return new RectF (minX, minY, maxX, maxY);
    }

    @Override
    public PolygonRegion copy() {
        PolygonRegion copy = new PolygonRegion (points);
        copy.setVisible ( isVisible() );
        return copy;
    }

    // Scales all points proportionally to fit the new bounding rect.
    @Override
    public void setEdges (float newLeft, float newTop, float newRight, float newBottom) {
        RectF oldBounds = getBounds();
        if (oldBounds == null || points.isEmpty()) return;

        float scaleX = (oldBounds.width()  > 0f) ? (newRight  - newLeft) / oldBounds.width()  : 1f;
        float scaleY = (oldBounds.height() > 0f) ? (newBottom - newTop)  / oldBounds.height() : 1f;

        for (PointF p : points) {
            p.x = newLeft + (p.x - oldBounds.left) * scaleX;
            p.y = newTop  + (p.y - oldBounds.top)  * scaleY;
        }

        super.setEdges (newLeft, newTop, newRight, newBottom);
    }

    @Override
    public void setEdges (RectF rect) { setEdges (rect.left, rect.top, rect.right, rect.bottom); }



    // =========================================================
    // PUBLIC METHODS — GETTERS
    // =========================================================

    @Override public List <PointF> getPoints()   { return Collections.unmodifiableList (points); }
    public int    getPointCount()                { return points.size();  }
    public PointF getPoint (int i)               { return points.get (i); }

    // True if polygon has at least 3 points.
    public boolean isValid() { return points != null && points.size() >= MIN_POLYGON_POINTS; }



    // =========================================================
    // PUBLIC METHODS — POINT MANAGEMENT
    // =========================================================

    public void addPoint    (float x, float y)              { points.add (new PointF (x, y));          }
    public void addPoint    (PointF point)                  { points.add (new PointF (point.x, point.y)); }
    public void insertPoint (int index, float x, float y)   { points.add (index, new PointF (x, y));   }
    public void movePoint   (int index, float x, float y)   { if (index >= 0 && index < points.size()) points.get (index).set (x, y); }
    public void removePoint (int index)                     { if (index >= 0 && index < points.size()) points.remove (index);         }
    public void clearPoints ()                              { points.clear();                           }



    // =========================================================
    // PUBLIC METHODS — HIT TESTING
    // =========================================================

    // Ray-casting polygon hit test.
    // Works for convex, concave, and complex polygons.
    @Override
    public boolean contains (float imageX, float imageY) {
        if ( ! isValid() ) return false;

        int     n      = points.size();
        boolean inside = false;
        int     j      = n - 1;

        for (int i = 0; i < n; i++) {
            float xi = points.get (i).x;
            float yi = points.get (i).y;
            float xj = points.get (j).x;
            float yj = points.get (j).y;

            boolean intersect = (
                ( (yi > imageY) != (yj > imageY) )
                &&
                (
                    imageX
                    <
                    ( (xj - xi) * (imageY - yi) / (yj - yi) ) + xi
                )
            );

            if (intersect) inside = ! inside;
            j = i;
        }

        return inside;
    }



}
