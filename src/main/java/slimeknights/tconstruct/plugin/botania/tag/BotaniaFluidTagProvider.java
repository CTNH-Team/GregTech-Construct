package slimeknights.tconstruct.plugin.botania.tag;

import slimeknights.tconstruct.plugin.botania.smeltery.BotaniaSmelteryCompat;

import slimeknights.tconstruct.library.addon.DatagenTagProviderRegistrar;

/**
 * Botania fluid tag hooks appended to TiC's core tag providers.
 */
public final class BotaniaFluidTagProvider {

    private BotaniaFluidTagProvider() {}

    public static void addTags(DatagenTagProviderRegistrar.FluidTagRegistrar tags) {
        BotaniaSmelteryCompat.INSTANCE.addFluidTags(tags);
    }
}
