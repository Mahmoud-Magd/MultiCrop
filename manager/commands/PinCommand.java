package com.magd.multicrop.manager.commands;



import java.util.ArrayList;
import java.util.List;



import com.magd.multicrop.manager.CropManager;
import com.magd.multicrop.manager.CropEntry;



// =========================================================
// PinCommand
// =========================================================

// Pins or unpins a list of CropEntries.

// =========================================================
// BEHAVIOR:

    // execute() — sets all targets to newPinState.
    // undo()    — sets all targets back to !newPinState.

// =========================================================
// PIN EFFECT:

    // Pinned entries are protected from:
        // delete(), rotate(), booleanOp()

    // Pinned entries are still allowed in:
        // copy(), show(), hide(), rename()

// =========================================================

public class PinCommand implements CropCommand {



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropManager      manager;
    private final List <CropEntry> targets;
    private final boolean          newPinState; // true = pin, false = unpin.



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public PinCommand (CropManager manager, List <CropEntry> targets, boolean newPinState) {
        this.manager     = manager;
        this.targets     = new ArrayList<> (targets);
        this.newPinState = newPinState;
    }



    // =========================================================
    // CROP COMMAND IMPLEMENTATIONS
    // =========================================================

    @Override
    public void execute() {
        for (CropEntry e : targets) {
            e.setPinned (newPinState);
            e.getCropArea().setPinned (newPinState); // Sync view layer.
        }
        manager.notifyEntriesChanged (targets);
    }

    @Override
    public void undo() {
        for (CropEntry e : targets) {
            e.setPinned ( ! newPinState );
            e.getCropArea().setPinned ( ! newPinState ); // Sync view layer.
        }
        manager.notifyEntriesChanged (targets);
    }

    @Override
    public String getDescription() {
        return (newPinState ? "Pin " : "Unpin ") + targets.size() + CropManager.plural (targets.size());
    }



}
