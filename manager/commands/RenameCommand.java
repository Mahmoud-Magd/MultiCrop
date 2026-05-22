package com.magd.multicrop.manager.commands;


import com.magd.multicrop.manager.CropManager;
import com.magd.multicrop.manager.CropEntry;

// =========================================================
// RenameCommand
// =========================================================

// Renames a single CropEntry's label.

// =========================================================
// UNDO:

    // oldLabel is snapshotted at construction time.
    // undo() restores the label to oldLabel exactly.

// =========================================================

public class RenameCommand implements CropCommand {



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropManager manager;
    private final CropEntry   target;
    private final String      newLabel;
    private final String      oldLabel; // Snapshotted at construction, before any change.



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public RenameCommand (CropManager manager, CropEntry target, String newLabel) {
        this.manager  = manager;
        this.target   = target;
        this.newLabel = newLabel;
        this.oldLabel = target.getLabel(); // Snapshot NOW.
    }



    // =========================================================
    // CROP COMMAND IMPLEMENTATIONS
    // =========================================================

    @Override
    public void execute() {
        target.setLabel (newLabel);
        manager.notifyEntryChanged (target);
    }

    @Override
    public void undo() {
        target.setLabel (oldLabel);
        manager.notifyEntryChanged (target);
    }

    @Override
    public String getDescription() {
        return "Rename to \"" + newLabel + "\"";
    }



}
