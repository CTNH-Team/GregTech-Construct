package slimeknights.tconstruct.data.tinkering;

import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.data.DynamicConditionSerializerRegistrar;
import slimeknights.tconstruct.data.pack.DynamicPackOutput;
import slimeknights.tconstruct.data.pack.TiCDynamicDataRegistrar;
import slimeknights.tconstruct.library.addon.DynamicDataRegistrar;
import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;
import slimeknights.tconstruct.library.data.RuntimeDataProvider;
import slimeknights.tconstruct.tools.data.EnchantmentToModifierProvider;
import slimeknights.tconstruct.tools.data.FluidEffectProvider;
import slimeknights.tconstruct.tools.data.ModifierProvider;
import slimeknights.tconstruct.tools.data.StationSlotLayoutProvider;
import slimeknights.tconstruct.tools.data.ToolDefinitionDataProvider;
import slimeknights.tconstruct.world.data.MobEquipmentProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class TiCDynamicTinkeringGenerator {
  static final List<TinkeringProviderEntry> ADDITIONAL_PROVIDER_ENTRIES = new ArrayList<>();

  private TiCDynamicTinkeringGenerator() {}

  public static void register() {
    DynamicConditionSerializerRegistrar.registerCommonSerializers();
    register(TiCDynamicDataRegistrar.INSTANCE);
  }

  /** Adds an extra runtime tinkering provider for TiC's dynamic data pack. */
  public static synchronized void addProvider(String name, Consumer<DynamicDataRegistrar> writer) {
    ADDITIONAL_PROVIDER_ENTRIES.add(new TinkeringProviderEntry(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(writer, "writer")
    ));
  }

  static void register(DynamicDataRegistrar registrar) {
    for (TinkeringProviderEntry entry : createProviderEntries()) {
      entry.writer().accept(registrar);
    }
  }

  public static void registerDefaultProviders(DynamicProviderRegistrar registrar) {
    registrar.addProvider("ToolDefinitionDataProvider", dataWriter(ToolDefinitionDataProvider::new));
    registrar.addProvider("StationSlotLayoutProvider", dataWriter(StationSlotLayoutProvider::new));
    registrar.addProvider("ModifierProvider", dataWriter(ModifierProvider::new));
    registrar.addProvider("FluidEffectProvider", dataWriter(FluidEffectProvider::new));
    registrar.addProvider("EnchantmentToModifierProvider", dataWriter(EnchantmentToModifierProvider::new));
    registrar.addProvider("MobEquipmentProvider", dataWriter(MobEquipmentProvider::new));
  }

  static List<TinkeringProviderEntry> createProviderEntries() {
    List<TinkeringProviderEntry> entries = new ArrayList<>();
    TiCAddonRegistry.collectTinkeringProviders((name, writer) -> entries.add(new TinkeringProviderEntry(name, writer)));
    synchronized (TiCDynamicTinkeringGenerator.class) {
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

  record TinkeringProviderEntry(String name, Consumer<DynamicDataRegistrar> writer) {}
}
