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
import org.mockito.Mockito;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.data.pack.TiCDynamicDataRegistrar;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    TiCDynamicRecipeGenerator.recipeWriter(output -> testRecipeProvider()).accept(TiCDynamicDataRegistrar.INSTANCE);

    ResourceLocation recipeLocation = new ResourceLocation("example", "recipes/generated.json");
    ResourceLocation advancementLocation = new ResourceLocation("example", "advancements/recipes/generated.json");

    assertThat(pack.getResource(PackType.SERVER_DATA, recipeLocation)).isNotNull();
    assertThat(pack.getResource(PackType.SERVER_DATA, advancementLocation)).isNotNull();
    assertThat(readString(recipeLocation)).contains("\"type\":\"minecraft:crafting_shapeless\"");
  }

  @Test
  void recipeWriterAddsRecipeAndAdvancementFilters() {
    TiCDynamicRecipeGenerator.recipeWriter(output -> testRecipeProvider()).accept(TiCDynamicDataRegistrar.INSTANCE);

    JsonObject filter = pack.getMetadataSection(TestFilterSerializer.INSTANCE);

    assertThat(filter).isNotNull();
    assertThat(filter.getAsJsonArray("block"))
      .extracting(element -> element.getAsJsonObject().get("path").getAsString())
      .containsExactlyInAnyOrder("^recipes/generated\\.json$", "^advancements/recipes/generated\\.json$");
  }

  @Test
  void modifierRecipeProviderWiresTcaeDefenseReinforcements() throws Exception {
    String provider = java.nio.file.Files.readString(java.nio.file.Path.of(
      "src/main/java/slimeknights/tconstruct/tools/data/ModifierRecipeProvider.java"));
    String modifiers = java.nio.file.Files.readString(java.nio.file.Path.of(
      "src/main/java/slimeknights/tconstruct/tools/TinkerModifiers.java"));
    String models = java.nio.file.Files.readString(java.nio.file.Path.of(
      "src/main/java/slimeknights/tconstruct/common/data/model/TinkerItemModelProvider.java"));

    assertThat(provider)
      .contains("ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.manyullynReinforcement)", "TinkerFluids.moltenManyullyn.ingredient(FluidValues.INGOT)", ".setCoolingTime(102)")
      .contains("ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.hepatizonReinforcement)", "TinkerFluids.moltenHepatizon.ingredient(FluidValues.INGOT)", ".setCoolingTime(75)")
      .contains("ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.queensSlimeReinforcement)", "TinkerFluids.moltenQueensSlime.ingredient(FluidValues.INGOT)", ".setCoolingTime(88)")
      .contains("ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.netheriteReinforcement)", "TinkerFluids.moltenNetherite.ingredient(FluidValues.INGOT)", ".setCoolingTime(121)")
      .contains(".setCast(TinkerCommons.obsidianPane, true)")
      .contains("IncrementalModifierRecipeBuilder.modifier(ModifierIds.meleeDefense)", ".setInput(TinkerModifiers.manyullynReinforcement, 1, 5)")
      .contains("IncrementalModifierRecipeBuilder.modifier(ModifierIds.projectileDefense)", ".setInput(TinkerModifiers.hepatizonReinforcement, 1, 5)")
      .contains("IncrementalModifierRecipeBuilder.modifier(ModifierIds.blastDefense)", ".setInput(TinkerModifiers.queensSlimeReinforcement, 1, 5)")
      .contains("ModifierRecipeBuilder.modifier(ModifierIds.physicsDefense)", ".addInput(TinkerModifiers.netheriteReinforcement, 5)");
    assertThat(modifiers)
      .contains("ITEMS.register(\"manyullyn_reinforcement\", ITEM_PROPS)", "ITEMS.register(\"hepatizon_reinforcement\", ITEM_PROPS)")
      .contains("ITEMS.register(\"queens_slime_reinforcement\", ITEM_PROPS)", "ITEMS.register(\"netherite_reinforcement\", ITEM_PROPS)");
    assertThat(models)
      .contains("generated(TinkerModifiers.manyullynReinforcement, \"item/reinforcement/manyullyn\")")
      .contains("generated(TinkerModifiers.hepatizonReinforcement, \"item/reinforcement/hepatizon\")")
      .contains("generated(TinkerModifiers.queensSlimeReinforcement, \"item/reinforcement/queens_slime\")")
      .contains("generated(TinkerModifiers.netheriteReinforcement, \"item/reinforcement/netherite\")");
  }

  @Test
  void productionToolsRecipeProviderUsesSlotSpecificPlateLayouts() throws Exception {
    String provider = java.nio.file.Files.readString(java.nio.file.Path.of(
      "src/main/java/slimeknights/tconstruct/tools/data/ToolsRecipeProvider.java"));

    assertThat(provider)
      .contains("TinkerTools.plateArmor.forEach((type, item) -> toolBuilding(consumer, item, plateFolder, plateArmorLayout(type)))")
      .contains("return isSmallArmor(type) ? Patterns.PLATE_ARMOR_SMALL : Patterns.PLATE_ARMOR_LARGE")
      .doesNotContain("TinkerTools.plateArmor.forEach(item -> toolBuilding(consumer, item, plateFolder, Patterns.PLATE_ARMOR))");
  }

  private String readString(ResourceLocation location) throws Exception {
    IoSupplier<java.io.InputStream> resource = pack.getResource(PackType.SERVER_DATA, location);
    assertThat(resource).isNotNull();
    try (java.io.InputStream input = resource.get()) {
      return new String(input.readAllBytes());
    }
  }

  private BaseRecipeProvider testRecipeProvider() {
    BaseRecipeProvider provider = Mockito.mock(BaseRecipeProvider.class);
    Mockito.doAnswer(invocation -> {
      Consumer<FinishedRecipe> consumer = invocation.getArgument(0);
      consumer.accept(new TestFinishedRecipe());
      return null;
    }).when(provider).buildRecipesDirect(Mockito.any());
    return provider;
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
