# Label Capture Cordova Integration Guide

Label Capture (Smart Label Capture) extracts multiple fields from a single label in one scan — e.g. a barcode, an expiry date, and a total price on a grocery label.

## Prerequisites

- Scandit Cordova plugins installed:
  - `scandit-cordova-datacapture-core`
  - `scandit-cordova-datacapture-barcode`
  - `scandit-cordova-datacapture-label`
- After installing, run `cordova prepare ios` and `cordova prepare android`. For iOS, a fresh `pod install` inside `platforms/ios/` may be required.
- Cordova `>=11`, `cordova-ios >=6.2`, `cordova-android >=10`.
- A valid Scandit license key from <https://ssl.scandit.com>.
- Camera permissions:
  - iOS: `NSCameraUsageDescription` in `Info.plist` (or via `<config-file>` in `config.xml`).
  - Android: declared automatically by the plugin; request at runtime via `cordova.plugins.diagnostic` (or similar) if `minSdkVersion >= 23`.

## Interactive Label Definition

Before writing any code, walk the user through their label. Ask one question at a time.

**Question A — What's on your label?** Show the field-type catalogue:

*Barcode fields:* `Scandit.CustomBarcode`, `Scandit.ImeiOneBarcode`, `Scandit.ImeiTwoBarcode`, `Scandit.PartNumberBarcode`, `Scandit.SerialNumberBarcode`.

*Text fields (preset recognisers):* `Scandit.ExpiryDateText`, `Scandit.PackingDateText`, `Scandit.DateText`, `Scandit.WeightText`, `Scandit.UnitPriceText`, `Scandit.TotalPriceText`.

*Text fields (custom):* `Scandit.CustomText` — any text, user provides a regex.

**Question B — For each selected field:**
- Required or optional?
- For `CustomBarcode`: which **symbologies**? Mention to the user that enabling only the symbologies they actually need improves scanning performance and accuracy.
- For `CustomText`: what **regex pattern**? (`field.valueRegex = '<pattern>'`)

**Question C — Which file should the integration code go in?** (typically `www/js/index.js` for the default Cordova template). Then write the code directly into that file.

After writing the code, show this setup checklist:

1. `cordova plugin add scandit-cordova-datacapture-core scandit-cordova-datacapture-barcode scandit-cordova-datacapture-label`
2. `cordova prepare ios && cordova prepare android`
3. iOS: `cd platforms/ios && pod install`
4. Add `NSCameraUsageDescription` to `Info.plist` (or `<config-file>` in `config.xml`).
5. Replace `'-- ENTER YOUR SCANDIT LICENSE KEY HERE --'` with your key.

## Step 1 — Initialize DataCaptureContext (after `deviceready`)

All Scandit code must run after the `deviceready` event — the `Scandit.*` global is not populated before then.

```javascript
document.addEventListener('deviceready', () => {
  const context = Scandit.DataCaptureContext.initialize('-- ENTER YOUR SCANDIT LICENSE KEY HERE --');
  // …rest of setup below…
}, false);
```

`Scandit.DataCaptureContext.initialize(licenseKey)` returns the singleton context. Do **not** call it more than once and do **not** construct additional contexts.

## Step 2 — Define the label fields (class-based)

Field definitions on Cordova are class-based, same shape as RN/Capacitor. **Do not use builders or factory functions** — those are web-only.

```javascript
const barcode = Scandit.CustomBarcode.initWithNameAndSymbologies('Barcode', [
  Scandit.Symbology.EAN13UPCA,
  Scandit.Symbology.Code128,
]);
barcode.optional = false;

const expiry = new Scandit.ExpiryDateText('Expiry Date');
expiry.optional = false;
expiry.labelDateFormat = new Scandit.LabelDateFormat(Scandit.LabelDateComponentFormat.MDY, false);

const total = new Scandit.TotalPriceText('Total Price');
total.optional = true;

const labelDefinition = new Scandit.LabelDefinition('Perishable Product');
labelDefinition.fields = [barcode, expiry, total];
```

**Field constructors at a glance:**

| Field type | Constructor |
|---|---|
| `CustomBarcode` | `Scandit.CustomBarcode.initWithNameAndSymbologies(name, [Scandit.Symbology.X, ...])` |
| `ImeiOneBarcode` / `ImeiTwoBarcode` | `Scandit.ImeiOneBarcode.initWithNameAndSymbologies(name, [...])` |
| `PartNumberBarcode` / `SerialNumberBarcode` | `Scandit.SerialNumberBarcode.initWithNameAndSymbologies(name, [...])` |
| `ExpiryDateText` / `PackingDateText` / `DateText` | `new Scandit.ExpiryDateText(name)` (etc.) |
| `WeightText` / `UnitPriceText` / `TotalPriceText` | `new Scandit.WeightText(name)` (etc.) |
| `CustomText` | `new Scandit.CustomText(name)` then `field.valueRegex = '<pattern>'` |

## Step 3 — Build LabelCaptureSettings

```javascript
const settings = Scandit.LabelCaptureSettings.settingsFromLabelDefinitions([labelDefinition], {});
```

Do **not** use `LabelCaptureSettingsBuilder` — it does not exist on Cordova.

## Step 4 — Create the LabelCapture mode and bind it to the context

```javascript
const labelCapture = new Scandit.LabelCapture(settings);
context.setMode(labelCapture);
```

## Step 5 — Configure the recommended camera

```javascript
const cameraSettings = Scandit.LabelCapture.createRecommendedCameraSettings();
const camera = Scandit.Camera.default;
camera.applySettings(cameraSettings);
context.setFrameSource(camera);
```

## Step 6 — Embed `DataCaptureView` and add the overlay

The Cordova `DataCaptureView` connects to a DOM element you reserve for it.

```html
<!-- index.html -->
<div id="data-capture-view" style="position:fixed; inset:0;"></div>
```

```javascript
const view = Scandit.DataCaptureView.forContext(context);
view.connectToElement(document.getElementById('data-capture-view'));

const basicOverlay = new Scandit.LabelCaptureBasicOverlay(labelCapture);
view.addOverlay(basicOverlay);
```

## Step 7 — Validation Flow (optional)

If the user wants to confirm OCR results, manually correct errors, or capture missing fields without rescanning, use `LabelCaptureValidationFlowOverlay`.

```javascript
const validationFlowOverlay = new Scandit.LabelCaptureValidationFlowOverlay(labelCapture);
validationFlowOverlay.listener = {
  didCaptureLabelWithFields(fields) {
    labelCapture.isEnabled = false;
    showResult(fields);
  },
  didSubmitManualInputForField(field, oldValue, newValue) {
    // 8.2+ — fires when the user manually corrects or enters a field value.
  },
  // 8.4+ — optional. Implement only for fine-grained progress feedback.
  didUpdateValidationFlowResult(type, asyncId, fields, getFrameData) {
    // Fires multiple times during capture as fields accumulate.
  },
};

view.addOverlay(validationFlowOverlay);
```

> **Listener naming.** On Cordova the methods are iOS-style: `didCaptureLabelWithFields`, `didSubmitManualInputForField`, `didUpdateValidationFlowResult`. Web names (`onValidationFlowLabelCaptured`, `onManualInput`) do not exist on Cordova.

> **Version-availability.** `didCaptureLabelWithFields` is the only required method (always available). `didSubmitManualInputForField` shipped in **v8.2**. `didUpdateValidationFlowResult` shipped in **v8.4**. Detect the installed version (read `config.xml` / `package.json`) before suggesting the newer methods.

## Step 8 — Result handling without the Validation Flow

If you only use `LabelCaptureBasicOverlay`, attach a `LabelCaptureListener` to the mode:

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

## Step 9 — Lifecycle (pause/resume + cleanup)

Cordova fires `pause` and `resume` events on `document`:

```javascript
let wasOn = false;
document.addEventListener('pause', async () => {
  wasOn = (await camera.getCurrentState()) === Scandit.FrameSourceState.On;
  await camera.switchToDesiredState(Scandit.FrameSourceState.Off);
});
document.addEventListener('resume', async () => {
  if (wasOn) await camera.switchToDesiredState(Scandit.FrameSourceState.On);
});
```

Do **not** call `context.dispose()` — the singleton context lives for the entire app lifetime.

## Step 10 — Complete working example

```javascript
let labelCapture;

document.addEventListener('deviceready', () => {
  const context = Scandit.DataCaptureContext.initialize('-- ENTER YOUR SCANDIT LICENSE KEY HERE --');

  const cameraSettings = Scandit.LabelCapture.createRecommendedCameraSettings();
  const camera = Scandit.Camera.default;
  camera.applySettings(cameraSettings);
  context.setFrameSource(camera);

  const barcode = Scandit.CustomBarcode.initWithNameAndSymbologies('Barcode', [
    Scandit.Symbology.EAN13UPCA,
    Scandit.Symbology.Code128,
  ]);
  barcode.optional = false;

  const expiry = new Scandit.ExpiryDateText('Expiry Date');
  expiry.optional = false;

  const total = new Scandit.TotalPriceText('Total Price');
  total.optional = true;

  const labelDefinition = new Scandit.LabelDefinition('Perishable Product');
  labelDefinition.fields = [barcode, expiry, total];

  const settings = Scandit.LabelCaptureSettings.settingsFromLabelDefinitions([labelDefinition], {});
  labelCapture = new Scandit.LabelCapture(settings);
  context.setMode(labelCapture);

  const view = Scandit.DataCaptureView.forContext(context);
  view.connectToElement(document.getElementById('data-capture-view'));

  const basicOverlay = new Scandit.LabelCaptureBasicOverlay(labelCapture);
  view.addOverlay(basicOverlay);

  const validationFlowOverlay = new Scandit.LabelCaptureValidationFlowOverlay(labelCapture);
  validationFlowOverlay.listener = {
    didCaptureLabelWithFields(fields) {
      labelCapture.isEnabled = false;
      showResult(formatLabelFields(fields));
    },
  };
  view.addOverlay(validationFlowOverlay);

  camera.switchToDesiredState(Scandit.FrameSourceState.On);
  labelCapture.isEnabled = true;
}, false);

function formatLabelFields(fields) {
  return fields
    .map((field) => {
      let value;
      if (field.barcode != null) value = field.barcode.data;
      else if (field.date != null) value = `${field.date.day}-${field.date.month}-${field.date.year}`;
      else if (field.text != null) value = field.text;
      else value = 'N/A';
      return `${field.name}: ${value}`;
    })
    .join('\n');
}

function showResult(text) {
  document.getElementById('modal-message').textContent = text;
  document.getElementById('result-modal').classList.remove('hidden');
}

function continueScan() {
  document.getElementById('result-modal').classList.add('hidden');
  if (labelCapture) labelCapture.isEnabled = true;
}
```

## Key Rules

- **`deviceready` first.** All Scandit code goes inside `document.addEventListener('deviceready', () => {...}, false)`.
- **Class-based field API only.** `Scandit.CustomBarcode.initWithNameAndSymbologies(...)`, `new Scandit.ExpiryDateText(name)`, `field.optional = true`, `Scandit.LabelCaptureSettings.settingsFromLabelDefinitions([...], {})`. No builders, no factory functions.
- **iOS-style listener names.** `didCaptureLabelWithFields`, `didSubmitManualInputForField` (8.2+), `didUpdateValidationFlowResult` (8.4+). Never `onValidationFlowLabelCaptured` / `onManualInput` (web).
- **Singleton context.** `Scandit.DataCaptureContext.initialize(licenseKey)` is called once. Never `dispose()` it.
- **DOM-anchored view.** `view.connectToElement(document.getElementById(...))`. Make sure the host `<div>` is sized.
- **License key in source is a placeholder** (`'-- ENTER YOUR SCANDIT LICENSE KEY HERE --'`). Replace it before shipping.

## Where to Go Next

- [Label Definitions](https://docs.scandit.com/sdks/cordova/label-capture/label-definitions/)
- [Advanced Configurations](https://docs.scandit.com/sdks/cordova/label-capture/advanced/)
- [LabelCaptureSimpleSample (Cordova)](https://github.com/Scandit/datacapture-cordova-samples/tree/master/03_Advanced_Batch_Scanning_Samples/05_Smart_Label_Capture/LabelCaptureSimpleSample)
