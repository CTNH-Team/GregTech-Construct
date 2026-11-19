package slimeknights.tconstruct.library.recipe.compat;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipeBuilder;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Thin, stable facade over {@link ItemCastingRecipeBuilder} for addons that feed Tinkers' Construct
 * casting recipes into their own recipe provider.
 *
 * <p>Parameters mirror the TiC builder; the signature is owned here so an upstream change only has
 * to be absorbed in this class.</p>
 */
public final class TinkersCastingRecipes {
  private TinkersCastingRecipes() {}

  /**
   * Casting table recipe with an explicit fluid stack; the cooling time is derived from it.
   *
   * @param provider Recipe sink, typically an {@code IGTAddon#addRecipes} consumer
   * @param id       Recipe id, including the namespace the consumer writes to
   * @param result   Recipe output
   * @param fluid    Fluid consumed by the cast
   * @param cast     Cast ingredient, or {@code null} for a cast that is not required
   * @param consumed Whether the cast is consumed; ignored when {@code cast} is null
   */
  public static void table(Consumer<FinishedRecipe> provider, ResourceLocation id,
                           ItemLike result, FluidStack fluid,
                           @Nullable Ingredient cast, boolean consumed) {
    table(provider, id, result, fluid.getFluid(), fluid.getAmount(), -1, cast, consumed);
  }

  /** Casting table recipe with an explicit temperature for the cooling time. */
  public static void table(Consumer<FinishedRecipe> provider, ResourceLocation id,
                           ItemLike result, FluidStack fluid, int temperature,
                           @Nullable Ingredient cast, boolean consumed) {
    table(provider, id, result, fluid.getFluid(), fluid.getAmount(), temperature, cast, consumed);
  }

  /** Casting basin recipe with an explicit fluid stack; the cooling time is derived from it. */
  public static void basin(Consumer<FinishedRecipe> provider, ResourceLocation id,
                           ItemLike result, FluidStack fluid,
                           @Nullable Ingredient cast, boolean consumed) {
    basin(provider, id, result, fluid.getFluid(), fluid.getAmount(), -1, cast, consumed);
  }

  /** Casting basin recipe with an explicit temperature for the cooling time. */
  public static void basin(Consumer<FinishedRecipe> provider, ResourceLocation id,
                           ItemLike result, FluidStack fluid, int temperature,
                           @Nullable Ingredient cast, boolean consumed) {
    basin(provider, id, result, fluid.getFluid(), fluid.getAmount(), temperature, cast, consumed);
  }

  /** Casting table recipe from a raw fluid and amount; {@code temperature < 0} auto-derives it. */
  public static void table(Consumer<FinishedRecipe> provider, ResourceLocation id,
                           ItemLike result, Fluid fluid, int amount, int temperature,
                           @Nullable Ingredient cast, boolean consumed) {
    build(ItemCastingRecipeBuilder.tableRecipe(result), fluid, amount, temperature, cast, consumed)
      .save(provider, id);
  }

  /** Casting basin recipe from a raw fluid and amount; {@code temperature < 0} auto-derives it. */
  public static void basin(Consumer<FinishedRecipe> provider, ResourceLocation id,
                           ItemLike result, Fluid fluid, int amount, int temperature,
                           @Nullable Ingredient cast, boolean consumed) {
    build(ItemCastingRecipeBuilder.basinRecipe(result), fluid, amount, temperature, cast, consumed)
      .save(provider, id);
  }

  private static ItemCastingRecipeBuilder build(ItemCastingRecipeBuilder builder, Fluid fluid, int amount,
                                                int temperature, @Nullable Ingredient cast, boolean consumed) {
    if (temperature < 0) {
      builder.setFluidAndTime(new FluidStack(fluid, amount));
    } else {
      builder.setFluid(fluid, amount).setCoolingTime(temperature, amount);
    }
    if (cast != null) {
      builder.setCast(cast, consumed);
    }
    return builder;
  }
}
