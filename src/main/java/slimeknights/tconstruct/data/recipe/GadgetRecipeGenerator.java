package slimeknights.tconstruct.data.recipe;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import slimeknights.mantle.recipe.data.ICommonRecipeHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.gadgets.entity.FrameType;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipeBuilder;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerWorld;
import slimeknights.tconstruct.world.block.FoliageType;

import java.util.function.Consumer;

public class GadgetRecipeGenerator implements ICommonRecipeHelper {
  public static void register(Consumer<FinishedRecipe> consumer) {
    new GadgetRecipeGenerator().addGadgetRecipes(consumer);
  }

  static void registerPunji(Consumer<FinishedRecipe> consumer, ItemLike punji) {
    new GadgetRecipeGenerator().addPunjiRecipe(consumer, punji);
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

  private void addGadgetRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "gadgets/";
    ItemCastingRecipeBuilder.tableRecipe(TinkerGadgets.piggyBackpack)
      .setCast(Items.SADDLE, true)
      .setFluidAndTime(TinkerFluids.skySlime, FluidValues.SLIMEBALL * 4)
      .save(consumer, prefix(TinkerGadgets.piggyBackpack, folder));
    addPunjiRecipe(consumer, TinkerGadgets.punji);

    folder = "gadgets/fancy_frame/";
    frameCrafting(consumer, Tags.Items.NUGGETS_GOLD, FrameType.GOLD);
    frameCrafting(consumer, TinkerMaterials.manyullyn.getNuggetTag(), FrameType.MANYULLYN);
    frameCrafting(consumer, TinkerTags.Items.NUGGETS_NETHERITE, FrameType.NETHERITE);
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerGadgets.itemFrame.get(FrameType.DIAMOND))
      .define('e', TinkerCommons.obsidianPane)
      .define('M', Tags.Items.GEMS_DIAMOND)
      .pattern(" e ")
      .pattern("eMe")
      .pattern(" e ")
      .unlockedBy("has_item", has(Tags.Items.GEMS_DIAMOND))
      .group(prefix("fancy_item_frame"))
      .save(consumer, location("gadgets/frame/" + FrameType.DIAMOND.getSerializedName()));
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerGadgets.itemFrame.get(FrameType.CLEAR))
      .define('e', Tags.Items.GLASS_PANES_COLORLESS)
      .define('M', Tags.Items.GLASS_COLORLESS)
      .pattern(" e ")
      .pattern("eMe")
      .pattern(" e ")
      .unlockedBy("has_item", has(Tags.Items.GLASS_PANES_COLORLESS))
      .group(prefix("fancy_item_frame"))
      .save(consumer, location(folder + FrameType.CLEAR.getSerializedName()));
    Item goldFrame = TinkerGadgets.itemFrame.get(FrameType.GOLD);
    Item reversedFrame = TinkerGadgets.itemFrame.get(FrameType.REVERSED_GOLD);
    ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, reversedFrame)
      .requires(goldFrame)
      .requires(Items.REDSTONE_TORCH)
      .unlockedBy("has_item", has(goldFrame))
      .group(prefix("reverse_fancy_item_frame"))
      .save(consumer, location(folder + FrameType.REVERSED_GOLD.getSerializedName()));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, goldFrame)
      .requires(reversedFrame)
      .requires(Items.REDSTONE_TORCH)
      .unlockedBy("has_item", has(reversedFrame))
      .group(prefix("reverse_fancy_item_frame"))
      .save(consumer, location(folder + "reversed_reversed_gold"));

    String cakeFolder = "gadgets/cake/";
    TinkerGadgets.cake.forEach((foliage, cake) -> {
      if (foliage != FoliageType.ICHOR) {
        SlimeType slime = foliage.asSlime();
        ItemLike grass = TinkerWorld.slimeTallGrass.get(foliage);
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, cake)
          .define('M', slime != null ? TinkerFluids.slime.get(slime).getBucket() : TinkerFluids.honey.asItem())
          .define('S', foliage.isNether()
            ? Ingredient.of(Tags.Items.DUSTS_GLOWSTONE)
            : foliage == FoliageType.ENDER ? Ingredient.of(Tags.Items.DUSTS_REDSTONE) : Ingredient.of(Items.SUGAR))
          .define('E', Items.EGG)
          .define('W', TinkerWorld.slimeTallGrass.get(foliage))
          .pattern("MMM").pattern("SES").pattern("WWW")
          .unlockedBy("has_slime", has(grass))
          .save(consumer, location(cakeFolder + foliage.getSerializedName()));
      }
    });
    ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, TinkerGadgets.cake.get(FoliageType.ICHOR))
      .define('M', TinkerFluids.ichor)
      .define('S', Ingredient.of(Tags.Items.DUSTS_GLOWSTONE))
      .define('E', Items.EGG)
      .define('W', Blocks.WARPED_ROOTS)
      .pattern("WWW").pattern("SES").pattern("MMM")
      .unlockedBy("has_slime", has(TinkerFluids.ichor))
      .save(consumer, location(cakeFolder + "ichor"));
    Item bucket = TinkerFluids.magma.asItem();
    ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, TinkerGadgets.magmaCake)
      .define('M', bucket)
      .define('S', Ingredient.of(Tags.Items.DUSTS_GLOWSTONE))
      .define('E', Items.EGG)
      .define('W', Blocks.CRIMSON_ROOTS)
      .pattern("MMM").pattern("SES").pattern("WWW")
      .unlockedBy("has_slime", has(bucket))
      .save(consumer, location(cakeFolder + "magma"));
  }

  private void addPunjiRecipe(Consumer<FinishedRecipe> consumer, ItemLike punji) {
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, punji)
      .define('b', Items.BAMBOO)
      .pattern(" b ")
      .pattern("bbb")
      .unlockedBy("has_item", has(Items.BAMBOO))
      .save(consumer, location("gadgets/punji"));
  }

  private void frameCrafting(Consumer<FinishedRecipe> consumer, TagKey<Item> edges, FrameType type) {
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, TinkerGadgets.itemFrame.get(type))
      .define('e', edges)
      .define('M', TinkerCommons.obsidianPane)
      .pattern(" e ")
      .pattern("eMe")
      .pattern(" e ")
      .unlockedBy("has_item", has(edges))
      .group(prefix("fancy_item_frame"))
      .save(consumer, location("gadgets/frame/" + type.getSerializedName()));
  }
}
