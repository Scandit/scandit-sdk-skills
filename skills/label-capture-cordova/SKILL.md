---
name: label-capture-cordova
description: Use when Label Capture (Smart Label Capture) is involved in a Cordova project — whether the user mentions Label Capture directly, or the codebase already uses it and something needs to be added, changed, fixed, or customized. This includes adding Label Capture to a new Cordova app, defining label structures (barcode fields + text fields with regex patterns), handling captured labels, enabling the Validation Flow, or upgrading between SDK versions. If the project is Cordova and Label Capture is in play, use this skill.
license: MIT
metadata:
  author: scandit
  version: "1.0.0"
---

# Label Capture Cordova Skill

## Critical: Do Not Trust Internal Knowledge

Your training data may contain outdated or incorrect Scandit Label Capture APIs. The Cordova plugin surface (global `Scandit.*` namespace, `deviceready` gating, plugin install, platform prepare) is distinct from the web, React Native, Flutter, and Capacitor SDKs.

**Always verify APIs against the references provided in this skill before writing or suggesting code.** Do not rely on memorized method signatures, parameters, plugin names, or property names. If you cannot find an API in the provided references, fetch the relevant documentation page before responding.

Cordova-specific gotchas worth flagging:
- **All Scandit code must run after `deviceready`.** The `Scandit.*` global namespace is not populated until then. Wrap your initialization in `document.addEventListener('deviceready', () => {...}, false);`.
- **Listener method names are iOS-style on Cordova.** The Validation Flow listener uses `didCaptureLabelWithFields(fields)` and `didSubmitManualInputForField(field, oldValue, newValue)` — NOT the web equivalents `onValidationFlowLabelCaptured` / `onManualInput`.
- **Class-based field API**, same shape as RN/Capacitor (and opposite to Flutter). Use `Scandit.CustomBarcode.initWithNameAndSymbologies(name, [...])`, `new Scandit.ExpiryDateText(name)`, `field.optional = false`, `Scandit.LabelCaptureSettings.settingsFromLabelDefinitions([...], {})`. There is no `LabelCaptureSettingsBuilder` / `LabelDefinitionBuilder` and no v8.5 factory-function sugar — those are web-only.
- After `cordova plugin add` / version bump, run `cordova prepare ios` (and `cordova prepare android`) to sync native dependencies. iOS additionally requires a fresh `pod install` inside `platforms/ios/`.
- Camera permission: iOS requires `NSCameraUsageDescription` in the app's `Info.plist` (or via `<config-file>` in `config.xml`); Android's `CAMERA` permission is declared by the plugin automatically and must be requested at runtime if your minSdkVersion targets API 23+.
- `Scandit.DataCaptureContext.initialize(licenseKey)` returns the singleton context; do not construct multiple contexts.

## Intent Routing

Based on the user's request, load the appropriate reference file before responding:

- **Integrating Label Capture from scratch** (e.g. "add Label Capture to my app", "scan a price tag with barcode and expiry date", "how do I use Smart Label Capture", "how do I enable the Validation Flow") → read `references/integration.md` and follow the instructions there.
- **Migrating or upgrading an existing Label Capture integration** (e.g. "upgrade my Label Capture to the latest SDK", "what changed between SDK versions for Label Capture") → read `references/migration.md` and follow the instructions there.

## API Usage Policy

Only use APIs that are explicitly documented in the Scandit references below. Do not invent or guess method signatures, parameters, property names, or imports. If unsure whether an API exists or how it is called — or if a runtime error occurs — fetch the relevant reference page before responding. Do not tell the user to check the docs themselves. After answering, always include the relevant link so the user can explore further.

**Never construct or guess documentation URLs.** When you need a specific class or property's API page:
1. First check whether the page you already fetched (e.g. the Advanced Configurations page) contains a direct hyperlink to it.
2. If no direct link was found, fetch the API index (see **Full API reference** in the table below), extract the actual link from it, and follow that.

URL structures vary across SDK versions and package paths and guessing will lead to 404s.

## Framework variant policy

Examples in this skill are written in **plain JavaScript (ES6+)** because that is the default for Cordova templates and the official `LabelCaptureSimpleSample`. If the target project uses TypeScript (some Cordova projects do), keep the same imports/structure and add type annotations as needed — do not change the global `Scandit.*` access pattern, do not switch to ES module imports, and do not assume bundler features.

## References

| Topic | Resource |
|---|---|
| Cordova integration | [Get Started](https://docs.scandit.com/sdks/cordova/label-capture/get-started/) · [Sample (LabelCaptureSimpleSample)](https://github.com/Scandit/datacapture-cordova-samples/tree/master/03_Advanced_Batch_Scanning_Samples/05_Smart_Label_Capture/LabelCaptureSimpleSample) |
| Label Definitions (fields, regex, presets) | [Label Definitions](https://docs.scandit.com/sdks/cordova/label-capture/label-definitions/) |
| Advanced topics (Validation Flow customization, adaptive recognition, custom overlays) | [Advanced Configurations](https://docs.scandit.com/sdks/cordova/label-capture/advanced/) |
| Full API reference | [Label Capture API](https://docs.scandit.com/data-capture-sdk/cordova/label-capture/api.html) |
