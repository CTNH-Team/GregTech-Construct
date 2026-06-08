package slimeknights.tconstruct.plugin.botania.modifier;

import slimeknights.tconstruct.library.modifiers.ModifierSetBonusHelper;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

public final class BotaniaModifiersProvider {

    public static final StaticModifier<?> manafix = new StaticModifier<>(BotaniaModifierIds.manafix);
    public static final StaticModifier<?> terrarecover = new StaticModifier<>(BotaniaModifierIds.terrarecover);
    public static final StaticModifier<?> ancientWill = new StaticModifier<>(BotaniaModifierIds.ancientWill);

    public static void registerSetBonuses() {
        ModifierSetBonusHelper.register(BotaniaModifierIds.terrarecover, BotaniaModifierIds.ancientWill);
    }

    private BotaniaModifiersProvider() {}
}
