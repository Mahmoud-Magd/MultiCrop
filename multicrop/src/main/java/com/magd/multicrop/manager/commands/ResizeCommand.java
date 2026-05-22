package com.magd.multicrop.manager.commands;



import android.graphics.RectF;

import com.magd.multicrop.manager.CropEntry;
import com.magd.multicrop.manager.CropManager;
import com.magd.multicrop.models.CropRect;
import com.magd.multicrop.utils.DimensUtils;



// =========================================================
// ResizeCommand
// =========================================================

// Sets the exact width and/or height of a CropEntry
// from dp values entered by the user (e.g. via an EditText).

// =========================================================
// RESIZE STRATEGY:
    // The new size is applied around the EXISTING CENTER POINT
        // of the region's bounding box.
    // This feels natural — the crop stays "where it was" and
        // only grows or shrinks outward.

    // If a dimension argument is <= 0 it is treated as
        // "no change" and the current value is preserved.

// =========================================================
// CLAMPING RULES:

    // Result rect is clamped to image bounds after resizing.
    // Width / height are never allowed below CropRect.MIN_WIDTH
        // and CropRect.MIN_HEIGHT respectively.

    // If the centered expansion would push an edge outside the
        // image, the whole rect is shifted back inward (same as
        // CropRect.offset clamping logic).

// =========================================================
// UNDO:

    // Previous bounding box is snapshotted at construction time.
    // undo() restores both the region edges and the CropArea
        // rect to the snapshot.

// =========================================================
// UNITS:

    // Public API accepts DP.
    // Internally converted to image-space pixels via DimensUtils.
    // Image space == bitmap pixel space (1:1).

    // NOTE: "image-space pixels" are NOT the same as screen pixels
        // when the user has zoomed in or out. The dp→px conversion
        // here targets BITMAP pixels, which is what makes the
        // exported image exactly the requested size.

    // Rationale: users think in dp (device-independent), so
        // they expect the same physical size on every device. Storing
        // the result as bitmap pixels ensures the crop area covers
        // the proportionally correct region of the source image
        // regardless of the device screen density.

// =========================================================

public class ResizeCommand implements CropCommand {



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropManager manager;
    private final CropEntry   target;

    // Requested dimensions in IMAGE-SPACE px (converted from dp).
    // -1 means "keep current".
    private final float requestedWidthPx;
    private final float requestedHeightPx;

    // Snapshot for undo.
    private final RectF previousBounds;



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    /**
     * @param manager          The owning CropManager.
     * @param target           The entry to resize.
     * @param newWidthDp       Desired width in dp. Pass ≤ 0 to leave width unchanged.
     * @param newHeightDp      Desired height in dp. Pass ≤ 0 to leave height unchanged.
     */
    public ResizeCommand (
        CropManager manager,
        CropEntry   target,
        float       newWidthDp,
        float       newHeightDp
    ) {
        this.manager = manager;
        this.target  = target;

        // Convert dp → px for internal use.
        this.requestedWidthPx  = (newWidthDp  > 0) ? DimensUtils.dpToPx (newWidthDp)  : -1f;
        this.requestedHeightPx = (newHeightDp > 0) ? DimensUtils.dpToPx (newHeightDp) : -1f;

        // Snapshot current bounds BEFORE any change.
        RectF b = target.getRegion().getBounds();
        this.previousBounds = (b != null) ? new RectF (b) : new RectF (0, 0, 1, 1);
    }



    // =========================================================
    // CROP COMMAND IMPLEMENTATIONS
    // =========================================================

    @Override
    public void execute() {
        applyBounds (computeNewBounds());
    }

    @Override
    public void undo() {
        applyBounds (previousBounds);
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder ("Resize");
        if (requestedWidthPx  > 0) sb.append (" W=").append ( (int) DimensUtils.pxToDp (requestedWidthPx)  ).append ("dp");
        if (requestedHeightPx > 0) sb.append (" H=").append ( (int) DimensUtils.pxToDp (requestedHeightPx) ).append ("dp");
        return sb.toString();
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    // Computes the new bounding rect centered on the old center.
    private RectF computeNewBounds() {
        float cx = previousBounds.centerX();
        float cy = previousBounds.centerY();

        float newW = (requestedWidthPx  > 0) ? requestedWidthPx  : previousBounds.width();
        float newH = (requestedHeightPx > 0) ? requestedHeightPx : previousBounds.height();

        // Enforce minimum size.
        newW = Math.max (newW, CropRect.getMinWidth());
        newH = Math.max (newH, CropRect.getMinHeight());

        float l = cx - newW / 2f;
        float t = cy - newH / 2f;
        float r = cx + newW / 2f;
        float b = cy + newH / 2f;

        // Clamp inside image bounds.
        float imgW = imageWidth();
        float imgH = imageHeight();

        if (l < 0)     { r -= l;          l = 0;    }
        if (t < 0)     { b -= t;          t = 0;    }
        if (r > imgW)  { l -= r - imgW;   r = imgW; }
        if (b > imgH)  { t -= b - imgH;   b = imgH; }

        // Final safety clamp (extreme edge case: image smaller than requested size).
        l = Math.max (0, l);
        t = Math.max (0, t);
        r = Math.min (imgW, Math.max (l + CropRect.getMinWidth(),  r));
        b = Math.min (imgH, Math.max (t + CropRect.getMinHeight(), b));

        return new RectF (l, t, r, b);
    }

    // Applies the new bounding rect to both region and CropArea.
    private void applyBounds (RectF bounds) {
        target.getRegion().setEdges   (bounds.left, bounds.top, bounds.right, bounds.bottom);
        target.getCropArea().getCropRect().set (bounds.left, bounds.top, bounds.right, bounds.bottom);
        target.getCropArea().invalidate();
        manager.getMultiCrop().notifyRegionChanged();
        manager.notifyEntryChanged (target);
    }

    private float imageWidth() {
        android.graphics.Bitmap bmp = manager.getMultiCrop().getSourceBitmap();
        return (bmp != null) ? bmp.getWidth()  : 10_000f;
    }

    private float imageHeight() {
        android.graphics.Bitmap bmp = manager.getMultiCrop().getSourceBitmap();
        return (bmp != null) ? bmp.getHeight() : 10_000f;
    }



}
