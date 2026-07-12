package slimeknights.tconstruct.data.gtceu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.utils.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

/**
 * Reflective adapter for the public fluid objects exposed by {@link TinkerFluids}.
 *
 * <p>Reflection stays isolated here so the public compatibility facade does not also own discovery,
 * filtering, and ordering policy.</p>
 */
final class TinkersFluidScanner {
  private static final Logger LOGGER = Util.getLogger("GTConstructFluid");

  private TinkersFluidScanner() {}

  static List<FluidMapping> scan() {
    return Arrays.stream(TinkerFluids.class.getDeclaredFields())
      .filter(TinkersFluidScanner::isFluidField)
      .map(TinkersFluidScanner::read)
      .flatMap(Optional::stream)
      .sorted(Comparator.comparing(mapping -> mapping.id().toString()))
      .toList();
  }

  private static boolean isFluidField(Field field) {
    int modifiers = field.getModifiers();
    return Modifier.isPublic(modifiers)
      && Modifier.isStatic(modifiers)
      && FluidObject.class.isAssignableFrom(field.getType());
  }

  private static Optional<FluidMapping> read(Field field) {
    try {
      Object value = field.get(null);
      if (!(value instanceof FluidObject<?> fluidObject)) {
        return Optional.empty();
      }

      ResourceLocation objectId = fluidObject.getId();
      Fluid fluid;
      try {
        fluid = fluidObject.get();
      } catch (RuntimeException e) {
        TagKey<Fluid> recipeTag = getRecipeTag(fluidObject, objectId);
        if (recipeTag == null) {
          LOGGER.debug("Skipping unavailable Tinkers' Construct fluid field '{}'.", field.getName());
          return Optional.empty();
        }
        return Optional.of(new FluidMapping(objectId, null, recipeTag));
      }
      Optional<ResourceLocation> fluidId = getRegisteredId(fluid);
      if (fluidId.isEmpty()) {
        LOGGER.debug("Skipping unregistered Tinkers' Construct fluid field '{}'.", field.getName());
        return Optional.empty();
      }

      TagKey<Fluid> recipeTag = getRecipeTag(fluidObject, fluidId.get());
      return Optional.of(new FluidMapping(fluidId.get(), fluid, recipeTag));
    } catch (IllegalAccessException e) {
      LOGGER.error("Failed to read Tinkers' Construct fluid field '{}'.", field.getName(), e);
      return Optional.empty();
    }
  }

  private static Optional<ResourceLocation> getRegisteredId(Fluid fluid) {
    try {
      if (ForgeRegistries.FLUIDS.getDelegate(fluid).isEmpty()) {
        return Optional.empty();
      }
      return Optional.ofNullable(ForgeRegistries.FLUIDS.getKey(fluid));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private static TagKey<Fluid> getRecipeTag(FluidObject<?> fluidObject, ResourceLocation fluidId) {
    if (fluidObject instanceof FlowingFluidObject<?> flowingFluid) {
      return GTConstructFluid.selectRecipeTag(flowingFluid.getCommonTag(), flowingFluid.getLocalTag(), fluidId);
    }
    return GTConstructFluid.selectRecipeTag(fluidObject.getCommonTag(), null, fluidId);
  }

  record FluidMapping(ResourceLocation id, @Nullable Fluid fluid, TagKey<Fluid> recipeTag) {}
}
