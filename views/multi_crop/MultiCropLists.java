package com.magd.multicrop.views.multi_crop;



import com.magd.multicrop.enums.RegionOperation;
import com.magd.multicrop.views.crop_area.CropArea;



// =========================================================
// MultiCropLists
// =========================================================

// Handles parallel-list Z-order synchronisation for MultiCrop.

// =========================================================
// WHY THIS EXISTS:

// MultiCrop maintains four parallel lists:
    // cropAreas, regions, operations, colors.

// When a CropArea calls bringToFront() (user touches it),
// the View Z-order changes but the parallel lists do not.

// This class re-orders the lists to stay in sync with
// the new View Z-order.

// Extracted from MultiCrop to keep that class under 300 lines.

// =========================================================

final class MultiCropLists {



    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private MultiCropLists() {}



    // =========================================================
    // PUBLIC METHODS
    // =========================================================

    // Moves the given CropArea (and its parallel entries) to the
    // end of all lists, matching its new position in the View Z-order.
    static void bringToFront (MultiCrop mc, CropArea area) {
        int idx = mc.cropAreas.indexOf (area);

        // Already at the top, or not found — nothing to do.
        if (idx < 0 || idx == mc.cropAreas.size() - 1) return;

        // Capture the operation before removing.
        RegionOperation op = mc.operations.get (idx);

        mc.cropAreas.remove  (idx);
        mc.regions.remove    (idx);
        mc.operations.remove (idx);
        mc.colors.remove     (idx);

        mc.cropAreas.add  (area);
        mc.regions.add    ( area.getRegion() );
        mc.operations.add (op);
        mc.colors.add     ( area.getColor() );
    }



}
