package slimeknights.tconstruct.data.gtceu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class TinkersFluidCatalog {
  private final Map<ResourceLocation, TinkersFluidScanner.FluidMapping> mappings;
  private final Map<ResourceLocation, Fluid> fluids;
  private final Map<Fluid, TagKey<Fluid>> recipeTags;

  private TinkersFluidCatalog() {
    this(TinkersFluidScanner.scan());
  }

  TinkersFluidCatalog(List<TinkersFluidScanner.FluidMapping> mappings) {
    Map<ResourceLocation, TinkersFluidScanner.FluidMapping> allMappings = new LinkedHashMap<>();
    Map<ResourceLocation, Fluid> fluids = new LinkedHashMap<>();
    Map<Fluid, TagKey<Fluid>> recipeTags = new LinkedHashMap<>();
    mappings.stream()
      .sorted(Comparator.comparing(mapping -> mapping.id().toString()))
      .forEach(mapping -> {
        allMappings.put(mapping.id(), mapping);
        if (mapping.fluid() != null) {
          fluids.put(mapping.id(), mapping.fluid());
          recipeTags.put(mapping.fluid(), mapping.recipeTag());
        }
      });
    this.mappings = Collections.unmodifiableMap(allMappings);
    this.fluids = Collections.unmodifiableMap(fluids);
    this.recipeTags = Collections.unmodifiableMap(recipeTags);
  }

  static TinkersFluidCatalog instance() {
    return Holder.INSTANCE;
  }

  Map<ResourceLocation, Fluid> fluids() {
    return fluids;
  }

  boolean containsTag(TagKey<Fluid> tag) {
    return mappings.values().stream().anyMatch(mapping -> tag.equals(mapping.recipeTag()));
  }

  boolean contains(Fluid fluid) {
    return recipeTags.containsKey(fluid);
  }

  TagKey<Fluid> recipeTag(Fluid fluid) {
    TagKey<Fluid> mappedTag = recipeTags.get(fluid);
    if (mappedTag != null) {
      return mappedTag;
    }

    ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
    if (fluidId == null) {
      throw new IllegalArgumentException("Unknown fluid: " + fluid);
    }
    return GTConstructFluid.selectRecipeTag(null, null, fluidId);
  }

  private static class Holder {
    private static final TinkersFluidCatalog INSTANCE = new TinkersFluidCatalog();
  }
}
