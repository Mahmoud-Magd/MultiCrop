package com.magd.multicrop.manager.commands;



// =========================================================
// CropCommand
// =========================================================

// Interface for all undoable / redoable crop operations.

// =========================================================
// COMMAND PATTERN:

// Every user operation is wrapped in a CropCommand:
    // execute() — applies the operation
    // undo()    — reverses the operation

// The CropHistoryManager holds a stack of executed commands.
// Undo pops from the executed stack and calls undo().
// Redo re-applies from the undone stack and calls execute().

// =========================================================
// CURRENT COMMANDS:

    // PinCommand      — pin / unpin a list of entries
    // DeleteCommand   — delete a list of entries
    // VisibilityCommand — show / hide a list of entries
    // RotateCommand   — rotate a list of entries
    // MergeCommand    — merge a list of entries into one

// =========================================================
// ADDING NEW COMMANDS:

// Implement this interface.
// Add a getDescription() for undo UI labels.
// Register via CropHistoryManager.execute(command).

// =========================================================

public interface CropCommand {



    // Applies the operation.
    // Called by CropHistoryManager.execute().
    void execute();

    // Reverses the operation exactly.
    // Called by CropHistoryManager.undo().
    void undo();

    // Returns a short human-readable description.
    // Used for undo/redo button labels.
    // Example: "Delete 3 crops", "Pin 1 crop", "Rotate 2 crops"
    String getDescription();



}
