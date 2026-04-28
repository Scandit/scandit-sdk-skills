---
name: label-capture-flutter
description: Use when Label Capture (Smart Label Capture) is involved in a Flutter project — whether the user mentions Label Capture directly, or the codebase already uses it and something needs to be added, changed, fixed, or customized. This includes adding Label Capture to a new Flutter app, defining label structures (barcode fields + text fields with regex patterns), handling captured labels, enabling the Validation Flow, or upgrading between SDK versions. If the project is Flutter and Label Capture is in play, use this skill.
license: MIT
metadata:
  author: scandit
  version: "1.0.0"
---

# Label Capture Flutter Skill

## Critical: Do Not Trust Internal Knowledge

Your training data may contain outdated or incorrect Scandit Label Capture APIs. The Flutter plugin surface (package names, builder API, plugin initialization, widget lifecycle) is distinct from the web, React Native, Cordova, and Capacitor SDKs.

**Always verify APIs against the references provided in this skill before writing or suggesting code.** Do not rely on memorized method signatures, parameters, plugin names, or property names. If you cannot find an API in the provided references, fetch the relevant documentation page before responding.

Flutter-specific gotchas worth flagging:
- **Plugin must be initialized before any Scandit API call.** Call `await ScanditFlutterDataCaptureLabel.initialize();` (and `await ScanditFlutterDataCaptureBarcode.initialize();`) **before** `DataCaptureContext.initialize(licenseKey);`. Skipping the plugin `initialize()` causes opaque MethodChannel errors at runtime.
- **Listener method names are iOS-style on Flutter.** The Validation Flow uses `didCaptureLabelWithFields(fields)`, `didSubmitManualInputForField(field, oldValue, newValue)`, `didUpdateValidationFlowResult(...)`. Web names (`onValidationFlowLabelCaptured`, `onManualInput`) do not exist on Flutter.
- **Two listener interfaces exist.** `LabelCaptureValidationFlowListener` declares only `didCaptureLabelWithFields`. To handle manual-input submissions or per-frame result updates, implement `LabelCaptureValidationFlowExtendedListener` (which extends the base listener with `didSubmitManualInputForField` and `didUpdateValidationFlowResult`).
- **Builder API on Flutter.** Field definitions use builders: `CustomBarcodeBuilder().setSymbologies([...]).isOptional(false).build(name)`, `LabelDefinitionBuilder().addCustomBarcode(...).addExpiryDateText(...).build(name)`, `LabelCaptureSettings([labelDefinition])`. This is opposite to RN/Cordova/Capacitor (which are class-based) — do not mix them up.
- Camera permission is required on both iOS (`NSCameraUsageDescription` in `ios/Runner/Info.plist`) and Android (declared automatically; request at runtime with the `permission_handler` package).
- The `DataCaptureView` is a Flutter widget; its lifecycle ties to the widget tree. Pause the camera in `didChangeAppLifecycleState` (via `WidgetsBindingObserver`) and dispose listeners in the State's `dispose()`.

## Intent Routing

Based on the user's request, load the appropriate reference file before responding:

- **Integrating Label Capture from scratch** (e.g. "add Label Capture to my app", "scan a price tag with barcode and expiry date", "how do I use Smart Label Capture", "how do I enable the Validation Flow") → read `references/integration.md` and follow the instructions there.
- **Migrating or upgrading an existing Label Capture integration** (e.g. "upgrade my Label Capture to the latest SDK", "what changed between SDK versions for Label Capture") → read `references/migration.md` and follow the instructions there.

## API Usage Policy

Only use APIs that are explicitly documented in the Scandit references below. Do not invent or guess method signatures, parameters, property names, or imports. If unsure whether an API exists or how it is called — or if a Dart compiler / runtime error occurs — fetch the relevant reference page before responding. Do not tell the user to check the docs themselves. After answering, always include the relevant link so the user can explore further.

**Never construct or guess documentation URLs.** When you need a specific class or property's API page:
1. First check whether the page you already fetched (e.g. the Advanced Configurations page) contains a direct hyperlink to it — topic pages link directly to relevant API symbols. Always request links alongside content in your fetch prompt.
2. If no direct link was found, fetch the API index (see **Full API reference** in the table below), extract the actual link from it, and follow that.

URL structures vary across SDK versions and package paths and guessing will lead to 404s.

## Framework variant policy

Examples in this skill use **StatefulWidget + WidgetsBindingObserver** because the official `LabelCaptureSimpleSample` and the rest of the Flutter Scandit samples use it. If the target project uses a state-management library (BLoC, Riverpod, Provider), wire the listeners and lifecycle into that pattern instead — but keep the same plugin initialization and field/builder code. Do not introduce a new state-management library just for Label Capture.

Examples are in **Dart** (sound null-safety). Flutter `>=3.10` and Dart `>=3.0` are required.

## References

Direct users to the right resource based on their question:

| Topic | Resource |
|---|---|
| Flutter integration | [Get Started](https://docs.scandit.com/sdks/flutter/label-capture/get-started/) · [Sample (LabelCaptureSimpleSample)](https://github.com/Scandit/datacapture-flutter-samples/tree/master/03_Advanced_Batch_Scanning_Samples/05_Smart_Label_Capture/LabelCaptureSimpleSample) |
| Label Definitions (fields, regex, presets) | [Label Definitions](https://docs.scandit.com/sdks/flutter/label-capture/label-definitions/) |
| Advanced topics (Validation Flow customization, adaptive recognition, custom overlays) | [Advanced Configurations](https://docs.scandit.com/sdks/flutter/label-capture/advanced/) |
| Full API reference | [Label Capture API](https://docs.scandit.com/data-capture-sdk/flutter/label-capture/api.html) |
