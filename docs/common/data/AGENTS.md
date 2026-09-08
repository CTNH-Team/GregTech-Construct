# COMMON DATA PROVIDERS

## OVERVIEW
Static Forge data generation for tags, loot, recipes, advancements, and related resources.

## WHERE TO LOOK
- `GatherDataEvent` registration and provider ordering: `TConstruct.gatherData()`.
- Shared provider base classes: `common/data/`.
- Generated output: `src/generated/resources/`.
- Data-provider tests: `src/test/java/slimeknights/tconstruct/data/`.

## CONVENTIONS
- Provider classes use `*Provider` names and write through Forge/Mantle data APIs.
- Existing resources under `src/main/resources` are inputs; generated resources are outputs.
- Compatibility namespaces may be generated intentionally (`forge`, `minecraft`, `gtceu`, and addon namespaces).
- Keep provider ordering stable when one provider consumes ids or tags from another.

## ANTI-PATTERNS
- Do not patch generated JSON to fix a provider bug.
- Do not assume a generated resource belongs to `tconstruct`; inspect its namespace before changing inputs.
- Do not add runtime dynamic-pack behavior to static providers; use `src/main/java/slimeknights/tconstruct/data`.

## NOTES
- `src/generated/resources` is included in `sourceSets.main.resources` by `build.gradle`.
- Data generation reads existing authored resources from `src/main/resources`.
- A provider can intentionally emit data for foreign namespaces used by compatibility.
- Run the narrowest data-generation test before invoking the full `runData` task.
- Changes to ids, tags, or provider ordering may require regenerating many tracked files.
- `FunctionProvider` emits `functions/grant_advancement/*.mcfunction` plus 8 internal advancements; keep fork-owned `combat/*`, `full_set/knights`, `special/*`.
## CHANGE IMPACT
- New registry objects usually need tags, loot, recipes, or advancements here.
- Verify generated diffs before retaining them; unrelated resource churn usually indicates an input or ordering issue.

## TESTING
- Prefer the matching provider test under `src/test/java/slimeknights/tconstruct/data`.

## SCOPE
Applies to `src/main/java/slimeknights/tconstruct/common/data` and the static data providers it owns.

## READ WHEN
- Adding a provider, changing a registry id, changing tags/loot/recipes, or reviewing generated-resource churn.

## SOURCE OF TRUTH
- Provider Java code and authored resources in `src/main/resources`.
- Generated output in `src/generated/resources` is a build product, not the design source.

## WORKFLOW
1. Change the provider or authored input.
2. Run the relevant provider test, then `.\gradlew.bat runData` when regeneration is required.
3. Inspect the generated diff for expected namespaces, ids, and file count.
