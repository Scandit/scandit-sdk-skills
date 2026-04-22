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

Before writing any code, walk the user through their label. Ask one question at a time.

**Question A — What's on your label?** Present this checklist of supported field types and ask the user to pick everything that applies.

*Barcode fields:*
- `CustomBarcodeBuilder` — any barcode, user chooses symbologies
- `ImeiOneBarcodeBuilder` — IMEI 1 (typically for smartphone boxes)
- `ImeiTwoBarcodeBuilder` — IMEI 2
- `PartNumberBarcodeBuilder` — part number
- `SerialNumberBarcodeBuilder` — serial number

*Text fields (preset recognisers):*
- `ExpiryDateTextBuilder` — expiry date (with configurable date format)
- `PackingDateTextBuilder` — packing date
- `DateTextBuilder` — generic date
- `WeightTextBuilder` — weight
- `UnitPriceTextBuilder` — unit price
- `TotalPriceTextBuilder` — total price

*Text fields (custom):*
- `CustomTextBuilder` — any text, user provides a regex

**Question B — For each selected field:**
- Is it **required** or **optional**? (required = label is not considered captured until this field matches; optional = captured when/if it matches)
- For `CustomBarcodeBuilder`: which **symbologies**? Mention to the user that enabling only the symbologies they actually need improves scanning performance and accuracy.
- For `CustomTextBuilder`: what **regex pattern** should the text match?

**Question C — Which file should the integration code go in?** Then write the code directly into that file. Do not just show it in chat.

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
