# TOOLS DOMAIN

## OVERVIEW
Complete tools, tool parts, modifiers, modules, projectiles, tool logic, stats, and tool-focused data generation.

## WHERE TO LOOK
| Concern | Location |
|---------|----------|
| Complete tool objects | `TinkerTools.java`, `item/` |
| Modifier registrations | `TinkerModifiers.java`, `modules/`, `modifiers/` |
| Tool parts | `TinkerToolParts.java`, `parts/` |
| Tool behavior/events | `logic/`, `entity/`, `item/` |
| Tool definitions and recipes | `data/`, `data/material/`, `data/sprite/` |
| Stats and armor extensions | `stats/` |
| Tool tests | `src/test/java/slimeknights/tconstruct/tools` and `library/tools` |

## CONVENTIONS
- `TinkerTools`, `TinkerModifiers`, and `TinkerToolParts` are separate registration modules with shared `TinkerModule` infrastructure.
- Tool definitions are composed from modules and hooks; keep data-driven definitions in their existing providers.
- Modifier behavior belongs in modules/hooks where possible, not in item classes.
- Tool and modifier resource ids use lowercase snake case and must match generated data.
- Keep projectile, dispenser, and equipment event behavior in their established logic/entity packages.

## ANTI-PATTERNS
- Do not add new behavior to deprecated modifier compatibility classes when a module exists.
- Do not mutate another modifier's quiver inventory.
- Do not combine material and part sprite providers in one addon generator.
- Do not bypass `IToolStackView` contracts with implementation casts.
- Do not add client-only rendering dependencies to common tool items or modules.

## NOTES
- Tool registries have high fan-out into common data, world loot, tables, and addon generators.
- `TinkerTools` initializes shared tool helpers such as slot types and random material support.
- `TinkerModifiers` contains legacy compatibility surfaces; new behavior should use current modules.
- Changes to tool definitions commonly require both Java tests and regenerated tinkering JSON.

## SCOPE
Applies to `src/main/java/slimeknights/tconstruct/tools` and tool-specific consumers under `library/tools`.

## READ WHEN
- Changing tool items, parts, modifiers, modules, stats, projectiles, tool events, or tool datagen.
- Cleaning up a tool or modifier file.

## SOURCE OF TRUTH
- Registration: `TinkerTools`, `TinkerModifiers`, and `TinkerToolParts`.
- Behavior: tool modules/hooks and `IToolStackView` contracts.
- Data: providers under `tools/data` plus generated tinkering resources.

## WORKFLOW
1. Check registry fan-out and library hook contracts before simplifying behavior.
2. Run focused tool tests; run `runData` when definitions or providers changed.
3. Use `runClient` for rendering/UI changes and inspect generated diffs for data changes.
