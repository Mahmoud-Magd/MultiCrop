/*

// =========================================================
// HOW TO WIRE THE BOOLEAN OP FLOW IN YOUR UI
// =========================================================
//
// The flow is:
//   1. User selects crop A  (first tap in selection mode)
//   2. User picks operation (UNION / DIFFERENCE / INTERSECT / XOR)
//   3. User selects crop B  (second tap)
//   4. Execute the operation
//
// This is a 3-step sequential flow, not a batch operation.
// The adapter stays in selection mode throughout.
// =========================================================

public class BooleanOpFlowExample {

    // In your Activity/Fragment:

    private CropManager  cropManager;
    private CropEntry    pendingA;         // First selected entry.
    private RegionOperation pendingOp;     // Chosen operation.

    // Step 1: user selects a crop and then taps a boolean op button.
    // Example: "DIFFERENCE" button clicked while one crop is selected.
    void onDifferenceButtonClicked () {
        List <CropEntry> selected = adapter.getSelected();
        if (selected.size() != 1) {
            // Show error: "Select exactly one crop first"
            return;
        }
        pendingA  = selected.get (0);
        pendingOp = RegionOperation.DIFFERENCE;

        adapter.clearSelection();
        // Show hint to user: "Now tap the second crop"
        showHint ("Now tap the second crop (B). Result = A − B");
    }

    // Step 2: user taps a second crop in the RecyclerView.
    // Wire this inside CropListAdapter.ActionListener.onCropTapped().
    @Override
    void onCropTapped (CropEntry entry) {
        if (pendingA != null && pendingOp != null) {
            // We have A and op — this tap is B.
            CropEntry entryB = entry;

            if (entryB == pendingA) {
                showHint ("Cannot operate on the same crop. Tap a different one.");
                return;
            }

            // Execute: A op B
            cropManager.booleanOp (pendingA, entryB, pendingOp);

            // Reset pending state.
            pendingA  = null;
            pendingOp = null;
            hideHint();

        } else {
            // Normal tap — activate on canvas.
            entry.getCropArea().setActive (true);
        }
    }

    // For UNION / INTERSECT / XOR — same pattern, different RegionOperation.
    void onUnionButtonClicked()     { startBooleanOp (RegionOperation.UNION);     }
    void onIntersectButtonClicked() { startBooleanOp (RegionOperation.INTERSECT); }
    void onXorButtonClicked()       { startBooleanOp (RegionOperation.XOR);       }

    private void startBooleanOp (RegionOperation op) {
        List <CropEntry> selected = adapter.getSelected();
        if (selected.size() != 1) return;
        pendingA  = selected.get (0);
        pendingOp = op;
        adapter.clearSelection();
        showHint ("Now tap the second crop (B)");
    }

    // For COPY — straightforward, no second selection needed.
    void onCopyButtonClicked () {
        cropManager.copy (adapter.getSelected());
        adapter.clearSelection();
    }

    // Helpers (implement as Toast, Snackbar, or a hint TextView in your layout).
    void showHint (String msg) { /* show to user / }
    void hideHint ()           { /* hide hint    / }
}








package com.magd.multicrop.manager.commands;



import com.magd.multicrop.enums.RegionOperation;

import java.util.List;



// =========================================================
// BooleanOpFlowExample
// =========================================================

// Shows how to wire the 3-step boolean-op flow
// in an Activity or Fragment.

// =========================================================
// FLOW:

    // 1. User selects crop A.
    // 2. User picks operation (UNION / DIFFERENCE / INTERSECT / XOR).
    // 3. User taps crop B → execute the operation.

// This is a 3-step sequential flow, not a batch operation.
// The adapter stays in selection mode throughout.

// =========================================================

public class BooleanOpFlowExample {



    // =========================================================
    // VARIABLES
    // =========================================================

    // Wire these from your Activity / Fragment.
    private CropManager     cropManager;
    private CropListAdapter adapter;

    private CropEntry       pendingA;   // First selected entry.
    private RegionOperation pendingOp;  // Chosen operation.



    // =========================================================
    // PUBLIC METHODS
    // =========================================================

    // Step 1 + 2: user taps an operation button while one crop is selected.
    void onUnionButtonClicked()      { startBooleanOp (RegionOperation.UNION);      }
    void onDifferenceButtonClicked() { startBooleanOp (RegionOperation.DIFFERENCE); }
    void onIntersectButtonClicked()  { startBooleanOp (RegionOperation.INTERSECT);  }
    void onXorButtonClicked()        { startBooleanOp (RegionOperation.XOR);        }

    // Step 3: wire this inside CropListAdapter.ActionListener.onCropTapped().
    void onCropTapped (CropEntry entry) {
        if (pendingA != null && pendingOp != null) {
            // We have A and op — this tap is B.
            if (entry == pendingA) {
                showHint ("Cannot operate on the same crop. Tap a different one.");
                return;
            }

            cropManager.booleanOp (pendingA, entry, pendingOp);

            // Reset pending state.
            pendingA  = null;
            pendingOp = null;
            hideHint();

        } else {
            // Normal tap — activate on canvas.
            entry.getCropArea().setActive (true);
        }
    }

    // COPY — no second selection needed.
    void onCopyButtonClicked() {
        cropManager.copy (adapter.getSelected());
        adapter.clearSelection();
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    private void startBooleanOp (RegionOperation op) {
        List <CropEntry> selected = adapter.getSelected();
        if (selected.size() != 1) {
            showHint ("Select exactly one crop first.");
            return;
        }

        pendingA  = selected.get (0);
        pendingOp = op;
        adapter.clearSelection();
        showHint ("Now tap the second crop (B).");
    }



    // =========================================================
    // HELPERS — implement as Toast, Snackbar, or hint TextView
    // =========================================================

    void showHint (String msg) { /* show to user *}
    void hideHint ()           { /* hide hint     }



}

*/