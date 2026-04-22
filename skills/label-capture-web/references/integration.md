# Label Capture Web Integration Guide

Label Capture (Smart Label Capture) extracts multiple fields from a single label in one scan — e.g. a barcode, an expiry date, and a total price on a grocery label. You declare the structure of the label (which fields, required/optional, barcode symbologies or text regex) and the SDK returns all matched fields per frame.

## Prerequisites

- Scandit Data Capture SDK for web — install three npm packages with your user's package manager (npm, pnpm, or yarn):
  - `@scandit/web-datacapture-core`
  - `@scandit/web-datacapture-barcode`
  - `@scandit/web-datacapture-label`
- A valid Scandit license key:
  - Sign in at <https://ssl.scandit.com> to generate one.
  - No account yet? Sign up at <https://ssl.scandit.com/dashboard/sign-up?p=test>.

## Interactive Label Definition

<!-- Filled in by Task 2.2 -->

## Minimal Integration (Web)

<!-- Filled in by Task 2.3 -->

## Setup Checklist

<!-- Filled in by Task 2.3 -->

## Optional: Validation Flow

<!-- Filled in by Task 2.4 -->

## Where to Go Next

After the core integration is running, point the user at the right resource for follow-ups:

- [Label Definitions](https://docs.scandit.com/sdks/web/label-capture/label-definitions/) — full catalogue of pre-built text/barcode field types and how to tune their regex anchors and value patterns.
- [Advanced Configurations](https://docs.scandit.com/sdks/web/label-capture/advanced/) — Validation Flow customisation, adaptive recognition, custom overlays.
- [LabelCaptureSimpleSample](https://github.com/Scandit/datacapture-web-samples/tree/master/03_Advanced_Batch_Scanning_Samples/05_Smart_Label_Capture/LabelCaptureSimpleSample) — working reference sample.
