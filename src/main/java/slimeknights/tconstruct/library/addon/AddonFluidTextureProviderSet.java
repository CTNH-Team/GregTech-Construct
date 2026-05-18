package slimeknights.tconstruct.library.addon;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.fluid.texture.AbstractFluidTextureProvider;
import slimeknights.tconstruct.data.resource.TiCDynamicResourceGenerator;

import java.util.Objects;
import java.util.function.Function;

/**
 * Shared resource-provider state for addon fluid textures and their camera textures.
 */
public final class AddonFluidTextureProviderSet<T extends AbstractFluidTextureProvider> {
  private final Function<PackOutput,T> fluidTextureProviderFactory;
  private ExistingFileHelper existingFileHelper;
  private T fluidTextures;

  private AddonFluidTextureProviderSet(Function<PackOutput,T> fluidTextureProviderFactory) {
    this.fluidTextureProviderFactory = Objects.requireNonNull(fluidTextureProviderFactory, "fluidTextureProviderFactory");
  }

  public static <T extends AbstractFluidTextureProvider> AddonFluidTextureProviderSet<T> create(Function<PackOutput,T> fluidTextureProviderFactory) {
    return new AddonFluidTextureProviderSet<>(fluidTextureProviderFactory);
  }

  public T fluidTextures(PackOutput output) {
    if (fluidTextures == null) {
      fluidTextures = fluidTextureProviderFactory.apply(output);
    }
    return fluidTextures;
  }

  public <P extends DataProvider> P cameraProvider(PackOutput output, CameraProviderFactory<T,P> factory) {
    return factory.create(output, existingFileHelper(), fluidTextures(output));
  }

  private ExistingFileHelper existingFileHelper() {
    if (existingFileHelper == null) {
      existingFileHelper = TiCDynamicResourceGenerator.createExistingFileHelperForAddons();
    }
    return existingFileHelper;
  }

  @FunctionalInterface
  public interface CameraProviderFactory<T extends AbstractFluidTextureProvider, P extends DataProvider> {
    P create(PackOutput output, ExistingFileHelper existingFileHelper, T fluidTextures);
  }
}
