package slimeknights.tconstruct.data.advancement;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.common.data.AdvancementsProvider;
import slimeknights.tconstruct.data.DynamicConditionSerializerRegistrar;
import slimeknights.tconstruct.data.pack.DynamicAdvancementProviderRunner;

import java.util.List;
import java.util.function.Function;

public final class TiCDynamicAdvancementGenerator {
  private TiCDynamicAdvancementGenerator() {}

  public static void register() {
    DynamicConditionSerializerRegistrar.registerCommonSerializers();
    register(DynamicAdvancementProviderRunner::run);
  }

  static void register(AdvancementRunner runner) {
    runner.run("tconstruct-advancements", createProviders());
  }

  static List<AdvancementProviderEntry> createProviderEntries() {
    return List.of(
      new AdvancementProviderEntry("AdvancementsProvider", AdvancementsProvider::new)
    );
  }

  static List<Function<PackOutput, ? extends DataProvider>> createProviders() {
    return List.of(AdvancementsProvider::new);
  }

  @FunctionalInterface
  interface AdvancementRunner {
    void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers);
  }

  record AdvancementProviderEntry(String name, Function<PackOutput, ? extends DataProvider> factory) {}
}
