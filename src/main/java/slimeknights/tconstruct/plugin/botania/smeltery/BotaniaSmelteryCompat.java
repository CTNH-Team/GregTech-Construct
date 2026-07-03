package slimeknights.tconstruct.plugin.botania.smeltery;

import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialIds;

import net.minecraftforge.fluids.ForgeFlowingFluid;

import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.library.addon.AddonSmelteryCompat;

import java.util.List;

import static slimeknights.tconstruct.library.addon.AddonSmelteryCompat.hot;

/**
 * Botania-owned smeltery and molten-fluid compat.
 */
public final class BotaniaSmelteryCompat extends TinkerModule implements AddonSmelteryCompat {

    public static final FlowingFluidObject<ForgeFlowingFluid> moltenManaSteel =
            FLUIDS.registerCompatMetal("mana_steel", hot("mana_steel").temperature(1100).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenTerraSteel =
            FLUIDS.registerCompatMetal("terra_steel", hot("terra_steel").temperature(1500).lightLevel(12));

    private static final List<Entry> ENTRIES = List.of(
            new Entry("mana_steel", moltenManaSteel, CompatType.NONE, BotaniaMaterialIds.manaSteel),
            new Entry("terra_steel", moltenTerraSteel, CompatType.NONE, BotaniaMaterialIds.terraSteel));

    public static final BotaniaSmelteryCompat INSTANCE = new BotaniaSmelteryCompat();

    private BotaniaSmelteryCompat() {}

    @Override
    public List<Entry> entries() {
        return ENTRIES;
    }
}
