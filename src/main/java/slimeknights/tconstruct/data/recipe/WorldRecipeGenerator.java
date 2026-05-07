package slimeknights.tconstruct.data.recipe;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import slimeknights.mantle.recipe.data.ICommonRecipeHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.common.registration.GeodeItemObject;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipeBuilder;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.function.Consumer;

public class WorldRecipeGenerator implements ICommonRecipeHelper {
  public static void register(Consumer<FinishedRecipe> consumer) {
    new WorldRecipeGenerator().addWorldRecipes(consumer);
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

  static void registerGreenheartPlanksRecipe(Consumer<FinishedRecipe> consumer, ItemLike planks, TagKey<Item> logTag) {
    ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, planks, 4)
                          .requires(logTag)
                          .group("planks")
                          .unlockedBy("has_log", has(logTag))
                          .save(consumer, TConstruct.getResource("world/wood/greenheart/planks"));
  }

  private void addWorldRecipes(Consumer<FinishedRecipe> consumer) {
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TinkerWorld.congealedSlime.get(SlimeType.EARTH))
                       .define('#', SlimeType.EARTH.getSlimeballTag())
                       .pattern("##")
                       .pattern("##")
                       .unlockedBy("has_item", has(SlimeType.EARTH.getSlimeballTag()))
                       .group("tconstruct:congealed_slime")
                       .save(consumer, location("common/slime/earth/congealed"));

    for (SlimeType slimeType : SlimeType.TINKER) {
      ResourceLocation name = location("common/slime/" + slimeType.getSerializedName() + "/congealed");
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TinkerWorld.congealedSlime.get(slimeType))
                         .define('#', slimeType.getSlimeballTag())
                         .pattern("##")
                         .pattern("##")
                         .unlockedBy("has_item", has(slimeType.getSlimeballTag()))
                         .group("tconstruct:congealed_slime")
                         .save(consumer, name);
      ResourceLocation blockName = location("common/slime/" + slimeType.getSerializedName() + "/slimeblock");
      ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TinkerWorld.slime.get(slimeType))
                         .define('#', slimeType.getSlimeballTag())
                         .pattern("###")
                         .pattern("###")
                         .pattern("###")
                         .unlockedBy("has_item", has(slimeType.getSlimeballTag()))
                         .group("slime_blocks")
                         .save(consumer, blockName);
      ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TinkerCommons.slimeball.get(slimeType), 9)
                            .requires(TinkerWorld.slime.get(slimeType))
                            .unlockedBy("has_item", has(TinkerWorld.slime.get(slimeType)))
                            .group("tconstruct:slime_balls")
                            .save(consumer, "tconstruct:common/slime/" + slimeType.getSerializedName() + "/slimeball_from_block");
    }

    for (SlimeType slimeType : SlimeType.values()) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TinkerCommons.slimeball.get(slimeType), 4)
                            .requires(TinkerWorld.congealedSlime.get(slimeType))
                            .unlockedBy("has_item", has(TinkerWorld.congealedSlime.get(slimeType)))
                            .group("tconstruct:slime_balls")
                            .save(consumer, "tconstruct:common/slime/" + slimeType.getSerializedName() + "/slimeball_from_congealed");
    }

    Consumer<FinishedRecipe> slimeConsumer = withCondition(consumer, ConfigEnabledCondition.SLIME_RECIPE_FIX);
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Blocks.STICKY_PISTON)
                       .pattern("#")
                       .pattern("P")
                       .define('#', Tags.Items.SLIMEBALLS)
                       .define('P', Blocks.PISTON)
                       .unlockedBy("has_slime_ball", has(Tags.Items.SLIMEBALLS))
                       .save(slimeConsumer, location("common/slime/sticky_piston"));
    ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.LEAD, 2)
                       .define('~', Items.STRING)
                       .define('O', Tags.Items.SLIMEBALLS)
                       .pattern("~~ ")
                       .pattern("~O ")
                       .pattern("  ~")
                       .unlockedBy("has_slime_ball", has(Tags.Items.SLIMEBALLS))
                       .save(slimeConsumer, location("common/slime/lead"));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.BREWING, Items.MAGMA_CREAM)
                          .requires(Items.BLAZE_POWDER)
                          .requires(Tags.Items.SLIMEBALLS)
                          .unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER))
                          .save(slimeConsumer, location("common/slime/magma_cream"));

    String woodFolder = "world/wood/";
    woodCrafting(consumer, TinkerWorld.greenheart, woodFolder + "greenheart/");
    woodCrafting(consumer, TinkerWorld.skyroot, woodFolder + "skyroot/");
    woodCrafting(consumer, TinkerWorld.bloodshroom, woodFolder + "bloodshroom/");
    woodCrafting(consumer, TinkerWorld.enderbark, woodFolder + "enderbark/");

    geodeRecipes(consumer, TinkerWorld.earthGeode, SlimeType.EARTH, "common/slime/earth/");
    geodeRecipes(consumer, TinkerWorld.skyGeode, SlimeType.SKY, "common/slime/sky/");
    geodeRecipes(consumer, TinkerWorld.ichorGeode, SlimeType.ICHOR, "common/slime/ichor/");
    geodeRecipes(consumer, TinkerWorld.enderGeode, SlimeType.ENDER, "common/slime/ender/");
  }

  private void geodeRecipes(Consumer<FinishedRecipe> consumer, GeodeItemObject geode, SlimeType slime, String folder) {
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, geode.getBlock())
                       .define('#', geode.asItem())
                       .pattern("##")
                       .pattern("##")
                       .unlockedBy("has_item", has(geode.asItem()))
                       .group("tconstruct:slime_crystal_block")
                       .save(consumer, location(folder + "crystal_block"));
    SimpleCookingRecipeBuilder.blasting(Ingredient.of(geode), RecipeCategory.MISC, TinkerCommons.slimeball.get(slime), 0.2f, 200)
                              .unlockedBy("has_crystal", has(geode))
                              .group("tconstruct:slime_crystal")
                              .save(consumer, location(folder + "crystal_smelting"));
    ItemLike dirt = TinkerWorld.slimeDirt.get(slime.asDirt());
    SimpleCookingRecipeBuilder.blasting(Ingredient.of(dirt), RecipeCategory.MISC, geode, 0.2f, 400)
                              .unlockedBy("has_dirt", has(dirt))
                              .group("tconstruct:slime_dirt")
                              .save(consumer, location(folder + "crystal_growing"));
  }
}
