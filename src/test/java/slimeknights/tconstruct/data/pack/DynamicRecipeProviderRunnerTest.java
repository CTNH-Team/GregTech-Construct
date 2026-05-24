package slimeknights.tconstruct.data.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicRecipeProviderRunnerTest extends BaseMcTest {
  private final TiCDynamicDataPack pack = new TiCDynamicDataPack("test");

  @BeforeEach
  void clearDynamicPack() {
    TiCDynamicDataPack.clearServer();
  }

  @Test
  void capturedRecipeAndAdvancementAreStoredWithFilters() {
    DynamicRecipeProviderRunner.run("test", List.of(output -> new TestRecipeProvider(output)));

    ResourceLocation recipeLocation = new ResourceLocation("example", "recipes/generated.json");
    ResourceLocation advancementLocation = new ResourceLocation("example", "advancements/recipes/generated.json");

    assertThat(pack.getResource(PackType.SERVER_DATA, recipeLocation)).isNotNull();
    assertThat(pack.getResource(PackType.SERVER_DATA, advancementLocation)).isNotNull();

    JsonObject filter = pack.getMetadataSection(TestFilterSerializer.INSTANCE);
    assertThat(filter).isNotNull();
    JsonArray block = filter.getAsJsonArray("block");
    assertThat(block)
      .extracting(element -> element.getAsJsonObject().get("path").getAsString())
      .contains(
        "^recipes/generated\\.json$",
        "^advancements/recipes/generated\\.json$"
      );
  }

  private static final class TestRecipeProvider implements DataProvider {
    private final PackOutput.PathProvider recipePath;
    private final PackOutput.PathProvider advancementPath;

    private TestRecipeProvider(PackOutput output) {
      this.recipePath = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
      this.advancementPath = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancements/recipes");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
      JsonObject recipe = new JsonObject();
      recipe.addProperty("type", "test");
      byte[] recipeBytes = recipe.toString().getBytes();
      ResourceLocation recipeId = new ResourceLocation("example", "generated");
      try {
        output.writeIfNeeded(recipePath.json(recipeId), recipeBytes, com.google.common.hash.Hashing.sha1().hashBytes(recipeBytes));
      } catch (IOException exception) {
        throw new RuntimeException(exception);
      }

      JsonObject advancement = new JsonObject();
      advancement.addProperty("criteria", "test");
      byte[] advancementBytes = advancement.toString().getBytes();
      try {
        output.writeIfNeeded(advancementPath.json(new ResourceLocation("example", "generated")), advancementBytes, com.google.common.hash.Hashing.sha1().hashBytes(advancementBytes));
      } catch (IOException exception) {
        throw new RuntimeException(exception);
      }
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
      return "Test Dynamic Recipe Provider";
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
