# DYNAMIC DATA DOMAIN

## OVERVIEW
Runtime generation of addon, material, recipe, tinkering, and resource-pack content.

## WHERE TO LOOK
| Concern | Location |
|---------|----------|
| Server-side pack | `pack/TiCDynamicDataPack.java` |
| Client-side pack | `pack/TiCDynamicResourcePack.java` |
| Material content | `material/` |
| Recipe content | `recipe/` |
| Tinkering content | `tinkering/` |
| Generic resource output | `resource/` |

## CONVENTIONS
- Generators register with the dynamic registrars and emit in-memory pack files during Forge lifecycle events.
- `TiCAddonRegistry` is the callback boundary for optional addon data.
- Runtime generation is separate from `runData`; do not expect dynamic pack files in `src/generated/resources`.
- Keep generator state resettable because pack registration can occur more than once across reload/test lifecycles.

## ANTI-PATTERNS
- Do not write runtime-generated content directly to the repository.
- Do not treat `src/generated/resources` as the output of these classes.
- Do not discover addons through a second scanner; use `TiCAddonFinder` and `TiCAddonRegistry`.

## NOTES
- `TConstruct.registerPackFinders()` installs the dynamic pack sources.
- `registerDynamicResources()` runs after resource manager initialization.
- Dynamic generators are reset during pack registration so reloads do not retain stale output.
- The dynamic pack path is exercised by the data and addon integration tests.
- `ModifierModelMapManager` reloads first-stage client resources; `ModelManagerMixin` triggers `RegisterDynamicResourcesEvent` during `ModelManager.reload`, and `ToolClientEvents` registers the manager plus legacy trim blacklist and banner sprite source.
## CHANGE IMPACT
- Runtime generator changes affect addon loading and resource reload behavior.
- Keep pack identifiers and registrar ownership stable for compatibility.

## TESTING
- Cover both generated content and pack reload/reset behavior when changing a generator.

## SCOPE
Applies to `src/main/java/slimeknights/tconstruct/data`; it does not own static `runData` providers.

## READ WHEN
- Changing runtime resource packs, addon callbacks, material generation, recipe generation, or reload behavior.

## SOURCE OF TRUTH
- Pack installation: `TConstruct.registerPackFinders()`.
- Runtime output: the dynamic registrars and in-memory pack classes.
- Static output: `common/data` and `runData`, kept separate from this domain.

## WORKFLOW
1. Trace the pack finder and registrar that owns the output.
2. Keep output in memory; never write it into `src/generated/resources`.
3. Run dynamic-pack tests and a client/server reload scenario when lifecycle behavior changes.
