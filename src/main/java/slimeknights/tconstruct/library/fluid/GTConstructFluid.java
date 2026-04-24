package slimeknights.tconstruct.library.fluid;

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
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

public class GTConstructFluid {

  private static final Map<ResourceLocation, Fluid> ALL_TCON_FLUIDS = new HashMap<>();
  private static final Logger LOGGER = Util.getLogger("GTConstructFluid");

  static {
    initializeFluidMappings();
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
    return Map.copyOf(ALL_TCON_FLUIDS);
  }

  public static TagKey<Fluid> getAutoTag(Fluid fluid) {
    ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
    if (fluidId == null) {
      throw new IllegalArgumentException("Unknown fluid: " + fluid);
    }
    return TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), ResourceLocation.tryBuild("forge", fluidId.getPath()));
  }

  public static String extractMaterialName(String path) {
    if (path.startsWith("")) {
      return path.substring("".length());
    }
    return path;
  }
}
