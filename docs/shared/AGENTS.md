# SHARED DOMAIN

## OVERVIEW
Cross-side modules and shared registries used by common, client, and domain-specific features.

## WHERE TO LOOK
- Core shared registrations: `TinkerCommons.java`, `TinkerMaterials.java`, and `TinkerEffects.java`.
- Client bootstrap and client-only setup: `TinkerClient.java` and `client/`.
- Shared blocks, items, and enums: neighboring domain packages.
- Shared behavior tests: `src/test/java/slimeknights/tconstruct/shared`.

## CONVENTIONS
- Shared classes must be safe to load from both client and dedicated-server paths unless explicitly under `client/`.
- Keep module construction responsibilities small; `TConstruct` owns ordering.
- Materials and effects are shared dependencies for tools, world, tables, and plugins.
- Cross-side helpers belong here only when they have no stronger domain owner.

## ANTI-PATTERNS
- Do not import client-only Minecraft classes into common/shared construction.
- Do not move domain-specific registration here merely to avoid a dependency edge.
- Do not initialize optional integrations from shared modules.

## NOTES
- `TinkerCommons`, `TinkerMaterials`, and `TinkerEffects` are constructed before most feature modules.
- `TinkerMaterials` is a dependency for tools, world content, and runtime addon generation.
- `TinkerClient` is the explicit client bootstrap; keep its reachability separate from shared classes.
- Shared state changes usually require tests in more than one domain because many modules consume it.

## CHANGE IMPACT
- Shared registry changes have broad fan-out and should be validated with a full test run.
- Client bootstrap changes require a client launch in addition to Java tests.

## TESTING
- Shared state tests should avoid depending on a previously generated runtime world.

## SCOPE
Applies to `src/main/java/slimeknights/tconstruct/shared` and cross-side modules consumed by multiple domains.

## READ WHEN
- Changing `TinkerCommons`, `TinkerMaterials`, `TinkerEffects`, `TinkerClient`, or shared state used by tools/world/tables.

## SOURCE OF TRUTH
- Module construction: `TConstruct.java`.
- Client boundary: `TinkerClient.java` and client event subscribers.
- Shared registry consumers: downstream domain modules and their tests.

## WORKFLOW
1. Confirm whether the changed class is safe on a dedicated server.
2. Run shared-state tests and a client launch for client bootstrap changes.
3. Check downstream registry consumers before changing shared ids or initialization.
