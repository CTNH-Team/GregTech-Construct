package slimeknights.tconstruct.data.gtceu;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import slimeknights.tconstruct.common.data.tags.MaterialTagProvider;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Expands TConstruct's material tag declarations for recipe-time material checks.
 *
 * <p>The provider remains the source of truth, so new parts and addon tag hooks are picked up
 * without maintaining a second list of material names in the GTM integration.</p>
 */
final class TinkersMaterialTagCatalog {
  private static final PackOutput OUTPUT = new PackOutput(Path.of("build", "tmp", "gtconstruct-material-tags"));

  private TinkersMaterialTagCatalog() {}

  static TinkersMaterialTagCatalog instance() {
    return Holder.INSTANCE;
  }

  private final Map<ResourceLocation, Set<MaterialId>> values = createValues();

  boolean contains(TagKey<IMaterial> tag, MaterialId material) {
    return values.getOrDefault(tag.location(), Set.of()).contains(material);
  }

  private static Map<ResourceLocation, Set<MaterialId>> createValues() {
    TagCollector collector = new TagCollector();
    Map<ResourceLocation, TagBuilder> builders = collector.collect();
    Map<ResourceLocation, Set<MaterialId>> values = new HashMap<>();
    for (ResourceLocation tag : builders.keySet()) {
      values.put(tag, expand(tag, builders, new HashMap<>(), new LinkedHashSet<>()));
    }
    return Map.copyOf(values);
  }

  private static Set<MaterialId> expand(ResourceLocation tag,
                                        Map<ResourceLocation, TagBuilder> builders,
                                        Map<ResourceLocation, Set<MaterialId>> cache,
                                        Set<ResourceLocation> resolving) {
    Set<MaterialId> cached = cache.get(tag);
    if (cached != null) {
      return cached;
    }
    if (!resolving.add(tag)) {
      return Set.of();
    }

    LinkedHashSet<MaterialId> values = new LinkedHashSet<>();
    TagBuilder builder = builders.get(tag);
    if (builder != null) {
      TagEntry.Lookup<MaterialId> lookup = new TagEntry.Lookup<>() {
        @Override
        public MaterialId element(ResourceLocation id) {
          return new MaterialId(id);
        }

        @Override
        public Set<MaterialId> tag(ResourceLocation id) {
          return expand(id, builders, cache, resolving);
        }
      };
      for (TagEntry entry : builder.build()) {
        entry.build(lookup, values::add);
      }
    }

    resolving.remove(tag);
    Set<MaterialId> expanded = Collections.unmodifiableSet(new LinkedHashSet<>(values));
    cache.put(tag, expanded);
    return expanded;
  }

  private static final class TagCollector extends MaterialTagProvider {
    private TagCollector() {
      super(OUTPUT, null);
    }

    @Override
    protected TagBuilder getOrCreateRawBuilder(TagKey<IMaterial> tag) {
      return builders.computeIfAbsent(tag.location(), ignored -> TagBuilder.create());
    }

    private Map<ResourceLocation, TagBuilder> collect() {
      builders.clear();
      addTags();
      return Map.copyOf(builders);
    }
  }

  private static final class Holder {
    private static final TinkersMaterialTagCatalog INSTANCE = new TinkersMaterialTagCatalog();
  }
}
