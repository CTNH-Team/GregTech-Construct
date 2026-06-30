package slimeknights.tconstruct.data.pack;

import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.addon.DynamicDataRegistrar;

public enum TiCDynamicDataRegistrar implements DynamicDataRegistrar {
  INSTANCE;

  @Override
  public void addData(ResourceLocation location, byte[] bytes) {
    TiCDynamicDataPack.addData(location, bytes);
  }

  @Override
  public void addFilter(ResourceLocation location) {
    TiCDynamicDataPack.addFilter(location);
  }

  @Override
  public void addRecipeFilter(ResourceLocation recipeId) {
    TiCDynamicDataPack.addRecipeFilter(recipeId);
  }
}
