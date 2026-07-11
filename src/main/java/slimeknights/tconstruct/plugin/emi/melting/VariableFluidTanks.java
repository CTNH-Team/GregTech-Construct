package slimeknights.tconstruct.plugin.emi.melting;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

final class VariableFluidTanks {
  private VariableFluidTanks() {}

  static <T> void add(WidgetHolder widgets, List<T> values, int x, int y, int totalWidth, int height,
                      int capacity, @Nullable EmiRecipe recipeContext, Function<T,List<FluidStack>> fluids) {
    int count = values.size();
    if (count == 0) {
      return;
    }
    int width = totalWidth / count;
    for (int index = 0; index < count; index++) {
      int tankX = x + index * width;
      int tankWidth = index == count - 1 ? totalWidth - width * index : width;
      EmiIngredient ingredient = TConstructEmiRecipe.fluids(fluids.apply(values.get(index)));
      TankWidget tank = EmiRenderHelper.tank(widgets, ingredient, tankX, y, tankWidth, height, capacity);
      if (recipeContext != null) {
        tank.recipeContext(recipeContext);
      }
    }
  }

  static void add(WidgetHolder widgets, List<List<FluidStack>> values, int x, int y, int totalWidth, int height,
                  int capacity, @Nullable EmiRecipe recipeContext) {
    add(widgets, values, x, y, totalWidth, height, capacity, recipeContext, Function.identity());
  }
}
