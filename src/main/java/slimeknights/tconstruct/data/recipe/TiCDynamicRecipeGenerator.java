package slimeknights.tconstruct.data.recipe;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.gadgets.data.GadgetRecipeProvider;
import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;
import slimeknights.tconstruct.shared.data.CommonRecipeProvider;
import slimeknights.tconstruct.smeltery.data.SmelteryRecipeProvider;
import slimeknights.tconstruct.tables.data.TableRecipeProvider;
import slimeknights.tconstruct.tools.data.ModifierRecipeProvider;
import slimeknights.tconstruct.tools.data.ToolsRecipeProvider;
import slimeknights.tconstruct.tools.data.material.MaterialRecipeProvider;
import slimeknights.tconstruct.world.data.WorldRecipeProvider;
import slimeknights.tconstruct.data.pack.DynamicRecipeProviderRunner;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;

public final class TiCDynamicRecipeGenerator {
  static final List<RecipeProviderEntry> ADDITIONAL_PROVIDER_ENTRIES = new ArrayList<>();

  private TiCDynamicRecipeGenerator() {}

  public static void register() {
    register(DynamicRecipeProviderRunner::run);
  }

  /** Adds an extra runtime recipe provider for TiC's dynamic recipe pack. */
  public static synchronized void addProvider(String name, Function<PackOutput, ? extends DataProvider> factory) {
    ADDITIONAL_PROVIDER_ENTRIES.add(new RecipeProviderEntry(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(factory, "factory")
    ));
  }

  static void register(RecipeRunner runner) {
    runner.run("tconstruct-recipes", createProviders());
  }

  public static void registerDefaultProviders(DynamicProviderRegistrar registrar) {
    registrar.addProvider("CommonRecipeProvider", CommonRecipeProvider::new);
    registrar.addProvider("TableRecipeProvider", TableRecipeProvider::new);
    registrar.addProvider("GadgetRecipeProvider", GadgetRecipeProvider::new);
    registrar.addProvider("WorldRecipeProvider", WorldRecipeProvider::new);
    registrar.addProvider("MaterialRecipeProvider", MaterialRecipeProvider::new);
    registrar.addProvider("ToolsRecipeProvider", ToolsRecipeProvider::new);
    registrar.addProvider("SmelteryRecipeProvider", SmelteryRecipeProvider::new);
    registrar.addProvider("ModifierRecipeProvider", ModifierRecipeProvider::new);
  }

  static List<RecipeProviderEntry> createProviderEntries() {
    List<RecipeProviderEntry> entries = new ArrayList<>();
    TiCAddonRegistry.collectRecipeProviders((name, factory) -> entries.add(new RecipeProviderEntry(name, factory)));
    synchronized (TiCDynamicRecipeGenerator.class) {
      entries.addAll(ADDITIONAL_PROVIDER_ENTRIES);
    }
    return List.copyOf(entries);
  }

  static List<Function<PackOutput, ? extends DataProvider>> createProviders() {
    List<Function<PackOutput, ? extends DataProvider>> providers = new ArrayList<>();
    for (RecipeProviderEntry entry : createProviderEntries()) {
      providers.add(entry.factory());
    }
    return providers;
  }

  @FunctionalInterface
  interface RecipeRunner {
    void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers);
  }

  record RecipeProviderEntry(String name, Function<PackOutput, ? extends DataProvider> factory) {}
}
