package com.magd.multicrop.manager;



import com.magd.multicrop.regions.CropRegion;
import com.magd.multicrop.views.crop_area.CropArea;



// =========================================================
// CropEntry
// =========================================================

// Data model that represents ONE crop in the CropManager.

// =========================================================
// WHY THIS EXISTS:

// CropArea is a View. Views should not hold UI-state metadata
// like labels, pinned state, or rotation angle.

// CropEntry wraps a CropArea and adds:
    // - Human-readable label ("Crop 1", "Face", etc.)
    // - Pinned state (pinned crops cannot be moved or resized)
    // - Rotation angle in degrees (image-space rotation)
    // - Visibility state (excludes region from tint + export)
    // - CropAreaHidden state (hides border + handles visually only)

// =========================================================
// VISIBILITY VS CROP AREA HIDDEN:

    // visible = false:
        // Region excluded from tint + export.
        // CropArea view hidden.
        // Entry greyed out in list.

    // cropAreaHidden = true:
        // Border + handles hidden (alpha = 0).
        // Region still active in tint + export.
        // Touch still routes to CropArea (unless pinned).
        // Entry appears normally in list.

// =========================================================
// RELATIONSHIP:

    // CropEntry (data layer)
    //     └── CropArea  (view layer)
    //             └── CropRegion (geometry layer)

// =========================================================

public class CropEntry {



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropArea   cropArea; // The view. Final — never replaced, only mutated.
    private final CropRegion region;   // The geometry. Convenience ref to cropArea.getRegion().
    private final int        color;    // Assigned color. Matches CropArea color.

    private String  label           = "Crop"; // Human-readable name shown in the list.
    private boolean pinned          = false;  // Pinned: move + resize blocked in CropArea.
    private boolean visible         = true;   // Region in tint + export.
    private float   rotation        = 0f;     // Image-space rotation in degrees.
    private boolean cropAreaHidden  = false;  // Border + handles hidden (region still active).



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public CropEntry (CropArea cropArea, String label) {
        this.cropArea = cropArea;
        this.region   = cropArea.getRegion();
        this.color    = cropArea.getColor();
        this.label    = label;
    }



    // =========================================================
    // PUBLIC METHODS — GETTERS
    // =========================================================

    public CropArea   getCropArea()       { return cropArea;       }
    public CropRegion getRegion()         { return region;         }
    public int        getColor()          { return color;          }
    public String     getLabel()          { return label;          }
    public boolean    isPinned()          { return pinned;         }
    public boolean    isVisible()         { return visible;        }
    public float      getRotation()       { return rotation;       }
    public boolean    isCropAreaHidden()  { return cropAreaHidden; }



    // =========================================================
    // PUBLIC METHODS — SETTERS
    // =========================================================

    public void setLabel          (String label)          { this.label          = label;          }
    public void setPinned         (boolean pinned)        { this.pinned         = pinned;         }
    public void setVisible        (boolean visible)       { this.visible        = visible;        }
    public void setRotation       (float rotation)        { this.rotation       = rotation;       }
    public void setCropAreaHidden (boolean hidden)        { this.cropAreaHidden = hidden;         }



}
