package slimeknights.tconstruct.library.addon;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.fluid.texture.AbstractFluidTextureProvider;
import slimeknights.tconstruct.test.BaseMcTest;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class AddonFluidTextureProviderSetTest extends BaseMcTest {
  @Test
  void createsFluidTextureProviderOnceAndSharesItWithCameraProvider() {
    AddonFluidTextureProviderSet<TestFluidTextureProvider> set = AddonFluidTextureProviderSet.create(TestFluidTextureProvider::new);
    PackOutput output = new PackOutput(Path.of("build", "test-addon-fluid-texture-provider-set"));

    TestFluidTextureProvider first = set.fluidTextures(output);
    TestFluidTextureProvider second = set.fluidTextures(output);
    TestCameraProvider cameras = set.cameraProvider(output, TestCameraProvider::new);

    assertThat(second).isSameAs(first);
    assertThat(cameras.fluidTextures()).isSameAs(first);
  }

  private static final class TestFluidTextureProvider extends AbstractFluidTextureProvider {
    private TestFluidTextureProvider(PackOutput output) {
      super(output, "test");
    }

    @Override
    public void addTextures() {}

    @Override
    public String getName() {
      return "Test Fluid Textures";
    }
  }

  private record TestCameraProvider(
    PackOutput output,
    ExistingFileHelper existingFileHelper,
    TestFluidTextureProvider fluidTextures
  ) implements DataProvider {
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
      return "Test Camera Provider";
    }
  }
}
