package slimeknights.tconstruct.plugin.emi.melting;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuelLookup;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.List;

abstract class AbstractMeltingEmiRecipe extends TConstructEmiRecipe {
  static final String KEY_TEMPERATURE = TConstruct.makeTranslationKey("emi", "temperature");
  static final String KEY_MULTIPLIER = TConstruct.makeTranslationKey("emi", "melting.multiplier");
  private static final String KEY_TIME = TConstruct.makeTranslationKey("emi", "melting.time");
  private static final Component TOOLTIP_ORE =
      Component.translatable(TConstruct.makeTranslationKey("emi", "melting.ore"));

  protected final MeltingRecipe recipe;
  protected final EmiIngredient input;
  protected final EmiIngredient liquidFuel;

  AbstractMeltingEmiRecipe(ResourceLocation id, EMIConstants.TConstructEmiCategory category,
                           MeltingRecipe recipe, List<EmiStack> outputs) {
    this(id, category, recipe, outputs, EmiIngredient.of(recipe.getInput()),
        TConstructEmiRecipe.fluids(EmiMeltingFuelHandler.getUsableFuels(recipe.getTemperature())));
  }

  private AbstractMeltingEmiRecipe(ResourceLocation id, EMIConstants.TConstructEmiCategory category,
                                   MeltingRecipe recipe, List<EmiStack> outputs, EmiIngredient input,
                                   EmiIngredient liquidFuel) {
    super(id, category, List.of(input), List.of(), outputs);
    this.recipe = recipe;
    this.input = input;
    this.liquidFuel = liquidFuel;
  }

  protected final void addSharedWidgets(WidgetHolder widgets, int fuelHeight) {
    addBackground(widgets);
    EmiRenderHelper.slot(widgets, input, 24, 18);
    var fuelTank = EmiRenderHelper.tank(widgets, liquidFuel, 4, 4, 12, fuelHeight, 1);
    fuelTank.appendTooltip(() -> EmiRenderHelper.tooltip(liquidFuelTooltip(false)));
    fuelTank.appendTooltip(() -> EmiRenderHelper.tooltip(liquidFuelTooltip(true)));
    widgets.addAnimatedTexture(EMIConstants.MELTING_TEXTURE, 56, 18, 24, 17, 150, 41,
        Math.max(1, recipe.getTime() * 5), true, false, false);

    if (recipe.getOreType() != null) {
      widgets.addTexture(EMIConstants.MELTING_TEXTURE, 87, 31, 6, 6, 132, 34, 6, 6, 256, 256);
      widgets.addTooltipText(List.of(TOOLTIP_ORE), 87, 31, 16, 16);
    }
    widgets.addTooltipText(List.of(Component.translatable(KEY_TIME, recipe.getTime() / 4)), 56, 18, 24, 17);

    Component temperature = Component.translatable(KEY_TEMPERATURE, recipe.getTemperature());
    widgets.addText(temperature, 56 - Minecraft.getInstance().font.width(temperature) / 2,
        3, 0xFF808080, false);
  }

  private Component liquidFuelTooltip(boolean multiplier) {
    List<EmiStack> stacks = liquidFuel.getEmiStacks();
    if (!stacks.isEmpty() && stacks.get(EmiRenderHelper.cycleIndex(stacks.size())).getKey() instanceof Fluid fluid) {
      MeltingFuel fuel = MeltingFuelLookup.findFuel(fluid);
      if (fuel != null) {
        return Component.translatable(multiplier ? KEY_MULTIPLIER : KEY_TEMPERATURE,
            multiplier ? fuel.getRate() / 10f : fuel.getTemperature()).withStyle(ChatFormatting.GRAY);
      }
    }
    return Component.empty();
  }
}
