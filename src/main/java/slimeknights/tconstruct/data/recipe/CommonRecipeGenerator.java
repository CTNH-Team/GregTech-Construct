package slimeknights.tconstruct.data.recipe;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraftforge.common.Tags;
import slimeknights.mantle.recipe.data.ConsumerWrapperBuilder;
import slimeknights.mantle.recipe.data.ICommonRecipeHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipeBuilder;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.shared.block.ClearStainedGlassBlock.GlassColor;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.Locale;
import java.util.function.Consumer;

public class CommonRecipeGenerator implements ICommonRecipeHelper {
  public static void register(Consumer<FinishedRecipe> consumer) {
    CommonRecipeGenerator generator = new CommonRecipeGenerator();
    generator.addCommonRecipes(consumer, TinkerCommons.goldBars);
    generator.addMaterialRecipes(consumer);
  }

  @Override
  public String getModId() {
    return TConstruct.MOD_ID;
  }

  static void registerGoldRecipes(Consumer<FinishedRecipe> consumer, ItemLike goldBars) {
    CommonRecipeGenerator generator = new CommonRecipeGenerator();
    generator.addGoldRecipes(consumer, goldBars);
  }

  private static InventoryChangeTrigger.TriggerInstance has(ItemLike... items) {
    return InventoryChangeTrigger.TriggerInstance.hasItems(items);
  }

  private static InventoryChangeTrigger.TriggerInstance has(TagKey<net.minecraft.world.item.Item> tag) {
    return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build());
  }

  private void addGoldRecipes(Consumer<FinishedRecipe> consumer, ItemLike goldBars) {
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, goldBars, 16)
      .define('#', Tags.Items.INGOTS_GOLD)
      .pattern("###")
      .pattern("###")
      .unlockedBy("has_ingot", has(Tags.Items.INGOTS_GOLD))
      .save(consumer, location("common/gold_bars"));
  }

  private void addCommonRecipes(Consumer<FinishedRecipe> consumer, ItemLike goldBars) {
    addGoldRecipes(consumer, goldBars);

    String folder = "common/firewood/";
    slabStairsCrafting(consumer, TinkerMaterials.blazewood, folder, false);
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerMaterials.blazewood.getFence(), 6)
      .pattern("WWW").pattern("WWW")
      .define('W', TinkerMaterials.blazewood)
      .unlockedBy("has_planks", has(TinkerMaterials.blazewood))
      .save(consumer, location(folder + "blazewood_fence"));

    slabStairsCrafting(consumer, TinkerMaterials.nahuatl, folder, false);
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerMaterials.nahuatl.getFence(), 6)
      .pattern("WWW").pattern("WWW")
      .define('W', TinkerMaterials.nahuatl)
      .unlockedBy("has_planks", has(TinkerMaterials.nahuatl))
      .save(consumer, location(folder + "nahuatl_fence"));
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerCommons.goldPlatform, 4)
      .define('#', Tags.Items.INGOTS_GOLD)
      .define('.', Tags.Items.NUGGETS_GOLD)
      .pattern("#.#")
      .pattern(". .")
      .pattern("#.#")
      .unlockedBy("has_gold", has(Tags.Items.INGOTS_GOLD))
      .save(consumer, location("common/gold_platform"));
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerCommons.ironPlatform, 4)
      .define('#', Tags.Items.INGOTS_IRON)
      .define('.', Tags.Items.NUGGETS_IRON)
      .pattern("#.#")
      .pattern(". .")
      .pattern("#.#")
      .unlockedBy("has_bars", has(Tags.Items.INGOTS_IRON))
      .save(consumer, location("common/iron_platform"));
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerCommons.copperPlatform.get(WeatherState.UNAFFECTED), 4)
      .define('#', Tags.Items.INGOTS_COPPER)
      .define('.', TinkerTags.Items.NUGGETS_COPPER)
      .pattern("#.#")
      .pattern(". .")
      .pattern("#.#")
      .unlockedBy("has_bars", has(Tags.Items.INGOTS_COPPER))
      .save(consumer, location("common/copper_platform"));
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerCommons.cobaltPlatform, 4)
      .define('#', TinkerMaterials.cobalt.getIngotTag())
      .define('.', TinkerMaterials.cobalt.getNuggetTag())
      .pattern("#.#")
      .pattern(". .")
      .pattern("#.#")
      .unlockedBy("has_bars", has(TinkerMaterials.cobalt.getIngotTag()))
      .save(consumer, location("common/cobalt_platform"));
    TinkerCommons.waxedCopperPlatform.forEach((age, block) -> {
      Block unwaxed = TinkerCommons.copperPlatform.get(age);
      ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, block)
        .requires(unwaxed)
        .requires(Items.HONEYCOMB)
        .group("tconstruct:wax_copper_platform")
        .unlockedBy("has_block", has(unwaxed))
        .save(consumer, location("common/copper_platform_waxing_" + age.toString().toLowerCase(Locale.ROOT)));
    });

    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TinkerCommons.materialsAndYou)
      .requires(Items.BOOK)
      .requires(TinkerTables.pattern)
      .unlockedBy("has_item", has(TinkerTables.pattern))
      .save(consumer, prefix(TinkerCommons.materialsAndYou, "common/"));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TinkerCommons.tinkersGadgetry)
      .requires(Items.BOOK)
      .requires(SlimeType.SKY.getSlimeballTag())
      .unlockedBy("has_item", has(SlimeType.SKY.getSlimeballTag()))
      .save(consumer, prefix(TinkerCommons.tinkersGadgetry, "common/"));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TinkerCommons.punySmelting)
      .requires(Items.BOOK)
      .requires(TinkerSmeltery.grout)
      .unlockedBy("has_item", has(TinkerSmeltery.grout))
      .save(consumer, prefix(TinkerCommons.punySmelting, "common/"));
    ItemCastingRecipeBuilder.tableRecipe(TinkerCommons.mightySmelting)
      .setFluidAndTime(TinkerFluids.searedStone, FluidValues.BRICK)
      .setCast(Items.BOOK, true)
      .save(consumer, prefix(TinkerCommons.mightySmelting, "common/"));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TinkerCommons.fantasticFoundry)
      .requires(Items.BOOK)
      .requires(TinkerSmeltery.netherGrout)
      .unlockedBy("has_item", has(TinkerSmeltery.netherGrout))
      .save(consumer, prefix(TinkerCommons.fantasticFoundry, "common/"));
    ItemCastingRecipeBuilder.tableRecipe(TinkerCommons.encyclopedia)
      .setFluidAndTime(TinkerFluids.moltenGold, FluidValues.INGOT)
      .setCast(Items.BOOK, true)
      .save(consumer, prefix(TinkerCommons.encyclopedia, "common/"));

    String folderName = "common/glass/";
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerCommons.clearGlassPane, 16)
      .define('#', TinkerCommons.clearGlass)
      .pattern("###")
      .pattern("###")
      .unlockedBy("has_block", has(TinkerCommons.clearGlass))
      .save(consumer, prefix(TinkerCommons.clearGlassPane, folderName));
    for (GlassColor color : GlassColor.values()) {
      Block block = TinkerCommons.clearStainedGlass.get(color);
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block, 8)
        .define('#', TinkerCommons.clearGlass)
        .define('X', color.getDye().getTag())
        .pattern("###")
        .pattern("#X#")
        .pattern("###")
        .group(prefix("stained_clear_glass"))
        .unlockedBy("has_clear_glass", has(TinkerCommons.clearGlass))
        .save(consumer, prefix(id(block), folderName));
      Block pane = TinkerCommons.clearStainedGlassPane.get(color);
      ResourceLocation paneId = id(pane);
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pane, 16)
        .define('#', block)
        .pattern("###")
        .pattern("###")
        .group(prefix("stained_clear_glass_pane"))
        .unlockedBy("has_block", has(block))
        .save(consumer, prefix(paneId, folderName));
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pane, 8)
        .define('#', TinkerCommons.clearGlassPane)
        .define('X', color.getDye().getTag())
        .pattern("###")
        .pattern("#X#")
        .pattern("###")
        .group(prefix("stained_clear_glass_pane"))
        .unlockedBy("has_clear_glass", has(TinkerCommons.clearGlassPane))
        .save(consumer, wrap(paneId, folderName, "_from_panes"));
    }

    String glassVanillaFolder = folderName + "vanilla/";
    Consumer<FinishedRecipe> vanillaGlassConsumer = withCondition(consumer, ConfigEnabledCondition.GLASS_RECIPE_FIX);
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.DAYLIGHT_DETECTOR)
      .define('Q', Items.QUARTZ)
      .define('G', Tags.Items.GLASS_COLORLESS)
      .define('W', ItemTags.WOODEN_SLABS)
      .pattern("GGG")
      .pattern("QQQ")
      .pattern("WWW")
      .unlockedBy("has_quartz", has(Items.QUARTZ))
      .save(vanillaGlassConsumer, prefix(id(Blocks.DAYLIGHT_DETECTOR), glassVanillaFolder));
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Items.END_CRYSTAL)
      .define('T', Items.GHAST_TEAR)
      .define('E', Items.ENDER_EYE)
      .define('G', Tags.Items.GLASS_COLORLESS)
      .pattern("GGG")
      .pattern("GEG")
      .pattern("GTG")
      .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
      .save(vanillaGlassConsumer, prefix(id(Items.END_CRYSTAL), glassVanillaFolder));
    ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, Items.GLASS_BOTTLE, 3)
      .define('#', Tags.Items.GLASS_COLORLESS)
      .pattern("# #")
      .pattern(" # ")
      .unlockedBy("has_glass", has(Tags.Items.GLASS_COLORLESS))
      .save(vanillaGlassConsumer, prefix(id(Items.GLASS_BOTTLE), glassVanillaFolder));

    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.FLINT)
      .requires(Blocks.GRAVEL)
      .requires(Blocks.GRAVEL)
      .requires(Blocks.GRAVEL)
      .unlockedBy("has_item", has(Blocks.GRAVEL))
      .save(
        ConsumerWrapperBuilder.wrap()
          .addCondition(ConfigEnabledCondition.GRAVEL_TO_FLINT)
          .build(consumer),
        location("common/flint"));

    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.BLAST_FURNACE)
      .define('#', Blocks.SMOOTH_BASALT)
      .define('X', Blocks.FURNACE)
      .define('I', Items.IRON_INGOT)
      .pattern("III")
      .pattern("IXI")
      .pattern("###")
      .unlockedBy("has_smooth_stone", has(Blocks.SMOOTH_BASALT))
      .save(consumer, location("common/basalt_blast_furnace"));

    ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, TinkerCommons.cheeseBlock)
      .define('#', TinkerCommons.cheeseIngot)
      .pattern("##").pattern("##")
      .unlockedBy("has_cheese", has(TinkerCommons.cheeseIngot))
      .save(consumer, location("common/cheese_block_from_ingot"));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, TinkerCommons.cheeseIngot, 4)
      .requires(TinkerCommons.cheeseBlock)
      .unlockedBy("has_cheese", has(TinkerCommons.cheeseBlock))
      .save(consumer, location("common/cheese_ingot_from_block"));
  }

  private void addMaterialRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "common/materials/";

    metalCrafting(consumer, TinkerMaterials.cobalt, folder);
    metalCrafting(consumer, TinkerMaterials.steel, folder);
    metalCrafting(consumer, TinkerMaterials.slimesteel, folder);
    metalCrafting(consumer, TinkerMaterials.amethystBronze, folder);
    metalCrafting(consumer, TinkerMaterials.roseGold, folder);
    metalCrafting(consumer, TinkerMaterials.pigIron, folder);
    metalCrafting(consumer, TinkerMaterials.cinderslime, folder);
    metalCrafting(consumer, TinkerMaterials.queensSlime, folder);
    metalCrafting(consumer, TinkerMaterials.manyullyn, folder);
    metalCrafting(consumer, TinkerMaterials.hepatizon, folder);
    metalCrafting(consumer, TinkerMaterials.knightmetal, folder);
    packingRecipe(consumer, RecipeCategory.MISC, "ingot", Items.COPPER_INGOT, "nugget", TinkerMaterials.copperNugget, TinkerTags.Items.NUGGETS_COPPER, folder);
    packingRecipe(consumer, RecipeCategory.MISC, "ingot", Items.NETHERITE_SCRAP, "nugget", TinkerMaterials.debrisNugget, TinkerTags.Items.NUGGETS_NETHERITE_SCRAP, folder);
    packingRecipe(consumer, RecipeCategory.MISC, "ingot", Items.NETHERITE_INGOT, "nugget", TinkerMaterials.netheriteNugget, TinkerTags.Items.NUGGETS_NETHERITE, folder);

    SimpleCookingRecipeBuilder.blasting(Ingredient.of(TinkerWorld.rawCobalt, TinkerWorld.cobaltOre), RecipeCategory.MISC, TinkerMaterials.cobalt.getIngot(), 1.5f, 200)
      .unlockedBy("has_item", has(TinkerWorld.rawCobalt))
      .save(consumer, location(folder + "cobalt_ingot_blasting"));
    SimpleCookingRecipeBuilder.blasting(Ingredient.of(TinkerWorld.cobaltShard), RecipeCategory.MISC, TinkerMaterials.cobalt.getNugget(), 0.2f, 50)
      .unlockedBy("has_item", has(TinkerWorld.cobaltShard))
      .save(consumer, location(folder + "cobalt_nugget_blasting"));
    SimpleCookingRecipeBuilder.smelting(Ingredient.of(TinkerWorld.steelShard), RecipeCategory.MISC, TinkerMaterials.steel.getNugget(), 0.2f, 50)
      .unlockedBy("has_item", has(TinkerWorld.steelShard))
      .save(consumer, location(folder + "steel_nugget_smelting"));
    SimpleCookingRecipeBuilder.blasting(Ingredient.of(TinkerWorld.steelShard), RecipeCategory.MISC, TinkerMaterials.steel.getNugget(), 0.2f, 25)
      .unlockedBy("has_item", has(TinkerWorld.steelShard))
      .save(consumer, location(folder + "steel_nugget_blasting"));
    SimpleCookingRecipeBuilder.blasting(Ingredient.of(TinkerTags.Items.KNIGHTMETAL_SHARD), RecipeCategory.MISC, TinkerMaterials.knightmetal.getNugget(), 0.2f, 50)
      .unlockedBy("has_item", has(TinkerWorld.knightmetalShard))
      .save(consumer, location(folder + "knightmetal_nugget_smelting"));

    packingRecipe(consumer, RecipeCategory.MISC, "raw_block", TinkerWorld.rawCobaltBlock, "raw", TinkerWorld.rawCobalt, TinkerTags.Items.RAW_COBALT, folder);
  }
}
