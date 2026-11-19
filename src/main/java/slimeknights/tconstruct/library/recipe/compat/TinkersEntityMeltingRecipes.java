package slimeknights.tconstruct.library.recipe.compat;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipeBuilder;

import java.util.function.Consumer;

/**
 * Thin, stable facade over {@link EntityMeltingRecipeBuilder}.
 */
public final class TinkersEntityMeltingRecipes {
  private TinkersEntityMeltingRecipes() {}

  /**
   * Melts entities touching the smeltery into fluid.
   *
   * @param damage Damage dealt per recipe run
   */
  public static void melting(Consumer<FinishedRecipe> provider, ResourceLocation id,
                             EntityIngredient ingredient, FluidStack output, int damage) {
    EntityMeltingRecipeBuilder.melting(ingredient, output, damage).save(provider, id);
  }

  /** Melts entities into fluid without dealing damage. */
  public static void melting(Consumer<FinishedRecipe> provider, ResourceLocation id,
                             EntityIngredient ingredient, FluidStack output) {
    EntityMeltingRecipeBuilder.melting(ingredient, output).save(provider, id);
  }
}
