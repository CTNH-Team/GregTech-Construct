package slimeknights.tconstruct.data.gtceu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import javax.annotation.Nullable;

import static com.gregtechceu.gtceu.api.GTValues.VA;

record GTConstructRecipePlan(@Nullable Fluid inputFluid,
                             @Nullable TagKey<Fluid> inputFluidTag,
                             @Nullable MaterialVariantId baseMaterial,
                             MaterialVariantId outputMaterial,
                             @Nullable ResourceLocation sourceRecipeId,
                             int voltage,
                             int durationMultiplier,
                             boolean useVacuum) {
  static GTConstructRecipePlan create(@Nullable Fluid inputFluid,
                                      @Nullable TagKey<Fluid> inputFluidTag,
                                      @Nullable MaterialVariantId baseMaterial,
                                      @Nullable MaterialVariantId outputMaterial,
                                      @Nullable ResourceLocation sourceRecipeId,
                                      int voltage,
                                      int durationMultiplier,
                                      boolean useVacuum) {
    if ((inputFluid == null) == (inputFluidTag == null)) {
      throw new IllegalStateException("Exactly one input fluid or fluid tag must be set");
    }
    if (outputMaterial == null) {
      throw new IllegalStateException("OutputMaterial must be set");
    }
    if (durationMultiplier <= 0) {
      throw new IllegalStateException("Duration multiplier must be positive: " + durationMultiplier);
    }
    if (voltage < 0 || voltage >= VA.length) {
      throw new IllegalStateException("Voltage tier is outside GTM's voltage table: " + voltage);
    }
    if (inputFluid != null) {
      try {
        if (ForgeRegistries.FLUIDS.getDelegate(inputFluid).isEmpty()) {
          throw new IllegalStateException("Input fluid is not registered");
        }
      } catch (IllegalArgumentException exception) {
        throw new IllegalStateException("Input fluid is not registered", exception);
      }
    }
    return new GTConstructRecipePlan(
      inputFluid, inputFluidTag, baseMaterial, outputMaterial, sourceRecipeId, voltage, durationMultiplier, useVacuum
    );
  }
}
