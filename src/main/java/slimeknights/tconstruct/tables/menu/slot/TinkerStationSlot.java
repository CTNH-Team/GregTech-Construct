package slimeknights.tconstruct.tables.menu.slot;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.layout.LayoutSlot;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;
import slimeknights.tconstruct.tables.apotheosis.ApotheosisSocketMode;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

import javax.annotation.Nullable;
import java.util.List;

/** Class for common logic with tinker station input slots */
public class TinkerStationSlot extends Slot {
  private final TinkerStationBlockEntity tile;
  private final LazyResultContainer craftResult;
  private LayoutSlot layout = null;
  @Nullable
  private TinkerStationContainerMenu menu;

  public TinkerStationSlot(TinkerStationBlockEntity tile, int index, int xPosition, int yPosition) {
    super(tile, index, xPosition, yPosition);
    this.tile = tile;
    this.craftResult = tile.getCraftingResult();
  }

  /** Sets the owning menu for gem-mode socket lookups. */
  public void setMenu(TinkerStationContainerMenu menu) {
    this.menu = menu;
  }

  /** If true, this slot is inactive */
  public boolean isDormant() {
    return layout == null;
  }

  /** Activates this slot */
  public void activate(LayoutSlot layout) {
    this.layout = layout;
  }

  /** Deactivates this slot */
  public void deactivate() {
    this.layout = null;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    if (isGemSocketSlot()) {
      if (!hasVisibleSocketEntry()) {
        return false;
      }
      if (getSocketEntry().isFilled()) {
        return false;
      }
      return ApotheosisBridge.sockets().canInsertGem(getCurrentTool(), getSocketEntry().rawSocketIndex(), stack);
    }
    // dormant slots don't take any items, they can only be taken out of
    return stack.isEmpty() || (layout != null && layout.isValid(stack));
  }

  @Override
  public ItemStack getItem() {
    if (isGemSocketSlot()) {
      if (!hasVisibleSocketEntry()) {
        return ItemStack.EMPTY;
      }
      return getSocketEntry().gem();
    }
    return super.getItem();
  }

  @Override
  public boolean hasItem() {
    if (isGemSocketSlot()) {
      if (!hasVisibleSocketEntry()) {
        return false;
      }
      return getSocketEntry().isFilled();
    }
    return super.hasItem();
  }

  @Override
  public boolean mayPickup(net.minecraft.world.entity.player.Player player) {
    if (isGemSocketSlot()) {
      return true;
    }
    return super.mayPickup(player);
  }

  @Override
  public void setChanged() {
    craftResult.clearContent();
    super.setChanged();
  }

  private boolean isGemSocketSlot() {
    if (this.menu == null || !this.tile.isGemMode()) {
      return false;
    }
    int socketSlotIndex = getContainerSlot() - TinkerStationBlockEntity.INPUT_SLOT;
    return socketSlotIndex >= 0 && socketSlotIndex < 5;
  }

  private boolean hasVisibleSocketEntry() {
    int socketSlotIndex = getContainerSlot() - TinkerStationBlockEntity.INPUT_SLOT;
    return socketSlotIndex >= 0 && socketSlotIndex < this.menu.getVisibleSocketCount();
  }

  private ApotheosisBridge.SocketGem getSocketEntry() {
    int socketSlotIndex = getContainerSlot() - TinkerStationBlockEntity.INPUT_SLOT;
    List<ApotheosisBridge.SocketGem> sockets = ApotheosisSocketMode.getVisibleSockets(getCurrentTool());
    if (socketSlotIndex < 0 || socketSlotIndex >= sockets.size()) {
      return ApotheosisBridge.SocketGem.empty(socketSlotIndex);
    }
    return sockets.get(socketSlotIndex);
  }

  private ItemStack getCurrentTool() {
    if (this.menu != null) {
      return this.menu.getCurrentTool();
    }
    return this.tile.getItem(TinkerStationBlockEntity.TINKER_SLOT);
  }
}
