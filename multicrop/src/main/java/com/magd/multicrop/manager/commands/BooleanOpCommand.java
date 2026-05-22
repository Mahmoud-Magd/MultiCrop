package com.magd.multicrop.manager.commands;



import android.graphics.Path;
import android.graphics.RectF;

import com.magd.multicrop.enums.RegionOperation;
import com.magd.multicrop.models.CropRect;
import com.magd.multicrop.regions.children.PathRegion;
import com.magd.multicrop.views.crop_area.CropArea;

import com.magd.multicrop.manager.CropManager;
import com.magd.multicrop.manager.CropEntry;



// =========================================================
// BooleanOpCommand
// =========================================================

// Performs a boolean path operation between two CropEntries.

// =========================================================
// SUPPORTED OPERATIONS:

    // UNION:      Result = A ∪ B
    // DIFFERENCE: Result = A − B  (order matters)
    // INTERSECT:  Result = A ∩ B
    // XOR:        Result = (A ∪ B) − (A ∩ B)

// =========================================================
// RESULT:

    // A new PathRegion is created from the resulting Path.
    // Both A and B are removed from MultiCrop and the list.
    // The result is added in their place.

    // If the operation produces no area (e.g. INTERSECT with
    // non-overlapping regions), both A and B are removed
    // and nothing is added.

// =========================================================
// UNDO:

    // The result is removed.
    // A and B are re-inserted at their original list positions.

// =========================================================

public class BooleanOpCommand implements CropCommand {



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropManager     manager;
    private final CropEntry       entryA;     // Base region (first selected).
    private final CropEntry       entryB;     // Applied region (second selected).
    private final RegionOperation operation;

    // Snapshotted at construction — before any list mutation.
    private final int indexA;
    private final int indexB;

    // Created by execute(), removed by undo().
    private CropEntry resultEntry;



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public BooleanOpCommand (
        CropManager     manager,
        CropEntry       entryA,
        CropEntry       entryB,
        RegionOperation operation
    ) {
        this.manager   = manager;
        this.entryA    = entryA;
        this.entryB    = entryB;
        this.operation = operation;

        // Snapshot indices NOW before any list mutation.
        this.indexA = manager.entries.indexOf (entryA);
        this.indexB = manager.entries.indexOf (entryB);
    }



    // =========================================================
    // CROP COMMAND IMPLEMENTATIONS
    // =========================================================

    @Override
    public void execute() {
        Path pathA = entryA.getRegion().buildPath();
        Path pathB = entryB.getRegion().buildPath();

        // Apply boolean operation.
        Path result = new Path (pathA);
        result.op (pathB, toPathOp (operation));

        // Compute resulting bounds.
        RectF bounds = new RectF();
        result.computeBounds (bounds, true);

        // Remove both source entries.
        removeSources();

        // Empty result — operation produced no area.
        if (bounds.isEmpty()) {
            manager.notifyListChanged();
            return;
        }

        // Create result region.
        PathRegion resultRegion = new PathRegion (result, bounds);
        CropRect   resultRect   = new CropRect (bounds.left, bounds.top, bounds.right, bounds.bottom);

        CropArea resultArea = manager.addCropArea (
            resultRegion,
            resultRect,
            RegionOperation.UNION,
            entryA.getColor()    // Result inherits A's color.
        );

        resultEntry = new CropEntry (resultArea, buildLabel());
        manager.addEntry (resultEntry);
        manager.notifyListChanged();
    }

    @Override
    public void undo() {
        // Remove the result.
        if (resultEntry != null) {
            manager.removeEntry (resultEntry);
            manager.removeCropArea (resultEntry.getCropArea());
            resultEntry = null;
        }

        // Re-insert A and B at their original positions.
        manager.insertEntry (Math.min (indexA, manager.entries.size()), entryA);
        manager.addCropArea (
            entryA.getRegion(),
            entryA.getCropArea().getCropRect(),
            RegionOperation.UNION,
            entryA.getColor()
        );

        manager.insertEntry (Math.min (indexB, manager.entries.size()), entryB);
        manager.addCropArea (
            entryB.getRegion(),
            entryB.getCropArea().getCropRect(),
            RegionOperation.UNION,
            entryB.getColor()
        );

        manager.notifyListChanged();
    }

    @Override
    public String getDescription() {
        return buildLabel();
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    private void removeSources() {
        manager.removeEntry (entryA);
        manager.removeCropArea (entryA.getCropArea());
        manager.removeEntry (entryB);
        manager.removeCropArea (entryB.getCropArea());
    }

    private String buildLabel() {
        return entryA.getLabel() + " " + opSymbol() + " " + entryB.getLabel();
    }

    private String opSymbol() {
        switch (operation) {
            case UNION:      return "∪";
            case DIFFERENCE: return "−";
            case INTERSECT:  return "∩";
            case XOR:        return "⊕";
            default:         return "op";
        }
    }

    private static Path.Op toPathOp (RegionOperation op) {
        switch (op) {
            case UNION:      return Path.Op.UNION;
            case DIFFERENCE: return Path.Op.DIFFERENCE;
            case INTERSECT:  return Path.Op.INTERSECT;
            case XOR:        return Path.Op.XOR;
            default:         return Path.Op.UNION;
        }
    }



}
