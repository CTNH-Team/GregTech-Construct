# PLUGIN DOMAIN

## OVERVIEW
Optional compatibility integrations discovered and initialized only when their target mods are available.

## WHERE TO LOOK
| Integration | Location |
|-------------|----------|
| Botania | `plugin/botania` |
| Create | `plugin/create` |
| Json Things | `plugin/jsonthings` |
| Apotheosis | `plugin/apotheosis` |
| Crafting Tweaks | `plugin/craftingtweaks` |
| Other bridges | neighboring integration classes under `plugin/` |

## CONVENTIONS
- Keep one integration's imports, event subscribers, recipes, and mixins inside its own package.
- Check `ModList` or the existing addon/plugin discovery path before touching foreign APIs.
- Put integration tests under the matching `src/test/java/slimeknights/tconstruct/plugin` package.
- Use addon callbacks and dynamic pack registrars for integration-provided data.

## ANTI-PATTERNS
- Do not turn an optional mod into a compile-time or runtime base dependency.
- Do not place integration-specific registrations in `common`, `shared`, or `library`.
- Do not load an integration client class from a dedicated-server path.
- Do not duplicate conditional checks when the existing plugin/addon registry already owns them.

## NOTES
- Botania and Create can also have mixin implications; coordinate plugin and mixin changes.
- Local GTCEU compatibility code is part of this fork's supported surface and uses the `gtceu` dependency in `libs`.
- Compatibility tests should be runnable without enabling every optional integration at once.

## CHANGE IMPACT
- Dependency metadata and plugin guards must change together when an integration API changes.

## SCOPE
Applies to `src/main/java/slimeknights/tconstruct/plugin` and integration-specific mixins, data, and tests.

## READ WHEN
- Adding or changing support for Botania, Create, GTCEU, Apotheosis, Json Things, or another optional mod.

## SOURCE OF TRUTH
- Integration guard and registration path in the integration package.
- Optional dependency declarations in `dependencies.gradle` and `mods.toml`.
- Integration behavior tests under `src/test/java/slimeknights/tconstruct/plugin`.

## WORKFLOW
1. Confirm the integration remains optional in metadata and runtime loading.
2. Test both the integration-present and integration-absent paths where practical.
3. Check client/server separation before running the matching Forge task.
