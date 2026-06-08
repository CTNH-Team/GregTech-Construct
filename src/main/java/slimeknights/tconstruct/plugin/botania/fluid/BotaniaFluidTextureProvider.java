package slimeknights.tconstruct.plugin.botania.fluid;

import slimeknights.tconstruct.plugin.botania.smeltery.BotaniaSmelteryCompat;

import net.minecraft.data.PackOutput;

import slimeknights.mantle.fluid.texture.AbstractFluidTextureProvider;

/**
 * Botania-owned molten fluid textures.
 */
public class BotaniaFluidTextureProvider extends AbstractFluidTextureProvider {

    public BotaniaFluidTextureProvider(PackOutput packOutput) {
        super(packOutput, null);
    }

    @Override
    public void addTextures() {
        BotaniaSmelteryCompat.INSTANCE.addFluidTextures(this);
    }

    @Override
    public String getName() {
        return "Tinkers' Construct Botania Fluid Texture Providers";
    }
}
