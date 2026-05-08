package slimeknights.tconstruct.data.material;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.crafting.conditions.AndCondition;
import net.minecraftforge.common.crafting.conditions.OrCondition;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.tconstruct.data.pack.DynamicDataProviderRunner;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.tools.data.material.MaterialDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialStatsDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialTraitsDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class TiCDynamicMaterialGenerator {
  private TiCDynamicMaterialGenerator() {}

  public static void register() {
    registerConditionSerializers();
    register((owner, providers) -> DynamicDataProviderRunner.run(owner, providers));
  }

  static void register(MaterialRunner runner) {
    runner.run("tconstruct-materials", createProviders());
  }

  static List<MaterialProviderEntry> createProviderEntries() {
    MaterialState state = new MaterialState();
    return List.of(
      new MaterialProviderEntry("MaterialDataProvider", state::createMaterialDataProvider),
      new MaterialProviderEntry("MaterialStatsDataProvider", state::createMaterialStatsDataProvider),
      new MaterialProviderEntry("MaterialTraitsDataProvider", state::createMaterialTraitsDataProvider)
    );
  }

  static List<Function<PackOutput, ? extends DataProvider>> createProviders() {
    List<Function<PackOutput, ? extends DataProvider>> providers = new ArrayList<>();
    for (MaterialProviderEntry entry : createProviderEntries()) {
      providers.add(entry.factory());
    }
    return providers;
  }

  private static void registerConditionSerializers() {
    tryRegister(OrCondition.Serializer.INSTANCE);
    tryRegister(AndCondition.Serializer.INSTANCE);
    tryRegister(ConfigEnabledCondition.SERIALIZER);
    tryRegister(TagFilledCondition.SERIALIZER);
  }

  private static void tryRegister(net.minecraftforge.common.crafting.conditions.IConditionSerializer<?> serializer) {
    try {
      CraftingHelper.register(serializer);
    } catch (IllegalStateException ignored) {
      // already registered
    }
  }

  @FunctionalInterface
  interface MaterialRunner {
    void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers);
  }

  record MaterialProviderEntry(String name, Function<PackOutput, ? extends DataProvider> factory) {}

  private static final class MaterialState {
    private MaterialDataProvider materials;

    private DataProvider createMaterialDataProvider(PackOutput output) {
      this.materials = new MaterialDataProvider(output);
      return this.materials;
    }

    private DataProvider createMaterialStatsDataProvider(PackOutput output) {
      if (materials == null) {
        createMaterialDataProvider(output);
      }
      return new MaterialStatsDataProvider(output, materials);
    }

    private DataProvider createMaterialTraitsDataProvider(PackOutput output) {
      if (materials == null) {
        createMaterialDataProvider(output);
      }
      return new MaterialTraitsDataProvider(output, materials);
    }
  }
}
