# WORLD DOMAIN

## OVERVIEW
World blocks, entities, structures, biome modifiers, features, and world-generation data.

## WHERE TO LOOK
- World module and registries: `TinkerWorld.java`.
- Structure module: `TinkerStructures.java`.
- Blocks and plants: `block/`.
- Entities and spawn rules: `entity/`.
- World generation: `worldgen/` and `data/`.
- World data providers: `data/WorldgenProvider.java`.

## CONVENTIONS
- Preserve the dependency from world registries to shared materials, fluids, and tool modules.
- Keep biome modifiers, configured features, placed features, structures, and structure sets in their corresponding registries.
- Use resource keys and tags for worldgen references instead of hard-coded registry lookups.
- Put server-side spawn and placement rules in common world classes; client renderers stay client-side.

## ANTI-PATTERNS
- Do not register world features from an unrelated domain module.
- Do not mutate client-only state from biome modifier or spawn callbacks.
- Do not hand-edit generated worldgen JSON when the provider is the source of truth.
- Do not reorder world module construction without checking data-provider and registry dependencies.

## NOTES
- `TinkerWorld` has high fan-out into loot, tags, advancements, and worldgen providers.
- `WorldgenProvider` registers structures, structure sets, biome modifiers, and related references.
- World data can emit multiple namespaces and should be checked by resource path, not only Java package.
- Dedicated-server validation is important for spawn, placement, and biome modifier changes.

## CHANGE IMPACT
- Registry key changes can invalidate generated loot, tags, and structure data.
- Worldgen changes should be checked with a clean test world rather than an existing save.

## TESTING
- Prefer deterministic registry and provider tests before manual world inspection.

## SCOPE
Applies to `src/main/java/slimeknights/tconstruct/world`, including world blocks, entities, structures, features, and worldgen providers.

## READ WHEN
- Changing world registry keys, spawn rules, biome modifiers, structures, features, or generated worldgen.

## SOURCE OF TRUTH
- Module ownership: `TinkerWorld.java` and `TinkerStructures.java`.
- Worldgen registration: `world/data/WorldgenProvider.java`.
- Generated resource inputs: provider code and tags, not generated JSON alone.

## WORKFLOW
1. Trace registry-key consumers in loot, tags, advancements, and worldgen.
2. Run focused provider tests, then use `runServer` with a clean test world.
3. Inspect generated worldgen diffs for namespace and key stability.
