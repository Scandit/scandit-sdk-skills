---
name: sparkscan-ios
description: Guide for integrating SparkScan on iOS using the Scandit Data Capture SDK. Use when a developer asks how to add SparkScan to an iOS app, set up barcode scanning, or handle scan results.
license: MIT
metadata:
  author: scandit
  version: "1.0.1"
---

# SparkScan iOS Integration Guide

SparkScan is a pre-built scanning UI for high-volume single-scanning workflows. It overlays a trigger button on top of any screen so users can scan without leaving their workflow.

## Prerequisites

- Scandit Data Capture SDK for iOS — add via Swift Package Manager:
  - URL: `https://github.com/Scandit/datacapture-spm`
  - Add `ScanditBarcodeCapture` and `ScanditCaptureCore` package products to your target
- A valid Scandit license key:
  - Sign in at https://ssl.scandit.com to generate one
  - No account yet? Sign up at https://ssl.scandit.com/dashboard/sign-up?p=test
- `NSCameraUsageDescription` in `Info.plist`

## Minimal Integration (Swift)

Ask the user which barcode symbologies they need to scan. When asking, mention that it's important to only enable the symbologies they actually need, as enabling fewer improves scanning performance and accuracy.

Once the user responds, ask them which file or view controller they'd like to integrate SparkScan into. Then write the integration code directly into that file. Do not just show the code in chat; apply it to the file.

After providing the code, show this setup checklist:

**Setup checklist:**
1. Add `ScanditBarcodeCapture` and `ScanditCaptureCore` via Swift Package Manager: `https://github.com/Scandit/datacapture-spm`
2. Make sure you have `NSCameraUsageDescription` added to your `Info.plist`
3. Replace `-- ENTER YOUR SCANDIT LICENSE KEY HERE --` with your key from https://ssl.scandit.com

The code example below is for UIKit. If the user is using SwiftUI, refer them to the SwiftUI get-started guide and sample instead (see References).

```swift
import ScanditBarcodeCapture

class ViewController: UIViewController {

    private lazy var context = {
        // Enter your Scandit License key here.
        // Your Scandit License key is available via your Scandit SDK web account.
        DataCaptureContext.initialize(licenseKey: "-- ENTER YOUR SCANDIT LICENSE KEY HERE --")
        return DataCaptureContext.shared
    }()

    private lazy var sparkScan: SparkScan = {
        let settings = SparkScanSettings()
        Set<Symbology>([.ean8, .ean13UPCA, .upce, .code39, .code128, .interleavedTwoOfFive]).forEach {
            settings.set(symbology: $0, enabled: true)
        }
        settings.settings(for: .code39).activeSymbolCounts = Set(7...20)

        let mode = SparkScan(settings: settings)
        return mode
    }()

    private var sparkScanView: SparkScanView!

    override func viewDidLoad() {
        super.viewDidLoad()
        setupRecognition()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        sparkScanView.prepareScanning()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        sparkScanView.stopScanning()
    }

    private func setupRecognition() {
        sparkScan.addListener(self)
        sparkScanView = SparkScanView(
            parentView: view,
            context: context,
            sparkScan: sparkScan,
            settings: SparkScanViewSettings()
        )
    }
}

extension ViewController: SparkScanListener {
    func sparkScan(_ sparkScan: SparkScan, didScanIn session: SparkScanSession, frameData: FrameData?) {
        guard let barcode = session.newlyRecognizedBarcode, let data = barcode.data else { return }
        DispatchQueue.main.async {
            print("Scanned barcode - \(data)")
            // Handle the barcode
        }
    }
}
```

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
