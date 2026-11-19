package slimeknights.tconstruct.library.recipe.compat;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipeBuilder;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Thin, stable facade over {@link MoldingRecipeBuilder}.
 */
public final class TinkersMoldingRecipes {
  private TinkersMoldingRecipes() {}

  /**
   * Molding table recipe: presses the material into {@code result}.
   *
   * @param result          Mold produced, such as a sand cast
   * @param pattern         Item pressed into the mold, or {@code null} for a plain pickup recipe
   * @param patternConsumed Whether the pattern is destroyed
   * @param material        Ingredient placed in the mold, typically a sand tag
   */
  public static void table(Consumer<FinishedRecipe> provider, ResourceLocation id,
                           ItemLike result, @Nullable ItemLike pattern, boolean patternConsumed,
                           Ingredient material) {
    build(MoldingRecipeBuilder.moldingTable(result), pattern, patternConsumed, material).save(provider, id);
  }

  /** Molding basin recipe; same parameters as {@link #table}. */
  public static void basin(Consumer<FinishedRecipe> provider, ResourceLocation id,
                           ItemLike result, @Nullable ItemLike pattern, boolean patternConsumed,
                           Ingredient material) {
    build(MoldingRecipeBuilder.moldingBasin(result), pattern, patternConsumed, material).save(provider, id);
  }

  private static MoldingRecipeBuilder build(MoldingRecipeBuilder builder, @Nullable ItemLike pattern,
                                            boolean patternConsumed, Ingredient material) {
    if (pattern != null) {
      builder.setPattern(pattern, patternConsumed);
    }
    return builder.setMaterial(material);
  }
}
