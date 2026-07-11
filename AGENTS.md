# PROJECT KNOWLEDGE BASE

**Generated:** 2026-07-11
**Commit:** bf5c7b3108
**Branch:** dev

## OVERVIEW
Single-module Gradle Java 17 Minecraft mod. This fork keeps the upstream `tconstruct` mod id and `slimeknights.tconstruct` namespace while adding GregTech/GTCEU compatibility.

## STRUCTURE
```text
./
|-- src/main/java/slimeknights/tconstruct/ # production code; domain guides live in docs/
|-- src/main/resources/                    # authored assets, data, metadata, mixins
|-- src/generated/resources/               # checked-in output from `runData`
|-- src/test/java/                         # JUnit and Minecraft-backed tests
|-- libs/                                  # required local GTCEU and Formula Loader jars
|-- gradle/                                # wrapper and shared Gradle scripts
|-- .github/                               # CI and issue templates
|-- docs/                                  # domain guides and third-party license texts
`-- project_files/                         # material texture source files
```

`build/`, `bin/`, `run/`, `workspace/`, `.gradle/`, `.codegraph/`, `.omo/`, and `.claude/` are local build, runtime, index, or agent state. They are not implementation sources.

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Mod entry point and lifecycle | `src/main/java/slimeknights/tconstruct/TConstruct.java` | Manual module construction and Forge event handlers |
| Deferred registers | `src/main/java/slimeknights/tconstruct/common/TinkerModule.java` | Shared register definitions and attachment |
| Feature implementation | `src/main/java/slimeknights/tconstruct/{library,tools,smeltery,world,tables}` | Domain guides live in `docs/{library,tools,smeltery,world,tables}` |
| Optional mod support | `src/main/java/slimeknights/tconstruct/plugin` | Keep integrations isolated and optional |
| Static data generation | `src/main/java/slimeknights/tconstruct/common/data` and domain `data` packages | Writes to `src/generated/resources` |
| Runtime packs | `src/main/java/slimeknights/tconstruct/data/{pack,material,recipe,resource,tinkering}` | In-memory dynamic resources |
| Authored resources | `src/main/resources` | Assets, metadata, mixin config, and static data |
| Tests and fixtures | `src/test/java` and `src/test/resources` | Mirrors production packages; most tests extend `BaseMcTest` |
| Dependency versions | `gradle.properties` and `dependencies.gradle` | Local jars are resolved from `libs/` |

## CODE MAP
| Symbol | Type | Location | Role |
|--------|------|----------|------|
| `TConstruct` | mod entry point | `src/main/java/slimeknights/tconstruct/TConstruct.java` | Creates modules, initializes networking, handles common setup and data/resource events |
| `TinkerModule` | abstract base | `src/main/java/slimeknights/tconstruct/common/TinkerModule.java` | Central deferred-register surface; called by every `Tinker*` module |
| `TinkerTools` | module | `src/main/java/slimeknights/tconstruct/tools/TinkerTools.java` | Complete tools, tool items, tool data, and tool-related setup |
| `TinkerWorld` | module | `src/main/java/slimeknights/tconstruct/world/TinkerWorld.java` | World blocks, entities, features, and biome integration |
| `TiCAddonFinder` | addon scanner | `src/main/java/slimeknights/tconstruct/library/addon/TiCAddonFinder.java` | Discovers annotated optional integrations through Forge scan data |
| `TiCAddonRegistry` | addon dispatcher | `src/main/java/slimeknights/tconstruct/library/addon/TiCAddonRegistry.java` | Routes addon registration and data callbacks |
| `TiCDynamicDataPack` | runtime data pack | `src/main/java/slimeknights/tconstruct/data/pack/TiCDynamicDataPack.java` | Holds server-side generated addon/material data |
| `TiCDynamicResourcePack` | runtime resource pack | `src/main/java/slimeknights/tconstruct/data/pack/TiCDynamicResourcePack.java` | Holds client-side generated resources |
| `TConstructMixinPlugin` | mixin plugin | `src/main/java/slimeknights/tconstruct/mixin/TConstructMixinPlugin.java` | Enables integration mixins only when their mods are present |
| `BaseMcTest` | test harness | `src/test/java/slimeknights/tconstruct/test/BaseMcTest.java` | Boots registries and a Forge test container for most tests |

## CONVENTIONS
- Java packages are lowercase under `slimeknights.tconstruct`; domain boundaries are top-level packages.
- Feature registries are exposed by `Tinker*` modules and instantiated in deliberate order from `TConstruct`.
- Optional integrations live below `plugin/<integration>` and must not become hard dependencies.
- Data providers use `*Provider`; runtime pack generators use `TiCDynamic*Generator`.
- Resource ids and paths use lowercase snake case; Java constants use uppercase snake case.
- `.editorconfig` is authoritative for repository text: UTF-8, final newline, 2-space Java indentation, 2-space JSON indentation, and 120-column guidance.
- `src/generated/resources` is a tracked build product. Change its inputs and regenerate with `runData` rather than hand-editing generated JSON.

## ANTI-PATTERNS (THIS PROJECT)
- Do not edit `build/`, `bin/`, `run/`, `workspace/`, `.gradle/`, `.codegraph/`, or agent state as implementation.
- Do not call `MaterialRegistry` during mod construction or static initialization.
- Do not cast `IToolStackView` to `ToolStack`; keep addon code on the interface.
- Do not call `ModifierTraitHook` directly; use the supported builder path.
- Do not mutate the supplied entity or item stack from modifier hooks that document cancellation or ownership constraints.
- Use `ModifierNBT.Builder` for multiple modifier additions.
- Keep material and part sprite generators separate in addon datagen.
- Do not include Forge fluid tags in local Chemthrower tags.

## COMMANDS
```text
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat runData
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat clean
```

Use Java 17. IntelliJ builds require the bundled JetBrains Runtime as the Gradle JVM. Local jars in `libs/` are required for the GTCEU/Formula Loader dependency setup.

## NOTES
- The current Gradle build configures and runs JUnit tests; the README statement that tests are skipped is stale.
- The current task list does not include the README's older `genIntellijRuns` command.
- `runData` writes static generated resources; runtime dynamic packs are assembled separately during Forge lifecycle events.
- Forge event subscribers and reflective addon callbacks have few ordinary Java callers; trace them through annotations and registration sites.
- Jenkins still references a Maven publication task that is not configured in the current `build.gradle`.
