package slimeknights.tconstruct.plugin.botania.smeltery;

import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.loading.LoadingModList;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialIds;

import net.minecraftforge.fluids.ForgeFlowingFluid;

import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.library.addon.AddonSmelteryCompat;

import java.util.List;

import static slimeknights.tconstruct.fluids.block.BurningLiquidBlock.createBurning;
import static slimeknights.tconstruct.library.addon.AddonSmelteryCompat.hot;

/**
 * Botania-owned smeltery and molten-fluid compat.
 */
public final class BotaniaSmelteryCompat extends TinkerModule implements AddonSmelteryCompat {

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
        if (ctnhManaLoaded()) {
            return FLUIDS.registerCompatMetal(name, properties);
        }
        return FLUIDS.registerMetal(name)
                .type(properties)
                .block(createBurning(MapColor.METAL, 12, 10, 5f))
                .bucket()
                .commonTag()
                .flowing();
    }

    private static boolean ctnhManaLoaded() {
        LoadingModList loadingModList = LoadingModList.get();
        return loadingModList != null && loadingModList.getModFileById("ctnhmana") != null;
    }

    @Override
    public List<Entry> entries() {
        return ENTRIES;
    }
}
