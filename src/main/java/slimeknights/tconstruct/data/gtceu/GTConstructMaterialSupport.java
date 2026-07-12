package slimeknights.tconstruct.data.gtceu;

import com.google.gson.JsonObject;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.crafting.conditions.ICondition;
import slimeknights.mantle.util.DataLoadedConditionContext;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.json.JsonRedirect;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialManager;
import slimeknights.tconstruct.library.materials.json.MaterialJson;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.tools.data.material.MaterialStatsDataProvider;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.library.utils.Util;

final class GTConstructMaterialSupport extends MaterialStatsDataProvider {
  private static final PackOutput OUTPUT = new PackOutput(
    Path.of("build", "tmp", "gtconstruct-material-support")
  );
  private static final Logger LOGGER = Util.getLogger("GTConstructMaterialSupport");

  static final GTConstructMaterialSupport INSTANCE = new GTConstructMaterialSupport();

  private final Map<MaterialId, Set<MaterialStatsId>> supportedStats = new HashMap<>();
  private final Set<MaterialId> repairKitMaterials = new HashSet<>();
  private volatile boolean initialized;

  private GTConstructMaterialSupport() {
    super(OUTPUT, new GTConstructEmptyMaterialDataProvider(OUTPUT));
  }

  private void ensureInitialized() {
    if (initialized) {
      return;
    }
    synchronized (this) {
      if (!initialized) {
        super.addMaterialStats();
        initialized = true;
      }
    }
  }

  boolean hasStat(MaterialId material, MaterialStatsId statId) {
    ensureInitialized();
    return supportedStats.getOrDefault(material, Set.of()).contains(statId);
  }

  boolean hasRepairKitSupport(MaterialId material) {
    ensureInitialized();
    return repairKitMaterials.contains(material);
  }

  /**
   * Checks whether a material definition is active in the dynamic data pack.
   *
   * <p>GTM invokes addon recipe writers before {@code MaterialManager} has finished loading its
   * runtime registry. Reading the generated definition keeps this check at the same stage as
   * material loading without touching that registry during recipe registration.</p>
   */
  boolean isMaterialAvailable(MaterialId material) {
    return isMaterialAvailable(material, new HashSet<>());
  }

  boolean hasTag(MaterialId material, TagKey<IMaterial> tag) {
    return TinkersMaterialTagCatalog.instance().contains(tag, material);
  }

  private boolean isMaterialAvailable(MaterialId material, Set<MaterialId> resolving) {
    if (!resolving.add(material)) {
      LOGGER.warn("Detected a material redirect cycle while checking '{}'.", material);
      return false;
    }

    ResourceLocation location = ResourceLocation.tryBuild(
      material.getNamespace(),
      MaterialManager.FOLDER + "/" + material.getPath() + ".json"
    );
    IoSupplier<InputStream> resource = new TiCDynamicDataPack("gtconstruct-material-support")
      .getResource(PackType.SERVER_DATA, location);
    if (resource == null) {
      return hasDynamicMaterialDefinitions()
        ? false
        : isKnownForTests(material);
    }

    try (InputStream stream = resource.get()) {
      JsonObject json = MaterialManager.GSON.fromJson(new String(stream.readAllBytes()), JsonObject.class);
      MaterialJson materialJson = MaterialManager.GSON.fromJson(json, MaterialJson.class);
      ICondition condition = materialJson.getCondition();
      if (condition != null && !condition.test(DataLoadedConditionContext.INSTANCE)) {
        return false;
      }

      JsonRedirect[] redirects = materialJson.getRedirect();
      if (redirects != null) {
        for (JsonRedirect redirect : redirects) {
          ICondition redirectCondition = redirect.getCondition();
          if (redirectCondition == null || redirectCondition.test(DataLoadedConditionContext.INSTANCE)) {
            MaterialId target = new MaterialId(redirect.getId());
            return isMaterialAvailable(target, resolving);
          }
        }
        return false;
      }
      return materialJson.getCraftable() != null;
    } catch (IOException | RuntimeException exception) {
      LOGGER.warn("Failed to read dynamic material definition '{}'.", material, exception);
      return false;
    } finally {
      resolving.remove(material);
    }
  }

  private boolean isKnownForTests(MaterialId material) {
    ensureInitialized();
    return supportedStats.containsKey(material) || repairKitMaterials.contains(material);
  }

  private static boolean hasDynamicMaterialDefinitions() {
    TiCDynamicDataPack pack = new TiCDynamicDataPack("gtconstruct-material-support");
    for (String namespace : pack.getNamespaces(PackType.SERVER_DATA)) {
      final boolean[] found = {false};
      pack.listResources(PackType.SERVER_DATA, namespace, MaterialManager.FOLDER,
        (location, supplier) -> found[0] = true);
      if (found[0]) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void addMaterialStats(MaterialId location, IMaterialStats... stats) {
    Set<MaterialStatsId> statIds = supportedStats.computeIfAbsent(location, ignored -> new HashSet<>());
    for (IMaterialStats stat : stats) {
      statIds.add(stat.getIdentifier());
      if (stat.getType().canRepair()
          || stat.getIdentifier().equals(StatlessMaterialStats.REPAIR_KIT.getIdentifier())) {
        repairKitMaterials.add(location);
      }
    }
  }

  @Override
  protected void addOptionalStats(MaterialId location, IMaterialStats... stats) {
    addMaterialStats(location, stats);
  }
}

final class GTConstructEmptyMaterialDataProvider extends AbstractMaterialDataProvider {
  GTConstructEmptyMaterialDataProvider(PackOutput packOutput) {
    super(packOutput);
  }

  @Override
  public String getName() {
    return "GTConstruct Empty Material Provider";
  }

  @Override
  protected void addMaterials() {}
}
