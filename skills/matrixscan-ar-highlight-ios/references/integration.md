# MatrixScan AR Highlights iOS Integration Guide

MatrixScan AR enables you to build applications and workflows involving augmented reality overlays that highlight barcodes in the camera feed. This guide covers adding highlights to an iOS app. For user interaction with highlights (tap handling), see `user-interaction.md`.

## Step 1: Confirm MatrixScan AR is already integrated

This skill assumes MatrixScan AR is already wired up in the project — it covers highlights only. Before writing anything, search the project for existing MatrixScan AR usage. Grep for, in order, and stop when you get a hit:

1. `BarcodeArView` — the view class; its presence is the strongest signal the integration exists.
2. `BarcodeAr` (without the open paren) — catches both `BarcodeAr(...)` construction calls and property declarations like `var scanner: BarcodeAr!` where the type appears but not the init.

Search the whole project, not just the file the user handed you — the `BarcodeArView` may be created in a helper, factory, or separate module.

- **If found** → MatrixScan AR is already set up. Proceed to **Attaching highlights to an existing `BarcodeArView`** below. Do not re-bootstrap the SDK, the license key, or symbologies.
- **If not found** → MatrixScan AR has not been integrated yet. Stop and tell the user that greenfield MatrixScan AR setup (SPM, license, `DataCaptureContext`, `BarcodeAr`, `BarcodeArView`, symbologies, lifecycle) is handled by the dedicated MatrixScan AR integration skill. Do not attempt to bootstrap it from this file.

If the situation is ambiguous (e.g. `BarcodeAr` is imported but no `BarcodeArView` is instantiated), ask the user before assuming.

## Highlight types

MatrixScan AR provides two concrete highlight types. Both implement `BarcodeArHighlight` and are returned from a `BarcodeArHighlightProvider`:

- `BarcodeArRectangleHighlight` — rectangular overlay around the barcode
- `BarcodeArCircleHighlight` — circular overlay over the barcode

If the user hasn't specified a preference, default to `BarcodeArRectangleHighlight` and mention the circle alternative.

## Attaching highlights to an existing `BarcodeArView`

Use this path when MatrixScan AR is already integrated in the project.

1. Locate the existing `BarcodeArView` instance (usually in a view controller or SwiftUI view). Confirm with the user which file to edit if multiple matches exist.
2. Make that type conform to `BarcodeArHighlightProvider` and assign it to `barcodeArView.highlightProvider`.
3. Do not touch the existing `DataCaptureContext`, `BarcodeAr`, `BarcodeArSettings`, symbology configuration, or lifecycle calls — those are out of scope for a highlights change. In particular, if the existing code uses `DataCaptureContext.initialize(licenseKey:)` + `DataCaptureContext.shared`, leave it alone — that is the recommended pattern, not a migration target.
4. Match the surrounding code's conventions (e.g. `import ScanditBarcodeCapture` umbrella only — no separate `import ScanditCaptureCore`; existing naming for the setup method like `setupRecognition()`).

Minimal addition (UIKit):

```swift
// In the view controller that already owns `barcodeArView`:
barcodeArView.highlightProvider = self
```

`BarcodeArHighlightProvider` offers two equivalent variants — a completion-handler form and an `async` form. Pick the one that matches the surrounding code: if the project already uses `async`/`await` (e.g. nearby methods are `async`, call sites use `Task { }`, or the file imports async APIs), use the async variant; otherwise use the completion handler.

Completion-handler variant:

```swift
extension ViewController: BarcodeArHighlightProvider {
    func highlight(
        for barcode: Barcode,
        completionHandler: @escaping ((any UIView & BarcodeArHighlight)?) -> Void
    ) {
        let highlight = BarcodeArRectangleHighlight(barcode: barcode)
        completionHandler(highlight)
    }
}
```

Async variant:

```swift
extension ViewController: BarcodeArHighlightProvider {
    func highlight(for barcode: Barcode) async -> (any UIView & BarcodeArHighlight)? {
        return BarcodeArRectangleHighlight(barcode: barcode)
    }
}
```

To use circular highlights instead, swap `BarcodeArRectangleHighlight` for `BarcodeArCircleHighlight`. `BarcodeArCircleHighlight` requires a `BarcodeArCircleHighlightPreset` (`.dot` for a smaller blue circle, `.icon` for a larger blue circle — these only set the default visuals):

```swift
let highlight = BarcodeArCircleHighlight(barcode: barcode, preset: .dot)
```

If the project already assigns a `highlightProvider`, ask the user whether they want to replace it or modify the existing one before overwriting.

## Customizing highlight appearance

Both highlight types expose customization properties (e.g. brush, icon, and — for the circle — size and pulsing). **Do not rely on memorized property names, types, or defaults.** When the user asks for any appearance change, fetch the relevant highlight class's API page and use the properties documented there. Include the link in your answer.

| Topic | Page to fetch |
|---|---|
| `BarcodeArRectangleHighlight` properties | https://docs.scandit.com/data-capture-sdk/ios/barcode-capture/api/ui/barcode-ar-highlight-rectangle.html |
| `BarcodeArCircleHighlight` properties (including `BarcodeArCircleHighlightPreset`) | https://docs.scandit.com/data-capture-sdk/ios/barcode-capture/api/ui/barcode-ar-highlight-circle.html |
| `Brush` (fill / stroke / stroke width) — used by both highlights | https://docs.scandit.com/data-capture-sdk/ios/core/api/ui/brush.html |
| `ScanditIcon` and `ScanditIconBuilder` (icon overlays) | https://docs.scandit.com/data-capture-sdk/ios/core/api/ui/scandit-icon.html |

Apply customizations on the highlight instance inside the provider before passing it to the completion handler (or returning it, in the async variant). If a property or initializer needed for the user's request isn't on the page you fetched, follow the URL guessing policy in `SKILL.md` — check for direct links first, otherwise consult the API index.
