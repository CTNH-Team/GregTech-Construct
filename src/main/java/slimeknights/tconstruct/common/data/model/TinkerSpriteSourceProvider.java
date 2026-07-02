package slimeknights.tconstruct.common.data.model;

import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.data.resource.RuntimeResourceWriter;
import slimeknights.tconstruct.library.addon.DynamicResourceRegistrar;
import slimeknights.tconstruct.library.client.modifiers.TrimModifierModel;
import slimeknights.tconstruct.library.data.RuntimeResourceProvider;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provider to stitch textures from additional folders
 */
public class TinkerSpriteSourceProvider extends SpriteSourceProvider implements RuntimeResourceProvider {
  /** List of trim variants supported, must all exist in vanilla */
  private static final String[] TRIMS = {
    "coast", "sentry", "dune", "wild", "ward", "eye", "vex", "tide", "snout",
    "rib", "spire", "wayfinder", "shaper", "silence", "raiser", "host"
  };

  public TinkerSpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper) {
    super(output, fileHelper, TConstruct.MOD_ID);
  }

  @Override
  protected void addSources() {
    buildSources().forEach((atlas, sources) -> {
      SourceList list = atlas(atlas);
      sources.forEach(list::addSource);
    });
  }

  @Override
  public void addToDynamicPack(DynamicResourceRegistrar registrar) {
    buildSources().forEach((atlas, sources) -> RuntimeResourceWriter.addAtlasSpriteSourceList(registrar, atlas, sources));
  }

  private Map<ResourceLocation, List<net.minecraft.client.renderer.texture.atlas.SpriteSource>> buildSources() {
    String paletteFolder = "trims/color_palettes/";
    String trimFolder = "trims/models/armor/";
    ResourceLocation trimPalette = ResourceLocation.tryParse(paletteFolder + "trim_palette");
    // map of material suffix to material paeltte for trims
    Map<String,ResourceLocation> materialMap = Arrays.stream(MaterialIds.TRIM_MATERIALS).collect(Collectors.toMap(id -> id.getNamespace() + "_" + id.getPath(), id -> id.withPrefix(paletteFolder)));

    List<net.minecraft.client.renderer.texture.atlas.SpriteSource> blocks = new ArrayList<>();
    blocks.add(directory("fluid"));
    blocks.add(directory("gui/modifiers"));
    blocks.add(directory("gui/tinker_pattern"));
    blocks.add(new PalettedPermutations(
      List.of(TrimModifierModel.TRIM_TEXTURES),
      trimPalette, materialMap));
    // add untinted trim textures, we use them as fallbacks
    for (ResourceLocation name : TrimModifierModel.TRIM_TEXTURES) {
      blocks.add(new SingleFile(name, Optional.empty()));
    }
    Map<ResourceLocation, List<net.minecraft.client.renderer.texture.atlas.SpriteSource>> sources = new LinkedHashMap<>();
    sources.put(BLOCKS_ATLAS, blocks);
    sources.put(ResourceLocation.tryParse("armor_trims"), List.of(new PalettedPermutations(
      Arrays.stream(TRIMS).flatMap(name -> Stream.of(ResourceLocation.tryParse(trimFolder + name), ResourceLocation.tryParse(trimFolder + name + "_leggings"))).toList(),
      trimPalette, materialMap)));
    return sources;
  }

  /** Creates a directory lister where the source matches the prefix. */
  private static DirectoryLister directory(String path) {
    return new DirectoryLister(path, path + '/');
  }
}
