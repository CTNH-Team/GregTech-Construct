package slimeknights.tconstruct.plugin.botania;

import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.library.client.data.material.MaterialPaletteDebugGenerator;

/**
 * Debug palettes for Botania-owned material sprite mappings.
 */
public class BotaniaMaterialPaletteDebugGenerator extends MaterialPaletteDebugGenerator {
  public BotaniaMaterialPaletteDebugGenerator(PackOutput packOutput) {
    super(packOutput, BotaniaTiCAddon.MOD_ID, new BotaniaMaterialSpriteProvider());
  }
}
