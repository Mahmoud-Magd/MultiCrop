package com.magd.multicrop.views.multi_crop;



import android.graphics.RectF;
import android.widget.FrameLayout;

import com.magd.multicrop.enums.RegionOperation;
import com.magd.multicrop.models.CropRect;
import com.magd.multicrop.regions.CropRegion;
import com.magd.multicrop.regions.children.FreePathRegion;
import com.magd.multicrop.regions.children.OvalRegion;
import com.magd.multicrop.regions.children.polygon.PolygonRegion;
import com.magd.multicrop.utils.ColorAssigner;
import com.magd.multicrop.views.crop_area.CropArea;

import java.util.Collections;
import java.util.List;



// =========================================================
// MultiCropAreaManager
// =========================================================

// Owns all crop area add / remove / clear logic for MultiCrop.

// =========================================================
// RESPONSIBILITY:

    // Creates CropArea views for each region type.
    // Maintains the four parallel lists in MultiCrop:
        // regions, operations, cropAreas, colors.
    // Wires CropArea callbacks back into MultiCrop.
    // Handles placement via MultiCropPlacer.

// =========================================================
// PARALLEL LISTS (owned by MultiCrop, mutated here):

    // regions    — CropRegion geometry objects.
    // operations — RegionOperation per region.
    // cropAreas  — CropArea view per region.
    // colors     — assigned color per region.

    // All four lists are index-synchronized.
    // MultiCropLists handles Z-order re-sync on bringToFront.

// =========================================================
// CALLBACK:

    // MultiCropAreaManager.Callback is implemented by MultiCrop.
    // After every add / remove / clear, the manager fires:
        // onAreaChanged() — MultiCrop rebuilds tint + invalidates.

// =========================================================
// WHY EXTRACTED:

    // Keeps MultiCrop under 300 lines.
    // All region-type construction lives in one place.
    // Adding a new region type only touches this file.

// =========================================================

final class MultiCropAreaManager {



    // =========================================================
    // CALLBACK
    // =========================================================

    // Implemented by MultiCrop.
    // Called after any structural change to the crop area lists.
    interface Callback {
        void onAreaChanged();
    }



    // =========================================================
    // VARIABLES
    // =========================================================

    private final MultiCrop mc;
    private final Callback  callback;



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    MultiCropAreaManager (MultiCrop mc, Callback callback) {
        this.mc       = mc;
        this.callback = callback;
    }



    // =========================================================
    // PACKAGE-PRIVATE METHODS — REGION FACTORIES
    // =========================================================

    // Adds a rectangle crop (4-sided polygon) centered on visible area.
    CropArea addRectCrop() {
        return addPolygonCrop (4);
    }

    // Adds a polygon crop centered on the visible image area.
    CropArea addPolygonCrop (int numOfPoints) {
        checkImageSet();
        PolygonRegion region = new PolygonRegion (numOfPoints, mc.imgW, mc.imgH);
        MultiCropPlacer.center (mc, region);
        return addCropArea (region, new CropRect (region), RegionOperation.UNION, ColorAssigner.next (mc.colors));
    }

    // Adds an oval crop centered on the visible image area.
    CropArea addOvalCrop() {
        checkImageSet();
        OvalRegion region = new OvalRegion (mc.imgW, mc.imgH);
        MultiCropPlacer.center (mc, region);
        return addCropArea (region, new CropRect (region), RegionOperation.UNION, ColorAssigner.next (mc.colors));
    }

    // Adds a committed FreePathRegion as a new crop area.

    // WHO CALLS THIS:
        // MultiCrop.addFreeDrawingCrop() — the public API entry point.
        // Internally called by CropManager.addFreeDrawingCrop()
        //   via multiCrop.addFreeDrawingCrop().

    // NOTE:
        // Does NOT go through CropHistoryManager.
        // The FreeDrawSession itself is the undo unit.
        // Once committed, undo removes the entry via DeleteCommand.
    CropArea addFreeDrawingCrop (FreePathRegion region) {
        if (region == null) return null;

        RectF bounds = region.getBounds();
        if (bounds == null) bounds = new RectF (0, 0, 100, 100);

        CropRect rect = new CropRect (
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.bottom
        );

        return addCropArea (region, rect, RegionOperation.UNION, ColorAssigner.next (mc.colors));
    }

    // Core method — creates a CropArea for any region type.
    // Wires callbacks and adds the view to MultiCrop.
    CropArea addCropArea (
        CropRegion      region,
        CropRect        initialRect,
        RegionOperation operation,
        int             color
    ) {
        mc.regions.add    (region);
        mc.operations.add (operation);
        mc.colors.add     (color);

        CropArea cropArea = new CropArea (mc.getContext(), region, initialRect, color, mc.zoomPan);

        // Rebuild tint + redraw when a CropArea moves or resizes.
        cropArea.setChangeCallback (area -> callback.onAreaChanged());

        // Keep parallel lists in sync when Android re-orders views.
        cropArea.setBringToFrontCallback (area -> MultiCropLists.bringToFront (mc, area));

        mc.cropAreas.add (cropArea);

        mc.addView (cropArea, new FrameLayout.LayoutParams (
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        callback.onAreaChanged();
        return cropArea;
    }



    // =========================================================
    // PACKAGE-PRIVATE METHODS — REMOVE / CLEAR
    // =========================================================

    // Removes one crop area and its parallel list entries.
    void removeCropArea (CropArea cropArea) {
        int index = mc.cropAreas.indexOf (cropArea);
        if (index < 0) return;

        mc.cropAreas.remove  (index);
        mc.regions.remove    (index);
        mc.operations.remove (index);
        mc.colors.remove     (index);

        mc.removeView (cropArea);
        callback.onAreaChanged();
    }

    // Removes all crop areas.
    void clearCropAreas() {
        for (CropArea ca : mc.cropAreas) mc.removeView (ca);

        mc.cropAreas.clear();
        mc.regions.clear();
        mc.operations.clear();
        mc.colors.clear();

        callback.onAreaChanged();
    }

    // Returns an unmodifiable view of the crop area list.
    List <CropArea> getCropAreas() {
        return Collections.unmodifiableList (mc.cropAreas);
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    // Guards against adding crops before an image is set.
    private void checkImageSet() {
        if (mc.getSourceBitmap() == null) {
            throw new IllegalStateException ("Cannot add crop areas before setting an image.");
        }
    }



}
