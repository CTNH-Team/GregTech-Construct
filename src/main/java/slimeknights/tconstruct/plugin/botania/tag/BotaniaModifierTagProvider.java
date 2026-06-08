package slimeknights.tconstruct.plugin.botania.tag;

import slimeknights.tconstruct.plugin.botania.modifier.BotaniaModifierIds;

import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.addon.DynamicTagProviderRegistrar;

/**
 * Botania modifier tag hooks appended to TiC's core tag providers.
 */
public final class BotaniaModifierTagProvider {

    private BotaniaModifierTagProvider() {}

    public static void addTags(DynamicTagProviderRegistrar.ModifierTagRegistrar tags) {
        tags.add(TinkerTags.Modifiers.GENERAL_UPGRADES, BotaniaModifierIds.manafix, BotaniaModifierIds.terrarecover);
        tags.add(TinkerTags.Modifiers.HIDDEN_FROM_RECIPE_VIEWERS, BotaniaModifierIds.ancientWill);
    }
}
