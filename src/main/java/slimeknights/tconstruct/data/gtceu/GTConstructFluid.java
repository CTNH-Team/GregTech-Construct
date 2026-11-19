package slimeknights.tconstruct.data.gtceu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/** Tag selection shared by the fluid scanner and catalog; internal to the GTM generator package. */
final class GTConstructFluid {
  private GTConstructFluid() {}

  static TagKey<Fluid> selectRecipeTag(@Nullable TagKey<Fluid> commonTag, @Nullable TagKey<Fluid> localTag,
                                       ResourceLocation fluidId) {
    if (commonTag != null) {
      return commonTag;
    }
    if (localTag != null) {
      return localTag;
    }
    return TagKey.create(
      ForgeRegistries.FLUIDS.getRegistryKey(),
      ResourceLocation.tryBuild("forge", fluidId.getPath())
    );
  }
}
