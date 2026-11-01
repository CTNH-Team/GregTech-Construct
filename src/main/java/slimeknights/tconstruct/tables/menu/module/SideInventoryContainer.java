package slimeknights.tconstruct.tables.menu.module;

import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import slimeknights.mantle.inventory.BaseContainerMenu;
import slimeknights.tconstruct.tables.menu.slot.HighStackCountItemHandlerSlot;

import javax.annotation.Nullable;
import java.util.List;

public class SideInventoryContainer<TILE extends BlockEntity> extends BaseContainerMenu<TILE> {

  @Getter
  private final int columns;
  @Getter
  private final int slotCount;
  protected final LazyOptional<IItemHandler> itemHandler;
  /** Additional block entities whose handlers are merged into this side inventory; the menu stays valid only while all of them exist */
  private final List<BlockEntity> extraTiles;

  public SideInventoryContainer(MenuType<?> containerType, int windowId, Inventory inv, @Nullable TILE tile, int x, int y, int columns) {
    this(containerType, windowId, inv, tile, (Direction)null, x, y, columns);
  }

  public SideInventoryContainer(MenuType<?> containerType, int windowId, Inventory inv, @Nullable TILE tile, @Nullable Direction inventoryDirection, int x, int y, int columns) {
    super(containerType, windowId, inv, tile);
    this.extraTiles = List.of();

    // must have a TE
    if (tile == null) {
      this.itemHandler = LazyOptional.of(() -> EmptyHandler.INSTANCE);
    } else {
      this.itemHandler = tile.getCapability(ForgeCapabilities.ITEM_HANDLER, inventoryDirection);
    }

    this.slotCount = this.itemHandler.orElse(EmptyHandler.INSTANCE).getSlots();
    this.columns = columns;
    this.addSlots(this.itemHandler.orElse(EmptyHandler.INSTANCE), x, y, columns);
  }

  /**
   * Creates a side inventory from an explicit item handler, used when multiple adjacent
   * containers are merged into a single panel.
   */
  public SideInventoryContainer(MenuType<?> containerType, int windowId, Inventory inv, @Nullable TILE tile, IItemHandler itemHandler, int x, int y, int columns) {
    this(containerType, windowId, inv, tile, List.of(), itemHandler, x, y, columns);
  }

  /**
   * Creates a side inventory from an explicit item handler, tracking every merged block entity
   * so the menu stays valid only while all merged containers still exist.
   */
  public SideInventoryContainer(MenuType<?> containerType, int windowId, Inventory inv, @Nullable TILE tile, List<BlockEntity> tiles, IItemHandler itemHandler, int x, int y, int columns) {
    super(containerType, windowId, inv, tile);

    this.extraTiles = tiles == null ? List.of() : tiles;
    this.itemHandler = LazyOptional.of(() -> itemHandler);
    this.slotCount = itemHandler.getSlots();
    this.columns = columns;
    this.addSlots(itemHandler, x, y, columns);
  }

  @Override
  public boolean stillValid(Player playerIn) {
    if (!super.stillValid(playerIn)) {
      return false;
    }
    for (BlockEntity tile : this.extraTiles) {
      if (!isTileValid(tile)) {
        return false;
      }
    }
    return true;
  }

  /** Mirrors {@link BaseContainerMenu#stillValid} for a single tile. */
  private static boolean isTileValid(@Nullable BlockEntity tile) {
    if (tile == null) {
      return true;
    }
    if (tile.isRemoved()) {
      return false;
    }
    Level world = tile.getLevel();
    return world != null && world.isLoaded(tile.getBlockPos());
  }

  /** Adds the slots for the given handler, shared by both constructors */
  private void addSlots(IItemHandler handler, int x, int y, int columns) {
    int rows = this.slotCount / columns;
    if (this.slotCount % columns != 0) {
      rows++;
    }

    // add slots
    int index = 0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        if (index >= this.slotCount) {
          break;
        }

        this.addSlot(this.createSlot(handler, index, x + c * 18, y + r * 18));
        index++;
      }
    }
  }

  /**
   * Creates a slot for this inventory
   * @param itemHandler  Item handler
   * @param index        Slot index
   * @param x            Slot X position
   * @param y            Slot Y position
   * @return  Inventory slot
   */
  protected Slot createSlot(IItemHandler itemHandler, int index, int x, int y) {
    return new HighStackCountItemHandlerSlot(itemHandler, index, x, y);
  }
}
