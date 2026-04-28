# Label Capture Capacitor Integration Guide

Label Capture (Smart Label Capture) extracts multiple fields from a single label in one scan — e.g. a barcode, an expiry date, and a total price on a grocery label.

## Prerequisites

- Scandit Capacitor packages installed:
  - `scandit-capacitor-datacapture-core`
  - `scandit-capacitor-datacapture-barcode`
  - `scandit-capacitor-datacapture-label`
- After installing, run `npx cap sync` and `cd ios/App && pod install` for iOS.
- Capacitor `>=5`, `@capacitor/ios >=5`, `@capacitor/android >=5`.
- A valid Scandit license key from <https://ssl.scandit.com>.
- Camera permissions:
  - iOS: `NSCameraUsageDescription` in `ios/App/App/Info.plist`.
  - Android: declared automatically by the plugin; request at runtime if `minSdkVersion >= 23`.

## Interactive Label Definition

Before writing any code, walk the user through their label. Ask one question at a time.

**Question A — What's on your label?** Show the field-type catalogue:

*Barcode fields:* `CustomBarcode`, `ImeiOneBarcode`, `ImeiTwoBarcode`, `PartNumberBarcode`, `SerialNumberBarcode`.

*Text fields (preset recognisers):* `ExpiryDateText`, `PackingDateText`, `DateText`, `WeightText`, `UnitPriceText`, `TotalPriceText`.

*Text fields (custom):* `CustomText` — any text, user provides a regex.

**Question B — For each selected field:**
- Required or optional?
- For `CustomBarcode`: which **symbologies**? Mention to the user that enabling only the symbologies they actually need improves scanning performance and accuracy.
- For `CustomText`: what **regex pattern**? (`field.valueRegex = '<pattern>'`)

**Question C — Which file should the integration code go in?** Then write the code directly into that file.

After writing the code, show this setup checklist:

1. `npm install scandit-capacitor-datacapture-core scandit-capacitor-datacapture-barcode scandit-capacitor-datacapture-label`
2. `npx cap sync`
3. iOS: `cd ios/App && pod install`. Add `NSCameraUsageDescription` to `Info.plist`.
4. Replace `'-- ENTER YOUR SCANDIT LICENSE KEY HERE --'` with your key.

## Step 1 — Initialize plugins, then the DataCaptureContext

```javascript
import { ScanditCaptureCorePlugin, DataCaptureContext } from 'scandit-capacitor-datacapture-core';

async function runApp() {
  await ScanditCaptureCorePlugin.initializePlugins();
  const context = DataCaptureContext.initialize('-- ENTER YOUR SCANDIT LICENSE KEY HERE --');
  // …rest of setup below…
}

runApp();
```

`initializePlugins()` wires the underlying Capacitor bridge. **It must run before any other Scandit API call**, including `DataCaptureContext.initialize`.

## Step 2 — Define the label fields (class-based)

Field definitions on Capacitor are class-based, same shape as RN/Cordova. **Do not use builders or factory functions** — those are web-only.

```javascript
import { Symbology } from 'scandit-capacitor-datacapture-barcode';
import {
  CustomBarcode,
  ExpiryDateText,
  TotalPriceText,
  LabelDefinition,
} from 'scandit-capacitor-datacapture-label';

const barcode = CustomBarcode.initWithNameAndSymbologies('Barcode', [
  Symbology.EAN13UPCA,
  Symbology.Code128,
]);
barcode.optional = false;

const expiry = new ExpiryDateText('Expiry Date');
expiry.optional = false;

const total = new TotalPriceText('Total Price');
total.optional = true;

const labelDefinition = new LabelDefinition('Perishable Product');
labelDefinition.fields = [barcode, expiry, total];
```

**Field constructors at a glance:**

| Field type | Constructor |
|---|---|
| `CustomBarcode` | `CustomBarcode.initWithNameAndSymbologies(name, [Symbology.X, ...])` |
| `ImeiOneBarcode` / `ImeiTwoBarcode` | `ImeiOneBarcode.initWithNameAndSymbologies(name, [...])` |
| `PartNumberBarcode` / `SerialNumberBarcode` | `SerialNumberBarcode.initWithNameAndSymbologies(name, [...])` |
| `ExpiryDateText` / `PackingDateText` / `DateText` | `new ExpiryDateText(name)` (etc.) |
| `WeightText` / `UnitPriceText` / `TotalPriceText` | `new WeightText(name)` (etc.) |
| `CustomText` | `new CustomText(name)` then `field.valueRegex = '<pattern>'` |

## Step 3 — Build LabelCaptureSettings

```javascript
import { LabelCaptureSettings } from 'scandit-capacitor-datacapture-label';

const settings = LabelCaptureSettings.settingsFromLabelDefinitions([labelDefinition], {});
```

Do **not** use `LabelCaptureSettingsBuilder` — it does not exist on Capacitor.

## Step 4 — Create the LabelCapture mode and bind it to the context

```javascript
import { LabelCapture } from 'scandit-capacitor-datacapture-label';

const labelCapture = new LabelCapture(settings);
context.setMode(labelCapture);
```

## Step 5 — Configure the recommended camera

```javascript
import { Camera, FrameSourceState } from 'scandit-capacitor-datacapture-core';

const camera = Camera.withSettings(LabelCapture.createRecommendedCameraSettings());
context.setFrameSource(camera);
```

## Step 6 — Embed `DataCaptureView` and add the overlay

```html
<!-- index.html -->
<div id="data-capture-view" style="position:fixed; inset:0;"></div>
```

```javascript
import { DataCaptureView } from 'scandit-capacitor-datacapture-core';
import { LabelCaptureBasicOverlay } from 'scandit-capacitor-datacapture-label';

const view = DataCaptureView.forContext(context);
view.connectToElement(document.getElementById('data-capture-view'));

const basicOverlay = new LabelCaptureBasicOverlay(labelCapture);
await view.addOverlay(basicOverlay);

// Native scan UI is BEHIND the WebView. While scanning, let the native overlays receive touches:
view.webViewContentOnTop = false;
```

## Step 7 — Validation Flow (optional)

```javascript
import { LabelCaptureValidationFlowOverlay } from 'scandit-capacitor-datacapture-label';

const validationFlowOverlay = new LabelCaptureValidationFlowOverlay(labelCapture);

validationFlowOverlay.listener = {
  didCaptureLabelWithFields(fields) {
    labelCapture.isEnabled = false;
    // Show DOM modal on top of the native view:
    view.webViewContentOnTop = true;
    showResult(fields);
  },
  didSubmitManualInputForField(field, oldValue, newValue) {
    // 8.2+ — fires when the user manually corrects or enters a field value.
  },
  // 8.4+ — optional. Implement only for fine-grained progress feedback.
  async didUpdateValidationFlowResult(type, asyncId, fields, getFrameData) {
    // Fires multiple times during capture as fields accumulate.
  },
};

await view.addOverlay(validationFlowOverlay);
```

> **Listener naming.** On Capacitor the methods are iOS-style: `didCaptureLabelWithFields`, `didSubmitManualInputForField`, `didUpdateValidationFlowResult`. Web names (`onValidationFlowLabelCaptured`, `onManualInput`) do not exist on Capacitor.

> **Version-availability.** `didCaptureLabelWithFields` is always available. `didSubmitManualInputForField` shipped in **v8.2**. `didUpdateValidationFlowResult` shipped in **v8.4**. Detect the installed version (read `package.json`) before suggesting the newer methods.

> **`webViewContentOnTop` toggle.** Set to `false` while scanning so the native overlay receives touches. Set to `true` whenever you show DOM-based UI (modal, alert) over the scan view. Toggle back to `false` when resuming scanning.

## Step 8 — Result handling without the Validation Flow

```javascript
labelCapture.addListener({
  didUpdateSession(_mode, session) {
    for (const captured of session.capturedLabels) {
      for (const field of captured.fields) {
        const value = field.barcode?.data ?? field.text;
        console.log(`${field.name} = ${value}`);
      }
    }
  },
});
```

## Step 9 — Lifecycle (App pause/resume + cleanup)

Use the `App` plugin from Capacitor to handle background/foreground transitions:

```javascript
import { App } from '@capacitor/app';

let wasOn = false;
App.addListener('appStateChange', async ({ isActive }) => {
  if (!isActive) {
    wasOn = (await camera.getCurrentState()) === FrameSourceState.On;
    await camera.switchToDesiredState(FrameSourceState.Off);
  } else if (wasOn) {
    await camera.switchToDesiredState(FrameSourceState.On);
  }
});
```

Do **not** call `context.dispose()` — the singleton context lives for the entire app lifetime.

## Step 10 — Complete working example

```javascript
import { Camera, DataCaptureContext, DataCaptureView, FrameSourceState, ScanditCaptureCorePlugin } from 'scandit-capacitor-datacapture-core';
import { Symbology } from 'scandit-capacitor-datacapture-barcode';
import {
  CustomBarcode,
  ExpiryDateText,
  LabelCapture,
  LabelCaptureBasicOverlay,
  LabelCaptureSettings,
  LabelCaptureValidationFlowOverlay,
  LabelDefinition,
  TotalPriceText,
} from 'scandit-capacitor-datacapture-label';

let labelCapture;

async function runApp() {
  await ScanditCaptureCorePlugin.initializePlugins();
  const context = DataCaptureContext.initialize('-- ENTER YOUR SCANDIT LICENSE KEY HERE --');

  const camera = Camera.withSettings(LabelCapture.createRecommendedCameraSettings());
  context.setFrameSource(camera);

  const barcode = CustomBarcode.initWithNameAndSymbologies('Barcode', [
    Symbology.EAN13UPCA,
    Symbology.Code128,
  ]);
  barcode.optional = false;

  const expiry = new ExpiryDateText('Expiry Date');
  expiry.optional = false;

  const total = new TotalPriceText('Total Price');
  total.optional = true;

  const labelDefinition = new LabelDefinition('Perishable Product');
  labelDefinition.fields = [barcode, expiry, total];

  const settings = LabelCaptureSettings.settingsFromLabelDefinitions([labelDefinition], {});
  labelCapture = new LabelCapture(settings);
  context.setMode(labelCapture);

  const view = DataCaptureView.forContext(context);
  view.connectToElement(document.getElementById('data-capture-view'));

  const basicOverlay = new LabelCaptureBasicOverlay(labelCapture);
  await view.addOverlay(basicOverlay);

  const validationFlowOverlay = new LabelCaptureValidationFlowOverlay(labelCapture);
  validationFlowOverlay.listener = {
    didCaptureLabelWithFields(fields) {
      labelCapture.isEnabled = false;
      view.webViewContentOnTop = true;
      showResult(formatLabelFields(fields));
    },
    didSubmitManualInputForField() {},
    async didUpdateValidationFlowResult() {},
  };
  await view.addOverlay(validationFlowOverlay);

  view.webViewContentOnTop = false;
  await camera.switchToDesiredState(FrameSourceState.On);
  labelCapture.isEnabled = true;
}

function formatLabelFields(fields) {
  return fields
    .map((f) => {
      const value = f.barcode?.data ?? (f.date ? `${f.date.day}-${f.date.month}-${f.date.year}` : f.text ?? 'N/A');
      return `${f.name}: ${value}`;
    })
    .join('\n');
}

function showResult(text) {
  document.getElementById('modal-message').textContent = text;
  document.getElementById('result-modal').classList.remove('hidden');
}

runApp();
```

## Key Rules

- **`initializePlugins()` first.** Always `await ScanditCaptureCorePlugin.initializePlugins()` before `DataCaptureContext.initialize(licenseKey)`.
- **`webViewContentOnTop` toggle.** `false` while scanning (so native overlays receive touches), `true` when showing DOM modals.
- **Class-based field API only.** `CustomBarcode.initWithNameAndSymbologies(...)`, `new ExpiryDateText(name)`, `field.optional = true`, `LabelCaptureSettings.settingsFromLabelDefinitions([...], {})`. No builders, no factory functions.
- **iOS-style listener names.** `didCaptureLabelWithFields`, `didSubmitManualInputForField` (8.2+), `didUpdateValidationFlowResult` (8.4+). Never `onValidationFlowLabelCaptured` / `onManualInput` (web).
- **Singleton context.** `DataCaptureContext.initialize(licenseKey)` once. Never `dispose()` it.
- **License key in source is a placeholder** (`'-- ENTER YOUR SCANDIT LICENSE KEY HERE --'`).

## Where to Go Next

- [Label Definitions](https://docs.scandit.com/sdks/capacitor/label-capture/label-definitions/)
- [Advanced Configurations](https://docs.scandit.com/sdks/capacitor/label-capture/advanced/)
- [LabelCaptureSimpleSample (Capacitor)](https://github.com/Scandit/datacapture-capacitor-samples/tree/master/03_Advanced_Batch_Scanning_Samples/05_Smart_Label_Capture/LabelCaptureSimpleSample)
