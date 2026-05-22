package com.magd.multicrop.export;



import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.magd.multicrop.enums.ExportMode;
import com.magd.multicrop.enums.FinalCropMode;
import com.magd.multicrop.enums.MaskMode;
import com.magd.multicrop.enums.RegionOperation;
import com.magd.multicrop.geometry.PathBuilder;
import com.magd.multicrop.regions.CropRegion;

import java.util.ArrayList;
import java.util.List;



// =========================================================
// BitmapExporter
// =========================================================

// Handles ALL export operations for MultiCrop.

// =========================================================
// EXPORT MODES:

    // INDIVIDUAL:
        // Returns one Bitmap per CropRegion.
        // Each bitmap is masked to the exact region shape.
        // Bounding box defines the bitmap dimensions.

    // MERGED:
        // Returns one combined Bitmap.
        // All regions composited using their RegionOperations.
        // FinalCropMode determines keep-inside or remove-inside.
        // Outside pixels handled by MaskMode.

// =========================================================
// COORDINATE MAPPING:

    // CropRegion paths are in IMAGE SPACE:
        // (0,0) = top-left of source bitmap
        // (imageWidth, imageHeight) = bottom-right

    // Source bitmap pixels map 1:1 to image space.
    // No scaling needed for full-resolution export.

// =========================================================
// NO INTERMEDIATE BITMAPS (MERGED mode):

    // Merged export draws directly using canvas.drawBitmap()
    // with a path-clipped canvas.
    // No per-region Bitmap.createBitmap() allocations.

// =========================================================

public final class BitmapExporter {



    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private BitmapExporter() {}



    // =========================================================
    // PUBLIC METHODS
    // =========================================================

    // Main export method.

    // Parameters:
        // src           -> original source bitmap (never modified)
        // regions       -> list of CropRegions in image space
        // operations    -> parallel list of RegionOperations
        // finalCropMode -> KEEP_INSIDE or REMOVE_INSIDE
        // exportMode    -> INDIVIDUAL or MERGED
        // maskMode      -> TRANSPARENT or BLACK (MERGED only)

    // Returns:
        // INDIVIDUAL -> one Bitmap per region
        // MERGED     -> list with exactly one Bitmap
    public static List <Bitmap> export (
        Bitmap src,
        List <? extends CropRegion> regions,
        List <RegionOperation> operations,
        FinalCropMode finalCropMode,
        ExportMode exportMode,
        MaskMode maskMode
    ) {
        if (src == null || regions == null || regions.isEmpty()) return new ArrayList<>();

        if (exportMode == ExportMode.INDIVIDUAL) {
            return exportIndividual (src, regions, finalCropMode);
        } else {
            List <Bitmap> result = new ArrayList<>();
            Bitmap merged = exportMerged (src, regions, operations, finalCropMode, maskMode);
            if (merged != null) result.add (merged);
            return result;
        }
    }



    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    // ======= Individual export =======

    // Returns one Bitmap per CropRegion.
    // Each bitmap is cropped to the region's bounding box
    // and masked to the exact region shape.
    private static List <Bitmap> exportIndividual (
        Bitmap src,
        List <? extends CropRegion> regions,
        FinalCropMode finalCropMode
    ) {
        List <Bitmap> results = new ArrayList<>();

        for (CropRegion region : regions) {
            if ( ! region.isVisible() ) continue;

            Bitmap cropped = exportSingleRegion (src, region, finalCropMode);
            if (cropped != null) results.add (cropped);
        }

        return results;
    }

    // Exports one region as a Bitmap.
    //
    // Steps:
        // 1. Get region bounding box.
        // 2. Clip canvas to region's exact Path.
        // 3. Draw source bitmap onto clipped canvas.
        // 4. Apply FinalCropMode (keep inside / remove inside).
    private static Bitmap exportSingleRegion (
        Bitmap src,
        CropRegion region,
        FinalCropMode finalCropMode
    ) {
        RectF bounds = region.getBounds();
        if (bounds == null || bounds.isEmpty()) return null;

        // Clamp bounds to bitmap dimensions.
        bounds.left   = Math.max (0, bounds.left);
        bounds.top    = Math.max (0, bounds.top);
        bounds.right  = Math.min (src.getWidth(),  bounds.right);
        bounds.bottom = Math.min (src.getHeight(), bounds.bottom);

        int w = (int) bounds.width();
        int h = (int) bounds.height();

        if (w <= 0 || h <= 0) return null;

        Bitmap output = Bitmap.createBitmap (w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas (output);

        // Translate so region draws relative to its bounding box.
        canvas.translate (-bounds.left, -bounds.top);

        Path  regionPath = region.buildPath();
        Paint paint      = new Paint (Paint.FILTER_BITMAP_FLAG);

        if (finalCropMode == FinalCropMode.KEEP_INSIDE) {
            canvas.clipPath (regionPath);
            canvas.drawBitmap (src, 0, 0, paint);
        } else {
            // REMOVE_INSIDE: draw full image, then clip out the region.
            canvas.drawBitmap (src, 0, 0, paint);
            canvas.clipOutPath (regionPath); // API 26+.
            canvas.drawColor (Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
        }

        return output;
    }



    // ======= Merged export =======

    // Returns one combined Bitmap.
    // All regions composited into a single output.
    // No intermediate per-region Bitmap allocations.
    private static Bitmap exportMerged (
        Bitmap src,
        List <? extends CropRegion> regions,
        List <RegionOperation> operations,
        FinalCropMode finalCropMode,
        MaskMode maskMode
    ) {
        int bw = src.getWidth();
        int bh = src.getHeight();

        // Always ARGB_8888 for transparency support.
        Bitmap output = Bitmap.createBitmap (bw, bh, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas (output);

        // Fill background based on mask mode.
        if (maskMode == MaskMode.BLACK) canvas.drawColor (Color.BLACK);
        // MaskMode.TRANSPARENT: ARGB_8888 initializes to 0x00000000.

        // Build the combined mask path from all regions.
        Path  maskPath = PathBuilder.build (regions, operations);
        Paint paint    = new Paint (Paint.FILTER_BITMAP_FLAG);

        if (finalCropMode == FinalCropMode.KEEP_INSIDE) {
            canvas.save();
            canvas.clipPath (maskPath);
            canvas.drawBitmap (src, 0, 0, paint);
            canvas.restore();
        } else {
            // REMOVE_INSIDE: draw full image, then erase the mask area.
            canvas.drawBitmap (src, 0, 0, paint);
            canvas.save();
            canvas.clipPath (maskPath);
            canvas.drawColor (Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
            canvas.restore();
        }

        return output;
    }



}
