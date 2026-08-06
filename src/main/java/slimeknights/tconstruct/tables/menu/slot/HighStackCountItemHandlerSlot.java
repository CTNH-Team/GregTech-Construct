package slimeknights.tconstruct.tables.menu.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import slimeknights.mantle.inventory.SmartItemHandlerSlot;

import java.util.Optional;

/** Slot projection for handlers that support stacks larger than the item's vanilla limit. */
public class HighStackCountItemHandlerSlot extends SmartItemHandlerSlot {
  public HighStackCountItemHandlerSlot(IItemHandler itemHandler, int index, int x, int y) {
    super(itemHandler, index, x, y);
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    IItemHandler handler = getItemHandler();
    int slotLimit = handler.getSlotLimit(getSlotIndex());
    if (stack.isEmpty() || slotLimit <= stack.getMaxStackSize()) {
      return super.getMaxStackSize(stack);
    }

    ItemStack maxInput = stack.copyWithCount(slotLimit);
    ItemStack current = getItem();
    if (!current.isEmpty() && !ItemStack.isSameItemSameTags(current, stack)) {
      // Vanilla reports the capacity the slot could hold if emptied, which the menu uses to
      // decide whether a different stack can be swapped in whole.
      return slotLimit;
    }
    return current.getCount() + slotLimit - handler.insertItem(getSlotIndex(), maxInput, true).getCount();
  }

  @Override
  public Optional<ItemStack> tryRemove(int amount, int maxCount, Player player) {
    // A primary click takes the whole slot content, which is one stack in vanilla slots but
    // can be several hundred items here. Cap whole-slot pickups at one item stack size;
    // secondary clicks (half) and normal sized slots are unaffected.
    ItemStack current = getItem();
    if (amount >= current.getCount() && current.getCount() > current.getMaxStackSize()) {
      amount = current.getMaxStackSize();
    }
    return super.tryRemove(amount, maxCount, player);
  }

  @Override
  public ItemStack safeTake(int amount, int maxCount, Player player) {
    // Drop key, double click and automation request an explicit amount, keep the full
    // request instead of the pickup cap.
    Optional<ItemStack> removed = super.tryRemove(amount, maxCount, player);
    removed.ifPresent(stack -> this.onTake(player, stack));
    return removed.orElse(ItemStack.EMPTY);
  }

  @Override
  public ItemStack remove(int amount) {
    if (amount <= 0) {
      return ItemStack.EMPTY;
    }

    // Some handlers still cap each extract call at the vanilla item limit. Drain in several
    // calls so the menu can move the complete logical stack in one click.
    ItemStack result = ItemStack.EMPTY;
    int remaining = amount;
    while (remaining > 0) {
      ItemStack extracted = getItemHandler().extractItem(getSlotIndex(), remaining, false);
      if (extracted.isEmpty()) {
        break;
      }
      if (result.isEmpty()) {
        result = extracted.copy();
      } else if (ItemStack.isSameItemSameTags(result, extracted)) {
        result.grow(extracted.getCount());
      } else {
        break;
      }
      int extractedCount = extracted.getCount();
      remaining -= extractedCount;
      if (extractedCount <= 0) {
        break;
      }
    }
    return result;
  }
}
