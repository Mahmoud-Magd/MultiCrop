package com.magd.multicrop.manager;



import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



import com.magd.multicrop.enums.RegionOperation;
import com.magd.multicrop.views.multi_crop.MultiCrop;
import com.magd.multicrop.views.crop_area.CropArea;

import com.magd.multicrop.regions.CropRegion;
import com.magd.multicrop.models.CropRect;

import com.magd.multicrop.manager.commands.PinCommand;
import com.magd.multicrop.manager.commands.DeleteCommand;
import com.magd.multicrop.manager.commands.VisibilityCommand;
import com.magd.multicrop.manager.commands.RotateCommand;
import com.magd.multicrop.manager.commands.RenameCommand;
import com.magd.multicrop.manager.commands.CopyCommand;
import com.magd.multicrop.manager.commands.BooleanOpCommand;
import com.magd.multicrop.manager.commands.HideCropAreaCommand;
import com.magd.multicrop.manager.commands.ResizeCommand;
import com.magd.multicrop.manager.commands.ReshapeCommand;



// =========================================================
// CropManager
// =========================================================

// Central coordinator between MultiCrop (view layer)
// and the crop list UI (RecyclerView adapter).

// =========================================================
// RESPONSIBILITY:

    // Maintains the ordered list of CropEntry objects.
    // Provides all multi-crop operations.
    // Routes every operation through CropHistoryManager.
    // Notifies the adapter when the list changes.

// =========================================================
// OPERATIONS:

    // Batch (act on a list):
        // pin / unpin / delete / show / hide / rotate / copy / rename
        // hideCropArea / showCropArea

    // Single-entry:
        // resize   — set exact width/height in dp (ResizeCommand)
        // reshape  — replace region type, preserve all metadata (ReshapeCommand)

    // Two-entry boolean (A op B → result):
        // booleanOp() — UNION | DIFFERENCE | INTERSECT | XOR

// =========================================================
// PIN PROTECTION:

    // Pinned entries are protected from:
        // delete(), rotate(), booleanOp()

    // Pinned entries ARE allowed in:
        // copy(), show(), hide(), hideCropArea(), showCropArea()
        // rename(), resize(), reshape()

// =========================================================
// COMMAND CLASSES (each in its own file):

    // PinCommand.java
    // DeleteCommand.java
    // VisibilityCommand.java
    // HideCropAreaCommand.java
    // RotateCommand.java
    // RenameCommand.java
    // CopyCommand.java
    // BooleanOpCommand.java
    // ResizeCommand.java
    // ReshapeCommand.java

// =========================================================

public class CropManager {



    // =========================================================
    // LISTENER
    // =========================================================

    // Implemented by the RecyclerView adapter.
    public interface ChangeListener {
        void onListChanged();            // Full refresh (add / remove / reorder).
        void onEntryChanged (int index); // Single item changed (pin, visible, rotate, rename).
    }



    // =========================================================
    // VARIABLES
    // =========================================================

    private final MultiCrop          multiCrop; // Root view — add / remove crops here.
    private final CropHistoryManager history;   // Undo / redo engine.

    // Package-private — command classes access directly.
    // In Dart: use @internal or part/part-of.
    public final List <CropEntry> entries;

    private ChangeListener listener;            // Adapter notification callback.

    private int labelCounter = 1;               // "Crop 1", "Crop 2", etc.



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public CropManager (MultiCrop multiCrop) {
        this.multiCrop = multiCrop;
        this.history   = new CropHistoryManager();
        this.entries   = new ArrayList<>();
    }



    // =========================================================
    // PUBLIC METHODS — SETUP
    // =========================================================

    public void setChangeListener (ChangeListener listener) { this.listener = listener; }

    public CropHistoryManager getHistory()         { return history;                                }
    public List <CropEntry>   getEntries()         { return Collections.unmodifiableList (entries); }
    public CropEntry          getEntry (int index) { return entries.get (index);                    }
    public int                getCount()           { return entries.size();                         }
    public MultiCrop          getMultiCrop()       { return multiCrop;                              }



    // =========================================================
    // PUBLIC METHODS — ADD CROPS
    // =========================================================

    // Always add crops through CropManager, not directly through MultiCrop.
    // This ensures entries are tracked and labeled consistently.

    public CropEntry addRectCrop() {
        return createEntry (multiCrop.addRectCrop());
    }

    public CropEntry addOvalCrop() {
        return createEntry (multiCrop.addOvalCrop());
    }

    public CropEntry addPolygonCrop (int numOfPoints) {
        return createEntry (multiCrop.addPolygonCrop (numOfPoints));
    }

    public CropEntry addFreeDrawingCrop (com.magd.multicrop.regions.children.FreePathRegion region) {
        return createEntry (multiCrop.addFreeDrawingCrop (region));
    }

    private CropEntry createEntry (CropArea area) {
        if (area == null) return null;
        CropEntry entry = new CropEntry (area, "Crop " + labelCounter++);
        entries.add (entry);
        notifyListChanged();
        return entry;
    }



    // =========================================================
    // PUBLIC METHODS — BATCH OPERATIONS
    // =========================================================

    public void pin (List <CropEntry> targets) {
        List <CropEntry> eligible = filterUnpinned (targets);
        if (!eligible.isEmpty()) history.execute (new PinCommand (this, eligible, true));
    }

    public void unpin (List <CropEntry> targets) {
        List <CropEntry> eligible = filterPinned (targets);
        if (!eligible.isEmpty()) history.execute (new PinCommand (this, eligible, false));
    }

    public void delete (List <CropEntry> targets) {
        List <CropEntry> eligible = filterUnpinned (targets);
        if (!eligible.isEmpty()) history.execute (new DeleteCommand (this, eligible));
    }

    public void show (List <CropEntry> targets) {
        if (targets != null && !targets.isEmpty())
            history.execute (new VisibilityCommand (this, targets, true));
    }

    public void hide (List <CropEntry> targets) {
        if (targets != null && !targets.isEmpty())
            history.execute (new VisibilityCommand (this, targets, false));
    }

    // Hides border + handles. Region stays active in tint + export.
    public void hideCropArea (List <CropEntry> targets) {
        if (targets != null && !targets.isEmpty())
            history.execute (new HideCropAreaCommand (this, targets, true));
    }

    // Shows border + handles.
    public void showCropArea (List <CropEntry> targets) {
        if (targets != null && !targets.isEmpty())
            history.execute (new HideCropAreaCommand (this, targets, false));
    }

    public void rotate (List <CropEntry> targets, float angleDegrees) {
        List <CropEntry> eligible = filterUnpinned (targets);
        if (!eligible.isEmpty()) history.execute (new RotateCommand (this, eligible, angleDegrees));
    }

    public void rename (CropEntry target, String newLabel) {
        if (target != null && newLabel != null)
            history.execute (new RenameCommand (this, target, newLabel));
    }

    public void copy (List <CropEntry> targets) {
        if (targets == null || targets.isEmpty()) return;
        float imgW = multiCrop.getSourceBitmap() != null ? multiCrop.getSourceBitmap().getWidth()  : 1000f;
        float imgH = multiCrop.getSourceBitmap() != null ? multiCrop.getSourceBitmap().getHeight() : 1000f;
        history.execute (new CopyCommand (this, targets, imgW, imgH));
    }



    // =========================================================
    // PUBLIC METHODS — BOOLEAN OPERATIONS
    // =========================================================

    // Performs A op B and replaces both with the result.
    // Order matters for DIFFERENCE: booleanOp(A, B, DIFFERENCE) = A minus B.
    // Pinned entries are rejected.
    public void booleanOp (CropEntry entryA, CropEntry entryB, RegionOperation operation) {
        if (entryA == null || entryB == null)       return;
        if (entryA == entryB)                        return;
        if (entryA.isPinned() || entryB.isPinned()) return;
        history.execute (new BooleanOpCommand (this, entryA, entryB, operation));
    }



    // =========================================================
    // PUBLIC METHODS — RESIZE
    // =========================================================

    // Sets the exact width and/or height of a CropEntry in dp.
    // Applied around the existing center — crop stays anchored in place.
    // Pass ≤ 0 for either dimension to leave it unchanged.
    // Result is clamped to image bounds and never below MIN_WIDTH/MIN_HEIGHT.
    // Fully undoable via CropHistoryManager.
    //
    // Example:
    //   cropManager.resize (entry, 200f, -1f); // set width only
    //   cropManager.resize (entry, 150f, 100f); // set both
    public void resize (CropEntry target, float newWidthDp, float newHeightDp) {
        if (target == null) return;
        history.execute (new ResizeCommand (this, target, newWidthDp, newHeightDp));
    }



    // =========================================================
    // PUBLIC METHODS — RESHAPE
    // =========================================================

    // Replaces the region of an existing CropEntry with a new region,
    // preserving all metadata: label, color, rotation, visibility,
    // pinned state, cropAreaHidden state, and list position.
    // Fully undoable via CropHistoryManager.
    //
    // Pinned entries ARE allowed — shape changes are non-destructive.
    //
    // Example:
    //   cropManager.reshape (entry, new OvalRegion (entry.getRegion().getBounds()));
    //   cropManager.reshape (entry, session.getRegion()); // from FreeDrawSession
    public void reshape (CropEntry target, CropRegion newRegion) {
        if (target == null || newRegion == null) return;
        history.execute (new ReshapeCommand (this, target, newRegion));
    }



    // =========================================================
    // PACKAGE-PRIVATE METHODS — FOR COMMAND CLASSES ONLY
    // Not part of the public API.
    // =========================================================

    // Adds entry to the end of the list without going through history.
    public void addEntry (CropEntry entry) {
        entries.add (entry);
    }

    // Inserts entry at a specific index without going through history.
    public void insertEntry (int index, CropEntry entry) {
        entries.add (Math.min (index, entries.size()), entry);
    }

    // Removes entry without going through history.
    public void removeEntry (CropEntry entry) {
        entries.remove (entry);
    }

    // Triggers full list refresh on the adapter.
    public void notifyListChanged() {
        if (listener != null) listener.onListChanged();
    }

    // Triggers single-item refresh on the adapter.
    public void notifyEntryChanged (CropEntry entry) {
        int index = entries.indexOf (entry);
        if (index >= 0 && listener != null) listener.onEntryChanged (index);
    }

    // Triggers refresh for multiple entries.
    public void notifyEntriesChanged (List <CropEntry> list) {
        for (CropEntry e : list) notifyEntryChanged (e);
    }

    // Returns all currently assigned colors.
    // Used by CopyCommand to assign unique colors to copies.
    public List <Integer> getAllColors() {
        List <Integer> colors = new ArrayList<>();
        for (CropEntry e : entries) colors.add (e.getColor());
        return colors;
    }

    // Exposes multiCrop.removeCropArea() to command classes.
    public void removeCropArea (CropArea area) {
        multiCrop.removeCropArea (area);
    }

    // Exposes multiCrop.addCropArea() to command classes.
    public CropArea addCropArea (
        CropRegion      region,
        CropRect        rect,
        RegionOperation operation,
        int             color
    ) {
        return multiCrop.addCropArea (region, rect, operation, color);
    }



    // =========================================================
    // PUBLIC STATIC HELPERS
    // =========================================================

    // Returns " crop" or " crops".
    public static String plural (int count) {
        return count == 1 ? " crop" : " crops";
    }



    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    private List <CropEntry> filterUnpinned (List <CropEntry> list) {
        List <CropEntry> result = new ArrayList<>();
        if (list != null) for (CropEntry e : list) if (!e.isPinned()) result.add (e);
        return result;
    }

    private List <CropEntry> filterPinned (List <CropEntry> list) {
        List <CropEntry> result = new ArrayList<>();
        if (list != null) for (CropEntry e : list) if (e.isPinned()) result.add (e);
        return result;
    }



}
