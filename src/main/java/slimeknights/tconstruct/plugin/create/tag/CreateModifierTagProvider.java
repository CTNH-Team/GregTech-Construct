/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.tag;

import slimeknights.tconstruct.plugin.create.modifier.CreateModifierIds;

import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.addon.DatagenTagProviderRegistrar;

public final class CreateModifierTagProvider {

    private CreateModifierTagProvider() {}

    public static void addTags(DatagenTagProviderRegistrar.ModifierTagRegistrar tags) {
        tags.add(TinkerTags.Modifiers.HARVEST_ABILITIES, CreateModifierIds.CRUSHING);
        tags.add(TinkerTags.Modifiers.CHESTPLATE_UPGRADES, CreateModifierIds.EXTENDO);
        tags.add(TinkerTags.Modifiers.GENERAL_SLOTLESS, CreateModifierIds.GOGGLES);
        tags.add(TinkerTags.Modifiers.INTERACTION_ABILITIES, CreateModifierIds.WRENCH);
        tags.add(TinkerTags.Modifiers.BOOT_UPGRADES, CreateModifierIds.DIVING_WEIGHTS);
    }
}
