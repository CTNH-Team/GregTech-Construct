# Dynamic Tag System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the runtime dynamic tag pack load cleanly without tag-reference crashes or registry bootstrap failures.

**Architecture:** Keep the dynamic tag path focused on tags that can be generated safely at runtime, and build a real `ExistingFileHelper` for providers that expect one. Split registry bootstrap concerns from tag emission so runtime tag generation does not pull in unsupported worldgen/data-registry setup.

**Tech Stack:** Java 17, Forge 1.20.1 datagen APIs, JUnit 5, AssertJ, Gradle test tasks.

---

### Task 1: Pin the current dynamic tag contract

**Files:**
- Modify: `src/test/java/slimeknights/tconstruct/data/tag/TiCDynamicTagGeneratorTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void registerUsesRuntimeSafeProviderSet() {
    assertThat(TiCDynamicTagGenerator.createProviderEntries()).extracting(TiCDynamicTagGenerator.TagProviderEntry::name).doesNotContain("BiomeTagProvider");
}

@Test
void registerBuildsHelperBackedProviders() {
    RecordingRunner runner = new RecordingRunner();

    TiCDynamicTagGenerator.register(runner);

    assertThat(runner.providers).isNotEmpty();
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew.bat test --tests slimeknights.tconstruct.data.tag.TiCDynamicTagGeneratorTest`
Expected: FAIL because the current provider set still includes runtime-unsafe entries and the helper-backed behavior is not implemented yet.

- [ ] **Step 3: Write minimal implementation**

Keep this task test-only; do not change production code yet.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew.bat test --tests slimeknights.tconstruct.data.tag.TiCDynamicTagGeneratorTest`
Expected: PASS after the generator is narrowed and helper-backed.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/slimeknights/tconstruct/data/tag/TiCDynamicTagGeneratorTest.java
git commit -m "test: pin dynamic tag runtime contract"
```

### Task 2: Build a real runtime `ExistingFileHelper`

**Files:**
- Modify: `src/main/java/slimeknights/tconstruct/data/tag/TiCDynamicTagGenerator.java`
- Modify: `src/test/java/slimeknights/tconstruct/data/tag/TiCDynamicTagGeneratorTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void helperBackedProvidersDoNotNpe() {
    TiCDynamicDataPack.clearServer();

    assertThatCode(() -> DynamicDataProviderRunner.run("test-tags", TiCDynamicTagGenerator.createProviders()))
        .doesNotThrowAnyException();
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew.bat test --tests slimeknights.tconstruct.data.tag.TiCDynamicTagGeneratorTest`
Expected: FAIL with the existing null-helper behavior in `MaterialTagProvider` and `ModifierTagProvider`.

- [ ] **Step 3: Write minimal implementation**

```java
private static ExistingFileHelper createExistingFileHelper() {
    return new ExistingFileHelper(List.of(), Set.of(), false, null, null);
}
```

Use that non-null, validation-disabled helper for every runtime provider that currently receives `existingFileHelper = null`; runtime dynamic generation should emit resources, not fail on static datagen reference validation.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew.bat test --tests slimeknights.tconstruct.data.tag.TiCDynamicTagGeneratorTest`
Expected: PASS and no `ExistingFileHelper.trackGenerated(...)` NPEs in the dynamic tag path.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/slimeknights/tconstruct/data/tag/TiCDynamicTagGenerator.java src/test/java/slimeknights/tconstruct/data/tag/TiCDynamicTagGeneratorTest.java
git commit -m "fix: provide runtime existing file helper"
```

### Task 3: Remove unsupported runtime tag/bootstrap inputs

**Files:**
- Modify: `src/main/java/slimeknights/tconstruct/data/tag/TiCDynamicTagGenerator.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void providerEntriesExcludeUnsupportedRuntimeTags() {
    assertThat(TiCDynamicTagGenerator.createProviderEntries())
        .extracting(TiCDynamicTagGenerator.TagProviderEntry::name)
        .doesNotContain("BiomeTagProvider");
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew.bat test --tests slimeknights.tconstruct.data.tag.TiCDynamicTagGeneratorTest`
Expected: FAIL while the runtime provider list still includes the unsafe biome registry path.

- [ ] **Step 3: Write minimal implementation**

```java
static List<TagProviderEntry> createProviderEntries() {
    TagProviderState state = new TagProviderState();
    return List.of(
        new TagProviderEntry("BlockTagProvider", state::createBlockTags),
        new TagProviderEntry("ItemTagProvider", state::createItemTags),
        new TagProviderEntry("FluidTagProvider", state::createFluidTags),
        new TagProviderEntry("EntityTypeTagProvider", state::createEntityTypeTags),
        new TagProviderEntry("BlockEntityTypeTagProvider", state::createBlockEntityTypeTags),
        new TagProviderEntry("EnchantmentTagProvider", state::createEnchantmentTags),
        new TagProviderEntry("MenuTypeTagProvider", state::createMenuTypeTags),
        new TagProviderEntry("PotionTagProvider", state::createPotionTags),
        new TagProviderEntry("DamageTypeTagProvider", state::createDamageTypeTags),
        new TagProviderEntry("MaterialTagProvider", state::createMaterialTags),
        new TagProviderEntry("ModifierTagProvider", state::createModifierTags)
    );
}
```

Remove `BiomeTagProvider` from the runtime provider list. Keep the damage-type registry bootstrap limited to the entries that `DamageTypeTagProvider` actually consumes.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew.bat test --tests slimeknights.tconstruct.data.tag.TiCDynamicTagGeneratorTest`
Expected: PASS and the runtime dynamic pack no longer tries to bootstrap unsupported biome/worldgen pieces.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/slimeknights/tconstruct/data/tag/TiCDynamicTagGenerator.java
git commit -m "fix: narrow dynamic tag bootstrap"
```

### Task 4: Verify the dynamic pack end-to-end

**Files:**
- Modify: `src/test/java/slimeknights/tconstruct/data/pack/DynamicDataProviderRunnerTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void dynamicTagRunnerStillCapturesLaterProvidersAfterOneFailure() {
    DynamicDataProviderRunner.run(
        "test",
        output -> new TestTagProvider(output),
        output -> { throw new IllegalStateException("expected"); },
        output -> new TestTagProvider(output)
    );

    ResourceLocation location = new ResourceLocation("example", "tags/items/generated.json");
    assertThat(pack.getResource(PackType.SERVER_DATA, location)).isNotNull();
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew.bat test --tests slimeknights.tconstruct.data.pack.DynamicDataProviderRunnerTest`
Expected: PASS, proving the runner still logs provider failures and captures resources from later providers.

- [ ] **Step 3: Write minimal implementation**

No runner code change is expected here; this step confirms the existing log-and-continue behavior stays intact after the tag-system changes.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew.bat test --tests slimeknights.tconstruct.data.pack.DynamicDataProviderRunnerTest --tests slimeknights.tconstruct.data.tag.TiCDynamicTagGeneratorTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/slimeknights/tconstruct/data/pack/DynamicDataProviderRunnerTest.java
git commit -m "test: lock down dynamic pack resilience"
```

### Task 5: Run the targeted verification suite

**Files:**
- None

- [ ] **Step 1: Run the focused tests**

Run: `./gradlew.bat test --tests slimeknights.tconstruct.data.tag.TiCDynamicTagGeneratorTest --tests slimeknights.tconstruct.data.pack.DynamicDataProviderRunnerTest`
Expected: PASS.

- [ ] **Step 2: Check for the original runtime errors**

Run the game path that previously triggered dynamic tags.
Expected: No `Missing references`, no `Biome registry does not have support for tags`, no `existingFileHelper is null`.

- [ ] **Step 3: Commit**

```bash
git add src/main/java src/test/java docs/superpowers/plans/2026-05-06-dynamic-tag-system.md
git commit -m "docs: plan dynamic tag repair"
```

### Gaps to watch

- The runtime helper factory needs the exact Forge constructor shape and the right mod-id set.
- The damage-type registry bootstrap must stay minimal so it does not reintroduce the registry-creation failures.
- The final provider list must stay aligned with the runtime pack, not the old datagen-only path.
