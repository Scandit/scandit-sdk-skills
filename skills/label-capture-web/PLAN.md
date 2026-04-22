# `label-capture-web` Skill Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a new `label-capture-web` skill in `scandit-sdk-skills` that helps AI coding assistants integrate and migrate Scandit Smart Label Capture in web (TypeScript/JavaScript) projects, following the structural pattern of `sparkscan-web`.

**Architecture:** Content-only skill. A `SKILL.md` dispatches to one of two reference files (`integration.md`, `migration.md`) based on user intent. Eval JSON files assert what generated code must look like when an LLM judge runs the skill against seeded fixture files. Follow the `sparkscan-web` shape exactly for consistency.

**Tech Stack:** Markdown (SKILL.md, references), JSON (evals, fixture package.json), TypeScript (fixture .ts files). No runtime code. Evals are declarative — executed by an external LLM-as-judge harness, not in-tree.

**Spec:** `skills/label-capture-web/DESIGN.md` (committed in `edfbbc7`).

---

## File Structure

Target layout:

```
skills/label-capture-web/
├── DESIGN.md                                      (already committed)
├── PLAN.md                                        (this file)
├── SKILL.md
├── references/
│   ├── integration.md
│   └── migration.md
└── evals/
    ├── integration-evals.json
    ├── migration-evals.json
    └── fixtures/
        ├── empty-app.ts
        ├── package-8.1.json
        ├── package-8.2.json
        ├── before-8.1-regex.ts
        ├── before-8.2-validation-flow.ts
        └── before-8.5-builders.ts
```

Plus two incidental files to modify:

- `skills/data-capture-sdk/SKILL.md` — handoff table gets two new rows (`sparkscan-web`, `label-capture-web`)
- `README.md` — skills table and usage section gain `label-capture-web` entries

Each reference file has a single responsibility: `integration.md` for integration flows, `migration.md` for version-upgrade flows. Fixtures and evals are one file per scenario. `SKILL.md` is the dispatch hub and never contains integration code itself — that's what references are for.

---

## Phase 0 — Verify open items from DESIGN.md

These three items in the design spec's "Open items" section must be resolved before writing reference content. Don't bake unverified API names into the skill.

### Task 0.1: Resolve Label Capture Web API reference URL

**Files:** none yet (research task).

- [ ] **Step 1: Fetch the Web API index**

Run (WebFetch tool):
```
URL: https://docs.scandit.com/data-capture-sdk/web/barcode-capture/api.html
Prompt: "Return the exact hyperlink to the Label Capture (Smart Label Capture) section. I need the link to the page that indexes LabelCapture, LabelCaptureSettings, LabelCaptureListener, LabelCaptureValidationFlowListener, LabelField and related classes. Return just the URL."
```
Expected: a concrete `docs.scandit.com/...` URL. Record it for Task 1.1.

- [ ] **Step 2: Verify the URL resolves**

Run (WebFetch tool) with the URL from Step 1 and prompt: "Return the first H1 heading and list the top 5 class names in the index." Expected: labels like `LabelCapture`, `LabelCaptureSettings`, etc. If it 404s, try the web API index root `https://docs.scandit.com/data-capture-sdk/web/label-capture/api.html` instead.

- [ ] **Step 3: Record the resolved URL**

No file change yet — hold the URL in task notes. It'll go into `SKILL.md` References table in Task 1.1.

### Task 0.2: Confirm redesigned Validation Flow customisation API

**Files:** none yet (research task). Read-only from local SDK source.

- [ ] **Step 1: Inspect `LabelCaptureValidationFlowSettings` source**

Run:
```bash
cat /Users/pasquale/work/data-capture-sdk/web/libs/@scandit/web-datacapture-label/src/main/api/LabelCaptureValidationFlowSettings.ts
```
Expected: the file contents, including `create()`, `setPlaceholderTextForLabelDefinition(name, placeholder)`, and the listener interface shape.

- [ ] **Step 2: Inspect the listener interface**

Run:
```bash
ls /Users/pasquale/work/data-capture-sdk/web/libs/@scandit/web-datacapture-label/src/main/api/LabelCaptureValidationFlow/
cat /Users/pasquale/work/data-capture-sdk/web/libs/@scandit/web-datacapture-label/src/main/api/LabelCaptureValidationFlow/LabelCaptureValidationFlowListener.ts 2>/dev/null || find /Users/pasquale/work/data-capture-sdk/web/libs/@scandit/web-datacapture-label/src -name "*ValidationFlowListener*"
```
Expected: the `LabelCaptureValidationFlowListener` interface definition with `onValidationFlowLabelCaptured`, `onManualInput`, and any additional optional methods.

- [ ] **Step 3: Record exact method signatures**

No file change — record signatures to use verbatim in `integration.md` (Task 2.4) and `migration.md` (Tasks 3.2, 3.3).

### Task 0.3: Confirm v8.5 factory-function exports

**Files:** none yet (research task).

- [ ] **Step 1: Grep the label package for factory-function exports**

Run:
```bash
grep -rn "^export function \(label\|customBarcode\|expiryDateText\|totalPriceText\|weightText\|unitPriceText\|customText\|packingDateText\|dateText\|imeiOneBarcode\|imeiTwoBarcode\|partNumberBarcode\|serialNumberBarcode\|labelCaptureSettings\)" /Users/pasquale/work/data-capture-sdk/web/libs/@scandit/web-datacapture-label/src/main/
```
Expected: one line per exported factory function. Record the exact list — these are what `migration.md` §4 references.

- [ ] **Step 2: If any expected factory is missing, note it**

Record which helpers are NOT yet exported. `migration.md` §4 will describe only the ones that exist.

---

## Phase 1 — Scaffold skill folder and SKILL.md

### Task 1.1: Create SKILL.md

**Files:**
- Create: `skills/label-capture-web/SKILL.md`

- [ ] **Step 1: Write the file**

Write `skills/label-capture-web/SKILL.md` with the following exact content (substitute `<API_URL>` with the URL resolved in Task 0.1):

```markdown
---
name: label-capture-web
description: Use when Label Capture (Smart Label Capture) is involved in a web project — whether the user mentions Label Capture directly, or the codebase already uses it and something needs to be added, changed, fixed, or customized. This includes adding Label Capture to a new web app, defining label structures (barcode fields + text fields with regex patterns), handling captured sessions, enabling the Validation Flow, or migrating between SDK versions. If the project is a web project and Label Capture is in play, use this skill.
license: MIT
metadata:
  author: scandit
  version: "1.0.0"
---

# Label Capture Web Skill

## Critical: Do Not Trust Internal Knowledge

Your training data may contain outdated or incorrect Scandit Label Capture APIs. Label Capture has evolved across recent SDK releases:

- In v8.1, `LabelFieldDefinition` regex properties were renamed (`pattern`→`valueRegex`, `patterns`→`valueRegexes`, `dataTypePattern`→`anchorRegex`, `dataTypePatterns`→`anchorRegexes`).
- In v8.2, the Validation Flow UI was redesigned and three customisation properties were deprecated.
- In v8.5, additive ergonomic shorthands were introduced for the builders.

**Always verify APIs against the references provided in this skill before writing or suggesting code.** Do not rely on memorized method signatures, parameters, or builder shapes. If you cannot find an API in the provided references, fetch the relevant documentation page before responding.

## Intent Routing

Based on the user's request, load the appropriate reference file before responding:

- **Integrating Label Capture from scratch** (e.g. "add Label Capture to my app", "scan a price tag with barcode and expiry date", "how do I use Smart Label Capture", "how do I enable the Validation Flow") → read `references/integration.md` and follow the instructions there.
- **Migrating or upgrading an existing Label Capture integration** (e.g. "upgrade my Label Capture to the latest SDK", "migrate from v8.1 to v8.2", "what changed between SDK versions for Label Capture") → read `references/migration.md` and follow the instructions there.

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
| Basic integration | [Get Started](https://docs.scandit.com/sdks/web/label-capture/get-started/) · [Sample (LabelCaptureSimpleSample)](https://github.com/Scandit/datacapture-web-samples/tree/master/03_Advanced_Batch_Scanning_Samples/05_Smart_Label_Capture/LabelCaptureSimpleSample) |
| Label Definitions (fields, regex, presets) | [Label Definitions](https://docs.scandit.com/sdks/web/label-capture/label-definitions/) |
| Advanced topics (Validation Flow customization, adaptive recognition, custom overlays) | [Advanced Configurations](https://docs.scandit.com/sdks/web/label-capture/advanced/) |
| Full API reference | [Label Capture API](<API_URL>) |
```

- [ ] **Step 2: Verify structure**

Run:
```bash
head -8 skills/label-capture-web/SKILL.md
grep -c "^##" skills/label-capture-web/SKILL.md
```
Expected: frontmatter starts with `---`, `name: label-capture-web`, and 5 `##`-level headings (`Critical`, `Intent Routing`, `API Usage Policy`, `References`).

- [ ] **Step 3: Commit**

```bash
git add skills/label-capture-web/SKILL.md
git commit -m "Add label-capture-web SKILL.md (entry point + intent routing)"
```

---

## Phase 2 — Write `references/integration.md`

This file has seven sections (per DESIGN.md §integration.md). Build it section by section.

### Task 2.1: Create integration.md scaffold (sections 1, 2, 7)

**Files:**
- Create: `skills/label-capture-web/references/integration.md`

- [ ] **Step 1: Write intro, prerequisites, and "Where to go next" sections**

Write the file with these three sections (leaving markers for the other four, which later tasks will fill in):

```markdown
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
```

- [ ] **Step 2: Commit**

```bash
git add skills/label-capture-web/references/integration.md
git commit -m "Add integration.md scaffold with intro, prerequisites, and next-steps"
```

### Task 2.2: Fill in the Interactive Label Definition section

**Files:**
- Modify: `skills/label-capture-web/references/integration.md`

- [ ] **Step 1: Replace the `<!-- Filled in by Task 2.2 -->` marker with this content**

```markdown
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
```

- [ ] **Step 2: Commit**

```bash
git add skills/label-capture-web/references/integration.md
git commit -m "Add interactive label-definition flow to integration.md"
```

### Task 2.3: Fill in Minimal Integration + Setup Checklist

**Files:**
- Modify: `skills/label-capture-web/references/integration.md`

- [ ] **Step 1: Replace the two markers**

Replace the `<!-- Filled in by Task 2.3 -->` marker under "Minimal Integration (Web)" with:

````markdown
Once the user has answered Questions A, B, and C, generate the integration code using the class-builder API. This form works across all shipped 8.x versions. Substitute the placeholders `[LABEL_NAME]`, `[FIELD_NAME]`, and the correct field builders based on the user's answers. Fields marked optional should call `.setIsOptional(true)`; required fields can omit the call (required is the default) or call `.setIsOptional(false)` explicitly for clarity.

```typescript
import { Symbology } from "@scandit/web-datacapture-barcode";
import { Camera, DataCaptureContext, DataCaptureView, FrameSourceState } from "@scandit/web-datacapture-core";
import {
  CustomBarcodeBuilder,
  ExpiryDateTextBuilder,
  LabelCapture,
  LabelCaptureBasicOverlay,
  LabelCaptureSettingsBuilder,
  LabelDefinitionBuilder,
  labelCaptureLoader,
  type LabelCaptureSession,
  type LabelField,
} from "@scandit/web-datacapture-label";

async function run() {
  const view = new DataCaptureView();
  view.connectToElement(document.getElementById("data-capture-view")!);

  const context = await DataCaptureContext.forLicenseKey(
    "-- ENTER YOUR SCANDIT LICENSE KEY HERE --",
    {
      libraryLocation: new URL("self-hosted-scandit-sdc-lib", document.baseURI).toString(),
      moduleLoaders: [labelCaptureLoader()],
    }
  );
  await view.setContext(context);

  const camera = Camera.pickBestGuess();
  await context.setFrameSource(camera);
  await camera.applySettings(LabelCapture.createRecommendedCameraSettings());
  await camera.switchToDesiredState(FrameSourceState.On);

  const settings = await new LabelCaptureSettingsBuilder()
    .addLabel(
      await new LabelDefinitionBuilder()
        .addCustomBarcode(
          await new CustomBarcodeBuilder()
            .setSymbologies([Symbology.EAN13UPCA, Symbology.Code128])
            .setIsOptional(false)
            .build("Barcode")
        )
        .addExpiryDateText(
          await new ExpiryDateTextBuilder()
            .setIsOptional(false)
            .build("Expiry Date")
        )
        .build("Perishable Product")
    )
    .build();

  const mode = await LabelCapture.forContext(context, settings);

  await LabelCaptureBasicOverlay.withLabelCaptureForView(mode, view);

  mode.addListener({
    didUpdateSession: (_labelCapture, session: LabelCaptureSession, _frameData) => {
      for (const capturedLabel of session.capturedLabels) {
        for (const field of capturedLabel.fields as LabelField[]) {
          console.log(field.name, "=", field.barcode?.data ?? field.text);
        }
      }
    },
  });
}

run();
```

Notes when generating this code:

- Import ONLY the field builders the user actually selected (`CustomBarcodeBuilder`, `ExpiryDateTextBuilder`, etc.). Do not import unused ones.
- The corresponding `addXxx` method on `LabelDefinitionBuilder` mirrors the field type: `addCustomBarcode`, `addExpiryDateText`, `addWeightText`, `addUnitPriceText`, `addTotalPriceText`, `addCustomText`, `addPackingDateText`, `addDateText`, `addImeiOneBarcode`, `addImeiTwoBarcode`, `addPartNumberBarcode`, `addSerialNumberBarcode`.
- For `CustomBarcodeBuilder`, use `setSymbologies([...])` with the symbologies the user selected. For a single symbology, `setSymbology(Symbology.X)` is also valid.
- For `CustomTextBuilder`, use `.setValueRegex(pattern)` (or `.setValueRegexes([patterns])` for multiple). Do not use `.setPattern` or `.setPatterns` — those names were renamed in v8.1 and no longer exist.
- Do NOT use the factory-function sugar (`label(...)`, `customBarcode(...)`, `labelCaptureSettings()`) — that shorthand is only guaranteed from v8.5. The class-builder form above works on all 8.x versions.
````

Replace the `<!-- Filled in by Task 2.3 -->` marker under "Setup Checklist" with:

```markdown
After writing the integration code, show this checklist:

1. Install the three npm packages with the user's package manager (npm, pnpm, or yarn):
   - `@scandit/web-datacapture-core`
   - `@scandit/web-datacapture-barcode`
   - `@scandit/web-datacapture-label`
2. Replace `-- ENTER YOUR SCANDIT LICENSE KEY HERE --` with your license key from <https://ssl.scandit.com>.
3. Make sure `libraryLocation` points to a self-hosted copy of the SDK library (the path in `new URL(...)`). You can copy the `sdc-lib` directory from `node_modules/@scandit/web-datacapture-label/sdc-lib/`, or use the CDN instead: `libraryLocation: "https://cdn.jsdelivr.net/npm/@scandit/web-datacapture-label@8/sdc-lib/"`.
4. Ensure a DOM element with id `data-capture-view` exists on the page before `run()` executes.
```

- [ ] **Step 2: Commit**

```bash
git add skills/label-capture-web/references/integration.md
git commit -m "Add minimal integration code and setup checklist to integration.md"
```

### Task 2.4: Fill in the version-aware Validation Flow section

**Files:**
- Modify: `skills/label-capture-web/references/integration.md`

- [ ] **Step 1: Replace the `<!-- Filled in by Task 2.4 -->` marker**

Replace with the following. Use the exact listener method signatures resolved in Task 0.2.

````markdown
If the user wants to confirm OCR results, manually correct errors, or capture missing fields without rescanning, enable the Validation Flow. Skip this section if the user is fine with the minimal scan-and-handle path above.

**Before writing Validation Flow code, determine the user's SDK version.** Prefer reading `package.json` for `@scandit/web-datacapture-label`. If that is unreadable or missing, ask: "Which version of `@scandit/web-datacapture-label` are you on?" Then write the version-matched block below — write only one, not both.

### v8.2+ — Redesigned Validation Flow

Import the Validation Flow classes and replace the `LabelCaptureBasicOverlay` line in the minimal integration with the Validation Flow overlay. Replace the existing `mode.addListener` block with the redesigned listener.

```typescript
import {
  LabelCaptureValidationFlowOverlay,
  LabelCaptureValidationFlowSettings,
  type LabelCaptureValidationFlowListener,
  type LabelField,
} from "@scandit/web-datacapture-label";

const overlay = await LabelCaptureValidationFlowOverlay.withLabelCaptureForView(mode, view);

const validationFlowSettings = await LabelCaptureValidationFlowSettings.create();
await validationFlowSettings.setPlaceholderTextForLabelDefinition("Expiry Date", "MM.DD.YY");
await validationFlowSettings.setPlaceholderTextForLabelDefinition("Total Price", "e.g., $13.66");
await overlay.applySettings(validationFlowSettings);

overlay.listener = {
  onManualInput: (_field: LabelField, _oldValue: string | undefined, _newValue: string) => {
    // User manually entered or corrected a value for a field.
  },
  onValidationFlowLabelCaptured: (fields: LabelField[]) => {
    for (const field of fields) {
      console.log(field.name, "=", field.barcode?.data ?? field.text);
    }
  },
} satisfies LabelCaptureValidationFlowListener;
```

Do NOT set `requiredFieldErrorText`, `missingFieldsHintText`, or `manualInputButtonText` — those properties were removed in the v8.2 redesign. Use `setPlaceholderTextForLabelDefinition` (and other methods on `LabelCaptureValidationFlowSettings`) for customisation.

### v8.1 and earlier — Original Validation Flow

For users still on v8.1 or earlier, the customisation surface is different. Keep `LabelCaptureBasicOverlay` (or replace it with `LabelCaptureValidationFlowOverlay` if the user wants validation UI), and configure the older text properties directly on the Validation Flow settings:

```typescript
import {
  LabelCaptureValidationFlowOverlay,
  LabelCaptureValidationFlowSettings,
  type LabelCaptureValidationFlowListener,
  type LabelField,
} from "@scandit/web-datacapture-label";

const overlay = await LabelCaptureValidationFlowOverlay.withLabelCaptureForView(mode, view);

const validationFlowSettings = await LabelCaptureValidationFlowSettings.create();
validationFlowSettings.requiredFieldErrorText = "This field is required";
validationFlowSettings.missingFieldsHintText = "Please fill the missing fields";
validationFlowSettings.manualInputButtonText = "Enter manually";
await overlay.applySettings(validationFlowSettings);

overlay.listener = {
  onManualInput: (_field: LabelField, _oldValue: string | null, _newValue: string) => {
    // User manually entered or corrected a value for a field.
  },
  onValidationFlowLabelCaptured: (fields: LabelField[]) => {
    for (const field of fields) {
      console.log(field.name, "=", field.barcode?.data ?? field.text);
    }
  },
} satisfies LabelCaptureValidationFlowListener;
```

If the user plans to upgrade to v8.2+, route them to the migration guide (`migration.md` §2) for the rewrite.
````

- [ ] **Step 2: Commit**

```bash
git add skills/label-capture-web/references/integration.md
git commit -m "Add version-aware Validation Flow section to integration.md"
```

---

## Phase 3 — Write `references/migration.md`

### Task 3.1: Create migration.md scaffold + §1 (v8.0 → v8.1 regex renames)

**Files:**
- Create: `skills/label-capture-web/references/migration.md`

- [ ] **Step 1: Write the file**

```markdown
# Label Capture Web Migration Guide

When a user asks to upgrade or migrate a Label Capture integration, identify which version boundary they're crossing. Prefer reading `package.json` for `@scandit/web-datacapture-label`. Otherwise ask directly: "Which version are you on, and which version are you upgrading to?"

The sections below are cumulative — if the user is going from v8.0 to v8.5, walk them through §1, §2, §3, and §4 in order.

## 1. v8.0 → v8.1 — `LabelFieldDefinition` regex renames (breaking)

The regex-configuration properties on every `LabelFieldDefinition` subclass were renamed. The old names no longer exist.

| Old | New |
|---|---|
| `pattern` | `valueRegex` |
| `patterns` | `valueRegexes` |
| `dataTypePattern` | `anchorRegex` |
| `dataTypePatterns` | `anchorRegexes` |

The same rename applies to the matching setter methods on every field builder (`CustomTextBuilder`, `ExpiryDateTextBuilder`, `TotalPriceTextBuilder`, `WeightTextBuilder`, etc.): `setPattern` → `setValueRegex`, `setPatterns` → `setValueRegexes`, `setDataTypePattern` → `setAnchorRegex`, `setDataTypePatterns` → `setAnchorRegexes`.

### Before (v8.0)

```typescript
const expiryBuilder = new ExpiryDateTextBuilder()
  .setDataTypePattern("EXP[:\\s]+")
  .setPattern("\\d{2}/\\d{2}/\\d{2,4}");
```

### After (v8.1+)

```typescript
const expiryBuilder = new ExpiryDateTextBuilder()
  .setAnchorRegex("EXP[:\\s]+")
  .setValueRegex("\\d{2}/\\d{2}/\\d{2,4}");
```

Apply the rename across every field definition in the user's codebase. No other logic changes.
```

- [ ] **Step 2: Commit**

```bash
git add skills/label-capture-web/references/migration.md
git commit -m "Add migration.md with §1 v8.0→v8.1 regex renames"
```

### Task 3.2: Add §2 (v8.1 → v8.2 Validation Flow redesign)

**Files:**
- Modify: `skills/label-capture-web/references/migration.md`

- [ ] **Step 1: Append §2 to migration.md**

```markdown

## 2. v8.1 → v8.2 — Validation Flow redesign (deprecations, non-breaking)

The Validation Flow UI was redesigned in v8.2. Three text-customisation properties on `LabelCaptureValidationFlowSettings` no longer apply to the redesigned flow and are deprecated:

- `requiredFieldErrorText`
- `missingFieldsHintText`
- `manualInputButtonText`

The old properties are still present in the 8.2.x line for backward compatibility — users not ready to adopt the redesign can stay on them. New customisation points in the redesigned flow include `setPlaceholderTextForLabelDefinition(name, placeholder)` for per-field placeholder hints.

### Before (v8.1)

```typescript
const validationFlowSettings = await LabelCaptureValidationFlowSettings.create();
validationFlowSettings.requiredFieldErrorText = "This field is required";
validationFlowSettings.missingFieldsHintText = "Please fill the missing fields";
validationFlowSettings.manualInputButtonText = "Enter manually";
await overlay.applySettings(validationFlowSettings);
```

### After (v8.2+, redesigned)

```typescript
const validationFlowSettings = await LabelCaptureValidationFlowSettings.create();
await validationFlowSettings.setPlaceholderTextForLabelDefinition("Expiry Date", "MM.DD.YY");
await validationFlowSettings.setPlaceholderTextForLabelDefinition("Total Price", "e.g., $13.66");
await overlay.applySettings(validationFlowSettings);
```

If the user prefers to keep the old flow in 8.2.x, leave their code as-is. Only rewrite to the redesigned API when the user explicitly asks to adopt the new UI.
```

- [ ] **Step 2: Commit**

```bash
git add skills/label-capture-web/references/migration.md
git commit -m "Add migration.md §2 v8.1→v8.2 Validation Flow redesign"
```

### Task 3.3: Add §3 (v8.2 → v8.4 new optional listener)

**Files:**
- Modify: `skills/label-capture-web/references/migration.md`

- [ ] **Step 1: Append §3**

```markdown

## 3. v8.2 → v8.4 — new optional `onValidationFlowResultUpdate` listener (additive, non-breaking)

`LabelCaptureValidationFlowListener` gains an optional method that fires when a validation-flow result is updated during capture. Existing listeners continue to work unchanged.

```typescript
export interface LabelCaptureValidationFlowListener {
  onValidationFlowLabelCaptured(fields: LabelField[]): void;
  onManualInput(field: LabelField, oldValue: string | undefined, newValue: string): void;
  onValidationFlowResultUpdate?(
    type: LabelResultUpdateType,
    fields: LabelField[],
    frameData: FrameData | null
  ): void;
}
```

`LabelResultUpdateType` is a new enum shipped alongside the listener method — import it from `@scandit/web-datacapture-label` when you use the new method.

### When to add it

The new method is useful if the user needs fine-grained progress feedback as the Validation Flow accumulates partial results (e.g. to update UI during the flow rather than only at its end). If the user doesn't ask for that, leave their listener alone — the method is optional.

### Example — adding the method

```typescript
import {
  type LabelCaptureValidationFlowListener,
  type LabelField,
  LabelResultUpdateType,
} from "@scandit/web-datacapture-label";
import type { FrameData } from "@scandit/web-datacapture-core";

overlay.listener = {
  onManualInput: (_field, _oldValue, _newValue) => { /* ... */ },
  onValidationFlowLabelCaptured: (_fields) => { /* ... */ },
  onValidationFlowResultUpdate: (
    type: LabelResultUpdateType,
    fields: LabelField[],
    _frameData: FrameData | null
  ) => {
    // Fires when the validation flow's current result is updated.
    // See LabelResultUpdateType for the update kind (e.g. added, modified).
  },
} satisfies LabelCaptureValidationFlowListener;
```
```

- [ ] **Step 2: Commit**

```bash
git add skills/label-capture-web/references/migration.md
git commit -m "Add migration.md §3 v8.2→v8.4 onValidationFlowResultUpdate listener"
```

### Task 3.4: Add §4 (v8.4 → v8.5 ergonomic builder improvements)

**Files:**
- Modify: `skills/label-capture-web/references/migration.md`

- [ ] **Step 1: Append §4**

Use the exact factory-function names verified in Task 0.3. If any expected factory is missing from the actual SDK exports, remove it from the "factory-function helpers" code block.

````markdown

## 4. v8.4 → v8.5 — ergonomic builder improvements (additive, non-breaking)

Two purely additive changes. All existing code continues to work — upgrading is a stylistic preference.

### Change A — inline builders, name in constructor, single outer `await`

`LabelDefinitionBuilder.addXxx()` now accepts either a pre-built field or the corresponding field builder. `LabelCaptureSettingsBuilder.addLabel()` similarly accepts either a `LabelDefinition` or a `LabelDefinitionBuilder`. Pending builders are resolved inside the top-level `build()` call. Field-builder constructors also accept an optional name; `build(name?)` falls back to the constructor-set value if no name is passed.

**Before (still valid in 8.5):**

```typescript
const settings = await new LabelCaptureSettingsBuilder()
  .addLabel(
    await new LabelDefinitionBuilder()
      .addCustomBarcode(
        await new CustomBarcodeBuilder()
          .setSymbology(Symbology.EAN13UPCA)
          .build("Barcode")
      )
      .addExpiryDateText(await new ExpiryDateTextBuilder().build("Expiry Date"))
      .build("Perishable Product")
  )
  .build();
```

**After (new in 8.5):**

```typescript
const settings = await new LabelCaptureSettingsBuilder()
  .addLabel(
    new LabelDefinitionBuilder("Perishable Product")
      .addCustomBarcode(new CustomBarcodeBuilder("Barcode").setSymbology(Symbology.EAN13UPCA))
      .addExpiryDateText(new ExpiryDateTextBuilder("Expiry Date"))
  )
  .build();
```

One `await` at the outermost `build()`, and each builder's name is co-located with its constructor.

### Change B — factory-function helpers (optional sugar)

Each `LabelDefinitionBuilder` and `LabelFieldDefinitionBuilder` now has a matching factory function whose name is the builder name with a lowercase first letter and no `Builder` suffix. Equivalent to the class constructors, but less noise at call sites.

```typescript
import {
  customBarcode,
  expiryDateText,
  label,
  labelCaptureSettings,
  LabelCapture,
} from "@scandit/web-datacapture-label";

const settings = await labelCaptureSettings()
  .addLabel(
    label("Perishable Product")
      .addCustomBarcode(customBarcode("Barcode").setSymbology(Symbology.EAN13UPCA))
      .addExpiryDateText(expiryDateText("Expiry Date"))
  )
  .build();
```

### When to migrate

Only if the user explicitly asks for the terser syntax. Both styles are fully supported. Do not rewrite working code just because a newer style exists.
````

- [ ] **Step 2: Commit**

```bash
git add skills/label-capture-web/references/migration.md
git commit -m "Add migration.md §4 v8.4→v8.5 ergonomic builder improvements"
```

---

## Phase 4 — Fixtures + eval JSONs

### Task 4.1: Create eval fixtures

**Files:**
- Create: `skills/label-capture-web/evals/fixtures/empty-app.ts`
- Create: `skills/label-capture-web/evals/fixtures/package-8.1.json`
- Create: `skills/label-capture-web/evals/fixtures/package-8.2.json`
- Create: `skills/label-capture-web/evals/fixtures/before-8.1-regex.ts`
- Create: `skills/label-capture-web/evals/fixtures/before-8.2-validation-flow.ts`
- Create: `skills/label-capture-web/evals/fixtures/before-8.5-builders.ts`

- [ ] **Step 1: Write `empty-app.ts`** — mirrors `sparkscan-web/evals/fixtures/empty-app.ts` exactly:

```typescript
async function run() {
    console.log("App started");
}

run();
```

- [ ] **Step 2: Write `package-8.1.json`**

```json
{
  "name": "my-label-capture-app",
  "version": "1.0.0",
  "dependencies": {
    "@scandit/web-datacapture-core": "8.1.0",
    "@scandit/web-datacapture-barcode": "8.1.0",
    "@scandit/web-datacapture-label": "8.1.0"
  }
}
```

- [ ] **Step 3: Write `package-8.2.json`**

```json
{
  "name": "my-label-capture-app",
  "version": "1.0.0",
  "dependencies": {
    "@scandit/web-datacapture-core": "8.2.0",
    "@scandit/web-datacapture-barcode": "8.2.0",
    "@scandit/web-datacapture-label": "8.2.0"
  }
}
```

- [ ] **Step 4: Write `before-8.1-regex.ts`** — uses the old regex property names:

```typescript
import { Symbology } from "@scandit/web-datacapture-barcode";
import {
  CustomBarcodeBuilder,
  CustomTextBuilder,
  ExpiryDateTextBuilder,
  LabelCaptureSettingsBuilder,
  LabelDefinitionBuilder,
} from "@scandit/web-datacapture-label";

export async function buildSettings() {
  const expiryBuilder = new ExpiryDateTextBuilder()
    .setDataTypePattern("EXP[:\\s]+")
    .setPattern("\\d{2}/\\d{2}/\\d{2,4}");

  const lotBuilder = new CustomTextBuilder()
    .setDataTypePatterns(["LOT[:\\s]+", "Batch[:\\s]+"])
    .setPatterns(["[A-Z0-9]{6,}"]);

  return await new LabelCaptureSettingsBuilder()
    .addLabel(
      await new LabelDefinitionBuilder()
        .addCustomBarcode(
          await new CustomBarcodeBuilder()
            .setSymbology(Symbology.EAN13UPCA)
            .build("Barcode")
        )
        .addExpiryDateText(await expiryBuilder.build("Expiry Date"))
        .addCustomText(await lotBuilder.build("Lot Number"))
        .build("Perishable Product")
    )
    .build();
}
```

- [ ] **Step 5: Write `before-8.2-validation-flow.ts`** — uses deprecated validation flow texts:

```typescript
import {
  LabelCaptureValidationFlowOverlay,
  LabelCaptureValidationFlowSettings,
  type LabelCaptureValidationFlowListener,
  type LabelField,
} from "@scandit/web-datacapture-label";

export async function attachValidationFlow(mode: any, view: any) {
  const overlay = await LabelCaptureValidationFlowOverlay.withLabelCaptureForView(mode, view);

  const validationFlowSettings = await LabelCaptureValidationFlowSettings.create();
  validationFlowSettings.requiredFieldErrorText = "This field is required";
  validationFlowSettings.missingFieldsHintText = "Please fill the missing fields";
  validationFlowSettings.manualInputButtonText = "Enter manually";
  await overlay.applySettings(validationFlowSettings);

  overlay.listener = {
    onManualInput: (_field: LabelField, _oldValue: string | null, _newValue: string) => {},
    onValidationFlowLabelCaptured: (fields: LabelField[]) => {
      for (const field of fields) {
        console.log(field.name, field.text ?? field.barcode?.data);
      }
    },
  } satisfies LabelCaptureValidationFlowListener;
}
```

- [ ] **Step 6: Write `before-8.5-builders.ts`** — verbose class-builder chain:

```typescript
import { Symbology } from "@scandit/web-datacapture-barcode";
import {
  CustomBarcodeBuilder,
  ExpiryDateTextBuilder,
  LabelCaptureSettingsBuilder,
  LabelDefinitionBuilder,
  TotalPriceTextBuilder,
} from "@scandit/web-datacapture-label";

export async function buildSettings() {
  return await new LabelCaptureSettingsBuilder()
    .addLabel(
      await new LabelDefinitionBuilder()
        .addCustomBarcode(
          await new CustomBarcodeBuilder()
            .setSymbology(Symbology.EAN13UPCA)
            .build("Barcode")
        )
        .addExpiryDateText(await new ExpiryDateTextBuilder().build("Expiry Date"))
        .addTotalPriceText(await new TotalPriceTextBuilder().build("Total Price"))
        .build("Perishable Product")
    )
    .build();
}
```

- [ ] **Step 7: Commit**

```bash
git add skills/label-capture-web/evals/fixtures/
git commit -m "Add eval fixtures for integration + migration scenarios"
```

### Task 4.2: Create `integration-evals.json`

**Files:**
- Create: `skills/label-capture-web/evals/integration-evals.json`

- [ ] **Step 1: Write the file**

```json
{
  "skill_name": "label-capture-web",
  "evals": [
    {
      "id": 1,
      "prompt": "I want to add Scandit Label Capture to my web app. Here's my main file: empty-app.ts. The label is a perishable-product sticker with an EAN-13 barcode and an expiry date (required) and a total price (optional).",
      "expected_output": "The skill reads integration.md, walks through the interactive label-definition questions, writes complete Label Capture integration code into empty-app.ts, and shows the setup checklist.",
      "files": [
        "eval-fixtures/empty-app.ts"
      ],
      "assertions": [
        { "text": "Setup checklist is shown and mentions @scandit/web-datacapture-core, @scandit/web-datacapture-barcode, and @scandit/web-datacapture-label npm packages" },
        { "text": "DataCaptureContext.forLicenseKey( is present with libraryLocation and moduleLoaders" },
        { "text": "labelCaptureLoader() is present in moduleLoaders" },
        { "text": "License key placeholder (e.g. ENTER YOUR SCANDIT LICENSE KEY or similar) is present" },
        { "text": "new LabelCaptureSettingsBuilder() is used with .addLabel(...).build()" },
        { "text": "new LabelDefinitionBuilder() is used and .build(\"Perishable Product\") or a similar label name is called" },
        { "text": "new CustomBarcodeBuilder() is used, with setSymbology(Symbology.EAN13UPCA) or setSymbologies including EAN13UPCA, and .build(\"Barcode\") or a similar field name" },
        { "text": "Symbology.EAN13UPCA is enabled" },
        { "text": "new ExpiryDateTextBuilder() is used with .build(\"Expiry Date\") or similar field name" },
        { "text": "new TotalPriceTextBuilder() is used with .build(\"Total Price\") or similar field name, and is marked as optional (setIsOptional(true))" },
        { "text": "LabelCapture.forContext( is present" },
        { "text": "LabelCaptureBasicOverlay.withLabelCaptureForView( is present" },
        { "text": "addListener with didUpdateSession callback is present" },
        { "text": "session.capturedLabels is accessed inside didUpdateSession" },
        { "text": "Camera.pickBestGuess() is called" },
        { "text": "camera.applySettings(LabelCapture.createRecommendedCameraSettings()) is called" },
        { "text": "camera.switchToDesiredState(FrameSourceState.On) is called" },
        { "text": "No renamed-away regex properties: pattern, patterns, dataTypePattern, dataTypePatterns are absent from the generated code" },
        { "text": "No v8.5-only factory functions: labelCaptureSettings(, label(, customBarcode(, expiryDateText(, totalPriceText( are absent" },
        { "text": "LabelCaptureValidationFlowOverlay is NOT used (the user did not ask for Validation Flow)" }
      ]
    },
    {
      "id": 2,
      "prompt": "Add Label Capture to my empty-app.ts with the redesigned Validation Flow. The label is a perishable-product sticker with an EAN-13 barcode (required) and an expiry date (required). My package.json pins @scandit/web-datacapture-label to 8.2.0.",
      "expected_output": "The skill detects the SDK version from package.json and uses the redesigned Validation Flow API (no deprecated text properties, uses setPlaceholderTextForLabelDefinition).",
      "files": [
        "eval-fixtures/empty-app.ts",
        "eval-fixtures/package-8.2.json"
      ],
      "assertions": [
        { "text": "Minimal integration (context, view, camera, settings, mode) is present" },
        { "text": "LabelCaptureValidationFlowOverlay.withLabelCaptureForView( is used instead of (or in addition to) LabelCaptureBasicOverlay" },
        { "text": "LabelCaptureValidationFlowSettings.create() is called" },
        { "text": "setPlaceholderTextForLabelDefinition is called on the validation-flow settings at least once" },
        { "text": "overlay.listener is assigned with onValidationFlowLabelCaptured and onManualInput handlers" },
        { "text": "Deprecated Validation Flow properties are absent: requiredFieldErrorText, missingFieldsHintText, manualInputButtonText do NOT appear in the generated code" },
        { "text": "Symbology.EAN13UPCA is enabled" }
      ]
    },
    {
      "id": 3,
      "prompt": "Add Label Capture with the Validation Flow to my empty-app.ts. The label is a perishable-product sticker with an EAN-13 barcode (required) and an expiry date (required). My package.json pins @scandit/web-datacapture-label to 8.1.0.",
      "expected_output": "The skill detects the v8.1 SDK version and uses the original Validation Flow API with the requiredFieldErrorText, missingFieldsHintText, and manualInputButtonText properties.",
      "files": [
        "eval-fixtures/empty-app.ts",
        "eval-fixtures/package-8.1.json"
      ],
      "assertions": [
        { "text": "LabelCaptureValidationFlowOverlay.withLabelCaptureForView( is used" },
        { "text": "LabelCaptureValidationFlowSettings.create() is called" },
        { "text": "requiredFieldErrorText, missingFieldsHintText, or manualInputButtonText is set on the validation-flow settings (original v8.1 API)" },
        { "text": "setPlaceholderTextForLabelDefinition is NOT called (that is the v8.2+ redesign API; the user is on v8.1)" },
        { "text": "overlay.listener is assigned with onValidationFlowLabelCaptured and onManualInput handlers" },
        { "text": "Symbology.EAN13UPCA is enabled" }
      ]
    }
  ]
}
```

- [ ] **Step 2: Commit**

```bash
git add skills/label-capture-web/evals/integration-evals.json
git commit -m "Add integration evals for label-capture-web"
```

### Task 4.3: Create `migration-evals.json`

**Files:**
- Create: `skills/label-capture-web/evals/migration-evals.json`

- [ ] **Step 1: Write the file**

```json
{
  "skill_name": "label-capture-web",
  "evals": [
    {
      "id": 1,
      "prompt": "I'm upgrading my Label Capture integration from Scandit SDK 8.0 to 8.1. Update the regex-property names in before-8.1-regex.ts.",
      "expected_output": "The skill renames pattern→valueRegex, patterns→valueRegexes, dataTypePattern→anchorRegex, dataTypePatterns→anchorRegexes (and the corresponding setter methods) throughout the file; leaves everything else untouched.",
      "files": [
        "eval-fixtures/before-8.1-regex.ts"
      ],
      "assertions": [
        { "text": "setPattern is renamed to setValueRegex" },
        { "text": "setPatterns is renamed to setValueRegexes" },
        { "text": "setDataTypePattern is renamed to setAnchorRegex" },
        { "text": "setDataTypePatterns is renamed to setAnchorRegexes" },
        { "text": "The old method names (setPattern, setPatterns, setDataTypePattern, setDataTypePatterns) are absent from the final file" },
        { "text": "Imports and non-regex logic are preserved (ExpiryDateTextBuilder, CustomTextBuilder, CustomBarcodeBuilder, LabelDefinitionBuilder, LabelCaptureSettingsBuilder, Symbology.EAN13UPCA are still used)" }
      ]
    },
    {
      "id": 2,
      "prompt": "Migrate my Validation Flow from Scandit SDK 8.1 to 8.2 in before-8.2-validation-flow.ts. Adopt the redesigned API.",
      "expected_output": "The skill removes the deprecated requiredFieldErrorText / missingFieldsHintText / manualInputButtonText assignments and replaces them with setPlaceholderTextForLabelDefinition calls. Listener shape preserved.",
      "files": [
        "eval-fixtures/before-8.2-validation-flow.ts"
      ],
      "assertions": [
        { "text": "requiredFieldErrorText is no longer set in the generated code" },
        { "text": "missingFieldsHintText is no longer set" },
        { "text": "manualInputButtonText is no longer set" },
        { "text": "setPlaceholderTextForLabelDefinition is called at least once on validationFlowSettings" },
        { "text": "LabelCaptureValidationFlowOverlay.withLabelCaptureForView is preserved" },
        { "text": "onValidationFlowLabelCaptured and onManualInput listener methods are preserved" }
      ]
    },
    {
      "id": 3,
      "prompt": "Add the new optional onValidationFlowResultUpdate listener method to my Validation Flow listener in before-8.2-validation-flow.ts. We just upgraded to SDK 8.4 and want to react to partial result updates.",
      "expected_output": "The skill adds onValidationFlowResultUpdate(type, fields, frameData) to the listener object, imports LabelResultUpdateType and FrameData, and preserves the existing onManualInput and onValidationFlowLabelCaptured methods.",
      "files": [
        "eval-fixtures/before-8.2-validation-flow.ts"
      ],
      "assertions": [
        { "text": "onValidationFlowResultUpdate is added to the overlay.listener object" },
        { "text": "The method signature receives (type, fields, frameData) — type is LabelResultUpdateType, fields is LabelField[], frameData is FrameData | null" },
        { "text": "LabelResultUpdateType is imported from @scandit/web-datacapture-label" },
        { "text": "FrameData is imported from @scandit/web-datacapture-core" },
        { "text": "The existing onManualInput listener is preserved" },
        { "text": "The existing onValidationFlowLabelCaptured listener is preserved" }
      ]
    },
    {
      "id": 4,
      "prompt": "Rewrite before-8.5-builders.ts to use the new 8.5 ergonomic builder syntax. I'd like the less-verbose form where the name goes in the constructor and there's only a single outer await.",
      "expected_output": "The skill moves each field-builder name into the constructor (e.g. new CustomBarcodeBuilder(\"Barcode\")), removes the inner awaits and .build(name) calls, and leaves a single outer .build() + await on LabelCaptureSettingsBuilder.",
      "files": [
        "eval-fixtures/before-8.5-builders.ts"
      ],
      "assertions": [
        { "text": "new CustomBarcodeBuilder(\"Barcode\") (name in constructor) is used" },
        { "text": "new ExpiryDateTextBuilder(\"Expiry Date\") is used" },
        { "text": "new TotalPriceTextBuilder(\"Total Price\") is used" },
        { "text": "new LabelDefinitionBuilder(\"Perishable Product\") is used" },
        { "text": "There is exactly one await in the expression — on the outer LabelCaptureSettingsBuilder.build() call" },
        { "text": "No inner .build(\"...\") calls on field builders remain" },
        { "text": "The field builders are passed directly into addCustomBarcode / addExpiryDateText / addTotalPriceText without await" },
        { "text": "Symbology.EAN13UPCA is still enabled" }
      ]
    }
  ]
}
```

- [ ] **Step 2: Commit**

```bash
git add skills/label-capture-web/evals/migration-evals.json
git commit -m "Add migration evals for label-capture-web"
```

---

## Phase 5 — Incidental updates

### Task 5.1: Update `data-capture-sdk` handoff table

**Files:**
- Modify: `skills/data-capture-sdk/SKILL.md`

- [ ] **Step 1: Find the current handoff table**

Run:
```bash
grep -n "Suggested Invocation" skills/data-capture-sdk/SKILL.md
grep -n "sparkscan-ios" skills/data-capture-sdk/SKILL.md
```
Expected: locate the line with the `sparkscan-ios` row. The table currently has one row.

- [ ] **Step 2: Replace the table with three rows**

Use Edit to replace this old_string (the single existing row):
```
| SparkScan | iOS | `sparkscan-ios` | "Ask me to integrate SparkScan into your iOS app" |
```
with this new_string:
```
| SparkScan | iOS | `sparkscan-ios` | "Ask me to integrate SparkScan into your iOS app" |
| SparkScan | Web | `sparkscan-web` | "Ask me to integrate SparkScan into your web app" |
| Smart Label Capture | Web | `label-capture-web` | "Ask me to integrate Label Capture into your web app" |
```

- [ ] **Step 3: Commit**

```bash
git add skills/data-capture-sdk/SKILL.md
git commit -m "Add label-capture-web and sparkscan-web to data-capture-sdk handoff table"
```

### Task 5.2: Update README.md

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add a row to the skills table**

Use Edit to replace:
```
| `sparkscan-web` | SparkScan integration and migration guide for Web |
```
with:
```
| `sparkscan-web` | SparkScan integration and migration guide for Web |
| `label-capture-web` | Smart Label Capture integration and migration guide for Web |
```

- [ ] **Step 2: Add a usage block at the end of the `## Usage` section**

Use Edit to append the following block after the `### SparkScan Web implementation` block's last bullet:

```
### Label Capture Web implementation

- "How do I add Label Capture to my web app?"
- "Scan a perishable-product label with a barcode and an expiry date"
- "How do I enable the Validation Flow?"
- "Migrate my Label Capture integration from SDK 8.1 to 8.2"
- "Upgrade my Label Capture to the latest SDK version"
```

- [ ] **Step 3: Commit**

```bash
git add README.md
git commit -m "Add label-capture-web to README skills table and usage examples"
```

---

## Phase 6 — Final review

### Task 6.1: Sanity-check the skill folder

**Files:** none (verification only).

- [ ] **Step 1: List the final tree**

Run:
```bash
find skills/label-capture-web -type f | sort
```
Expected files (in order):
```
skills/label-capture-web/DESIGN.md
skills/label-capture-web/PLAN.md
skills/label-capture-web/SKILL.md
skills/label-capture-web/evals/fixtures/before-8.1-regex.ts
skills/label-capture-web/evals/fixtures/before-8.2-validation-flow.ts
skills/label-capture-web/evals/fixtures/before-8.5-builders.ts
skills/label-capture-web/evals/fixtures/empty-app.ts
skills/label-capture-web/evals/fixtures/package-8.1.json
skills/label-capture-web/evals/fixtures/package-8.2.json
skills/label-capture-web/evals/integration-evals.json
skills/label-capture-web/evals/migration-evals.json
skills/label-capture-web/references/integration.md
skills/label-capture-web/references/migration.md
```

- [ ] **Step 2: Check SKILL.md front-matter parses**

Run:
```bash
head -8 skills/label-capture-web/SKILL.md
```
Expected: `---`, `name: label-capture-web`, `description: ...`, `license: MIT`, `metadata:`, `  author: scandit`, `  version: "1.0.0"`, `---`.

- [ ] **Step 3: Validate eval JSON**

Run:
```bash
python3 -c "import json; json.load(open('skills/label-capture-web/evals/integration-evals.json')); json.load(open('skills/label-capture-web/evals/migration-evals.json')); print('OK')"
```
Expected: `OK`.

- [ ] **Step 4: Grep for forbidden strings in integration.md**

Run:
```bash
grep -nE "setPattern[^V]|setPatterns|setDataTypePattern|setDataTypePatterns" skills/label-capture-web/references/integration.md || echo "clean"
grep -nE "^\\s*label\\(|customBarcode\\(|expiryDateText\\(|labelCaptureSettings\\(" skills/label-capture-web/references/integration.md || echo "clean"
```
Expected: `clean` for both — integration.md must not teach the renamed-away APIs or the v8.5-only factory functions.

- [ ] **Step 5: Grep for forbidden strings in `before-*` fixtures**

Run:
```bash
# before-8.1-regex.ts must use the OLD names (to be migrated FROM):
grep -nE "setPattern|setDataTypePattern" skills/label-capture-web/evals/fixtures/before-8.1-regex.ts
# before-8.2-validation-flow.ts must use the OLD properties:
grep -nE "requiredFieldErrorText|missingFieldsHintText|manualInputButtonText" skills/label-capture-web/evals/fixtures/before-8.2-validation-flow.ts
# before-8.5-builders.ts must use the verbose form:
grep -nE "\\.build\\(\"" skills/label-capture-web/evals/fixtures/before-8.5-builders.ts
```
Expected: each grep produces matches. If any returns empty, the fixture doesn't represent its "before" state — fix the fixture.

- [ ] **Step 6: Commit any fixes from earlier steps (if any)**

If any previous step required a fix, commit it:
```bash
git add -u skills/label-capture-web/
git commit -m "Fix skill sanity-check issues"
```

### Task 6.2: Update PLAN.md status

**Files:**
- Modify: `skills/label-capture-web/PLAN.md`

- [ ] **Step 1: Mark all tasks complete**

Manually tick every `- [ ]` to `- [x]` in this file, OR (when using subagent-driven-development) let the runner do it.

- [ ] **Step 2: Commit**

```bash
git add skills/label-capture-web/PLAN.md
git commit -m "Mark label-capture-web implementation plan complete"
```

---

## Self-Review (performed after plan was drafted)

1. **Spec coverage.** Every section of `DESIGN.md` maps to a task:
   - File layout → Phase 0–4 create each listed file; Phase 5 handles the two incidental edits.
   - `SKILL.md` spec → Task 1.1 produces exact frontmatter and body.
   - `integration.md` sections 1–7 → Tasks 2.1 (§§1–2, 7), 2.2 (§3 interactive), 2.3 (§§4–5), 2.4 (§6 Validation Flow).
   - `migration.md` sections 1–4 → Tasks 3.1, 3.2, 3.3, 3.4 respectively.
   - Evals (integration + migration) + fixtures → Phase 4 (Tasks 4.1, 4.2, 4.3).
   - Version-identification strategy → baked into integration.md §6 (Task 2.4) and migration.md intro (Task 3.1).
   - Open items → Phase 0 (Tasks 0.1, 0.2, 0.3) resolves each before content is written.
   - Guardrails → enforced by grep checks in Task 6.1 plus prose in SKILL.md (Task 1.1) and integration.md (Task 2.3).

2. **Placeholder scan.** No TODOs, no "add appropriate X", no skipped test code. Task 1.1 has a single `<API_URL>` substitution explicitly driven by Task 0.1's output.

3. **Type / name consistency.** Field-builder names (`CustomBarcodeBuilder`, `ExpiryDateTextBuilder`, `TotalPriceTextBuilder`, `CustomTextBuilder`) and their corresponding `addXxx` methods match across tasks. Regex renames (`setValueRegex`, `setValueRegexes`, `setAnchorRegex`, `setAnchorRegexes`) are consistent between migration.md §1 and the integration.md "do not use the old names" callout. Validation Flow listener methods (`onValidationFlowLabelCaptured`, `onManualInput`, `onValidationFlowResultUpdate`) have matching signatures in integration.md §6, migration.md §2, and migration.md §3.

4. **Scope.** Single skill, single platform. No tasks stray into SparkScan or other skills beyond the two-line handoff-table update and the README entry.
