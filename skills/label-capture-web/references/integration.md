# Label Capture Web Integration Guide

Label Capture (Smart Label Capture) extracts multiple fields from a single label in one scan — e.g. a barcode, an expiry date, and a total price on a grocery label. You declare the structure of the label (which fields, required/optional, barcode symbologies or text regex) and the SDK returns all matched fields per frame.

## Prerequisites

- Scandit Data Capture SDK for web — install three npm packages with your user's package manager (npm, pnpm, or yarn):
  - `@scandit/web-datacapture-core`
  - `@scandit/web-datacapture-barcode`
  - `@scandit/web-datacapture-label`
- A valid Scandit license key:
  - Sign in at <https://ssl.scandit.com> to generate one.
  - No account yet? Sign up at <https://ssl.scandit.com/dashboard/sign-up?p=test>.

## Interactive Label Definition

Before writing any code, walk the user through their label. Ask one question at a time.

**Question A — What's on your label?** Present this checklist of supported field types and ask the user to pick everything that applies.

*Barcode fields:*
- `CustomBarcodeBuilder` — any barcode, user chooses symbologies
- `ImeiOneBarcodeBuilder` — IMEI 1 (typically for smartphone boxes)
- `ImeiTwoBarcodeBuilder` — IMEI 2
- `PartNumberBarcodeBuilder` — part number
- `SerialNumberBarcodeBuilder` — serial number

*Text fields (preset recognisers):*
- `ExpiryDateTextBuilder` — expiry date (with configurable date format)
- `PackingDateTextBuilder` — packing date
- `DateTextBuilder` — generic date
- `WeightTextBuilder` — weight
- `UnitPriceTextBuilder` — unit price
- `TotalPriceTextBuilder` — total price

*Text fields (custom):*
- `CustomTextBuilder` — any text, user provides a regex

**Question B — For each selected field:**
- Is it **required** or **optional**? (required = label is not considered captured until this field matches; optional = captured when/if it matches)
- For `CustomBarcodeBuilder`: which **symbologies**? Mention to the user that enabling only the symbologies they actually need improves scanning performance and accuracy.
- For `CustomTextBuilder`: what **regex pattern** should the text match?

**Question C — Which file should the integration code go in?** Then write the code directly into that file. Do not just show it in chat.

## Minimal Integration (Web)

Once the user has answered Questions A, B, and C, generate the integration code using the class-builder API. This form works across all shipped 8.x versions. Substitute the placeholders `[LABEL_NAME]`, `[FIELD_NAME]`, and the correct field builders based on the user's answers. Fields marked optional should call `.setIsOptional(true)`; required fields can omit the call (required is the default) or call `.setIsOptional(false)` explicitly for clarity.

```typescript
import { Symbology } from "@scandit/web-datacapture-barcode";
import { Camera, DataCaptureContext, DataCaptureView, FrameSourceState } from "@scandit/web-datacapture-core";
import {
  CustomBarcodeBuilder,
  ExpiryDateTextBuilder,
  LabelCapture,
  LabelCaptureBasicOverlay,
  LabelCaptureSettingsBuilder,
  LabelDefinitionBuilder,
  labelCaptureLoader,
  type LabelCaptureSession,
  type LabelField,
} from "@scandit/web-datacapture-label";

async function run() {
  const view = new DataCaptureView();
  view.connectToElement(document.getElementById("data-capture-view")!);

  const context = await DataCaptureContext.forLicenseKey(
    "-- ENTER YOUR SCANDIT LICENSE KEY HERE --",
    {
      libraryLocation: new URL("self-hosted-scandit-sdc-lib", document.baseURI).toString(),
      moduleLoaders: [labelCaptureLoader()],
    }
  );
  await view.setContext(context);

  const camera = Camera.pickBestGuess();
  await context.setFrameSource(camera);
  await camera.applySettings(LabelCapture.createRecommendedCameraSettings());
  await camera.switchToDesiredState(FrameSourceState.On);

  const settings = await new LabelCaptureSettingsBuilder()
    .addLabel(
      await new LabelDefinitionBuilder()
        .addCustomBarcode(
          await new CustomBarcodeBuilder()
            .setSymbologies([Symbology.EAN13UPCA, Symbology.Code128])
            .setIsOptional(false)
            .build("Barcode")
        )
        .addExpiryDateText(
          await new ExpiryDateTextBuilder()
            .setIsOptional(false)
            .build("Expiry Date")
        )
        .build("Perishable Product")
    )
    .build();

  const mode = await LabelCapture.forContext(context, settings);

  await LabelCaptureBasicOverlay.withLabelCaptureForView(mode, view);

  mode.addListener({
    didUpdateSession: (_labelCapture, session: LabelCaptureSession, _frameData) => {
      for (const capturedLabel of session.capturedLabels) {
        for (const field of capturedLabel.fields as LabelField[]) {
          console.log(field.name, "=", field.barcode?.data ?? field.text);
        }
      }
    },
  });
}

run();
```

Notes when generating this code:

- Import ONLY the field builders the user actually selected (`CustomBarcodeBuilder`, `ExpiryDateTextBuilder`, etc.). Do not import unused ones.
- The corresponding `addXxx` method on `LabelDefinitionBuilder` mirrors the field type: `addCustomBarcode`, `addExpiryDateText`, `addWeightText`, `addUnitPriceText`, `addTotalPriceText`, `addCustomText`, `addPackingDateText`, `addDateText`, `addImeiOneBarcode`, `addImeiTwoBarcode`, `addPartNumberBarcode`, `addSerialNumberBarcode`.
- For `CustomBarcodeBuilder`, use `setSymbologies([...])` with the symbologies the user selected. For a single symbology, `setSymbology(Symbology.X)` is also valid.
- For `CustomTextBuilder`, use `.setValueRegex(pattern)` (or `.setValueRegexes([patterns])` for multiple). Do not use `.setPattern` or `.setPatterns` — those names were renamed in v8.1 and no longer exist.
- Do NOT use the factory-function sugar (`label(...)`, `customBarcode(...)`, `labelCaptureSettings()`) — that shorthand is only guaranteed from v8.5. The class-builder form above works on all 8.x versions.

## Setup Checklist

After writing the integration code, show this checklist:

1. Install the three npm packages with the user's package manager (npm, pnpm, or yarn):
   - `@scandit/web-datacapture-core`
   - `@scandit/web-datacapture-barcode`
   - `@scandit/web-datacapture-label`
2. Replace `-- ENTER YOUR SCANDIT LICENSE KEY HERE --` with your license key from <https://ssl.scandit.com>.
3. Make sure `libraryLocation` points to a self-hosted copy of the SDK library (the path in `new URL(...)`). You can copy the `sdc-lib` directory from `node_modules/@scandit/web-datacapture-label/sdc-lib/`, or use the CDN instead: `libraryLocation: "https://cdn.jsdelivr.net/npm/@scandit/web-datacapture-label@8/sdc-lib/"`.
4. Ensure a DOM element with id `data-capture-view` exists on the page before `run()` executes.

## Optional: Validation Flow

If the user wants to confirm OCR results, manually correct errors, or capture missing fields without rescanning, enable the Validation Flow. Skip this section if the user is fine with the minimal scan-and-handle path above.

**Before writing Validation Flow code, determine the user's SDK version.** Prefer reading `package.json` for `@scandit/web-datacapture-label`. If that is unreadable or missing, ask: "Which version of `@scandit/web-datacapture-label` are you on?" Then write the version-matched block below — write only one, not both.

### v8.2+ — Redesigned Validation Flow

Import the Validation Flow classes and replace the `LabelCaptureBasicOverlay` line in the minimal integration with the Validation Flow overlay. Replace the existing `mode.addListener` block with the redesigned listener.

```typescript
import {
  LabelCaptureValidationFlowOverlay,
  LabelCaptureValidationFlowSettings,
  type LabelCaptureValidationFlowListener,
  type LabelField,
} from "@scandit/web-datacapture-label";

const overlay = await LabelCaptureValidationFlowOverlay.withLabelCaptureForView(mode, view);

const validationFlowSettings = await LabelCaptureValidationFlowSettings.create();
await validationFlowSettings.setPlaceholderTextForLabelDefinition("Expiry Date", "MM.DD.YY");
await validationFlowSettings.setPlaceholderTextForLabelDefinition("Total Price", "e.g., $13.66");
await overlay.applySettings(validationFlowSettings);

overlay.listener = {
  onManualInput: (_field: LabelField, _oldValue: string | undefined, _newValue: string) => {
    // User manually entered or corrected a value for a field.
  },
  onValidationFlowLabelCaptured: (fields: LabelField[]) => {
    for (const field of fields) {
      console.log(field.name, "=", field.barcode?.data ?? field.text);
    }
  },
} satisfies LabelCaptureValidationFlowListener;
```

The setter methods `setRequiredFieldErrorText`, `setMissingFieldsHintText`, and `setManualInputButtonText` still exist in 8.2.x for backward compatibility, but they are deprecated — they have no effect in the redesigned UI. Do not call them for new v8.2+ integrations. Use `setPlaceholderTextForLabelDefinition` (and other new methods on `LabelCaptureValidationFlowSettings`) for customisation.

### v8.1 and earlier — Original Validation Flow

On v8.1 the listener interface has **only** `onValidationFlowLabelCaptured` — `onManualInput` did not exist yet (it was added in v8.2). Customise the flow via async setter methods on `LabelCaptureValidationFlowSettings`. There is **no** `setPlaceholderTextForLabelDefinition` in 8.1 — that method was added in v8.2.

```typescript
import {
  LabelCaptureValidationFlowOverlay,
  LabelCaptureValidationFlowSettings,
  type LabelCaptureValidationFlowListener,
  type LabelField,
} from "@scandit/web-datacapture-label";

const overlay = await LabelCaptureValidationFlowOverlay.withLabelCaptureForView(mode, view);

const validationFlowSettings = await LabelCaptureValidationFlowSettings.create();
await validationFlowSettings.setRequiredFieldErrorText("This field is required");
await validationFlowSettings.setMissingFieldsHintText("Please fill the missing fields");
await validationFlowSettings.setManualInputButtonText("Enter manually");
await overlay.applySettings(validationFlowSettings);

overlay.listener = {
  onValidationFlowLabelCaptured: (fields: LabelField[]) => {
    for (const field of fields) {
      console.log(field.name, "=", field.barcode?.data ?? field.text);
    }
  },
} satisfies LabelCaptureValidationFlowListener;
```

If the user plans to upgrade to v8.2+, route them to the migration guide (`migration.md` §2) for the listener change (`onManualInput` becomes required) and the optional switch to `setPlaceholderTextForLabelDefinition`.

## Where to Go Next

After the core integration is running, point the user at the right resource for follow-ups:

- [Label Definitions](https://docs.scandit.com/sdks/web/label-capture/label-definitions/) — full catalogue of pre-built text/barcode field types and how to tune their regex anchors and value patterns.
- [Advanced Configurations](https://docs.scandit.com/sdks/web/label-capture/advanced/) — Validation Flow customisation, adaptive recognition, custom overlays.
- [LabelCaptureSimpleSample](https://github.com/Scandit/datacapture-web-samples/tree/master/03_Advanced_Batch_Scanning_Samples/05_Smart_Label_Capture/LabelCaptureSimpleSample) — working reference sample.
