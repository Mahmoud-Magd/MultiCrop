package com.magd.multicrop.views.multi_crop;



import android.graphics.PointF;
import android.graphics.RectF;

import com.magd.multicrop.central.RtVault;
import com.magd.multicrop.models.CropRect;
import com.magd.multicrop.regions.CropRegion;



// =========================================================
// MultiCropPlacer
// =========================================================

// Repositions and resizes a freshly-created region so it
// fits inside the currently visible image area.

// =========================================================
// WHY THIS EXISTS:

// centerRegionOnVisible() was extracted from MultiCrop to
// keep that class under 300 lines.

// =========================================================
// RULES:

    // Size is proportional to the visible image area
    //   via RtVault.R_C_WIDTH and R_C_HEIGHT.
    // Never smaller than CropRect.MIN_WIDTH / MIN_HEIGHT.
    // Clamped so the region never exits image bounds.
    // Final safety clamp prevents inversion at extreme zoom.

// =========================================================

final class MultiCropPlacer {



    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private MultiCropPlacer() {}



    // =========================================================
    // PUBLIC METHODS
    // =========================================================

    // Centers and sizes the region relative to the visible image area.
    static void center (MultiCrop mc, CropRegion region) {
        RectF bounds = region.getBounds();
        if (bounds == null) return;

        // ── 1. Visible image area in screen px ──────────────────────────────
        RectF ios = mc.zoomPan.getImageScreenRect();

        float vsl = Math.max (0,            ios.left);
        float vst = Math.max (0,            ios.top);
        float vsr = Math.min (mc.getWidth(),  ios.right);
        float vsb = Math.min (mc.getHeight(), ios.bottom);

        // ── 2. Convert visible area to image-space dimensions ────────────────
        float scale   = mc.zoomPan.getScale();
        float visImgW = (scale > 0) ? (vsr - vsl) / scale : mc.imgW;
        float visImgH = (scale > 0) ? (vsb - vst) / scale : mc.imgH;

        // ── 3. Target crop size (ratio × visible area, min-clamped) ─────────
        float cropW = Math.max (CropRect.getMinWidth(),  visImgW * RtVault.R_C_WIDTH);
        float cropH = Math.max (CropRect.getMinHeight(), visImgH * RtVault.R_C_HEIGHT);

        // ── 4. Center on visible image center ────────────────────────────────
        PointF vc = mc.getVisibleImageCenter();

        float l = vc.x - cropW / 2f;
        float t = vc.y - cropH / 2f;
        float r = vc.x + cropW / 2f;
        float b = vc.y + cropH / 2f;

        // ── 5. Clamp inside image bounds ─────────────────────────────────────
        if (l < 0)        { r -= l;           l = 0;       }
        if (t < 0)        { b -= t;           t = 0;       }
        if (r > mc.imgW)  { l -= r - mc.imgW; r = mc.imgW; }
        if (b > mc.imgH)  { t -= b - mc.imgH; b = mc.imgH; }

        // ── 6. Final safety clamp (handles extreme zoom edge cases) ──────────
        l = Math.max (0,      l);
        t = Math.max (0,      t);
        r = Math.min (mc.imgW, Math.max (l + CropRect.getMinWidth(),  r));
        b = Math.min (mc.imgH, Math.max (t + CropRect.getMinHeight(), b));

        region.setEdges (l, t, r, b);
    }



}
