package slimeknights.tconstruct.library.addon;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;

public interface DynamicResourceRegistrar {
  void addResource(ResourceLocation location, byte[] bytes);

  default void addResource(ResourceLocation location, JsonElement json) {
    addResource(location, json.toString().getBytes(StandardCharsets.UTF_8));
  }

  void addTexture(ResourceLocation location, byte[] bytes);

  void addModel(ResourceLocation location, JsonElement json);

  void addBlockModel(ResourceLocation location, JsonElement json);

  void addItemModel(ResourceLocation location, JsonElement json);

  void addBlockState(ResourceLocation location, JsonElement json);
}
