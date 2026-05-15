package slimeknights.tconstruct.tables.menu;

import lombok.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.layout.LayoutSlot;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayout;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.apotheosis.ApotheosisSocketMode;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.slot.ArmorSlot;
import slimeknights.tconstruct.tables.menu.slot.LazyResultSlot;
import slimeknights.tconstruct.tables.menu.slot.PlayerSensitiveLazyResultSlot;
import slimeknights.tconstruct.tables.menu.slot.TinkerStationSlot;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TinkerStationContainerMenu extends TabbedContainerMenu<TinkerStationBlockEntity> {
  @Getter
  private final List<Slot> inputSlots;
  private final LazyResultSlot resultSlot;
  @Getter
  private boolean socketExtractionMode = false;
  @Getter
  private int selectedSocket = -1;

  /**
   * Standard constructor
   * @param id    Window ID
   * @param inv   Player inventory
   * @param tile  Relevant tile entity
   */
  @SuppressWarnings("deprecation")
  public TinkerStationContainerMenu(int id, Inventory inv, @Nullable TinkerStationBlockEntity tile) {
    super(TinkerTables.tinkerStationContainer.get(), id, inv, tile);

    // unfortunately, nothing works with no tile
    if (tile != null) {
      tile.setItemName("");
      // send the player the current recipe, as we only sync to open containers
      tile.syncRecipe(inv.player);

      inputSlots = new ArrayList<>();
      TinkerStationSlot tinkerSlot = new TinkerStationSlot(tile, TinkerStationBlockEntity.TINKER_SLOT, 0, 0);
      tinkerSlot.setMenu(this);
      this.addSlot(tinkerSlot);

      for (int index = 0; index < tile.getContainerSize() - 1; index++) {
        TinkerStationSlot inputSlot = new TinkerStationSlot(tile, index + TinkerStationBlockEntity.INPUT_SLOT, 0, 0);
        inputSlot.setMenu(this);
        inputSlots.add(this.addSlot(inputSlot));
      }

      // add result slot, will fetch result cache
      this.addSlot(this.resultSlot = new PlayerSensitiveLazyResultSlot(inv.player, tile.getCraftingResult(), 114, 38));
      // set initial slot filters and activations
      setToolSelection(StationSlotLayoutLoader.getInstance().get(BuiltInRegistries.BLOCK.getKey(tile.getBlockState().getBlock())));
    }
    else {
      // requirement for final variable
      this.resultSlot = null;
      this.inputSlots = Collections.emptyList();
    }

    // add armor and offhand slots, for convenience
    for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
      this.addSlot(new ArmorSlot(inv, slotType.getSlot(), 152, 20 + slotType.ordinal() * 18));
    }
    this.addSlot(new Slot(inv, 40, 132, 74).setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD));

    this.addInventorySlots();
  }

  /**
   * Factory constructor
   * @param id   Window ID
   * @param inv  Player inventory
   * @param buf  Buffer for fetching tile
   */
  public TinkerStationContainerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
    this(id, inv, getTileEntityFromBuf(buf, TinkerStationBlockEntity.class));
  }

  @Override
  protected int getInventoryYOffset() {
    return 102;
  }

  @Override
  public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
    return slot != this.resultSlot && super.canTakeItemForPickAll(stack, slot);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    Slot slot = this.slots.get(index);
    if (slot == resultSlot) {
      if (tile != null && slot.hasItem()) {
        ItemStack original = slot.getItem().copy();
        ItemStack crafted = getDisplayedResult().copy();
        if (!crafted.isEmpty()) {
          ItemStack moved = crafted.copy();
          boolean nothingDone = true;
          if (subContainers.size() > 0) {
            nothingDone = this.refillAnyContainer(moved, this.subContainers);
          }
          nothingDone &= this.moveToPlayerInventory(moved);
          if (subContainers.size() > 0) {
            nothingDone &= this.moveToAnyContainer(moved, this.subContainers);
          }
          if (!nothingDone) {
            tile.onCraft(player, crafted, crafted.getCount());
            tile.getCraftingResult().clearContent();
            if (this.resultSlot instanceof PlayerSensitiveLazyResultSlot playerSensitive) {
              playerSensitive.invalidatePlayerResult();
            }
            return original;
          }
        }
      }
      return ItemStack.EMPTY;
    }
    return super.quickMoveStack(player, index);
  }

  /** Gets the current result stack as displayed to the active player. */
  public ItemStack getDisplayedResult() {
    if (this.resultSlot == null) {
      return ItemStack.EMPTY;
    }
    return this.resultSlot.getItem();
  }

  /** True if this anvil is currently in shared gem mode. */
  public boolean isGemMode() {
    return this.tile != null && this.tile.isGemMode();
  }

  /** Gets the current tool shown in the station tool slot. */
  public ItemStack getCurrentTool() {
    if (this.tile == null) {
      return ItemStack.EMPTY;
    }
    return this.tile.getItem(TinkerStationBlockEntity.TINKER_SLOT);
  }

  /** Gets the number of visible socket entries currently shown in gem mode. */
  public int getVisibleSocketCount() {
    return ApotheosisSocketMode.getVisibleSockets(getCurrentTool()).size();
  }

  /** Updates the shared backend gem mode state on the active anvil. */
  public void setGemMode(boolean enabled) {
    if (this.tile == null) {
      return;
    }
    if (enabled) {
      this.tile.enterGemMode();
    } else {
      this.tile.exitGemMode();
    }
    invalidateDisplayedResult();
  }

  /** Updates the per-player socket extraction state. */
  public void setSocketExtractionState(boolean enabled, int selectedSocket) {
    boolean oldMode = this.socketExtractionMode;
    int oldSelectedSocket = this.selectedSocket;
    this.socketExtractionMode = enabled;
    this.selectedSocket = selectedSocket;
    refreshSocketExtractionState();
    if (oldMode != this.socketExtractionMode || oldSelectedSocket != this.selectedSocket) {
      invalidateDisplayedResult();
    }
  }

  /** Refreshes the current extraction state against the tool and input count. */
  public void refreshSocketExtractionState() {
    if (this.tile == null) {
      this.socketExtractionMode = false;
      this.selectedSocket = -1;
      return;
    }

    ItemStack tool = this.tile.getItem(TinkerStationBlockEntity.TINKER_SLOT);
    if (!ApotheosisSocketMode.canExtract(tool, this.tile.getInputCount())) {
      this.socketExtractionMode = false;
      this.selectedSocket = -1;
      return;
    }

    this.selectedSocket = ApotheosisSocketMode.normalizeSelection(tool, this.selectedSocket);
    if (this.selectedSocket < 0) {
      this.socketExtractionMode = false;
    }
  }

  /** True if a valid socket extraction selection is currently active for this player. */
  public boolean hasActiveSocketExtraction() {
    return this.socketExtractionMode && this.selectedSocket >= 0;
  }

  /**
   * Updates the active slots from the screen
   * @param layout     New layout
   */
  public void setToolSelection(StationSlotLayout layout) {
    assert this.tile != null;
    int maxSize = tile.getContainerSize();
    for (int i = 0; i < maxSize; i++) {
      Slot slot = this.slots.get(i);
      if (slot instanceof TinkerStationSlot slotToolPart) {
        // activate or deactivate the slots, sets the filters
        LayoutSlot layoutSlot = layout.getSlot(i);
        if (layoutSlot.isHidden()) {
          slotToolPart.deactivate();
        }
        else {
          slotToolPart.activate(layoutSlot);
        }
      }
    }
  }

  @Override
  public void slotsChanged(Container inventory) {
    refreshSocketExtractionState();
  }

  private void invalidateDisplayedResult() {
    if (this.tile != null) {
      this.tile.getCraftingResult().clearContent();
    }
    if (this.resultSlot instanceof PlayerSensitiveLazyResultSlot playerSensitive) {
      playerSensitive.invalidatePlayerResult();
    }
  }
}
