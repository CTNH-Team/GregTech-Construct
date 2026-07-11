package slimeknights.tconstruct.plugin.emi.melting;

import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;

import java.util.List;

public final class FoundryEmiRecipe extends AbstractMeltingEmiRecipe {
  public FoundryEmiRecipe(ResourceLocation id, MeltingRecipe recipe) {
    super(id, EMIConstants.FOUNDRY, recipe,
        TConstructEmiRecipe.fluidOutputs(recipe.getOutputWithByproducts().stream().flatMap(List::stream).toList()));
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    addSharedWidgets(widgets, 32);
    List<List<FluidStack>> outputs = recipe.getOutputWithByproducts();
    int capacity = FluidValues.METAL_BLOCK;
    for (List<FluidStack> stacks : outputs) {
      for (FluidStack stack : stacks) {
        capacity = Math.max(capacity, stack.getAmount());
      }
    }
    VariableFluidTanks.add(widgets, outputs, 96, 4, 32, 32, capacity, this);
  }
}
