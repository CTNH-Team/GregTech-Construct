package slimeknights.tconstruct.plugin.botania.smeltery;

import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.library.addon.AddonSmelteryCompat;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialIds;

import java.util.List;

import static slimeknights.tconstruct.fluids.block.BurningLiquidBlock.createBurning;
import static slimeknights.tconstruct.library.addon.AddonSmelteryCompat.hot;

/**
 * Botania-owned smeltery and molten-fluid compat.
 */
public final class BotaniaSmelteryCompat extends TinkerModule implements AddonSmelteryCompat {
  public static final FlowingFluidObject<ForgeFlowingFluid> moltenManaSteel = FLUIDS.registerMetal("manasteel").type(hot("manasteel").temperature(1100).lightLevel(12)).block(createBurning(MapColor.METAL, 12, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<ForgeFlowingFluid> moltenTerraSteel = FLUIDS.registerMetal("terrasteel").type(hot("terrasteel").temperature(1500).lightLevel(12)).block(createBurning(MapColor.METAL, 12, 10, 5f)).bucket().commonTag().flowing();

  private static final List<Entry> ENTRIES = List.of(
    new Entry("manasteel", moltenManaSteel, CompatType.NONE, BotaniaMaterialIds.manaSteel),
    new Entry("terrasteel", moltenTerraSteel, CompatType.NONE, BotaniaMaterialIds.terraSteel)
  );

  public static final BotaniaSmelteryCompat INSTANCE = new BotaniaSmelteryCompat();

  private BotaniaSmelteryCompat() {}

  @Override
  public List<Entry> entries() {
    return ENTRIES;
  }
}
