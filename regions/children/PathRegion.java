package com.magd.multicrop.regions.children;



import android.graphics.Path;
import android.graphics.RectF;

import com.magd.multicrop.regions.CropRegion;



// =========================================================
// PathRegion
// =========================================================

// A region defined by an arbitrary pre-built Path.

// =========================================================
// WHY THIS EXISTS:

// Boolean operations (UNION, DIFFERENCE, INTERSECT, XOR)
// produce a Path that is NOT a rectangle, oval, or polygon.
// It is a computed composite shape.

// PathRegion stores that computed Path directly.
// It is the result type for BooleanOpCommand.

// =========================================================
// COORDINATE SPACE:
    // Path is in IMAGE SPACE.
    // (0,0) = top-left of source image.

// =========================================================
// SYNC BEHAVIOR:
    // setEdges() scales the path to fit the new bounding rect.
    // This preserves shape proportions when handles resize the region.

// =========================================================

public class PathRegion extends CropRegion {



    // =========================================================
    // VARIABLES
    // =========================================================
    private Path  path;         // The actual computed path in image space.
    private RectF bounds;       // Cached bounding rect of the path.



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public PathRegion (Path path, RectF bounds) {
        this.path   = new Path (path);   // Defensive copy.
        this.bounds = new RectF (bounds);
    }



    // =========================================================
    // ABSTRACT IMPLEMENTATIONS
    // =========================================================

    @Override
    public Path buildPath() {
        return new Path (path); // Defensive copy — never expose mutable path.
    }

    @Override
    public RectF getBounds() {
        return new RectF (bounds);
    }

    @Override
    public PathRegion copy() {
        PathRegion copy = new PathRegion (path, bounds);
        copy.setVisible (isVisible());
        return copy;
    }

    // Scales the path to fit the new bounding rect.
    // Preserves shape proportions.
    @Override
    public void setEdges (float newLeft, float newTop, float newRight, float newBottom) {
        if (bounds.width() <= 0 || bounds.height() <= 0) return;

        float scaleX = (newRight  - newLeft) / bounds.width();
        float scaleY = (newBottom - newTop)  / bounds.height();

        android.graphics.Matrix m = new android.graphics.Matrix();
        m.setScale (scaleX, scaleY, bounds.left, bounds.top);
        m.postTranslate (newLeft - bounds.left, newTop - bounds.top);

        path.transform (m);

        bounds.set (newLeft, newTop, newRight, newBottom);
    }



    // =========================================================
    // PUBLIC METHODS
    // =========================================================

    // ======= Hit testing =======

    // Bounding box hit test.
    // Precise path hit testing for arbitrary paths is expensive.
    // For interaction purposes, bounding box is sufficient.
    @Override
    public boolean contains (float imageX, float imageY) {
        return bounds.contains (imageX, imageY);
    }



}
