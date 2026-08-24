package slimeknights.tconstruct.tables.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;
import slimeknights.tconstruct.tables.menu.slot.PlayerSensitiveLazyResultSlot;
import slimeknights.tconstruct.tables.menu.slot.CraftingStationToolSlot;

import javax.annotation.Nullable;

public class CraftingStationContainerMenu extends TabbedContainerMenu<CraftingStationBlockEntity> {
  private final PlayerSensitiveLazyResultSlot resultSlot;
  /** Button id for the shift click result target */
  public static final int SHIFT_CLICK_TARGET_BUTTON = 0;

  /** Plugin hook controlling where shift clicked results go. */
  @Nullable
  public static IShiftResultTarget resultTarget;

  /** Hook for a plugin-provided shift click result target selection. */
  public interface IShiftResultTarget {
    /** Whether the shift clicked result prefers the adjacent container. */
    boolean shiftClickIntoStorage(CraftingStationContainerMenu menu);

    /** Sets the shift click result target. */
    void setShiftClickIntoStorage(CraftingStationContainerMenu menu, boolean intoStorage);

    /** Toggles the shift click result target. */
    void toggle(CraftingStationContainerMenu menu);
  }

  /** Syncs whether shift clicking the result prefers the adjacent container */
  public final DataSlot shiftClickIntoStorage;

  /**
   * Standard constructor
   * @param id    Window ID
   * @param inv   Player inventory
   * @param tile  Relevant tile entity
   */
  public CraftingStationContainerMenu(int id, Inventory inv, @Nullable CraftingStationBlockEntity tile) {
    this(id, inv, tile, -1, null, null);
  }

  public CraftingStationContainerMenu(int id, Inventory inv, @Nullable CraftingStationBlockEntity tile, int sideInventorySlotCount) {
    this(id, inv, tile, sideInventorySlotCount, null, null);
  }

  public CraftingStationContainerMenu(int id, Inventory inv, @Nullable CraftingStationBlockEntity tile, int sideInventorySlotCount, @Nullable BlockEntity sideInventoryTile) {
    this(id, inv, tile, sideInventorySlotCount, sideInventoryTile, null);
  }

  public CraftingStationContainerMenu(int id, Inventory inv, @Nullable CraftingStationBlockEntity tile, int sideInventorySlotCount, @Nullable BlockEntity sideInventoryTile, @Nullable int[] sideInventoryLimits) {
    super(TinkerTables.craftingStationContainer.get(), id, inv, tile, sideInventorySlotCount, sideInventoryTile, sideInventoryLimits);

    // unfortunately, nothing works with no tile
    if (tile != null) {
      // send the player the current recipe, as we only sync to open containers
      tile.syncRecipe(inv.player);

      // add crafting slots first, as each added slot will clear the result cache
      for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 3; col++) {
          this.addSlot(new Slot(tile, col + row * 3, 30 + col * 18, 17 + row * 18));
        }
      }
      // GT crafting tools live in their own nine single-item slots. They are deliberately
      // outside the CraftingContainerWrapper used for recipe matching.
      for (int index = 0; index < CraftingStationBlockEntity.TOOL_SLOT_COUNT; index++) {
        this.addSlot(new CraftingStationToolSlot(tile, CraftingStationBlockEntity.TOOL_SLOT_START + index,
          8 + index * 18, 0));
      }
      // add result slot, will fetch result cache
      this.addSlot(resultSlot = new PlayerSensitiveLazyResultSlot(inv.player, tile.getCraftingResult(), 124, 35));

      this.addChestSideInventory();
    } else {
      // requirement for final variable
      resultSlot = null;
    }

    // sync the shift click result target to the client, driven by the plugin hook
    this.shiftClickIntoStorage = this.addDataSlot(new DataSlot() {
      @Override
      public int get() {
        IShiftResultTarget target = resultTarget;
        return target != null && target.shiftClickIntoStorage(CraftingStationContainerMenu.this) ? 1 : 0;
      }

      @Override
      public void set(int value) {
        IShiftResultTarget target = resultTarget;
        if (target != null) {
          target.setShiftClickIntoStorage(CraftingStationContainerMenu.this, value != 0);
        }
      }
    });

    this.addInventorySlots();
  }

  /**
   * Factory constructor
   * @param id   Window ID
   * @param inv  Player inventory
   * @param buf  Buffer for fetching tile
   */
  public CraftingStationContainerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
    this(id, inv, getTileEntityFromBuf(buf, CraftingStationBlockEntity.class), TabbedContainerMenu.SideInventoryClientData.read(buf));
  }

  public CraftingStationContainerMenu(int id, Inventory inv, @Nullable CraftingStationBlockEntity tile, TabbedContainerMenu.SideInventoryClientData data) {
    this(id, inv, tile, data.slotCount(), data.tile(), data.limits());
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    IShiftResultTarget target = resultTarget;
    if (id == SHIFT_CLICK_TARGET_BUTTON && target != null) {
      target.toggle(this);
      return true;
    }
    return super.clickMenuButton(player, id);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    Slot slot = this.slots.get(index);
    // fix issue on shift clicking from the result slot if the recipe result mismatches the displayed item
    if (slot == resultSlot) {
      if (tile != null && slot.hasItem()) {
        // return the original result so shift click works
        ItemStack original = slot.getItem().copy(); // TODO: are these copies really needed?
        // but add the true result into the inventory
        ItemStack result = tile.getResultForPlayer(player);
        if (!result.isEmpty()) {
          boolean nothingDone = true;
          // move into the selected target first, falling back to the other
          IShiftResultTarget target = resultTarget;
          boolean intoStorage = target == null || target.shiftClickIntoStorage(this);
          if (intoStorage) {
            if (subContainers.size() > 0) { // the sub container check does not do well with 0 sub containers
              nothingDone = this.refillAnyContainer(result, this.subContainers);
              if (!result.isEmpty()) {
                nothingDone &= this.moveToAnyContainer(result, this.subContainers);
              }
            }
            nothingDone &= this.moveToPlayerInventory(result);
          } else {
            nothingDone = this.moveToPlayerInventory(result);
            if (subContainers.size() > 0) {
              nothingDone &= this.refillAnyContainer(result, this.subContainers);
              if (!result.isEmpty()) {
                nothingDone &= this.moveToAnyContainer(result, this.subContainers);
              }
            }
          }
          // if successfully added to an inventory, update
          if (!nothingDone) {
            // The moved stack count is the recipe output count, not the number of
            // recipe executions. A single shift-click performs one craft, so keep
            // ingredient consumption and dedicated-tool damage to one use.
            tile.takeResult(player, result, 1);
            tile.getCraftingResult().clearContent();
            return original;
          }
        } else {
          tile.notifyUncraftable(player);
        }
      }
      return ItemStack.EMPTY;
    } else if (index >= this.playerInventoryStart) {
      // shift clicking the player inventory moves into the adjacent side container first,
      // the crafting grid is not a storage
      ItemStack stack = slot.getItem().copy();
      ItemStack remaining = stack.copy();
      if (subContainers.size() > 0) {
        this.refillAnyContainer(remaining, this.subContainers);
        if (!remaining.isEmpty()) {
          this.moveToAnyContainer(remaining, this.subContainers);
        }
      }
      // only apply the slot when the side container accepted something
      if (remaining.getCount() < stack.getCount()) {
        slot.set(remaining);
        if (!remaining.isEmpty()) {
          slot.setChanged();
        }
        return stack;
      }
      return super.quickMoveStack(player, index);
    } else {
      return super.quickMoveStack(player, index);
    }
  }

  @Override
  public void slotsChanged(Container inventoryIn) {
    // handled in TE item display logic
  }

  @Override
  public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
    return slot != this.resultSlot && super.canTakeItemForPickAll(stack, slot);
  }
}
