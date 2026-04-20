# SparkScan Web Integration Guide

SparkScan is a pre-built scanning UI for high-volume single-scanning workflows.
It overlays a trigger button on top of any screen so users can scan without leaving their workflow.

## Prerequisites

- Scandit Data Capture SDK for web — add via npm, pnpm or yarn package manager:
  - URL: `https://www.npmjs.com/package/@scandit/web-datacapture-core`, `https://www.npmjs.com/package/@scandit/web-datacapture-barcode`
  - Add `@scandit/web-datacapture-core` and `@scandit/web-datacapture-barcode` package products to your target
  - A valid Scandit license key:
  - Sign in at <https://ssl.scandit.com> to generate one
  - No account yet? Sign up at <https://ssl.scandit.com/dashboard/sign-up?p=test>

## Minimal Integration (Web)

Ask the user which barcode symbologies they need to scan. When asking, mention that it's important to only enable the symbologies they actually need, as enabling fewer improves scanning performance and accuracy.

Once the user responds, ask him which file or view controller he'd like to integrate SparkScan into. Then write the integration code directly into that file. Do not just show the code in chat; apply it to the file.

After providing the code, show this setup checklist:

**Setup checklist:**

1. Add `@scandit/web-datacapture-core` and `@scandit/web-datacapture-barcode` via the current user package manager that could be npm, pnpm or yarn: `https://www.npmjs.com/package/@scandit/web-datacapture-core` `https://www.npmjs.com/package/@scandit/web-datacapture-barcode`
2. Replace `-- ENTER YOUR SCANDIT LICENSE KEY HERE --` with your key from <https://ssl.scandit.com>

The code example below is a basic typescript v8 implementation.
If the user is using React, use the React get-started guide and SparkScanReactSample instead (see References).

```typescript
import {
  barcodeCaptureLoader,
  SparkScan,
  SparkScanBarcodeErrorFeedback,
  SparkScanBarcodeSuccessFeedback,
  type SparkScanFeedbackDelegate,
  type SparkScanSession,
  SparkScanSettings,
  SparkScanView,
  SparkScanViewSettings,
  Symbology,
} from "@scandit/web-datacapture-barcode";
import { DataCaptureContext } from "@scandit/web-datacapture-core";

async function run() {
    
    await DataCaptureContext.forLicenseKey(
        "-- ENTER YOUR SCANDIT LICENSE KEY HERE --",
        {
         // or use the cdn https://cdn.jsdelivr.net/npm/@scandit/web-datacapture-barcode@8/sdc-lib/
         libraryLocation: new URL("self-hosted-scandit-sdc-lib", document.baseURI).toString(),
         moduleLoaders: [barcodeCaptureLoader()],
        }
    );

    const settings: SparkScanSettings = new SparkScanSettings();
    settings.enableSymbologies([
        Symbology.EAN13UPCA,
        Symbology.Code128,
        Symbology.QR,
    ]);

    const sparkScan: SparkScan = SparkScan.forSettings(settings);

    sparkScan.addListener({
        didScan: (sparkScan, session) => {
            const barcode = session.newlyRecognizedBarcode;
            if (barcode) {
              console.log("Scanned", barcode.symbology, barcode.data);
            }
        },
    });

    const sparkScanViewSettings = new SparkScanViewSettings();

    const sparkScanView = SparkScanView.forElement(
      document.body,
      DataCaptureContext.sharedInstance,
      sparkScan,
      sparkScanViewSettings
    );

    const feedbackDelegate: SparkScanFeedbackDelegate = {
      getFeedbackForBarcode: (barcode) => {
          if (barcode.data === "5901234123457") {
            return new SparkScanBarcodeErrorFeedback("Invalid barcode.", 60_000);
          } else {
            return new SparkScanBarcodeSuccessFeedback();
          }
      },
    };

    sparkScanView.feedbackDelegate = feedbackDelegate;

    async function mount() {
      await sparkScanView.prepareScanning();
    }

    async function unmount() {
      await sparkScanView.stopScanning();
    }

    return mount().catch(async (error) => {
      console.error(error);
      await unmount();
    });
}

run();

```
