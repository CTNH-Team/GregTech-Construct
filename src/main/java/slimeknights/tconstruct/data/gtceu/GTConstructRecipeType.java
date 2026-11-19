package slimeknights.tconstruct.data.gtceu;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.LV;

/**
 * Internal builder for one batch of GTM solidifier recipes. Package-private on purpose: this is
 * generation plumbing, not an addon API.
 */
final class GTConstructRecipeType {
  private GTConstructRecipeType() {}

  static DynamicRecipeBuilder builder() {
    return new DynamicRecipeBuilder();
  }

  static final class DynamicRecipeBuilder {
    private Fluid inputFluid;

    private TagKey<Fluid> inputFluidTag;
    private MaterialVariantId baseMaterial;
    private MaterialVariantId outputMaterial;
    private ResourceLocation sourceRecipeId;
    private int voltage = LV;
    private int durationMultiplier = 1;
    private boolean useVacuum = false;

    private DynamicRecipeBuilder() {}

    DynamicRecipeBuilder inputFluids(Fluid fluid) {
      this.inputFluid = fluid;
      return this;
    }

    DynamicRecipeBuilder inputFluidTag(TagKey<Fluid> tag) {
      this.inputFluidTag = tag;
      return this;
    }

    DynamicRecipeBuilder baseMaterial(MaterialId material) {
      this.baseMaterial = material;
      return this;
    }

    DynamicRecipeBuilder baseMaterialVariant(MaterialVariantId material) {
      this.baseMaterial = material;
      return this;
    }

    DynamicRecipeBuilder sourceRecipeId(ResourceLocation recipeId) {
      this.sourceRecipeId = recipeId;
      return this;
    }

    DynamicRecipeBuilder outputMaterial(MaterialVariantId material) {
      this.outputMaterial = material;
      return this;
    }

    DynamicRecipeBuilder voltage(int voltageTier) {
      this.voltage = voltageTier;
      if (voltageTier > LV) {
        this.useVacuum = true;
      }
      return this;
    }

    DynamicRecipeBuilder duration(int secondsPerIngot) {
      this.durationMultiplier = secondsPerIngot;
      return this;
    }

    DynamicRecipeBuilder inVacuumFreezer() {
      this.useVacuum = true;
      return this;
    }

    DynamicRecipeBuilder inSolidifier() {
      this.useVacuum = false;
      return this;
    }

    void register(Consumer<FinishedRecipe> provider) {
      register(provider, outputMaterial == null
        ? List.of()
        : GTConstructSolidifierRecipes.getSupportedParts(outputMaterial.getId(),
                                                        GTConstructSolidifierRecipes.getDefaultSolidifierParts()));
    }

    void register(Consumer<FinishedRecipe> provider, List<GTConstructSolidifierRecipes.SolidifierPart> parts) {
      GTConstructRecipePlan plan = GTConstructRecipePlan.create(
        inputFluid, inputFluidTag, baseMaterial, outputMaterial, sourceRecipeId, voltage, durationMultiplier, useVacuum
      );
      GTConstructRecipeWriter.write(provider, plan, parts);
    }
  }
}
