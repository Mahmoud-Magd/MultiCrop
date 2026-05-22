package com.magd.multicrop.manager.commands;



import java.util.ArrayList;
import java.util.List;



import com.magd.multicrop.manager.CropManager;
import com.magd.multicrop.manager.CropEntry;



// =========================================================
// VisibilityCommand
// =========================================================

// Shows or hides a list of CropEntries.

// =========================================================
// WHAT VISIBILITY AFFECTS:

    // CropEntry.visible       — the data model state.
    // CropRegion.visible      — affects tint hole punching + export masking.
    // CropArea.setVisibility  — affects whether handles and border draw.

// All three are synced together on every execute() and undo().

// =========================================================
// PREVIOUS STATES:

    // Each entry's previous visibility is snapshotted at construction.
    // undo() restores each entry to its individual previous state,
    // not just the inverse of newVisible.

    // Why?
        // If entry A was already hidden, and the user runs "hide all",
        // undo() should restore A to hidden — not flip it to visible.

// =========================================================

public class VisibilityCommand implements CropCommand {



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropManager      manager;
    private final List <CropEntry> targets;
    private final boolean          newVisible;
    private final List <Boolean>   previousStates = new ArrayList<>();



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public VisibilityCommand (CropManager manager, List <CropEntry> targets, boolean newVisible) {
        this.manager    = manager;
        this.targets    = new ArrayList<> (targets);
        this.newVisible = newVisible;

        // Snapshot each entry's current visibility before changing anything.
        for (CropEntry e : targets) previousStates.add (e.isVisible());
    }



    // =========================================================
    // CROP COMMAND IMPLEMENTATIONS
    // =========================================================

    @Override
    public void execute() {
        for (CropEntry e : targets) apply (e, newVisible);
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
        return (newVisible ? "Show " : "Hide ") + targets.size() + CropManager.plural (targets.size());
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    private void apply (CropEntry entry, boolean visible) {
        entry.setVisible (visible);                          // Data model.
        entry.getRegion().setVisible (visible);              // Tint + export.
        entry.getCropArea().setVisibility (                  // Handles + border.
            visible
            ? android.view.View.VISIBLE
            : android.view.View.INVISIBLE
        );
        manager.getMultiCrop().notifyRegionChanged();        // Rebuild tint mask.
    }



}
