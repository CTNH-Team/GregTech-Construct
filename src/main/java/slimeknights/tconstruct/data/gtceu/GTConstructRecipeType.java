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

public final class GTConstructRecipeType {

  private GTConstructRecipeType() {}

  public static DynamicRecipeBuilder builder() {
    return new DynamicRecipeBuilder();
  }

  public static class DynamicRecipeBuilder {
    private Fluid inputFluid;

    private TagKey<Fluid> inputFluidTag;
    private MaterialVariantId baseMaterial;
    private MaterialVariantId outputMaterial;
    private ResourceLocation sourceRecipeId;
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
    DynamicRecipeBuilder baseMaterialVariant(MaterialVariantId material) { this.baseMaterial = material; return this; }
    DynamicRecipeBuilder sourceRecipeId(ResourceLocation recipeId) { this.sourceRecipeId = recipeId; return this; }
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
      GTConstructRecipePlan plan = GTConstructRecipePlan.create(
        inputFluid, inputFluidTag, baseMaterial, outputMaterial, sourceRecipeId, voltage, durationMultiplier, useVacuum
      );
      GTConstructRecipeWriter.write(provider, plan, parts);
    }
  }
}
