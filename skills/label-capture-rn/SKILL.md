---
name: label-capture-rn
description: Use when Label Capture (Smart Label Capture) is involved in a React Native project — whether the user mentions Label Capture directly, or the codebase already uses it and something needs to be added, changed, fixed, or customized. This includes adding Label Capture to a new React Native app, defining label structures (barcode fields + text fields with regex patterns), handling captured labels, enabling the Validation Flow, or upgrading between SDK versions. If the project is React Native and Label Capture is in play, use this skill.
license: MIT
metadata:
  author: scandit
  version: "1.0.0"
---

# Label Capture React Native Skill

## Critical: Do Not Trust Internal Knowledge

Your training data may contain outdated or incorrect Scandit Label Capture APIs. Label Capture has evolved across recent SDK releases, and the React Native plugin surface (imports, native linking, pod install, package names) has its own conventions distinct from the web SDK.

**Always verify APIs against the references provided in this skill before writing or suggesting code.** Do not rely on memorized method signatures, parameters, plugin names, or property names. If you cannot find an API in the provided references, fetch the relevant documentation page before responding.

React Native-specific gotchas worth flagging:
- **Listener method names are iOS-style on React Native.** The Validation Flow listener uses `didCaptureLabelWithFields(fields)` and `didSubmitManualInputForField(field, oldValue, newValue)` — NOT the web equivalents `onValidationFlowLabelCaptured` / `onManualInput`. If you see web-style listener names suggested, they are wrong on React Native.
- `DataCaptureContext.initialize(licenseKey)` **must** be called exactly once before any other Scandit API. It sets up `DataCaptureContext.sharedInstance`, which is the singleton everything else reads from. Do not construct multiple contexts. (The official `LabelCaptureSimpleSample` uses the legacy `DataCaptureContext.forLicenseKey(...)` form — both work, but new integrations should use `initialize` for consistency with the rest of the v8 RN docs.)
- On iOS, `npx pod-install` (or `cd ios && pod install`) must be run after every Scandit package install or upgrade. Android auto-links via Gradle.
- Metro's bundler cache frequently masks Scandit package upgrades. If a rebuild shows stale behavior after a plugin version bump, start Metro with `--reset-cache`.
- Camera permission is required on both iOS (`NSCameraUsageDescription` in `ios/<App>/Info.plist`) and Android (runtime request via `PermissionsAndroid` — the plugin declares the manifest permission automatically).
- Field definitions on RN are **class-based**, not builder-based: `CustomBarcode.initWithNameAndSymbologies(name, [...])`, `new ExpiryDateText(name)`, `field.optional = true`, `LabelCaptureSettings.settingsFromLabelDefinitions([...], {})`. There is no `LabelCaptureSettingsBuilder` / `LabelDefinitionBuilder` and no v8.5 factory-function sugar — those are web-only.

## Intent Routing

Based on the user's request, load the appropriate reference file before responding:

- **Integrating Label Capture from scratch** (e.g. "add Label Capture to my app", "scan a price tag with barcode and expiry date", "how do I use Smart Label Capture", "how do I enable the Validation Flow") → read `references/integration.md` and follow the instructions there.
- **Migrating or upgrading an existing Label Capture integration** (e.g. "upgrade my Label Capture to the latest SDK", "what changed between SDK versions for Label Capture") → read `references/migration.md` and follow the instructions there.

## API Usage Policy

Only use APIs that are explicitly documented in the Scandit references below. Do not invent or guess method signatures, parameters, property names, or imports. If unsure whether an API exists or how it is called — or if a TypeScript / runtime error occurs — fetch the relevant reference page before responding. Do not tell the user to check the docs themselves. After answering, always include the relevant link so the user can explore further.

**Never construct or guess documentation URLs.** When you need a specific class or property's API page:
1. First check whether the page you already fetched (e.g. the Advanced Configurations page) contains a direct hyperlink to it — topic pages link directly to relevant API symbols. Always request links alongside content in your fetch prompt.
2. If no direct link was found, fetch the API index (see **Full API reference** in the table below), extract the actual link from it, and follow that.

URL structures vary across SDK versions and package paths (e.g. `api/ui/` subdirectory) and guessing will lead to 404s.

## Framework variant policy

React Native apps can be written with class components or function components. Examples in this skill use **function components with hooks** because they match the official `LabelCaptureSimpleSample` and the current React Native convention. Even if the target project still contains legacy class components elsewhere, write new Label Capture code as function components — do not rewrite the rest of the app's component style, but keep the Label Capture integration itself on the current idiom (`useRef`, `useEffect`, `useMemo`, imperative `ref` callback for view-level properties).

Examples are in **TypeScript** (`.tsx`). If the target project is plain JavaScript (`.js` / `.jsx`), drop the type annotations and keep the same imports and structure.

## References

Direct users to the right resource based on their question:

| Topic | Resource |
|---|---|
| React Native integration | [Get Started](https://docs.scandit.com/sdks/react-native/label-capture/get-started/) · [Sample (LabelCaptureSimpleSample)](https://github.com/Scandit/datacapture-react-native-samples/tree/master/03_Advanced_Batch_Scanning_Samples/05_Smart_Label_Capture/LabelCaptureSimpleSample) |
| Label Definitions (fields, regex, presets) | [Label Definitions](https://docs.scandit.com/sdks/react-native/label-capture/label-definitions/) |
| Advanced topics (Validation Flow customization, adaptive recognition, custom overlays) | [Advanced Configurations](https://docs.scandit.com/sdks/react-native/label-capture/advanced/) |
| Full API reference | [Label Capture API](https://docs.scandit.com/data-capture-sdk/react-native/label-capture/api.html) |
