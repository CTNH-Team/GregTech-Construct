package slimeknights.tconstruct.data.gtceu;

import com.gregtechceu.gtceu.api.recipe.ingredient.fluid.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.L;
import static com.gregtechceu.gtceu.api.GTValues.VA;

final class GTConstructRecipeWriter {
  private GTConstructRecipeWriter() {}

  static void write(Consumer<FinishedRecipe> provider, GTConstructRecipePlan plan,
                    List<GTConstructRecipes.SolidifierPart> parts) {
    if (parts.isEmpty()) {
      return;
    }

    String recipeTypeName = plan.useVacuum() ? "vacuum_freeze" : "solidify";
    for (GTConstructRecipes.SolidifierPart part : parts) {
      writePart(provider, plan, part, recipeTypeName);
    }
  }

  private static void writePart(Consumer<FinishedRecipe> provider, GTConstructRecipePlan plan,
                                GTConstructRecipes.SolidifierPart part, String recipeTypeName) {
    if (part == null) {
      throw new IllegalStateException("Solidifier part cannot be null");
    }
    if (part.materialCost() <= 0) {
      throw new IllegalStateException("Solidifier part '" + part.path() + "' must have a positive material cost");
    }

    int amount = multiplyExact(part.materialCost(), L, "fluid amount", part);
    int duration = multiplyExact(
      multiplyExact(part.materialCost(), plan.durationMultiplier(), "duration", part),
      20,
      "duration",
      part
    );
    FluidIngredient ingredient = createFluidIngredient(plan, amount);
    String recipePath = part.recipePath(recipeTypeName, inputName(plan));
    Item toolPart = getRegisteredPartItem(part);
    MaterialVariantId baseMaterial = plan.baseMaterial();
    CastItemObject cast = null;
    if (baseMaterial == null) {
      cast = part.cast();
      if (cast == null) {
        throw new IllegalStateException(
          "Solidifier part " + part.path() + " requires a cast when no base material is set"
        );
      }
      validateRegisteredCast(cast, part);
    }

    GTRecipeBuilder builder = selectedRecipeType(plan).recipeBuilder(recipePath)
      .outputItems(getToolStack(toolPart, plan.outputMaterial()))
      .duration(duration)
      .EUt(VA[plan.voltage()])
      .inputFluids(ingredient);

    if (baseMaterial != null) {
      builder.inputItems(getToolStack(toolPart, baseMaterial));
    } else {
      builder.notConsumable(cast);
    }
    builder.save(provider);
  }

  private static int multiplyExact(int left, int right, String valueName,
                                   GTConstructRecipes.SolidifierPart part) {
    try {
      return Math.multiplyExact(left, right);
    } catch (ArithmeticException exception) {
      throw new IllegalStateException(
        "Solidifier part '" + part.path() + "' has an overflowing " + valueName,
        exception
      );
    }
  }

  private static Item getRegisteredPartItem(GTConstructRecipes.SolidifierPart part) {
    Item item = Objects.requireNonNull(
      Objects.requireNonNull(part.part(), "Solidifier part '" + part.path() + "' has no item supplier").get(),
      "Solidifier part '" + part.path() + "' has no item"
    );
    try {
      if (ForgeRegistries.ITEMS.getDelegate(item).isEmpty()) {
        throw new IllegalStateException("Solidifier part '" + part.path() + "' uses an unregistered item");
      }
    } catch (IllegalArgumentException exception) {
      throw new IllegalStateException("Solidifier part '" + part.path() + "' uses an unregistered item", exception);
    }
    return item;
  }

  private static void validateRegisteredCast(CastItemObject cast, GTConstructRecipes.SolidifierPart part) {
    Item castItem = Objects.requireNonNull(cast.get(), "Solidifier part '" + part.path() + "' has an empty cast");
    try {
      if (ForgeRegistries.ITEMS.getDelegate(castItem).isEmpty()) {
        throw new IllegalStateException("Solidifier part '" + part.path() + "' uses an unregistered cast");
      }
    } catch (IllegalArgumentException exception) {
      throw new IllegalStateException("Solidifier part '" + part.path() + "' uses an unregistered cast", exception);
    }
  }

  private static FluidIngredient createFluidIngredient(GTConstructRecipePlan plan, int amount) {
    if (plan.inputFluidTag() != null) {
      return FluidIngredient.of(plan.inputFluidTag(), amount);
    }
    return FluidIngredient.of(new FluidStack(Objects.requireNonNull(plan.inputFluid()), amount));
  }

  private static String inputName(GTConstructRecipePlan plan) {
    String inputName;
    if (plan.inputFluid() != null) {
      ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(plan.inputFluid());
      if (fluidId == null) {
        throw new IllegalStateException("Input fluid is not registered: " + plan.inputFluid());
      }
      inputName = fluidId.getPath();
    } else {
      inputName = plan.inputFluidTag().location().getPath();
    }
    if (plan.sourceRecipeId() == null) {
      return inputName;
    }
    ResourceLocation sourceRecipeId = plan.sourceRecipeId();
    String encodedSourceId = Base64.getUrlEncoder().withoutPadding()
      .encodeToString(sourceRecipeId.toString().getBytes(StandardCharsets.UTF_8));
    return inputName + "_" + encodedSourceId;
  }

  private static com.gregtechceu.gtceu.api.recipe.GTRecipeType selectedRecipeType(GTConstructRecipePlan plan) {
    return plan.useVacuum() ? GTRecipeTypes.VACUUM_RECIPES : GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES;
  }

  private static ItemStack getToolStack(Item toolPart, MaterialVariantId material) {
    ItemStack stack = new ItemStack(toolPart);
    stack.getOrCreateTag().putString("Material", material.toString());
    return stack;
  }
}
