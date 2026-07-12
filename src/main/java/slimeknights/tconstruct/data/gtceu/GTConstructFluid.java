package slimeknights.tconstruct.data.gtceu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Map;

public class GTConstructFluid {

  private GTConstructFluid() {}

  public static Map<ResourceLocation, Fluid> getAllTinkersFluids() {
    return TinkersFluidCatalog.instance().fluids();
  }

  public static TagKey<Fluid> getAutoTag(Fluid fluid) {
    return TinkersFluidCatalog.instance().recipeTag(fluid);
  }

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

  public static String extractMaterialName(String path) {
    return path;
  }
}
