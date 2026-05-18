package slimeknights.tconstruct.data.advancement;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.common.data.AdvancementsProvider;
import slimeknights.tconstruct.data.DynamicConditionSerializerRegistrar;
import slimeknights.tconstruct.data.pack.DynamicAdvancementProviderRunner;
import slimeknights.tconstruct.data.pack.DynamicProviderFactory;
import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class TiCDynamicAdvancementGenerator {
  static final List<AdvancementProviderEntry> ADDITIONAL_PROVIDER_ENTRIES = new ArrayList<>();

  private TiCDynamicAdvancementGenerator() {}

  public static void register() {
    DynamicConditionSerializerRegistrar.registerCommonSerializers();
    register(DynamicAdvancementProviderRunner::run);
  }

  /** Adds an extra runtime advancement provider for TiC's dynamic data pack. */
  public static synchronized void addProvider(String name, Function<PackOutput, ? extends DataProvider> factory) {
    ADDITIONAL_PROVIDER_ENTRIES.add(new AdvancementProviderEntry(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(factory, "factory")
    ));
  }

  static void register(AdvancementRunner runner) {
    runner.run("tconstruct-advancements", createProviders());
  }

  public static void registerDefaultProviders(DynamicProviderRegistrar registrar) {
    registrar.addProvider("AdvancementsProvider", AdvancementsProvider::new);
  }

  static List<AdvancementProviderEntry> createProviderEntries() {
    List<AdvancementProviderEntry> entries = new ArrayList<>();
    TiCAddonRegistry.collectAdvancementProviders((name, factory) -> entries.add(new AdvancementProviderEntry(name, factory)));
    synchronized (TiCDynamicAdvancementGenerator.class) {
      entries.addAll(ADDITIONAL_PROVIDER_ENTRIES);
    }
    return List.copyOf(entries);
  }

  static List<Function<PackOutput, ? extends DataProvider>> createProviders() {
    List<Function<PackOutput, ? extends DataProvider>> providers = new ArrayList<>();
    for (AdvancementProviderEntry entry : createProviderEntries()) {
      providers.add(new DynamicProviderFactory(entry.name(), entry.factory()));
    }
    return providers;
  }

  @FunctionalInterface
  interface AdvancementRunner {
    void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers);
  }

  record AdvancementProviderEntry(String name, Function<PackOutput, ? extends DataProvider> factory) {}
}
