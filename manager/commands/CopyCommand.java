package com.magd.multicrop.manager.commands;



import com.magd.multicrop.enums.RegionOperation;
import com.magd.multicrop.models.CropRect;
import com.magd.multicrop.regions.CropRegion;
import com.magd.multicrop.utils.ColorAssigner;

import com.magd.multicrop.views.crop_area.CropArea;

import com.magd.multicrop.manager.CropManager;
import com.magd.multicrop.manager.CropEntry;

import java.util.ArrayList;
import java.util.List;



// =========================================================
// CopyCommand
// =========================================================

// Duplicates a list of CropEntries.

// =========================================================
// EACH COPY:

    // Deep-clones the original region geometry via region.copy().
    // Is offset by COPY_OFFSET_PX in image space so it doesn't
    // overlap exactly with the original.
    // Gets a new label: original label + " (copy)".
    // Gets a new unique color via ColorAssigner.
    // Carries over the original's rotation angle.

// =========================================================
// UNDO:

    // All created copies are removed.
    // The original entries are NEVER touched by this command.

// =========================================================
// OFFSET WRAPPING:

    // If the offset would push the copy out of image bounds,
    // it wraps back to COPY_OFFSET_PX from the top-left corner,
    // with the same dimensions.

// =========================================================

public class CopyCommand implements CropCommand {



    // =========================================================
    // CONSTANTS
    // =========================================================

    // Offset in image-space pixels applied to each copy.
    private static final float COPY_OFFSET_PX = 30f;



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropManager      manager;
    private final List <CropEntry> sources;
    private final float            imageWidth;
    private final float            imageHeight;

    // Created by execute(), removed by undo().
    private final List <CropEntry> copies = new ArrayList<>();



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public CopyCommand (
        CropManager      manager,
        List <CropEntry> sources,
        float            imageWidth,
        float            imageHeight
    ) {
        this.manager     = manager;
        this.sources     = new ArrayList<> (sources);
        this.imageWidth  = imageWidth;
        this.imageHeight = imageHeight;
    }



    // =========================================================
    // CROP COMMAND IMPLEMENTATIONS
    // =========================================================

    @Override
    public void execute() {
        copies.clear();

        for (CropEntry source : sources) {
            CropEntry copy = buildCopy (source);
            if (copy != null) {
                copies.add (copy);
                manager.addEntry (copy);
            }
        }

        manager.notifyListChanged();
    }

    @Override
    public void undo() {
        for (CropEntry copy : copies) {
            manager.removeEntry (copy);
            manager.removeCropArea (copy.getCropArea());
        }
        copies.clear();
        manager.notifyListChanged();
    }

    @Override
    public String getDescription() {
        return "Copy " + sources.size() + CropManager.plural (sources.size());
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    private CropEntry buildCopy (CropEntry source) {
        CropRegion originalRegion = source.getRegion();
        CropRegion copiedRegion   = originalRegion.copy();

        CropRect originalRect = source.getCropArea().getCropRect();

        float w    = originalRect.getRight()  - originalRect.getLeft();
        float h    = originalRect.getBottom() - originalRect.getTop();

        float newLeft   = originalRect.getLeft()   + COPY_OFFSET_PX;
        float newTop    = originalRect.getTop()    + COPY_OFFSET_PX;
        float newRight  = newLeft + w;
        float newBottom = newTop  + h;

        // Wrap back to top-left if copy goes out of image bounds.
        if (newRight > imageWidth || newBottom > imageHeight) {
            newLeft   = COPY_OFFSET_PX;
            newTop    = COPY_OFFSET_PX;
            newRight  = newLeft + w;
            newBottom = newTop  + h;
        }

        copiedRegion.setEdges (newLeft, newTop, newRight, newBottom);

        CropRect copiedRect = new CropRect (newLeft, newTop, newRight, newBottom);

        int color = ColorAssigner.next (manager.getAllColors());

        CropArea copiedArea = manager.addCropArea (
            copiedRegion,
            copiedRect,
            RegionOperation.UNION,
            color
        );

        CropEntry copy = new CropEntry (copiedArea, source.getLabel() + " (copy)");
        copy.setRotation (source.getRotation());

        return copy;
    }



}
