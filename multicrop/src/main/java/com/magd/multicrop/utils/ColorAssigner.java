package com.magd.multicrop.utils;



import android.graphics.Color;

import java.util.Arrays;
import java.util.List;
import java.util.Random;



// =========================================================
// ColorAssigner
// =========================================================

// Assigns a unique color to each new CropArea.

// =========================================================
// COLOR RULES:

    // Index 0:
        // Always WHITE.

    // Index 1 ... COLORS.size():
        // Assigned from COLORS list in order.

    // Beyond list:
        // Random unique color generated.
        // Must not collide with COLORS list.
        // Must not collide with any already-used color.

// =========================================================
// WHY WHITE FIRST?

    // First crop area benefits from high visibility.
    // White stands out against most images.

// =========================================================

public final class ColorAssigner {
    
    
    
    
    
    // =========================================================
    // CONSTANTS
    // =========================================================
    
    // Ordered color pool for areas 1, 2, 3 ...
    public static final List <Integer> COLORS = Arrays.asList (
        Color.parseColor ("#FF4444"), // red
        Color.parseColor ("#44AAFF"), // blue
        Color.parseColor ("#44FF88"), // green
        Color.parseColor ("#FFAA00"), // orange
        Color.parseColor ("#CC44FF"), // purple
        Color.parseColor ("#FF44CC"), // pink
        Color.parseColor ("#00CCCC"), // teal
        Color.parseColor ("#FFFF44") // yellow
    );
    
    private static final Random RNG = new Random();
    
    
    
    
    
    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private ColorAssigner() {}
    
    
    
    
    
    // =========================================================
    // PUBLIC METHODS
    // =========================================================
    
    // Returns the color for the next crop area.
    
    // Parameters:
        // existingColors -> colors already assigned to current areas.
            // Used to guarantee uniqueness for random colors.
    
    // Rules:
        // existingColors.size() == 0 -> WHITE
        // existingColors.size() == 1 -> COLORS.get (0)
        // existingColors.size() == 2 -> COLORS.get (1)
        // ... and so on until COLORS is exhausted.
        // Beyond list -> unique random color.
    public static int next (List <Integer> existingColors) {
        int index = existingColors.size();
        
        if (index == 0) return Color.WHITE;
        
        int colorIndex = index - 1;
        if ( colorIndex < COLORS.size() ) return COLORS.get (colorIndex);
        
        return generateUnique (existingColors);
    }
    
    // Overload for convenience when passing count only (no collision check).
    
    // WARNING:
        // Only use this overload if uniqueness against previously assigned
        // colors is not required. Prefer next(List) for full safety.
    public static int next (int existingCount) {
        if (existingCount == 0) return Color.WHITE;
        
        int colorIndex = existingCount - 1;
        if ( colorIndex < COLORS.size() ) return COLORS.get (colorIndex);
        
        return generateUnique (null);
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================
    
    // Generates a random color not in COLORS list and not in existingColors.
    
    // Max attempts: 200.
    // After 200 failed attempts, returns last candidate regardless.
    // (Collision at that scale is visually irrelevant.)
    private static int generateUnique (List <Integer> existingColors) {
        int candidate;
        int attempts = 0;
        
        do {
            // Keep components in visible mid-range.
            // Avoids near-black / near-white / near-grey results.
            int r = 60 + RNG.nextInt (180);
            int g = 60 + RNG.nextInt (180);
            int b = 60 + RNG.nextInt (180);

            candidate = Color.rgb (r, g, b);
            attempts++;
            
        } while (
            attempts < 200
            &&
            (
                COLORS.contains (candidate)
                ||
                (
                    existingColors != null
                    &&
                    existingColors.contains (candidate)
                )
            )
        );
        
        return candidate;
    }
    
    
    
    
    
}


