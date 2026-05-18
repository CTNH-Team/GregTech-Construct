package slimeknights.tconstruct.plugin.botania.tag;

import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.addon.DynamicTagProviderRegistrar;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialIds;

/**
 * Botania material tag hooks appended to TiC's core tag providers.
 */
public final class BotaniaMaterialTagProvider {
  private BotaniaMaterialTagProvider() {}

  public static void addTags(DynamicTagProviderRegistrar.MaterialTagRegistrar tags) {
    tags.addOptional(TinkerTags.Materials.COMPATABILITY_METALS, BotaniaMaterialIds.manaSteel, BotaniaMaterialIds.terraSteel);
    tags.addOptional(TinkerTags.Materials.HARD_METALS, BotaniaMaterialIds.manaSteel, BotaniaMaterialIds.terraSteel);
    tags.addOptional(TinkerTags.Materials.LIGHT, BotaniaMaterialIds.manaSteel, BotaniaMaterialIds.terraSteel);
  }
}
