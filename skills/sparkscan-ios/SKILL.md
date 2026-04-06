---
name: sparkscan-ios
description: Guide for SparkScan on iOS using the Scandit Data Capture SDK. Use this skill when a developer asks how to add SparkScan to an iOS app, set up barcode scanning, handle scan results, OR migrate/upgrade an existing SparkScan integration from one SDK version to another.
license: MIT
metadata:
  author: scandit
  version: "1.1.1"
---

# SparkScan iOS Skill

## Intent Routing

Based on the user's request, load the appropriate reference file before responding:

- **Integrating SparkScan from scratch** (e.g. "add SparkScan to my app", "set up barcode scanning", "how do I use SparkScan", "how do I handle feedback in SparkScan") → read `references/integration.md` and follow the instructions there.
- **Migrating or upgrading an existing SparkScan integration** (e.g. "upgrade from v6 to v7", "migrate my SparkScan", "what changed between SDK versions") → read `references/migration.md` and follow the instructions there.

## API Usage Policy

Only use APIs that are explicitly documented in the Scandit references below. Do not invent or guess method signatures, parameters, or view modifiers. If unsure whether an API exists or how it is called — or if a compile error occurs — fetch the relevant reference page before responding. Do not tell the user to check the docs themselves. After answering, always include the relevant link so the user can explore further.

**Never construct or guess documentation URLs.** When you need a specific class or property's API page:
1. First check whether the page you already fetched (e.g. the Advanced Configurations page) contains a direct hyperlink to it — topic pages link directly to relevant API symbols. Always request links alongside content in your fetch prompt.
2. If no direct link was found, fetch the API index (see **Full API reference** in the table below), extract the actual link from it, and follow that.

URL structures can vary (e.g. `api/ui/` subdirectory) and guessing will lead to 404s.

## References

Direct users to the right resource based on their question:

| Topic | Resource |
|---|---|
| UIKit integration | [Get Started (UIKit)](https://docs.scandit.com/sdks/ios/sparkscan/get-started/) · [Sample](https://github.com/Scandit/datacapture-ios-samples/tree/master/01_Single_Scanning_Samples/01_Barcode_Scanning_with_Prebuilt_UI/ListBuildingSampleUIKit) |
| SwiftUI integration | [Get Started (SwiftUI)](https://docs.scandit.com/sdks/ios/sparkscan/get-started-with-swift-ui/) · [Sample](https://github.com/Scandit/datacapture-ios-samples/tree/master/01_Single_Scanning_Samples/01_Barcode_Scanning_with_Prebuilt_UI/ListBuildingSampleSwiftUI) |
| Advanced topics (custom feedback, hardware triggers, scanning modes, UI customization) | [Advanced Configurations](https://docs.scandit.com/sdks/ios/sparkscan/advanced/) |
| Full API reference | [SparkScan API](https://docs.scandit.com/data-capture-sdk/ios/barcode-capture/api.html) |
