# COMMON DOMAIN

## OVERVIEW
Shared registration, configuration, networking, common events, and most static data providers.

## WHERE TO LOOK
| Task | Location |
|------|----------|
| Register blocks/items/fluids/menus | `registration/` and `TinkerModule.java` |
| Shared tags and ids | `TinkerTags.java`, `tags/` |
| Configuration | `config/` |
| Network channels and packets | `network/` |
| Static data | `data/` |
| Cross-cutting events | `event/`, `recipe/`, `multiblock/` |

## CONVENTIONS
- `TinkerModule` owns internal shared deferred registers and is the base for feature modules.
- Registration extensions wrap Mantle deferred registers; new addon code should use Mantle equivalents directly.
- Data providers are grouped by output type: tags, loot, recipes, advancements, and resources.
- Keep common event handlers free of client-only references.

## ANTI-PATTERNS
- Do not add unrelated gameplay logic to `TinkerModule`.
- Do not initialize `MaterialRegistry` from static fields or module construction.
- Do not put optional mod integration branches in common registration code; use `plugin/`.
- Do not hand-edit generated output produced by common data providers.

## NOTES
- `common/network` initializes early because tool and table modules register packets during construction.
- The `TinkerNetwork` channel version is `"3"` (int-sized container stack synchronization). `HighStackCountSynchronizer` swaps only the server-to-client stack projection; bump the version when packet schemas change.
- `common/config` is read by multiple domains; avoid duplicating config keys in feature packages.
- `common/registration` is an internal compatibility layer around Mantle registration objects.
- `common/data` may reference registries from every domain, so provider changes can have a broad build impact.

## CHANGE IMPACT
- Registration changes can affect every module that extends `TinkerModule`.
- Network changes require packet and affected feature tests.

## SCOPE
Applies to `src/main/java/slimeknights/tconstruct/common` except the more specific `common/data` guide.

## READ WHEN
- Adding or changing a deferred register, config key, packet, common event, or shared tag.
- Changing a module constructor or code used by several domains.

## SOURCE OF TRUTH
- Internal register ownership: `TinkerModule.java`.
- Packet registration: `common/network/TinkerNetwork.java`.
- Configuration: `common/config`.
- Static providers: `common/data` and its dedicated guide.

## WORKFLOW
1. Confirm the owning module and lifecycle event.
2. Keep common code server-safe and avoid optional integration imports.
3. Run focused tests, then `.\gradlew.bat test` for shared registration changes.
