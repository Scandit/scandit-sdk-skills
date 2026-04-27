# MatrixScan Count Cordova Integration Guide

## Source note

There is no public Cordova MatrixScan Count sample. This guide is anchored to the internal DebugApp (`frameworks/cordova/debugapp/src/pages/BarcodeCount.tsx`, `useBarcodeCount.ts`), the Cordova plugin source (`frameworks/cordova/scandit-cordova-datacapture-barcode/www/ts/src/BarcodeCountView.ts`), the shared JS framework source (`frameworks/shared/jsmobile/scandit-datacapture-frameworks-barcode/src/barcodecount/`), and the RST API docs (`docs/source/barcode-capture/api/`). All API shown here is verified against those sources.

## Integration flow

Before writing any code, align with the user:

1. **Which symbologies do they need to scan?** Retail typically uses EAN-13/UPC-A, EAN-8, UPC-E, Code 128, Code 39, ITF. Logistics often adds Data Matrix, QR, PDF417. Only enable what the user asks for — each extra symbology costs processing time.
2. **Are they scanning against a known list (expected barcodes)?** If yes, a `BarcodeCountCaptureList` with `TargetBarcode` items is needed. If not, the list is optional.
3. **Which file should BarcodeCount be wired into?** If the user hasn't told you, ask for the path of the JS/TS file that owns the scanning screen.
4. **Write the code directly into that file.** Do not dump a giant snippet and tell the user to copy/paste — open the file with the edit tools and make the changes in place. Preserve existing code alongside the new BarcodeCount integration.
5. **After the code is in place, show a setup checklist** (packages, camera permissions, CSP, iOS/Android prerequisites) so the user can verify the runtime prerequisites.

MatrixScan Count (BarcodeCount) scans multiple barcodes simultaneously and renders a pre-built UI with count tracking, progress bar, toolbar, and scan completion animations. The view is a transparent native overlay positioned by mirroring an HTML element.

## Prerequisites

- **Cordova plugins installed**:
  - `scandit-cordova-datacapture-core`
  - `scandit-cordova-datacapture-barcode`
- Install with:
  ```bash
  cordova plugin add scandit-cordova-datacapture-core
  cordova plugin add scandit-cordova-datacapture-barcode
  ```
- **Minimum plugin version**: 6.24 for BarcodeCount on Cordova. Use 7.6+ for the context-free constructor. Use 8.3+ for Status mode, Mapping flow, and several new hint texts.
- After any plugin change: `cordova prepare` (or re-add the platform) so the native side is re-synced.
- A valid **Scandit license key** (get one at [scandit.com](https://www.scandit.com)).
- **Camera permissions** are configured automatically by the plugins:
  - iOS: `NSCameraUsageDescription` is added to `Info.plist` via `plugin.xml`.
  - Android: `CAMERA` and `VIBRATE` permissions are added to `AndroidManifest.xml`.
- **iOS deployment target**: 15.0 or higher (`<preference name="deployment-target" value="15.0"/>` in `config.xml`).
- **Android minSdkVersion**: 24 or higher.
- **Swift support**: `cordova-plugin-add-swift-support` must be installed for iOS builds.
- **Web platform NOT supported**.

## Step 1 — Load the SDK and wait for `deviceready`

The Scandit SDK is exposed on the global `window.Scandit` object. Both plugins auto-register at app startup. You **must** wait for the `deviceready` event before using any Scandit API.

```javascript
document.addEventListener('deviceready', () => {
  // Safe to call Scandit APIs here
  initializeSDK();
}, false);
```

If the project is TypeScript, declare the global type in a `global.d.ts`:

```typescript
import type * as ScanditCore from 'scandit-cordova-datacapture-core';
import type * as ScanditBarcode from 'scandit-cordova-datacapture-barcode';

declare global {
  const Scandit: typeof ScanditCore & typeof ScanditBarcode;
}
```

Reference it from your TS file with `/// <reference path="./global.d.ts" />`.

> **Do not** import from `scandit-cordova-datacapture-*` at runtime in a plain-Cordova project — those are plugin manifests, not ES modules. Use `Scandit.X` at runtime.

## Step 2 — Create the DataCaptureContext

```javascript
const context = Scandit.DataCaptureContext.initialize('-- ENTER YOUR SCANDIT LICENSE KEY HERE --');
```

`DataCaptureContext.initialize(key)` is the v8 entry point. Call this exactly once, after `deviceready`.

## Step 3 — Set up the camera

```javascript
const cameraSettings = Scandit.BarcodeCount.createRecommendedCameraSettings(); // ≥7.6
const camera = Scandit.Camera.withSettings(cameraSettings);
await context.setFrameSource(camera);
```

Start and stop the camera explicitly:

```javascript
// Start:
await camera.switchToDesiredState(Scandit.FrameSourceState.On);

// Stop (teardown):
await camera.switchToDesiredState(Scandit.FrameSourceState.Off);
```

## Step 4 — Configure BarcodeCountSettings

```javascript
const settings = new Scandit.BarcodeCountSettings();
settings.enableSymbologies([
  Scandit.Symbology.EAN13UPCA,
  Scandit.Symbology.EAN8,
  Scandit.Symbology.UPCE,
  Scandit.Symbology.Code39,
  Scandit.Symbology.Code128,
  Scandit.Symbology.DataMatrix,
  Scandit.Symbology.QR,
]);
```

### BarcodeCountSettings properties and methods

| Member | Type | Description |
|--------|------|-------------|
| `enableSymbologies(symbologies)` | method | Enable multiple symbologies at once. |
| `enableSymbology(symbology, enabled)` | method | Enable or disable a single symbology. |
| `settingsForSymbology(symbology)` | method | Get per-symbology `SymbologySettings`. |
| `enabledSymbologies` | `Symbology[]` | Currently enabled symbologies (read-only). |
| `expectsOnlyUniqueBarcodes` | `boolean` | Optimize for unique barcodes only. Do not enable if scanning multiple identical barcodes. |
| `disableModeWhenCaptureListCompleted` | `boolean` | Auto-disable mode when capture list is completed (≥8.3). Default `false`. |
| `mappingEnabled` | `boolean` | Enable barcode mapping (beta). Default `false`. |
| `clusteringMode` | `ClusteringMode` | Smart grouping of neighbouring barcodes (≥8.3). Default `Disabled`. |
| `setProperty(name, value)` / `getProperty(name)` | method | Advanced unstable properties. |

## Step 5 — Create the BarcodeCount mode (≥7.6 constructor)

```javascript
const barcodeCount = new Scandit.BarcodeCount(settings);
context.addMode(barcodeCount);
barcodeCount.isEnabled = true;
```

The ≥7.6 constructor does NOT automatically wire the mode to the context — you must call `context.addMode(barcodeCount)` explicitly.

### BarcodeCount methods

| Method | Description |
|--------|-------------|
| `addListener(listener)` | Add a `BarcodeCountListener`. |
| `removeListener(listener)` | Remove a listener. |
| `reset()` | Clear all tracked barcodes. Returns `Promise<void>`. |
| `startScanningPhase()` | Programmatically trigger the scan. |
| `endScanningPhase()` | Stop scanning and switch off the frame source. |
| `setBarcodeCountCaptureList(captureList)` | Attach a capture list for list-scanning workflows. |
| `setAdditionalBarcodes(barcodes)` | Inject barcodes from previous sessions. Returns `Promise<void>`. |
| `clearAdditionalBarcodes()` | Clear injected barcodes. Returns `Promise<void>`. |
| `applySettings(settings)` | Asynchronously apply new settings. Returns `Promise<void>`. |
| `static createRecommendedCameraSettings()` | Returns recommended camera settings (≥7.6). |

### BarcodeCount properties

| Property | Type | Description |
|----------|------|-------------|
| `isEnabled` | `boolean` | Enable or disable the mode. |
| `feedback` | `BarcodeCountFeedback` | Sound/vibration feedback on scan events. |
| `context` | `DataCaptureContext \| null` | The bound context (read-only). |

## Step 6 — Add a BarcodeCountListener (optional)

```javascript
barcodeCount.addListener({
  didScan: async (barcodeCount, session, getFrameData) => {
    const recognized = session.recognizedBarcodes; // available ≥7.0
    if (recognized) {
      Object.values(recognized).forEach(barcode => {
        console.log('Recognized:', barcode.data, barcode.symbology);
      });
    }
  },
  // ≥8.3 only:
  onSessionUpdated: async (barcodeCount, session, getFrameData) => {
    // Called every frame (not just on scan)
  },
});
```

### BarcodeCountListener callbacks

| Callback | When called |
|----------|-------------|
| `didScan(barcodeCount, session, getFrameData)` | Once per scanning phase completion. |
| `onSessionUpdated(barcodeCount, session, getFrameData)` | Every frame (≥8.3). |

### BarcodeCountSession properties

| Property | Type | Description |
|----------|------|-------------|
| `recognizedBarcodes` | `{ [id: string]: Barcode } \| Barcode[]` | All currently recognized barcodes (≥7.0). |
| `additionalBarcodes` | `Barcode[]` | Barcodes injected via `setAdditionalBarcodes`. |
| `frameSequenceID` | `number` | Current frame sequence identifier. |
| `reset()` | `Promise<void>` | Reset session state (call only inside listener callbacks). |

## Step 7 — Create and mount BarcodeCountView

`BarcodeCountView` is the pre-built scanning UI. It must be attached to an HTML element. The native view mirrors the element's size and position.

### Step 7a — Add the container element to your HTML

```html
<div id="barcode-count-view" class="prebuilt-view-container"></div>
```

The element should fill the space where the camera feed will appear.

### Step 7b — Construct BarcodeCountView

Two factory methods are available on Cordova:

```javascript
// Without explicit style (defaults to Dot):
const view = Scandit.BarcodeCountView.forContextWithMode(context, barcodeCount);

// With explicit style:
const view = Scandit.BarcodeCountView.forContextWithModeAndStyle(
  context,
  barcodeCount,
  Scandit.BarcodeCountViewStyle.Icon // or .Dot
);

// With mapping flow (≥8.3):
const view = Scandit.BarcodeCountView.forMapping(
  context,
  barcodeCount,
  Scandit.BarcodeCountViewStyle.Icon,
  mappingFlowSettings
);
```

### Step 7c — Attach the view to the DOM element

```javascript
const containerEl = document.getElementById('barcode-count-view');
view.connectToElement(containerEl);
```

`connectToElement` is synchronous (`void` return) — no `await` needed. The native view starts tracking the element's bounding rect immediately. The element must be in the DOM and visible before calling this.

> **Important**: Do not use `setFrame` and `connectToElement` together. Choose one positioning strategy.

### Detach on teardown

```javascript
view.detachFromElement(); // synchronous void
```

Call this when leaving the scanning screen. It releases the native view resources.

## Step 8 — BarcodeCountView Customization

`BarcodeCountView` exposes a large set of properties. All properties shown here are verified against the Cordova plugin source (`BarcodeCountView.ts`) and the RST API docs.

### View style

| Enum value | Description |
|-----------|-------------|
| `Scandit.BarcodeCountViewStyle.Icon` | Highlights drawn as icons with entrance animation. |
| `Scandit.BarcodeCountViewStyle.Dot` | Highlights drawn as dots with entrance animation. |

The style is set at construction time and is read-only afterward (`view.style`).

### Visibility booleans

| Property | Default | Description |
|----------|---------|-------------|
| `view.shouldShowUserGuidanceView` | `true` | Show user guidance and loading view. |
| `view.shouldShowListProgressBar` | `true` | Show progress bar when a capture list is active (≥Cordova 6.25). |
| `view.shouldShowListButton` | `true` | Show the list button (lower-left). Triggers `didTapListButton`. |
| `view.shouldShowExitButton` | `true` | Show the exit button (lower-right). Triggers `didTapExitButton`. |
| `view.shouldShowShutterButton` | `true` | Show the shutter button (bottom center). |
| `view.shouldShowHints` | `true` | Show scanning hint messages. |
| `view.shouldShowClearHighlightsButton` | `false` | Show a "clear highlights" button above the shutter. |
| `view.shouldShowSingleScanButton` | `false` | Show a single-scan button (lower-left). Triggers `didTapSingleScanButton`. |
| `view.shouldShowStatusModeButton` | `false` | Show the status mode toggle button (≥8.3). Requires a `BarcodeCountStatusProvider`. |
| `view.shouldShowFloatingShutterButton` | `false` | Show a draggable floating shutter button. |
| `view.shouldShowToolbar` | `true` | Show the collapsible toolbar at the top. |
| `view.shouldShowScanAreaGuides` | `false` | Visualize the scan area (debug use only). |
| `view.shouldShowTorchControl` | `false` | Show a torch toggle button (≥Cordova 6.26). |
| `view.shouldShowStatusIconsOnScan` | `false` | Show status icons on scan immediately (≥8.3). When `true`, `shouldShowStatusModeButton` has no effect. |
| `view.shouldDisableModeOnExitButtonTapped` | `true` | Auto-disable mode when exit button is tapped (≥Cordova 7.0). |
| `view.tapToUncountEnabled` | `false` | Allow tapping a highlight to deselect/uncount a barcode (≥Cordova 7.0). |
| `view.hardwareTriggerEnabled` | — | Enable hardware trigger support. On Android (≥7.1), reacts to hardware button. On iOS, reacts to volume button. Use `view.enableHardwareTrigger(keyCode)` method instead for full control. |

Example — hide toolbar and exit button:
```javascript
view.shouldShowToolbar = false;
view.shouldShowExitButton = false;
```

### Torch control position

```javascript
view.shouldShowTorchControl = true;
view.torchControlPosition = Scandit.Anchor.TopLeft; // TopLeft, TopRight, BottomLeft, BottomRight
```

### Brushes (Dot style only)

Global brushes apply to all barcodes when no listener provides per-barcode brushes.

| Property | Default accessor | Description |
|----------|-----------------|-------------|
| `view.recognizedBrush` | `Scandit.BarcodeCountView.defaultRecognizedBrush` | Brush for recognized (in-list) barcodes. |
| `view.notInListBrush` | `Scandit.BarcodeCountView.defaultNotInListBrush` | Brush for recognized barcodes not in the capture list. |
| `view.acceptedBrush` | `Scandit.BarcodeCountView.defaultAcceptedBrush` | Brush for accepted barcodes (≥Cordova 7.1). |
| `view.rejectedBrush` | `Scandit.BarcodeCountView.defaultRejectedBrush` | Brush for rejected barcodes (≥Cordova 7.1). |

```javascript
view.recognizedBrush = new Scandit.Brush(
  Scandit.Color.fromHex('#00FF0066'), // fill
  Scandit.Color.fromHex('#00FF00'),   // stroke
  2.0                                 // stroke width
);
view.notInListBrush = new Scandit.Brush(
  Scandit.Color.fromHex('#FF000066'),
  Scandit.Color.fromHex('#FF0000'),
  2.0
);
```

Set a brush to `null` to hide all barcodes of that category.

#### Per-barcode brush overrides

These are methods (not properties), available from Cordova 7.1, for overriding the brush of a specific `TrackedBarcode`:

```javascript
// Returns Promise<void>
view.setBrushForRecognizedBarcode(trackedBarcode, new Scandit.Brush(...));
view.setBrushForRecognizedBarcodeNotInList(trackedBarcode, new Scandit.Brush(...));
view.setBrushForAcceptedBarcode(trackedBarcode, new Scandit.Brush(...)); // ≥7.1
view.setBrushForRejectedBarcode(trackedBarcode, new Scandit.Brush(...)); // ≥7.1
```

### Customizable text

```javascript
view.exitButtonText = 'Close';
view.clearHighlightsButtonText = 'Clear All';
```

### Hint text localization

All hint text properties are available on Cordova and accept arbitrary strings:

```javascript
view.textForTapShutterToScanHint      = 'Tap to scan';
view.textForScanningHint              = 'Scanning…';
view.textForMoveCloserAndRescanHint   = 'Move closer and scan again';
view.textForMoveFurtherAndRescanHint  = 'Move further away and scan again';
view.textForBarcodesNotInListDetectedHint = 'Unknown barcode detected'; // ≥8.3
view.textForScreenCleanedUpHint       = 'Screen cleared';              // ≥8.3
view.textForTapToUncountHint          = 'Tap to remove';               // ≥Cordova 7.0
view.textForClusteringGestureHint     = 'Swipe to group';              // ≥8.3
```

### Accessibility

Accessibility labels and hints are **iOS-only** (no effect on Android). Content descriptions are **Android-only** (no effect on iOS).

```javascript
// iOS: accessibility labels
view.listButtonAccessibilityLabel           = 'View list';
view.exitButtonAccessibilityLabel           = 'Exit scanner';
view.shutterButtonAccessibilityLabel        = 'Scan';
view.floatingShutterButtonAccessibilityLabel = 'Scan (floating)';
view.singleScanButtonAccessibilityLabel     = 'Single scan';
view.clearHighlightsButtonAccessibilityLabel = 'Clear highlights';
view.statusModeButtonAccessibilityLabel     = 'Status mode'; // ≥8.3

// iOS: accessibility hints
view.listButtonAccessibilityHint            = 'Shows scanned items list';
view.exitButtonAccessibilityHint            = 'Closes the scanner';
view.shutterButtonAccessibilityHint         = 'Triggers barcode scan';
view.floatingShutterButtonAccessibilityHint = 'Triggers barcode scan (floating)';
view.singleScanButtonAccessibilityHint      = 'Scans a single barcode';
view.clearHighlightsButtonAccessibilityHint = 'Clears all barcode highlights';
view.statusModeButtonAccessibilityHint      = 'Toggles status mode'; // ≥8.3

// Android: content descriptions
view.listButtonContentDescription           = 'View list';
view.exitButtonContentDescription           = 'Exit scanner';
view.shutterButtonContentDescription        = 'Scan';
view.floatingShutterButtonContentDescription = 'Scan (floating)';
view.singleScanButtonContentDescription     = 'Single scan';
view.clearHighlightsButtonContentDescription = 'Clear highlights';
view.statusModeButtonContentDescription     = 'Status mode'; // ≥8.3
```

### Hardware trigger

```javascript
// Enable with default key (volume-down on Android, volume button on iOS):
await view.enableHardwareTrigger(null);

// Enable with a specific Android keyCode:
await view.enableHardwareTrigger(24); // e.g. KEYCODE_VOLUME_UP

// Or use the property (iOS volume button only, ≥Cordova 7.1):
view.hardwareTriggerEnabled = true;

// Check support (Android API ≥28 required):
if (Scandit.BarcodeCountView.hardwareTriggerSupported) {
  await view.enableHardwareTrigger(null);
}
```

### Not-in-list action settings (≥Cordova 7.1)

When a capture list is active, barcodes not in the list can show an accept/reject popup:

```javascript
const notInListSettings = view.barcodeNotInListActionSettings; // getter returns existing instance
notInListSettings.enabled = true;
notInListSettings.acceptButtonText = 'Accept';
notInListSettings.rejectButtonText = 'Reject';
notInListSettings.cancelButtonText = 'Cancel';
notInListSettings.barcodeAcceptedHint = 'Barcode accepted';
notInListSettings.barcodeRejectedHint = 'Barcode rejected';
view.barcodeNotInListActionSettings = notInListSettings; // assign back
```

### Filter settings

`view.filterSettings` accepts a `BarcodeFilterHighlightSettings` value. On Cordova, `BarcodeFilterHighlightSettings` is an interface; the concrete implementation is `BarcodeFilterHighlightSettingsBrush` (available from Cordova 6.24):

```javascript
// Construct with a Brush (optional — omit to use a transparent/no-stroke brush):
const filterBrush = new Scandit.Brush(
  Scandit.Color.fromHex('#FF000033'), // fill (semi-transparent red)
  Scandit.Color.fromHex('#FF0000'),   // stroke
  2.0
);
const filterSettings = new Scandit.BarcodeFilterHighlightSettingsBrush(filterBrush);
// or: Scandit.BarcodeFilterHighlightSettingsBrush.create(filterBrush)

view.filterSettings = filterSettings;
```

Set `view.filterSettings = null` to remove the filter highlight overlay.

### Toolbar settings

`BarcodeCountToolbarSettings` is a plain class with a no-arg constructor (available from Cordova 6.24; the `constructor()` signature is explicitly documented from Cordova 8.3). Construct, set properties, then pass to `view.setToolbarSettings()`:

```javascript
const toolbarSettings = new Scandit.BarcodeCountToolbarSettings();

// Button labels (both On and Off states):
toolbarSettings.audioOnButtonText = 'Sound On';
toolbarSettings.audioOffButtonText = 'Sound Off';
toolbarSettings.vibrationOnButtonText = 'Vibration On';
toolbarSettings.vibrationOffButtonText = 'Vibration Off';
toolbarSettings.strapModeOnButtonText = 'Strap On';
toolbarSettings.strapModeOffButtonText = 'Strap Off';
toolbarSettings.colorSchemeOnButtonText = 'Color On';
toolbarSettings.colorSchemeOffButtonText = 'Color Off';

// iOS accessibility hints (ignored on Android):
toolbarSettings.audioButtonAccessibilityHint = 'Toggles scan sound';
toolbarSettings.audioButtonAccessibilityLabel = 'Sound';
toolbarSettings.vibrationButtonAccessibilityHint = 'Toggles vibration';
toolbarSettings.vibrationButtonAccessibilityLabel = 'Vibration';
toolbarSettings.strapModeButtonAccessibilityHint = 'Toggles strap mode';
toolbarSettings.strapModeButtonAccessibilityLabel = 'Strap mode';
toolbarSettings.colorSchemeButtonAccessibilityHint = 'Toggles color scheme';
toolbarSettings.colorSchemeButtonAccessibilityLabel = 'Color scheme';

// Android content descriptions (ignored on iOS):
toolbarSettings.audioButtonContentDescription = 'Sound toggle';
toolbarSettings.vibrationButtonContentDescription = 'Vibration toggle';
toolbarSettings.strapModeButtonContentDescription = 'Strap mode toggle';
toolbarSettings.colorSchemeButtonContentDescription = 'Color scheme toggle';

view.setToolbarSettings(toolbarSettings);
```

### Methods summary

| Method | Returns | Description |
|--------|---------|-------------|
| `connectToElement(element)` | `void` | Attach to a DOM element (mirrors its size/position). |
| `detachFromElement()` | `void` | Release the DOM element. Call on teardown. |
| `setFrame(rect, isUnderContent)` | `Promise<void>` | Manually position by `Rect`. Do not use with `connectToElement`. |
| `show()` | `Promise<void>` | Make visible (only with `setFrame`). |
| `hide()` | `Promise<void>` | Make invisible (only with `setFrame`). |
| `clearHighlights()` | `Promise<void>` | Clear all visible highlights (does not affect session data). |
| `setToolbarSettings(settings)` | `void` | Configure toolbar text and accessibility. |
| `setStatusProvider(provider)` | `void` | Set a status provider for status mode (≥8.3). |
| `setBrushForRecognizedBarcode(tb, brush)` | `Promise<void>` | Per-barcode recognized brush override (≥7.1). |
| `setBrushForRecognizedBarcodeNotInList(tb, brush)` | `Promise<void>` | Per-barcode not-in-list brush override (≥7.1). |
| `setBrushForAcceptedBarcode(tb, brush)` | `Promise<void>` | Per-barcode accepted brush override (≥7.1). |
| `setBrushForRejectedBarcode(tb, brush)` | `Promise<void>` | Per-barcode rejected brush override (≥7.1). |
| `enableHardwareTrigger(keyCode)` | `Promise<void>` | Enable hardware trigger (≥7.1). |

## Step 9 — BarcodeCountView Listeners

### BarcodeCountViewListener (brush callbacks and tap callbacks)

Set `view.listener` to an object implementing `IBarcodeCountViewListener`. These are optional — only implement what you need. The brush callbacks run on the **rendering thread**; the tap callbacks run on the **main thread**.

```javascript
view.listener = {
  // Brush callbacks (Dot style only) — return null to hide the barcode
  brushForRecognizedBarcode: (view, trackedBarcode) => {
    return new Scandit.Brush(
      Scandit.Color.fromHex('#00FF0066'),
      Scandit.Color.fromHex('#00FF00'),
      2.0
    );
  },
  brushForRecognizedBarcodeNotInList: (view, trackedBarcode) => {
    return new Scandit.Brush(
      Scandit.Color.fromHex('#FF000066'),
      Scandit.Color.fromHex('#FF0000'),
      2.0
    );
  },
  brushForAcceptedBarcode: (view, trackedBarcode) => { // ≥7.1
    return new Scandit.Brush(
      Scandit.Color.fromHex('#0000FF66'),
      Scandit.Color.fromHex('#0000FF'),
      2.0
    );
  },
  brushForRejectedBarcode: (view, trackedBarcode) => { // ≥7.1
    return new Scandit.Brush(
      Scandit.Color.fromHex('#FF880066'),
      Scandit.Color.fromHex('#FF8800'),
      2.0
    );
  },

  // Tap callbacks
  didTapRecognizedBarcode: (view, trackedBarcode) => {
    console.log('Tapped recognized:', trackedBarcode.barcode.data);
  },
  didTapFilteredBarcode: (view, trackedBarcode) => {
    console.log('Tapped filtered:', trackedBarcode.barcode.data);
  },
  didTapRecognizedBarcodeNotInList: (view, trackedBarcode) => {
    console.log('Tapped not-in-list:', trackedBarcode.barcode.data);
  },
  didTapAcceptedBarcode: (view, trackedBarcode) => { // ≥7.1
    console.log('Tapped accepted:', trackedBarcode.barcode.data);
  },
  didTapRejectedBarcode: (view, trackedBarcode) => { // ≥7.1
    console.log('Tapped rejected:', trackedBarcode.barcode.data);
  },
  didCompleteCaptureList: (view) => {
    console.log('All target barcodes scanned!');
  },
};
```

### BarcodeCountViewUiListener (button tap callbacks)

Set `view.uiListener` to an object implementing `IBarcodeCountViewUiListener`:

```javascript
view.uiListener = {
  didTapListButton: (view) => {
    console.log('List button tapped');
    // Typically: show results screen, freeze mode, navigate
  },
  didTapExitButton: (view) => {
    console.log('Exit button tapped');
    // Typically: navigate back or stop scanning
  },
  didTapSingleScanButton: (view) => {
    console.log('Single scan button tapped');
  },
};
```

## Step 10 — Scanning against a list (BarcodeCountCaptureList)

When you want to verify that specific barcodes have been scanned, attach a `BarcodeCountCaptureList`:

```javascript
const captureList = Scandit.BarcodeCountCaptureList.create(
  {
    didUpdateSession: (captureList, session) => {
      if (session.acceptedBarcodes.length > 0) {
        console.log('Accepted:', session.acceptedBarcodes);
      }
    },
  },
  [
    Scandit.TargetBarcode.create('12345670', 1),  // data, quantity
    Scandit.TargetBarcode.create('98765432', 2),
  ]
);

barcodeCount.setBarcodeCountCaptureList(captureList);
```

When a capture list is active, `view.shouldShowListProgressBar` shows scan progress (default `true`).

## Step 11 — Status mode (BarcodeCountStatusProvider, ≥8.3)

Status mode overlays each barcode with a status indicator provided by a `BarcodeCountStatusProvider`. Enable with:

```javascript
view.shouldShowStatusModeButton = true;

view.setStatusProvider({
  statusForBarcodes: async (barcodes) => {
    // barcodes: TrackedBarcode[]
    // Return an array of BarcodeCountStatusItem (one per barcode you want to annotate).
    // BarcodeCountStatusItem.create(barcode, status) — two parameters only.
    return barcodes.map(trackedBarcode => {
      return Scandit.BarcodeCountStatusItem.create(
        trackedBarcode,
        Scandit.BarcodeCountStatus.Error
      );
    });
  },
});
```

> **Note**: Status mode API is beta and may change. Mark any status-related code with a comment.

## Step 12 — Feedback

```javascript
// Default feedback (sound + vibration):
barcodeCount.feedback = Scandit.BarcodeCountFeedback.default;

// Customize:
const customFeedback = new Scandit.BarcodeCountFeedback();
customFeedback.success = new Scandit.Feedback(
  new Scandit.Vibration(Scandit.VibrationType.Default),
  null // no sound
);
barcodeCount.feedback = customFeedback;

// Disable all feedback:
barcodeCount.feedback = Scandit.BarcodeCountFeedback.emptyFeedback; // ≥7.1
```

### BarcodeCountFeedback properties

| Property | Description |
|----------|-------------|
| `static default` | Default feedback (sound + vibration for success and failure). |
| `static emptyFeedback` | No feedback (≥Cordova 7.1). |
| `success` | `Feedback` emitted on scan success. |
| `failure` | `Feedback` emitted on scan failure. |

## Step 13 — setFrame / show / hide (alternative positioning)

If you cannot use `connectToElement` (e.g. you need to place the view under WebView content while showing a results overlay), use `setFrame`:

```javascript
// The DebugApp uses this pattern to push the native view behind the overlay:
const el = document.getElementById('barcode-count-view');
const rect = el.getBoundingClientRect();

// isUnderContent = true: native view goes BEHIND the WebView (requires transparent HTML background)
await view.setFrame(
  new Scandit.Rect(
    new Scandit.Point(rect.left, rect.top),
    new Scandit.Size(rect.width, rect.height)
  ),
  true // isUnderContent
);

// Bring back to front:
await view.setFrame(
  new Scandit.Rect(new Scandit.Point(rect.left, rect.top), new Scandit.Size(rect.width, rect.height)),
  false
);
```

> **Do not** mix `setFrame` with `connectToElement` for the same view instance.

## Step 14 — Lifecycle management

### Starting scanning

```javascript
view.connectToElement(containerEl);
barcodeCount.isEnabled = true;
await camera.switchToDesiredState(Scandit.FrameSourceState.On);
```

### Teardown (navigating away from the scan screen)

Always detach on teardown:

```javascript
const teardown = async () => {
  if (camera) {
    await camera.switchToDesiredState(Scandit.FrameSourceState.Off);
    camera = null;
  }
  if (view) {
    view.detachFromElement();  // synchronous void
    view = null;
  }
  if (barcodeCount && context) {
    barcodeCount.isEnabled = false;
    context.removeMode(barcodeCount);
  }
  barcodeCount = null;
  context = null;
};
```

`detachFromElement()` releases the native view resources. Failing to call it leaks native memory.

### App backgrounding / resuming

```javascript
document.addEventListener('pause', async () => {
  if (camera) await camera.switchToDesiredState(Scandit.FrameSourceState.Off);
}, false);

document.addEventListener('resume', async () => {
  if (camera) await camera.switchToDesiredState(Scandit.FrameSourceState.On);
}, false);
```

## Step 15 — HTML setup

### Container element

The container must be in the DOM before `connectToElement` is called:

```html
<div id="barcode-count-view" style="flex: 1; width: 100%; height: 100%;"></div>
```

Or, matching the DebugApp style:

```html
<div id="barcode-count-view" class="prebuilt-view-container"></div>
```

With CSS:
```css
.prebuilt-view-container {
  flex: 1;
  width: 100%;
  height: 100%;
}
```

### Content-Security-Policy

```html
<meta http-equiv="Content-Security-Policy"
  content="default-src 'self' 'unsafe-inline' data: gap: https://ssl.gstatic.com 'unsafe-eval';
           style-src 'self' 'unsafe-inline';
           media-src *;
           img-src 'self' data: content:;" />
```

### Viewport and safe area

```html
<meta name="viewport" content="width=device-width, user-scalable=no, viewport-fit=cover" />
```

```css
.toolbar {
  padding-top: calc(env(safe-area-inset-top, 0px) + 16px);
}
.bottom-bar {
  padding-bottom: calc(env(safe-area-inset-bottom, 0px) + 16px);
}
```

## Step 16 — Complete example

```javascript
// @ts-check

let context = null;
let camera = null;
let barcodeCount = null;
let barcodeCountView = null;

async function initializeSDK() {
  if (context) return;

  context = Scandit.DataCaptureContext.initialize('-- ENTER YOUR SCANDIT LICENSE KEY HERE --');

  const cameraSettings = Scandit.BarcodeCount.createRecommendedCameraSettings();
  camera = Scandit.Camera.withSettings(cameraSettings);
  await context.setFrameSource(camera);

  const settings = new Scandit.BarcodeCountSettings();
  settings.enableSymbologies([
    Scandit.Symbology.EAN13UPCA,
    Scandit.Symbology.EAN8,
    Scandit.Symbology.Code128,
    Scandit.Symbology.DataMatrix,
  ]);

  barcodeCount = new Scandit.BarcodeCount(settings);
  context.addMode(barcodeCount);
  barcodeCount.isEnabled = true;

  barcodeCount.addListener({
    didScan: async (mode, session) => {
      const recognized = session.recognizedBarcodes;
      if (recognized) {
        Object.values(recognized).forEach(barcode => {
          console.log('Scanned:', barcode.data);
        });
      }
    },
  });

  barcodeCountView = Scandit.BarcodeCountView.forContextWithModeAndStyle(
    context,
    barcodeCount,
    Scandit.BarcodeCountViewStyle.Icon
  );

  barcodeCountView.shouldShowToolbar = true;
  barcodeCountView.shouldShowExitButton = true;

  barcodeCountView.uiListener = {
    didTapExitButton: () => {
      teardown();
    },
    didTapListButton: () => {
      // Show results overlay
    },
  };

  const containerEl = document.getElementById('barcode-count-view');
  barcodeCountView.connectToElement(containerEl);

  await camera.switchToDesiredState(Scandit.FrameSourceState.On);
}

async function teardown() {
  if (camera) {
    await camera.switchToDesiredState(Scandit.FrameSourceState.Off);
    camera = null;
  }
  if (barcodeCountView) {
    barcodeCountView.detachFromElement();
    barcodeCountView = null;
  }
  if (barcodeCount && context) {
    barcodeCount.isEnabled = false;
    context.removeMode(barcodeCount);
  }
  barcodeCount = null;
  context = null;
}

document.addEventListener('deviceready', initializeSDK, false);

document.addEventListener('pause', async () => {
  if (camera) await camera.switchToDesiredState(Scandit.FrameSourceState.Off);
}, false);

document.addEventListener('resume', async () => {
  if (camera) await camera.switchToDesiredState(Scandit.FrameSourceState.On);
}, false);
```

## Key rules

1. **Always wait for `deviceready`** before calling any `Scandit.*` API.
2. **Use the `Scandit.*` global at runtime** in plain Cordova projects.
3. **BarcodeCount requires Cordova plugin ≥6.24.** Use ≥7.6 for the context-free constructor.
4. **`new Scandit.BarcodeCount(settings)` + `context.addMode(barcodeCount)`** — the ≥7.6 pattern. The constructor alone does not wire the mode to the context.
5. **`DataCaptureContext.initialize(key)`** — the v8 entry point.
6. **`view.connectToElement(el)`** is synchronous (`void`). No `await` needed.
7. **`view.detachFromElement()`** must be called during teardown. It is also synchronous (`void`).
8. **Do not mix `connectToElement` and `setFrame`** on the same view instance.
9. **Status mode (≥8.3) is beta** — mark status-related code with a comment.
10. **Run `cordova prepare`** after installing or updating plugins.
11. **Safe-area CSS** is required on modern iOS notch/Dynamic Island devices.
