package com.magd.multicrop.manager.adapter;



import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;



import com.magd.multicrop.manager.CropManager;
import com.magd.multicrop.manager.CropEntry;



// =========================================================
// CropListAdapter
// =========================================================

// RecyclerView adapter that displays all CropEntry objects.

// =========================================================
// FEATURES:

    // Multi-selection mode:
        // Long-press a row to enter selection mode.
        // Tap more rows to add / remove from selection.
        // Exit mode when selection becomes empty.

    // Row states:
        // Normal   — default appearance.
        // Selected — highlighted background.
        // Pinned   — pin icon visible.
        // Hidden   — reduced alpha.

    // Click behavior:
        // NORMAL mode:    single tap = activate crop on canvas.
        // SELECTION mode: single tap = toggle selection.

// =========================================================
// LAYOUT (programmatic — no XML required):

// Each row is built by CropRowViewBuilder and contains:
    // [ColorSwatch] [Label] [RotationLabel] [PinnedIcon] [VisibleIcon]

// =========================================================
// WIRING:

    // CropManager.setChangeListener(adapter)
    //     → adapter receives onListChanged() / onEntryChanged(index)

// =========================================================

public class CropListAdapter
    extends RecyclerView.Adapter <CropListAdapter.ViewHolder>
    implements CropManager.ChangeListener {



    // =========================================================
    // LISTENER
    // =========================================================

    public interface ActionListener {

        // Called when a crop row is tapped in normal mode.
        void onCropTapped (CropEntry entry);

        // Called when selection changes.
        void onSelectionChanged (List <CropEntry> selected, boolean inSelectionMode);
    }



    // =========================================================
    // VARIABLES
    // =========================================================

    private final CropManager    manager;
    private final ActionListener actionListener;

    private final List <CropEntry> selected       = new ArrayList<>();
    private boolean                inSelectionMode = false;



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public CropListAdapter (CropManager manager, ActionListener actionListener) {
        this.manager        = manager;
        this.actionListener = actionListener;

        manager.setChangeListener (this);
    }



    // =========================================================
    // RECYCLER VIEW ADAPTER
    // =========================================================

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder ( CropRowViewBuilder.build (parent.getContext()) );
    }

    @Override
    public void onBindViewHolder (@NonNull ViewHolder holder, int position) {
        CropEntry entry      = manager.getEntry (position);
        boolean   isSelected = selected.contains (entry);

        // Color swatch.
        GradientDrawable swatch = new GradientDrawable();
        swatch.setShape (GradientDrawable.OVAL);
        swatch.setColor (entry.getColor());
        swatch.setSize (40, 40);
        holder.colorSwatch.setBackground (swatch);

        // Label.
        holder.label.setText ( entry.getLabel() );

        // Pinned icon.
        holder.pinnedIcon.setVisibility (entry.isPinned() ? View.VISIBLE : View.GONE);

        // Visibility icon.
        holder.visibleIcon.setAlpha (entry.isVisible() ? 1f : 0.3f);

        // Rotation label.
        float rot = entry.getRotation();
        holder.rotationLabel.setVisibility (rot != 0f ? View.VISIBLE : View.GONE);
        holder.rotationLabel.setText (String.valueOf ( (int) rot ) + "°");

        // Selection highlight.
        holder.itemView.setBackgroundColor (
            isSelected
            ? 0x336699FF   // Blue-ish translucent highlight.
            : Color.TRANSPARENT
        );

        // Hidden state — reduce entire row alpha.
        holder.itemView.setAlpha (entry.isVisible() ? 1f : 0.5f);

        // Touch handling.
        holder.itemView.setOnClickListener     (v -> onRowClicked     (entry));
        holder.itemView.setOnLongClickListener (v -> { onRowLongClicked (entry); return true; });
    }

    @Override
    public int getItemCount() {
        return manager.getCount();
    }



    // =========================================================
    // CROP MANAGER CHANGE LISTENER
    // =========================================================

    @Override public void onListChanged()           { notifyDataSetChanged();  }
    @Override public void onEntryChanged (int index) { notifyItemChanged (index); }



    // =========================================================
    // PUBLIC METHODS — SELECTION
    // =========================================================

    public List <CropEntry> getSelected()       { return new ArrayList<> (selected); }
    public boolean          isInSelectionMode() { return inSelectionMode;            }

    // Clears selection and exits selection mode.
    public void clearSelection() {
        selected.clear();
        inSelectionMode = false;
        notifyDataSetChanged();
        if (actionListener != null) actionListener.onSelectionChanged (new ArrayList<>(), false);
    }

    // Selects all entries.
    public void selectAll() {
        selected.clear();
        selected.addAll ( manager.getEntries() );
        inSelectionMode = ! selected.isEmpty();
        notifyDataSetChanged();
        if (actionListener != null) actionListener.onSelectionChanged (getSelected(), inSelectionMode);
    }



    // =========================================================
    // PRIVATE METHODS — INTERACTION
    // =========================================================

    private void onRowClicked (CropEntry entry) {
        if (inSelectionMode) {
            if ( selected.contains (entry) ) selected.remove (entry);
            else                             selected.add    (entry);

            if (selected.isEmpty()) inSelectionMode = false;

            notifyDataSetChanged();
            if (actionListener != null) actionListener.onSelectionChanged (getSelected(), inSelectionMode);

        } else {
            if (actionListener != null) actionListener.onCropTapped (entry);
        }
    }

    private void onRowLongClicked (CropEntry entry) {
        if ( ! inSelectionMode) {
            inSelectionMode = true;
            selected.clear();
        }

        if ( ! selected.contains (entry) ) selected.add (entry);

        notifyDataSetChanged();
        if (actionListener != null) actionListener.onSelectionChanged (getSelected(), inSelectionMode);
    }



    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView colorSwatch;
        final TextView  label;
        final TextView  pinnedIcon;
        final TextView  visibleIcon;
        final TextView  rotationLabel;

        public ViewHolder (@NonNull View row) {
            super (row);
            colorSwatch   = row.findViewWithTag ("colorSwatch");
            label         = row.findViewWithTag ("label");
            pinnedIcon    = row.findViewWithTag ("pinIcon");
            visibleIcon   = row.findViewWithTag ("visIcon");
            rotationLabel = row.findViewWithTag ("rotLabel");
        }
    }



}
