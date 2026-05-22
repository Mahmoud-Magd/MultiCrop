package com.magd.multicrop.views.multi_crop;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import com.magd.multicrop.central.RtVault;
import com.magd.multicrop.enums.ExportMode;
import com.magd.multicrop.enums.FinalCropMode;
import com.magd.multicrop.enums.MaskMode;
import com.magd.multicrop.enums.RegionOperation;
import com.magd.multicrop.export.BitmapExporter;
import com.magd.multicrop.geometry.PathBuilder;
import com.magd.multicrop.geometry.ZoomPanEngine;
import com.magd.multicrop.models.CropRect;
import com.magd.multicrop.regions.CropRegion;
import com.magd.multicrop.regions.children.FreePathRegion;
import com.magd.multicrop.render.TintRenderer;
import com.magd.multicrop.tint.TintLayer;
import com.magd.multicrop.views.crop_area.CropArea;



// =========================================================
// MultiCrop
// =========================================================

// Root View of the MultiCrop library. PUBLIC API.

// =========================================================
// VIEW LAYER ORDER (strict):

    // Layer 0 — image (drawn in onDraw via canvas + matrix)
    // Layer 1 — tint  (drawn in onDraw after image)
    // Layer 2 — CropAreas (child Views, drawn by Android)

// =========================================================
// RESPONSIBILITY:

    // Owns source bitmap, ZoomPanEngine, tint, parallel lists.
    // Draws image + tint in onDraw.
    // Delegates touch to MultiCropTouchHandler.
    // Delegates crop area add/remove to MultiCropAreaManager.
    // Delegates placement to MultiCropPlacer.
    // Delegates Z-order sync to MultiCropLists.

// =========================================================
// TOUCH ROUTING:

    // Two-finger pinch  -> zoom  (MultiCropTouchHandler)
    // One-finger drag   -> pan   (MultiCropTouchHandler)
    // One-finger on CropArea -> CropArea handles it directly

// =========================================================
// COORDINATE SPACES:

    // Image space:  (0,0) = top-left of source bitmap.
    // Screen space: (0,0) = top-left of this View.
    // ZoomPanEngine converts between the two.

// =========================================================

public class MultiCrop extends FrameLayout {



    // =========================================================
    // VARIABLES
    // =========================================================

    // Source image.
    private Bitmap sourceBitmap;      // The original image. Never modified.
    float          imgW;              // Package-private — accessed by MultiCropPlacer.
    float          imgH;              // Package-private — accessed by MultiCropPlacer.

    // Zoom / pan engine. Package-private — accessed by helpers.
    final ZoomPanEngine zoomPan = new ZoomPanEngine();

    // Parallel lists. Package-private — accessed by MultiCropLists + MultiCropAreaManager.
    final List <CropRegion>      regions    = new ArrayList<>();
    final List <RegionOperation> operations = new ArrayList<>();
    final List <CropArea>        cropAreas  = new ArrayList<>();
    final List <Integer>         colors     = new ArrayList<>();

    // Tint.
    private final TintLayer tintLayer    = new TintLayer();
    private       Path      tintMaskPath = new Path();

    // Config.
    private FinalCropMode finalCropMode = FinalCropMode.KEEP_INSIDE;

    // Image paint.
    private final Paint imagePaint = new Paint (Paint.FILTER_BITMAP_FLAG);

    // Helpers.
    private MultiCropTouchHandler  touchHandler;
    private MultiCropAreaManager   areaManager;



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public MultiCrop (Context context)                                   { super (context);                  init (context); }
    public MultiCrop (Context context, AttributeSet attrs)               { super (context, attrs);            init (context); }
    public MultiCrop (Context context, AttributeSet attrs, int defStyle) { super (context, attrs, defStyle);  init (context); }

    private void init (Context context) {
        try { RtVault.init (context); }
            catch (IllegalStateException ignored) {} // Already initialized. Safe to ignore.

        setWillNotDraw (false); // We draw image + tint in onDraw.
        super.setPadding (0, 0, 0, 0);

        touchHandler = new MultiCropTouchHandler (context, this, this::onZoomOrPan);
        areaManager  = new MultiCropAreaManager  (this,          this::onAreaChanged);
    }



    // =========================================================
    // VIEW LIFECYCLE
    // =========================================================

    @Override
    protected void onSizeChanged (int w, int h, int oldW, int oldH) {
        super.onSizeChanged (w, h, oldW, oldH);
        if (w > 0 && h > 0 && sourceBitmap != null) {
            zoomPan.setup (w, h, sourceBitmap.getWidth(), sourceBitmap.getHeight());
            rebuildTintMaskPath();
        }
    }

    // Force padding to always be 0 — it affects coordinate calculations.
    @Override public void setPadding         (int l, int t, int r, int b) { super.setPadding         (0, 0, 0, 0); }
    @Override public void setPaddingRelative (int s, int t, int e, int b) { super.setPaddingRelative (0, 0, 0, 0); }



    // =========================================================
    // DRAWING
    // =========================================================

    @Override
    protected void onDraw (Canvas canvas) {
        super.onDraw (canvas);
        if (sourceBitmap == null) return;

        // Layer 0: image with zoom/pan matrix.
        canvas.save();
        canvas.concat (zoomPan.getMatrix());
        canvas.drawBitmap (sourceBitmap, 0, 0, imagePaint);
        canvas.restore();

        // Layer 1: tint with holes punched by crop mask.
        TintRenderer.draw (canvas, tintLayer, tintMaskPath, getWidth(), getHeight());
    }



    // =========================================================
    // TOUCH
    // =========================================================

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        return touchHandler.onTouchEvent (event);
    }



    // =========================================================
    // PUBLIC API — IMAGE
    // =========================================================

    public Bitmap getSourceBitmap() { return sourceBitmap; }

    // Sets the source bitmap from a file path.
    public void setImageFromPath (String path) {
        if (path == null) throw new IllegalStateException ("Can't set image.\npath == null");
        setImageBitmap (BitmapFactory.decodeFile (path));
    }

    // Sets the source bitmap.
    // Clears all existing crop areas — their image-space coords become meaningless.
    public void setImageBitmap (Bitmap bitmap) {
        if (bitmap == null) throw new IllegalStateException ("Can't set image.\nbitmap == null");

        areaManager.clearCropAreas();

        this.sourceBitmap = bitmap;
        imgW = bitmap.getWidth();
        imgH = bitmap.getHeight();

        if (getWidth() > 0 && getHeight() > 0) zoomPan.setup (getWidth(), getHeight(), imgW, imgH);

        rebuildTintMaskPath();
        invalidate();
    }



    // =========================================================
    // PUBLIC API — TINT
    // =========================================================

    public void setTintColor   (int color)      { tintLayer.setColor   (color);   invalidate(); }
    public void setTintAlpha   (float alpha)     { tintLayer.setAlpha   (alpha);   invalidate(); }
    public void setTintVisible (boolean visible) { tintLayer.setVisible (visible); invalidate(); }



    // =========================================================
    // PUBLIC API — CROP AREAS
    // =========================================================

    public CropArea addRectCrop()                  { return areaManager.addRectCrop();            }
    public CropArea addOvalCrop()                  { return areaManager.addOvalCrop();            }
    public CropArea addPolygonCrop (int numPoints) { return areaManager.addPolygonCrop (numPoints); }

    // Adds a committed FreePathRegion as a new crop area.
    // Called by CropManager.addFreeDrawingCrop() — do not call directly.
    public CropArea addFreeDrawingCrop (FreePathRegion region) {
        return areaManager.addFreeDrawingCrop (region);
    }

    // Adds a CropArea with a custom region.
    // Called by command classes (CopyCommand, ReshapeCommand, etc.)
    // via CropManager.addCropArea(). Do not call directly from UI.
    public CropArea addCropArea (
        CropRegion      region,
        CropRect        initialRect,
        RegionOperation operation,
        int             color
    ) {
        return areaManager.addCropArea (region, initialRect, operation, color);
    }

    public void removeCropArea (CropArea cropArea)  { areaManager.removeCropArea (cropArea); }
    public void clearCropAreas()                    { areaManager.clearCropAreas();          }

    public List <CropArea> getCropAreas()     { return areaManager.getCropAreas(); }
    public FinalCropMode   getFinalCropMode() { return finalCropMode;              }

    public void setFinalCropMode (FinalCropMode mode) {
        this.finalCropMode = mode;
        invalidate();
    }



    // =========================================================
    // PUBLIC API — EXPORT
    // =========================================================

    // Returns one Bitmap per region (INDIVIDUAL) or one combined Bitmap (MERGED).
    public List <Bitmap> export (ExportMode exportMode, MaskMode maskMode) {
        if (sourceBitmap == null) return new ArrayList<>();

        return BitmapExporter.export (
            sourceBitmap,
            regions,
            operations,
            finalCropMode,
            exportMode,
            maskMode
        );
    }



    // =========================================================
    // PUBLIC API — ZOOM
    // =========================================================

    public void resetZoomAndPan() { zoomPan.resetZoomAndPan(); rebuildTintMaskPath(); invalidateAll(); }
    public void resetZoom()       { zoomPan.resetZoom();       rebuildTintMaskPath(); invalidateAll(); }
    public void resetPan()        { zoomPan.resetPan();        rebuildTintMaskPath(); invalidateAll(); }



    // =========================================================
    // PUBLIC API — NOTIFICATIONS
    // =========================================================

    // Called by CropManager after any operation that changes region
    // visibility, rotation, or geometry outside the CropArea touch flow.
    public void notifyRegionChanged() {
        rebuildTintMaskPath();
        invalidateAll();
    }



    // =========================================================
    // PUBLIC API — COORDINATE CONVERSION
    // =========================================================

    // Converts a Path from IMAGE SPACE to SCREEN SPACE.

    // WHY THIS EXISTS ON MultiCrop (not just ZoomPanEngine):
        // ZoomPanEngine is package-private.
        // External classes (FreeDrawSession, etc.) need this bridge
        // without touching ZoomPanEngine directly.

    // WHY dst PARAMETER (not return value):
        // Path.transform(matrix, dst) is allocation-free.
        // Called on every ACTION_MOVE — avoiding allocation
        // prevents GC pauses during live drawing strokes.

    // Example:
        //   Path screenPath = new Path();
        //   multiCrop.imagePathToScreen (region.buildPath(), screenPath);
        //   canvas.drawPath (screenPath, paint);
    public void imagePathToScreen (Path imagePath, Path dst) {
        zoomPan.imagePathToScreen (imagePath, dst);
    }



    // =========================================================
    // PACKAGE-PRIVATE METHODS — FOR HELPER CLASSES ONLY
    // =========================================================

    // Returns the center of the currently visible image area in IMAGE SPACE.
    // Used by MultiCropPlacer when centering new crops.
    PointF getVisibleImageCenter() {
        RectF ios = zoomPan.getImageScreenRect();

        float visLeft   = Math.max (0,            ios.left);
        float visTop    = Math.max (0,            ios.top);
        float visRight  = Math.min (getWidth(),   ios.right);
        float visBottom = Math.min (getHeight(),  ios.bottom);

        float screenCx = (visLeft  + visRight)  / 2f;
        float screenCy = (visTop   + visBottom) / 2f;

        return zoomPan.screenToImage (screenCx, screenCy);
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    // Fired by MultiCropTouchHandler after every zoom or pan.
    private void onZoomOrPan() {
        rebuildTintMaskPath();
        invalidateAll();
    }

    // Fired by MultiCropAreaManager after every add / remove / clear.
    private void onAreaChanged() {
        rebuildTintMaskPath();
        invalidate();
    }

    // Rebuilds the tint mask path in screen space from current regions.
    private void rebuildTintMaskPath() {
        Path imagePath = PathBuilder.build (regions, operations);
        tintMaskPath   = new Path();
        imagePathToScreen (imagePath, tintMaskPath);
    }

    // Invalidates this view and all CropArea children.
    private void invalidateAll() {
        invalidate();
        for (CropArea ca : cropAreas) ca.invalidate();
    }



}
