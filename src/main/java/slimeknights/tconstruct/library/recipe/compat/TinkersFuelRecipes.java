package slimeknights.tconstruct.library.recipe.compat;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuelBuilder;

import java.util.function.Consumer;

/**
 * Thin, stable facade over {@link MeltingFuelBuilder}.
 */
public final class TinkersFuelRecipes {
  private TinkersFuelRecipes() {}

  /**
   * Fluid fuel for the melter and smeltery.
   *
   * @param duration Fuel duration in ticks
   */
  public static void fuel(Consumer<FinishedRecipe> provider, ResourceLocation id,
                          FluidStack fluid, int duration) {
    MeltingFuelBuilder.fuel(fluid, duration).save(provider, id);
  }

  /** Solid fuel (items such as coal) for the melter. */
  public static void solidFuel(Consumer<FinishedRecipe> provider, ResourceLocation id, int temperature) {
    MeltingFuelBuilder.solid(temperature).save(provider, id);
  }
}
