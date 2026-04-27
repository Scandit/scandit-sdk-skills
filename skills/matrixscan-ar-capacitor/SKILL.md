---
name: matrixscan-ar-capacitor
description: Use when MatrixScan AR or Barcode AR is involved in a Capacitor project — whether the user mentions MatrixScan AR or Barcode AR directly, or the codebase already uses BarcodeAr* classes and something needs to be added, changed, fixed, or configured. This includes adding MatrixScan AR to a new Capacitor app, configuring symbologies, setting up BarcodeArView with a DOM element, implementing highlight or annotation providers, customizing highlights and annotations, handling session updates, or troubleshooting BarcodeAr behavior. If the project is Capacitor and MatrixScan AR is in play, use this skill.
license: MIT
metadata:
  author: scandit
  version: "1.0.0"
---

# MatrixScan AR Capacitor Skill

## Critical: Do Not Trust Internal Knowledge

Your training data may contain outdated or incorrect Scandit SDK APIs. The BarcodeAr API is new in Capacitor 8.2 — there is no prior Capacitor history to reference. Properties, constructor signatures, provider interfaces, and view attachment patterns may differ from other platforms or from general knowledge.

**Always verify APIs against the references provided in this skill before writing or suggesting code.** Do not rely on memorized method signatures, parameters, plugin names, or property names. If you cannot find an API in the provided references, fetch the relevant documentation page before responding.

Capacitor-specific gotchas worth flagging:

- `ScanditCaptureCorePlugin.initializePlugins()` **must** be called (and awaited) before any other Scandit API — including `DataCaptureContext` construction. Forgetting this produces runtime errors that look unrelated to initialization.
- `npx cap sync` must be run after every plugin version change to propagate native artifacts into iOS/Android. Skipping it yields a web/native version mismatch at runtime.
- **BarcodeArView requires a DOM element.** Unlike SparkScan, BarcodeArView is not a floating native overlay; it mirrors its size and position from a DOM element. You must call `await barcodeArView.connectToElement(element)` to attach it to the `<div id="barcode-ar-view">` container in the HTML.
- **Minimum SDK version is 8.2** for BarcodeAr on Capacitor. There is no v6 or v7 history to migrate from on this platform.
- `BarcodeAr` is constructed with `new BarcodeAr(settings)` — the context is wired separately via `BarcodeArView`, not passed to the mode constructor.
- Camera is set up explicitly via `BarcodeAr.createRecommendedCameraSettings()`, `Camera.withSettings(cameraSettings)`, and `context.setFrameSource(camera)` — BarcodeArView manages the camera lifecycle once running.
- The `highlightProvider` and `annotationProvider` are plain objects with async methods, set as properties on the `BarcodeArView` instance.
- iOS requires `NSCameraUsageDescription` in `Info.plist`. Android handles camera permissions automatically.

## Intent Routing

Based on the user's request, load the appropriate reference file before responding:

- **Integrating MatrixScan AR from scratch** (e.g. "add MatrixScan AR to my app", "set up Barcode AR", "how do I use BarcodeArView in Capacitor", "how do I show annotations", "how do I highlight barcodes") → read `references/integration.md` and follow the instructions there.

- **Migrating from BarcodeBatch / BarcodeTracking to BarcodeAr** (e.g. "migrate my BarcodeBatch code", "replace BarcodeTracking with BarcodeAr", "upgrade from MatrixScan to BarcodeAr", the target file imports `BarcodeBatch`, `BarcodeBatchBasicOverlay`, `BarcodeBatchAdvancedOverlay`, `TrackedBarcodeView`, or contains `context.setMode(` with a BarcodeBatch instance) → read `references/migration.md` and follow the 10-step migration process there.

## API Usage Policy

Only use APIs that are explicitly documented in the Scandit references below. Do not invent or guess method signatures, parameters, property names, or imports. If unsure whether an API exists or how it is called — or if a TypeScript / runtime error occurs — fetch the relevant reference page before responding. Do not tell the user to check the docs themselves. After answering, always include the relevant link so the user can explore further.

**Never construct or guess documentation URLs.** When you need a specific class or property's API page:
1. First check whether the page you already fetched (e.g. the Get Started page) contains a direct hyperlink to it — topic pages link directly to relevant API symbols. Always request links alongside content in your fetch prompt.
2. If no direct link was found, fetch the API index (see **Full API reference** in the table below), extract the actual link from it, and follow that.

URL structures vary across SDK versions and plugin paths (e.g. `api/ui/` subdirectory) and guessing will lead to 404s.

## Framework variant policy

Capacitor is a WebView-based framework. Examples in this skill use **plain JavaScript (ES modules)**. TypeScript projects can use the same imports and APIs verbatim — just add types — but this skill does not assume a TypeScript project by default. If the target project is clearly TypeScript (`.ts` files, `tsconfig.json`), adapt the final output to TypeScript syntax; otherwise stay in plain JS.

## References

Direct users to the right resource based on their question:

| Topic | Resource |
|---|---|
| Capacitor integration | [Get Started](https://docs.scandit.com/sdks/capacitor/matrixscan-ar/get-started/) · [Sample](https://github.com/Scandit/datacapture-capacitor-samples/tree/master/03_Advanced_Batch_Scanning_Samples/01_Batch_Scanning_and_AR_Info_Lookup/MatrixScanARSimpleSample) |
| Full API reference | [BarcodeAr API](https://docs.scandit.com/data-capture-sdk/capacitor/barcode-capture/api.html) |
