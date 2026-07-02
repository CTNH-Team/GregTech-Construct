package slimeknights.tconstruct.data.recipe;

import lombok.extern.log4j.Log4j2;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.data.pack.DynamicPackOutput;
import slimeknights.tconstruct.data.pack.TiCDynamicDataRegistrar;
import slimeknights.tconstruct.gadgets.data.GadgetRecipeProvider;
import slimeknights.tconstruct.library.addon.DynamicDataRegistrar;
import slimeknights.tconstruct.library.addon.DynamicPackProviderFactory;
import slimeknights.tconstruct.library.addon.DynamicRecipeProviderRegistrar;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;
import slimeknights.tconstruct.shared.data.CommonRecipeProvider;
import slimeknights.tconstruct.smeltery.data.SmelteryRecipeProvider;
import slimeknights.tconstruct.tables.data.TableRecipeProvider;
import slimeknights.tconstruct.tools.data.ModifierRecipeProvider;
import slimeknights.tconstruct.tools.data.ToolsRecipeProvider;
import slimeknights.tconstruct.tools.data.material.MaterialRecipeProvider;
import slimeknights.tconstruct.world.data.WorldRecipeProvider;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

@Log4j2
public final class TiCDynamicRecipeGenerator {
  static final List<RecipeProviderEntry> ADDITIONAL_PROVIDER_ENTRIES = new ArrayList<>();

  private TiCDynamicRecipeGenerator() {}

  public static void register() {
    register(TiCDynamicDataRegistrar.INSTANCE);
  }

  /** Adds an extra runtime recipe provider for TiC's dynamic recipe pack. */
  public static synchronized void addProvider(String name, Consumer<DynamicDataRegistrar> writer) {
    ADDITIONAL_PROVIDER_ENTRIES.add(new RecipeProviderEntry(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(writer, "writer")
    ));
  }

  static void register(DynamicDataRegistrar registrar) {
    int completed = 0;
    long startTime = System.nanoTime();
    for (RecipeProviderEntry entry : createProviderEntries()) {
      try {
        entry.writer().accept(registrar);
        completed++;
      } catch (Exception exception) {
        log.error("Failed to run dynamic recipe writer '{}' for tconstruct-recipes", entry.name(), exception);
        throw new IllegalStateException("Failed to run dynamic recipe writer '" + entry.name() + "' for tconstruct-recipes", exception);
      }
    }
    log.info("Generated dynamic recipes from {} writers in {} ms", completed, (System.nanoTime() - startTime) / 1000000f);
  }

  public static void registerDefaultProviders(DynamicRecipeProviderRegistrar registrar) {
    registrar.addRecipeProvider(CommonRecipeProvider.class);
    registrar.addRecipeProvider(TableRecipeProvider.class);
    registrar.addRecipeProvider(GadgetRecipeProvider.class);
    registrar.addRecipeProvider(WorldRecipeProvider.class);
    registrar.addRecipeProvider(MaterialRecipeProvider.class);
    registrar.addRecipeProvider(ToolsRecipeProvider.class);
    registrar.addRecipeProvider(SmelteryRecipeProvider.class);
    registrar.addRecipeProvider(ModifierRecipeProvider.class);
  }

  static List<RecipeProviderEntry> createProviderEntries() {
    List<RecipeProviderEntry> entries = new ArrayList<>();
    TiCAddonRegistry.collectRecipeProviders((name, writer) -> entries.add(new RecipeProviderEntry(name, writer)));
    synchronized (TiCDynamicRecipeGenerator.class) {
      entries.addAll(ADDITIONAL_PROVIDER_ENTRIES);
    }
    return List.copyOf(entries);
  }

  @FunctionalInterface
  public interface RecipeProviderFactory extends DynamicPackProviderFactory<BaseRecipeProvider> {}

  public static Consumer<DynamicDataRegistrar> recipeWriter(RecipeProviderFactory factory) {
    return registrar -> factory.create(DynamicPackOutput.dummy()).buildRecipesDirect(registrar::addRecipe);
  }

  record RecipeProviderEntry(String name, Consumer<DynamicDataRegistrar> writer) {}
}
