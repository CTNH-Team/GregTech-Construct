package slimeknights.tconstruct.library.addon;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import slimeknights.tconstruct.library.utils.Util;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Finds addon entrypoints annotated with {@link TiCAddon}.
 */
public final class TiCAddonFinder {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final ITiCAddon BUILTIN_ADDON = new TConstructBuiltinAddon();

  static List<ITiCAddon> cache;
  static Map<String, ITiCAddon> modIdMap = new LinkedHashMap<>();

  private TiCAddonFinder() {}

  public static List<ITiCAddon> getAddons() {
    if (cache == null) {
      List<ITiCAddon> addons = new ArrayList<>();
      addons.add(BUILTIN_ADDON);
      addons.addAll(getInstances(TiCAddon.class, ITiCAddon.class));
      List<ITiCAddon> builtAddons = List.copyOf(addons);
      Map<String, ITiCAddon> builtModIdMap = new LinkedHashMap<>();
      for (ITiCAddon addon : builtAddons) {
        String modId = addon.addonModId();
        if (modId == null) {
          throw new IllegalStateException("TiC addon " + addon.getClass().getName() + " has a null addon mod ID");
        }
        if (modId.isBlank()) {
          throw new IllegalStateException("TiC addon " + addon.getClass().getName() + " has a blank addon mod ID");
        }
        ITiCAddon duplicate = builtModIdMap.putIfAbsent(modId, addon);
        if (duplicate != null) {
          throw new IllegalStateException("Duplicate TiC addon mod ID '" + modId + "' for " + duplicate.getClass().getName() + " and " + addon.getClass().getName());
        }
      }
      cache = builtAddons;
      modIdMap = builtModIdMap;
    }
    return cache;
  }

  public static ITiCAddon getAddon(String modId) {
    getAddons();
    return modIdMap.get(modId);
  }

  private static <T> List<T> getInstances(Class<?> annotationClass, Class<T> instanceClass) {
    ModList modList = ModList.get();
    if (modList == null) {
      return List.of();
    }
    Type annotationType = Type.getType(annotationClass);
    List<ModFileScanData> allScanData = modList.getAllScanData();
    LinkedHashSet<String> addonClassNames = new LinkedHashSet<>();
    for (ModFileScanData scanData : allScanData) {
      for (ModFileScanData.AnnotationData annotation : scanData.getAnnotations()) {
        if (Objects.equals(annotation.annotationType(), annotationType)) {
          if (missingRequiredMod(annotation)) {
            continue;
          }
          addonClassNames.add(annotation.memberName());
        }
      }
    }

    List<T> instances = new ArrayList<>();
    for (String className : addonClassNames) {
      try {
        Class<?> asmClass = Class.forName(className);
        Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
        Constructor<? extends T> constructor = asmInstanceClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        instances.add(constructor.newInstance());
      } catch (ReflectiveOperationException | LinkageError exception) {
        LOGGER.error("Failed to load TiC addon {}", className, exception);
      }
    }
    return instances;
  }

  @SuppressWarnings("unchecked")
  private static boolean missingRequiredMod(ModFileScanData.AnnotationData annotation) {
    Object modIds = annotation.annotationData().get("modID");
    if (!(modIds instanceof List<?> mods) || mods.isEmpty()) {
      return false;
    }
    for (Object mod : mods) {
      if (mod instanceof String modId && !Util.isModLoaded(modId)) {
        LOGGER.debug("Skipping TiC addon {} because required mod {} is not loaded", annotation.memberName(), modId);
        return true;
      }
    }
    if (!mods.stream().allMatch(String.class::isInstance)) {
      LOGGER.warn("Ignoring malformed modID on TiC addon {}: {}", annotation.memberName(), Arrays.toString(mods.toArray()));
    }
    return false;
  }
}
