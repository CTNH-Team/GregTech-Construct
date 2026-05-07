package slimeknights.tconstruct.data.recipe;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.DifferenceIngredient;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipeBuilder;
import slimeknights.mantle.recipe.data.ItemNameIngredient;
import slimeknights.mantle.recipe.data.ItemNameOutput;
import slimeknights.mantle.recipe.helper.SimpleFinishedRecipe;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.data.recipe.CraftingNBTWrapper;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.recipe.material.MaterialsConsumerBuilder;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.recipe.TinkerStationPartSwappingBuilder;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.function.Consumer;
public class TableRecipeGenerator implements slimeknights.mantle.recipe.data.ICommonRecipeHelper {
  public static void register(Consumer<FinishedRecipe> consumer) {
    register(consumer, TinkerTables.pattern);
  }

  static void register(Consumer<FinishedRecipe> consumer, ItemObject<? extends ItemLike> pattern) {
    new TableRecipeGenerator().addTableRecipes(consumer, pattern);
  }

  static void registerPattern(Consumer<FinishedRecipe> consumer, ItemLike pattern) {
    new TableRecipeGenerator().addPatternRecipe(consumer, pattern);
  }

  @Override
  public String getModId() {
    return TConstruct.MOD_ID;
  }

  private static InventoryChangeTrigger.TriggerInstance has(ItemLike... items) {
    return InventoryChangeTrigger.TriggerInstance.hasItems(items);
  }

  private static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tag) {
    return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build());
  }

  private void addTableRecipes(Consumer<FinishedRecipe> consumer, ItemObject<? extends ItemLike> pattern) {
    addPatternRecipe(consumer, pattern);

    String folder = "tables/";
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BOOK)
      .requires(Items.PAPER)
      .requires(Items.PAPER)
      .requires(Items.PAPER)
      .requires(Tags.Items.SLIMEBALLS)
      .requires(pattern)
      .requires(pattern)
      .unlockedBy("has_item", has(pattern))
      .save(consumer, location(folder + "book_substitute"));

    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.craftingStation)
      .define('p', pattern)
      .define('w', DifferenceIngredient.of(Ingredient.of(TinkerTags.Items.WORKBENCHES), Ingredient.of(TinkerTables.craftingStation.get())))
      .pattern("p")
      .pattern("w")
      .unlockedBy("has_item", has(pattern))
      .save(consumer, prefix(TinkerTables.craftingStation, folder));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.craftingStation)
          .define('p', pattern)
          .define('w', ItemTags.LOGS)
          .pattern("p")
          .pattern("w")
          .unlockedBy("has_item", has(pattern)))
      .setSource('w')
      .build(consumer, wrap(TinkerTables.craftingStation, folder, "_from_logs"));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.craftingStation)
          .define('p', pattern)
          .define('w', DifferenceIngredient.of(Ingredient.of(TinkerTags.Items.TABLES), Ingredient.of(TinkerTables.craftingStation.get())))
          .pattern("p")
          .pattern("w")
          .unlockedBy("has_item", has(pattern)))
      .setSource('w')
      .build(consumer, wrap(TinkerTables.craftingStation, folder, "_from_tables"));

    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.partBuilder)
          .define('p', pattern)
          .define('w', TinkerTags.Items.PLANKLIKE)
          .pattern("pp")
          .pattern("ww")
          .unlockedBy("has_item", has(pattern)))
      .setSource('w')
      .setMatchAll()
      .build(consumer, prefix(TinkerTables.partBuilder, folder));

    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.tinkerStation)
          .define('p', pattern)
          .define('w', TinkerTags.Items.PLANKLIKE)
          .pattern("ppp")
          .pattern("w w")
          .pattern("w w")
          .unlockedBy("has_item", has(pattern)))
      .setSource('w')
      .setMatchAll()
      .build(consumer, prefix(TinkerTables.tinkerStation, folder));

    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.partChest)
      .define('p', pattern)
      .define('w', ItemTags.PLANKS)
      .define('s', Tags.Items.RODS_WOODEN)
      .define('C', Tags.Items.CHESTS_WOODEN)
      .pattern(" p ")
      .pattern("sCs")
      .pattern("sws")
      .unlockedBy("has_item", has(pattern))
      .save(consumer, prefix(TinkerTables.partChest, folder));
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.tinkersChest)
      .define('p', pattern)
      .define('w', ItemTags.PLANKS)
      .define('l', Tags.Items.GEMS_LAPIS)
      .define('C', Tags.Items.CHESTS_WOODEN)
      .pattern(" p ")
      .pattern("lCl")
      .pattern("lwl")
      .unlockedBy("has_item", has(pattern))
      .save(consumer, prefix(TinkerTables.tinkersChest, folder));
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.castChest)
      .define('c', TinkerTags.Items.GOLD_CASTS)
      .define('b', TinkerSmeltery.searedBrick)
      .define('B', TinkerSmeltery.searedBricks)
      .define('C', Tags.Items.CHESTS_WOODEN)
      .pattern(" c ")
      .pattern("bCb")
      .pattern("bBb")
      .unlockedBy("has_item", has(TinkerTags.Items.GOLD_CASTS))
      .save(consumer, prefix(TinkerTables.castChest, folder));

    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.modifierWorktable)
          .define('r', TinkerTags.Items.WORKSTATION_ROCK)
          .define('s', TinkerTags.Items.SEARED_BLOCKS)
          .pattern("sss")
          .pattern("r r")
          .pattern("r r")
          .unlockedBy("has_item", has(TinkerTags.Items.SEARED_BLOCKS)))
      .setSource('r')
      .setMatchAll()
      .build(consumer, prefix(TinkerTables.modifierWorktable, folder));

    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.tinkersAnvil)
          .define('m', TinkerTags.Items.ANVIL_METAL)
          .define('s', TinkerTags.Items.SEARED_BLOCKS)
          .pattern("mmm")
          .pattern(" s ")
          .pattern("sss")
          .unlockedBy("has_item", has(TinkerTags.Items.ANVIL_METAL)))
      .setSource('m')
      .setMatchAll()
      .build(consumer, prefix(TinkerTables.tinkersAnvil, folder));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.scorchedAnvil)
          .define('m', TinkerTags.Items.ANVIL_METAL)
          .define('s', TinkerTags.Items.SCORCHED_BLOCKS)
          .pattern("mmm")
          .pattern(" s ")
          .pattern("sss")
          .unlockedBy("has_item", has(TinkerTags.Items.ANVIL_METAL)))
      .setSource('m')
      .setMatchAll()
      .build(consumer, prefix(TinkerTables.scorchedAnvil, folder));

    Consumer<FinishedRecipe> toolForge;
    {
      CompoundTag nbt = new CompoundTag();
      CompoundTag display = new CompoundTag();
      display.putString("Name", Serializer.toJson(Component.translatable("block.tconstruct.tool_forge")));
      nbt.put("display", display);
      toolForge = CraftingNBTWrapper.wrap(consumer, nbt);
    }
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.tinkersAnvil)
          .define('m', TinkerTags.Items.ANVIL_METAL)
          .define('s', TinkerTags.Items.SEARED_BLOCKS)
          .define('t', TinkerTables.tinkerStation)
          .pattern("sss")
          .pattern("mtm")
          .pattern("m m")
          .unlockedBy("has_item", has(TinkerTags.Items.ANVIL_METAL)))
      .setSource('m')
      .setMatchAll()
      .build(toolForge, location(folder + "tinkers_forge"));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.scorchedAnvil)
          .define('m', TinkerTags.Items.ANVIL_METAL)
          .define('s', TinkerTags.Items.SCORCHED_BLOCKS)
          .define('t', TinkerTables.tinkerStation)
          .pattern("sss")
          .pattern("mtm")
          .pattern("m m")
          .unlockedBy("has_item", has(TinkerTags.Items.ANVIL_METAL)))
      .setSource('m')
      .setMatchAll()
      .build(toolForge, location(folder + "scorched_forge"));

    Consumer<FinishedRecipe> materialConsumer = MaterialsConsumerBuilder.shaped("m").build(consumer);
    Ingredient fakeStorageBlock = slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient.of(
      TinkerToolParts.fakeStorageBlock, MaterialPredicate.tag(TinkerTags.Materials.COMPATABILITY_ALLOYS));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.tinkersAnvil)
      .define('m', fakeStorageBlock)
      .define('s', TinkerTags.Items.SEARED_BLOCKS)
      .pattern("mmm")
      .pattern(" s ")
      .pattern("sss")
      .unlockedBy("has_item", has(TinkerToolParts.fakeStorageBlock))
      .save(materialConsumer, wrap(TinkerTables.tinkersAnvil, folder, "_material"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.scorchedAnvil)
      .define('m', fakeStorageBlock)
      .define('s', TinkerTags.Items.SCORCHED_BLOCKS)
      .pattern("mmm")
      .pattern(" s ")
      .pattern("sss")
      .unlockedBy("has_item", has(TinkerToolParts.fakeStorageBlock))
      .save(materialConsumer, wrap(TinkerTables.scorchedAnvil, folder, "_material"));
    materialConsumer = MaterialsConsumerBuilder.shaped("m").build(toolForge);
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.tinkersAnvil)
      .define('m', fakeStorageBlock)
      .define('s', TinkerTags.Items.SEARED_BLOCKS)
      .define('t', TinkerTables.tinkerStation)
      .pattern("sss")
      .pattern("mtm")
      .pattern("m m")
      .unlockedBy("has_item", has(TinkerToolParts.fakeStorageBlock))
      .save(materialConsumer, location(folder + "seared_forge_material"));
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerTables.scorchedAnvil)
      .define('m', fakeStorageBlock)
      .define('s', TinkerTags.Items.SCORCHED_BLOCKS)
      .define('t', TinkerTables.tinkerStation)
      .pattern("sss")
      .pattern("mtm")
      .pattern("m m")
      .unlockedBy("has_item", has(TinkerToolParts.fakeStorageBlock))
      .save(materialConsumer, location(folder + "scorched_forge_material"));

    TinkerStationPartSwappingBuilder.tools(DifferenceIngredient.of(Ingredient.of(TinkerTags.Items.MULTIPART_TOOL), Ingredient.of(TinkerTags.Items.UNSWAPPABLE)))
      .save(consumer, location(folder + "tinker_station_part_swapping"));
    TinkerStationPartSwappingBuilder.tools(Ingredient.of(TinkerTools.arrow.get(), TinkerTools.shuriken.get()))
      .maxStackSize(4)
      .save(consumer, location(folder + "ammo_part_swapping"));
    TinkerStationPartSwappingBuilder.tools(Ingredient.of(TinkerTools.throwingAxe.get()))
      .maxStackSize(2)
      .save(consumer, location(folder + "throwing_axe_part_swapping"));

    consumer.accept(new SimpleFinishedRecipe(location(folder + "tinker_station_repair"), TinkerTables.tinkerStationRepairSerializer.get()));
    consumer.accept(new SimpleFinishedRecipe(location(folder + "crafting_table_repair"), TinkerTables.craftingTableRepairSerializer.get()));
  }

  private void addPatternRecipe(Consumer<FinishedRecipe> consumer, ItemLike pattern) {
    String folder = "tables/";
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, pattern, 6)
      .define('s', Tags.Items.RODS_WOODEN)
      .define('p', ItemTags.PLANKS)
      .pattern("ps")
      .pattern("sp")
      .unlockedBy("has_item", has(Tags.Items.RODS_WOODEN))
      .save(consumer, location(folder + "pattern"));
  }
}
