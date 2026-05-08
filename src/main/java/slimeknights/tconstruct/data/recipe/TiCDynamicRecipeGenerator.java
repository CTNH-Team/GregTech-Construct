package slimeknights.tconstruct.data.recipe;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.gadgets.data.GadgetRecipeProvider;
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
import java.util.function.Function;

public final class TiCDynamicRecipeGenerator {
  private TiCDynamicRecipeGenerator() {}

  public static void register() {
    register(DynamicRecipeProviderRunner::run);
  }

  static void register(RecipeRunner runner) {
    runner.run("tconstruct-recipes", createProviders());
  }

  static List<RecipeProviderEntry> createProviderEntries() {
    return List.of(
      new RecipeProviderEntry("CommonRecipeProvider", CommonRecipeProvider::new),
      new RecipeProviderEntry("TableRecipeProvider", TableRecipeProvider::new),
      new RecipeProviderEntry("GadgetRecipeProvider", GadgetRecipeProvider::new),
      new RecipeProviderEntry("WorldRecipeProvider", WorldRecipeProvider::new),
      new RecipeProviderEntry("MaterialRecipeProvider", MaterialRecipeProvider::new),
      new RecipeProviderEntry("ToolsRecipeProvider", ToolsRecipeProvider::new),
      new RecipeProviderEntry("SmelteryRecipeProvider", SmelteryRecipeProvider::new),
      new RecipeProviderEntry("ModifierRecipeProvider", ModifierRecipeProvider::new)
    );
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
