package slimeknights.tconstruct.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.test.BaseMcTest;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TiCRecipesTest extends BaseMcTest {
  private static final ResourceLocation TABLE_PATTERN_RECIPE = new ResourceLocation("tconstruct", "recipes/tables/pattern.json");
  private static final ResourceLocation TABLE_PATTERN_ADVANCEMENT = new ResourceLocation("tconstruct", "advancements/recipes/misc/tables/pattern.json");
  private static final ResourceLocation PUNJI_RECIPE = new ResourceLocation("tconstruct", "recipes/gadgets/punji.json");
  private static final ResourceLocation PUNJI_ADVANCEMENT = new ResourceLocation("tconstruct", "advancements/recipes/decorations/gadgets/punji.json");
  private static final ResourceLocation GREENHEART_PLANKS_RECIPE = new ResourceLocation("tconstruct", "recipes/world/wood/greenheart/planks.json");
  private static final ResourceLocation GREENHEART_PLANKS_ADVANCEMENT = new ResourceLocation("tconstruct", "advancements/recipes/building_blocks/world/wood/greenheart/planks.json");
  private static final Set<String> MIGRATED_FILTER_PATHS = Set.of(
    "^recipes/common/gold_bars\\.json$",
    "^advancements/recipes/decorations/common/gold_bars\\.json$",
    "^recipes/tables/pattern\\.json$",
    "^advancements/recipes/misc/tables/pattern\\.json$",
    "^recipes/gadgets/punji\\.json$",
    "^advancements/recipes/decorations/gadgets/punji\\.json$",
    "^recipes/world/wood/greenheart/planks\\.json$",
    "^advancements/recipes/building_blocks/world/wood/greenheart/planks\\.json$"
  );
  private final TiCDynamicDataPack pack = new TiCDynamicDataPack("test");

  @BeforeEach
  void clearDynamicPack() {
    TiCDynamicDataPack.clearServer();
  }

  @Test
  void registerRecipesAddsTableRecipeAndRecipeAdvancement() {
    TableRecipeGenerator.registerPattern(TiCDynamicDataPack::addRecipe, Blocks.CRAFTING_TABLE);

    assertResourceExists(TABLE_PATTERN_RECIPE);
    assertResourceExists(TABLE_PATTERN_ADVANCEMENT);
  }

  @Test
  void registerRecipesAddsPunjiGadgetRecipe() {
    GadgetRecipeGenerator.registerPunji(TiCDynamicDataPack::addRecipe, Blocks.BAMBOO);

    assertResourceExists(PUNJI_RECIPE);
    assertResourceExists(PUNJI_ADVANCEMENT);
  }

  @Test
  void registerRecipesAddsWorldGreenheartPlanksRecipe() {
    WorldRecipeGenerator.registerGreenheartPlanksRecipe(TiCDynamicDataPack::addRecipe, Blocks.OAK_PLANKS, ItemTags.LOGS);

    assertResourceExists(GREENHEART_PLANKS_RECIPE);
    assertResourceExists(GREENHEART_PLANKS_ADVANCEMENT);
  }

  @Test
  void registerRecipesRuntimePathCallsWorldGenerator() {
    try (MockedStatic<CommonRecipeGenerator> common = Mockito.mockStatic(CommonRecipeGenerator.class);
         MockedStatic<TableRecipeGenerator> table = Mockito.mockStatic(TableRecipeGenerator.class);
         MockedStatic<GadgetRecipeGenerator> gadget = Mockito.mockStatic(GadgetRecipeGenerator.class);
         MockedStatic<WorldRecipeGenerator> world = Mockito.mockStatic(WorldRecipeGenerator.class);
         MockedStatic<MaterialRecipeGenerator> material = Mockito.mockStatic(MaterialRecipeGenerator.class);
         MockedStatic<ToolsRecipeGenerator> tools = Mockito.mockStatic(ToolsRecipeGenerator.class);
         MockedStatic<SmelteryRecipeGenerator> smeltery = Mockito.mockStatic(SmelteryRecipeGenerator.class);
         MockedStatic<ModifierRecipeGenerator> modifier = Mockito.mockStatic(ModifierRecipeGenerator.class);
         MockedStatic<slimeknights.tconstruct.data.material.MaterialDataGenerator> materialData = Mockito.mockStatic(slimeknights.tconstruct.data.material.MaterialDataGenerator.class)) {
      TiCRecipes.registerRecipes();

      world.verify(() -> WorldRecipeGenerator.register(Mockito.any()));
    }
  }

  @Test
  void registerRecipesFiltersMigratedRecipeAndAdvancementPaths() {
    CommonRecipeGenerator.registerGoldRecipes(TiCDynamicDataPack::addRecipe, Blocks.IRON_BARS);
    TableRecipeGenerator.registerPattern(TiCDynamicDataPack::addRecipe, Blocks.CRAFTING_TABLE);
    GadgetRecipeGenerator.registerPunji(TiCDynamicDataPack::addRecipe, Blocks.BAMBOO);
    WorldRecipeGenerator.registerGreenheartPlanksRecipe(TiCDynamicDataPack::addRecipe, Blocks.OAK_PLANKS, ItemTags.LOGS);

    JsonObject filter = pack.getMetadataSection(TestFilterSerializer.INSTANCE);
    assertThat(filter).isNotNull();

    JsonArray block = filter.getAsJsonArray("block");
    Set<String> paths = new HashSet<>();
    for (int index = 0; index < block.size(); index++) {
      paths.add(block.get(index).getAsJsonObject().get("path").getAsString());
    }

    assertThat(paths).containsExactlyInAnyOrderElementsOf(MIGRATED_FILTER_PATHS);
  }

  private void assertResourceExists(ResourceLocation location) {
    IoSupplier<InputStream> resource = pack.getResource(PackType.SERVER_DATA, location);
    assertThat(resource).as(location.toString()).isNotNull();
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
