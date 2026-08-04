package slimeknights.tconstruct.plugin.emi.casting;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.ArrayList;
import java.util.List;

public final class CastingEmiRecipe extends TConstructEmiRecipe {
  private static final String KEY_COOLING_TIME = TConstruct.makeTranslationKey("emi", "time");
  private static final String KEY_CAST_KEPT = TConstruct.makeTranslationKey("emi", "casting.cast_kept");
  private static final String KEY_CAST_CONSUMED = TConstruct.makeTranslationKey("emi", "casting.cast_consumed");

  private final IDisplayableCastingRecipe recipe;
  private final boolean basin;
  private final EmiIngredient fluids;
  private final EmiIngredient casts;

  public CastingEmiRecipe(ResourceLocation id, EMIConstants.TConstructEmiCategory category,
                          IDisplayableCastingRecipe recipe, boolean basin) {
    this(id, category, recipe, basin, TConstructEmiRecipe.fluids(recipe.getFluids()),
        TConstructEmiRecipe.itemIngredient(recipe.getCastItems()));
  }

  private CastingEmiRecipe(ResourceLocation id, EMIConstants.TConstructEmiCategory category,
                           IDisplayableCastingRecipe recipe, boolean basin, EmiIngredient fluids,
                           EmiIngredient casts) {
    super(id, category, inputs(recipe, fluids, casts), catalysts(recipe, casts),
        TConstructEmiRecipe.itemOutputs(recipe.getOutputs()));
    this.recipe = recipe;
    this.basin = basin;
    this.fluids = fluids;
    this.casts = casts;
  }

  private static List<EmiIngredient> inputs(IDisplayableCastingRecipe recipe, EmiIngredient fluids,
                                            EmiIngredient casts) {
    List<EmiIngredient> inputs = new ArrayList<>();
    inputs.add(fluids);
    if (recipe.hasCast() && recipe.isConsumed()) {
      inputs.add(casts);
    }
    return inputs;
  }

  private static List<EmiIngredient> catalysts(IDisplayableCastingRecipe recipe, EmiIngredient casts) {
    return recipe.hasCast() && !recipe.isConsumed() ? List.of(casts) : List.of();
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    addBackground(widgets);
    EmiRenderHelper.tank(widgets, fluids, 3, 3, 32, 32, FluidValues.METAL_BLOCK);
    EmiRenderHelper.tankOverlay(widgets, EMIConstants.CASTING_TEXTURE, 3, 3, 32, 32, 133, 0);

    int streamHeight = recipe.hasCast() ? 11 : 27;
    EmiRenderHelper.tank(widgets, fluids, 43, 8, 6, streamHeight, 1);

    if (recipe.hasCast()) {
      SlotWidget cast = EmiRenderHelper.slot(widgets, casts, 38, 19);
      if (!recipe.isConsumed()) {
        cast.catalyst(true);
      }
      widgets.addTexture(EMIConstants.CASTING_TEXTURE, 63, 39, 13, 11, 141,
          recipe.isConsumed() ? 32 : 43, 13, 11, 256, 256);
      widgets.addTooltipText(List.of(Component.translatable(recipe.isConsumed() ? KEY_CAST_CONSUMED : KEY_CAST_KEPT)),
          63, 39, 13, 11);
    }

    EmiRenderHelper.slot(widgets, TConstructEmiRecipe.itemIngredient(recipe.getOutputs()), 93, 18)
        .recipeContext(this);
    widgets.addAnimatedTexture(EMIConstants.CASTING_TEXTURE, 58, 18, 24, 17, 117, 32,
        Math.max(1, recipe.getCoolingTime()), true, false, false);
    widgets.addTexture(EMIConstants.CASTING_TEXTURE, 38, 35, 16, 16, 117, basin ? 16 : 0,
        16, 16, 256, 256);

    Component cooling = Component.translatable(KEY_COOLING_TIME, recipe.getCoolingTime() / 20);
    widgets.addText(cooling, 72 - Minecraft.getInstance().font.width(cooling) / 2,
        2, 0xFF808080, false);
  }
}
