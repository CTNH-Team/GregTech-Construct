# [Tinkers' Construct](https://github.com/SlimeKnights/TinkersConstruct) Unoffical Fork
**"This fork will make Tinkers' Construct 3 compatible with Mod GTM in all aspects."**  

# [Tinkers' Construct](https://slimeknights.github.io/projects/#tinkers-construct)

Tinkers' Construct is a mod about putting tools together in a wide variety of ways, then modifying them until they turn into something else. The tools never disappear and can be named and changed to your heart's desire. Once you make them, they're yours forever. Many different materials can be used to make your tools. Tools can be forged in the part builder, or by melting down metals in the multiblock smeltery.

## Documentation

For documentation on writing addons or working with Tinkers' Consrtuct datapacks, see the pages on the SlimeKnight's Github.io pages: https://slimeknights.github.io/docs/

For a gameplay overview, see https://slimeknights.github.io/docs/gameplay/tinkers-construct-3/.

## Setting up a Workspace/Compiling from Source


- Java 17
- JetBrains Runtime (JBR) is mandatory. You must configure IntelliJ IDEA to use the bundled JBR (version 17 or newer) for the Gradle JVM. This can be set in `Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM`.

The project uses a modern Forge plugin to define run configurations directly in `build.gradle`. These are imported into your IDE by running the `genIntellijRuns` Gradle task.

- **`client`**: Launches the Minecraft client for testing mod features, UI, and gameplay.
- **`server`**: Launches a dedicated, no-GUI server instance for testing server-side logic and commands.
- **`data`**: Runs the data generator. It scans your code for data providers and automatically generates resource files like recipes, models, and loot tables into the `src/generated/resources/` directory.

To build the project, execute `gradlew build`. For troubleshooting Gradle issues, use `gradlew clean` and `gradlew cleanCache`. The build script is configured to skip tests to resolve plugin compatibility problems.

## Issue reporting
Please include the following:

* Minecraft version
* Tinkers' Construct version
* Forge version/build
* Versions of any mods potentially related to the issue 
* Any relevant screenshots are greatly appreciated.
* For crashes:
	* Steps to reproduce
	* latest.log (the FML log) from the root folder of the client

## Licenses
Code, Textures and binaries are licensed under the [MIT License](https://tldrlegal.com/license/mit-license).

You are allowed to use the mod in your modpack.
Any modpack which uses Tinkers' Construct takes **full** responsibility for user support queries. For anyone else, we only support official builds from the main CI server, not custom built jars. We also do not take bug reports for outdated builds of Minecraft.

If you have queries about any license or the above support restrictions, please join [our Discord](https://discord.com/invite/njGrvuh) or reach out via the links on https://slimeknights.github.io/about/.

Any alternate licenses are noted where appropriate.=
