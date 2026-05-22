package com.magd.multicrop.manager.adapter;



import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;



// =========================================================
// CropRowViewBuilder
// =========================================================

// Builds the RecyclerView row view for CropListAdapter.

// =========================================================
// WHY THIS EXISTS:

// CropListAdapter.buildRowView() was extracted here to
// keep CropListAdapter under 300 lines.

// Row layout (programmatic — no XML):
    // [ColorSwatch] [Label] [RotationLabel] [PinnedIcon] [VisibleIcon]

// =========================================================

final class CropRowViewBuilder {



    // =========================================================
    // CONSTRUCTORS
    // =========================================================
    private CropRowViewBuilder() {}



    // =========================================================
    // PUBLIC METHODS
    // =========================================================

    static View build (Context context) {

        LinearLayout row = new LinearLayout (context);
        row.setOrientation (LinearLayout.HORIZONTAL);
        row.setPadding (24, 16, 24, 16);
        row.setLayoutParams ( new RecyclerView.LayoutParams (
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        ));

        // Color swatch.
        ImageView swatch = new ImageView (context);
        LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams (40, 40);
        swatchParams.gravity     = Gravity.CENTER_VERTICAL;
        swatchParams.rightMargin = 24;
        swatch.setLayoutParams (swatchParams);
        swatch.setTag ("colorSwatch");
        row.addView (swatch);

        // Label.
        TextView label = new TextView (context);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams (
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        labelParams.gravity = Gravity.CENTER_VERTICAL;
        label.setLayoutParams (labelParams);
        label.setTextSize (14f);
        label.setTag ("label");
        row.addView (label);

        // Rotation label.
        TextView rotLabel = new TextView (context);
        LinearLayout.LayoutParams rotParams = new LinearLayout.LayoutParams (
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rotParams.gravity     = Gravity.CENTER_VERTICAL;
        rotParams.rightMargin = 8;
        rotLabel.setLayoutParams (rotParams);
        rotLabel.setTextSize (11f);
        rotLabel.setTextColor (0xFF888888);
        rotLabel.setTag ("rotLabel");
        row.addView (rotLabel);

        // Pinned icon (📌 as text — no drawable required).
        TextView pinIcon = new TextView (context);
        LinearLayout.LayoutParams pinParams = new LinearLayout.LayoutParams (
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        pinParams.gravity     = Gravity.CENTER_VERTICAL;
        pinParams.rightMargin = 8;
        pinIcon.setLayoutParams (pinParams);
        pinIcon.setTextSize (16f);
        pinIcon.setText ("📌");
        pinIcon.setTag ("pinIcon");
        row.addView (pinIcon);

        // Visible icon (👁 as text).
        TextView visIcon = new TextView (context);
        LinearLayout.LayoutParams visParams = new LinearLayout.LayoutParams (
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        visParams.gravity = Gravity.CENTER_VERTICAL;
        visIcon.setLayoutParams (visParams);
        visIcon.setTextSize (16f);
        visIcon.setText ("👁");
        visIcon.setTag ("visIcon");
        row.addView (visIcon);

        return row;
    }



}
