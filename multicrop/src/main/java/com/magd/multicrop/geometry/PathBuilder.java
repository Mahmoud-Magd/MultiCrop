package com.magd.multicrop.geometry;



import android.graphics.Path;

import com.magd.multicrop.enums.RegionOperation;
import com.magd.multicrop.regions.CropRegion;

import java.util.List;



// =========================================================
// PathBuilder
// =========================================================

// Converts a list of CropRegions into a single composited Path.

// =========================================================
// RESPONSIBILITY:

// PathBuilder does ONE thing:
    // Takes regions + their operations
    // Produces a final Path

// It does NOT:
    // Draw anything
    // Hold any state
    // Know about the canvas
    // Know about bitmaps

// =========================================================
// HOW IT WORKS:

// Regions are processed in list order.
// Each region's operation determines how its Path
// combines with the running result.

// Step by step:
    // 1. Start with an empty result Path.
    // 2. For each visible region:
        // a. Build the region's individual Path.
        // b. Apply Path.Op based on RegionOperation.
        // c. Combine into running result.
    // 3. Return final composited Path.

// =========================================================
// OPERATION MAPPING:
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
// COORDINATE SPACE:
    // All input region paths must be in IMAGE SPACE.
    // Output path is in IMAGE SPACE.
    // Caller applies view matrix (zoom/pan) before drawing.

// =========================================================

public final class PathBuilder {
    
    
    
    
    
    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private PathBuilder() {}
    
    
    
    
    
    // =========================================================
    // PUBLIC METHODS
    // =========================================================
    
    // Builds a composited Path from a list of regions.
    
    // Parameters:
        // regions -> list of CropRegions with their operations.
        // Only visible regions are included.
    
    // Returns:
        // A single composited Path in image space.
        // Returns an empty Path if no visible regions exist.
    public static Path build (List<? extends CropRegion> regions, List <RegionOperation> operations) {
        Path result = new Path();
        
        if (regions == null || operations == null) return result;
        
        int count = Math.min ( regions.size(), operations.size() );
        
        for (int i = 0; i < count; i++) {
            CropRegion region = regions.get (i);
            
            if ( ! region.isVisible() ) continue; // If the region is not visible, skip it.
            
            Path regionPath = region.buildPath();
            RegionOperation operation = operations.get (i);
            
            result.op ( regionPath, toPathOp (operation) );
        }
        
        return result;
    }
    
    // Builds a path from a single region.
    // Useful for hit testing, preview rendering, or single-area export.
    public static Path buildSingle (CropRegion region) {
        if ( region == null || ! region.isVisible() ) return new Path();
        return region.buildPath();
    }
    
    
    
    
    
    // =========================================================
    // PRIVATE METHODS
    // =========================================================
    
    // Maps RegionOperation to the corresponding Path.Op.
    private static Path.Op toPathOp (RegionOperation operation) {
        switch (operation) {
            case UNION: return Path.Op.UNION;
            case DIFFERENCE: return Path.Op.DIFFERENCE;
            case INTERSECT: return Path.Op.INTERSECT;
            case XOR: return Path.Op.XOR;
            default: return Path.Op.UNION;
        }
    }
    
    
    
    
    
}


