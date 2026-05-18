package slimeknights.tconstruct.plugin.botania.material;

import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.library.client.data.material.MaterialPaletteDebugGenerator;
import slimeknights.tconstruct.plugin.botania.BotaniaTiCAddon;

/**
 * Debug palettes for Botania-owned material sprite mappings.
 */
public class BotaniaMaterialPaletteDebugGenerator extends MaterialPaletteDebugGenerator {
  public BotaniaMaterialPaletteDebugGenerator(PackOutput packOutput) {
    super(packOutput, BotaniaTiCAddon.MOD_ID, new BotaniaMaterialSpriteProvider());
  }
}
