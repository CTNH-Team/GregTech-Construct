package slimeknights.tconstruct.data.gtceu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

public class GTConstructFluid {

  private static final Map<ResourceLocation, Fluid> ALL_TCON_FLUIDS = new HashMap<>();
  private static final Map<Fluid, TagKey<Fluid>> RECIPE_TAGS = new HashMap<>();
  private static final Logger LOGGER = Util.getLogger("GTConstructFluid");
  private static boolean initialized = false;

  private GTConstructFluid() {}

  private static synchronized void ensureInitialized() {
    if (initialized) {
      return;
    }
    initializeFluidMappings();
    initialized = true;
  }

  private static void initializeFluidMappings() {
    LOGGER.info("Initializing Tinkers' Construct fluid mappings via reflection...");
    int foundCount = 0;
    try {
      Field[] fields = TinkerFluids.class.getDeclaredFields();
      for (Field field : fields) {
        int modifiers = field.getModifiers();
        if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
          Class<?> fieldType = field.getType();
          if (FluidObject.class.isAssignableFrom(fieldType) || FlowingFluidObject.class.isAssignableFrom(fieldType)) {
            Object fluidObjectInstance = field.get(null);
            if (fluidObjectInstance != null) {
              Fluid fluid = ((FluidObject<?>) fluidObjectInstance).get();
              if (fluid != null) {
                ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
                if (fluidId != null) {
                  ALL_TCON_FLUIDS.put(fluidId, fluid);
                  RECIPE_TAGS.put(fluid, getRecipeTag(fluidObjectInstance, fluidId));
                  foundCount++;
                }
              }
            }
          }
        }
      }
    } catch (IllegalAccessException e) {
      LOGGER.error("Failed to initialize fluid mappings via reflection!", e);
    }
    LOGGER.info("Successfully initialized {} Tinkers' Construct fluid mappings.", foundCount);
  }

  public static Map<ResourceLocation, Fluid> getAllTinkersFluids() {
    ensureInitialized();
    return Map.copyOf(ALL_TCON_FLUIDS);
  }

  public static TagKey<Fluid> getAutoTag(Fluid fluid) {
    ensureInitialized();
    TagKey<Fluid> mappedTag = RECIPE_TAGS.get(fluid);
    if (mappedTag != null) {
      return mappedTag;
    }
    ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
    if (fluidId == null) {
      throw new IllegalArgumentException("Unknown fluid: " + fluid);
    }
    return TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), ResourceLocation.tryBuild("forge", fluidId.getPath()));
  }

  static TagKey<Fluid> selectRecipeTag(@Nullable TagKey<Fluid> commonTag, @Nullable TagKey<Fluid> localTag, ResourceLocation fluidId) {
    if (commonTag != null) {
      return commonTag;
    }
    if (localTag != null) {
      return localTag;
    }
    return TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), ResourceLocation.tryBuild("forge", fluidId.getPath()));
  }

  @SuppressWarnings("unchecked")
  private static TagKey<Fluid> getRecipeTag(Object fluidObjectInstance, ResourceLocation fluidId) {
    if (fluidObjectInstance instanceof FlowingFluidObject<?> flowingFluid) {
      return selectRecipeTag(flowingFluid.getCommonTag(), flowingFluid.getLocalTag(), fluidId);
    }
    FluidObject<?> fluidObject = (FluidObject<?>) fluidObjectInstance;
    return selectRecipeTag(fluidObject.getCommonTag(), null, fluidId);
  }

  public static String extractMaterialName(String path) {
    if (path.startsWith("")) {
      return path.substring("".length());
    }
    return path;
  }
}
