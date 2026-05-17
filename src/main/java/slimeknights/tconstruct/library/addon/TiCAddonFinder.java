package slimeknights.tconstruct.library.addon;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
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
      cache = List.copyOf(addons);
      modIdMap = new LinkedHashMap<>();
      for (ITiCAddon addon : cache) {
        modIdMap.put(addon.addonModId(), addon);
      }
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
}
