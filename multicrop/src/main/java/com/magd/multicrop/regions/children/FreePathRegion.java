package com.magd.multicrop.regions.children;



import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import com.magd.multicrop.regions.CropRegion;



// =========================================================
// FreePathRegion
// =========================================================

// Freehand drawn path region — always LIVE build strategy.
// Stamps union directly into the stored Path as the user draws.

// =========================================================
// DRAWING MODES:

    // FREEHAND:
        // Smooth quadratic Bezier curves through recorded points.
        // No thickness — pure skeletal path.
        // Good for: lasso selection, outline mask.

    // STAMP_ROUND:
        // Each finger point stamps a circle (addOval).
        // Diameter = strokeWidth.
        // Stamps union into the growing Path.
        // Gap-fill: stamps every (radius * 0.5) px between drag events.
        // Good for: brush mask, soft eraser (with DIFFERENCE operation).

    // STAMP_SHARP:
        // Same as STAMP_ROUND but stamps axis-aligned rectangles.
        // Good for: hard-edge brush mask, pixel eraser.

// =========================================================
// setEdges() — Matrix scaling:

    // Applies a scale + translate Matrix to the entire stored Path.
    // old bounds → new bounds = one Matrix transform.
    // Works on any Path shape regardless of complexity.
    // Called by CropArea when user drags a resize handle.

// =========================================================
// restorePath() — session undo support:

    // FreeDrawSession snapshots the path before each stroke.
    // restorePath() replaces the current path + bounds with a snapshot.
    // Called by FreeDrawSession.undo() / redo().

// =========================================================
// COORDINATE SPACE:
    // All coordinates in IMAGE SPACE.
    // (0,0) = top-left of source image.

// =========================================================

public class FreePathRegion extends CropRegion {



    // =========================================================
    // ENUMS
    // =========================================================

    public enum DrawMode {
        FREEHAND,     // Smooth Bezier curves. No thickness.
        STAMP_ROUND,  // Circle stamps. Round edges.
        STAMP_SHARP   // Rect stamps. Sharp edges.
    }



    // =========================================================
    // CONSTANTS
    // =========================================================

    // Stamps placed every (radius * GAP_FILL_RATIO) px along each drag segment.
    private static final float GAP_FILL_RATIO    = 0.5f;

    // Minimum travel before a FREEHAND point is recorded.
    private static final float MIN_FREEHAND_STEP = 1f;



    // =========================================================
    // VARIABLES
    // =========================================================

    private DrawMode drawMode;    // Current drawing mode.
    private float    strokeWidth; // Stamp diameter in image-space px. STAMP modes only.
    private boolean  closePath;   // Whether to close path on buildPath(). FREEHAND only.

    // The single live Path — all stamps / curves union into this.
    private final Path path = new Path();

    // Gap fill: last stamp position.
    private float lastStampX = Float.NaN;
    private float lastStampY = Float.NaN;

    // FREEHAND incremental Bezier state.
    private float prevFreeX = Float.NaN; // Previous finger point (quadTo control).
    private float prevFreeY = Float.NaN;

    // Bounding box — maintained incrementally.
    private float boundsLeft   =  Float.MAX_VALUE;
    private float boundsTop    =  Float.MAX_VALUE;
    private float boundsRight  = -Float.MAX_VALUE;
    private float boundsBottom = -Float.MAX_VALUE;



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    // Default: FREEHAND, strokeWidth 20, closePath true.
    public FreePathRegion() {
        this (DrawMode.FREEHAND, 20f, true);
    }

    // Convenience: mode + thickness. closePath = true.
    public FreePathRegion (DrawMode drawMode, float strokeWidth) {
        this (drawMode, strokeWidth, true);
    }

    // Full constructor.
    public FreePathRegion (DrawMode drawMode, float strokeWidth, boolean closePath) {
        this.drawMode    = drawMode;
        this.strokeWidth = Math.max (1f, strokeWidth);
        this.closePath   = closePath;
    }

    // Private copy constructor — used by copy().
    private FreePathRegion (FreePathRegion src) {
        this.drawMode    = src.drawMode;
        this.strokeWidth = src.strokeWidth;
        this.closePath   = src.closePath;

        this.path.set (src.path);

        this.lastStampX  = src.lastStampX;
        this.lastStampY  = src.lastStampY;
        this.prevFreeX   = src.prevFreeX;
        this.prevFreeY   = src.prevFreeY;

        this.boundsLeft   = src.boundsLeft;
        this.boundsTop    = src.boundsTop;
        this.boundsRight  = src.boundsRight;
        this.boundsBottom = src.boundsBottom;
    }



    // =========================================================
    // ABSTRACT IMPLEMENTATIONS
    // =========================================================

    // Returns a defensive copy of the live path.
    // FREEHAND: closes the path if closePath = true.
    @Override
    public Path buildPath() {
        Path copy = new Path (path);
        if (drawMode == DrawMode.FREEHAND && closePath && !path.isEmpty()) copy.close();
        return copy;
    }

    @Override
    public RectF getBounds() {
        if (boundsLeft > boundsRight || boundsTop > boundsBottom) return null;
        return new RectF (boundsLeft, boundsTop, boundsRight, boundsBottom);
    }

    @Override
    public FreePathRegion copy() {
        FreePathRegion c = new FreePathRegion (this);
        c.setVisible (isVisible());
        return c;
    }

    // Scales the entire live Path to fit the new bounding box.
    // Uses a Matrix: scale + translate from old bounds → new bounds.
    @Override
    public void setEdges (float newLeft, float newTop, float newRight, float newBottom) {
        RectF old = getBounds();

        if (old != null && !path.isEmpty()) {
            float scaleX = (old.width()  > 0f) ? (newRight  - newLeft) / old.width()  : 1f;
            float scaleY = (old.height() > 0f) ? (newBottom - newTop)  / old.height() : 1f;

            Matrix m = new Matrix();
            m.setScale      (scaleX, scaleY, old.left, old.top);
            m.postTranslate (newLeft - old.left, newTop - old.top);

            path.transform (m);
        }

        boundsLeft   = newLeft;
        boundsTop    = newTop;
        boundsRight  = newRight;
        boundsBottom = newBottom;

        super.setEdges (newLeft, newTop, newRight, newBottom);
    }

    @Override public float getLeft()   { return boundsLeft;   }
    @Override public float getTop()    { return boundsTop;    }
    @Override public float getRight()  { return boundsRight;  }
    @Override public float getBottom() { return boundsBottom; }



    // =========================================================
    // PUBLIC METHODS — STROKE RECORDING
    // =========================================================

    // ─── ACTION_DOWN ────────────────────────────────────────

    public void startStroke (float imageX, float imageY) {
        switch (drawMode) {

            case FREEHAND:
                path.moveTo (imageX, imageY);
                prevFreeX = imageX;
                prevFreeY = imageY;
                expandBoundsPoint (imageX, imageY);
                break;

            case STAMP_ROUND:
            case STAMP_SHARP:
                lastStampX = imageX;
                lastStampY = imageY;
                stampOnto (imageX, imageY);
                break;
        }
    }



    // ─── ACTION_MOVE ────────────────────────────────────────

    public void continueStroke (float imageX, float imageY) {
        switch (drawMode) {

            case FREEHAND:
                float dx = imageX - prevFreeX;
                float dy = imageY - prevFreeY;
                if ( (dx * dx + dy * dy) < (MIN_FREEHAND_STEP * MIN_FREEHAND_STEP) ) return;

                // Midpoint smoothing: quadTo (prevPoint, midpoint between prev and current).
                float midX = (prevFreeX + imageX) / 2f;
                float midY = (prevFreeY + imageY) / 2f;
                path.quadTo (prevFreeX, prevFreeY, midX, midY);

                prevFreeX = imageX;
                prevFreeY = imageY;
                expandBoundsPoint (imageX, imageY);
                break;

            case STAMP_ROUND:
            case STAMP_SHARP:
                stampWithGapFill (imageX, imageY);
                break;
        }
    }



    // ─── ACTION_UP ──────────────────────────────────────────

    public void endStroke (float imageX, float imageY) {
        switch (drawMode) {

            case FREEHAND:
                path.lineTo (imageX, imageY);
                expandBoundsPoint (imageX, imageY);
                prevFreeX = Float.NaN;
                prevFreeY = Float.NaN;
                break;

            case STAMP_ROUND:
            case STAMP_SHARP:
                stampWithGapFill (imageX, imageY);
                lastStampX = Float.NaN;
                lastStampY = Float.NaN;
                break;
        }
    }



    // =========================================================
    // PUBLIC METHODS — SESSION SUPPORT
    // =========================================================

    // Replaces the current path + bounds with a snapshot.
    // Called by FreeDrawSession.undo() / redo().
    public void restorePath (Path snapshot) {
        path.set (snapshot);

        // Recompute bounds from the restored path.
        RectF computed = new RectF();
        snapshot.computeBounds (computed, true);

        if (computed.isEmpty()) {
            resetBounds();
        } else {
            // For STAMP modes, expand by radius so bounds include stamp edges.
            float r = isStampMode() ? strokeWidth / 2f : 0f;
            boundsLeft   = computed.left   - r;
            boundsTop    = computed.top    - r;
            boundsRight  = computed.right  + r;
            boundsBottom = computed.bottom + r;
        }

        super.setEdges (boundsLeft, boundsTop, boundsRight, boundsBottom);
    }

    // Returns true if no drawing has been done yet.
    public boolean isEmpty() { return path.isEmpty(); }

    // Clears all drawing.
    public void clear() {
        path.reset();
        lastStampX = Float.NaN;
        lastStampY = Float.NaN;
        prevFreeX  = Float.NaN;
        prevFreeY  = Float.NaN;
        resetBounds();
    }



    // =========================================================
    // PUBLIC METHODS — GETTERS / SETTERS
    // =========================================================

    public DrawMode getDrawMode()    { return drawMode;    }
    public float    getStrokeWidth() { return strokeWidth; }
    public boolean  isClosePath()    { return closePath;   }

    public boolean isStampMode() {
        return drawMode == DrawMode.STAMP_ROUND || drawMode == DrawMode.STAMP_SHARP;
    }

    // Affects future stamps only. Past stamps are baked into the live path.
    public void setStrokeWidth (float strokeWidth) { this.strokeWidth = Math.max (1f, strokeWidth); }
    public void setDrawMode    (DrawMode mode)     { this.drawMode    = mode; }
    public void setClosePath   (boolean close)     { this.closePath   = close; }



    // =========================================================
    // PUBLIC METHODS — HIT TESTING
    // =========================================================

    @Override
    public boolean contains (float imageX, float imageY) {
        RectF b = getBounds();
        return b != null && b.contains (imageX, imageY);
    }



    // =========================================================
    // PRIVATE METHODS — STAMP
    // =========================================================

    // Gap-fills stamps from lastStampXY to the new point.
    private void stampWithGapFill (float imageX, float imageY) {
        if (Float.isNaN (lastStampX)) {
            stampOnto (imageX, imageY);
            lastStampX = imageX;
            lastStampY = imageY;
            return;
        }

        float dx       = imageX - lastStampX;
        float dy       = imageY - lastStampY;
        float distance = (float) Math.sqrt (dx * dx + dy * dy);
        float radius   = strokeWidth / 2f;
        float step     = Math.max (1f, radius * GAP_FILL_RATIO);

        if (distance <= step) {
            stampOnto (imageX, imageY);
        } else {
            int steps = (int) Math.ceil (distance / step);
            for (int i = 1; i <= steps; i++) {
                float t  = (float) i / steps;
                stampOnto (lastStampX + dx * t, lastStampY + dy * t);
            }
        }

        lastStampX = imageX;
        lastStampY = imageY;
    }

    // Unions one circle or rect stamp into the live path.
    private void stampOnto (float cx, float cy) {
        float r = strokeWidth / 2f;

        Path stamp = new Path();

        switch (drawMode) {
            case STAMP_ROUND:
                stamp.addOval (new RectF (cx - r, cy - r, cx + r, cy + r), Path.Direction.CW);
                break;
            case STAMP_SHARP:
                stamp.addRect (new RectF (cx - r, cy - r, cx + r, cy + r), Path.Direction.CW);
                break;
            default:
                return;
        }

        path.op (stamp, Path.Op.UNION);
        expandBoundsStamp (cx, cy);
    }



    // =========================================================
    // PRIVATE METHODS — BOUNDS
    // =========================================================

    private void expandBoundsStamp (float cx, float cy) {
        float r = strokeWidth / 2f;
        if (cx - r < boundsLeft)   boundsLeft   = cx - r;
        if (cy - r < boundsTop)    boundsTop    = cy - r;
        if (cx + r > boundsRight)  boundsRight  = cx + r;
        if (cy + r > boundsBottom) boundsBottom = cy + r;
    }

    private void expandBoundsPoint (float x, float y) {
        if (x < boundsLeft)   boundsLeft   = x;
        if (y < boundsTop)    boundsTop    = y;
        if (x > boundsRight)  boundsRight  = x;
        if (y > boundsBottom) boundsBottom = y;
    }

    private void resetBounds() {
        boundsLeft   =  Float.MAX_VALUE;
        boundsTop    =  Float.MAX_VALUE;
        boundsRight  = -Float.MAX_VALUE;
        boundsBottom = -Float.MAX_VALUE;
    }



}
