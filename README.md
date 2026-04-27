# Scandit SDK Skills

AI agent skills for integrating the [Scandit Data Capture SDK](https://docs.scandit.com).

Each skill teaches your coding assistant how to integrate a specific Scandit SDK correctly. Instead of pasting docs snippets into your AI editor, install a skill once and your agent follows Scandit's recommended patterns whenever you ask it to add a Scandit feature.

Skills are distributed using [`skills`](https://github.com/vercel-labs/skills), the open-source CLI from Vercel that has become the common standard for agent-skill packaging.

## What you get

Each skill bundles:

- The recommended integration code for a specific SDK (e.g. SparkScan iOS)
- Up-to-date setup, permissions, and license-key wiring
- Common customization recipes (modes, callbacks, UI tweaks)
- Links back to the relevant Scandit documentation

A shared `data-capture-sdk` skill provides cross-cutting integration knowledge (license activation, framework boilerplate, troubleshooting). We recommend installing it alongside any product skill.

## Available skills

| Skill | Description |
| --- | --- |
| `data-capture-sdk` | Shared baseline — product selection, license activation, framework boilerplate, troubleshooting. Recommended alongside any product skill. |
| `sparkscan-{framework}` | [SparkScan](https://docs.scandit.com/sdks/ios/sparkscan/intro/) integration & migration. Available for `ios`, `web`, `cordova`, `capacitor`, `flutter`, `rn` (React Native). |

## Installation

### Skills CLI (45+ agents)

The [`skills`](https://github.com/vercel-labs/skills) CLI from Vercel installs skills into any supported agent (Claude Code, Codex, Cursor, Antigravity, GitHub Copilot, Cline, Continue, Windsurf, and 35+ others). Run it and follow the interactive prompts to pick agent and skills:

```bash
npx skills add Scandit/scandit-sdk-skills
```

### Claude Code plugin

Claude Code can also install the skills as a plugin from the marketplace:

```bash
/plugin marketplace add Scandit/scandit-sdk-skills
/plugin install scandit-sdk@scandit-plugins
```

## Using a skill

Two ways the skill is invoked:

- **Slash command.** Call the skill explicitly:

  ```
  /sparkscan-ios use the skill to help me integrate the barcode scanner in my application
  ```

- **Automatic pickup.** Most agents read the skill's description and load it automatically when your prompt matches relevant keywords. With `sparkscan-ios` installed, asking _"add a SparkScan view to the home screen"_ pulls in the skill without explicit invocation.
