package slimeknights.tconstruct.library.addon;

import net.minecraft.data.PackOutput;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.plugin.botania.fluid.BotaniaFluidTextureCameraProvider;
import slimeknights.tconstruct.plugin.botania.fluid.BotaniaFluidTextureProvider;
import slimeknights.tconstruct.test.BaseMcTest;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AddonFluidTextureProviderSetTest extends BaseMcTest {
  @Test
  void createsFluidTextureProviderOnceAndSharesItWithCameraProvider() {
    AddonFluidTextureProviderSet<BotaniaFluidTextureProvider> set = AddonFluidTextureProviderSet.create(BotaniaFluidTextureProvider::new);
    PackOutput output = new PackOutput(Path.of("build", "test-addon-fluid-texture-provider-set"));

    BotaniaFluidTextureProvider first = set.fluidTextures(output);
    BotaniaFluidTextureProvider second = set.fluidTextures(output);
    BotaniaFluidTextureCameraProvider cameras = set.cameraProvider(output, BotaniaFluidTextureCameraProvider::new);

    assertThat(second).isSameAs(first);
    assertThat(cameras.getName()).isEqualTo("Tinkers' Construct Botania Fluid Texture Cameras");
  }
}
