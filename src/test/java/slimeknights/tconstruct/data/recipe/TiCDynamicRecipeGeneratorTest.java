package slimeknights.tconstruct.data.recipe;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.library.materials.definition.MaterialManager;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicRecipeGeneratorTest extends BaseMcTest {
  private final TiCDynamicDataPack pack = new TiCDynamicDataPack("test");

  @AfterEach
  void clearExternalProviders() throws ReflectiveOperationException {
    Field field = TiCDynamicRecipeGenerator.class.getDeclaredField("ADDITIONAL_PROVIDER_ENTRIES");
    field.setAccessible(true);
    ((ArrayList<?>) field.get(null)).clear();
  }

  @Test
  void registerUsesEightProviderFactories() {
    RecordingRunner runner = new RecordingRunner();

    TiCDynamicRecipeGenerator.register(runner);

    assertThat(runner.owner).isEqualTo("tconstruct-recipes");
    assertThat(runner.providers).hasSize(8);
  }

  @Test
  void registerKeepsProviderOrder() {
    assertThat(TiCDynamicRecipeGenerator.createProviderEntries()).extracting(TiCDynamicRecipeGenerator.RecipeProviderEntry::name).containsExactly(
      "CommonRecipeProvider",
      "TableRecipeProvider",
      "GadgetRecipeProvider",
      "WorldRecipeProvider",
      "MaterialRecipeProvider",
      "ToolsRecipeProvider",
      "SmelteryRecipeProvider",
      "ModifierRecipeProvider"
    );
  }

  @Test
  void addProviderAppendsExternalProviderEntry() {
    TiCDynamicRecipeGenerator.addProvider("ExternalRecipeProvider", output -> new StubProvider("ExternalRecipeProvider"));

    assertThat(TiCDynamicRecipeGenerator.createProviderEntries())
      .extracting(TiCDynamicRecipeGenerator.RecipeProviderEntry::name)
      .containsExactly(
        "CommonRecipeProvider",
        "TableRecipeProvider",
        "GadgetRecipeProvider",
        "WorldRecipeProvider",
        "MaterialRecipeProvider",
        "ToolsRecipeProvider",
        "SmelteryRecipeProvider",
        "ModifierRecipeProvider",
        "ExternalRecipeProvider"
      );
  }

  @Test
  void registerIncludesExternalProviderFactory() {
    RecordingRunner runner = new RecordingRunner();
    PackOutput output = new PackOutput(Path.of("build", "test-dynamic-recipe-generator"));

    TiCDynamicRecipeGenerator.addProvider("ExternalRecipeProvider", ignored -> new StubProvider("ExternalRecipeProvider"));
    TiCDynamicRecipeGenerator.register(runner);

    assertThat(runner.providers).hasSize(9);
    assertThat(runner.providers)
      .extracting(factory -> factory.apply(output).getName())
      .endsWith("ExternalRecipeProvider");
  }

  @Test
  void registerDoesNotGenerateRuntimeMaterialDefinitions() {
    TiCDynamicDataPack.clearServer();

    TiCDynamicRecipeGenerator.register((owner, providers) -> {
      // Skip recipe providers so the test isolates the recipe registration itself.
    });

    ResourceLocation woodDefinition = new ResourceLocation(TConstruct.MOD_ID, MaterialManager.FOLDER + "/wood.json");
    assertThat(pack.getResource(PackType.SERVER_DATA, woodDefinition)).as("runtime material definitions").isNull();
  }

  private static final class StubProvider implements DataProvider {
    private final String name;

    private StubProvider(String name) {
      this.name = name;
    }

    @Override
    public CompletableFuture<?> run(net.minecraft.data.CachedOutput output) {
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
      return name;
    }
  }

  private static final class RecordingRunner implements TiCDynamicRecipeGenerator.RecipeRunner {
    private String owner;
    private List<Function<PackOutput, ? extends DataProvider>> providers = List.of();

    @Override
    public void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers) {
      this.owner = owner;
      this.providers = providers;
    }
  }
}
