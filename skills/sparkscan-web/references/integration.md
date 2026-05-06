# SparkScan Web Integration Guide

SparkScan is a pre-built scanning UI for high-volume single-scanning workflows.
It overlays a trigger button on top of any screen so users can scan without leaving their workflow.

## Starting from zero? Use the pre-built sample

If the user has no existing app yet, always offer the official sample as the fastest path to a working integration — it already has the correct project structure, dependencies, and best practices in place.

Ask whether they are using React or plain web (TypeScript/JavaScript), then point them to the right sample:

- **Vanilla JS / TypeScript:** <https://github.com/Scandit/datacapture-web-samples/tree/master/01_Single_Scanning_Samples/01_Barcode_Scanning_with_Pre-built_UI/ListBuildingSample>
- **React:** <https://github.com/Scandit/datacapture-web-samples/tree/master/05_Framework_Integration_Samples/SparkScanReactSample>

Tell the user to clone the repo and open the relevant sample folder. Once they have it open, help them:

1. Replace `-- ENTER YOUR SCANDIT LICENSE KEY HERE --` with their key from <https://ssl.scandit.com>
2. Adjust the enabled symbologies to match their use case (remind them to only enable what they need — fewer symbologies means better performance and accuracy)
3. Run `npm install` (or their package manager of choice) and start the app

Only proceed to the manual integration steps below if the user already has an existing project they need to add SparkScan to.

---

## Adding SparkScan to an existing project

### Prerequisites

- Scandit Data Capture SDK for web — add via npm, pnpm or yarn:
  - `@scandit/web-datacapture-core`: <https://www.npmjs.com/package/@scandit/web-datacapture-core>
  - `@scandit/web-datacapture-barcode`: <https://www.npmjs.com/package/@scandit/web-datacapture-barcode>
  - A valid Scandit license key — sign in at <https://ssl.scandit.com> to generate one (no account? sign up at <https://ssl.scandit.com/dashboard/sign-up?p=test>)

Ask the user which barcode symbologies they need to scan. When asking, mention that it's important to only enable the symbologies they actually need, as enabling fewer improves scanning performance and accuracy.

Once the user responds, ask which file or component they'd like to integrate SparkScan into. Then write the integration code directly into that file — do not just show the code in chat; apply it to the file.

After providing the code, show this setup checklist:

**Setup checklist:**

1. Add `@scandit/web-datacapture-core` and `@scandit/web-datacapture-barcode` via your package manager: <https://www.npmjs.com/package/@scandit/web-datacapture-core> <https://www.npmjs.com/package/@scandit/web-datacapture-barcode>
2. Replace `-- ENTER YOUR SCANDIT LICENSE KEY HERE --` with your key from <https://ssl.scandit.com>

The code example below is a basic TypeScript v8 implementation.
If the user is using React, use the React get-started guide and SparkScanReactSample instead (see References).

> **Mount point requirement:** The SparkScan view needs a container with defined dimensions and positioning to render its camera preview and trigger button correctly. If the container has zero or unresolved dimensions, the SparkScan UI will not display.
> - **Vanilla JS:** style the element passed to `SparkScanView.forElement` — e.g. `document.body.style.cssText = "position: fixed; top: 0; left: 0; width: 100%; height: 100%;"`.
> - **React:** wrap `<spark-scan-view>` in a container `<div>` with those styles — e.g. `<div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%' }}>`. The `<spark-scan-view>` element itself does not need the styles.

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

    const sparkScanListener = {
        didScan: (sparkScan, session) => {
            const barcode = session.newlyRecognizedBarcode;
            if (barcode) {
              console.log("Scanned", barcode.symbology, barcode.data);
            }
        },
    };
    sparkScan.addListener(sparkScanListener);

    const sparkScanViewSettings = new SparkScanViewSettings();

    // SparkScan requires the mount element to have defined dimensions and positioning.
    document.body.style.cssText = "position: fixed; top: 0; left: 0; width: 100%; height: 100%;";

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
      sparkScan.removeListener(sparkScanListener);
      await sparkScanView.stopScanning();
    }

    return mount().catch(async (error) => {
      console.error(error);
      await unmount();
    });
}

run();

```

## Capturing the scanned frame image

When users want to display or store the image of the frame that contained the barcode, use `frameData.toBlob()` inside `didScan`.

Two important constraints:
- **Do not `await` the blob conversion.** It can take tens of milliseconds; awaiting it blocks the scan pipeline and degrades throughput. Fire it with `.then()/.catch()` and update the UI when it resolves.
- **JPEG at low quality is the fastest option.** `"image/jpeg"` with quality `0.3` gives a usable thumbnail quickly. Use higher quality only if the image needs to be legible.

```typescript
function didScan(_sparkScan: SparkScan, session: SparkScanSession, frameData: FrameData): Promise<void> {
    const barcode = session.newlyRecognizedBarcode;
    if (!barcode) {
        return;
    }

    const data = barcode.data ?? "";
    const symbology = new SymbologyDescription(barcode.symbology).readableName;

    // JPEG is the fastest format to convert to blob — do not await, it may take a while
    frameData
        .toBlob("image/jpeg", 0.3)
        .then((blob) => {
            addScanResult(blob, data, symbology);
        })
        .catch((error) => {
            console.error(error);
            // Add scan result without image if conversion fails
            addScanResult(null, data, symbology);
        });
}
```

Adapt `addScanResult` to your app's data model — it receives a `Blob | null` image, the barcode data string, and the human-readable symbology name. Import `SymbologyDescription` from `@scandit/web-datacapture-barcode` if not already present.

---

## Custom trigger button: always track view state

When hiding the default trigger button and providing your own, a naive click-toggle approach breaks in practice:

1. User clicks → `startScanning()` → scanner starts ✓
2. Barcode is scanned → SparkScan **automatically** transitions back to idle
3. User clicks again → handler still thinks scanner is active → calls `pauseScanning()` on an already-idle engine → **nothing happens**
4. Another click is needed to restart, leaving the user stuck

**The fix:** always use `SparkScanViewUiListener.didChangeViewState` to keep the button in sync with the actual scanner state. Drive click behavior from the current state, not from a toggled local flag:

```typescript
let currentViewState: SparkScanViewState = SparkScanViewState.Idle;

const uiListener = {
    didChangeViewState: (_view: SparkScanView, viewState: SparkScanViewState) => {
        currentViewState = viewState;
        button.textContent = viewState === SparkScanViewState.Active ? "STOP SCANNING" : "START SCANNING";
    },
};
sparkScanView.uiListener = uiListener;

const handleButtonClick = async () => {
    if (currentViewState === SparkScanViewState.Active) {
        await sparkScanView.pauseScanning();
    } else {
        await sparkScanView.startScanning();
    }
};
button.addEventListener("click", handleButtonClick);

// In the unmount/cleanup function:
async function unmount() {
    button.removeEventListener("click", handleButtonClick);
    sparkScanView.uiListener = null;
    await sparkScanView.stopScanning();
}
```

This ensures that after an automatic state change (e.g. scan completes and engine idles), the next button click does the right thing. Using named references for both the click handler and the UI listener means cleanup is clean and avoids memory leaks.
