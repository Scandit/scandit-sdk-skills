# Label Capture Android Integration Guide

Label Capture (Smart Label Capture) extracts multiple fields from a single label in one scan — e.g. a barcode, an expiry date, and a total price on a grocery label. You declare the structure of the label (which fields, required/optional, barcode symbologies or text regex) and the SDK returns all matched fields per frame.

## Prerequisites

- Android Studio with Kotlin support.
- A valid Scandit license key:
  - Sign in at <https://ssl.scandit.com> to generate one.
  - No account yet? Sign up at <https://ssl.scandit.com/dashboard/sign-up?p=test>.

### Gradle setup

Add the required dependencies. The exact modules depend on which field types the label uses.

**app/build.gradle.kts** (or `app/build.gradle`):
```kotlin
dependencies {
    implementation("com.scandit.datacapture:core:<version>")
    implementation("com.scandit.datacapture:barcode:<version>")
    implementation("com.scandit.datacapture:label:<version>")
    // Required when the label includes any text field (expiry date, price, weight, custom text):
    implementation("com.scandit.datacapture:label-text-models:<version>")
}
```

Replace `<version>` with the current SDK version (e.g. `8.3.1`). All four artifacts must use the same version. Read `app/build.gradle` or `app/build.gradle.kts` to find the version already in use.

### Camera permission

Add to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.CAMERA" />
```

Request the `CAMERA` permission at runtime before starting the camera (use `ActivityResultContracts.RequestPermission` or the legacy `requestPermissions` approach).

## Interactive Label Definition

Before writing any code, walk the user through their label. Ask one question at a time.

**Question A — What's on your label?** Present this checklist of supported field types and ask the user to pick everything that applies.

*Barcode fields:*
- `addCustomBarcode()` — any barcode, user chooses symbologies
- `addSerialNumberBarcode()` — serial number (preset symbologies and regex)
- `addPartNumberBarcode()` — part number (preset symbologies and regex)

*Text fields (preset recognisers):*
- `addExpiryDateText()` — expiry date (with optional date format)
- `addTotalPriceText()` — total price
- `addUnitPriceText()` — unit price
- `addWeightText()` — weight

*Text fields (custom):*
- `addCustomText()` — any text, user provides a value regex

**Question B — For each selected field:**
- Is it **required** or **optional**? (required = label is not considered captured until this field matches; optional = captured when/if it matches)
- For `addCustomBarcode()`: which **symbologies**? Mention to the user that enabling only the symbologies they actually need improves scanning performance and accuracy. Android symbology names use underscores: `Symbology.EAN13_UPCA`, `Symbology.CODE128`, `Symbology.GS1_DATABAR_EXPANDED`, etc.
- For `addCustomText()`: what **value regex** should the text match?
- For `addExpiryDateText()`: does the user need a specific date format? If so, ask for the component order (MDY, DMY, YMD, etc.) and whether partial dates are accepted.

**Question C — Which file should the integration code go in?** Then write the code directly into that file. Do not just show it in chat.

## Minimal Integration (Android / Kotlin)

Once the user has answered Questions A, B, and C, generate the integration code. Substitute the placeholder field and label names based on the user's answers. Fields marked optional should call `.isOptional(true)` on the builder chain; required fields can omit it (required is the default).

```kotlin
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.scandit.datacapture.barcode.data.Symbology
import com.scandit.datacapture.core.capture.DataCaptureContext
import com.scandit.datacapture.core.data.FrameData
import com.scandit.datacapture.core.source.Camera
import com.scandit.datacapture.core.source.FrameSourceState
import com.scandit.datacapture.core.ui.DataCaptureView
import com.scandit.datacapture.label.capture.LabelCapture
import com.scandit.datacapture.label.capture.LabelCaptureListener
import com.scandit.datacapture.label.capture.LabelCaptureSession
import com.scandit.datacapture.label.capture.LabelCaptureSettings
import com.scandit.datacapture.label.ui.overlay.LabelCaptureBasicOverlay

class ScanActivity : AppCompatActivity() {

    private lateinit var dataCaptureContext: DataCaptureContext
    private lateinit var labelCapture: LabelCapture
    private lateinit var camera: Camera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        dataCaptureContext = DataCaptureContext.forLicenseKey("-- ENTER YOUR SCANDIT LICENSE KEY HERE --")

        val settings = LabelCaptureSettings.builder()
            .addLabel()
                .addCustomBarcode()
                    .setSymbologies(Symbology.EAN13_UPCA, Symbology.CODE128)
                .buildFluent("barcode")
                .addExpiryDateText()
                .buildFluent("expiry-date")
            .buildFluent("perishable-product")
            .build()

        labelCapture = LabelCapture.forDataCaptureContext(dataCaptureContext, settings)

        labelCapture.addListener(object : LabelCaptureListener {
            override fun onSessionUpdated(
                mode: LabelCapture,
                session: LabelCaptureSession,
                data: FrameData,
            ) {
                val capturedLabel = session.capturedLabels.firstOrNull() ?: return
                val barcodeData = capturedLabel.fields
                    .find { it.name == "barcode" }?.barcode?.data
                val expiryDate = capturedLabel.fields
                    .find { it.name == "expiry-date" }?.asDate()

                mode.isEnabled = false
                // Process barcodeData and expiryDate on the main thread
            }
        })

        val dataCaptureView = DataCaptureView.newInstance(this, dataCaptureContext)
        val container = findViewById<FrameLayout>(R.id.data_capture_container)
        container.addView(
            dataCaptureView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        LabelCaptureBasicOverlay.newInstance(labelCapture, dataCaptureView)

        camera = Camera.getDefaultCamera(LabelCapture.createRecommendedCameraSettings())
            ?: throw IllegalStateException("Failed to init camera!")
        dataCaptureContext.setFrameSource(camera)
    }

    override fun onResume() {
        super.onResume()
        camera.switchToDesiredState(FrameSourceState.ON)
    }

    override fun onPause() {
        super.onPause()
        camera.switchToDesiredState(FrameSourceState.OFF)
    }
}
```

Notes when generating this code:

- Import ONLY the field builders the user actually selected. Do not import unused ones.
- The builder method on `LabelCaptureSettings.builder().addLabel()` mirrors the field type: `addCustomBarcode()`, `addExpiryDateText()`, `addTotalPriceText()`, `addUnitPriceText()`, `addWeightText()`, `addCustomText()`, `addSerialNumberBarcode()`, `addPartNumberBarcode()`. Each is chained and terminated with `.buildFluent("<field-name>")`.
- Android symbology enum values use underscores: `Symbology.EAN13_UPCA`, `Symbology.CODE128`, `Symbology.GS1_DATABAR_EXPANDED`, `Symbology.QR`, `Symbology.DATA_MATRIX`, etc. Do NOT use camelCase forms.
- For `addCustomBarcode()`, use `.setSymbologies(Symbology.X, Symbology.Y, ...)` (vararg). For a single symbology, `.setSymbologies(Symbology.X)` is fine.
- For `addCustomText()`, use `.setValueRegexes("<pattern>")` (vararg). For multiple patterns, `.setValueRegexes("pattern1", "pattern2")`. Do NOT use `.setPatterns` or `.setPattern` — those names were renamed and no longer exist in v8+.
- For `addExpiryDateText()`, optionally call `.setLabelDateFormat(LabelDateFormat(LabelDateComponentFormat.MDY, false))` to control date parsing. Import `LabelDateFormat` and `LabelDateComponentFormat` from `com.scandit.datacapture.label.data`.
- Text fields (expiry date, price, weight, custom text) require the `label-text-models` Gradle artifact. If the user's label has only barcode fields, that artifact is not needed.
- `onSessionUpdated` is called on a background thread. Dispatch any UI updates to the main thread (e.g. with `runOnUiThread { }` or a coroutine).
- Set `mode.isEnabled = false` after a successful capture to prevent duplicate results.
- Re-enable the mode (e.g. `labelCapture.isEnabled = true`) when the user is ready to scan again.
- In a Fragment-based setup replace `AppCompatActivity` lifecycle hooks with Fragment lifecycle hooks; the camera and overlay setup logic is identical.

## Setup Checklist

After writing the integration code, show this checklist:

1. Add the four Gradle dependencies to `app/build.gradle.kts` (see Prerequisites above). Use the same version for all.
2. Replace `-- ENTER YOUR SCANDIT LICENSE KEY HERE --` with your key from <https://ssl.scandit.com>.
3. Add the `CAMERA` permission to `AndroidManifest.xml` and request it at runtime before starting the camera.
4. Add a `FrameLayout` (or other container) with the id `data_capture_container` to your layout XML.

## Optional: Validation Flow

If the user wants a guided scanning experience with a live checklist of captured/missing fields, and the ability to manually enter values for missed fields without rescanning, enable the Validation Flow. Skip this section if the user is fine with the minimal scan-and-handle path above.

Replace `LabelCaptureBasicOverlay` with `LabelCaptureValidationFlowOverlay`, and implement `LabelCaptureValidationFlowListener` instead of `LabelCaptureListener`.

```kotlin
import com.scandit.datacapture.label.data.LabelField
import com.scandit.datacapture.label.ui.overlay.validation.LabelCaptureValidationFlowListener
import com.scandit.datacapture.label.ui.overlay.validation.LabelCaptureValidationFlowOverlay
import com.scandit.datacapture.label.ui.overlay.validation.LabelCaptureValidationFlowSettings

// In onCreate, after creating dataCaptureView:
val validationFlowSettings = LabelCaptureValidationFlowSettings.newInstance()
// Optional: set placeholder text shown in the manual-entry field for each label field
validationFlowSettings.setPlaceholderTextForLabelDefinition("expiry-date", "MM/DD/YYYY")

val validationFlowOverlay = LabelCaptureValidationFlowOverlay.newInstance(
    this,
    labelCapture,
    dataCaptureView,
)
validationFlowOverlay.applySettings(validationFlowSettings)
validationFlowOverlay.listener = object : LabelCaptureValidationFlowListener {
    override fun onValidationFlowLabelCaptured(fields: List<LabelField>) {
        val barcodeData = fields.find { it.name == "barcode" }?.barcode?.data
        val expiryDate = fields.find { it.name == "expiry-date" }?.asDate()
        // All required fields are confirmed — process results on the main thread
    }
}
```

Add lifecycle delegation so the Validation Flow overlay can manage its own UI state:

```kotlin
override fun onPause() {
    super.onPause()
    camera.switchToDesiredState(FrameSourceState.OFF)
    validationFlowOverlay.onPause()
}

override fun onResume() {
    super.onResume()
    camera.switchToDesiredState(FrameSourceState.ON)
    validationFlowOverlay.onResume()
}
```

You can also customize button labels and toast messages via `LabelCaptureValidationFlowSettings` properties:

```kotlin
validationFlowSettings.restartButtonText = "Restart"
validationFlowSettings.pauseButtonText = "Pause"
validationFlowSettings.finishButtonText = "Finish"
validationFlowSettings.standbyHintText = "No label detected, camera paused"
validationFlowSettings.validationHintText = "data fields collected"
validationFlowSettings.validationErrorText = "Incorrect format."
validationFlowSettings.scanningText = "Scan in progress"
validationFlowSettings.adaptiveScanningText = "Processing"
```

## Where to Go Next

After the core integration is running, point the user at the right resource for follow-ups:

- [Label Definitions](https://docs.scandit.com/sdks/android/label-capture/label-definitions/) — full catalogue of pre-built text/barcode field types and how to tune their regex anchors and value patterns.
- [Advanced Configurations](https://docs.scandit.com/sdks/android/label-capture/advanced/) — Validation Flow customisation, adaptive recognition, custom overlays.
- [LabelCaptureSimpleSample](https://github.com/Scandit/datacapture-android-samples/tree/master/03_Advanced_Batch_Scanning_Samples/05_Smart_Label_Capture/LabelCaptureSimpleSample) — working reference sample.
