# Third-Party Barcode Scanner → BarcodeCapture Migration

## Before Anything Else

Read the existing code. Do not ask the user to describe what their scanner does. Identify:
- Which framework is in use (read the imports)
- Which symbologies are enabled
- What result handling logic exists (deduplication, filtering, accumulation)
- What data models are defined
- How the scanner is launched (Activity, Fragment, intent-based, embedded view)

---

## Remove

- The old framework's imports and dependencies
- The scanner class instance and all its setup code
- The old callback, listener, or `onActivityResult` override
- Any UI code specific to the old scanner (e.g. intent launch, dialog, overlay the old library provided)

---

## Integrate BarcodeCapture

Follow `references/integration.md`. When configuring `BarcodeCaptureSettings`, map the symbologies from the old scanner to the corresponding `Symbology.*` values.

BarcodeCapture replaces the old scanner's camera and preview entirely — `DataCaptureView` becomes the new content view, and `BarcodeCaptureOverlay` draws the highlight. The Activity no longer needs to manage a separate camera or preview widget.

---

## Preserve

- Custom data models — keep as-is
- Result accumulation and deduplication logic — move verbatim into the `onBarcodeScanned` callback
- Any downstream business logic triggered on scan result

---

When done, show only what changed. Do not list APIs that were unchanged.
