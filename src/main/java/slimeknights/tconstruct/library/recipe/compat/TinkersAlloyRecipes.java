package slimeknights.tconstruct.library.recipe.compat;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipeBuilder;

import java.util.List;
import java.util.function.Consumer;

/**
 * Thin, stable facade over {@link AlloyRecipeBuilder}.
 */
public final class TinkersAlloyRecipes {
  private TinkersAlloyRecipes() {}

  /**
   * Alloys the given inputs into the result.
   *
   * @param provider Recipe sink, typically an {@code IGTAddon#addRecipes} consumer
   * @param id       Recipe id, including the namespace the consumer writes to
   * @param result   Alloy produced
   * @param inputs   Fluids consumed, in declaration order
   */
  public static void alloy(Consumer<FinishedRecipe> provider, ResourceLocation id,
                           FluidStack result, List<FluidStack> inputs) {
    alloy(provider, id, result, inputs, List.of());
  }

  /** Alloys the given inputs into the result, with catalysts that are not consumed. */
  public static void alloy(Consumer<FinishedRecipe> provider, ResourceLocation id,
                           FluidStack result, List<FluidStack> inputs, List<FluidStack> catalysts) {
    AlloyRecipeBuilder builder = AlloyRecipeBuilder.alloy(result);
    for (FluidStack input : inputs) {
      builder.addInput(input);
    }
    for (FluidStack catalyst : catalysts) {
      builder.addCatalyst(FluidIngredient.of(catalyst));
    }
    builder.save(provider, id);
  }
}
