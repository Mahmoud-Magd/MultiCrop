package com.magd.multicrop.manager.commands;



import com.magd.multicrop.enums.RegionOperation;

import com.magd.multicrop.manager.CropManager;
import com.magd.multicrop.manager.CropEntry;

import java.util.ArrayList;
import java.util.List;



// =========================================================
// DeleteCommand
// =========================================================

// Removes a list of CropEntries from MultiCrop and the list.

// =========================================================
// UNDO STRATEGY:

    // Each entry's original index is snapshotted at construction
    // (before any mutation happens).

    // On undo(), entries are re-inserted in ascending index order
    // so earlier insertions don't shift later indices.

// =========================================================
// PIN RULE:

    // CropManager.delete() filters out pinned entries before
    // passing targets here. This class always receives
    // only eligible (unpinned) entries.

// =========================================================

public class DeleteCommand implements CropCommand {



    // =========================================================
    // INNER CLASS
    // =========================================================

    // Stores an entry alongside its original list position.
    private static class DeleteRecord {
        final CropEntry entry;
        final int       originalIndex;

        DeleteRecord (CropEntry entry, int index) {
            this.entry         = entry;
            this.originalIndex = index;
        }
    }



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropManager        manager;
    private final List <DeleteRecord> records = new ArrayList<>();



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public DeleteCommand (CropManager manager, List <CropEntry> targets) {
        this.manager = manager;

        // Snapshot positions NOW, before any list mutation.
        for (CropEntry e : targets) {
            records.add ( new DeleteRecord (e, manager.entries.indexOf (e)) );
        }
    }



    // =========================================================
    // CROP COMMAND IMPLEMENTATIONS
    // =========================================================

    @Override
    public void execute() {
        for (DeleteRecord r : records) {
            manager.removeEntry (r.entry);
            manager.removeCropArea (r.entry.getCropArea());
        }
        manager.notifyListChanged();
    }

    @Override
    public void undo() {
        // Re-insert in ascending index order so indices remain stable.
        records.sort ( (a, b) -> Integer.compare (a.originalIndex, b.originalIndex) );

        for (DeleteRecord r : records) {
            manager.insertEntry (r.originalIndex, r.entry);
            manager.addCropArea (
                r.entry.getRegion(),
                r.entry.getCropArea().getCropRect(),
                RegionOperation.UNION,
                r.entry.getColor()
            );
        }
        manager.notifyListChanged();
    }

    @Override
    public String getDescription() {
        return "Delete " + records.size() + CropManager.plural (records.size());
    }



}
