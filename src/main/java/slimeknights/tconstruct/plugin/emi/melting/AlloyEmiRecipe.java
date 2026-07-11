package slimeknights.tconstruct.plugin.emi.melting;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuelLookup;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.ArrayList;
import java.util.List;

public final class AlloyEmiRecipe extends TConstructEmiRecipe {
  private static final Component CATALYST =
      TConstruct.makeTranslation("emi", "alloy.catalyst").withStyle(ChatFormatting.ITALIC);
  private static final String KEY_TEMPERATURE = TConstruct.makeTranslationKey("emi", "temperature");

  private final AlloyRecipe recipe;
  private final List<FluidInput> fluidInputs;
  private final EmiIngredient fuel;
  private final int capacity;

  public AlloyEmiRecipe(ResourceLocation id, AlloyRecipe recipe) {
    this(id, recipe, createInputs(recipe),
        TConstructEmiRecipe.fluids(EmiMeltingFuelHandler.getUsableFuels(recipe.getTemperature())));
  }

  private AlloyEmiRecipe(ResourceLocation id, AlloyRecipe recipe, List<FluidInput> fluidInputs, EmiIngredient fuel) {
    super(id, EMIConstants.ALLOY,
        fluidInputs.stream().filter(input -> !input.catalyst()).map(FluidInput::ingredient).toList(),
        fluidInputs.stream().filter(FluidInput::catalyst).map(FluidInput::ingredient).toList(),
        TConstructEmiRecipe.fluidOutputs(List.of(recipe.getOutput())));
    this.recipe = recipe;
    this.fluidInputs = fluidInputs;
    this.fuel = fuel;
    int max = recipe.getOutput().getAmount();
    for (FluidInput input : fluidInputs) {
      for (FluidStack stack : input.fluids()) {
        max = Math.max(max, stack.getAmount());
      }
    }
    this.capacity = max;
  }

  private static List<FluidInput> createInputs(AlloyRecipe recipe) {
    List<FluidInput> inputs = new ArrayList<>();
    for (AlloyRecipe.AlloyIngredient input : recipe.getInputs()) {
      List<FluidStack> fluids = input.fluid().getFluids();
      inputs.add(new FluidInput(fluids, TConstructEmiRecipe.fluids(fluids), input.catalyst()));
    }
    return inputs;
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    addBackground(widgets);
    int count = fluidInputs.size();
    if (count > 0) {
      int width = 48 / count;
      for (int index = 0; index < count; index++) {
        FluidInput input = fluidInputs.get(index);
        int tankX = 19 + index * width;
        int tankWidth = index == count - 1 ? 48 - width * index : width;
        var tank = EmiRenderHelper.tank(widgets, input.ingredient(), tankX, 11, tankWidth, 32, capacity);
        if (input.catalyst()) {
          tank.catalyst(true).appendTooltip(CATALYST);
        }
      }
    }

    EmiRenderHelper.tank(widgets, TConstructEmiRecipe.fluidIngredient(recipe.getOutput()),
        137, 11, 16, 32, capacity).recipeContext(this);
    EmiRenderHelper.tank(widgets, fuel, 94, 43, 16, 16, 1)
        .appendTooltip(() -> EmiRenderHelper.tooltip(fuelTooltip(fuel)));
    EmiRenderHelper.tankOverlay(widgets, EMIConstants.ALLOY_TEXTURE, 94, 43, 16, 16, 172, 17);
    widgets.addAnimatedTexture(EMIConstants.ALLOY_TEXTURE, 90, 21, 24, 17, 172, 0,
        200, true, false, false);

    Component temperature = Component.translatable(KEY_TEMPERATURE, recipe.getTemperature());
    widgets.addText(temperature, 102 - Minecraft.getInstance().font.width(temperature) / 2,
        5, 0xFF808080, false);
  }

  private static Component fuelTooltip(EmiIngredient ingredient) {
    List<dev.emi.emi.api.stack.EmiStack> stacks = ingredient.getEmiStacks();
    if (!stacks.isEmpty() && stacks.get(EmiRenderHelper.cycleIndex(stacks.size())).getKey() instanceof Fluid fluid) {
      var fuel = MeltingFuelLookup.findFuel(fluid);
      if (fuel != null) {
        return Component.translatable(KEY_TEMPERATURE, fuel.getTemperature()).withStyle(ChatFormatting.GRAY);
      }
    }
    return Component.empty();
  }

  private record FluidInput(List<FluidStack> fluids, EmiIngredient ingredient, boolean catalyst) {}
}
