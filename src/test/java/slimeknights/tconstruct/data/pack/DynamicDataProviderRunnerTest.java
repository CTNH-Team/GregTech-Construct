package slimeknights.tconstruct.data.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DynamicDataProviderRunnerTest extends BaseMcTest {
    private final TiCDynamicDataPack pack = new TiCDynamicDataPack("test");

    @BeforeEach
    void clearDynamicPack() {
        TiCDynamicDataPack.clearServer();
    }

    @Test
    void generatedServerDataIsCaptured() {
        DynamicDataProviderRunner.run("test", output -> new TestTagProvider(output));

        ResourceLocation location = new ResourceLocation("example", "tags/items/generated.json");
        IoSupplier<InputStream> resource = pack.getResource(PackType.SERVER_DATA, location);

        assertThat(resource).isNotNull();
        assertThat(pack.getNamespaces(PackType.SERVER_DATA)).contains("example");
    }

    @Test
    void listResourcesFindsRootPathEntries() {
        DynamicDataProviderRunner.run("test", output -> new TestTagProvider(output));

        List<ResourceLocation> resources = new ArrayList<>();
        pack.listResources(PackType.SERVER_DATA, "example", "", (location, supplier) -> resources.add(location));

        assertThat(resources).containsExactly(new ResourceLocation("example", "tags/items/generated.json"));
    }

    @Test
    void clearServerResetsDynamicNamespaces() {
        DynamicDataProviderRunner.run("test", output -> new TestTagProvider(output));

        TiCDynamicDataPack.clearServer();

        assertThat(pack.getNamespaces(PackType.SERVER_DATA))
            .containsExactlyInAnyOrder("tconstruct", "minecraft", "forge", "c");
    }

  @Test
  void failingProviderConstructionStopsLaterProviders() {
    assertThatThrownBy(() -> DynamicDataProviderRunner.run(
        "test",
        output -> {
          throw new IllegalStateException("expected provider construction failure");
        },
        output -> new TestTagProvider(output)
    ))
      .isInstanceOf(IllegalStateException.class)
      .hasRootCauseMessage("expected provider construction failure");

    ResourceLocation location = new ResourceLocation("example", "tags/items/generated.json");
    assertThat(pack.getResource(PackType.SERVER_DATA, location)).isNull();
  }

  @Test
  void failingProviderRunStopsLaterProviders() {
    assertThatThrownBy(() -> DynamicDataProviderRunner.run(
        "test",
        output -> new TestTagProvider(output, "first"),
        output -> new FailingRunProvider(),
        output -> new TestTagProvider(output, "later")
    ))
      .isInstanceOf(IllegalStateException.class)
      .hasRootCauseMessage("expected provider run failure");

    ResourceLocation firstLocation = new ResourceLocation("example", "tags/items/first.json");
    ResourceLocation laterLocation = new ResourceLocation("example", "tags/items/later.json");
    assertThat(pack.getResource(PackType.SERVER_DATA, firstLocation)).isNotNull();
    assertThat(pack.getResource(PackType.SERVER_DATA, laterLocation)).isNull();
  }

  @Test
  void nullProviderStopsLaterProviders() {
    assertThatThrownBy(() -> DynamicDataProviderRunner.run(
        "test",
        output -> null,
        output -> new TestTagProvider(output)
    ))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("returned null");

    ResourceLocation location = new ResourceLocation("example", "tags/items/generated.json");
    assertThat(pack.getResource(PackType.SERVER_DATA, location)).isNull();
  }

    @Test
    void malformedServerDataPathIsIgnored() {
        assertThatCode(() -> DynamicDataProviderRunner.run("test", output -> new InvalidPathProvider(output)))
            .doesNotThrowAnyException();

        ResourceLocation location = new ResourceLocation("malformed", "tags/items/generated.json");
        assertThat(pack.getResource(PackType.SERVER_DATA, location)).isNull();
        assertThat(pack.getNamespaces(PackType.SERVER_DATA)).doesNotContain("malformed");
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
    void addRecipeFiltersRecipeAndGeneratedAdvancement() {
        TiCDynamicDataPack.addRecipe(new TestFinishedRecipe());

        JsonObject filter = pack.getMetadataSection(TestFilterSerializer.INSTANCE);

        assertThat(filter).isNotNull();
        JsonArray block = filter.getAsJsonArray("block");
        assertThat(block).hasSize(2);
        assertThat(block)
            .extracting(element -> element.getAsJsonObject().get("namespace").getAsString())
            .containsOnly("^tconstruct$");
        assertThat(block)
            .extracting(element -> element.getAsJsonObject().get("path").getAsString())
            .containsExactlyInAnyOrder(
                "^recipes/common/materials/cobalt_ingot_from_block\\.json$",
                "^advancements/recipes/common/materials/cobalt_ingot_from_block\\.json$"
            );
    }

    private static class TestTagProvider implements DataProvider {
        private final PackOutput.PathProvider paths;
        private final String path;

        private TestTagProvider(PackOutput output) {
            this(output, "generated");
        }

        private TestTagProvider(PackOutput output, String path) {
            this.paths = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/items");
            this.path = path;
        }

        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            JsonObject json = new JsonObject();
            json.addProperty("replace", false);
            json.add("values", new com.google.gson.JsonArray());
            return DataProvider.saveStable(output, json, paths.json(new ResourceLocation("example", path)));
        }

        @Override
        public String getName() {
            return "Test Dynamic Tag Provider";
        }
    }

    private static class FailingRunProvider implements DataProvider {
        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            throw new IllegalStateException("expected provider run failure");
        }

        @Override
        public String getName() {
            return "Failing Run Provider";
        }
    }

    private static class InvalidPathProvider implements DataProvider {
        private final PackOutput.PathProvider paths;

        private InvalidPathProvider(PackOutput output) {
            this.paths = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/items");
        }

        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            JsonObject json = new JsonObject();
            json.addProperty("replace", false);
            json.add("values", new com.google.gson.JsonArray());
            try {
                byte[] bytes = json.toString().getBytes();
                output.writeIfNeeded(
                    paths.json(new ResourceLocation("malformed", "generated")).getParent().resolve("bad path").resolve("generated.json"),
                    bytes,
                    com.google.common.hash.Hashing.sha1().hashBytes(bytes)
                );
            } catch (java.io.IOException exception) {
                throw new RuntimeException(exception);
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public String getName() {
            return "Invalid Path Provider";
        }
    }

    private static class TestFinishedRecipe implements net.minecraft.data.recipes.FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("type", "test");
        }

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
