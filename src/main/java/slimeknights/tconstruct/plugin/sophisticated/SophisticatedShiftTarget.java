package slimeknights.tconstruct.plugin.sophisticated;

import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;

/**
 * Stores the shift click result target on the crafting station tile, mimicking the
 * Sophisticated Core crafting upgrade setting.
 */
public class SophisticatedShiftTarget implements CraftingStationContainerMenu.IShiftResultTarget {
  @Override
  public boolean shiftClickIntoStorage(CraftingStationContainerMenu menu) {
    CraftingStationBlockEntity tile = menu.getTile();
    return tile == null || tile.isShiftClickIntoStorage();
  }

  @Override
  public void setShiftClickIntoStorage(CraftingStationContainerMenu menu, boolean intoStorage) {
    CraftingStationBlockEntity tile = menu.getTile();
    if (tile != null) {
      tile.setShiftClickIntoStorage(intoStorage);
    }
  }

  @Override
  public void toggle(CraftingStationContainerMenu menu) {
    setShiftClickIntoStorage(menu, !shiftClickIntoStorage(menu));
  }
}
