# MultiCrop 🎯

A powerful Android image-cropping library supporting unlimited simultaneous crop regions with freehand drawing, boolean path operations (Union, Difference, Intersect, XOR), full undo/redo history, [...]

---

## 📦 Installation

Add the dependency to your project's `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.Mahmoud-Magd:MultiCrop:RELEASE_VERSION'
}
```

Replace `RELEASE_VERSION` with the latest release version from the [releases page](https://github.com/Mahmoud-Magd/MultiCrop/releases).


Right now the last one is "beta-0.0.0" ..

```gradle
dependencies {
    implementation 'com.github.Mahmoud-Magd:MultiCrop:beta-0.0.0'
}
```



---

## 📐 Class Architecture Map

```
com.magd.multicrop/
│
├── 🎨 ROOT VIEW LAYER
│   └── MultiCrop (FrameLayout)
│       └── The main view. Owns image, tint, zoom/pan engine.
│           Renders image + tint. Delegates touch & crop management.
│
├── 📋 CROP AREA (Interactive Overlay)
│   └── CropArea (View)
│       └── One crop region on-screen. Handles touch (move + resize).
│           Draws border, handles, guidelines. Coordinates via ZoomPanEngine.
│
├── 🎮 MANAGEMENT LAYER
│   ├── CropManager
│   │   └── Central coordinator. Manages list of crops (CropEntry).
│   │       Routes all operations through undo/redo history.
│   │       Public API for crop operations.
│   │
│   ├── CropEntry
│   │   └── Wrapper around CropArea. Tracks label, color, pin state,
│   │       visibility, rotation, hidden state.
│   │
│   └── CropHistoryManager
│       └── Undo / redo engine. Stores and replays commands.
│
├── 📐 REGIONS (Shape Definitions)
│   ├── CropRegion (Abstract)
│   │   └── Base class for all region types.
│   │       Defines contract: buildPath(), getBounds(), copy().
│   │       Stores visibility, bounds (left, top, right, bottom).
│   │
│   ├── RectangleRegion
│   │   └── Axis-aligned rectangle.
│   │
│   ├── OvalRegion
│   │   └── Oval / ellipse.
│   │
│   ├── PolygonRegion
│   │   └── Arbitrary polygon (list of points).
│   │
│   └── FreePathRegion
│       └── Freehand brush / pen path.
│
├── 🔧 GEOMETRY ENGINES
│   ├── ZoomPanEngine
│   │   └── Manages zoom + pan transformations.
│   │       Converts between image space ↔ screen space.
│   │
│   ├── ResizeEngine
│   │   └── Applies handle-based resizing to crop rectangles.
│   │       Validates bounds against image limits.
│   │
│   ├── HandlePositionCalculator
│   │   └── Calculates screen positions of 8 resize handles.
│   │
│   └── PathBuilder
│       └── Combines all regions using their RegionOperations.
│           Produces final composited mask Path.
│
├── 🖼️ RENDERING
│   ├── TintRenderer
│   │   └── Renders tint with holes punched by crop mask.
│   │       Dims uncropped areas.
│   │
│   ├── HandleDrawer
│   │   └── Draws individual resize handles at corners & edges.
│   │
│   ├── GuideLinesDrawer
│   │   └── Draws 3x3 grid guidelines (Rule of Thirds).
│   │
│   └── TintLayer
│       └── Stores tint color, alpha, visibility state.
│
├── 📤 EXPORT
│   └── BitmapExporter
│       └── Exports crops as individual or merged bitmaps.
│           Applies mask, handles MaskMode (transparent/black).
│
├── 🎯 ENUMS
│   ├── ExportMode (INDIVIDUAL | MERGED)
│   │   └── How to structure export output.
│   │
│   ├── MaskMode (TRANSPARENT | BLACK)
│   │   └── How to handle pixels outside crop areas.
│   │
│   ├── FinalCropMode (KEEP_INSIDE | REMOVE_INSIDE)
│   │   └── How the final mask is applied globally.
│   │
│   ├── RegionOperation (UNION | DIFFERENCE | INTERSECT | XOR)
│   │   └── How individual regions combine with the existing mask.
│   │
│   ├── TouchTarget (BODY | TOP_LEFT_HANDLE | ... | NONE)
│   │   └── Identifies what was touched.
│   │
│   ├── HandleType (TOP_LEFT, TOP, TOP_RIGHT, ... CENTER)
│   │   └── The 8 resize handles + center point.
│   │
│   └── GuideLineMode (ON_TOUCH | ALWAYS | HIDDEN)
│       └── When guidelines are visible.
│
├── 📦 MODELS
│   └── CropRect
│       └── Immutable rectangle (left, top, right, bottom).
│           Stores crop bounds in image space.
│
├── 🔐 CENTRAL
│   └── RtVault
│       └── Initialization utility. Caches Android resources.
│
└── 🛠️ UTILITIES
    ├── ColorAssigner
    │   └── Assigns unique colors to new crop areas.
    │
    └── DimensUtils
        └── DP ↔ PX conversions, dimen utilities.

```

---

## 🚀 Quick Start

### 1. Basic Setup

```java
// In your Activity/Fragment layout XML
<com.magd.multicrop.views.multi_crop.MultiCrop
    android:id="@+id/multiCrop"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

// In your Activity
MultiCrop multiCrop = findViewById(R.id.multiCrop);
multiCrop.setImageBitmap(bitmap);
```

### 2. Create Crop Regions

```java
// Create a rectangle crop (centered, auto-positioned)
CropArea rectCrop = multiCrop.addRectCrop();

// Create an oval crop
CropArea ovalCrop = multiCrop.addOvalCrop();

// Create a polygon crop (5-point star, for example)
CropArea polygonCrop = multiCrop.addPolygonCrop(5);
```

### 3. Manage with CropManager (Recommended)

```java
// Create the manager
CropManager cropManager = new CropManager(multiCrop);

// Set listener for UI updates
cropManager.setChangeListener(new CropManager.ChangeListener() {
    @Override
    public void onListChanged() {
        // Full refresh: crop added/removed/reordered
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEntryChanged(int index) {
        // Single item changed: label/pin/color/rotation
        adapter.notifyItemChanged(index);
    }
});

// Add crops (creates CropEntry wrappers)
CropEntry entry1 = cropManager.addRectCrop();      // "Crop 1"
CropEntry entry2 = cropManager.addOvalCrop();      // "Crop 2"
CropEntry entry3 = cropManager.addPolygonCrop(6);  // "Crop 3"
```

### 4. Export Results

```java
// Export all crops as individual bitmaps
List<Bitmap> individual = multiCrop.export(ExportMode.INDIVIDUAL, MaskMode.TRANSPARENT);

// Export all crops merged into one bitmap
List<Bitmap> merged = multiCrop.export(ExportMode.MERGED, MaskMode.BLACK);
```

---

## 📖 Public API Reference

### MultiCrop (Root View)

#### Image Management

| Method | Description |
|--------|-------------|
| `setImageBitmap(Bitmap bitmap)` | Sets the source image. Clears all existing crops. |
| `setImageFromPath(String path)` | Sets image from file path (convenience). |
| `getSourceBitmap()` | Returns the current source bitmap. |

#### Crop Area Creation

| Method | Description |
|--------|-------------|
| `addRectCrop()` | Creates a new rectangular crop area. Returns `CropArea`. |
| `addOvalCrop()` | Creates a new oval crop area. Returns `CropArea`. |
| `addPolygonCrop(int numPoints)` | Creates a polygon with N vertices. Returns `CropArea`. |
| `addFreeDrawingCrop(FreePathRegion region)` | Adds a freehand drawn region. |
| `addCropArea(CropRegion, CropRect, RegionOperation, int color)` | Advanced: adds crop with custom region & operation. |

#### Crop Area Management

| Method | Description |
|--------|-------------|
| `removeCropArea(CropArea cropArea)` | Removes a crop area. |
| `clearCropAreas()` | Removes all crop areas. |
| `getCropAreas()` | Returns list of all `CropArea` objects. |

#### Tint Control

| Method | Description |
|--------|-------------|
| `setTintColor(int color)` | Sets the color of the dimmed (uncropped) area. |
| `setTintAlpha(float alpha)` | Sets tint opacity (0.0 = transparent, 1.0 = opaque). |
| `setTintVisible(boolean visible)` | Shows/hides the tint overlay. |

#### Final Crop Mode

| Method | Description |
|--------|-------------|
| `setFinalCropMode(FinalCropMode mode)` | Sets global mask behavior (`KEEP_INSIDE` or `REMOVE_INSIDE`). |
| `getFinalCropMode()` | Returns current final crop mode. |

#### Zoom & Pan

| Method | Description |
|--------|-------------|
| `resetZoomAndPan()` | Resets zoom to 1.0 and pan to (0,0). |
| `resetZoom()` | Resets only zoom level. |
| `resetPan()` | Resets only pan offset. |

#### Export

| Method | Description |
|--------|-------------|
| `export(ExportMode exportMode, MaskMode maskMode)` | Exports crop results as bitmaps. |

#### Coordinate Conversion

| Method | Description |
|--------|-------------|
| `imagePathToScreen(Path imagePath, Path dst)` | Converts a path from image space to screen space. Used for custom rendering. |

#### Notifications

| Method | Description |
|--------|-------------|
| `notifyRegionChanged()` | Signals that a region's geometry, visibility, or rotation changed outside normal touch flow. |

---

### CropManager (Operation Coordinator)

#### Setup

| Method | Description |
|--------|-------------|
| `setChangeListener(ChangeListener listener)` | Sets UI update callback. |

#### Getters

| Method | Description |
|--------|-------------|
| `getMultiCrop()` | Returns the underlying `MultiCrop` view. |
| `getHistory()` | Returns the `CropHistoryManager` (for undo/redo). |
| `getEntries()` | Returns immutable list of all `CropEntry` objects. |
| `getEntry(int index)` | Returns `CropEntry` at index. |
| `getCount()` | Returns number of crops. |

#### Add Crops

| Method | Description |
|--------|-------------|
| `addRectCrop()` | Creates rect crop, returns `CropEntry` wrapper. |
| `addOvalCrop()` | Creates oval crop, returns `CropEntry` wrapper. |
| `addPolygonCrop(int numOfPoints)` | Creates polygon crop, returns `CropEntry` wrapper. |
| `addFreeDrawingCrop(FreePathRegion region)` | Adds freehand crop from session. |

#### Batch Operations (Lock-Safe)

| Method | Description | Pin-Safe |
|--------|-------------|----------|
| `pin(List<CropEntry> targets)` | Locks crops (blocks delete, rotate, boolOp). | ✅ |
| `unpin(List<CropEntry> targets)` | Unlocks crops. | ✅ |
| `delete(List<CropEntry> targets)` | Deletes crops. | ❌ Unpinned only |
| `show(List<CropEntry> targets)` | Makes crops visible in tint + export. | ✅ |
| `hide(List<CropEntry> targets)` | Hides crops in tint + export. | ✅ |
| `showCropArea(List<CropEntry> targets)` | Shows border + handles. | ✅ |
| `hideCropArea(List<CropEntry> targets)` | Hides border + handles (region still active). | ✅ |
| `rotate(List<CropEntry> targets, float angleDegrees)` | Rotates crops. | ❌ Unpinned only |
| `copy(List<CropEntry> targets)` | Duplicates crops with new colors. | ✅ |
| `rename(CropEntry target, String newLabel)` | Renames single crop. | ✅ |

#### Boolean Operations (Unpinned Only)

| Method | Description |
|--------|-------------|
| `booleanOp(CropEntry A, CropEntry B, RegionOperation op)` | Combines two crops (UNION, DIFFERENCE, INTERSECT, XOR). Result replaces both. |

#### Single-Entry Advanced Operations

| Method | Description |
|--------|-------------|
| `resize(CropEntry target, float widthDp, float heightDp)` | Sets exact width/height. Pass ≤0 to skip dimension. |
| `reshape(CropEntry target, CropRegion newRegion)` | Changes region type, preserves metadata (label, color, rotation, etc.). |

---

### CropArea (Interactive View)

#### State Getters

| Method | Description |
|--------|-------------|
| `getCropRect()` | Returns the crop bounds (image space). |
| `getRegion()` | Returns the underlying `CropRegion`. |
| `getColor()` | Returns the crop color (int). |
| `isActive()` | Returns true if currently being edited. |
| `isPinned()` | Returns true if locked (touch rejected). |
| `isCropAreaVisible()` | Returns true if border + handles are visible. |

#### State Setters

| Method | Description |
|--------|-------------|
| `setActive(boolean active)` | Sets active/inactive state. |
| `setPinned(boolean pinned)` | Locks/unlocks the crop (blocks all touch). |
| `setCropAreaVisible(boolean visible)` | Shows/hides border + handles (alpha = 0). |
| `setZoomPan(ZoomPanEngine zoomPan)` | Updates zoom/pan engine reference. |

#### Callbacks

| Method | Description |
|--------|-------------|
| `setChangeCallback(ChangeCallback cb)` | Called when crop is moved/resized. |
| `setBringToFrontCallback(BringToFrontCallback cb)` | Called when crop is touched (for Z-ordering). |

---

### CropRegion (Shape Base Class)

#### Abstract Methods (Implemented by subclasses)

| Method | Description |
|--------|-------------|
| `buildPath()` | Constructs and returns the region's `Path` (image space). |
| `getBounds()` | Returns axis-aligned bounding box (`RectF`). |
| `copy()` | Returns deep copy for undo/redo snapshots. |

#### Bounds Access

| Method | Description |
|--------|-------------|
| `getLeft()`, `getTop()`, `getRight()`, `getBottom()` | Get individual edges. |
| `setEdges(float left, top, right, bottom)` | Set all bounds at once. |
| `setEdges(RectF rect)` | Set bounds from `RectF`. |

#### Geometry Helpers

| Method | Description |
|--------|-------------|
| `width()`, `height()` | Dimensions. |
| `centerX()`, `centerY()` | Center point. |

#### Visibility

| Method | Description |
|--------|-------------|
| `isVisible()` | Returns visibility state. |
| `setVisible(boolean v)` | Sets visibility (affects tint + export). |

#### Shape Analysis

| Method | Description |
|--------|-------------|
| `isRegular()` | Returns true if polygon is nearly regular (≥95% score). |
| `isSymmetrical()` | Returns true if shape is nearly symmetric (≥95% score). |
| `regularityScore()` | Float 0.0-1.0: how regular the polygon is. |
| `symmetryScore()` | Float 0.0-1.0: how symmetric the shape is. |
| `contains(float imageX, float imageY)` | Hit test: returns true if point is inside. |

#### Polygon Points (Polygons only)

| Method | Description |
|--------|-------------|
| `getPoints()` | Returns list of vertices (`PointF`). |

---

### CropHistoryManager (Undo/Redo)

| Method | Description |
|--------|-------------|
| `undo()` | Reverts the last operation. |
| `redo()` | Re-applies the last undone operation. |
| `canUndo()` | Returns true if undo is available. |
| `canRedo()` | Returns true if redo is available. |
| `clear()` | Clears all history. |

---

## 🎨 Enums Explained

### ExportMode

Controls **how export results are structured**:

- **`INDIVIDUAL`**: Each crop becomes its own `Bitmap`. Useful for OCR, document scanning, batch processing.
- **`MERGED`**: All crops rendered into ONE `Bitmap`. Useful for collages, smart scans, multi-selection masks.

### MaskMode

Controls **how pixels OUTSIDE crop areas are handled** during export:

- **`TRANSPARENT`**: Outside pixels become transparent (requires `ARGB_8888`).
- **`BLACK`**: Outside pixels become black.

### FinalCropMode

Controls **how the final combined mask is applied globally**:

- **`KEEP_INSIDE`**: Keep pixels inside the mask, hide everything outside (standard crop). Used with `UNION` operations.
- **`REMOVE_INSIDE`**: Remove pixels inside the mask, keep everything outside (eraser/background removal). Used with `DIFFERENCE` operations.

### RegionOperation

Controls **how individual regions combine** as they're added to the mask:

- **`UNION`**: Add region to mask (combine areas).
- **`DIFFERENCE`**: Subtract region from mask (create holes).
- **`INTERSECT`**: Keep only overlap with existing mask.
- **`XOR`**: Toggle regions (symmetric difference).

### TouchTarget

Identifies **what the user touched**:

- **`BODY`**: Inside the crop area.
- **`TOP_LEFT_HANDLE`, `TOP_HANDLE`, `TOP_RIGHT_HANDLE`, etc.**: The 8 resize handles.
- **`NONE`**: Nothing touched.

### GuideLineMode

Controls **when the Rule-of-Thirds grid is visible**:

- **`ON_TOUCH`**: Only during interaction.
- **`ALWAYS`**: Always visible.
- **`HIDDEN`**: Never visible.

---

## 📐 Coordinate Spaces

The library uses **two coordinate systems**:

### Image Space
- Origin: **(0, 0)** = top-left of source bitmap
- Bounds: **(imageWidth, imageHeight)** = bottom-right
- **Source of truth** for all crop data
- Used internally by regions, `CropRect`, bounds calculations

### Screen Space
- Origin: **(0, 0)** = top-left of `MultiCrop` view
- Bounds: **(viewWidth, viewHeight)** = bottom-right
- Used for **rendering and touch input**
- Converted via `ZoomPanEngine`

**Conversion Methods:**
- `zoomPan.imageToScreen(imageX, imageY)` → screen point
- `zoomPan.screenToImage(screenX, screenY)` → image point
- `multiCrop.imagePathToScreen(imagePath, dstPath)` → allocation-free path transform

---

## 🔄 Coordinate Flow Diagram

```
User touches screen
        ↓
CropArea receives MotionEvent (screen space)
        ↓
Converts to image space via ZoomPanEngine
        ↓
Updates CropRect (image space) + Region
        ↓
Fires changed callback
        ↓
MultiCrop rebuilds tint mask + redraws
        ↓
PathBuilder converts regions to image-space path
        ↓
TintRenderer converts path to screen space + renders
```

---

## 🎬 Typical Usage Flow

### 1. Initialize

```java
MultiCrop multiCrop = findViewById(R.id.multiCrop);
CropManager cropManager = new CropManager(multiCrop);

// Set image
Bitmap sourceBitmap = BitmapFactory.decodeFile(imagePath);
multiCrop.setImageBitmap(sourceBitmap);

// Listen for changes
cropManager.setChangeListener(new CropManager.ChangeListener() {
    @Override
    public void onListChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEntryChanged(int index) {
        adapter.notifyItemChanged(index);
    }
});
```

### 2. User Creates Crops (via UI buttons)

```java
// User taps "Add Rectangle"
CropEntry entry = cropManager.addRectCrop();  // "Crop 1"

// CropArea appears on screen, user can drag/resize
// onListChanged() fires → adapter updates → list shows "Crop 1"
```

### 3. User Modifies Crops (via CropArea touch or adapter)

```java
// Via adapter item click
cropManager.rename(entry, "Face");

// Via batch selection
List<CropEntry> selected = /* ... */;
cropManager.pin(selected);        // Lock selected crops
cropManager.rotate(selected, 45f); // Rotate all by 45°
```

### 4. User Performs Boolean Operations

```java
CropEntry faceEntry = /* ... */;
CropEntry backgroundEntry = /* ... */;

// Remove background from face
cropManager.booleanOp(
    faceEntry,
    backgroundEntry,
    RegionOperation.DIFFERENCE
);
// Result replaces both entries
```

### 5. Export Results

```java
// Get individual crops
List<Bitmap> crops = multiCrop.export(
    ExportMode.INDIVIDUAL,
    MaskMode.TRANSPARENT
);

for (int i = 0; i < crops.size(); i++) {
    Bitmap crop = crops.get(i);
    CropEntry entry = cropManager.getEntry(i);
    // Save crop with entry.getLabel()
    saveBitmap(crop, entry.getLabel());
}
```

---

## 🏗️ Key Classes Explained

### **MultiCrop**
- **Purpose**: Root view. Owns the image, zoom/pan engine, and tint layer.
- **Responsibility**: Draw image + tint, delegate touch handling, manage crop area lifecycle.
- **Touch Flow**: Routes to `MultiCropTouchHandler` (zoom/pan) or child `CropArea` views.
- **Rendering**: Layer 0 (image), Layer 1 (tint with holes), Layer 2 (child CropAreas).

### **CropArea**
- **Purpose**: Single interactive crop region on-screen.
- **Responsibility**: Draw border + handles + guidelines, handle touch (move + resize).
- **State**: Tracks `CropRect` (bounds), `CropRegion` (shape), pin state, visibility.
- **Touch Priority**: Pinned → reject all. Handle touched → resize. Inside region → move. Outside → ignore.

### **CropManager**
- **Purpose**: Centralized crop list coordinator. Bridges UI (adapter) and `MultiCrop`.
- **Responsibility**: Manage `CropEntry` list, route all operations through undo/redo, notify adapter.
- **Operations**: Add, delete, pin, rotate, copy, boolean ops, resize, reshape.
- **History**: Every operation (except getters) is undoable.

### **CropEntry**
- **Purpose**: Wrapper around `CropArea`. Tracks metadata.
- **Stores**: Label, color, rotation, visibility, pin state, cropAreaHidden state.
- **Used by**: `CropManager` for operations, RecyclerView adapter for list display.

### **CropHistoryManager**
- **Purpose**: Undo/redo engine.
- **Responsibility**: Execute commands, store snapshots, support undo/redo.
- **Usage**: Every `CropManager` operation goes through history (except reads).

### **CropRegion** (Abstract)
- **Purpose**: Base class for all shape types.
- **Subclasses**: `RectangleRegion`, `OvalRegion`, `PolygonRegion`, `FreePathRegion`.
- **Contract**: Must implement `buildPath()`, `getBounds()`, `copy()`.
- **Usage**: Regions are combined by `PathBuilder` using their `RegionOperation`.

### **ZoomPanEngine**
- **Purpose**: Transform engine for zoom/pan.
- **Responsibility**: Convert between image ↔ screen space, apply matrix transformations.
- **Usage**: Touch handlers and renderers use this to sync coordinates.

### **PathBuilder**
- **Purpose**: Combines all regions into one final mask path.
- **Algorithm**: Iterates regions in order, applies each region's `RegionOperation` to the running path.
- **Output**: Single `Path` (image space) representing the final mask shape.

### **TintRenderer**
- **Purpose**: Renders tint (dim layer) with holes punched by mask.
- **Algorithm**: Draws tint color with mask path as cutout.
- **Effect**: Visually shows which areas will be exported.

### **BitmapExporter**
- **Purpose**: Exports crops as `Bitmap` objects.
- **Modes**: `INDIVIDUAL` (one bitmap per crop) or `MERGED` (all in one).
- **Masking**: Applies mask using `FinalCropMode` and `MaskMode`.

---

## ⚙️ Advanced: Custom Region Types

To create a custom region (e.g., AI mask, star shape):

```java
public class StarRegion extends CropRegion {
    private int numPoints;

    public StarRegion(float left, float top, float right, float bottom, int numPoints) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.numPoints = numPoints;
    }

    @Override
    public Path buildPath() {
        // Build star path using left, top, right, bottom, numPoints
        Path path = new Path();
        // ... star geometry ...
        return path;
    }

    @Override
    public RectF getBounds() {
        return new RectF(left, top, right, bottom);
    }

    @Override
    public CropRegion copy() {
        return new StarRegion(left, top, right, bottom, numPoints);
    }
}

// Use it
StarRegion star = new StarRegion(100, 100, 300, 300, 5);
CropArea area = multiCrop.addCropArea(
    star,
    new CropRect(100, 100, 300, 300),
    RegionOperation.UNION,
    Color.BLUE
);
```

---

## 🎯 Performance Tips

1. **Batch Operations**: Use list-based methods (`pin()`, `delete()`, `rotate()`) instead of multiple single calls.
2. **Large Bitmaps**: Scale source bitmap before setting (≤2048px recommended).
3. **Many Regions**: Limit simultaneous crops to <50 for smooth interaction.
4. **Export**: Call on background thread—bitmap operations can block.
5. **Undo History**: Clear periodically with `cropManager.getHistory().clear()` to free memory.

---

## 📋 Feature Checklist

- ✅ Multiple simultaneous crop regions
- ✅ Rectangle, oval, polygon, freehand shapes
- ✅ Boolean operations (Union, Difference, Intersect, XOR)
- ✅ Full undo/redo history
- ✅ Zoom and pan
- ✅ Touch-driven move and resize
- ✅ Tint overlay with crop preview
- ✅ Pin/lock crops
- ✅ Batch operations (pin, delete, rotate, copy)
- ✅ Flexible export (individual or merged)
- ✅ Coordinate space conversion
- ✅ Shape analysis (regularity, symmetry)

---

## 📄 License

[Specify your license here]

---

## 🤝 Contributing

Contributions welcome! Please open issues for bugs or feature requests.

---

**Built with ❤️ for powerful image cropping on Android.**
