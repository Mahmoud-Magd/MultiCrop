package com.magd.multicrop.enums;



// =========================================================
// FinalCropMode
// =========================================================

// Defines how the FINAL composited mask is applied to the image.

// =========================================================
// LEVEL: GLOBAL

// This enum works at the GLOBAL level.
// It acts AFTER PathBuilder has finished building the full mask Path.

// Think of it as:
    // "Now that we have the final mask shape, do we KEEP what's inside it
        // or REMOVE what's inside it?"

// =========================================================
// FinalCropMode vs RegionOperation:
    // The FinalCropMode
        // works at the global level
        // controls how the final mask is applied to the image during export,
        // after the final path or region has been calculated.
    // RegionOperation
        // works at each CropRegion in relation with the existing mask SEPARATELY,
        // controls how individual regions interacts with the existing mask.

// =========================================================
// TWO-STAGE PIPELINE:
    // Stage 1 — PathBuilder (per-region, uses RegionOperation):
        // Takes all individual regions.
        // Combines them using their RegionOperations.
        // Produces ONE final composited mask Path.
        // At this point the path is a single shape.
        // Individual regions no longer exist separately.
    
    // Stage 2 — FinalCropMode (global, applied at render/export time):
        // Takes the final single Path from Stage 1.
        // Decides what to do with it.
        // KEEP_INSIDE -> show pixels inside the path, hide everything outside.
        // REMOVE_INSIDE -> hide pixels inside the path, show everything outside.

// =========================================================
// WHY THESE ARE NOT THE SAME THING:

// RegionOperation answers:
    // "How does region B combine with the mask built so far?"
    // This is a local, incremental question. Answered per region.

// FinalCropMode answers:
    // "What do we do with the finished single mask shape as a whole?"
    // This is a global question. Answered once for the entire session.

// =========================================================
// CONCRETE EXAMPLE:

// Setup:
    // Region 1: large rectangle (UNION)
    // Region 2: small circle inside it (DIFFERENCE)

// After PathBuilder (Stage 1):
    // Result = one single path = rectangle with a hole where the circle was.
    // The circle is GONE from the path. It is no longer part of the shape.
    // What remains is a donut-like shape (rectangle minus the circular area).

// With FinalCropMode.KEEP_INSIDE:
    // The donut shape acts as a window.
    // Export shows: the donut area of the image.
    // The hole area is transparent (the circle was cut — it stays gone).
    // Everything outside the rectangle is also transparent.

// With FinalCropMode.REMOVE_INSIDE:
    // The donut shape acts as a mask to remove.
    // Export shows: everything EXCEPT the donut area.
    // So: the hole area AND everything outside the rectangle are both visible.
    // The donut ring itself becomes transparent.
    // The circle still does NOT reappear — it was cut from the path in Stage 1.
        // It is absent from the mask, which means it is NOT removed by FinalCropMode,
        // which means it remains visible in the output.

// =========================================================
// RELATIONSHIP TO RegionOperation:

// RegionOperation = per-region logic = how regions combine with each other.
// FinalCropMode = global logic = how the FINAL single mask is applied to the image.

// These are two completely separate concerns.
// One does NOT replace the other.

// =========================================================

public enum FinalCropMode {
    
    
    
    // =========================================================
    // KEEP_INSIDE
    // =========================================================
    
    // Pixels inside the final mask path are KEPT.
    // Pixels outside the final mask path are hidden (transparent or background color).
    
    // The mask acts like a "window":
        // Only what's inside the window survives.
    
    // Rendering behavior:
        // Final output = original image ∩ mask
        // ∩ = Common area only, between the (original img) and the (final mask).
    
    // Typical use cases:
        // Standard cropping tools (like Instagram crop)
        // Profile picture cropping
        // Any scenario where user defines "visible area"
    KEEP_INSIDE,
    
    
    
    // =========================================================
    // REMOVE_INSIDE
    // =========================================================
    
    // Pixels inside the final mask path are REMOVED.
    // Pixels outside the final mask path remain fully visible.
    
    // The mask acts like an "eraser":
        // Whatever is inside it gets cut out.
    
    // Rendering behavior:
        // Final output = original image - mask
    
    // Important:
        // This is NOT just a visual inversion.
        // It changes the compositing logic during rendering and export.
    
    // Typical use cases:
        // Background removal tools
        // Eraser / cut-out editing
        // Highlighting by exclusion (mask holes)
    REMOVE_INSIDE
    
    
    
}


