package slimeknights.tconstruct.data.resource;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.addon.DynamicResourceRegistrar;

import java.util.List;

public final class RuntimeResourceWriter {
  private static final FileToIdConverter ATLAS_ID_CONVERTER = FileToIdConverter.json("atlases");

  private RuntimeResourceWriter() {}

  public static <T extends ModelBuilder<T>> void writeModels(ModelProvider<T> provider, DynamicResourceRegistrar registrar) {
    for (T model : provider.generatedModels.values()) {
      registrar.addModel(model.getLocation(), model.toJson());
    }
  }

  public static void addAtlasSpriteSourceList(DynamicResourceRegistrar registrar, ResourceLocation atlas, List<SpriteSource> sources) {
    ResourceLocation location = ATLAS_ID_CONVERTER.idToFile(atlas);
    JsonElement json = SpriteSources.FILE_CODEC.encodeStart(JsonOps.INSTANCE, sources)
      .getOrThrow(false, error -> TConstruct.LOG.error("Failed to encode atlas sprite source: {}", error));
    registrar.addResource(location, json);
  }
}
