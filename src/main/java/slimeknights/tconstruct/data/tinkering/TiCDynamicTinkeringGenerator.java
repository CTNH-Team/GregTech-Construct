package slimeknights.tconstruct.data.tinkering;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.data.DynamicConditionSerializerRegistrar;
import slimeknights.tconstruct.data.pack.DynamicDataProviderRunner;
import slimeknights.tconstruct.tools.data.EnchantmentToModifierProvider;
import slimeknights.tconstruct.tools.data.FluidEffectProvider;
import slimeknights.tconstruct.tools.data.ModifierProvider;
import slimeknights.tconstruct.tools.data.StationSlotLayoutProvider;
import slimeknights.tconstruct.tools.data.ToolDefinitionDataProvider;
import slimeknights.tconstruct.world.data.MobEquipmentProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class TiCDynamicTinkeringGenerator {
  private TiCDynamicTinkeringGenerator() {}

  public static void register() {
    DynamicConditionSerializerRegistrar.registerCommonSerializers();
    register(DynamicDataProviderRunner::run);
  }

  static void register(TinkeringRunner runner) {
    runner.run("tconstruct-tinkering", createProviders());
  }

  static List<TinkeringProviderEntry> createProviderEntries() {
    return List.of(
      new TinkeringProviderEntry("ToolDefinitionDataProvider", ToolDefinitionDataProvider::new),
      new TinkeringProviderEntry("StationSlotLayoutProvider", StationSlotLayoutProvider::new),
      new TinkeringProviderEntry("ModifierProvider", ModifierProvider::new),
      new TinkeringProviderEntry("FluidEffectProvider", FluidEffectProvider::new),
      new TinkeringProviderEntry("EnchantmentToModifierProvider", EnchantmentToModifierProvider::new),
      new TinkeringProviderEntry("MobEquipmentProvider", MobEquipmentProvider::new)
    );
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
