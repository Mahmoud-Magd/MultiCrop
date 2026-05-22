package com.magd.multicrop.enums;



// =========================================================
// ExportMode
// =========================================================

// Controls HOW crop results are exported.

// IMPORTANT:
    // This enum controls OUTPUT STRUCTURE.
    // NOT masking behavior.
    // NOT bitmap compression.
    // NOT scaling.

// =========================================================
// MODES:

    // INDIVIDUAL:
        // Export every CropArea as its own Bitmap.

    // MERGED:
        // Export ALL CropAreas into ONE Bitmap.
        // Outside pixels are deleted (become transparent or black).
        // Pixels INSIDE crop areas remain unchanged.

// =========================================================
// EXAMPLES:

    // INDIVIDUAL:
        // Crop #1 -> bitmap
        // Crop #2 -> bitmap
        // Crop #3 -> bitmap

    // MERGED:
        // ALL crop regions merged into single bitmap.
        // Outside pixels are deleted (become transparent or black).
        // Pixels INSIDE crop areas remain unchanged.

// =========================================================

public enum ExportMode {
    
    
    
    
    
    // =========================================================
    // INDIVIDUAL
    // =========================================================
    
    // Every CropArea becomes its own Bitmap.
    
    // Example use cases:
        // OCR regions
        // document pieces
        // batch export
        // independent image saving
    INDIVIDUAL,
    
    
    
    
    
    // =========================================================
    // MERGED
    // =========================================================
    
    // All CropAreas rendered into ONE Bitmap.
    // Example use cases:
        // combined collage
        // multi-selection masking
        // smart scan result
    MERGED
    
    
    
    
    
}


