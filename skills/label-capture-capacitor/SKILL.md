---
name: label-capture-capacitor
description: Use when Label Capture (Smart Label Capture) is involved in a Capacitor project — whether the user mentions Label Capture directly, or the codebase already uses it and something needs to be added, changed, fixed, or customized. This includes adding Label Capture to a new Capacitor app, defining label structures (barcode fields + text fields with regex patterns), handling captured labels, enabling the Validation Flow, or upgrading between SDK versions. If the project is Capacitor and Label Capture is in play, use this skill.
license: MIT
metadata:
  author: scandit
  version: "1.0.0"
---

# Label Capture Capacitor Skill

## Critical: Do Not Trust Internal Knowledge

Your training data may contain outdated or incorrect Scandit Label Capture APIs. The Capacitor plugin surface (ES module imports, `initializePlugins()`, `webViewContentOnTop` toggle) is distinct from the web, React Native, Flutter, and Cordova SDKs.

**Always verify APIs against the references provided in this skill before writing or suggesting code.** Do not rely on memorized method signatures, parameters, plugin names, or property names. If you cannot find an API in the provided references, fetch the relevant documentation page before responding.

Capacitor-specific gotchas worth flagging:
- **Plugins must be initialized first.** Call `await ScanditCaptureCorePlugin.initializePlugins();` **before** `DataCaptureContext.initialize(licenseKey)`. Skipping this step causes plugin-bridge errors at runtime.
- **`webViewContentOnTop` toggle.** The native scan view sits *behind* the WebView. While scanning, set `view.webViewContentOnTop = false` so native overlay UI receives touch events. To show DOM-based UI on top (e.g. a result modal), flip back to `view.webViewContentOnTop = true`. Forgetting this is the most common cause of "modal/buttons don't work during scanning" or "scan UI is invisible".
- **Listener method names are iOS-style on Capacitor.** The Validation Flow listener uses `didCaptureLabelWithFields(fields)`, `didSubmitManualInputForField(field, oldValue, newValue)`, `didUpdateValidationFlowResult(...)`. Web names (`onValidationFlowLabelCaptured`, `onManualInput`) do not exist on Capacitor.
- **Class-based field API**, same shape as RN/Cordova (and opposite to Flutter). `CustomBarcode.initWithNameAndSymbologies(...)`, `new ExpiryDateText(...)`, `field.optional = false`, `LabelCaptureSettings.settingsFromLabelDefinitions([...], {})`. There is no `LabelCaptureSettingsBuilder` / `LabelDefinitionBuilder` and no v8.5 factory-function sugar — those are web-only.
- After installing or upgrading Scandit packages, run `npx cap sync` (and `cd ios/App && pod install` for iOS).
- Camera permission: iOS requires `NSCameraUsageDescription` in `ios/App/App/Info.plist`. Android: declared automatically by the plugin; request at runtime via `@capacitor/camera` or `@capgo/permission` if `minSdkVersion >= 23`.

## Intent Routing

- **Integrating Label Capture from scratch** → read `references/integration.md`.
- **Migrating or upgrading an existing Label Capture integration** → read `references/migration.md`.

## API Usage Policy

Only use APIs that are explicitly documented in the Scandit references below. Do not invent or guess method signatures, parameters, property names, or imports. If unsure whether an API exists, or if a runtime error occurs, fetch the relevant reference page before responding. Do not tell the user to check the docs themselves. After answering, always include the relevant link.

**Never construct or guess documentation URLs.** Either follow a hyperlink already present in a fetched page, or fetch the API index and follow the link from there.

## Framework variant policy

Examples in this skill use **plain JavaScript with ES module imports** because that is the default for Capacitor templates and the official `LabelCaptureSimpleSample`. If the target project uses a framework (React, Vue, Angular, Ionic), keep the Scandit init/setup outside the component tree (e.g. in a small module that exports the singleton context) and call into it from the framework component. Don't introduce a state-management library just for Label Capture.

## References

| Topic | Resource |
|---|---|
| Capacitor integration | [Get Started](https://docs.scandit.com/sdks/capacitor/label-capture/get-started/) · [Sample (LabelCaptureSimpleSample)](https://github.com/Scandit/datacapture-capacitor-samples/tree/master/03_Advanced_Batch_Scanning_Samples/05_Smart_Label_Capture/LabelCaptureSimpleSample) |
| Label Definitions (fields, regex, presets) | [Label Definitions](https://docs.scandit.com/sdks/capacitor/label-capture/label-definitions/) |
| Advanced topics (Validation Flow customization, adaptive recognition, custom overlays) | [Advanced Configurations](https://docs.scandit.com/sdks/capacitor/label-capture/advanced/) |
| Full API reference | [Label Capture API](https://docs.scandit.com/data-capture-sdk/capacitor/label-capture/api.html) |
