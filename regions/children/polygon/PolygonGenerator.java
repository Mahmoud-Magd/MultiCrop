package com.magd.multicrop.regions.children.polygon;



import android.graphics.PointF;

import com.magd.multicrop.central.RtVault;

import java.util.List;



// =========================================================
// PolygonGenerator
// =========================================================

// Generates evenly-spaced points for a regular polygon.

// =========================================================
// WHY THIS EXISTS:

// The regular-polygon constructor logic was extracted from
// PolygonRegion to keep that class under 300 lines.

// =========================================================
// ALGORITHM:

    // Polygon is inscribed in an ellipse sized:
        // width  = imgW × RtVault.R_C_WIDTH
        // height = imgH × RtVault.R_C_HEIGHT
    // Centered in the image.

    // Start angle:
        // Odd sides  → top vertex    (−PI/2)
        // Even sides → flat top edge (−PI/2 + step/2)

// =========================================================

final class PolygonGenerator {



    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private PolygonGenerator() {}



    // =========================================================
    // PUBLIC METHODS
    // =========================================================

    // Populates `out` with `n` evenly-spaced PointF objects
    // forming a regular polygon inscribed in the image center area.
    static void generate (List <PointF> out, int n, float imgW, float imgH) {
        float crpW = imgW * RtVault.R_C_WIDTH;
        float crpH = imgH * RtVault.R_C_HEIGHT;

        float left = (imgW - crpW) / 2f;
        float top  = (imgH - crpH) / 2f;

        float cx = left + crpW / 2f;
        float cy = top  + crpH / 2f;
        float rx = crpW / 2f; // Horizontal radius.
        float ry = crpH / 2f; // Vertical radius.

        double step = (Math.PI * 2d) / n;

        // Odd polygons: top vertex. Even polygons: flat top.
        double startAngle = (n % 2 == 0)
            ? (-Math.PI / 2d) + (step / 2d)
            : (-Math.PI / 2d);

        for (int i = 0; i < n; i++) {
            double angle = startAngle + (i * step);

            out.add ( new PointF (
                (float) (cx + Math.cos (angle) * rx),
                (float) (cy + Math.sin (angle) * ry)
            ));
        }
    }



}
