package slimeknights.tconstruct.data.gtceu;

import com.gregtechceu.gtceu.api.recipe.ingredient.fluid.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;

public final class GTConstructRecipeType {

  private GTConstructRecipeType() {}

  public static DynamicRecipeBuilder builder() {
    return new DynamicRecipeBuilder();
  }

  public static class DynamicRecipeBuilder {
    private Fluid inputFluid;

    private TagKey<Fluid> inputFluidTag;
    private MaterialId baseMaterial;
    private MaterialVariantId outputMaterial;
    private int voltage = LV;
    private int durationMultiplier = 1;
    private boolean useVacuum = false;

    private DynamicRecipeBuilder() {}

    public DynamicRecipeBuilder inputFluids(Fluid fluid) {
      this.inputFluid = fluid;
      return this;
    }

    public DynamicRecipeBuilder inputFluidTag(TagKey<Fluid> tag) {
      this.inputFluidTag = tag;
      return this;
    }

    public DynamicRecipeBuilder baseMaterial(MaterialId material) { this.baseMaterial = material; return this; }
    public DynamicRecipeBuilder outputMaterial(MaterialVariantId material) { this.outputMaterial = material; return this; }
    public DynamicRecipeBuilder voltage(int voltageTier) { this.voltage = voltageTier; if (voltageTier > LV) { this.useVacuum = true; } return this; }
    public DynamicRecipeBuilder duration(int secondsPerIngot) { this.durationMultiplier = secondsPerIngot; return this; }
    public DynamicRecipeBuilder inVacuumFreezer() { this.useVacuum = true; return this; }
    public DynamicRecipeBuilder inSolidifier() { this.useVacuum = false; return this; }

    public void register(Consumer<FinishedRecipe> provider) {
      register(provider, outputMaterial == null
        ? List.of()
        : GTConstructRecipes.getSupportedParts(outputMaterial.getId(), GTConstructRecipes.getDefaultSolidifierParts()));
    }

    public void register(Consumer<FinishedRecipe> provider, List<GTConstructRecipes.SolidifierPart> parts) {
      if (inputFluid == null && inputFluidTag == null || outputMaterial == null) {
        throw new IllegalStateException("InputFluid (or Tag) and OutputMaterial must be set!");
      }

      if (parts.isEmpty()) {
        return;
      }

      String recipeTypeName = useVacuum ? "vacuum_freeze" : "solidify";

      for (GTConstructRecipes.SolidifierPart part : parts) {
        registerPart(provider, part, recipeTypeName);
      }
    }

    private void registerPart(Consumer<FinishedRecipe> provider, GTConstructRecipes.SolidifierPart part, String recipeTypeName) {
      FluidIngredient ingredient;
      int amount = part.materialCost() * L;

      if (this.inputFluidTag != null) {
        ingredient = FluidIngredient.of(this.inputFluidTag, amount);
      } else {
        ingredient = FluidIngredient.of(new FluidStack(this.inputFluid, amount));
      }

      int duration = part.materialCost() * durationMultiplier * 20;

      var recipeType = useVacuum ? GTRecipeTypes.VACUUM_RECIPES : GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES;

      String fluidNamePath = (this.inputFluid != null)
        ? ForgeRegistries.FLUIDS.getKey(this.inputFluid).getPath()
        : this.inputFluidTag.location().getPath();

      String recipePath = part.recipePath(recipeTypeName, fluidNamePath);
      Item toolPart = part.part().get();

      if (baseMaterial != null) {
        MaterialVariantId baseMaterialVariantId = MaterialVariantId.tryParse(baseMaterial.toString());
        recipeType.recipeBuilder(recipePath)
          .outputItems(getToolStack(toolPart, outputMaterial))
          .duration(duration)
          .EUt(VA[voltage])
          .inputFluids(ingredient)
          .inputItems(getToolStack(toolPart, baseMaterialVariantId))
          .save(provider);
      } else {
        CastItemObject cast = part.cast();
        if (cast == null) {
          throw new IllegalStateException("Solidifier part " + part.path() + " requires a cast when no base material is set");
        }
        recipeType.recipeBuilder(recipePath)
          .outputItems(getToolStack(toolPart, outputMaterial))
          .duration(duration)
          .EUt(VA[voltage])
          .inputFluids(ingredient)
          .notConsumable(cast)
          .save(provider);
      }
    }
  }

  private static ItemStack getToolStack(net.minecraft.world.item.Item toolPart, MaterialVariantId matVariantId) {
    ItemStack stack = new ItemStack(toolPart);
    stack.getOrCreateTag().putString("Material", matVariantId.toString());
    return stack;
  }
}
