package slimeknights.tconstruct.data.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicDataPackTest extends BaseMcTest {
  private final TiCDynamicDataPack pack = new TiCDynamicDataPack("test");

  @BeforeEach
  void clearDynamicPack() {
    TiCDynamicDataPack.clearServer();
  }

  @Test
  void addDataStoresBytesInMemory() {
    TiCDynamicDataPack.addData(new ResourceLocation("example", "custom/generated.json"), "{}".getBytes());

    assertThat(pack.getResource(PackType.SERVER_DATA, new ResourceLocation("example", "custom/generated.json"))).isNotNull();
    assertThat(pack.getNamespaces(PackType.SERVER_DATA)).contains("example");
  }

  @Test
  void listResourcesFindsRootPathEntries() {
    TiCDynamicDataPack.addData(new ResourceLocation("example", "custom/generated.json"), "{}".getBytes());

    List<ResourceLocation> resources = new ArrayList<>();
    pack.listResources(PackType.SERVER_DATA, "example", "", (location, supplier) -> resources.add(location));

    assertThat(resources).containsExactly(new ResourceLocation("example", "custom/generated.json"));
  }

  @Test
  void clearServerResetsDynamicNamespaces() {
    TiCDynamicDataPack.addData(new ResourceLocation("example", "custom/generated.json"), "{}".getBytes());

    TiCDynamicDataPack.clearServer();

    assertThat(pack.getNamespaces(PackType.SERVER_DATA))
      .containsExactlyInAnyOrder("tconstruct", "minecraft", "forge", "c");
  }

  @Test
  void filterMetadataBlocksExplicitServerDataResources() {
    TiCDynamicDataPack.addFilter(new ResourceLocation("tconstruct", "advancements/recipes/common/materials/cobalt_ingot_from_block.json"));

    JsonObject filter = pack.getMetadataSection(TestFilterSerializer.INSTANCE);

    assertThat(filter).isNotNull();
    JsonArray block = filter.getAsJsonArray("block");
    assertThat(block).hasSize(1);
    JsonObject entry = block.get(0).getAsJsonObject();
    assertThat(entry.get("namespace").getAsString()).isEqualTo("^tconstruct$");
    assertThat(entry.get("path").getAsString()).isEqualTo("^advancements/recipes/common/materials/cobalt_ingot_from_block\\.json$");
  }

  @Test
  void filterMetadataBlocksLegacyRecipeFilters() {
    TiCDynamicDataPack.RECIPE_FILTERS.add(new ResourceLocation("tconstruct", "common/materials/cobalt_ingot_from_block"));

    JsonObject filter = pack.getMetadataSection(TestFilterSerializer.INSTANCE);

    assertThat(filter).isNotNull();
    JsonArray block = filter.getAsJsonArray("block");
    assertThat(block).hasSize(1);
    JsonObject entry = block.get(0).getAsJsonObject();
    assertThat(entry.get("namespace").getAsString()).isEqualTo("^tconstruct$");
    assertThat(entry.get("path").getAsString()).isEqualTo("^recipes/common/materials/cobalt_ingot_from_block\\.json$");
  }

  @Test
  void filterMetadataDoesNotDuplicateRecipeFiltersAddedThroughHelper() {
    TiCDynamicDataPack.addRecipeFilter(new ResourceLocation("tconstruct", "common/materials/cobalt_ingot_from_block"));

    JsonObject filter = pack.getMetadataSection(TestFilterSerializer.INSTANCE);

    assertThat(filter).isNotNull();
    JsonArray block = filter.getAsJsonArray("block");
    assertThat(block).hasSize(1);
    JsonObject entry = block.get(0).getAsJsonObject();
    assertThat(entry.get("namespace").getAsString()).isEqualTo("^tconstruct$");
    assertThat(entry.get("path").getAsString()).isEqualTo("^recipes/common/materials/cobalt_ingot_from_block\\.json$");
  }

  @Test
  void addRecipeStoresRecipeAdvancementAndFilters() {
    TiCDynamicDataPack.addRecipe(new TestFinishedRecipe());

    assertThat(pack.getResource(PackType.SERVER_DATA, new ResourceLocation("tconstruct", "recipes/common/materials/cobalt_ingot_from_block.json"))).isNotNull();
    assertThat(pack.getResource(PackType.SERVER_DATA, new ResourceLocation("tconstruct", "advancements/recipes/common/materials/cobalt_ingot_from_block.json"))).isNotNull();

    JsonObject filter = pack.getMetadataSection(TestFilterSerializer.INSTANCE);

    assertThat(filter).isNotNull();
    JsonArray block = filter.getAsJsonArray("block");
    assertThat(block)
      .extracting(element -> element.getAsJsonObject().get("path").getAsString())
      .containsExactlyInAnyOrder(
        "^recipes/common/materials/cobalt_ingot_from_block\\.json$",
        "^advancements/recipes/common/materials/cobalt_ingot_from_block\\.json$"
      );
  }

  private static class TestFinishedRecipe implements FinishedRecipe {
    @Override
    public void serializeRecipeData(JsonObject json) {}

    @Override
    public ResourceLocation getId() {
      return new ResourceLocation("tconstruct", "common/materials/cobalt_ingot_from_block");
    }

    @Override
    public RecipeSerializer<?> getType() {
      return BuiltInRegistries.RECIPE_SERIALIZER.get(new ResourceLocation("minecraft", "crafting_shapeless"));
    }

    @Override
    public JsonObject serializeAdvancement() {
      return new JsonObject();
    }

    @Override
    public ResourceLocation getAdvancementId() {
      return new ResourceLocation("tconstruct", "recipes/common/materials/cobalt_ingot_from_block");
    }
  }

  private enum TestFilterSerializer implements MetadataSectionSerializer<JsonObject> {
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
