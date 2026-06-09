package slimeknights.tconstruct.plugin.botania.modifier;

import slimeknights.tconstruct.library.modifiers.ModifierSetBonusHelper;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

import java.util.List;

public final class BotaniaModifiersProvider {

    public static final StaticModifier<?> manafix = new StaticModifier<>(BotaniaModifierIds.manafix);
    public static final StaticModifier<?> terrarecover = new StaticModifier<>(BotaniaModifierIds.terrarecover);
    public static final StaticModifier<?> ancientWill = new StaticModifier<>(BotaniaModifierIds.ancientWill);
    public static final StaticModifier<?> ancientWillAhrim = new StaticModifier<>(BotaniaModifierIds.ancientWillAhrim);
    public static final StaticModifier<?> ancientWillDharok = new StaticModifier<>(BotaniaModifierIds.ancientWillDharok);
    public static final StaticModifier<?> ancientWillGuthan = new StaticModifier<>(BotaniaModifierIds.ancientWillGuthan);
    public static final StaticModifier<?> ancientWillTorag = new StaticModifier<>(BotaniaModifierIds.ancientWillTorag);
    public static final StaticModifier<?> ancientWillVerac = new StaticModifier<>(BotaniaModifierIds.ancientWillVerac);
    public static final StaticModifier<?> ancientWillKaril = new StaticModifier<>(BotaniaModifierIds.ancientWillKaril);
    public static final List<StaticModifier<?>> ancientWills = List.of(
            ancientWillAhrim, ancientWillDharok, ancientWillGuthan, ancientWillTorag, ancientWillVerac, ancientWillKaril);

    public static void registerSetBonuses() {
        ModifierSetBonusHelper.register(BotaniaModifierIds.terrarecover, BotaniaModifierIds.ancientWill);
    }

    private BotaniaModifiersProvider() {}
}
