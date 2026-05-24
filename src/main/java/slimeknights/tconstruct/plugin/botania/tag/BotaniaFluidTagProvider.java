package slimeknights.tconstruct.plugin.botania.tag;

import slimeknights.tconstruct.library.addon.DynamicTagProviderRegistrar;
import slimeknights.tconstruct.plugin.botania.smeltery.BotaniaSmelteryCompat;

/**
 * Botania fluid tag hooks appended to TiC's core tag providers.
 */
public final class BotaniaFluidTagProvider {
  private BotaniaFluidTagProvider() {}

  public static void addTags(DynamicTagProviderRegistrar.FluidTagRegistrar tags) {
    BotaniaSmelteryCompat.INSTANCE.addFluidTags(tags);
  }
}
