package slimeknights.tconstruct.data.material;

import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.data.DynamicConditionSerializerRegistrar;
import slimeknights.tconstruct.data.pack.DynamicPackOutput;
import slimeknights.tconstruct.data.pack.TiCDynamicDataRegistrar;
import slimeknights.tconstruct.library.addon.DynamicDataRegistrar;
import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;
import slimeknights.tconstruct.library.data.RuntimeDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialStatsDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialTraitsDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class TiCDynamicMaterialGenerator {
  static final List<MaterialProviderEntry> ADDITIONAL_PROVIDER_ENTRIES = new ArrayList<>();

  private TiCDynamicMaterialGenerator() {}

  public static void register() {
    DynamicConditionSerializerRegistrar.registerCommonSerializers();
    register(TiCDynamicDataRegistrar.INSTANCE);
  }

  /** Adds an extra runtime material provider for TiC's dynamic data pack. */
  public static synchronized void addProvider(String name, Consumer<DynamicDataRegistrar> writer) {
    ADDITIONAL_PROVIDER_ENTRIES.add(new MaterialProviderEntry(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(writer, "writer")
    ));
  }

  static void register(DynamicDataRegistrar registrar) {
    for (MaterialProviderEntry entry : createProviderEntries()) {
      entry.writer().accept(registrar);
    }
  }

  public static void registerDefaultProviders(DynamicProviderRegistrar registrar) {
    MaterialState state = new MaterialState();
    registrar.addProvider("MaterialDataProvider", state::writeMaterialData);
    registrar.addProvider("MaterialStatsDataProvider", state::writeMaterialStats);
    registrar.addProvider("MaterialTraitsDataProvider", state::writeMaterialTraits);
  }

  static List<MaterialProviderEntry> createProviderEntries() {
    List<MaterialProviderEntry> entries = new ArrayList<>();
    TiCAddonRegistry.collectMaterialProviders((name, writer) -> entries.add(new MaterialProviderEntry(name, writer)));
    synchronized (TiCDynamicMaterialGenerator.class) {
      entries.addAll(ADDITIONAL_PROVIDER_ENTRIES);
    }
    return List.copyOf(entries);
  }

  @FunctionalInterface
  public interface RuntimeProviderFactory<T extends RuntimeDataProvider> {
    T create(PackOutput output);
  }

  public static Consumer<DynamicDataRegistrar> dataWriter(RuntimeProviderFactory<? extends RuntimeDataProvider> factory) {
    return registrar -> factory.create(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
  }

  record MaterialProviderEntry(String name, Consumer<DynamicDataRegistrar> writer) {}

  private static final class MaterialState {
    private MaterialDataProvider materials;

    private MaterialDataProvider createMaterialDataProvider(PackOutput output) {
      this.materials = new MaterialDataProvider(output);
      return this.materials;
    }

    private MaterialDataProvider getMaterials(PackOutput output) {
      if (materials == null) {
        this.materials = (MaterialDataProvider) createMaterialDataProvider(output);
      }
      return materials;
    }

    private MaterialStatsDataProvider createMaterialStatsDataProvider(PackOutput output) {
      return new MaterialStatsDataProvider(output, getMaterials(output));
    }

    private MaterialTraitsDataProvider createMaterialTraitsDataProvider(PackOutput output) {
      return new MaterialTraitsDataProvider(output, getMaterials(output));
    }

    private void writeMaterialData(DynamicDataRegistrar registrar) {
      createMaterialDataProvider(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
    }

    private void writeMaterialStats(DynamicDataRegistrar registrar) {
      createMaterialStatsDataProvider(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
    }

    private void writeMaterialTraits(DynamicDataRegistrar registrar) {
      createMaterialTraitsDataProvider(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
    }
  }
}
