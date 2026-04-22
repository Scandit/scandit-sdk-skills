# Design — `label-capture-web` skill

**Status:** approved for implementation
**Date:** 2026-04-22
**Target repo:** `Scandit/scandit-sdk-skills`

## Goal

Add a new skill, `label-capture-web`, that helps an AI coding assistant integrate and maintain Scandit Smart Label Capture on the web (TypeScript / JavaScript). The skill follows the structural pattern already established by `sparkscan-web` and `sparkscan-ios` so the three skills share a predictable shape.

The skill covers two intents:

1. **Integration** — adding Label Capture to a web app from scratch.
2. **Migration** — upgrading an existing Label Capture integration across SDK versions.

Scope is web only; vanilla TypeScript / JavaScript only (no framework-specific variants).

## Non-goals

- **No React-specific reference.** The user may adapt the vanilla output to their framework. A future skill can add React specifics if demand materialises.
- **No other platforms.** iOS, Android, React Native, Flutter, .NET, Cordova, Capacitor are out of scope for this skill.
- **No marketplace.json or repo-level packaging changes** beyond an entry in the `README.md` skills table and usage section.

## File layout

```
skills/label-capture-web/
├── SKILL.md
├── references/
│   ├── integration.md
│   └── migration.md
└── evals/
    ├── integration-evals.json
    ├── migration-evals.json
    └── fixtures/
        ├── empty-app.ts
        ├── before-8.0-regex.ts
        ├── before-8.2-validation-flow.ts
        ├── before-8.5-builders.ts
        ├── package-8.1.json
        └── package-8.2.json
```

Plus three incidental edits:

- `skills/data-capture-sdk/SKILL.md` — add rows to the implementation-skill handoff table for `label-capture-web` (web · Smart Label Capture) and for the already-existing `sparkscan-web` (web · SparkScan) which is currently missing from that table.
- `README.md` — add `label-capture-web` to the skills table and add a "Label Capture Web implementation" block to the usage section.
- No `marketplace.json` edits; that file describes the plugin, not individual skills.

## `SKILL.md`

Mirrors the `sparkscan-web` spine exactly. Differences are limited to name/description, the intent-routing list, and the References table.

**Frontmatter:**

```yaml
name: label-capture-web
description: Use when Label Capture (Smart Label Capture) is involved in a web
  project — whether the user mentions Label Capture directly, or the codebase
  already uses it and something needs to be added, changed, fixed, or customized.
  This includes adding Label Capture to a new web app, defining label structures
  (barcode fields + text fields with regex patterns), handling captured sessions,
  enabling the Validation Flow, or migrating between SDK versions. If the
  project is a web project and Label Capture is in play, use this skill.
license: MIT
metadata:
  author: scandit
  version: "1.0.0"
```

**Body sections, in order:**

1. Title: `# Label Capture Web Skill`.
2. **Critical: Do Not Trust Internal Knowledge** — same copy as `sparkscan-web`, retargeted at the Label Capture API. Call out specifically that `LabelFieldDefinition` regex properties were renamed in v8.1, Validation Flow was redesigned in v8.2, and builders gained ergonomic shorthands in v8.5 — all examples where memorised code paths are likely wrong.
3. **Intent Routing** — two bullets:
    - Integrating Label Capture from scratch → read `references/integration.md` and follow the instructions there.
    - Migrating or upgrading an existing Label Capture integration → read `references/migration.md` and follow the instructions there.
4. **API Usage Policy** — same "only use documented APIs, don't guess URLs, fetch the API index first" copy as `sparkscan-web`.
5. **References table** — four rows:
    - Basic integration → `https://docs.scandit.com/sdks/web/label-capture/get-started/` · `https://github.com/Scandit/datacapture-web-samples/tree/master/03_Advanced_Batch_Scanning_Samples/05_Smart_Label_Capture/LabelCaptureSimpleSample`
    - Label Definitions (fields, regex, presets) → `https://docs.scandit.com/sdks/web/label-capture/label-definitions/`
    - Advanced topics (Validation Flow customization, adaptive recognition, custom overlays) → `https://docs.scandit.com/sdks/web/label-capture/advanced/`
    - Full API reference → Label Capture API (resolve the real URL from the web API index during implementation — do not hand-construct it).

## `references/integration.md`

### Structure

1. **Intro** (2–3 lines). What Label Capture is: extract multiple fields (barcodes + text) from one label in a single scan.
2. **Prerequisites.** Three npm packages (`@scandit/web-datacapture-core`, `@scandit/web-datacapture-barcode`, `@scandit/web-datacapture-label`). License key placeholder + link to `https://ssl.scandit.com`.
3. **Interactive label definition.** Instructions tell the assistant to ask, in order:
    1. *What's on your label?* — present the full supported field vocabulary as a checklist:
        - **Barcode fields:** `CustomBarcodeBuilder`, `ImeiOneBarcodeBuilder`, `ImeiTwoBarcodeBuilder`, `PartNumberBarcodeBuilder`, `SerialNumberBarcodeBuilder`
        - **Text fields (preset):** `ExpiryDateTextBuilder`, `PackingDateTextBuilder`, `DateTextBuilder`, `WeightTextBuilder`, `UnitPriceTextBuilder`, `TotalPriceTextBuilder`
        - **Text fields (custom):** `CustomTextBuilder` (regex-based)
    2. *For each selected field:* required or optional? For `CustomBarcode`: which symbologies? For `CustomText`: what regex? Mention that enabling only needed symbologies improves scanning performance.
    3. *Which file should the integration code go in?* — the assistant writes the code directly into that file, not in chat.
4. **Minimal TS code template — current stable class-builder API.** No Validation Flow.
    - `await DataCaptureContext.forLicenseKey(licenseKey, { libraryLocation, moduleLoaders: [labelCaptureLoader()] })`
    - `const view = new DataCaptureView(); view.connectToElement(el); await view.setContext(context);`
    - `const camera = Camera.pickBestGuess(); await context.setFrameSource(camera); await camera.applySettings(LabelCapture.createRecommendedCameraSettings()); await camera.switchToDesiredState(FrameSourceState.On);`
    - `const settings = await new LabelCaptureSettingsBuilder().addLabel(await new LabelDefinitionBuilder().addCustomBarcode(await new CustomBarcodeBuilder().setSymbology(Symbology.EAN13UPCA).build("Barcode")).addExpiryDateText(await new ExpiryDateTextBuilder().build("Expiry Date")).build("Perishable Product")).build();`
    - `const mode = await LabelCapture.forContext(context, settings);`
    - `await LabelCaptureBasicOverlay.withLabelCaptureForView(mode, view);`
    - `mode.addListener({ didUpdateSession: (labelCapture, session, frameData) => { /* handle session.capturedLabels */ } });`
5. **Setup checklist.** Same shape as `sparkscan-web`:
    1. Install `@scandit/web-datacapture-core`, `@scandit/web-datacapture-barcode`, `@scandit/web-datacapture-label` with the user's package manager.
    2. Replace `-- ENTER YOUR SCANDIT LICENSE KEY HERE --` with a key from `https://ssl.scandit.com`.
    3. Ensure `libraryLocation` points to a self-hosted copy of the SDK library, or use the CDN (`https://cdn.jsdelivr.net/npm/@scandit/web-datacapture-label@8/sdc-lib/`).
6. **Optional: Validation Flow.** Version-aware section. Before writing Validation Flow code, the assistant determines the user's `@scandit/web-datacapture-label` version (read `package.json` if available; otherwise ask). Two code blocks, each clearly labelled; the assistant writes the version-matched one, not both:
    - **v8.2+ (redesigned)** — `LabelCaptureValidationFlowOverlay.withLabelCaptureForView(mode, view)`; `const validationFlowSettings = await LabelCaptureValidationFlowSettings.create();` with new-redesign customisation (placeholder texts via `setPlaceholderTextForLabelDefinition`); listener shape `{ onValidationFlowLabelCaptured, onManualInput }`. Deprecated texts (`requiredFieldErrorText`, `missingFieldsHintText`, `manualInputButtonText`) must not appear.
    - **v8.1 and earlier (original)** — `LabelCaptureValidationFlowOverlay` with `requiredFieldErrorText`, `missingFieldsHintText`, `manualInputButtonText`; original listener shape.
7. **Where to go next.** Links pulled from the SKILL.md References table: Get Started, Label Definitions, Advanced Configurations, sample app.

### Guardrails

- No `pattern` / `patterns` / `dataTypePattern` / `dataTypePatterns` on `LabelFieldDefinition` in generated code. Those names were renamed in v8.1; integration code targets ≥ v8.1.
- No v8.5-only syntax in generated code (factory functions like `label()` / `customBarcode()`, or the single-outer-`await` form with name-in-constructor). Those land in `migration.md` only. The integration code uses the `await new ...Builder().build(name)` form that works across all shipped 8.x SDK versions at the time of writing.

## `references/migration.md`

Four ordered sections. Each section: short "what changed and why", before snippet, after snippet, migration notes.

### 1. v7.6 → v8.0 — `LabelFieldDefinition` regex renames (breaking)

| Old | New |
|---|---|
| `pattern` | `valueRegex` |
| `patterns` | `valueRegexes` |
| `dataTypePattern` | `anchorRegex` |
| `dataTypePatterns` | `anchorRegexes` |

Applies wherever a `LabelFieldDefinition` (or its subclasses like `ExpiryDateTextBuilder`, `TotalPriceTextBuilder`, `CustomTextBuilder`, etc.) configured regex anchors or values. Rename everywhere in the user's codebase; the old property names no longer exist.

### 2. v8.1 → v8.2 — Validation Flow redesign (deprecations)

The Validation Flow UI was redesigned. Three properties on `LabelCaptureValidationFlowSettings` no longer apply to the redesigned flow and are deprecated:

- `requiredFieldErrorText`
- `missingFieldsHintText`
- `manualInputButtonText`

Migration section explains:

- The old API is deprecated but still supported in 8.2.x for users not ready to adopt the redesign. The skill keeps the old-API code path available for exactly this reason (see integration.md §6).
- The new design replaces these three knobs with different customisation points (e.g. placeholder texts via `setPlaceholderTextForLabelDefinition`). Migration offers a before/after that rewrites a minimal Validation Flow integration to the redesign.

### 3. v8.2 → v8.4 — new optional `onValidationFlowResultUpdate` listener (additive, non-breaking)

`LabelCaptureValidationFlowListener` gains an optional method:

```ts
onValidationFlowResultUpdate?(
  type: LabelResultUpdateType,
  fields: LabelField[],
  frameData: FrameData | null
): void;
```

`LabelResultUpdateType` is a new enum shipped alongside. Purely additive — existing listeners continue to work unchanged. Migration section documents the method's signature and points at the docs for when it fires.

### 4. v8.4 → v8.5 — ergonomic builder improvements (additive, non-breaking)

Two changes:

**Builder signatures.** `LabelDefinitionBuilder.addXxx()` now accepts either a pre-built field or the corresponding field builder. `LabelCaptureSettingsBuilder.addLabel()` similarly accepts either a `LabelDefinition` or a `LabelDefinitionBuilder`. Pending builders are resolved inside the top-level `build()` call. Field-builder constructors accept an optional name; `build(name?)` falls back to the constructor-set value if no name is passed.

Before (still valid in 8.5):

```ts
const settings = await new LabelCaptureSettingsBuilder()
  .addLabel(
    await new LabelDefinitionBuilder()
      .addCustomBarcode(await new CustomBarcodeBuilder().setSymbology(Symbology.EAN13UPCA).build("Barcode"))
      .addExpiryDateText(await new ExpiryDateTextBuilder().build("Expiry Date"))
      .build("Perishable Product")
  )
  .build();
```

After:

```ts
const settings = await new LabelCaptureSettingsBuilder()
  .addLabel(
    new LabelDefinitionBuilder("Perishable Product")
      .addCustomBarcode(new CustomBarcodeBuilder("Barcode").setSymbology(Symbology.EAN13UPCA))
      .addExpiryDateText(new ExpiryDateTextBuilder("Expiry Date"))
  )
  .build();
```

**Factory-function helpers.** Sugar over the class-builder API:

```ts
const settings = await labelCaptureSettings()
  .addLabel(
    label("Perishable Product")
      .addCustomBarcode(customBarcode("Barcode").setSymbology(Symbology.EAN13UPCA))
      .addExpiryDateText(expiryDateText("Expiry Date"))
  )
  .build();
```

Each `LabelDefinitionBuilder` and `LabelFieldDefinitionBuilder` type has a matching factory function with the same name but lower-camel-case first letter.

Migration notes emphasise: both styles are fully supported; upgrading is a stylistic preference, not a requirement.

## Evals

Mirror `sparkscan-web/evals/` exactly in shape.

### `evals/integration-evals.json`

LLM-as-judge evals. Each prompt seeds an `empty-app.ts` fixture plus a pinned `package.json` so the assistant can detect the SDK version. Assertions the judge checks include:

- Setup checklist mentions all three npm packages.
- `DataCaptureContext.forLicenseKey(` is present with `libraryLocation` and `moduleLoaders: [labelCaptureLoader()]`.
- License key placeholder is present.
- Class-builder chain is used: `new LabelCaptureSettingsBuilder()`, `new LabelDefinitionBuilder()`, and the correct field-builder classes for the symbologies/fields the user asked for.
- Only the symbologies the prompt requested are enabled (for `customBarcode`).
- `LabelCapture.forContext(` is present.
- `LabelCaptureBasicOverlay.withLabelCaptureForView(` is present.
- A `didUpdateSession` listener is wired onto the mode.
- Camera setup: `Camera.pickBestGuess()`, `applySettings(LabelCapture.createRecommendedCameraSettings())`, `switchToDesiredState(FrameSourceState.On)`.
- No renamed-away regex properties: `pattern` / `patterns` / `dataTypePattern` / `dataTypePatterns` are absent.
- No v8.5-only sugar: `labelCaptureSettings(`, `label(`, `customBarcode(` factory calls are absent. Integration always uses the class-builder form; the sugar is only exercised in the v8.4 → v8.5 migration eval.
- If Validation Flow was requested, the version-matched block was emitted (deprecated texts only for v8.1 fixtures; redesigned API only for v8.2+ fixtures).

At minimum, three eval prompts: one minimal-no-validation, one v8.2+ with Validation Flow, one v8.1 with Validation Flow.

### `evals/migration-evals.json`

Four eval prompts, one per migration section. Each seeds a `before-*.ts` fixture and asserts the assistant rewrote the file to the "after" state described in `migration.md`:

1. v7.6 → v8.0 regex renames — assert all four renames applied; old names gone.
2. v8.1 → v8.2 Validation Flow — assert deprecated text properties removed; redesigned-API customisation applied.
3. v8.2 → v8.4 new listener — assert the new optional method is added only if the user asked for it, and has the correct `(type, fields, frameData)` signature.
4. v8.4 → v8.5 ergonomic builders — assert the rewrite to name-in-constructor + single-outer-`await` style when the user opted in; otherwise the original style is left untouched.

### Fixtures

- `empty-app.ts` — empty TypeScript starter for integration evals.
- `before-8.0-regex.ts` — minimal snippet using `pattern` and `dataTypePattern`.
- `before-8.2-validation-flow.ts` — minimal snippet using `requiredFieldErrorText`, `missingFieldsHintText`, `manualInputButtonText`.
- `before-8.5-builders.ts` — minimal snippet using the verbose `await new Builder().build(name)` chain.
- `package-8.1.json` / `package-8.2.json` — pin `@scandit/web-datacapture-label` so Validation Flow evals can exercise version-aware branches.

## Runtime behaviour (what the assistant does when the skill is invoked)

**Integration path:**

1. Assistant reads `references/integration.md`.
2. Asks the field-definition questions (§3 above), one at a time: fields → required/optional → symbologies or regex per field → target file.
3. If Validation Flow is in scope, detects SDK version from `package.json` (or asks).
4. Writes the filled-in class-builder code directly into the target file.
5. Shows the setup checklist.
6. Adds the version-matched Validation Flow block if the user opted in.
7. Includes a docs link from the References table.

**Migration path:**

1. Assistant reads `references/migration.md`.
2. Identifies which version boundary the user is crossing (by asking or reading `package.json`).
3. Walks the relevant section(s) — before/after snippet, then patches the user's file(s).
4. For §3 and §4, explicitly flags that the change is additive and the user can skip it.
5. Includes a docs link.

## Guardrails (inherited from SKILL.md top-matter)

- No trusting memorised APIs — verify against references before writing code.
- No hand-constructed doc URLs — use the References table, or fetch the API index.
- If an API is not documented in references, fetch the relevant docs page before answering.

## Version-identification strategy

Where the skill needs to branch on SDK version (Validation Flow in `integration.md`; every section of `migration.md`):

1. Prefer reading the user's `package.json` to find `@scandit/web-datacapture-label` version.
2. If unreadable or missing, ask the user directly: "Which version of `@scandit/web-datacapture-label` are you on?"
3. Interpret the version conservatively: apply only the features/APIs available at or below that version. For migrations, the user specifies source and target versions.

## Open items to resolve during implementation

- Confirm the exact URL for the Label Capture Web API reference page on `docs.scandit.com` by fetching the Web API index; do not hand-construct.
- Confirm the exact `setPlaceholderTextForLabelDefinition` (or equivalent) customisation surface on the redesigned v8.2 Validation Flow — from the local SDK source at `/Users/pasquale/work/data-capture-sdk/web/libs/@scandit/web-datacapture-label/src/main/api/LabelCaptureValidationFlow*` — rather than memory.
- Confirm the exported name of each field-builder factory function shipping in v8.5 (spot-checked: `label`, `customBarcode`, `expiryDateText`, `totalPriceText`).
