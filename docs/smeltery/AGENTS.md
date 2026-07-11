# SMELTERY DOMAIN

## OVERVIEW
Smeltery, foundry, casting, melting, tank, and smeltery-specific data behavior.

## WHERE TO LOOK
- Main module and registries: `TinkerSmeltery.java`.
- Smeltery/foundry blocks and block entities: `block/` and `block/entity/`.
- Casting and melting recipes: `recipe/`.
- Smeltery data providers and generated inputs: `data/`.
- Fluid/tank behavior: neighboring `fluids` and smeltery tank packages.

## CONVENTIONS
- Register smeltery objects through `TinkerSmeltery`; preserve its construction order relative to `TinkerFluids`.
- Keep recipe serialization and runtime matching separate from block/menu UI code.
- Use existing Mantle recipe/loadable helpers for JSON-backed recipe types.
- Put client screens/renderers under client packages and keep common recipe logic server-safe.

## ANTI-PATTERNS
- Do not make casting recipes depend on client-only model or screen classes.
- Do not introduce a second fluid registry for smeltery fluids.
- Do not bypass recipe serializers with ad hoc NBT or string parsing.

## NOTES
- `TinkerSmeltery` is constructed before `TinkerFluids`, while recipes may reference both registries.
- Smeltery data providers feed static recipes and runtime dynamic material content through separate paths.
- Tank and transfer behavior may cross into `fluids`; keep ownership with the existing capability/helper class.
- Recipe tests should cover serialization and matching independently when both are changed.

## CHANGE IMPACT
- Recipe ids and serializer fields affect authored, generated, and addon data.
- Tank or menu changes should be checked on both client and dedicated-server runs.

## TESTING
- Keep recipe serialization tests separate from menu or screen tests.
