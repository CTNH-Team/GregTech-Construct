package slimeknights.tconstruct.plugin.botania.fluid;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.fluid.texture.FluidTextureCameraProvider;

/**
 * Botania-owned molten fluid camera texture generator.
 */
public class BotaniaFluidTextureCameraProvider extends FluidTextureCameraProvider {
  public BotaniaFluidTextureCameraProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper, BotaniaFluidTextureProvider provider) {
    super(packOutput, existingFileHelper, provider);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Botania Fluid Texture Cameras";
  }
}
