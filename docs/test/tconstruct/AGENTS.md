# TEST DOMAIN

## OVERVIEW
JUnit 5 tests use a single `src/test/java` source set, with most integration-style tests bootstrapping a lightweight Forge/Minecraft harness.

## STRUCTURE
```text
src/test/java/slimeknights/
|-- mantle/                         # Mantle-facing data tests
`-- tconstruct/
    |-- test/                       # BaseMcTest and shared helpers
    |-- fixture/                    # material/tool/registration fixtures
    |-- library/                    # core API and tool/material tests
    |-- data/                       # data and runtime-pack tests
    |-- tables/                     # menus, screens, slots, packets
    |-- tools/                      # tool logic, stats, and integration behavior
    `-- plugin/                     # compatibility tests
```

## WHERE TO LOOK
- Minecraft-backed setup: `test/BaseMcTest.java`.
- Fixture lifecycle: `fixture/` and `library/materials/MaterialRegistryExtension.java`.
- JSON fixtures: `src/test/resources/data/tconstruct/tinkering`.
- Plain JUnit examples: library tool NBT/helper tests that do not extend `BaseMcTest`.

## CONVENTIONS
- Mirror the production package for a test unless the test is a shared fixture or harness.
- Name test classes `*Test`; use `*IntegrationTest` for cross-system behavior.
- Use AssertJ for assertions and Mockito for mocks/static interactions, matching existing dependencies.
- Reset registry/material state through the existing extensions and harness lifecycle.
- Run the narrowest test class first, then the affected package or full `test` task.

## ANTI-PATTERNS
- Do not add a second Gradle test source set without updating the build configuration.
- Do not rely on local runtime state in `run/` for unit-test fixtures.
- Do not bypass `BaseMcTest` when a test touches Forge registries, mod containers, or Minecraft-backed types.
- Do not add GameTest annotations and assume they run; this repository has no implemented GameTest suite.

## SCOPE
Applies to `src/test/java`, `src/test/resources`, and test-only fixtures used by the production source tree.

## READ WHEN
- Adding regression coverage, changing test harness setup, changing fixtures, or evaluating behavior coverage during cleanup.

## SOURCE OF TRUTH
- Test lifecycle: `BaseMcTest` and the existing JUnit extensions.
- Fixture contracts: `fixture/` and `src/test/resources`.
- Production behavior: the source domain guide plus the implementation under test.

## WORKFLOW
1. Identify the observable behavior and the narrowest existing test surface.
2. Add or update a focused test before removing non-obvious production code.
3. Run the focused class, then `.\gradlew.bat test` for shared harness or fixture changes.
