# LIBRARY DOMAIN

## OVERVIEW
Reusable mod API and core implementations for materials, modifiers, tools, recipes, capabilities, addon discovery, and client data.

## WHERE TO LOOK
| Concern | Location |
|---------|----------|
| Material lifecycle and ids | `materials/` |
| Modifier contracts and hooks | `modifiers/` |
| Tool definitions and NBT views | `tools/` |
| Recipe types and serializers | `recipe/` |
| Addon scanning/dispatch | `addon/` |
| JSON/loadable contracts | `json/` |
| Client model/data generation | `client/` |
| General helpers | `utils/` |

## CONVENTIONS
- Public-facing behavior is expressed through interfaces, views, hooks, modules, and loadables.
- Use module-based modifier composition and builder APIs rather than legacy modifier constants.
- Keep material registry access within its established lifecycle; addon code must use the supported callbacks.
- JSON predicates and serializers use Mantle loadable/registry patterns.
- Tests for this domain usually use `BaseMcTest`, fixture data, or `MaterialRegistryExtension`.

## ANTI-PATTERNS
- Do not cast `IToolStackView` to `ToolStack`.
- Do not mutate data through a view documented as read-only or reload-resettable.
- Do not invoke `ModifierTraitHook` directly; use the supported builder path.
- Do not mutate the entity in cancellable melee hooks or the supplied stack in inventory-tick hooks.
- Do not validate modifier max levels in the validation hook; follow the hook contract.
- Use `ModifierNBT.Builder` for multiple additions.
- Keep deprecated compatibility APIs isolated and migrate new callers to current Mantle/module APIs.

## NOTES
- This is the highest-volume Java domain; prefer an existing subpackage over a new top-level library package.
- `library/addon` is the boundary for reflection and Forge scan data; keep that mechanism localized.
- Changes here often affect tools, tables, plugins, and tests even when callers are reflective.

## SCOPE
Applies to `src/main/java/slimeknights/tconstruct/library`, including API contracts consumed by other domains and addons.

## READ WHEN
- Changing materials, modifiers, hooks, tool views, recipes, loadables, capabilities, or addon discovery.
- Removing code where reflection or framework callbacks may hide callers.

## SOURCE OF TRUTH
- API contracts: interfaces, views, hooks, and loader types.
- Runtime registration: `MaterialRegistry`, `ModifierManager`, and related lifecycle owners.
- Compatibility behavior: addon discovery and integration tests, not unused-looking call sites alone.

## WORKFLOW
1. Find callers and callback registrations before simplifying a shared API.
2. Lock behavior with the narrowest library or integration test.
3. Run focused tests, then the full `.\gradlew.bat test` for cross-domain API changes.
