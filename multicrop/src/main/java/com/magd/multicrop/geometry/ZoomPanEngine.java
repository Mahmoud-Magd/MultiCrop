package com.magd.multicrop.geometry;



import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;



// =========================================================
// ZoomPanEngine
// =========================================================

// Manages the zoom and pan transform for the image display.

// =========================================================
// TWO COORDINATE SPACES:

    // IMAGE SPACE:
        // (0, 0) = top-left of source image.
        // All regions, paths, and nodes live here.
        // Never changes regardless of zoom or pan.

    // SCREEN SPACE:
        // (0, 0) = top-left of the view.
        // Touch events arrive in screen space.
        // Drawing happens in screen space.

// =========================================================
// THE MATRIX:

    // One Matrix converts image space → screen space.
    // Its inverse converts screen space → image space.
    // Both are kept in sync after every zoom / pan.

// =========================================================
// ZOOM LIMITS:

    // MIN_SCALE: computed from fit-to-view scale × 0.8.
    // MAX_SCALE: 30x — sufficient for pixel-level work.

// =========================================================

public class ZoomPanEngine {



    // =========================================================
    // CONSTANTS
    // =========================================================
    private static final float MAX_SCALE = 30f;   // Maximum zoom level.
    private static final float MIN_SCALE = 0.01f; // Fallback minimum (refined by fitToView).



    // =========================================================
    // VARIABLES
    // =========================================================
    private final Matrix matrix        = new Matrix(); // Image → screen transform.
    private final Matrix inverseMatrix = new Matrix(); // Screen → image (cached inverse).

    private float viewWidth   = 0f;
    private float viewHeight  = 0f;
    private float imageWidth  = 0f;
    private float imageHeight = 0f;
    private float minScale    = MIN_SCALE; // Refined after fitToView.



    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    public ZoomPanEngine() {}



    // =========================================================
    // PUBLIC METHODS — SETUP
    // =========================================================

    // Must be called when view size is known or image changes.
    // Fits image inside view and resets zoom.
    public void setup (float viewWidth, float viewHeight, float imageWidth, float imageHeight) {
        this.viewWidth   = viewWidth;
        this.viewHeight  = viewHeight;
        this.imageWidth  = imageWidth;
        this.imageHeight = imageHeight;

        resetZoomAndPan();
    }

    // Call this from View.onSizeChanged().
    public void onViewSizeChanged (float newWidth, float newHeight) {
        if (newWidth <= 0 || newHeight <= 0) return;
        viewWidth  = newWidth;
        viewHeight = newHeight;
        if (imageWidth > 0 && imageHeight > 0) resetZoomAndPan();
    }



    // =========================================================
    // PUBLIC METHODS — RESET
    // =========================================================

    // Fits image inside view, centered, at optimal scale.
    // Resets BOTH zoom and pan.
    public void resetZoomAndPan() {
        if (imageWidth <= 0 || imageHeight <= 0) return;
        if (viewWidth  <= 0 || viewHeight  <= 0) return;

        float scale = Math.min (viewWidth / imageWidth, viewHeight / imageHeight);
        float dx    = (viewWidth  - imageWidth  * scale) / 2f;
        float dy    = (viewHeight - imageHeight * scale) / 2f;

        matrix.reset();
        matrix.postScale     (scale, scale);
        matrix.postTranslate (dx, dy);

        minScale = scale * 0.8f;

        updateInverse();
    }

    // Resets ZOOM only — restores fit-to-view scale.
    // Does NOT change pan beyond what re-centering requires.
    public void resetZoom() {
        if (imageWidth <= 0 || viewWidth <= 0) return;

        float fitScale     = Math.min (viewWidth / imageWidth, viewHeight / imageHeight);
        float currentScale = getScale();
        if (currentScale <= 0) return;

        float[] v  = matrixValues();
        float   cx = v [Matrix.MTRANS_X] + imageWidth  * currentScale / 2f;
        float   cy = v [Matrix.MTRANS_Y] + imageHeight * currentScale / 2f;

        matrix.postScale (fitScale / currentScale, fitScale / currentScale, cx, cy);
        clampTranslation();
        updateInverse();
    }

    // Resets PAN only — re-centers image at current zoom level.
    // Does NOT touch the scale.
    public void resetPan() {
        if (imageWidth <= 0 || viewWidth <= 0) return;

        float   scale = getScale();
        float[] v     = matrixValues();
        v [Matrix.MTRANS_X] = (viewWidth  - imageWidth  * scale) / 2f;
        v [Matrix.MTRANS_Y] = (viewHeight - imageHeight * scale) / 2f;
        matrix.setValues (v);
        updateInverse();
    }



    // =========================================================
    // PUBLIC METHODS — ZOOM
    // =========================================================

    // Zooms by scaleFactor, centered at (focusX, focusY) in screen space.
    // focusX / focusY: the screen point that stays stationary during zoom.
    public void zoom (float scaleFactor, float focusX, float focusY) {
        float currentScale = getScale();
        float newScale     = Math.max (minScale, Math.min (currentScale * scaleFactor, MAX_SCALE));
        float factor       = newScale / currentScale;

        matrix.postScale (factor, factor, focusX, focusY);
        clampTranslation();
        updateInverse();
    }



    // =========================================================
    // PUBLIC METHODS — PAN
    // =========================================================

    // Translates the view by (dx, dy) in screen space.
    public void pan (float dx, float dy) {
        matrix.postTranslate (dx, dy);
        clampTranslation();
        updateInverse();
    }



    // =========================================================
    // PUBLIC METHODS — COORDINATE CONVERSION
    // =========================================================

    // Converts a point from screen space to image space.
    public PointF screenToImage (float screenX, float screenY) {
        float[] pt = { screenX, screenY };
        inverseMatrix.mapPoints (pt);
        return new PointF (pt [0], pt [1]);
    }

    // Converts a point from image space to screen space.
    public PointF imageToScreen (float imageX, float imageY) {
        float[] pt = { imageX, imageY };
        matrix.mapPoints (pt);
        return new PointF (pt [0], pt [1]);
    }

    // Transforms a Path from image space to screen space.
    public void imagePathToScreen (Path src, Path dst) {
        src.transform (matrix, dst);
    }

    // Transforms a RectF from image space to screen space.
    public RectF imageRectToScreen (RectF imageRect) {
        RectF screenRect = new RectF();
        matrix.mapRect (screenRect, imageRect);
        return screenRect;
    }



    // =========================================================
    // PUBLIC METHODS — GETTERS
    // =========================================================

    public Matrix getMatrix()          { return matrix; } // Returns the current image-to-screen matrix.
    public float  getScale()           { return matrixValues() [Matrix.MSCALE_X]; }
    public float  getTranslationX()    { return matrixValues() [Matrix.MTRANS_X]; }
    public float  getTranslationY()    { return matrixValues() [Matrix.MTRANS_Y]; }

    // Returns the image rect mapped to its current screen position.
    public RectF  getImageScreenRect() { return imageRectToScreen ( new RectF (0, 0, imageWidth, imageHeight) ); }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    // Prevents the image from being panned completely off-screen.
    // Keeps at least 20% of the image always visible.
    private void clampTranslation() {
        float scale = getScale();
        float scaledW = imageWidth  * scale;
        float scaledH = imageHeight * scale;
        float tx      = getTranslationX();
        float ty      = getTranslationY();

        float marginX = Math.min (scaledW * 0.2f, viewWidth  * 0.5f);
        float marginY = Math.min (scaledH * 0.2f, viewHeight * 0.5f);

        float minTx = - (scaledW - marginX);
        float maxTx = viewWidth  - marginX;
        float minTy = - (scaledH - marginY);
        float maxTy = viewHeight - marginY;

        float clampedTx = Math.max (minTx, Math.min (tx, maxTx));
        float clampedTy = Math.max (minTy, Math.min (ty, maxTy));

        if (clampedTx != tx || clampedTy != ty) {
            matrix.postTranslate (clampedTx - tx, clampedTy - ty);
        }
    }

    // Keeps the inverse matrix in sync with the main matrix.
    private void updateInverse() { matrix.invert (inverseMatrix); }

    // Extracts all 9 matrix values into an array.
    private float[] matrixValues() {
        float[] values = new float [9];
        matrix.getValues (values);
        return values;
    }



}
