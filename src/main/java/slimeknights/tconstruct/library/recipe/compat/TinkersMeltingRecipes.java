package slimeknights.tconstruct.library.recipe.compat;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer.OreRateType;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipeBuilder;

import java.util.List;
import java.util.function.Consumer;

/**
 * Thin, stable facade over {@link MeltingRecipeBuilder} for addons that feed Tinkers' Construct
 * melting recipes into their own recipe provider.
 *
 * <p>This is deliberately a pass-through: the parameters mirror the TiC builder so existing
 * knowledge transfers directly, but the signature is owned here. If an upstream builder changes,
 * only this class has to be updated.</p>
 */
public final class TinkersMeltingRecipes {
  private TinkersMeltingRecipes() {}

  /**
   * Melts the input into an exact fluid stack at a fixed temperature and time.
   *
   * @param provider    Recipe sink, typically an {@code IGTAddon#addRecipes} consumer
   * @param id          Recipe id, including the namespace the consumer writes to
   * @param input       Item ingredient that melts
   * @param fluid       Resulting fluid stack
   * @param temperature Required temperature
   * @param time        Time in ticks
   */
  public static void melting(Consumer<FinishedRecipe> provider, ResourceLocation id,
                             Ingredient input, FluidStack fluid, int temperature, int time) {
    MeltingRecipeBuilder.melting(input, fluid, temperature, time).save(provider, id);
  }

  /**
   * Melts the input into an exact fluid stack, deriving the temperature from the fluid and the
   * time from the ingot-relative factor.
   */
  public static void melting(Consumer<FinishedRecipe> provider, ResourceLocation id,
                             Ingredient input, FluidStack fluid, float timeFactor) {
    MeltingRecipeBuilder.melting(input, fluid, timeFactor).save(provider, id);
  }

  /** Melts the input into {@code amount} mB of the given fluid, temperature derived from it. */
  public static void melting(Consumer<FinishedRecipe> provider, ResourceLocation id,
                             Ingredient input, Fluid fluid, int amount, float timeFactor) {
    MeltingRecipeBuilder.melting(input, fluid, amount, timeFactor).save(provider, id);
  }

  /** Melts the input with additional fluid byproducts. */
  public static void melting(Consumer<FinishedRecipe> provider, ResourceLocation id,
                             Ingredient input, FluidStack fluid, int temperature, int time,
                             List<FluidStack> byproducts) {
    MeltingRecipeBuilder builder = MeltingRecipeBuilder.melting(input, fluid, temperature, time);
    for (FluidStack byproduct : byproducts) {
      builder.addByproduct(byproduct);
    }
    builder.save(provider, id);
  }

  /**
   * Melts an ore-like input; the output scales with the melter and the byproduct rates are handed
   * to {@link MeltingRecipeBuilder#setOre}.
   */
  public static void oreMelting(Consumer<FinishedRecipe> provider, ResourceLocation id,
                                Ingredient input, FluidStack fluid, int temperature, int time,
                                OreRateType rate, OreRateType... byproductRates) {
    MeltingRecipeBuilder.melting(input, fluid, temperature, time)
      .setOre(rate, byproductRates)
      .save(provider, id);
  }
}
