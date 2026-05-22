package com.magd.multicrop.views.multi_crop;



import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;



// =========================================================
// MultiCropTouchHandler
// =========================================================

// Handles ALL touch input for MultiCrop.

// =========================================================
// RESPONSIBILITY:

    // Owns the ScaleGestureDetector (pinch-to-zoom).
    // Owns pan tracking state (lastPanX, lastPanY, isPanning).
    // Translates raw MotionEvents into zoom / pan operations
    // on ZoomPanEngine.
    // Calls back into MultiCrop to rebuild tint + invalidate.

// =========================================================
// TOUCH ROUTING:

    // Two-finger pinch  -> zoom via ScaleGestureDetector.
    // One-finger drag   -> pan via ZoomPanEngine.pan().
    // CropArea touches  -> handled by CropArea directly
    //                      (MultiCrop never sees those — child
    //                       views consume them first).

// =========================================================
// CALLBACK:

    // MultiCropTouchHandler.Callback is implemented by MultiCrop.
    // After every zoom or pan, the handler fires:
        // onZoomOrPan() — MultiCrop rebuilds tint + invalidates.

// =========================================================
// WHY EXTRACTED:

    // Keeps MultiCrop under 300 lines.
    // Touch state variables live here, not scattered in MultiCrop.
    // ScaleGestureDetector setup stays in one place.

// =========================================================

final class MultiCropTouchHandler {



    // =========================================================
    // CALLBACK
    // =========================================================

    // Implemented by MultiCrop.
    // Called after every zoom or pan operation.
    interface Callback {
        void onZoomOrPan();
    }



    // =========================================================
    // VARIABLES
    // =========================================================

    private final MultiCrop              mc;
    private final Callback               callback;
    private final ScaleGestureDetector   scaleDetector;

    // Pan tracking state.
    private float   lastPanX  = 0f;
    private float   lastPanY  = 0f;
    private boolean isPanning = false;



    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    MultiCropTouchHandler (Context context, MultiCrop mc, Callback callback) {
        this.mc       = mc;
        this.callback = callback;

        scaleDetector = new ScaleGestureDetector (
            context,
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale (ScaleGestureDetector detector) {
                    mc.zoomPan.zoom (
                        detector.getScaleFactor(),
                        detector.getFocusX(),
                        detector.getFocusY()
                    );
                    callback.onZoomOrPan();
                    return true;
                }
            }
        );
    }



    // =========================================================
    // PACKAGE-PRIVATE METHODS
    // =========================================================

    // Called from MultiCrop.onTouchEvent().
    // Returns true if the event was consumed.
    boolean onTouchEvent (MotionEvent event) {
        // Always feed to scale detector first — it tracks pointer count.
        scaleDetector.onTouchEvent (event);

        // While pinching: cancel any active pan.
        if ( scaleDetector.isInProgress() ) {
            isPanning = false;
            return true;
        }

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: {
                lastPanX  = event.getX();
                lastPanY  = event.getY();
                isPanning = true;
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                if ( ! isPanning ) return false;

                float dx = event.getX() - lastPanX;
                float dy = event.getY() - lastPanY;
                lastPanX = event.getX();
                lastPanY = event.getY();

                mc.zoomPan.pan (dx, dy);
                callback.onZoomOrPan();
                return true;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                isPanning = false;
                return true;
            }
        }

        return false;
    }



}
