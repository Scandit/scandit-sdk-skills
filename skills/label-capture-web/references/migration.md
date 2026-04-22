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
