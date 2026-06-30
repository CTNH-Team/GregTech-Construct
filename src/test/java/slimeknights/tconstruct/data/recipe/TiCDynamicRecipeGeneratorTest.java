package slimeknights.tconstruct.data.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.data.pack.TiCDynamicDataRegistrar;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicRecipeGeneratorTest extends BaseMcTest {
  private final TiCDynamicDataPack pack = new TiCDynamicDataPack("test");

  @AfterEach
  void clearExternalProviders() throws ReflectiveOperationException {
    Field field = TiCDynamicRecipeGenerator.class.getDeclaredField("ADDITIONAL_PROVIDER_ENTRIES");
    field.setAccessible(true);
    ((ArrayList<?>) field.get(null)).clear();
  }

  @BeforeEach
  void clearDynamicPack() {
    TiCDynamicDataPack.clearServer();
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
    TiCDynamicRecipeGenerator.addProvider("ExternalRecipeWriter", registrar -> registrar.addData(new ResourceLocation("example", "recipes/external.json"), "{}".getBytes()));

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
        "ExternalRecipeWriter"
      );
  }

  @Test
  void recipeWriterStoresRecipeAndAdvancementDirectlyInMemory() throws Exception {
    TiCDynamicRecipeGenerator.recipeWriter(TestRecipeProvider::new).accept(TiCDynamicDataRegistrar.INSTANCE);

    ResourceLocation recipeLocation = new ResourceLocation("example", "recipes/generated.json");
    ResourceLocation advancementLocation = new ResourceLocation("example", "advancements/recipes/generated.json");

    assertThat(pack.getResource(PackType.SERVER_DATA, recipeLocation)).isNotNull();
    assertThat(pack.getResource(PackType.SERVER_DATA, advancementLocation)).isNotNull();
    assertThat(readString(recipeLocation)).contains("\"type\":\"minecraft:crafting_shapeless\"");
  }

  @Test
  void recipeWriterAddsRecipeAndAdvancementFilters() {
    TiCDynamicRecipeGenerator.recipeWriter(TestRecipeProvider::new).accept(TiCDynamicDataRegistrar.INSTANCE);

    JsonObject filter = pack.getMetadataSection(TestFilterSerializer.INSTANCE);

    assertThat(filter).isNotNull();
    assertThat(filter.getAsJsonArray("block"))
      .extracting(element -> element.getAsJsonObject().get("path").getAsString())
      .containsExactlyInAnyOrder("^recipes/generated\\.json$", "^advancements/recipes/generated\\.json$");
  }

  private String readString(ResourceLocation location) throws Exception {
    IoSupplier<java.io.InputStream> resource = pack.getResource(PackType.SERVER_DATA, location);
    assertThat(resource).isNotNull();
    try (java.io.InputStream input = resource.get()) {
      return new String(input.readAllBytes());
    }
  }

  private static final class TestRecipeProvider extends slimeknights.tconstruct.common.data.BaseRecipeProvider {
    private TestRecipeProvider(net.minecraft.data.PackOutput output) {
      super(output);
    }

    @Override
    protected void buildRecipes(java.util.function.Consumer<FinishedRecipe> consumer) {
      consumer.accept(new TestFinishedRecipe());
    }

    @Override
    public String getName() {
      return "Test Recipe Provider";
    }
  }

  private static final class TestFinishedRecipe implements FinishedRecipe {
    @Override
    public void serializeRecipeData(JsonObject json) {}

    @Override
    public ResourceLocation getId() {
      return new ResourceLocation("example", "generated");
    }

    @Override
    public RecipeSerializer<?> getType() {
      return BuiltInRegistries.RECIPE_SERIALIZER.get(new ResourceLocation("minecraft", "crafting_shapeless"));
    }

    @Override
    public JsonObject serializeAdvancement() {
      JsonObject json = new JsonObject();
      json.add("criteria", new JsonObject());
      return json;
    }

    @Override
    public ResourceLocation getAdvancementId() {
      return new ResourceLocation("example", "recipes/generated");
    }
  }

  private enum TestFilterSerializer implements net.minecraft.server.packs.metadata.MetadataSectionSerializer<JsonObject> {
    INSTANCE;

    @Override
    public String getMetadataSectionName() {
      return "filter";
    }

    @Override
    public JsonObject fromJson(JsonObject json) {
      return json;
    }
  }
}
