package slimeknights.tconstruct.plugin.sophisticated;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import slimeknights.tconstruct.library.addon.ITiCAddon;
import slimeknights.tconstruct.library.addon.TiCAddon;
import slimeknights.tconstruct.plugin.sophisticated.client.SophisticatedLockedSlotRenderer;
import slimeknights.tconstruct.plugin.sophisticated.client.SophisticatedSearch;
import slimeknights.tconstruct.plugin.sophisticated.client.SophisticatedShiftTargetButton;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;

/**
 * Adds Sophisticated style workstation features: a search box for side inventories, a
 * shift click result target toggle on the crafting station, and locked slot ghosts for
 * adjacent Sophisticated storage blocks and backpack blocks.
 */
@TiCAddon(modID = SophisticatedTiCAddon.MOD_ID)
public class SophisticatedTiCAddon implements ITiCAddon {
  public static final String MOD_ID = "sophisticatedcore";

  public SophisticatedTiCAddon() {
    // both sides need the result target hook, the search box and target button are client only
    CraftingStationContainerMenu.resultTarget = new SophisticatedShiftTarget();
    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
      BaseTabbedScreen.search = new SophisticatedSearch();
      BaseTabbedScreen.stationButton = new SophisticatedShiftTargetButton();
      BaseTabbedScreen.slotOverlay = new SophisticatedLockedSlotRenderer();
    });
  }

  @Override
  public String addonModId() {
    return MOD_ID;
  }
}
