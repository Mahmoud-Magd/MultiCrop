package com.magd.multicrop.manager.commands;



import java.util.ArrayList;
import java.util.List;

import com.magd.multicrop.manager.CropEntry;
import com.magd.multicrop.manager.CropManager;



// =========================================================
// HideCropAreaCommand
// =========================================================

// Hides or shows the CropArea view (border + handles)
// WITHOUT affecting region visibility in tint or export.

// =========================================================
// DIFFERENCE FROM VisibilityCommand:

    // VisibilityCommand:
        // Hides the CropArea view.
        // AND hides the region from tint + export.
        // Region is excluded from the mask entirely.

    // HideCropAreaCommand:
        // Hides the CropArea view (border + handles invisible).
        // Region STAYS visible in tint + export.
        // Touch interaction still works (move + resize).
        // If pinned: touch is ignored by CropArea anyway.

// =========================================================
// USE CASES:

    // User wants a clean view of the image without
    // handle clutter, but the region is still active.

    // User pins a region and hides its handles
    // for a locked, clean crop.

// =========================================================
// UNDO:

    // Each entry's previous cropAreaHidden state is
    // snapshotted at construction.
    // undo() restores each entry to its individual prior state.

// =========================================================

public class HideCropAreaCommand implements CropCommand {



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropManager      manager;
    private final List <CropEntry> targets;
    private final boolean          hide;           // true = hide, false = show.
    private final List <Boolean>   previousStates; // Per-entry snapshot for undo.



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public HideCropAreaCommand (
        CropManager      manager,
        List <CropEntry> targets,
        boolean          hide
    ) {
        this.manager        = manager;
        this.targets        = new ArrayList<> (targets);
        this.hide           = hide;
        this.previousStates = new ArrayList<>();

        // Snapshot BEFORE any mutation.
        for (CropEntry e : targets) {
            previousStates.add (e.isCropAreaHidden());
        }
    }



    // =========================================================
    // CROP COMMAND IMPLEMENTATIONS
    // =========================================================

    @Override
    public void execute() {
        for (CropEntry e : targets) apply (e, hide);
        manager.notifyEntriesChanged (targets);
    }

    @Override
    public void undo() {
        for (int i = 0; i < targets.size(); i++) {
            apply (targets.get (i), previousStates.get (i));
        }
        manager.notifyEntriesChanged (targets);
    }

    @Override
    public String getDescription() {
        return (hide ? "Hide handles " : "Show handles ") + targets.size() + CropManager.plural (targets.size());
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    private void apply (CropEntry entry, boolean shouldHide) {
        entry.setCropAreaHidden (shouldHide);

        // INVISIBLE hides border + handles but the view still receives touch.
        // We use alpha = 0 instead of INVISIBLE so touch still routes correctly.
        entry.getCropArea().setCropAreaVisible (!shouldHide);
    }



}
