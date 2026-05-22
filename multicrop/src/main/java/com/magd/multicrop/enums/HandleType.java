package com.magd.multicrop.enums;



// =========================================================
// HandleType
// =========================================================
//
// Represents the 8 resize handles around a CropArea.
//
// Layout:
//
//      TOP_LEFT          TOP             TOP_RIGHT
//
//      LEFT                                     RIGHT
//
//      BOTTOM_LEFT   BOTTOM     BOTTOM_RIGHT
//
// =========================================================
// ======= Corner Handles =======
    // TOP_LEFT / TOP_RIGHT / BOTTOM_LEFT / BOTTOM_RIGHT
    // Can move on X + Y axis.
    
// =========================================================
// ======= Edge Handles =======
    // TOP / BOTTOM
    // Can move only on Y axis.
    
    // LEFT / RIGHT
    // Can move only on X axis.

// =========================================================



public enum HandleType {
    
    // ======= Arranged in order; for easier memorization =======
    
    TOP_LEFT, // Corner Handle (moves on X + Y axis)
    TOP, // Edge Handle (moves only on Y axis)
    TOP_RIGHT, // Corner Handle (moves on X + Y axis)
    
    RIGHT, // Edge Handle (moves only on X axis)
    
    BOTTOM_RIGHT, // Corner Handle (moves on X + Y axis)
    BOTTOM, // Edge Handle (moves only on Y axis)
    BOTTOM_LEFT, // Corner Handle (moves on X + Y axis)
    
    LEFT // Edge Handle (moves only on X axis)
    
    
    
}


