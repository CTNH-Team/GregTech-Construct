package slimeknights.tconstruct.library.addon;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.fluid.texture.AbstractFluidTextureProvider;
import slimeknights.tconstruct.data.pack.DynamicPackOutput;
import slimeknights.tconstruct.data.resource.TiCDynamicResourceGenerator;

import java.util.Objects;

/**
 * Shared resource-provider state for addon fluid textures and their camera textures.
 */
public final class AddonFluidTextureProviderSet<T extends AbstractFluidTextureProvider> {
  private final FluidTextureProviderFactory<T> fluidTextureProviderFactory;
  private ExistingFileHelper existingFileHelper;
  private T fluidTextures;

  private AddonFluidTextureProviderSet(FluidTextureProviderFactory<T> fluidTextureProviderFactory) {
    this.fluidTextureProviderFactory = Objects.requireNonNull(fluidTextureProviderFactory, "fluidTextureProviderFactory");
  }

  public static <T extends AbstractFluidTextureProvider> AddonFluidTextureProviderSet<T> create(FluidTextureProviderFactory<T> fluidTextureProviderFactory) {
    return new AddonFluidTextureProviderSet<>(fluidTextureProviderFactory);
  }

  public T fluidTextures(PackOutput output) {
    if (fluidTextures == null) {
      fluidTextures = fluidTextureProviderFactory.create(output);
    }
    return fluidTextures;
  }

  public <P extends DataProvider> P cameraProvider(PackOutput output, CameraProviderFactory<T,P> factory) {
    return factory.create(output, existingFileHelper(), fluidTextures(output));
  }

  public void addFluidTextures(DynamicResourceRegistrar registrar) {
    TiCDynamicResourceGenerator.writeFluidTextures(registrar, fluidTextures(DynamicPackOutput.dummy()));
  }

  public void addCameraTextures(DynamicResourceRegistrar registrar) {
    TiCDynamicResourceGenerator.writeFluidTextureCameras(registrar, existingFileHelper(), fluidTextures(DynamicPackOutput.dummy()));
  }

  private ExistingFileHelper existingFileHelper() {
    if (existingFileHelper == null) {
      existingFileHelper = TiCDynamicResourceGenerator.createExistingFileHelperForAddons();
    }
    return existingFileHelper;
  }

  @FunctionalInterface
  public interface FluidTextureProviderFactory<T extends AbstractFluidTextureProvider> {
    T create(PackOutput output);
  }

  @FunctionalInterface
  public interface CameraProviderFactory<T extends AbstractFluidTextureProvider, P extends DataProvider> {
    P create(PackOutput output, ExistingFileHelper existingFileHelper, T fluidTextures);
  }
}
