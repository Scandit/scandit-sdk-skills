---
name: sparkscan-ios
description: Guide for SparkScan on iOS using the Scandit Data Capture SDK. Use this skill when a developer asks how to add SparkScan to an iOS app, set up barcode scanning, handle scan results, OR migrate/upgrade an existing SparkScan integration from one SDK version to another.
license: MIT
metadata:
  author: scandit
  version: "1.1.0"
---

# SparkScan iOS Skill

## Intent Routing

Based on the user's request, load the appropriate reference file before responding:

- **Integrating SparkScan from scratch** (e.g. "add SparkScan to my app", "set up barcode scanning", "how do I use SparkScan") → read `references/integration.md` and follow the instructions there.
- **Migrating or upgrading an existing SparkScan integration** (e.g. "upgrade from v6 to v7", "migrate my SparkScan", "what changed between SDK versions") → read `references/migration.md` and follow the instructions there.

## API Usage Policy

Only use APIs that are explicitly documented in the Scandit references listed below. Do not invent or guess method signatures, parameters, or view modifiers. If you are not certain an API exists or how it is called, fetch the relevant reference page first. If a compile error occurs, fetch the API reference to find the correct API before attempting a fix.

## References

If you are unsure whether a specific SparkScan behavior or feature is supported, or if a compile error indicates a wrong API, fetch the relevant page from the table below before responding. Do not tell the user to check the docs themselves — look it up first. After answering, always include the relevant link so the user can explore further details if they want.

Direct users to the right resource based on their question:

| Topic | Resource |
|---|---|
| UIKit integration | [Get Started (UIKit)](https://docs.scandit.com/sdks/ios/sparkscan/get-started/) · [Sample](https://github.com/Scandit/datacapture-ios-samples/tree/master/01_Single_Scanning_Samples/01_Barcode_Scanning_with_Prebuilt_UI/ListBuildingSampleUIKit) |
| SwiftUI integration | [Get Started (SwiftUI)](https://docs.scandit.com/sdks/ios/sparkscan/get-started-with-swift-ui/) · [Sample](https://github.com/Scandit/datacapture-ios-samples/tree/master/01_Single_Scanning_Samples/01_Barcode_Scanning_with_Prebuilt_UI/ListBuildingSampleSwiftUI) |
| Advanced topics (custom feedback, hardware triggers, scanning modes, UI customization) | [Advanced Configurations](https://docs.scandit.com/sdks/ios/sparkscan/advanced/) |
| Full API reference | [SparkScan API](https://docs.scandit.com/data-capture-sdk/ios/barcode-capture/api.html) |
