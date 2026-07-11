package slimeknights.tconstruct.plugin.emi.melting;

import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuelLookup;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

public final class MeltingEmiRecipe extends AbstractMeltingEmiRecipe {
  public MeltingEmiRecipe(ResourceLocation id, MeltingRecipe recipe) {
    super(id, EMIConstants.MELTING, recipe, TConstructEmiRecipe.fluidOutputs(java.util.List.of(recipe.getOutput())));
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    boolean solidFuel = recipe.getTemperature() <= MeltingFuelLookup.getSolid().getTemperature();
    addSharedWidgets(widgets, solidFuel ? 15 : 32);

    EmiRenderHelper.tank(widgets, TConstructEmiRecipe.fluidIngredient(recipe.getOutput()),
        96, 4, 32, 32, FluidValues.METAL_BLOCK).recipeContext(this);
    EmiRenderHelper.tankOverlay(widgets, EMIConstants.MELTING_TEXTURE, 96, 4, 32, 32, 132, 0);

    if (solidFuel) {
      MeltingFuel fuel = MeltingFuelLookup.getSolid();
      widgets.addTexture(EMIConstants.MELTING_TEXTURE, 1, 19, 18, 20, 164, 0, 18, 20, 256, 256);
      EmiRenderHelper.slot(widgets, EmiMeltingFuelHandler.SOLID_FUELS, 2, 22)
          .appendTooltip(Component.translatable(KEY_TEMPERATURE, fuel.getTemperature()).withStyle(ChatFormatting.GRAY))
          .appendTooltip(Component.translatable(KEY_MULTIPLIER, fuel.getRate() / 10f).withStyle(ChatFormatting.GRAY));
    }
  }
}
