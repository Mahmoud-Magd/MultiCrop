package com.magd.multicrop.manager.commands;



import android.graphics.RectF;

import com.magd.multicrop.enums.RegionOperation;
import com.magd.multicrop.manager.CropEntry;
import com.magd.multicrop.manager.CropManager;
import com.magd.multicrop.models.CropRect;
import com.magd.multicrop.regions.CropRegion;
import com.magd.multicrop.views.crop_area.CropArea;



// =========================================================
// ReshapeCommand
// =========================================================

// Replaces the region type of an existing CropEntry
// with any new CropRegion, preserving all metadata.

// =========================================================
// WHAT IS PRESERVED:

    // ✅ Label
    // ✅ Color
    // ✅ Rotation angle
    // ✅ Visibility
    // ✅ Pinned state
    // ✅ CropArea hidden state
    // ✅ List position (z-order in the adapter)
    // ✅ Bounding box (new region is fitted to old bounds)

// =========================================================
// WHAT CHANGES:

    // The CropRegion subclass — e.g. RectangleRegion → FreePathRegion.
    // The CropArea child view is rebuilt with the new region.

// =========================================================
// ACCEPTED REGION TYPES:

    // Any CropRegion subclass is valid:
        // OvalRegion      -> oval / ellipse / circle
        // PolygonRegion   -> triangle, square, hexagon, etc.
        // FreePathRegion  -> freehand drawn stroke (from FreeDrawSession)
        // PathRegion      -> computed boolean result
        // Any future subclass

// =========================================================
// CALLER CONTRACT:

    // The caller constructs whichever region they want.
    // This command only does the swap.
    // It does NOT decide what the new shape is.
    // That decision belongs to the UI / session layer.

    // Example — caller side:
        //
        //   // User drew a free path. Session committed. Caller calls:
        //   cropManager.reshape (entry, session.getRegion());
        //
        //   // Or caller wants an oval with the same bounds:
        //   OvalRegion oval = new OvalRegion (entry.getRegion().getBounds());
        //   cropManager.reshape (entry, oval);
        //

// =========================================================
// BOUNDING BOX CONTRACT:

    // ReshapeCommand calls newRegion.setEdges(oldBounds) as a
    // final safety step to ensure the new region starts
    // inside the same bounding box as the old one.

    // For FreePathRegion: setEdges() scales the path to fit.
    // For OvalRegion: setEdges() updates the bounding rect.
    // For PolygonRegion: setEdges() scales all points proportionally.

// =========================================================
// UNDO:

    // The replacement entry + CropArea are removed.
    // The original entry + original CropArea are re-added
    // at the exact original list position.

// =========================================================

public class ReshapeCommand implements CropCommand {



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropManager manager;
    private final CropEntry   target;
    private final CropRegion  newRegion;

    // Snapshotted at construction for undo — BEFORE any mutation.
    private final int        originalIndex;
    private final CropRegion originalRegion;
    private final CropArea   originalCropArea;
    private final RectF      originalBounds;

    // Created during execute(), removed by undo().
    private CropEntry resultEntry;



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public ReshapeCommand (
        CropManager manager,
        CropEntry   target,
        CropRegion  newRegion
    ) {
        this.manager   = manager;
        this.target    = target;
        this.newRegion = newRegion;

        // Snapshot everything needed for undo BEFORE any mutation.
        this.originalIndex    = manager.entries.indexOf (target);
        this.originalRegion   = target.getRegion();
        this.originalCropArea = target.getCropArea();

        RectF b = target.getRegion().getBounds();
        this.originalBounds = (b != null) ? new RectF (b) : new RectF (0, 0, 100, 100);
    }



    // =========================================================
    // CROP COMMAND IMPLEMENTATIONS
    // =========================================================

    @Override
    public void execute() {
        // Fit new region into the old bounding box.
        // This ensures the new shape starts at the same position
        // and size as the old one — no jarring jumps.
        newRegion.setEdges (
            originalBounds.left,
            originalBounds.top,
            originalBounds.right,
            originalBounds.bottom
        );

        // Carry over visibility from old region.
        newRegion.setVisible (originalRegion.isVisible());

        CropRect newRect = new CropRect (
            originalBounds.left,
            originalBounds.top,
            originalBounds.right,
            originalBounds.bottom
        );

        // Remove old entry + CropArea.
        manager.removeEntry    (target);
        manager.removeCropArea (originalCropArea);

        // Add new CropArea with the new region.
        // Same color and RegionOperation as the original.
        CropArea newCropArea = manager.addCropArea (
            newRegion,
            newRect,
            RegionOperation.UNION,
            target.getColor()
        );

        // Build replacement entry — carry over ALL metadata.
        resultEntry = new CropEntry (newCropArea, target.getLabel());
        resultEntry.setRotation       (target.getRotation());
        resultEntry.setPinned         (target.isPinned());
        resultEntry.setVisible        (target.isVisible());
        resultEntry.setCropAreaHidden (target.isCropAreaHidden());

        // Re-insert at exact original list position.
        manager.insertEntry (Math.min (originalIndex, manager.entries.size()), resultEntry);
        manager.notifyListChanged();
    }

    @Override
    public void undo() {
        // Remove the replacement.
        if (resultEntry != null) {
            manager.removeEntry    (resultEntry);
            manager.removeCropArea (resultEntry.getCropArea());
            resultEntry = null;
        }

        // Re-add the original CropArea.
        manager.addCropArea (
            originalRegion,
            new CropRect (
                originalBounds.left,
                originalBounds.top,
                originalBounds.right,
                originalBounds.bottom
            ),
            RegionOperation.UNION,
            target.getColor()
        );

        // Restore original entry at original position.
        manager.insertEntry (Math.min (originalIndex, manager.entries.size()), target);
        manager.notifyListChanged();
    }

    @Override
    public String getDescription() {
        return "Reshape \"" + target.getLabel() + "\"";
    }



}
