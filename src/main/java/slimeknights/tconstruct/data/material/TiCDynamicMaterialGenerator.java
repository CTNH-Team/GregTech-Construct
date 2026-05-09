package slimeknights.tconstruct.data.material;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.data.DynamicConditionSerializerRegistrar;
import slimeknights.tconstruct.data.pack.DynamicDataProviderRunner;
import slimeknights.tconstruct.tools.data.material.MaterialDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialStatsDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialTraitsDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class TiCDynamicMaterialGenerator {
  private TiCDynamicMaterialGenerator() {}

  public static void register() {
    DynamicConditionSerializerRegistrar.registerCommonSerializers();
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

    private MaterialDataProvider getMaterials(PackOutput output) {
      if (materials == null) {
        this.materials = (MaterialDataProvider) createMaterialDataProvider(output);
      }
      return materials;
    }

    private DataProvider createMaterialStatsDataProvider(PackOutput output) {
      return new MaterialStatsDataProvider(output, getMaterials(output));
    }

    private DataProvider createMaterialTraitsDataProvider(PackOutput output) {
      return new MaterialTraitsDataProvider(output, getMaterials(output));
    }
  }
}
