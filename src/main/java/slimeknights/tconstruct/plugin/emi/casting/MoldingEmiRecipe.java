package slimeknights.tconstruct.plugin.emi.casting;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.ArrayList;
import java.util.List;

public final class MoldingEmiRecipe extends TConstructEmiRecipe {
  private static final Component TOOLTIP_PATTERN_CONSUMED =
      Component.translatable(TConstruct.makeTranslationKey("emi", "molding.pattern_consumed"));

  private final MoldingRecipe recipe;
  private final EmiIngredient material;
  private final EmiIngredient pattern;
  private final ItemStack output;

  public MoldingEmiRecipe(ResourceLocation id, MoldingRecipe recipe, RegistryAccess access) {
    this(id, recipe, EmiIngredient.of(recipe.getMaterial()), EmiIngredient.of(recipe.getPattern()),
        recipe.getResultItem(access));
  }

  private MoldingEmiRecipe(ResourceLocation id, MoldingRecipe recipe, EmiIngredient material,
                           EmiIngredient pattern, ItemStack output) {
    super(id, EMIConstants.MOLDING, inputs(recipe, material, pattern),
        recipe.isPatternConsumed() ? List.of() : List.of(pattern),
        TConstructEmiRecipe.itemOutputs(List.of(output)));
    this.recipe = recipe;
    this.material = material;
    this.pattern = pattern;
    this.output = output;
  }

  private static List<EmiIngredient> inputs(MoldingRecipe recipe, EmiIngredient material, EmiIngredient pattern) {
    List<EmiIngredient> inputs = new ArrayList<>();
    inputs.add(material);
    if (!recipe.getPattern().isEmpty() && recipe.isPatternConsumed()) {
      inputs.add(pattern);
    }
    return inputs;
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    addBackground(widgets);
    EmiRenderHelper.slot(widgets, material, 3, 24);
    EmiRenderHelper.slot(widgets, TConstructEmiRecipe.itemIngredient(output), 51, 24).recipeContext(this);

    Ingredient patternIngredient = recipe.getPattern();
    if (!patternIngredient.isEmpty()) {
      EmiRenderHelper.slot(widgets, pattern, 3, 1);
      if (!recipe.isPatternConsumed()) {
        EmiRenderHelper.slot(widgets, pattern, 51, 8);
      } else {
        widgets.addTooltipText(List.of(TOOLTIP_PATTERN_CONSUMED), 50, 7, 18, 18);
      }
    }

    boolean basin = recipe.getType() == TinkerRecipeTypes.MOLDING_BASIN.get();
    widgets.addTexture(EMIConstants.CASTING_TEXTURE, 3, 40, 16, 16, 117, basin ? 16 : 0,
        16, 16, 256, 256);
    if (!patternIngredient.isEmpty()) {
      widgets.addTexture(EMIConstants.CASTING_TEXTURE, 51, 40, 16, 16, 117, basin ? 16 : 0,
          16, 16, 256, 256);
      widgets.addTexture(EMIConstants.CASTING_TEXTURE, 8, 17, 6, 6, 70, 55, 6, 6, 256, 256);
    } else {
      widgets.addTexture(EMIConstants.CASTING_TEXTURE, 8, 17, 6, 6, 76, 55, 6, 6, 256, 256);
    }
  }
}
