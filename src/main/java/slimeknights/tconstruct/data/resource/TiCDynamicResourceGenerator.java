package slimeknights.tconstruct.data.resource;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.fluid.texture.AbstractFluidTextureProvider;
import slimeknights.mantle.fluid.texture.FluidTexture;
import slimeknights.mantle.fluid.texture.FluidTextureCameraProvider;
import slimeknights.mantle.fluid.texture.FluidTextureManager;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.data.model.ModelSpriteProvider;
import slimeknights.tconstruct.common.data.model.TinkerBlockStateProvider;
import slimeknights.tconstruct.common.data.model.TinkerItemModelProvider;
import slimeknights.tconstruct.common.data.model.TinkerSpriteSourceProvider;
import slimeknights.tconstruct.common.data.render.RenderFluidProvider;
import slimeknights.tconstruct.common.data.render.RenderItemProvider;
import slimeknights.tconstruct.data.pack.DynamicPackOutput;
import slimeknights.tconstruct.data.pack.TiCDynamicResourceRegistrar;
import slimeknights.tconstruct.fluids.data.FluidBlockstateModelProvider;
import slimeknights.tconstruct.fluids.data.FluidBucketModelProvider;
import slimeknights.tconstruct.fluids.data.FluidTextureProvider;
import slimeknights.tconstruct.fluids.data.FluidTooltipProvider;
import slimeknights.tconstruct.library.addon.DynamicResourceProviderRegistrar;
import slimeknights.tconstruct.library.addon.DynamicResourceRegistrar;
import slimeknights.tconstruct.library.addon.DynamicPackProviderFactory;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;
import slimeknights.tconstruct.library.client.data.material.GeneratorPartTextureJsonGenerator;
import slimeknights.tconstruct.library.client.data.material.MaterialPaletteDebugGenerator;
import slimeknights.tconstruct.library.client.data.material.MaterialPartTextureGenerator;
import slimeknights.tconstruct.library.client.data.material.TrimMaterialPaletteGenerator;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToColorMapping;
import slimeknights.tconstruct.library.client.data.spritetransformer.ISpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.RecolorSpriteTransformer;
import slimeknights.tconstruct.library.client.data.util.DataGenSpriteReader;
import slimeknights.tconstruct.library.data.RuntimeResourceProvider;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.tools.data.ArmorModelProvider;
import slimeknights.tconstruct.tools.data.ToolItemModelProvider;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.data.material.MaterialRenderInfoProvider;
import slimeknights.tconstruct.tools.data.sprite.TinkerMaterialSpriteProvider;
import slimeknights.tconstruct.tools.data.sprite.TinkerPartSpriteProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class TiCDynamicResourceGenerator {
  static final List<ResourceProviderEntry> ADDITIONAL_PROVIDER_ENTRIES = new ArrayList<>();

  private TiCDynamicResourceGenerator() {}

  /**
   * Main entry point for dynamic resource generation.
   * Called from {@link slimeknights.tconstruct.library.events.RegisterDynamicResourcesEvent}.
   *
   * <p>GTM-style simplified approach: directly run providers instead of complex registration.
   */
  public static void register() {
    DynamicResourceRegistrar registrar = TiCDynamicResourceRegistrar.INSTANCE;

    // Create shared state for providers
    ResourceProviderState state = new ResourceProviderState();

    // Run core providers directly (GTM style)
    runBlockStatesAndModels(registrar, state);
    runSpriteProviders(registrar, state);
    runFluidProviders(registrar, state);
    runMaterialProviders(registrar, state);
    runToolProviders(registrar, state);

    // Allow addons to contribute
    runAddonProviders(registrar);
  }

  public static synchronized void addProvider(String name, Consumer<DynamicResourceRegistrar> writer) {
    ADDITIONAL_PROVIDER_ENTRIES.add(new ResourceProviderEntry(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(writer, "writer")
    ));
  }

  /** Run blockstates and models generation */
  private static void runBlockStatesAndModels(DynamicResourceRegistrar registrar, ResourceProviderState state) {
    try {
      TinkerBlockStateProvider provider = state.createBlockStateProvider(DynamicPackOutput.dummy());
      provider.addToDynamicPack(registrar);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate blockstates", e);
    }
  }

  /** Run sprite source generation */
  private static void runSpriteProviders(DynamicResourceRegistrar registrar, ResourceProviderState state) {
    try {
      state.createSpriteSourceProvider(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
      state.createModelSpriteProvider(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate sprites", e);
    }
  }

  /** Run fluid-related providers */
  private static void runFluidProviders(DynamicResourceRegistrar registrar, ResourceProviderState state) {
    try {
      new RenderFluidProvider(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
      new FluidTooltipProvider(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
      writeFluidTextures(registrar, state.fluidTextureProvider);
      writeFluidTextureCameras(registrar, state.existingFileHelper, state.fluidTextureProvider);
      new FluidBucketModelProvider(DynamicPackOutput.dummy(), TConstruct.MOD_ID).addToDynamicPack(registrar);
      new FluidBlockstateModelProvider(DynamicPackOutput.dummy(), TConstruct.MOD_ID).addToDynamicPack(registrar);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate fluid resources", e);
    }
  }

  /** Run material-related providers */
  private static void runMaterialProviders(DynamicResourceRegistrar registrar, ResourceProviderState state) {
    try {
      state.createMaterialRenderInfoProvider(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
      state.createGeneratorPartTextureJsonGenerator(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
      state.createMaterialPartTextureGenerator(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
      state.createMaterialPaletteDebugGenerator(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
      state.createTrimMaterialPaletteGenerator(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate material resources", e);
    }
  }

  /** Run tool-related providers */
  private static void runToolProviders(DynamicResourceRegistrar registrar, ResourceProviderState state) {
    try {
      state.createItemModelProvider(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
      state.createToolItemModelProvider(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
      new RenderItemProvider(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
      new ArmorModelProvider(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate tool resources", e);
    }
  }

  /** Run addon-contributed providers */
  private static void runAddonProviders(DynamicResourceRegistrar registrar) {
    List<ResourceProviderEntry> addonProviders = new ArrayList<>();
    TiCAddonRegistry.collectResourceProviders((name, writer) -> addonProviders.add(new ResourceProviderEntry(name, writer)));

    synchronized (TiCDynamicResourceGenerator.class) {
      addonProviders.addAll(ADDITIONAL_PROVIDER_ENTRIES);
    }

    for (ResourceProviderEntry entry : addonProviders) {
      try {
        entry.writer().accept(registrar);
      } catch (Exception exception) {
        throw new IllegalStateException("Failed to run addon resource writer '" + entry.name() + "'", exception);
      }
    }
  }

  public static ExistingFileHelper createExistingFileHelperForAddons() {
    return ResourceProviderState.createExistingFileHelper();
  }

  public static void writeFluidTextures(DynamicResourceRegistrar registrar, AbstractFluidTextureProvider provider) {
    provider.getAllTextures().forEach((type, builder) -> {
      ResourceLocation id = Objects.requireNonNull(ForgeRegistries.FLUID_TYPES.get().getKey(type));
      registrar.addResource(ResourceLocation.tryBuild(id.getNamespace(), FluidTextureManager.FOLDER + "/" + id.getPath() + ".json"), builder.build().serialize());
    });
  }

  public static void writeFluidTextureCameras(DynamicResourceRegistrar registrar, ExistingFileHelper existingFileHelper, AbstractFluidTextureProvider provider) {
    DataGenSpriteReader reader = new DataGenSpriteReader(existingFileHelper, "textures");
    try {
      for (FluidTexture.Builder builder : provider.getAllTextures().values()) {
        ResourceLocation camera = builder.getCamera();
        if (camera != null) {
          try (NativeImage image = reader.read(builder.getStill())) {
            NativeImage copy = new NativeImage(image.getWidth(), image.getWidth(), true);
            copy.copyFrom(image);
            try {
              registrar.addTexture(camera, copy.asByteArray());
            } finally {
              copy.close();
            }
          }
        }
      }
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to generate dynamic fluid camera textures", exception);
    } finally {
      reader.closeAll();
    }
  }

  record ResourceProviderEntry(String name, Consumer<DynamicResourceRegistrar> writer) {}

  private static final class ResourceProviderState {
    private final ExistingFileHelper existingFileHelper;
    private final TinkerMaterialSpriteProvider materialSprites = new TinkerMaterialSpriteProvider();
    private final TinkerPartSpriteProvider partSprites = new TinkerPartSpriteProvider();
    private final FluidTextureProvider fluidTextureProvider;

    private ResourceProviderState() {
      this.existingFileHelper = createExistingFileHelper();
      this.fluidTextureProvider = new FluidTextureProvider(DynamicPackOutput.dummy());
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
      return RuntimeExistingFileHelper.INSTANCE;
    }
  }
}
