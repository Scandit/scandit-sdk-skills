# Scandit SDK Skills

AI agent skills that turn your coding assistant into a Scandit integration expert.

This plugin gives Claude Code, Cursor, and other compatible AI coding assistants up-to-date, grounded knowledge of the [Scandit Data Capture SDK](https://docs.scandit.com) — covering product selection, supported symbologies, platform requirements, API usage, and version migrations. Instead of relying on the model's training data (which can be stale or wrong about Scandit APIs), the assistant follows authoritative skills that point to the correct documentation and sample apps for your platform.

Use it to:

- **Pick the right product** for your use case — SparkScan, Barcode Capture, MatrixScan, Smart Label Capture, ID Capture, and more.
- **Implement scanning features** with platform-specific guidance.
- **Migrate between SDK versions** safely, with skills that know which APIs were renamed, removed, or restructured.
- **Get answers grounded in current Scandit docs** rather than the model's general (and often outdated) recollection.

## Install

### Claude Code

```
/plugin marketplace add Scandit/scandit-sdk-skills
/plugin install scandit-sdk@scandit-plugins
```

### Cursor

Search for **Scandit SDK** in the Cursor plugin marketplace, or install from Settings > Plugins.

### Skills CLI (40+ agents)

```bash
npx skills add Scandit/scandit-sdk-skills
```

## Available Skills

| Skill | Description |
|---|---|
| `data-capture-sdk` | Expert guide for the Scandit Data Capture SDK — product selection, docs, and sample apps |
| `sparkscan-ios` | SparkScan integration and migration guide for iOS |
| `sparkscan-web` | SparkScan integration and migration guide for Web |
| `sparkscan-capacitor` | SparkScan integration and migration guide for Capacitor |
| `sparkscan-cordova` | SparkScan integration and migration guide for Cordova |
| `sparkscan-flutter` | SparkScan integration and migration guide for Flutter |
| `sparkscan-rn` | SparkScan integration and migration guide for React Native |

## Usage

After installing, ask your AI coding assistant:

### Data Capture SDK

- "Which Scandit product should I use for my warehouse app?"
- "I need to scan barcodes and read expiry dates from labels"
- "What's the difference between SparkScan and Barcode Capture?"
- "I need to count inventory items and verify against a list"

### SparkScan iOS implementation

- "How do I add SparkScan to my iOS app?"
- "How do I enable symbologies in SparkScan?"
- "How do I handle scan results in SparkScan?"
- "Migrate my SparkScan integration from SDK v6 to v7"
- "Upgrade my SparkScan to the latest SDK version"

### SparkScan Web implementation

- "How do I add SparkScan to my web app?"
- "How do I add SparkScan to my react web app?"
- "How do I enable symbologies in SparkScan?"
- "How do I handle scan results in SparkScan?"
- "Migrate my SparkScan integration from SDK v6 to v7"
- "Upgrade my SparkScan to the latest SDK version"

### SparkScan Capacitor implementation

- "How do I add SparkScan to my Capacitor app?"
- "How do I enable symbologies in SparkScan?"
- "How do I handle scan results in SparkScan?"
- "Migrate my SparkScan integration from SDK v6 to v7"
- "Upgrade my SparkScan to the latest SDK version"

### SparkScan Cordova implementation

- "How do I add SparkScan to my Cordova app?"
- "How do I enable symbologies in SparkScan?"
- "How do I handle scan results in SparkScan?"
- "Migrate my SparkScan integration from SDK v6 to v7"
- "Upgrade my SparkScan to the latest SDK version"

### SparkScan Flutter implementation

- "How do I add SparkScan to my Flutter app?"
- "How do I enable symbologies in SparkScan?"
- "How do I handle scan results in SparkScan?"
- "Migrate my SparkScan integration from SDK v6 to v7"
- "Upgrade my SparkScan to the latest SDK version"

### SparkScan React Native implementation

- "How do I add SparkScan to my React Native app?"
- "How do I enable symbologies in SparkScan?"
- "How do I handle scan results in SparkScan?"
- "Migrate my SparkScan integration from SDK v6 to v7"
- "Upgrade my SparkScan to the latest SDK version"
