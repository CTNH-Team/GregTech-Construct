package slimeknights.tconstruct.plugin.botania.material;

import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.data.resource.TiCDynamicResourceGenerator;
import slimeknights.tconstruct.library.client.data.material.MaterialPartTextureGenerator;
import slimeknights.tconstruct.tools.data.sprite.TinkerPartSpriteProvider;

/**
 * Generates TiC part textures for Botania-owned materials only.
 */
public class BotaniaMaterialPartTextureGenerator extends MaterialPartTextureGenerator {
  public BotaniaMaterialPartTextureGenerator(PackOutput packOutput) {
    super(packOutput, TiCDynamicResourceGenerator.createExistingFileHelperForAddons(), new TinkerPartSpriteProvider(), new BotaniaMaterialSpriteProvider());
  }
}
