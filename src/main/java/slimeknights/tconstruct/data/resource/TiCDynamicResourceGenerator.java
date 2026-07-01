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

  public static void register() {
    register(TiCDynamicResourceRegistrar.INSTANCE);
  }

  public static synchronized void addProvider(String name, Consumer<DynamicResourceRegistrar> writer) {
    ADDITIONAL_PROVIDER_ENTRIES.add(new ResourceProviderEntry(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(writer, "writer")
    ));
  }

  static void register(DynamicResourceRegistrar registrar) {
    for (ResourceProviderEntry entry : createProviderEntries()) {
      try {
        entry.writer().accept(registrar);
      } catch (Exception exception) {
        throw new IllegalStateException("Failed to run dynamic resource writer '" + entry.name() + "'", exception);
      }
    }
  }

  public static ExistingFileHelper createExistingFileHelperForAddons() {
    return ResourceProviderState.createExistingFileHelper();
  }

  public static void registerDefaultProviders(DynamicResourceProviderRegistrar registrar) {
    ResourceProviderStateHolder stateHolder = new ResourceProviderStateHolder();
    registrar.addResourceProvider(ModelSpriteProvider.class, output -> stateHolder.get().createModelSpriteProvider(output));
    registrar.addResourceProvider(TinkerSpriteSourceProvider.class, output -> stateHolder.get().createSpriteSourceProvider(output));
    registrar.addResourceProvider(TinkerItemModelProvider.class, output -> stateHolder.get().createItemModelProvider(output));
    registrar.addResourceProvider(TinkerBlockStateProvider.class, output -> stateHolder.get().createBlockStateProvider(output));
    registrar.addResourceProvider(RenderFluidProvider.class);
    registrar.addResourceProvider(RenderItemProvider.class);
    registrar.addResourceProvider(FluidTooltipProvider.class);
    registrar.addResourceWriter(FluidTextureProvider.class, registrar1 -> writeFluidTextures(registrar1, stateHolder.get().createFluidTextureProvider(DynamicPackOutput.dummy())));
    registrar.addResourceWriter(FluidTextureCameraProvider.class, registrar1 -> writeFluidTextureCameras(registrar1, stateHolder.get().existingFileHelper, stateHolder.get().createFluidTextureProvider(DynamicPackOutput.dummy())));
    registrar.addResourceProvider(FluidBucketModelProvider.class, output -> new FluidBucketModelProvider(output, TConstruct.MOD_ID));
    registrar.addResourceProvider(FluidBlockstateModelProvider.class, output -> new FluidBlockstateModelProvider(output, TConstruct.MOD_ID));
    registrar.addResourceProvider(ToolItemModelProvider.class, output -> stateHolder.get().createToolItemModelProvider(output));
    registrar.addResourceProvider(MaterialRenderInfoProvider.class, output -> stateHolder.get().createMaterialRenderInfoProvider(output));
    registrar.addResourceProvider(GeneratorPartTextureJsonGenerator.class, output -> stateHolder.get().createGeneratorPartTextureJsonGenerator(output));
    registrar.addResourceProvider(MaterialPartTextureGenerator.class, output -> stateHolder.get().createMaterialPartTextureGenerator(output));
    registrar.addResourceProvider(MaterialPaletteDebugGenerator.class, output -> stateHolder.get().createMaterialPaletteDebugGenerator(output));
    registrar.addResourceProvider(ArmorModelProvider.class);
    registrar.addResourceProvider(TrimMaterialPaletteGenerator.class, output -> stateHolder.get().createTrimMaterialPaletteGenerator(output));
  }

  static List<ResourceProviderEntry> createProviderEntries() {
    List<ResourceProviderEntry> providers = new ArrayList<>();
    TiCAddonRegistry.collectResourceProviders((name, writer) -> providers.add(new ResourceProviderEntry(name, writer)));
    synchronized (TiCDynamicResourceGenerator.class) {
      providers.addAll(ADDITIONAL_PROVIDER_ENTRIES);
    }
    return List.copyOf(providers);
  }

  public static Consumer<DynamicResourceRegistrar> runtime(RuntimeResourceProviderFactory<? extends RuntimeResourceProvider> factory) {
    return registrar -> factory.create(DynamicPackOutput.dummy()).addToDynamicPack(registrar);
  }

  /**
   * Creates a runtime resource provider using the GTM-style dummy {@link PackOutput}.
   * The output exists only for Forge/Mantle builder construction; runtime writes must
   * go through {@link DynamicResourceRegistrar}.
   */
  @FunctionalInterface
  public interface RuntimeResourceProviderFactory<T extends RuntimeResourceProvider> extends DynamicPackProviderFactory<T> {}

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

  private static final class ResourceProviderStateHolder {
    private ResourceProviderState state;

    private ResourceProviderState get() {
      if (state == null) {
        state = new ResourceProviderState();
      }
      return state;
    }
  }
}
