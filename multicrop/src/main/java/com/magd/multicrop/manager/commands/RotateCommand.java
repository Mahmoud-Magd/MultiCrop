package com.magd.multicrop.manager.commands;



import android.graphics.Matrix;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;



import com.magd.multicrop.manager.CropManager;
import com.magd.multicrop.manager.CropEntry;



// =========================================================
// RotateCommand
// =========================================================

// Rotates a list of CropEntries by a given angle delta.

// =========================================================
// HOW ROTATION WORKS:

    // Rotation is stored as a cumulative angle in CropEntry.rotation.
    // Each execute() adds angleDelta to the current angle.
    // Each undo() subtracts angleDelta from the current angle.

    // The region's bounding rect is rotated around its own center
    // using a Matrix. This keeps the region anchored to the image.

// =========================================================
// BOUNDING RECT ROTATION:

    // Only the 4 corners of the bounding rect are transformed.
    // The new bounding rect is the axis-aligned box that fits
    // around the rotated corners.

    // This means:
        // The region's axis-aligned handle box grows for diagonal angles.
        // This is intentional — handles always stay axis-aligned.

// =========================================================
// PIN RULE:

    // CropManager.rotate() filters out pinned entries before
    // passing targets here.

// =========================================================

public class RotateCommand implements CropCommand {



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropManager      manager;
    private final List <CropEntry> targets;
    private final float            angleDelta; // Degrees. Added on execute, subtracted on undo.



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public RotateCommand (CropManager manager, List <CropEntry> targets, float angleDelta) {
        this.manager    = manager;
        this.targets    = new ArrayList<> (targets);
        this.angleDelta = angleDelta;
    }



    // =========================================================
    // CROP COMMAND IMPLEMENTATIONS
    // =========================================================

    @Override
    public void execute() {
        for (CropEntry e : targets) {
            e.setRotation (e.getRotation() + angleDelta);
            applyDelta (e, angleDelta);
        }
        manager.notifyEntriesChanged (targets);
    }

    @Override
    public void undo() {
        for (CropEntry e : targets) {
            e.setRotation (e.getRotation() - angleDelta);
            applyDelta (e, -angleDelta);
        }
        manager.notifyEntriesChanged (targets);
    }

    @Override
    public String getDescription() {
        return "Rotate " + targets.size() + CropManager.plural (targets.size()) + " " + angleDelta + "°";
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    // Rotates the region's bounding rect by delta degrees around its center.
    private void applyDelta (CropEntry entry, float delta) {
        RectF bounds = entry.getRegion().getBounds();
        if (bounds == null) return;

        float cx = bounds.centerX();
        float cy = bounds.centerY();

        // Build rotation matrix around center.
        Matrix m = new Matrix();
        m.postRotate (delta, cx, cy);

        // Map all 4 corners through the rotation.
        float[] pts = {
            bounds.left,  bounds.top,
            bounds.right, bounds.top,
            bounds.right, bounds.bottom,
            bounds.left,  bounds.bottom
        };
        m.mapPoints (pts);

        // New bounding rect = axis-aligned box around rotated corners.
        float newLeft   = pts[0];
        float newTop    = pts[1];
        float newRight  = pts[0];
        float newBottom = pts[1];
        for (int i = 2; i < pts.length; i += 2) {
            if (pts[i]     < newLeft)   newLeft   = pts[i];
            if (pts[i + 1] < newTop)    newTop    = pts[i + 1];
            if (pts[i]     > newRight)  newRight  = pts[i];
            if (pts[i + 1] > newBottom) newBottom = pts[i + 1];
        }

        entry.getRegion().setEdges (newLeft, newTop, newRight, newBottom);
        entry.getCropArea().invalidate();
        manager.getMultiCrop().notifyRegionChanged();
    }



}
