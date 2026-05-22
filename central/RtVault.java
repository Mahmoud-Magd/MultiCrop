package com.magd.multicrop.central;



import android.content.Context;





// =========================================================
// RtVault = RuntimeVault
// =========================================================

// Internal runtime cache used by the library.

// PURPOSE:
    // Centralized place for:
        // app context
        // density
        // future caches
        // shared reusable objects

// IMPORTANT:
    // Internal library infrastructure.
    // Not intended for public usage.

// =========================================================

public final class RtVault {
    
    
    
    
    
    // =========================================================
    // CONSTANTS
    // =========================================================
    
    // Regular centered crop shapes, width & height.
    public static final float R_C_WIDTH = 0.4f; // 40% of the original image width.
    public static final float R_C_HEIGHT = 0.2f; // 20% of the original image height.
    
    
    
    
    
    // =========================================================
    // VARIABLES
    // =========================================================
    private static Context appContext; // Store application context only.
    private static float density; // Cached screen density.
    
    
    
    
    
    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private RtVault() {}
    
    
    
    
    
    // =========================================================
    // PUBLIC METHODS
    // =========================================================
    
    // Initializes runtime cache.
    // IMPORTANT:
        // Uses application context internally
        // to avoid Activity leaks.
    // synchronized → for thread safety
    public static synchronized void init (Context context) {
        if ( isInitialized() ) throw new IllegalStateException ("RtVault is already initialized.");
        if (context == null) throw new NullPointerException ("RtVault.init() received null context.");
        
        cacheRuntimeData (context);
    }
    
    // Returns cached application context.
    public static Context getAppCtx() {
        ensureInitialized();
        return appContext;
    }
    
    // Returns cached density.
    public static float getDensity() {
        ensureInitialized();
        return density;
    }
    
    
    
    
    
    // =========================================================
    // PRIVATE METHODS
    // =========================================================
    
    private static boolean isInitialized() { return appContext != null; }
    
    private static void cacheRuntimeData (Context context) {
        appContext = context.getApplicationContext();
        density = appContext .getResources() .getDisplayMetrics() .density;
    }
    
    // Ensures runtime initialized before usage.
    private static void ensureInitialized() {
        if ( ! isInitialized() ) {
            throw new IllegalStateException (
                "MultiCrop library is not initialized.\n"
                +
                "Call MultiCrop.init ( getApplicationContext() ) first."
            );
        }
    }
    
    
    
    
    
}


