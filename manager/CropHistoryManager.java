package com.magd.multicrop.manager;



import java.util.ArrayDeque;
import java.util.Deque;


import com.magd.multicrop.manager.commands.CropCommand;



// =========================================================
// CropHistoryManager
// =========================================================

// Manages the undo / redo history stack.

// =========================================================
// ARCHITECTURE:

// Two stacks:
    // undoStack — commands that have been executed.
    //             Undo pops from here, reverses, pushes to redoStack.

    // redoStack — commands that have been undone.
    //             Redo pops from here, re-executes, pushes to undoStack.

// New execute() clears the redoStack.
    // This is standard behavior:
        // Once you take a new action after undoing,
        // the undone history is gone.
        // Same as Photoshop, Figma, VS Code.

// =========================================================
// HISTORY LIMIT:

// MAX_HISTORY = 50 commands.
// Oldest commands are dropped when limit is exceeded.
// Prevents unbounded memory growth on long sessions.

// =========================================================

public class CropHistoryManager {



    // =========================================================
    // CONSTANTS
    // =========================================================
    private static final int MAX_HISTORY = 50; // Maximum undo steps.



    // =========================================================
    // VARIABLES
    // =========================================================
    private final Deque <CropCommand> undoStack = new ArrayDeque<>(); // Commands ready to undo.
    private final Deque <CropCommand> redoStack = new ArrayDeque<>(); // Commands ready to redo.

    // Optional listener notified after every history change.
    // Use to update undo/redo button enabled state in your UI.
    private HistoryListener listener;



    // =========================================================
    // LISTENER
    // =========================================================

    // Notified whenever history changes.
    public interface HistoryListener {
        void onHistoryChanged (boolean canUndo, boolean canRedo);
    }



    // =========================================================
    // PUBLIC METHODS
    // =========================================================

    // ======= Execute =======

    // Executes a command and records it for undo.
    //
    // Clears the redo stack — new action invalidates undone history.
    public void execute (CropCommand command) {
        command.execute();

        undoStack.push (command);
        redoStack.clear(); // New action invalidates redo history.

        // Enforce history limit.
        while (undoStack.size() > MAX_HISTORY) {
            // Remove oldest entry (from tail of deque).
            ( (ArrayDeque <CropCommand>) undoStack ).removeLast();
        }

        notifyListener();
    }



    // ======= Undo =======

    // Undoes the most recently executed command.
    // Returns the description of what was undone, or null if nothing to undo.
    public String undo() {
        if (undoStack.isEmpty()) return null;

        CropCommand command = undoStack.pop();
        command.undo();
        redoStack.push (command);

        notifyListener();
        return command.getDescription();
    }



    // ======= Redo =======

    // Redoes the most recently undone command.
    // Returns the description of what was redone, or null if nothing to redo.
    public String redo() {
        if (redoStack.isEmpty()) return null;

        CropCommand command = redoStack.pop();
        command.execute();
        undoStack.push (command);

        notifyListener();
        return command.getDescription();
    }



    // ======= State =======
    public boolean canUndo()        { return !undoStack.isEmpty(); } // True if undo is available.
    public boolean canRedo()        { return !redoStack.isEmpty(); } // True if redo is available.
    public int     undoStackSize()  { return  undoStack.size();    } // Number of undoable actions.
    public int     redoStackSize()  { return  redoStack.size();    } // Number of redoable actions.

    // Returns description of the next undo action, or null.
    public String  nextUndoLabel()  { return undoStack.isEmpty() ? null : undoStack.peek().getDescription(); }

    // Returns description of the next redo action, or null.
    public String  nextRedoLabel()  { return redoStack.isEmpty() ? null : redoStack.peek().getDescription(); }



    // ======= Listener =======
    public void setHistoryListener (HistoryListener listener) {
        this.listener = listener;
    }



    // ======= Clear =======

    // Clears all history. Use when image changes.
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        notifyListener();
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    private void notifyListener() {
        if (listener != null) listener.onHistoryChanged (canUndo(), canRedo());
    }



}
