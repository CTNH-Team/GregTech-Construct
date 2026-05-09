package slimeknights.tconstruct.data.resource;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import slimeknights.mantle.fluid.texture.FluidTextureCameraProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.data.model.ModelSpriteProvider;
import slimeknights.tconstruct.common.data.model.TinkerBlockStateProvider;
import slimeknights.tconstruct.common.data.model.TinkerItemModelProvider;
import slimeknights.tconstruct.common.data.model.TinkerSpriteSourceProvider;
import slimeknights.tconstruct.common.data.render.RenderFluidProvider;
import slimeknights.tconstruct.common.data.render.RenderItemProvider;
import slimeknights.tconstruct.data.pack.DynamicResourceProviderRunner;
import slimeknights.tconstruct.fluids.data.FluidBlockstateModelProvider;
import slimeknights.tconstruct.fluids.data.FluidBucketModelProvider;
import slimeknights.tconstruct.fluids.data.FluidTextureProvider;
import slimeknights.tconstruct.fluids.data.FluidTooltipProvider;
import slimeknights.tconstruct.library.client.data.material.MaterialPaletteDebugGenerator;
import slimeknights.tconstruct.library.client.data.material.MaterialPartTextureGenerator;
import slimeknights.tconstruct.library.client.data.material.TrimMaterialPaletteGenerator;
import slimeknights.tconstruct.library.client.data.material.GeneratorPartTextureJsonGenerator;
import slimeknights.tconstruct.tools.data.ArmorModelProvider;
import slimeknights.tconstruct.tools.data.ToolItemModelProvider;
import slimeknights.tconstruct.tools.data.material.MaterialRenderInfoProvider;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.data.sprite.TinkerMaterialSpriteProvider;
import slimeknights.tconstruct.tools.data.sprite.TinkerPartSpriteProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.nio.file.Files;
import java.nio.file.Path;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToColorMapping;
import slimeknights.tconstruct.library.client.data.spritetransformer.ISpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.RecolorSpriteTransformer;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

public final class TiCDynamicResourceGenerator {
  private TiCDynamicResourceGenerator() {}

  public static void register() {
    register(DynamicResourceProviderRunner::run);
  }

  static void register(ResourceRunner runner) {
    runner.run("tconstruct-client-resources", createProviders());
  }

  static List<Function<PackOutput, ? extends DataProvider>> createProviders() {
    ResourceProviderStateHolder stateHolder = new ResourceProviderStateHolder();
    return List.of(
      output -> stateHolder.get(output).createModelSpriteProvider(output),
      output -> stateHolder.get(output).createSpriteSourceProvider(output),
      output -> stateHolder.get(output).createItemModelProvider(output),
      output -> stateHolder.get(output).createBlockStateProvider(output),
      RenderFluidProvider::new,
      RenderItemProvider::new,
      FluidTooltipProvider::new,
      output -> stateHolder.get(output).createFluidTextureProvider(output),
      output -> stateHolder.get(output).createFluidTextureCameraProvider(output),
      output -> new FluidBucketModelProvider(output, TConstruct.MOD_ID),
      output -> new FluidBlockstateModelProvider(output, TConstruct.MOD_ID),
      output -> stateHolder.get(output).createToolItemModelProvider(output),
      output -> stateHolder.get(output).createMaterialRenderInfoProvider(output),
      output -> stateHolder.get(output).createGeneratorPartTextureJsonGenerator(output),
      output -> stateHolder.get(output).createMaterialPartTextureGenerator(output),
      output -> stateHolder.get(output).createMaterialPaletteDebugGenerator(output),
      ArmorModelProvider::new,
      output -> stateHolder.get(output).createTrimMaterialPaletteGenerator(output)
    );
  }

  @FunctionalInterface
  interface ResourceRunner {
    void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers);
  }

  private static final class ResourceProviderState {
    private final ExistingFileHelper existingFileHelper;
    private final TinkerMaterialSpriteProvider materialSprites = new TinkerMaterialSpriteProvider();
    private final TinkerPartSpriteProvider partSprites = new TinkerPartSpriteProvider();
    private final FluidTextureProvider fluidTextureProvider;

    private ResourceProviderState(PackOutput output) {
      this.existingFileHelper = createExistingFileHelper();
      this.fluidTextureProvider = new FluidTextureProvider(output);
    }

    private ModelSpriteProvider createModelSpriteProvider(PackOutput output) {
      return new ModelSpriteProvider(output, existingFileHelper);
    }

    private TinkerSpriteSourceProvider createSpriteSourceProvider(PackOutput output) {
      return new TinkerSpriteSourceProvider(output, existingFileHelper);
    }

    private TinkerItemModelProvider createItemModelProvider(PackOutput output) {
      return new TinkerItemModelProvider(output, existingFileHelper);
    }

    private TinkerBlockStateProvider createBlockStateProvider(PackOutput output) {
      return new TinkerBlockStateProvider(output, existingFileHelper);
    }

    private ToolItemModelProvider createToolItemModelProvider(PackOutput output) {
      return new ToolItemModelProvider(output, existingFileHelper);
    }

    private MaterialRenderInfoProvider createMaterialRenderInfoProvider(PackOutput output) {
      return new MaterialRenderInfoProvider(output, materialSprites, existingFileHelper);
    }

    private FluidTextureProvider createFluidTextureProvider(PackOutput output) {
      return fluidTextureProvider;
    }

    private FluidTextureCameraProvider createFluidTextureCameraProvider(PackOutput output) {
      return new FluidTextureCameraProvider(output, existingFileHelper, fluidTextureProvider);
    }

    private GeneratorPartTextureJsonGenerator createGeneratorPartTextureJsonGenerator(PackOutput output) {
      return new GeneratorPartTextureJsonGenerator(output, TConstruct.MOD_ID, partSprites);
    }

    private MaterialPartTextureGenerator createMaterialPartTextureGenerator(PackOutput output) {
      return new MaterialPartTextureGenerator(output, existingFileHelper, partSprites, materialSprites);
    }

    private MaterialPaletteDebugGenerator createMaterialPaletteDebugGenerator(PackOutput output) {
      return new MaterialPaletteDebugGenerator(output, TConstruct.MOD_ID, materialSprites);
    }

    private TrimMaterialPaletteGenerator createTrimMaterialPaletteGenerator(PackOutput output) {
      return new TrimMaterialPaletteGenerator(output, TConstruct.MOD_ID, existingFileHelper, materialSprites, MaterialIds.TRIM_MATERIALS) {
        @Override
        protected ISpriteTransformer getTransformer(MaterialId material) {
          if (MaterialIds.queensSlime.equals(material)) {
            return new RecolorSpriteTransformer(GreyToColorMapping.builderFromBlack().addARGB(63, 0xFF5F1100).addARGB(102, 0xFF893200).addARGB(140, 0xFF966A03).addARGB(178, 0xFF8C9226).addARGB(216, 0xFF52BB53).addARGB(255, 0xFF5DD45F).build());
          }
          return super.getTransformer(material);
        }
      };
    }

    private static ExistingFileHelper createExistingFileHelper() {
      Path outputRoot = DynamicResourceProviderRunner.getOutputRootPath();
      try {
        Files.createDirectories(outputRoot);
      } catch (IOException exception) {
        throw new IllegalStateException("Failed to create dynamic resource output root for existing file helper", exception);
      }
      List<Path> existingPacks = new ArrayList<>();
      existingPacks.add(outputRoot);
      addIfExists(existingPacks, Path.of("src", "main", "resources"));
      addIfExists(existingPacks, Path.of("src", "generated", "resources"));
      Set<String> existingMods = new HashSet<>();
      ModList modList = ModList.get();
      if (modList != null) {
        modList.getMods().stream().map(IModInfo::getModId).forEach(existingMods::add);
        existingMods.add("minecraft");
      }
      return new ExistingFileHelper(existingPacks, existingMods, true, null, null);
    }

    private static void addIfExists(List<Path> packs, Path path) {
      if (Files.exists(path)) {
        packs.add(path);
      }
    }
  }

  private static final class ResourceProviderStateHolder {
    private ResourceProviderState state;

    private ResourceProviderState get(PackOutput output) {
      if (state == null) {
        state = new ResourceProviderState(output);
      }
      return state;
    }
  }
}
