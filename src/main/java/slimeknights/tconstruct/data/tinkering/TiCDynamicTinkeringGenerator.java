package slimeknights.tconstruct.data.tinkering;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.data.DynamicConditionSerializerRegistrar;
import slimeknights.tconstruct.data.pack.DynamicDataProviderRunner;
import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;
import slimeknights.tconstruct.tools.data.EnchantmentToModifierProvider;
import slimeknights.tconstruct.tools.data.FluidEffectProvider;
import slimeknights.tconstruct.tools.data.ModifierProvider;
import slimeknights.tconstruct.tools.data.StationSlotLayoutProvider;
import slimeknights.tconstruct.tools.data.ToolDefinitionDataProvider;
import slimeknights.tconstruct.world.data.MobEquipmentProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class TiCDynamicTinkeringGenerator {
  static final List<TinkeringProviderEntry> ADDITIONAL_PROVIDER_ENTRIES = new ArrayList<>();

  private TiCDynamicTinkeringGenerator() {}

  public static void register() {
    DynamicConditionSerializerRegistrar.registerCommonSerializers();
    register(DynamicDataProviderRunner::run);
  }

  /** Adds an extra runtime tinkering provider for TiC's dynamic data pack. */
  public static synchronized void addProvider(String name, Function<PackOutput, ? extends DataProvider> factory) {
    ADDITIONAL_PROVIDER_ENTRIES.add(new TinkeringProviderEntry(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(factory, "factory")
    ));
  }

  static void register(TinkeringRunner runner) {
    runner.run("tconstruct-tinkering", createProviders());
  }

  public static void registerDefaultProviders(DynamicProviderRegistrar registrar) {
    registrar.addProvider("ToolDefinitionDataProvider", ToolDefinitionDataProvider::new);
    registrar.addProvider("StationSlotLayoutProvider", StationSlotLayoutProvider::new);
    registrar.addProvider("ModifierProvider", ModifierProvider::new);
    registrar.addProvider("FluidEffectProvider", FluidEffectProvider::new);
    registrar.addProvider("EnchantmentToModifierProvider", EnchantmentToModifierProvider::new);
    registrar.addProvider("MobEquipmentProvider", MobEquipmentProvider::new);
  }

  static List<TinkeringProviderEntry> createProviderEntries() {
    List<TinkeringProviderEntry> entries = new ArrayList<>();
    TiCAddonRegistry.collectTinkeringProviders((name, factory) -> entries.add(new TinkeringProviderEntry(name, factory)));
    synchronized (TiCDynamicTinkeringGenerator.class) {
      entries.addAll(ADDITIONAL_PROVIDER_ENTRIES);
    }
    return List.copyOf(entries);
  }

  static List<Function<PackOutput, ? extends DataProvider>> createProviders() {
    List<Function<PackOutput, ? extends DataProvider>> providers = new ArrayList<>();
    for (TinkeringProviderEntry entry : createProviderEntries()) {
      providers.add(entry.factory());
    }
    return providers;
  }

  @FunctionalInterface
  interface TinkeringRunner {
    void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers);
  }

  record TinkeringProviderEntry(String name, Function<PackOutput, ? extends DataProvider> factory) {}
}
