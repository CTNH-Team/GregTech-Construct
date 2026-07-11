# MIXIN DOMAIN

## OVERVIEW
Mixin classes and the conditional plugin that enables integration-specific transformations.

## WHERE TO LOOK
- Mixin list and side/config metadata: `src/main/resources/tconstruct.mixins.json`.
- Conditional plugin: `TConstructMixinPlugin.java`.
- Mixin implementations: this directory and its integration subpackages.
- Build registration: `build.gradle` `mixin` block and jar manifest.

## CONVENTIONS
- Keep mixins narrowly scoped to the target behavior and side.
- Gate optional integration mixins through the plugin's mod-presence checks.
- Maintain the refmap/config names expected by `build.gradle`.
- Prefer Forge/Mantle extension points when they can express the behavior without a mixin.

## ANTI-PATTERNS
- Do not add a mixin class without adding it to the correct JSON config.
- Do not reference optional integration classes from unconditional mixins.
- Do not use a mixin to bypass a library API contract.

## NOTES
- The mixin refmap is configured in `build.gradle` and must remain consistent with the config name.
- Create and Botania have conditional mixin paths; test both present and absent-mod behavior when changing them.
- Mixin failures may surface only during `runClient` or `runServer`, not during plain Java compilation.
- Keep target names aligned with the Minecraft/Forge version pinned in `gradle.properties`.

## CHANGE IMPACT
- A target signature change can break startup before normal mod logging is available.
- Keep a minimal launch scenario ready for every changed mixin target.

## TESTING
- Compilation alone does not verify mixin application; use the relevant Forge run.

## SCOPE
Applies to `src/main/java/slimeknights/tconstruct/mixin` and `src/main/resources/tconstruct.mixins.json`.

## READ WHEN
- Changing a mixin target, refmap/config entry, conditional integration, or client/server transformation.

## SOURCE OF TRUTH
- Config membership: `tconstruct.mixins.json`.
- Conditional loading: `TConstructMixinPlugin.java`.
- Packaging: the `mixin` and jar-manifest blocks in `build.gradle`.

## WORKFLOW
1. Verify the target class/method against the pinned Minecraft/Forge version.
2. Check mod-present and mod-absent branches for optional integrations.
3. Run the relevant `.\gradlew.bat runClient` or `runServer` scenario; compilation is insufficient.
