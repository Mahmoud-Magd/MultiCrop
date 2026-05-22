package com.magd.multicrop.enums;



// =========================================================
// RegionOperation
// =========================================================

// Defines how a CropRegion combines with the overall mask.

// =========================================================
// ARCHITECTURE:

// Every CropRegion in the system carries a RegionOperation.
// The MaskCompositor reads these operations and builds
// the final composited mask Path.

// Operations are applied sequentially in region list order.
// Order matters for DIFFERENCE and XOR.

// =========================================================
// BOOLEAN OPERATION REFERENCE:
    // A = existing mask
    // B = this region's path

    // UNION:
        // Expand mask to include B.
        // Result = A ∪ B.
        // ∪ = +
        // ∪ = Merging the two shapes together.
        // Use: add a new independent region.

    // DIFFERENCE:
        // Subtract B from A.
        // Result = A - B (difference: A minus B).
        // Use: background removal, hole punching, cut-out, subtraction mask.

    // INTERSECT:
        // Keep only the overlap of A and B.
        // Result = A ∩ B.
        // ∩ = Common area only.
        // Use: user defines what to keep (default croper behavior), refine selection to intersection only.

    // XOR:
        // XOR → Abbreviation for Exclusive OR (meaning .. A exclusive or B exclusive)
        // Keep non-overlapping parts of A and B.
        // Result = (A ∪ B) - (A ∩ B).
        // Use: toggle-style selection, outline cutouts.

// =========================================================
// ANDROID PATH MAPPING:
    // UNION -> Path.Op.UNION
    // DIFFERENCE -> Path.Op.DIFFERENCE
    // INTERSECT -> Path.Op.INTERSECT
    // XOR -> Path.Op.XOR

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

public enum RegionOperation {
    
    
    
    
    
    // =========================================================
    // UNION
    // =========================================================
    
    // Merges this region into the existing mask.
    // Expands the visible area.
    
    // Example use cases:
        // Adding additional crop areas
        // Multi-region selection
        // Additive masking
    UNION,
    
    
    
    // =========================================================
    // DIFFERENCE
    // =========================================================
    
    // Subtracts this region from the existing mask.
    // Carves a hole in whatever was previously masked.
        
    // Pixels inside this region are REMOVED.
    
    // Example use cases:
        // Complex cutout shapes
        // Multi-step mask refinement
        // Subtractive editing workflows
        // Background removal
        // Cutout editing
        // Exclusion zones
    DIFFERENCE,
    
    
    
    // =========================================================
    // INTERSECT
    // =========================================================
        
    // Keeps only the area where this region overlaps
    // with the existing mask.
    
    // Pixels inside this region are KEPT (IF IT OVERLAPS WITH THE EXISTING MASK)
    
    // Example use cases:
        // Constraining a selection to a boundary
        // Clip-to-region workflows
        // Precision selection refinement
        // Standard crop tool
        // Profile picture selection
        // Document region extraction
    INTERSECT,
    
    
    
    // =========================================================
    // XOR
    // =========================================================
    
    // Keeps non-overlapping areas of this region
    // and the existing mask.
    // Overlapping areas cancel each other out.
    
    // Example use cases:
        // Toggle-style region editing
        // Outline / border cutouts
        // Symmetric difference masks
    XOR
    
    
    
    
}


