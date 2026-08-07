# JAVA NAMESPACE GUIDE

## OVERVIEW
Production code is a single `slimeknights.tconstruct` namespace organized by gameplay domain, lifecycle concern, and client/server responsibility.

## STRUCTURE
```text
slimeknights/tconstruct/
|-- common/    # shared registration, config, events, networking, static datagen
|-- data/      # runtime dynamic packs and generators
|-- fluids/    # fluid blocks, items, data, and utilities
|-- gadgets/   # gadget blocks, entities, capabilities, and items
|-- library/   # reusable API: materials, modifiers, tools, recipes, client data
|-- mixin/     # conditional mixin plugin and mixin classes
|-- plugin/    # optional integrations
|-- shared/    # common modules and cross-side shared behavior
|-- smeltery/  # smeltery/foundry/casting behavior
|-- tables/    # stations, menus, screens, and packets
|-- tools/     # complete tools, parts, modifiers, modules, and tool data
`-- world/     # world blocks, entities, structures, and worldgen
```

## WHERE TO LOOK
| Concern | Location |
|---------|----------|
| Module construction order | `TConstruct.java` |
| Shared registries | `common/TinkerModule.java` |
| Client bootstrap | `shared/TinkerClient.java` and client event subscribers |
| Optional addon discovery | `library/addon` |
| Static data event fan-out | `TConstruct.gatherData()` and `common/data` |
| Runtime resource packs | `data/pack` and `data/*Generator.java` |
| Conditional mixins | `mixin/TConstructMixinPlugin.java` |

## RUNTIME FLOW
1. Forge instantiates `TConstruct` from `META-INF/mods.toml`.
2. `TConstruct` constructs shared, world, table, tool, smeltery, and fluid modules in order (`TinkerCommons` → `TinkerWorld` → `TinkerTables` → `TinkerTools` → `TinkerSmeltery` → `TinkerFluids`).
3. `TinkerModule.initRegisters()` attaches shared deferred registers to the mod event bus.
4. The constructor initializes networking, tags, client bootstrap, addon discovery, and optional integrations.
5. Forge dispatches common setup, client setup, registry, data, and pack events reflectively.

## CONVENTIONS
- Use the existing top-level domain for new behavior; avoid adding catch-all utility packages.
- Keep `client`, `data`, `block`, `entity`, `item`, `menu`, `network`, and `recipe` subpackages aligned with their established domain.
- Use the shared `TinkerModule` only for internal module registration; addons create their own registers.
- Treat event subscribers and registry callbacks as lifecycle entry points.

## ANTI-PATTERNS
- Do not reorder `Tinker*` construction without checking registry dependencies and data-provider references.
- Do not move optional integrations into base modules.
- Do not make client-only classes reachable from common construction paths.
- Do not bypass the runtime pack registrars with ad hoc global state.

## SCOPE
Applies to `src/main/java/slimeknights/tconstruct` and its child packages. It is a reference guide loaded through the root routing table, not an additional source-tree instruction file.

## READ WHEN
- A change crosses two or more top-level domains.
- A change affects module construction, Forge event flow, registration order, or addon discovery.
- A cleanup pass cannot tell whether a callback is reflective or framework-owned.

## SOURCE OF TRUTH
- Runtime order: `TConstruct.java` and `TinkerModule.java`.
- Forge metadata and mixins: `src/main/resources/META-INF/mods.toml` and `tconstruct.mixins.json`.
- Static generated data: providers plus `src/generated/resources`, never the generated files alone.

## WORKFLOW
1. Map the changed symbol to its domain and read that domain guide.
2. Check constructor order, event registration, and reflective entry points.
3. Run the narrowest test or Forge task for the affected surface.
4. Re-read the root routing table if the change introduces a new domain boundary.
