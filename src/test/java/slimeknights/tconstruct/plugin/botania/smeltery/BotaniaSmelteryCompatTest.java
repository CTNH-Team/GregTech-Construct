package slimeknights.tconstruct.plugin.botania.smeltery;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.fluid.texture.AbstractFluidTextureProvider;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.library.addon.AddonSmelteryCompat;
import slimeknights.tconstruct.test.BaseMcTest;

import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

class BotaniaSmelteryCompatTest extends BaseMcTest {
  @Test
  void fluidTextureHelperUsesCompatOrePaths() {
    CapturingFluidTextureProvider provider = new CapturingFluidTextureProvider();

    var manaSteel = AddonSmelteryCompat.compatOre(provider, fluid("manasteel")).build();
    var terraSteel = AddonSmelteryCompat.compatOre(provider, fluid("terrasteel")).build();

    assertThat(manaSteel.still()).isEqualTo(ResourceLocation.tryBuild("tconstruct", "fluid/compat_ore/manasteel/still"));
    assertThat(manaSteel.flowing()).isEqualTo(ResourceLocation.tryBuild("tconstruct", "fluid/compat_ore/manasteel/flowing"));
    assertThat(manaSteel.camera()).isEqualTo(ResourceLocation.tryBuild("tconstruct", "fluid/compat_ore/manasteel/camera"));
    assertThat(terraSteel.still()).isEqualTo(ResourceLocation.tryBuild("tconstruct", "fluid/compat_ore/terrasteel/still"));
    assertThat(terraSteel.flowing()).isEqualTo(ResourceLocation.tryBuild("tconstruct", "fluid/compat_ore/terrasteel/flowing"));
    assertThat(terraSteel.camera()).isEqualTo(ResourceLocation.tryBuild("tconstruct", "fluid/compat_ore/terrasteel/camera"));
  }

  private static FluidObject<Fluid> fluid(String name) {
    return new FluidObject<>(ResourceLocation.tryBuild("tconstruct", name), null, () -> new FluidType(FluidType.Properties.create()) {}, () -> null);
  }

  private static final class CapturingFluidTextureProvider extends AbstractFluidTextureProvider {
    private CapturingFluidTextureProvider() {
      super(new PackOutput(Path.of("build", "test-botania-fluid-texture-helper")), null);
    }

    @Override
    public void addTextures() {}

    @Override
    public String getName() {
      return "Capturing Fluid Texture Provider";
    }
  }
}
