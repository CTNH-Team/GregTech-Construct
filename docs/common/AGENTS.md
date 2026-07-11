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
- `common/config` is read by multiple domains; avoid duplicating config keys in feature packages.
- `common/registration` is an internal compatibility layer around Mantle registration objects.
- `common/data` may reference registries from every domain, so provider changes can have a broad build impact.

## CHANGE IMPACT
- Registration changes can affect every module that extends `TinkerModule`.
- Network changes require packet and affected feature tests.
