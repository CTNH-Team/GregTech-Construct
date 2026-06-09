package slimeknights.tconstruct.plugin.botania.modifier;

import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierId;

public final class BotaniaModifierIds {

    public static final ResourceLocation PLATE_HELMET = TConstruct.getResource("plate_helmet");

    public static final ModifierId manafix = id("manafix");
    public static final ModifierId terrarecover = id("terrarecover");
    public static final ModifierId ancientWill = id("ancient_will");
    public static final ModifierId ancientWillAhrim = id("ancient_will_ahrim");
    public static final ModifierId ancientWillDharok = id("ancient_will_dharok");
    public static final ModifierId ancientWillGuthan = id("ancient_will_guthan");
    public static final ModifierId ancientWillTorag = id("ancient_will_torag");
    public static final ModifierId ancientWillVerac = id("ancient_will_verac");
    public static final ModifierId ancientWillKaril = id("ancient_will_karil");

    private BotaniaModifierIds() {}

    private static ModifierId id(String name) {
        return new ModifierId(TConstruct.MOD_ID, name);
    }
}
