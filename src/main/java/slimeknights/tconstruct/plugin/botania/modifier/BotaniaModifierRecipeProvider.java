package slimeknights.tconstruct.plugin.botania.modifier;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.CompoundIngredient;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.library.recipe.modifiers.adding.ModifierRecipeBuilder;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.plugin.botania.BotaniaTiCAddon;

import java.util.function.Consumer;

/**
 * Botania-specific modifier recipes registered through TiCAddon.
 */
public class BotaniaModifierRecipeProvider extends BaseRecipeProvider {
  public BotaniaModifierRecipeProvider(PackOutput packOutput) {
    super(packOutput);
  }

  @Override
  protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
    String upgradeFolder = "tools/modifiers/upgrade/";
    ModifierRecipeBuilder.modifier(BotaniaModifierIds.manafix)
      .addInput(vazkii.botania.common.block.BotaniaBlocks.spawnerClaw)
      .setMaxLevel(5)
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(ingredientFromTags(TinkerTags.Items.MODIFIABLE))
      .save(withCondition(consumer, new ModLoadedCondition(BotaniaTiCAddon.MOD_ID)), location(upgradeFolder + "manafix"));
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Botania Modifier Recipes";
  }

  @SafeVarargs
  private static Ingredient ingredientFromTags(TagKey<Item>... tags) {
    Ingredient[] tagIngredients = new Ingredient[tags.length];
    for (int i = 0; i < tags.length; i++) {
      tagIngredients[i] = Ingredient.of(tags[i]);
    }
    return CompoundIngredient.of(tagIngredients);
  }
}
