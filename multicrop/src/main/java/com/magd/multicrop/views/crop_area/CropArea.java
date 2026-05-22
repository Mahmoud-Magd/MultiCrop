package com.magd.multicrop.views.crop_area;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.magd.multicrop.enums.GuideLineMode;
import com.magd.multicrop.enums.HandleType;
import com.magd.multicrop.enums.TouchTarget;

import com.magd.multicrop.geometry.HandlePositionCalculator;
import com.magd.multicrop.geometry.ResizeEngine;
import com.magd.multicrop.geometry.ZoomPanEngine;

import com.magd.multicrop.models.CropRect;

import com.magd.multicrop.regions.CropRegion;

import com.magd.multicrop.render.GuideLinesDrawer;
import com.magd.multicrop.render.handle.HandleDrawer;

import com.magd.multicrop.utils.DimensUtils;



// =========================================================
// CropArea
// =========================================================

// A single interactive crop shape drawn on screen.

// =========================================================
// RESPONSIBILITY:

// CropArea is the interactive layer.
// It handles:
    // Drawing the region border
    // Drawing the 8 resize handles
    // Drawing the 3x3 guidelines
    // Touch input (move + resize)
    // Converting touches to image space via ZoomPanEngine

// It does NOT:
    // Draw the tint (that is MultiCrop's responsibility)
    // Know about other CropAreas
    // Export bitmaps

// =========================================================
// PIN BEHAVIOR:

    // When pinned:
        // All touch events return false immediately.
        // The region cannot be moved or resized via touch.
        // The border and handles are still drawn (unless hidden separately).
        // The region stays fully active in tint + export.

// =========================================================
// CROP AREA HIDDEN:

    // setCropAreaVisible(false) sets alpha = 0.
    // The view is invisible but still receives touch events.
    // Combined with pin: completely invisible AND locked.
    // Combined without pin: invisible but still movable/resizable.

// =========================================================
// COORDINATE SPACES:

// CropRect → IMAGE SPACE (source of truth).
// Drawing  → SCREEN SPACE (converted via ZoomPanEngine before drawing).
// Touch    → SCREEN SPACE on arrival, converted to IMAGE SPACE before use.

// =========================================================
// TOUCH PRIORITY (strict order):
    // 0. Pinned → reject all touch (return false).
    // 1. Handle touched → RESIZE.
    // 2. Inside region → MOVE.
    // 3. Outside → IGNORE (return false).

// =========================================================

public class CropArea extends View {



    // =========================================================
    // CALLBACKS
    // =========================================================

    public interface ChangeCallback {
        void onChanged (CropArea area);
    }

    public interface BringToFrontCallback {
        void onBroughtToFront (CropArea area);
    }



    // =========================================================
    // CONSTANTS
    // =========================================================
    private static final float BORDER_STROKE_DP = 2f;



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropRect   cropRect;
    private final CropRegion region;
    private final int        color;

    private final Paint            borderPaint;
    private final GuideLinesDrawer guideLines;
    private final HandleDrawer[]   handles;

    private ZoomPanEngine zoomPan;

    // Touch state.
    private TouchTarget activeTarget    = TouchTarget.NONE;
    private float       touchDownImageX = 0f;
    private float       touchDownImageY = 0f;

    private final CropRect rectAtTouchDown = new CropRect (0, 0, 1, 1);
    private final CropRect lastValidRect   = new CropRect (0, 0, 1, 1);

    // State.
    private boolean isActive        = false;
    private boolean pinned          = false; // When true: all touch is rejected.
    private boolean cropAreaVisible = true;  // When false: alpha = 0, view invisible.

    private ChangeCallback       changeCallback;
    private BringToFrontCallback bringToFrontCallback;



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public CropArea (
        Context       context,
        CropRegion    region,
        CropRect      initialRect,
        int           color,
        ZoomPanEngine zoomPan
    ) {
        super (context);

        this.region   = region;
        this.color    = color;
        this.zoomPan  = zoomPan;
        this.cropRect = initialRect.copy();

        setWillNotDraw (false);

        borderPaint = new Paint (Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle (Paint.Style.STROKE);
        borderPaint.setColor (color);
        borderPaint.setStrokeWidth (DimensUtils.dpToPx (BORDER_STROKE_DP));
        borderPaint.setAlpha (204);

        guideLines = new GuideLinesDrawer (GuideLineMode.ON_TOUCH);
        guideLines.setColor (color);

        handles = new HandleDrawer [HandleType.values().length];
        HandleType[] types = HandleType.values();
        for (int i = 0; i < types.length; i++) {
            handles [i] = new HandleDrawer (types [i]);
            handles [i].setColor (color);
        }
    }



    // =========================================================
    // DRAWING
    // =========================================================

    @Override
    protected void onDraw (Canvas canvas) {
        if (zoomPan == null) return;

        RectF    screenRectF = zoomPan.imageRectToScreen (cropRect.toRectF());
        CropRect screenRect  = new CropRect (
            screenRectF.left, screenRectF.top,
            screenRectF.right, screenRectF.bottom
        );

        canvas.drawRect (screenRectF, borderPaint);

        boolean isInteracting = (activeTarget != TouchTarget.NONE);
        guideLines.draw (canvas, screenRect, isInteracting);

        HandleType[] types = HandleType.values();
        for (int i = 0; i < types.length; i++) {
            PointF center = HandlePositionCalculator.getCenter (types [i], screenRect);
            handles [i].draw (canvas, center.x, center.y);
        }
    }



    // =========================================================
    // TOUCH HANDLING
    // =========================================================

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        if (zoomPan == null) return false;

        // Pinned: reject ALL touch. Region is locked in image space.
        if (pinned) return false;

        float  screenX    = event.getX();
        float  screenY    = event.getY();
        PointF imagePoint = zoomPan.screenToImage (screenX, screenY);
        float  imageX     = imagePoint.x;
        float  imageY     = imagePoint.y;

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: {
                TouchTarget target = resolveTouchTarget (screenX, screenY);
                if (target == TouchTarget.NONE) return false;

                activeTarget    = target;
                touchDownImageX = imageX;
                touchDownImageY = imageY;
                rectAtTouchDown.set (cropRect);
                lastValidRect.set   (cropRect);

                setActive (true);
                if (bringToFrontCallback != null) bringToFrontCallback.onBroughtToFront (this);
                bringToFront();
                getParent().requestDisallowInterceptTouchEvent (true);
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                if (activeTarget == TouchTarget.NONE) return false;

                float dx = imageX - touchDownImageX;
                float dy = imageY - touchDownImageY;

                if (activeTarget == TouchTarget.BODY) {
                    cropRect.set (rectAtTouchDown);
                    cropRect.offset (dx, dy, getImageWidth(), getImageHeight());
                } else {
                    HandleType handleType = CropAreaMaps.toHandleType (activeTarget);
                    if (handleType != null) {
                        CropRect proposed = rectAtTouchDown.copy();
                        boolean  applied  = ResizeEngine.apply (
                            handleType, dx, dy, proposed,
                            getImageWidth(), getImageHeight()
                        );
                        if (applied) {
                            cropRect.set (proposed);
                            lastValidRect.set (proposed);
                        } else {
                            cropRect.set (lastValidRect);
                        }
                    }
                }

                region.setEdges (cropRect.toRectF());
                fireChanged();
                invalidate();
                return true;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                activeTarget = TouchTarget.NONE;
                getParent().requestDisallowInterceptTouchEvent (false);
                invalidate();
                return true;
            }
        }

        return false;
    }



    // =========================================================
    // PUBLIC API
    // =========================================================

    public CropRect   getCropRect() { return cropRect; }
    public CropRegion getRegion()   { return region;   }
    public int        getColor()    { return color;    }
    public boolean    isActive()    { return isActive; }
    public boolean    isPinned()    { return pinned;   }

    public void setActive  (boolean active) { this.isActive = active; invalidate(); }
    public void setZoomPan (ZoomPanEngine z){ this.zoomPan  = z;      invalidate(); }

    // Syncs pin state from CropEntry.
    // Pinned = all touch rejected. Drawing still happens normally.
    public void setPinned (boolean pinned) {
        this.pinned = pinned;
        // No invalidate needed — pinned doesn't change drawing.
    }

    // Shows or hides border + handles by setting alpha.
    // Alpha = 0: invisible but touch still routes here (unless pinned).
    // Alpha = 1: fully visible.
    public void setCropAreaVisible (boolean visible) {
        this.cropAreaVisible = visible;
        setAlpha (visible ? 1f : 0f);
        // No invalidate needed — setAlpha triggers it.
    }

    public boolean isCropAreaVisible() { return cropAreaVisible; }

    public void setChangeCallback       (ChangeCallback cb)       { this.changeCallback       = cb; }
    public void setBringToFrontCallback (BringToFrontCallback cb) { this.bringToFrontCallback = cb; }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    private TouchTarget resolveTouchTarget (float screenX, float screenY) {
        RectF    rf         = zoomPan.imageRectToScreen (cropRect.toRectF());
        CropRect screenRect = new CropRect (rf.left, rf.top, rf.right, rf.bottom);

        HandleType[] types = HandleType.values();
        for (int i = 0; i < types.length; i++) {
            PointF center    = HandlePositionCalculator.getCenter (types [i], screenRect);
            float  hitRadius = handles [i].getHalfHitSize_px();

            if (
                Math.abs (screenX - center.x) <= hitRadius
                &&
                Math.abs (screenY - center.y) <= hitRadius
            ) {
                return CropAreaMaps.toTouchTarget (types [i]);
            }
        }

        if (screenRect.contains (screenX, screenY)) return TouchTarget.BODY;

        return TouchTarget.NONE;
    }

    private void fireChanged() {
        if (changeCallback != null) changeCallback.onChanged (this);
    }

    private float getImageWidth() {
        RectF  r  = zoomPan.getImageScreenRect();
        PointF tl = zoomPan.screenToImage (r.left, r.top);
        PointF br = zoomPan.screenToImage (r.right, r.bottom);
        return br.x - tl.x;
    }

    private float getImageHeight() {
        RectF  r  = zoomPan.getImageScreenRect();
        PointF tl = zoomPan.screenToImage (r.left, r.top);
        PointF br = zoomPan.screenToImage (r.right, r.bottom);
        return br.y - tl.y;
    }



}
