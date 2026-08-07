package slimeknights.tconstruct.tables.menu;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.tables.menu.slot.HighStackCountItemHandlerSlot;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Quick-move behavior of the high stack count side inventory, driven by the shared move core. */
class TabbedContainerMenuMoveTest extends BaseMcTest {
  /** Player slots over a plain container, mimicking the menu's player inventory range. */
  private static List<Slot> playerSlots(int count) {
    SimpleContainer inventory = new SimpleContainer(count);
    List<Slot> slots = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      slots.add(new Slot(inventory, i, 0, 0));
    }
    return slots;
  }

  @Test
  void bucketMergesIntoExistingHighStackSlot() {
    // a vanilla non-stackable item (bucket) held multiple times by a high capacity slot
    HighStackHandler handler = new HighStackHandler();
    handler.setStackInSlot(0, new ItemStack(Items.WATER_BUCKET, 3));
    List<Slot> slots = List.of(new HighStackCountItemHandlerSlot(handler, 0, 0, 0));
    ItemStack bucket = new ItemStack(Items.WATER_BUCKET, 1);

    boolean moved = TabbedContainerMenu.moveItemStackTo(slots, bucket, 0, 1, false);

    assertThat(moved).isTrue();
    assertThat(bucket.isEmpty()).isTrue();
    assertThat(handler.getStackInSlot(0).getCount()).isEqualTo(4);
  }

  @Test
  void highCountNonStackableStackSplitsAcrossPlayerSlots() {
    List<Slot> slots = playerSlots(9);
    ItemStack buckets = new ItemStack(Items.WATER_BUCKET, 7);

    boolean moved = TabbedContainerMenu.moveItemStackTo(slots, buckets, 0, 9, true);

    assertThat(moved).isTrue();
    assertThat(buckets.isEmpty()).isTrue();
    int filled = 0;
    for (int i = 0; i < 9; i++) {
      ItemStack stack = ((SimpleContainer)slots.get(i).container).getItem(i);
      if (!stack.isEmpty()) {
        filled++;
        assertThat(stack.getItem()).isEqualTo(Items.WATER_BUCKET);
        assertThat(stack.getCount()).isEqualTo(1);
      }
    }
    assertThat(filled).isEqualTo(7);
  }

  @Test
  void stackableStackTopsUpHighStackSlot() {
    HighStackHandler handler = new HighStackHandler();
    handler.setStackInSlot(0, new ItemStack(Items.DIRT, 200));
    List<Slot> slots = List.of(new HighStackCountItemHandlerSlot(handler, 0, 0, 0));
    ItemStack dirt = new ItemStack(Items.DIRT, 64);

    boolean moved = TabbedContainerMenu.moveItemStackTo(slots, dirt, 0, 1, false);

    assertThat(moved).isTrue();
    assertThat(dirt.isEmpty()).isTrue();
    assertThat(handler.getStackInSlot(0).getCount()).isEqualTo(264);
  }

  @Test
  void mergeRespectsPlayerSlotItemLimit() {
    // a high count stack must not merge into an existing player stack past the item limit
    List<Slot> slots = playerSlots(2);
    SimpleContainer inventory = (SimpleContainer)slots.get(0).container;
    inventory.setItem(0, new ItemStack(Items.WATER_BUCKET, 1));
    ItemStack buckets = new ItemStack(Items.WATER_BUCKET, 7);

    boolean moved = TabbedContainerMenu.moveItemStackTo(slots, buckets, 0, 2, false);

    assertThat(moved).isTrue();
    assertThat(inventory.getItem(0).getCount()).isEqualTo(1);
    assertThat(inventory.getItem(1).getCount()).isEqualTo(1);
    assertThat(buckets.getCount()).isEqualTo(6);
  }

  @Test
  void stackableStackSplitsAcrossPlayerSlotsByItemLimit() {
    List<Slot> slots = playerSlots(9);
    ItemStack dirt = new ItemStack(Items.DIRT, 130);

    boolean moved = TabbedContainerMenu.moveItemStackTo(slots, dirt, 0, 9, true);

    assertThat(moved).isTrue();
    assertThat(dirt.isEmpty()).isTrue();
    SimpleContainer inventory = (SimpleContainer)slots.get(0).container;
    assertThat(inventory.getItem(8).getCount()).isEqualTo(64);
    assertThat(inventory.getItem(7).getCount()).isEqualTo(64);
    assertThat(inventory.getItem(6).getCount()).isEqualTo(2);
  }

  private static class HighStackHandler extends ItemStackHandler {
    private HighStackHandler() {
      super(1);
    }

    @Override
    public int getSlotLimit(int slot) {
      return 1024;
    }

    @Override
    protected int getStackLimit(int slot, ItemStack stack) {
      return getSlotLimit(slot);
    }
  }
}
