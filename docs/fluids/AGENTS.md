# FLUIDS DOMAIN

## OVERVIEW
Fluid registration plus fluid blocks, items, client presentation, data providers, and transfer helpers.

## WHERE TO LOOK
- Primary module and registry objects: `TinkerFluids.java`.
- Fluid block/item implementations: `block/` and `item/`.
- Fluid-specific generation: `data/`.
- Transfer and capability helpers: `util/`.

## CONVENTIONS
- Fluid ids and tags follow the lowercase snake-case resource convention.
- Keep fluid registration in `TinkerFluids`; consumers should reference its registry objects.
- Client rendering and model code stays under client-specific packages or event subscribers.
- Fluid compatibility is expressed through tags and optional integrations, not hard-coded foreign registry lookups.

## ANTI-PATTERNS
- Do not include Forge fluid tags in local Chemthrower tags.
- Do not put fluid registration in a feature module that is constructed after its consumers.
- Do not assume a fluid tag belongs to the `tconstruct` namespace.

## NOTES
- `TinkerFluids` is constructed after the main world/tool modules in the current entry-point order.
- Fluid blocks and fluid items often share properties; keep those defaults in the established helper path.
- Fluid data providers may feed both smeltery recipes and compatibility integrations.
- Test fluid behavior with registry-backed fixtures rather than the local `run/` world.

## CHANGE IMPACT
- Fluid id or tag changes can invalidate recipes, worldgen, and compatibility data.
- Client model changes need a client run; registry changes need a dedicated-server check.

## TESTING
- Pair registry tests with at least one consumer test when changing a fluid id or tag.

## SCOPE
Applies to `src/main/java/slimeknights/tconstruct/fluids` and its fluid registration, block, item, data, and utility code.

## READ WHEN
- Changing fluid ids, tags, transfer behavior, fluid blocks/items, or client fluid presentation.

## SOURCE OF TRUTH
- Registry ownership: `TinkerFluids.java`.
- Cross-domain consumers: smeltery recipes, world generation, and compatibility tags.
- Resource paths: authored and generated resources, checked by namespace and id.

## WORKFLOW
1. Check all registry and tag consumers before changing an id.
2. Run focused tests and `.\gradlew.bat runServer` for common behavior.
3. Run `.\gradlew.bat runClient` when models, textures, or client fluid code changes.
