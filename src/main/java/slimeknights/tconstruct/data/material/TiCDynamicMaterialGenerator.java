package slimeknights.tconstruct.data.material;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.data.DynamicConditionSerializerRegistrar;
import slimeknights.tconstruct.data.pack.DynamicDataProviderRunner;
import slimeknights.tconstruct.data.pack.DynamicProviderFactory;
import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;
import slimeknights.tconstruct.tools.data.material.MaterialDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialStatsDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialTraitsDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class TiCDynamicMaterialGenerator {
  static final List<MaterialProviderEntry> ADDITIONAL_PROVIDER_ENTRIES = new ArrayList<>();

  private TiCDynamicMaterialGenerator() {}

  public static void register() {
    DynamicConditionSerializerRegistrar.registerCommonSerializers();
    register(DynamicDataProviderRunner::run);
  }

  /** Adds an extra runtime material provider for TiC's dynamic data pack. */
  public static synchronized void addProvider(String name, Function<PackOutput, ? extends DataProvider> factory) {
    ADDITIONAL_PROVIDER_ENTRIES.add(new MaterialProviderEntry(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(factory, "factory")
    ));
  }

  static void register(MaterialRunner runner) {
    runner.run("tconstruct-materials", createProviders());
  }

  public static void registerDefaultProviders(DynamicProviderRegistrar registrar) {
    MaterialState state = new MaterialState();
    registrar.addProvider("MaterialDataProvider", state::createMaterialDataProvider);
    registrar.addProvider("MaterialStatsDataProvider", state::createMaterialStatsDataProvider);
    registrar.addProvider("MaterialTraitsDataProvider", state::createMaterialTraitsDataProvider);
  }

  static List<MaterialProviderEntry> createProviderEntries() {
    List<MaterialProviderEntry> entries = new ArrayList<>();
    TiCAddonRegistry.collectMaterialProviders((name, factory) -> entries.add(new MaterialProviderEntry(name, factory)));
    synchronized (TiCDynamicMaterialGenerator.class) {
      entries.addAll(ADDITIONAL_PROVIDER_ENTRIES);
    }
    return List.copyOf(entries);
  }

  static List<Function<PackOutput, ? extends DataProvider>> createProviders() {
    List<Function<PackOutput, ? extends DataProvider>> providers = new ArrayList<>();
    for (MaterialProviderEntry entry : createProviderEntries()) {
      providers.add(new DynamicProviderFactory(entry.name(), entry.factory()));
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
