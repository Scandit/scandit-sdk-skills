# BarcodeCapture Android Integration Guide

BarcodeCapture is the low-level single-barcode scanning mode. On Android you wire it up by hand: a `DataCaptureContext`, a `Camera` as the frame source, the `BarcodeCapture` mode with a `BarcodeCaptureListener`, a `DataCaptureView` for the camera preview, and a `BarcodeCaptureOverlay` for the highlight. Unlike SparkScan, there is no pre-built UI — the camera preview and highlight rectangle are the only visuals.

Examples below use Kotlin and an Activity. The same APIs work identically with Java and in Fragments — adapt ownership of `DataCaptureContext`, `BarcodeCapture`, and the `Camera` to the project's existing structure.

## Prerequisites

- Scandit Data Capture SDK for Android — add via Gradle. Before writing the dependency, fetch the latest published version from `https://central.sonatype.com/artifact/com.scandit.datacapture/barcode` and extract the latest version number from the page. Then add both dependencies to `app/build.gradle`:
  ```gradle
  dependencies {
      implementation "com.scandit.datacapture:barcode:<latest-version>"
      implementation "com.scandit.datacapture:core:<latest-version>"
  }
  ```
  Or in `app/build.gradle.kts`:
  ```kotlin
  dependencies {
      implementation("com.scandit.datacapture:barcode:<latest-version>")
      implementation("com.scandit.datacapture:core:<latest-version>")
  }
  ```
  The SDK is distributed via Maven Central.
- A valid Scandit license key:
  - Sign in at https://ssl.scandit.com to generate one.
  - No account yet? Sign up at https://ssl.scandit.com/dashboard/sign-up?p=test.
- Camera permission in `AndroidManifest.xml`:
  ```xml
  <uses-feature
      android:name="android.hardware.camera"
      android:required="true" />
  <uses-permission android:name="android.permission.CAMERA" />
  ```
  Request the permission at runtime using the standard Android permission API before scanning starts.

## Integration flow

Ask the user which barcode symbologies they need to scan. When asking, mention that it's important to only enable the symbologies they actually need, as enabling fewer improves scanning performance and accuracy.

Once the user responds, ask them which Activity or Fragment they'd like to integrate BarcodeCapture into. Then write the integration code directly into that file. Do not just show the code in chat; apply it to the file.

After providing the code, show this setup checklist:

**Setup checklist:**
1. Add `implementation "com.scandit.datacapture:barcode:<version>"` and `implementation "com.scandit.datacapture:core:<version>"` to `app/build.gradle` (the version was already fetched and filled in above).
2. Add `<uses-permission android:name="android.permission.CAMERA" />` and the `<uses-feature>` element to `AndroidManifest.xml`.
3. Request the `CAMERA` permission at runtime before scanning starts.
4. Replace `-- ENTER YOUR SCANDIT LICENSE KEY HERE --` with your key from https://ssl.scandit.com.

## Step 1 — Create the DataCaptureContext

The `DataCaptureContext` is the central hub of the SDK. Construct it once and reuse the same reference for the lifetime of the scanning surface.

```kotlin
import com.scandit.datacapture.core.capture.DataCaptureContext

private val dataCaptureContext = DataCaptureContext.forLicenseKey("-- ENTER YOUR SCANDIT LICENSE KEY HERE --")
```

## Step 2 — Configure BarcodeCaptureSettings

Choose which barcode symbologies to scan. By default, all symbologies are disabled — enable each one explicitly. Only enable what you need; each extra symbology adds processing time.

```kotlin
import com.scandit.datacapture.barcode.capture.BarcodeCaptureSettings
import com.scandit.datacapture.barcode.data.Symbology

val settings = BarcodeCaptureSettings().apply {
    enableSymbology(Symbology.EAN13_UPCA, true)
    enableSymbology(Symbology.EAN8, true)
    enableSymbology(Symbology.UPCE, true)
    enableSymbology(Symbology.CODE39, true)
    enableSymbology(Symbology.CODE128, true)
}

// Optional: adjust active symbol counts for variable-length symbologies
val code39Settings = settings.getSymbologySettings(Symbology.CODE39)
code39Settings.activeSymbolCounts = setOf(7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
```

You can also enable a set of symbologies at once:
```kotlin
settings.enableSymbologies(setOf(Symbology.EAN13_UPCA, Symbology.CODE128))
```

### BarcodeCaptureSettings Members

| Member | Type | Description |
|--------|------|-------------|
| `enableSymbology(symbology, enabled)` | method | Enable or disable one symbology. |
| `enableSymbologies(symbologies)` | method | Enable a `Set<Symbology>` in one call. |
| `getSymbologySettings(symbology)` | method | Get per-symbology `SymbologySettings` (e.g. `activeSymbolCounts`). |
| `codeDuplicateFilter` | `TimeInterval` | Time window to suppress duplicate scans of the same code (e.g. `TimeInterval.millis(500)`). `TimeInterval.zero()` reports every detection; `TimeInterval.millis(-1)` reports each code only once until scanning stops. |

## Step 3 — Camera setup

`Camera.getDefaultCamera(cameraSettings)` returns the back camera pre-configured with the recommended settings. Attach it to the context via `setFrameSource`.

```kotlin
import com.scandit.datacapture.barcode.capture.BarcodeCapture
import com.scandit.datacapture.core.source.Camera
import com.scandit.datacapture.core.source.FrameSourceState

private val camera = Camera.getDefaultCamera(BarcodeCapture.createRecommendedCameraSettings())

init {
    dataCaptureContext.setFrameSource(camera)
}
```

Switch the camera on / off:

```kotlin
camera?.switchToDesiredState(FrameSourceState.ON)   // start preview / scanning
camera?.switchToDesiredState(FrameSourceState.OFF)  // release the camera
```

## Step 4 — Create the BarcodeCapture mode

```kotlin
import com.scandit.datacapture.barcode.capture.BarcodeCapture

private val barcodeCapture = BarcodeCapture.forDataCaptureContext(dataCaptureContext, settings)
```

Re-applying settings at runtime is done via `barcodeCapture.applySettings(newSettings)`.

### BarcodeCapture Members

| Member | Description |
|--------|-------------|
| `BarcodeCapture.forDataCaptureContext(context, settings)` | Factory — creates the mode and attaches it to the context. |
| `isEnabled` | Pause / resume scanning without tearing down the camera. |
| `feedback` | `BarcodeCaptureFeedback` — sound / vibration on success. |
| `applySettings(settings)` | Update settings at runtime. |
| `addListener(listener)` / `removeListener(listener)` | Register or remove a `BarcodeCaptureListener`. |
| `BarcodeCapture.createRecommendedCameraSettings()` | Static — returns the recommended `CameraSettings` for BarcodeCapture. |

## Step 5 — DataCaptureView and BarcodeCaptureOverlay

`DataCaptureView.newInstance(context, dataCaptureContext)` creates the camera preview. In an Activity, pass it directly to `setContentView`. `BarcodeCaptureOverlay.newInstance(barcodeCapture, dataCaptureView)` adds the highlight overlay to the view.

```kotlin
import com.scandit.datacapture.core.ui.DataCaptureView
import com.scandit.datacapture.barcode.ui.overlay.BarcodeCaptureOverlay

// In onCreate():
val dataCaptureView = DataCaptureView.newInstance(this, dataCaptureContext)
val overlay = BarcodeCaptureOverlay.newInstance(barcodeCapture, dataCaptureView)
setContentView(dataCaptureView)
```

In a Fragment, add the view to a container in your layout:
```kotlin
val dataCaptureView = DataCaptureView.newInstance(requireContext(), dataCaptureContext)
BarcodeCaptureOverlay.newInstance(barcodeCapture, dataCaptureView)
binding.scannerContainer.addView(dataCaptureView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
```

### BarcodeCaptureOverlay Members

| Member | Description |
|--------|-------------|
| `BarcodeCaptureOverlay.newInstance(mode, view)` | Factory — creates the overlay and adds it to the view. |
| `brush` | `Brush` — fill / stroke for recognized-barcode highlights. |
| `viewfinder` | `Viewfinder?` — optional viewfinder drawn on the preview. |

## Step 6 — Implement BarcodeCaptureListener

Implement `BarcodeCaptureListener` on the Activity or a dedicated controller class.

```kotlin
import com.scandit.datacapture.barcode.capture.BarcodeCaptureListener
import com.scandit.datacapture.barcode.capture.BarcodeCaptureSession
import com.scandit.datacapture.core.data.FrameData

class BarcodeScanActivity : AppCompatActivity(), BarcodeCaptureListener {

    override fun onBarcodeScanned(
        barcodeCapture: BarcodeCapture,
        session: BarcodeCaptureSession,
        data: FrameData
    ) {
        val barcode = session.newlyRecognizedBarcode ?: return

        // Disable while we handle the scan, so duplicates don't fire.
        barcodeCapture.isEnabled = false

        // onBarcodeScanned is called on a background thread — dispatch UI work.
        runOnUiThread {
            // Handle the barcode: barcode.data, barcode.symbology
        }
    }

    override fun onSessionUpdated(
        barcodeCapture: BarcodeCapture,
        session: BarcodeCaptureSession,
        data: FrameData
    ) {
        // Called every frame; keep this fast.
    }

    override fun onObservationStarted(barcodeCapture: BarcodeCapture) {}
    override fun onObservationStopped(barcodeCapture: BarcodeCapture) {}
}
```

### BarcodeCaptureListener Interface

| Callback | Description |
|----------|-------------|
| `onBarcodeScanned(barcodeCapture, session, data)` | A barcode was recognized. Read it from `session.newlyRecognizedBarcode`. Called on a background thread. |
| `onSessionUpdated(barcodeCapture, session, data)` | Called for every processed frame. Keep work minimal. |
| `onObservationStarted(barcodeCapture)` | Listener was added. |
| `onObservationStopped(barcodeCapture)` | Listener was removed. |

### BarcodeCaptureSession Properties

| Property | Type | Description |
|----------|------|-------------|
| `newlyRecognizedBarcode` | `Barcode?` | The barcode just scanned. |
| `newlyLocalizedBarcodes` | `List<LocalizedOnlyBarcode>` | Codes that were located but not decoded. |
| `frameSequenceId` | `Long` | Identifier of the current frame sequence. |

## Step 7 — Lifecycle management

Drive the camera from `onResume` and `onPause`. The camera must not be active while the app is in the background.

```kotlin
override fun onResume() {
    super.onResume()
    // Re-enable after returning from background.
    barcodeCapture.isEnabled = true
    camera?.switchToDesiredState(FrameSourceState.ON)
}

override fun onPause() {
    barcodeCapture.isEnabled = false
    camera?.switchToDesiredState(FrameSourceState.OFF)
    super.onPause()
}

override fun onDestroy() {
    barcodeCapture.removeListener(this)
    dataCaptureContext.removeCurrentMode()
    super.onDestroy()
}
```

## Complete minimal example

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.scandit.datacapture.barcode.capture.*
import com.scandit.datacapture.barcode.data.Symbology
import com.scandit.datacapture.barcode.ui.overlay.BarcodeCaptureOverlay
import com.scandit.datacapture.core.capture.DataCaptureContext
import com.scandit.datacapture.core.data.FrameData
import com.scandit.datacapture.core.source.Camera
import com.scandit.datacapture.core.source.FrameSourceState
import com.scandit.datacapture.core.ui.DataCaptureView

class BarcodeScanActivity : AppCompatActivity(), BarcodeCaptureListener {

    private val dataCaptureContext =
        DataCaptureContext.forLicenseKey("-- ENTER YOUR SCANDIT LICENSE KEY HERE --")

    private val camera = Camera.getDefaultCamera(BarcodeCapture.createRecommendedCameraSettings())

    private val barcodeCapture: BarcodeCapture

    init {
        dataCaptureContext.setFrameSource(camera)

        val settings = BarcodeCaptureSettings().apply {
            enableSymbology(Symbology.EAN13_UPCA, true)
            enableSymbology(Symbology.CODE128, true)
        }

        barcodeCapture = BarcodeCapture.forDataCaptureContext(dataCaptureContext, settings)
        barcodeCapture.addListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataCaptureView = DataCaptureView.newInstance(this, dataCaptureContext)
        BarcodeCaptureOverlay.newInstance(barcodeCapture, dataCaptureView)
        setContentView(dataCaptureView)
        // Request CAMERA permission here before scanning starts
    }

    override fun onResume() {
        super.onResume()
        barcodeCapture.isEnabled = true
        camera?.switchToDesiredState(FrameSourceState.ON)
    }

    override fun onPause() {
        barcodeCapture.isEnabled = false
        camera?.switchToDesiredState(FrameSourceState.OFF)
        super.onPause()
    }

    override fun onDestroy() {
        barcodeCapture.removeListener(this)
        dataCaptureContext.removeCurrentMode()
        super.onDestroy()
    }

    override fun onBarcodeScanned(
        barcodeCapture: BarcodeCapture,
        session: BarcodeCaptureSession,
        data: FrameData
    ) {
        val barcode = session.newlyRecognizedBarcode ?: return
        barcodeCapture.isEnabled = false
        runOnUiThread {
            // handle barcode.data and barcode.symbology
        }
    }

    override fun onSessionUpdated(barcodeCapture: BarcodeCapture, session: BarcodeCaptureSession, data: FrameData) {}
    override fun onObservationStarted(barcodeCapture: BarcodeCapture) {}
    override fun onObservationStopped(barcodeCapture: BarcodeCapture) {}
}
```

## Optional configuration

### BarcodeCaptureFeedback

By default, BarcodeCapture beeps and vibrates on success. To customize feedback, replace `barcodeCapture.feedback`:

```kotlin
import com.scandit.datacapture.barcode.capture.BarcodeCaptureFeedback

// Suppress all feedback (silent mode):
barcodeCapture.feedback = BarcodeCaptureFeedback()

// Restore defaults:
barcodeCapture.feedback = BarcodeCaptureFeedback.defaultFeedback()
```

For per-result feedback (e.g. success sound only, no vibration), fetch the Advanced Configurations page — the exact `Feedback` constructor arguments need to be verified against the docs.

### Viewfinder

Attach a viewfinder to the overlay to draw a guide on the preview:

```kotlin
import com.scandit.datacapture.core.ui.viewfinder.RectangularViewfinder
import com.scandit.datacapture.core.ui.viewfinder.RectangularViewfinderStyle

overlay.viewfinder = RectangularViewfinder(RectangularViewfinderStyle.SQUARE)
```

### CodeDuplicateFilter

Suppress duplicate scans of the same code within a time window. `-1` reports each code only once until scanning is stopped; `0` reports every detection. The value is a `TimeInterval`.

```kotlin
import com.scandit.datacapture.core.time.TimeInterval

settings.codeDuplicateFilter = TimeInterval.millis(500)  // suppress duplicates within 500 ms
```

Set this before calling `BarcodeCapture.forDataCaptureContext(dataCaptureContext, settings)`. To change at runtime, use `barcodeCapture.applySettings(newSettings)`.

### LocationSelection

To restrict scanning to a sub-area of the preview, fetch the [Advanced Configurations](https://docs.scandit.com/sdks/android/barcode-capture/advanced/) page for the `RectangularLocationSelection` API — the exact method signatures need to be verified against the live docs.

### Composite codes

Composite codes (linear + 2D companion) require both symbologies and composite types to be enabled. Fetch the Advanced Configurations page for the exact API.

## Key Rules

1. **One context per scanning surface** — construct `DataCaptureContext.forLicenseKey(key)` once and reuse it.
2. **Factory wires the mode** — `BarcodeCapture.forDataCaptureContext(context, settings)` both creates the mode and attaches it to the context.
3. **Listener thread** — `onBarcodeScanned` runs on a background thread; always dispatch UI work via `runOnUiThread {}`.
4. **Disable inside onBarcodeScanned** — set `barcodeCapture.isEnabled = false` before doing any non-trivial work to avoid duplicate scans.
5. **Camera lifecycle** — turn the camera off in `onPause()`, back on in `onResume()`. Call `dataCaptureContext.removeCurrentMode()` in `onDestroy()`.
6. **Overlay is explicit** — `BarcodeCaptureOverlay.newInstance(barcodeCapture, view)` adds the overlay to the view in one step. There is no implicit overlay.
7. **Runtime permission** — add `CAMERA` to the manifest and request it at runtime before the first scan.
8. **Symbologies** — enable only what's needed. Variable-length 1D symbologies (Code39, Code128, ITF) may need `activeSymbolCounts` adjusted.
9. **Settings before construction** — configure `BarcodeCaptureSettings` before passing to `forDataCaptureContext`. To change at runtime, use `barcodeCapture.applySettings(newSettings)`.
