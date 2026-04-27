# MatrixScan Count Flutter Integration Guide

MatrixScan Count (API class name: `BarcodeCount`) is a data capture mode that implements an out-of-the-box scan-and-count solution. It simultaneously tracks all barcodes in the camera feed and provides a ready-to-use UI via `BarcodeCountView`. The user presses the shutter, the SDK counts the barcodes in view, and the result is delivered in the `BarcodeCountListener.didScan` callback.

> **State management note**: Examples below use the BLoC pattern (as in the official `MatrixScanCountSimpleSample`). The same APIs work with Provider, Riverpod, GetX, or plain `StatefulWidget` — adapt ownership of `DataCaptureContext`, `BarcodeCount`, and the camera to the project's existing convention.

## Prerequisites

- Scandit Flutter packages in `pubspec.yaml`:
  - `scandit_flutter_datacapture_barcode` (pulls in `scandit_flutter_datacapture_core` transitively)
  - `permission_handler` (for the runtime camera permission on Android)
- Flutter `>=3.22.0`, Dart SDK `>=3.0.0 <4.0.0`.
- After editing `pubspec.yaml`, run `flutter pub get` to fetch the packages.
- A valid Scandit license key:
  - Sign in at https://ssl.scandit.com to generate one.
  - No account yet? Sign up at https://ssl.scandit.com/dashboard/sign-up?p=test.
- Camera permissions configured by the app:
  - iOS: add `NSCameraUsageDescription` to `ios/Runner/Info.plist`.
  - Android: the manifest permission is declared by the plugin; request at runtime with `permission_handler`.

## Integration flow

Ask the user which barcode symbologies they need to scan. When asking, mention that enabling only the symbologies actually needed improves scanning performance and accuracy.

Once the user responds, ask them which file they would like to integrate BarcodeCount into (typically a BLoC / controller class, or a page `StatefulWidget`). Then write the integration code directly into that file. Do not just show the code in chat; apply it to the file.

After providing the code, show this setup checklist:

**Setup checklist:**
1. Add `scandit_flutter_datacapture_barcode` and `permission_handler` to `pubspec.yaml`, then run `flutter pub get`.
2. Add `NSCameraUsageDescription` to `ios/Runner/Info.plist` with a short usage explanation.
3. Replace `'-- ENTER YOUR SCANDIT LICENSE KEY HERE --'` with the key from https://ssl.scandit.com.
4. Ensure `main()` calls `WidgetsFlutterBinding.ensureInitialized()` and then `await ScanditFlutterDataCaptureBarcode.initialize()` before `runApp(...)`.
5. Call `Permission.camera.request()` from `permission_handler` before the first scan (usually in `initState()` of the scanning page).

## Step 1 — Initialize the SDK in `main()`

Plugin initialization **must** happen before any other Scandit API call.

```dart
import 'package:flutter/material.dart';
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // Must be called first — sets up all Scandit plugins.
  await ScanditFlutterDataCaptureBarcode.initialize();
  runApp(const MyApp());
}
```

> **Important**: Always call `WidgetsFlutterBinding.ensureInitialized()` before the `await`. Flutter requires the binding before any platform-channel call.

## Step 2 — Create the DataCaptureContext

```dart
import 'package:scandit_flutter_datacapture_core/scandit_flutter_datacapture_core.dart';

const String licenseKey = '-- ENTER YOUR SCANDIT LICENSE KEY HERE --';

final DataCaptureContext dataCaptureContext =
    DataCaptureContext.forLicenseKey(licenseKey);
```

## Step 3 — Configure BarcodeCountSettings and Symbologies

All symbologies are disabled by default. Only enable the ones the app actually needs.

```dart
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode.dart';
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode_count.dart';

final settings = BarcodeCountSettings()
  ..enableSymbologies({
    Symbology.ean13Upca,
    Symbology.ean8,
    Symbology.upce,
    Symbology.code39,
    Symbology.code128,
  });

// Optional: declare that only unique barcodes are expected (optimizes tracking).
settings.expectsOnlyUniqueBarcodes = true;
```

### BarcodeCountSettings Properties and Methods

| API | Type | Description |
|-----|------|-------------|
| `enableSymbologies(symbologies)` | method | Enable multiple symbologies (takes a `Set<Symbology>`). |
| `enableSymbology(symbology, enabled)` | method | Enable or disable a single symbology. |
| `settingsForSymbology(symbology)` | method | Get per-symbology settings (e.g. `activeSymbolCounts`). Flutter: `SymbologySettings`. |
| `expectsOnlyUniqueBarcodes` | `bool` | Set to `true` if duplicates within a batch are not expected. Default `false`. |
| `disableModeWhenCaptureListCompleted` | `bool` | Auto-disable the mode when the capture list is fully scanned. Flutter ≥8.3. Default `false`. |
| `mappingEnabled` | `bool` | Enable the barcode mapping flow (beta). Flutter ≥8.3. Default `false`. |
| `clusteringMode` | `ClusteringMode` | Smart grouping of neighbouring barcodes. Flutter ≥8.3. |
| `scanPreviewEnabled` | `bool` (read-only) | Whether scan preview is enabled (set at construction). Flutter ≥8.3. |
| `setProperty<T>(name, value)` | method | Advanced hidden property setter. |
| `getProperty<T>(name)` | method | Advanced hidden property getter. |

### Scan Preview (beta)

To enable the scan preview so barcodes appear highlighted before the shutter is pressed:

```dart
final settings = BarcodeCountSettings(scanPreviewEnabled: true);
```

Only basic scanning and scanning against a list are supported when scan preview is enabled.

## Step 4 — Construct BarcodeCount

Use the `BarcodeCount(settings)` constructor available on Flutter ≥7.6. After construction, register the mode with the context explicitly.

```dart
final barcodeCount = BarcodeCount(settings);
dataCaptureContext.setMode(barcodeCount);
```

Apply recommended camera settings:

```dart
final cameraSettings = BarcodeCount.createRecommendedCameraSettings();
camera.applySettings(cameraSettings);
```

### BarcodeCount Methods and Properties

| API | Available | Description |
|-----|-----------|-------------|
| `BarcodeCount(settings)` | Flutter ≥7.6 | Context-free constructor. |
| `addListener(listener)` / `removeListener(listener)` | Flutter ≥6.17 | Register/remove a `BarcodeCountListener`. |
| `applySettings(settings)` | Flutter ≥6.17 | Asynchronously apply updated settings at runtime. Returns `Future<void>`. |
| `BarcodeCount.createRecommendedCameraSettings()` | Flutter ≥7.6 | Returns `CameraSettings` tuned for BarcodeCount. |
| `reset()` | Flutter ≥6.17 | Resets the session, clearing history. Returns `Future<void>`. |
| `startScanningPhase()` | Flutter ≥6.17 | Programmatically trigger a scan (same as pressing the shutter). Returns `Future<void>`. |
| `endScanningPhase()` | Flutter ≥6.17 | Disables the mode and switches off the frame source. Returns `Future<void>`. |
| `setBarcodeCountCaptureList(list)` | Flutter ≥6.17 | Enable scanning against a target list. Returns `Future<void>`. |
| `setAdditionalBarcodes(barcodes)` | Flutter ≥6.17 | Inject barcodes from a previous session. Returns `Future<void>`. |
| `clearAdditionalBarcodes()` | Flutter ≥6.17 | Clear injected additional barcodes. Returns `Future<void>`. |
| `isEnabled` | `bool` | Enable/disable the mode. |
| `feedback` | `BarcodeCountFeedback` | Sound/vibration feedback on scan events. |
| `context` | `DataCaptureContext?` | The context this mode is attached to (read-only). |

## Step 5 — BarcodeCountListener

Implement `BarcodeCountListener` on the BLoC to receive scan results. The primary callback is `didScan`, called once the scanning phase completes (i.e., the user pressed the shutter or `startScanningPhase()` was called).

```dart
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode.dart';
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode_count.dart';
import 'package:scandit_flutter_datacapture_core/scandit_flutter_datacapture_core.dart';

class CountBloc implements BarcodeCountListener {
  @override
  Future<void> didScan(
    BarcodeCount barcodeCount,
    BarcodeCountSession session,
    Future<FrameData> Function() getFrameData,
  ) async {
    final recognized = session.recognizedBarcodes;
    for (final barcode in recognized) {
      debugPrint('Scanned: ${barcode.data} (${barcode.symbology})');
    }
  }
}
```

### BarcodeCountListener Dart Callbacks

| Callback | Signature | Description |
|----------|-----------|-------------|
| `didScan` | `(BarcodeCount, BarcodeCountSession, Future<FrameData> Function()) => Future<void>` | Called once the scanning phase is over (shutter pressed). |

### IBarcodeCountExtendedListener (Flutter ≥8.3)

Implement `IBarcodeCountExtendedListener` (instead of `BarcodeCountListener`) to also receive per-frame session updates:

```dart
class CountBloc implements IBarcodeCountExtendedListener {
  @override
  Future<void> didScan(BarcodeCount barcodeCount, BarcodeCountSession session,
      Future<FrameData> Function() getFrameData) async { ... }

  @override
  void didUpdateSession(BarcodeCount barcodeCount, BarcodeCountSession session,
      Future<FrameData> Function() getFrameData) { ... }
}
```

`didUpdateSession` is **Flutter-only** (part of `IBarcodeCountExtendedListener`) and is called on every processed frame. Use it to react to barcode tracking changes in real-time between shutter presses.

### BarcodeCountSession Properties

| Property | Type | Description |
|----------|------|-------------|
| `recognizedBarcodes` | `List<Barcode>` | All currently recognized barcodes (available from Flutter 7.0). |
| `additionalBarcodes` | `List<Barcode>` | Barcodes injected via `setAdditionalBarcodes`. |
| `frameSequenceId` | `int` | Identifier of the current frame sequence. |
| `reset()` | `Future<void>` | Reset the session inside the listener callback. |
| `getSpatialMap()` | `Future<BarcodeSpatialGrid?>` | Compute the spatial map (requires `mappingEnabled = true`). Flutter ≥8.3. |
| `getSpatialMapWithHints(rows, cols)` | `Future<BarcodeSpatialGrid?>` | Compute spatial map with grid size hints. Flutter ≥8.3. |

## Step 6 — Create BarcodeCountView

`BarcodeCountView` is a Flutter `StatefulWidget`. It must be **presented full screen**. The canonical pattern from the sample creates it inline in `build()` with the cascade operator to set the listeners:

```dart
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode_count.dart';

// Inside build():
BarcodeCountView.forContextWithModeAndStyle(
  _bloc.dataCaptureContext,
  _bloc.barcodeCount,
  BarcodeCountViewStyle.icon,
)
  ..uiListener = _bloc
  ..listener = _bloc
```

### BarcodeCountView Constructors

| Constructor | Description |
|-------------|-------------|
| `BarcodeCountView.forContextWithMode(dataCaptureContext, barcodeCount)` | Default style (dot). |
| `BarcodeCountView.forContextWithModeAndStyle(dataCaptureContext, barcodeCount, style)` | Explicit style (icon or dot). |
| `BarcodeCountView.forMapping(dataCaptureContext, barcodeCount, style, mappingFlowSettings)` | For the grid mapping flow (Flutter ≥8.3). |

### BarcodeCountViewStyle

| Value | Description |
|-------|-------------|
| `BarcodeCountViewStyle.icon` | Draws highlights as icons with entry animation. |
| `BarcodeCountViewStyle.dot` | Draws highlights as dots with entry animation. |

Default style is `dot`. Use `icon` for the most common MatrixScan Count UI.

### Widget tree integration

The view must fill the screen. The sample wraps it in a `LayoutBuilder` + `AspectRatio` to handle portrait/landscape, but a simple `Expanded` or `SizedBox.expand` is sufficient for full-screen:

```dart
@override
Widget build(BuildContext context) {
  return Scaffold(
    backgroundColor: Colors.black,
    body: SafeArea(
      bottom: false,
      child: BarcodeCountView.forContextWithModeAndStyle(
        _bloc.dataCaptureContext,
        _bloc.barcodeCount,
        BarcodeCountViewStyle.icon,
      )
        ..uiListener = _bloc
        ..listener = _bloc,
    ),
  );
}
```

## Step 7 — BarcodeCountViewListener

Implement `BarcodeCountViewListener` on the BLoC to receive tap events on barcode highlights. This listener also receives `didCompleteCaptureList` when a target list is fully scanned.

```dart
class CountBloc implements BarcodeCountListener, BarcodeCountViewListener {
  @override
  Brush? brushForRecognizedBarcode(BarcodeCountView view, TrackedBarcode trackedBarcode) {
    // Return null to use the default brush (or when using icon style).
    return null;
  }

  @override
  Brush? brushForRecognizedBarcodeNotInList(BarcodeCountView view, TrackedBarcode trackedBarcode) {
    return null;
  }

  @override
  void didTapRecognizedBarcode(BarcodeCountView view, TrackedBarcode trackedBarcode) {
    debugPrint('Tapped: ${trackedBarcode.barcode.data}');
  }

  @override
  void didTapRecognizedBarcodeNotInList(BarcodeCountView view, TrackedBarcode trackedBarcode) {
    // Only called when a capture list is set.
  }

  @override
  void didTapFilteredBarcode(BarcodeCountView view, TrackedBarcode filteredBarcode) {}

  @override
  void didCompleteCaptureList(BarcodeCountView view) {
    // All target barcodes have been scanned.
  }
}
```

### BarcodeCountViewListener Dart Callbacks

| Callback | Description |
|----------|-------------|
| `brushForRecognizedBarcode(view, trackedBarcode) -> Brush?` | Per-barcode brush for recognized barcodes. Returns `null` to use default. Dot style only. |
| `brushForRecognizedBarcodeNotInList(view, trackedBarcode) -> Brush?` | Per-barcode brush for not-in-list barcodes. Dot style only. Only called when a capture list is set. |
| `didTapRecognizedBarcode(view, trackedBarcode)` | Called when a recognized barcode is tapped. |
| `didTapRecognizedBarcodeNotInList(view, trackedBarcode)` | Called when a not-in-list barcode is tapped. Only when a capture list is set. |
| `didTapFilteredBarcode(view, filteredBarcode)` | Called when a filtered barcode is tapped. |
| `didCompleteCaptureList(view)` | Called when all items in the capture list have been scanned. Only when a capture list is set. |

### IBarcodeCountViewExtendedListener (Flutter ≥8.3)

Implement `IBarcodeCountViewExtendedListener` (instead of `BarcodeCountViewListener`) to also receive cluster-tap events:

```dart
class CountBloc implements IBarcodeCountViewExtendedListener {
  // ... all BarcodeCountViewListener methods plus:
  @override
  void didTapCluster(BarcodeCountView view, Cluster cluster) { ... }
}
```

`didTapCluster` is **Flutter-only** (part of `IBarcodeCountViewExtendedListener`).

## Step 8 — BarcodeCountViewUiListener

Implement `BarcodeCountViewUiListener` to receive button-tap events from the view's UI.

```dart
class CountBloc implements BarcodeCountViewUiListener {
  @override
  void didTapListButton(BarcodeCountView view) {
    // Navigate to the scanned items list.
  }

  @override
  void didTapExitButton(BarcodeCountView view) {
    // Navigate out of the scanning screen.
  }

  @override
  void didTapSingleScanButton(BarcodeCountView view) {
    // Trigger a single-item scan.
  }
}
```

### BarcodeCountViewUiListener Dart Callbacks

| Callback | Description |
|----------|-------------|
| `didTapListButton(view)` | List button tapped. Freezes the mode by default (unless `shouldShowListButton` is modified). |
| `didTapExitButton(view)` | Exit button tapped. |
| `didTapSingleScanButton(view)` | Single scan button tapped (only visible when `shouldShowSingleScanButton` is `true`). |

## Step 9 — BarcodeCountView Customization

`BarcodeCountView` exposes an extensive set of properties to tailor the UI. All are set directly on the view instance (typically via cascade in `build()` or after construction in `initState()`).

### Visibility Booleans

| Property | Default | Description |
|----------|---------|-------------|
| `shouldShowUserGuidanceView` | `true` | Show the user guidance / loading view. |
| `shouldShowListProgressBar` | `true` | Show the capture-list progress bar (Flutter ≥6.25). |
| `shouldShowListButton` | `true` | Show the list button (lower-left). |
| `shouldShowExitButton` | `true` | Show the exit button (lower-right). |
| `shouldShowShutterButton` | `true` | Show the centered shutter button. |
| `shouldShowHints` | `true` | Show scanning hint messages. |
| `shouldShowClearHighlightsButton` | `false` | Show the "clear highlights" button above the shutter. |
| `shouldShowSingleScanButton` | `false` | Show the single-scan button (lower-left). |
| `shouldShowStatusModeButton` | `false` | Show the status-mode toggle button (Flutter ≥7.0, beta). |
| `shouldShowFloatingShutterButton` | `false` | Show the draggable floating shutter button. |
| `shouldShowToolbar` | `true` | Show the collapsable toolbar at the top. |
| `shouldShowScanAreaGuides` | `false` | Visualize the scan area (debug only). |
| `shouldShowTorchControl` | `false` | Show the torch toggle button (Flutter ≥6.28). |
| `shouldShowStatusIconsOnScan` | `false` | Show status icons immediately on scan without activating status mode (Flutter ≥8.3, beta). |
| `shouldDisableModeOnExitButtonTapped` | `true` | Auto-disable mode when exit button is tapped (Flutter ≥8.3). |

```dart
BarcodeCountView.forContextWithModeAndStyle(ctx, mode, BarcodeCountViewStyle.icon)
  ..shouldShowToolbar = false
  ..shouldShowExitButton = false
  ..shouldShowClearHighlightsButton = true
  ..shouldShowFloatingShutterButton = true
```

### Brushes (Dot style only)

Brushes control the highlight color for barcodes in the dot style. They have no effect in icon style.

**Static defaults** (read the defaults for reference):
```dart
final defaultRecognized = BarcodeCountView.defaultRecognizedBrush;
final defaultNotInList  = BarcodeCountView.defaultNotInListBrush;
final defaultAccepted   = BarcodeCountView.defaultAcceptedBrush;   // Flutter ≥8.3
final defaultRejected   = BarcodeCountView.defaultRejectedBrush;   // Flutter ≥8.3
```

**Instance brush properties** (override the default for all barcodes of a category):
```dart
view
  ..recognizedBrush = Brush(
      const Color(0x6600FF00), const Color(0xFF00FF00), 1.0)
  ..notInListBrush = Brush(
      const Color(0x66FF0000), const Color(0xFFFF0000), 1.0)
  ..acceptedBrush = Brush(                              // Flutter ≥8.3
      const Color(0x660000FF), const Color(0xFF0000FF), 1.0)
  ..rejectedBrush = Brush(                              // Flutter ≥8.3
      const Color(0x66FF8800), const Color(0xFFFF8800), 1.0);
```

Setting any brush property to `null` hides all barcodes in that category.

**Per-barcode brush methods** (override for an individual barcode, called from `BarcodeCountViewListener`):
```dart
// Inside didTapRecognizedBarcode or a custom flow — Flutter ≥8.3:
await view.setBrushForRecognizedBarcode(trackedBarcode, myBrush);
await view.setBrushForRecognizedBarcodeNotInList(trackedBarcode, myBrush);
await view.setBrushForAcceptedBarcode(trackedBarcode, myBrush);    // Flutter ≥8.3
await view.setBrushForRejectedBarcode(trackedBarcode, myBrush);    // Flutter ≥8.3
```

### Customizable Text

| Property | Description |
|----------|-------------|
| `exitButtonText` | Text of the exit button label. |
| `clearHighlightsButtonText` | Text of the "clear highlights" button. |

### Hint Text Setters

All `textFor*Hint` properties are available on Flutter (exact version varies per property):

| Property | Description |
|----------|-------------|
| `textForTapShutterToScanHint` | Hint prompting the user to tap the shutter. |
| `textForScanningHint` | Hint shown while scanning is in progress. |
| `textForMoveCloserAndRescanHint` | Hint shown when the camera is too far. |
| `textForMoveFurtherAndRescanHint` | Hint shown when the camera is too close. |
| `textForBarcodesNotInListDetectedHint` | Hint shown when a not-in-list barcode is detected (Flutter ≥8.3). |
| `textForScreenCleanedUpHint` | Hint shown when the screen is cleaned (Flutter ≥8.3). |
| `textForTapToUncountHint` | Hint shown when the user deselects an item (Flutter ≥7.0). |
| `textForClusteringGestureHint` | Hint shown for cluster gestures (Flutter ≥8.3). |

```dart
view
  ..textForTapShutterToScanHint = 'Tap to scan'
  ..textForScanningHint = 'Scanning…'
  ..textForMoveCloserAndRescanHint = 'Move closer and scan again'
  ..textForMoveFurtherAndRescanHint = 'Move further and scan again'
  ..textForTapToUncountHint = 'Tap to remove';
```

### Accessibility (label and hint pairs)

The following accessibility string properties are available on Flutter. Properties marked "(iOS only)" have effect only on iOS; "(Android only)" only on Android.

| Property | Platform | Description |
|----------|----------|-------------|
| `listButtonAccessibilityLabel` | iOS only | Accessibility label for the list button. |
| `listButtonAccessibilityHint` | iOS only | Accessibility hint for the list button. |
| `listButtonContentDescription` | Android only | Content description for the list button. |
| `exitButtonAccessibilityLabel` | iOS only | Accessibility label for the exit button. |
| `exitButtonAccessibilityHint` | iOS only | Accessibility hint for the exit button. |
| `exitButtonContentDescription` | Android only | Content description for the exit button. |
| `shutterButtonAccessibilityLabel` | iOS only | Accessibility label for the shutter button. |
| `shutterButtonAccessibilityHint` | iOS only | Accessibility hint for the shutter button. |
| `shutterButtonContentDescription` | Android only | Content description for the shutter button. |
| `floatingShutterButtonAccessibilityLabel` | iOS only | Accessibility label for the floating shutter button. |
| `floatingShutterButtonAccessibilityHint` | iOS only | Accessibility hint for the floating shutter button. |
| `floatingShutterButtonContentDescription` | Android only | Content description for the floating shutter button. |
| `singleScanButtonAccessibilityLabel` | iOS only | Accessibility label for the single scan button. |
| `singleScanButtonAccessibilityHint` | iOS only | Accessibility hint for the single scan button. |
| `singleScanButtonContentDescription` | Android only | Content description for the single scan button. |
| `clearHighlightsButtonAccessibilityLabel` | iOS only | Accessibility label for the clear highlights button. |
| `clearHighlightsButtonAccessibilityHint` | iOS only | Accessibility hint for the clear highlights button. |
| `clearHighlightsButtonContentDescription` | Android only | Content description for the clear highlights button. |
| `statusModeButtonAccessibilityLabel` | iOS only | Accessibility label for the status mode button (Flutter ≥8.3). |
| `statusModeButtonAccessibilityHint` | iOS only | Accessibility hint for the status mode button (Flutter ≥8.3). |
| `statusModeButtonContentDescription` | Android only | Content description for the status mode button (Flutter ≥8.3). |

### Hardware Trigger (enterprise devices)

For enterprise scanners (e.g. Zebra XCover) with a physical scan button:

```dart
// Android: enable the hardware trigger (pass null to use the default key):
await view.enableHardwareTrigger(null);

// iOS: enable hardware trigger via the volume button:
view.hardwareTriggerEnabled = true;   // Flutter ≥8.3

// Check support at runtime (Android only):
if (BarcodeCountView.hardwareTriggerSupported) {  // Flutter ≥8.3
  await view.enableHardwareTrigger(null);
}
```

`enableHardwareTrigger(keyCode)` — Flutter ≥8.3. On Android, pass `null` to use the default key (dedicated HW key on XCover, or volume-down on others; requires API ≥28). On iOS, `hardwareTriggerEnabled = true` reacts to the volume button.

### Tap-to-Uncount

```dart
view.tapToUncountEnabled = true;  // Flutter ≥7.0, default false
view.textForTapToUncountHint = 'Tap to remove';
```

Allows users to deselect a scanned barcode by tapping on its highlight.

### Torch Control

```dart
view
  ..shouldShowTorchControl = true    // Flutter ≥6.28, default false
  ..torchControlPosition = Anchor.topLeft;  // topLeft, topRight, bottomLeft, bottomRight
```

### Not-in-List Action Settings (Flutter ≥8.3, beta)

When scanning against a capture list, optionally show an accept/reject popup for barcodes not in the list:

```dart
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode_count.dart';

final notInListSettings = BarcodeCountNotInListActionSettings()
  ..enabled = true
  ..acceptButtonText = 'Accept'
  ..rejectButtonText = 'Reject'
  ..cancelButtonText = 'Cancel'
  ..barcodeAcceptedHint = 'Barcode accepted'
  ..barcodeRejectedHint = 'Barcode rejected';

view.barcodeNotInListActionSettings = notInListSettings;
```

### Filter Settings

```dart
// view.filterSettings accepts a BarcodeFilterHighlightSettings instance.
// See the BarcodeFilterHighlightSettings API for details.
view.filterSettings = myFilterHighlightSettings;
```

### View-level Methods

| Method | Description |
|--------|-------------|
| `view.clearHighlights()` | Clear all highlight overlays (returns `Future<void>`). |
| `view.setToolbarSettings(settings)` | Configure toolbar text and accessibility strings (returns `Future<void>`). |
| `view.setStatusProvider(provider)` | Attach a `BarcodeCountStatusProvider` for status mode (returns `Future<void>`). Flutter ≥7.0. |

### Listeners

| Property | Type | Description |
|----------|------|-------------|
| `view.listener` | `BarcodeCountViewListener?` | Brush and tap callbacks per tracked barcode. |
| `view.uiListener` | `BarcodeCountViewUiListener?` | Button tap callbacks (list, exit, single-scan). |

## Step 10 — Scanning Against a Target List

`BarcodeCountCaptureList` enables scanning against a fixed set of expected barcodes (quantity-aware). The view automatically shows a progress bar and highlights not-in-list barcodes differently.

```dart
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode_count.dart';

// Define the target barcodes (data + expected quantity):
final targetBarcodes = [
  TargetBarcode.create('123456789012', 2),
  TargetBarcode.create('987654321098', 1),
];

// Create the list with a listener (the BLoC or a dedicated class):
final captureList = BarcodeCountCaptureList.create(this, targetBarcodes);

// Attach the list to the mode:
await barcodeCount.setBarcodeCountCaptureList(captureList);
```

`TargetBarcode.create(data, quantity)` — data is the barcode string content, quantity is the expected scan count.

### BarcodeCountCaptureListListener Dart Callback

```dart
class CountBloc implements BarcodeCountCaptureListListener {
  @override
  void didUpdateSession(
    BarcodeCountCaptureList barcodeCountCaptureList,
    BarcodeCountCaptureListSession session,
  ) {
    // Called after each frame updates the list state.
  }
}
```

`IBarcodeCountCaptureListExtendedListener` (Flutter ≥8.3) adds `didCompleteCaptureList` for when all list items are scanned — **Flutter-only** extended interface.

## Step 11 — Status Provider (Flutter ≥7.0, beta)

`BarcodeCountStatusProvider` allows assigning a status icon to each scanned barcode (e.g. "in stock", "needs recount"). Activated via the status mode button (or `shouldShowStatusIconsOnScan`).

```dart
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode_count.dart';

class MyStatusProvider implements BarcodeCountStatusProvider {
  @override
  void onStatusRequested(
    List<TrackedBarcode> barcodes,
    BarcodeCountStatusProviderCallback providerCallback,
  ) {
    // Build a list of BarcodeCountStatusItem for each barcode and call the callback.
    // Choose a BarcodeCountStatus value for each barcode (see enum table below).
    providerCallback.onStatusReady(statusItems);
  }
}

// Attach to the view:
await view.setStatusProvider(MyStatusProvider());
view.shouldShowStatusModeButton = true;
```

### BarcodeCountStatus Enum Values (Flutter ≥7.0)

All values are available on Flutter from SDK 7.0 unless noted otherwise. Use Dart lowerCamelCase form when constructing a `BarcodeCountStatusItem`.

| Value | Description |
|-------|-------------|
| `BarcodeCountStatus.none` | No status — barcode has not been assigned any status icon. |
| `BarcodeCountStatus.notAvailable` | Error retrieving the status — the backend could not provide a result. |
| `BarcodeCountStatus.expired` | The item is expired. |
| `BarcodeCountStatus.fragile` | The item must be handled with care (fragile). |
| `BarcodeCountStatus.qualityCheck` | The item requires a quality check before acceptance. |
| `BarcodeCountStatus.lowStock` | Stock level for this item is low. |
| `BarcodeCountStatus.wrong` | The item is incorrect (e.g. wrong SKU or wrong location). |
| `BarcodeCountStatus.expiringSoon` | The item will expire soon (Flutter ≥7.0, Android/iOS ≥7.0). |

> **Note**: `BarcodeCountStatus` is a true enum. There are no `accept` / `reject` values in the Flutter SDK enum. If you need to distinguish accepted vs. rejected barcodes visually, use `BarcodeCountStatus.none` for accepted items (no icon shown) and a meaningful value such as `BarcodeCountStatus.wrong` or `BarcodeCountStatus.notAvailable` for rejected items, combined with the `acceptedBrush` / `rejectedBrush` view properties.

## Step 12 — Feedback

`BarcodeCountFeedback` controls sound and vibration on scan events.

```dart
// Use the default feedback (sound + vibration for success and failure):
barcodeCount.feedback = BarcodeCountFeedback.defaultFeedback;

// Disable all feedback:
barcodeCount.feedback = BarcodeCountFeedback();

// Custom: keep success vibration, disable success sound:
final feedback = BarcodeCountFeedback()
  ..success = Feedback(vibration: Vibration.defaultVibration, sound: null)
  ..failure = Feedback(vibration: null, sound: null);
barcodeCount.feedback = feedback;
```

### BarcodeCountFeedback Properties

| Property | Type | Description |
|----------|------|-------------|
| `success` | `Feedback` | Feedback for a successful scan event. |
| `failure` | `Feedback` | Feedback for a failure event. |
| `BarcodeCountFeedback.defaultFeedback` | static | Default config: both sound and vibration for success and failure. |

## Step 13 — Camera lifecycle

Camera state is managed via `Camera.switchToDesiredState`. Pause when the app goes to background; resume on foreground.

```dart
class _CountScreenState extends State<CountScreen> with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _bloc.didResume(); // request camera permission + enable mode
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {
      case AppLifecycleState.resumed:
        _bloc.didResume();
        break;
      default:
        _bloc.didPause();
        break;
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _bloc.dispose();
    super.dispose();
  }
}
```

In the BLoC:
```dart
void didResume() {
  barcodeCount.addListener(this);
  barcodeCount.isEnabled = true;
  Permission.camera.request().then((status) {
    if (status.isGranted) resumeFrameSource();
  });
}

void didPause() {
  pauseFrameSource();
  barcodeCount.removeListener(this);
}

void pauseFrameSource() => camera?.switchToDesiredState(FrameSourceState.off);
void resumeFrameSource() => camera?.switchToDesiredState(FrameSourceState.on);
```

## Step 14 — Toolbar Settings

`BarcodeCountToolbarSettings` configures the text of the collapsable toolbar buttons.

```dart
final toolbarSettings = BarcodeCountToolbarSettings()
  ..audioOnButtonText = 'Sound On'
  ..audioOffButtonText = 'Sound Off'
  ..vibrationOnButtonText = 'Vibration On'
  ..vibrationOffButtonText = 'Vibration Off';
await view.setToolbarSettings(toolbarSettings);
```

Key properties: `audioOnButtonText`, `audioOffButtonText`, `vibrationOnButtonText`, `vibrationOffButtonText`, `strapModeOnButtonText`, `strapModeOffButtonText`, `colorSchemeOnButtonText`, `colorSchemeOffButtonText`. Each has iOS (`*AccessibilityLabel`, `*AccessibilityHint`) and Android (`*ContentDescription`) accessibility variants.

## Step 15 — Complete Example

### main.dart

```dart
import 'package:flutter/material.dart';
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await ScanditFlutterDataCaptureBarcode.initialize();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(
    debugShowCheckedModeBanner: false,
    title: 'MatrixScan Count',
    home: CountScreen(),
  );
}
```

### count_bloc.dart

```dart
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode.dart';
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode_count.dart';
import 'package:scandit_flutter_datacapture_core/scandit_flutter_datacapture_core.dart';

const String licenseKey = '-- ENTER YOUR SCANDIT LICENSE KEY HERE --';

class CountBloc
    implements BarcodeCountListener, BarcodeCountViewListener, BarcodeCountViewUiListener {
  late final DataCaptureContext dataCaptureContext;
  late final BarcodeCount barcodeCount;
  Camera? _camera;

  final List<Barcode> scannedBarcodes = [];
  final _controller = StreamController<List<Barcode>>.broadcast();
  Stream<List<Barcode>> get scanned => _controller.stream;

  CountBloc() {
    dataCaptureContext = DataCaptureContext.forLicenseKey(licenseKey);
    _camera = Camera.defaultCamera;
    _camera?.applySettings(BarcodeCount.createRecommendedCameraSettings());
    if (_camera != null) dataCaptureContext.setFrameSource(_camera!);

    final settings = BarcodeCountSettings()
      ..enableSymbologies({
        Symbology.ean13Upca,
        Symbology.ean8,
        Symbology.code128,
      });

    barcodeCount = BarcodeCount(settings);
    dataCaptureContext.setMode(barcodeCount);
  }

  void didResume() {
    barcodeCount.addListener(this);
    barcodeCount.isEnabled = true;
    Permission.camera.request().then((status) {
      if (status.isGranted) _camera?.switchToDesiredState(FrameSourceState.on);
    });
  }

  void didPause() {
    _camera?.switchToDesiredState(FrameSourceState.off);
    barcodeCount.removeListener(this);
  }

  @override
  Future<void> didScan(BarcodeCount barcodeCount, BarcodeCountSession session,
      Future<FrameData> Function() getFrameData) async {
    scannedBarcodes.addAll(session.recognizedBarcodes);
    _controller.add(List.unmodifiable(scannedBarcodes));
  }

  @override
  Brush? brushForRecognizedBarcode(BarcodeCountView view, TrackedBarcode trackedBarcode) => null;

  @override
  Brush? brushForRecognizedBarcodeNotInList(BarcodeCountView view, TrackedBarcode trackedBarcode) => null;

  @override
  void didTapRecognizedBarcode(BarcodeCountView view, TrackedBarcode trackedBarcode) {}

  @override
  void didTapRecognizedBarcodeNotInList(BarcodeCountView view, TrackedBarcode trackedBarcode) {}

  @override
  void didTapFilteredBarcode(BarcodeCountView view, TrackedBarcode filteredBarcode) {}

  @override
  void didCompleteCaptureList(BarcodeCountView view) {}

  @override
  void didTapListButton(BarcodeCountView view) {}

  @override
  void didTapExitButton(BarcodeCountView view) {}

  @override
  void didTapSingleScanButton(BarcodeCountView view) {}

  Future<void> resetSession() async {
    scannedBarcodes.clear();
    await barcodeCount.clearAdditionalBarcodes();
    await barcodeCount.reset();
  }

  void dispose() {
    _camera?.switchToDesiredState(FrameSourceState.off);
    barcodeCount.removeListener(this);
    dataCaptureContext.removeAllModes();
    _controller.close();
  }
}
```

### count_screen.dart

```dart
import 'package:flutter/material.dart';
import 'package:scandit_flutter_datacapture_barcode/scandit_flutter_datacapture_barcode_count.dart';
import 'count_bloc.dart';

class CountScreen extends StatefulWidget {
  @override
  State<CountScreen> createState() => _CountScreenState();
}

class _CountScreenState extends State<CountScreen> with WidgetsBindingObserver {
  late final CountBloc _bloc;

  @override
  void initState() {
    super.initState();
    _bloc = CountBloc();
    WidgetsBinding.instance.addObserver(this);
    _bloc.didResume();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: SafeArea(
        bottom: false,
        child: BarcodeCountView.forContextWithModeAndStyle(
          _bloc.dataCaptureContext,
          _bloc.barcodeCount,
          BarcodeCountViewStyle.icon,
        )
          ..uiListener = _bloc
          ..listener = _bloc,
      ),
    );
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {
      case AppLifecycleState.resumed:
        _bloc.didResume();
        break;
      default:
        _bloc.didPause();
        break;
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _bloc.dispose();
    super.dispose();
  }
}
```

## Key Rules

1. **Initialize plugins first** — `await ScanditFlutterDataCaptureBarcode.initialize()` must be in `main()` after `WidgetsFlutterBinding.ensureInitialized()` and before `runApp(...)`.
2. **Import** — BarcodeCount classes live in `scandit_flutter_datacapture_barcode_count`. Always import this alongside `scandit_flutter_datacapture_barcode`.
3. **Constructor version** — Use `BarcodeCount(settings)` on Flutter ≥7.6, then call `dataCaptureContext.setMode(barcodeCount)` explicitly. On older SDKs, use `BarcodeCount.forDataCaptureContext(context, settings)` and await the Future.
4. **Present full screen** — `BarcodeCountView` must be full screen per SDK documentation. The sample uses `AspectRatio(9/16)` in portrait / `AspectRatio(16/9)` in landscape.
5. **BLoC owns listeners** — The BLoC implements `BarcodeCountListener`, `BarcodeCountViewListener`, and `BarcodeCountViewUiListener`. Wire them via `..listener = _bloc ..uiListener = _bloc` on the view.
6. **Brush properties are dot-style only** — `recognizedBrush`, `notInListBrush`, `acceptedBrush`, `rejectedBrush`, `setBrushFor*` methods, and `brushFor*` listener callbacks have no effect in icon style.
7. **Symbologies** — use lowerCamelCase: `Symbology.ean13Upca`, `Symbology.code128`, etc.
8. **Session access** — Access `BarcodeCountSession` only inside `didScan`. Do not keep a reference to the session beyond the callback.
9. **Camera permission** — iOS needs `NSCameraUsageDescription` in `Info.plist`. Android runtime permission via `permission_handler`. Re-request on `AppLifecycleState.resumed`.
10. **Beta APIs** — Status mode, mapping flow, not-in-list action settings, and clustering are marked beta and may change in future SDK versions.
