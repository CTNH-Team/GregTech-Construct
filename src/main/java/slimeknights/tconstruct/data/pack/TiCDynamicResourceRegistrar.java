package slimeknights.tconstruct.data.pack;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.addon.DynamicResourceRegistrar;

public enum TiCDynamicResourceRegistrar implements DynamicResourceRegistrar {
  INSTANCE;

  @Override
  public void addResource(ResourceLocation location, byte[] bytes) {
    TiCDynamicResourcePack.addResource(location, bytes);
  }

  @Override
  public void addTexture(ResourceLocation location, byte[] bytes) {
    TiCDynamicResourcePack.addTexture(location, bytes);
  }

  @Override
  public void addModel(ResourceLocation location, JsonElement json) {
    TiCDynamicResourcePack.addModel(location, json);
  }

  @Override
  public void addBlockModel(ResourceLocation location, JsonElement json) {
    TiCDynamicResourcePack.addBlockModel(location, json);
  }

  @Override
  public void addItemModel(ResourceLocation location, JsonElement json) {
    TiCDynamicResourcePack.addItemModel(location, json);
  }

  @Override
  public void addBlockState(ResourceLocation location, JsonElement json) {
    TiCDynamicResourcePack.addBlockState(location, json);
  }
}
