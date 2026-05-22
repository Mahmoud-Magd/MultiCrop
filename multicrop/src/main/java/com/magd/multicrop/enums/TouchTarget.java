package com.magd.multicrop.enums;



// =========================================================
// TouchTarget
// =========================================================

// Represents the currently touched / interacted region
// inside a CropArea.

// Used by:
    // - Touch handling
    // - Resize logic
    // - Move logic
    // - Gesture routing

// =========================================================
// WHY THIS EXISTS:

// During touch handling we must know:
    // What exactly did the user touch?

// Examples:
    // - Crop body?
    // - Top-left handle?
    // - Right edge?
    // - Nothing?

// Different targets trigger different behaviors:
    // BODY -> move crop rect
    // TOP_LEFT -> diagonal resize
    // LEFT -> horizontal resize
    // TOP -> vertical resize
    // NONE -> ignore touch

// =========================================================
// IMPORTANT:

// This enum represents LOGICAL interaction targets.
// NOT visual elements.

// Example:
    // BODY does NOT mean:
        // "background view"
    // It means:
        // "touch inside movable crop region"

// =========================================================

public enum TouchTarget {
    
    // ======= NO TARGET =======
    // User touched nothing interactive.
    // Usually means:
        // - touch outside CropArea
        // - touch missed handles
        // - ignore gesture
    NONE,
    
    // ======= Body =======
    // User touched inside crop body.
    // Behavior:
        // move entire CropRect
    BODY,
    
    // ======= CORNER HANDLES =======
    // Diagonal resize handles.
    // Can resize:
        // width + height simultaneously
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    
    // ======= EDGE HANDLES =======
    // Single-axis resize handles.
    
    // TOP / BOTTOM:
        // vertical resize only
    TOP,
    BOTTOM,
    
    // LEFT / RIGHT:
        // horizontal resize only
    LEFT,
    RIGHT
    
    
    
    
    
}


