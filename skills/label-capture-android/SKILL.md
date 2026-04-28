---
name: label-capture-android
description: Use when Label Capture (Smart Label Capture) is involved in an Android project — whether the user mentions Label Capture directly, or the codebase already uses it and something needs to be added, changed, fixed, or customized. This includes adding Label Capture to a new Android app, defining label structures (barcode fields + text fields with regex patterns), handling captured sessions, enabling the Validation Flow, or migrating between SDK versions. If the project is an Android project and Label Capture is in play, use this skill.
license: MIT
metadata:
  author: scandit
  version: "1.0.0"
---

# Label Capture Android Skill

## Critical: Do Not Trust Internal Knowledge

Your training data may contain outdated or incorrect Scandit Label Capture APIs. Label Capture has evolved across recent SDK releases:

- At the v7→v8 major bump, `LabelFieldDefinition` regex builder methods were renamed (`setPattern`→`setValueRegex`, `setPatterns`→`setValueRegexes`, `setDataTypePattern`→`setAnchorRegex`, `setDataTypePatterns`→`setAnchorRegexes`).
- At v8.2, Validation Flow 2.0 introduced `shouldHandleKeyboardInsetsInternally` on `LabelCaptureValidationFlowOverlay` — relevant for Android 15 edge-to-edge enforcement.
- Android symbology names use underscores: `Symbology.EAN13_UPCA`, not `Symbology.EAN13UPCA`.

**Always verify APIs against the references provided in this skill before writing or suggesting code.** Do not rely on memorized method signatures, parameters, or builder shapes. If you cannot find an API in the provided references, fetch the relevant documentation page before responding.

## Intent Routing

Based on the user's request, load the appropriate reference file before responding:

- **Integrating Label Capture from scratch** (e.g. "add Label Capture to my Android app", "scan a price tag with barcode and expiry date", "how do I use Smart Label Capture") → read `references/integration.md` and follow the instructions there.
- **Enabling or customizing the Validation Flow** (e.g. "how do I enable the Validation Flow", "add the validation flow overlay", "customize the placeholder text / button labels in the validation flow", "how do I handle the result from the validation flow") → read `references/validation-flow.md` and follow the instructions there.
- **Migrating or upgrading an existing Label Capture integration** (e.g. "upgrade my Label Capture to the latest SDK", "migrate from v7 to v8", "what changed between SDK versions for Label Capture", "keyboard covers the input in Validation Flow after upgrading", "migrate Validation Flow to 2.0") → read `references/migration.md` and follow the instructions there.

## API Usage Policy

Only use APIs that are explicitly documented in the Scandit references below. Do not invent or guess method signatures, parameters, or builder shapes. If unsure whether an API exists or how it is called — or if a compile error occurs — fetch the relevant reference page before responding. Do not tell the user to check the docs themselves. After answering, always include the relevant link so the user can explore further.

**Never construct or guess documentation URLs.** When you need a specific class or property's API page:

1. First check whether the page you already fetched (e.g. the Advanced Configurations page) contains a direct hyperlink to it — topic pages link directly to relevant API symbols. Always request links alongside content in your fetch prompt.
2. If no direct link was found, fetch the API index (see **Full API reference** in the table below), extract the actual link from it, and follow that.

URL structures can vary and guessing will lead to 404s.

## References

Direct users to the right resource based on their question:

| Topic | Resource |
|---|---|
| Basic integration | [Get Started](https://docs.scandit.com/sdks/android/label-capture/get-started/) · [Sample (LabelCaptureSimpleSample)](https://github.com/Scandit/datacapture-android-samples/tree/master/03_Advanced_Batch_Scanning_Samples/05_Smart_Label_Capture/LabelCaptureSimpleSample) |
| Label Definitions (fields, regex, presets) | [Label Definitions](https://docs.scandit.com/sdks/android/label-capture/label-definitions/) |
| Advanced topics (Validation Flow customization, adaptive recognition, custom overlays) | [Advanced Configurations](https://docs.scandit.com/sdks/android/label-capture/advanced/) |
| Full API reference | [Label Capture API](https://docs.scandit.com/data-capture-sdk/android/label-capture/api.html) |
