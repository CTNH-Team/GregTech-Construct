package slimeknights.tconstruct.plugin.botania.smeltery;

import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.library.addon.AddonSmelteryCompat;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialIds;

import java.util.List;

import static slimeknights.tconstruct.library.addon.AddonSmelteryCompat.hot;

/**
 * Botania-owned smeltery and molten-fluid compat.
 */
public final class BotaniaSmelteryCompat extends TinkerModule implements AddonSmelteryCompat {
    /** Mod that supplies mana metals through the common tag. */
    private static final String MANA_PROVIDER = "ctnhmana";

    public static final FlowingFluidObject<ForgeFlowingFluid> moltenManaSteel =
            registerBotaniaMetal("mana_steel", hot("mana_steel").temperature(1100).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenTerraSteel =
            registerBotaniaMetal("terra_steel", hot("terra_steel").temperature(1500).lightLevel(12));

    private static final List<Entry> ENTRIES = List.of(
            new Entry("mana_steel", moltenManaSteel, CompatType.NONE, BotaniaMaterialIds.manaSteel),
            new Entry("terra_steel", moltenTerraSteel, CompatType.NONE, BotaniaMaterialIds.terraSteel));

    public static final BotaniaSmelteryCompat INSTANCE = new BotaniaSmelteryCompat();

    private BotaniaSmelteryCompat() {}

    private static FlowingFluidObject<ForgeFlowingFluid> registerBotaniaMetal(String name, FluidType.Properties properties) {
        return FLUIDS.registerCompatMetal(name, properties, MANA_PROVIDER);
    }

    @Override
    public List<Entry> entries() {
        return ENTRIES;
    }
}
