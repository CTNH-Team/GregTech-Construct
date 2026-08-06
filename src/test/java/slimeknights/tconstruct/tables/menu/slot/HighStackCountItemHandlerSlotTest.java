package slimeknights.tconstruct.tables.menu.slot;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class HighStackCountItemHandlerSlotTest extends BaseMcTest {
  @Test
  void insertsWholeHighCountStack() {
    HighStackHandler handler = new HighStackHandler();
    HighStackCountItemHandlerSlot slot = new HighStackCountItemHandlerSlot(handler, 0, 0, 0);
    ItemStack incoming = new ItemStack(Items.DIRT, 256);

    ItemStack remainder = slot.safeInsert(incoming);

    assertThat(remainder.isEmpty()).isTrue();
    assertThat(handler.getStackInSlot(0).getCount()).isEqualTo(256);
  }

  @Test
  void insertsIntoExistingHighCountStack() {
    HighStackHandler handler = new HighStackHandler();
    handler.setStackInSlot(0, new ItemStack(Items.DIRT, 256));
    HighStackCountItemHandlerSlot slot = new HighStackCountItemHandlerSlot(handler, 0, 0, 0);
    ItemStack incoming = new ItemStack(Items.DIRT, 768);

    ItemStack remainder = slot.safeInsert(incoming);

    assertThat(remainder.isEmpty()).isTrue();
    assertThat(handler.getStackInSlot(0).getCount()).isEqualTo(1024);
  }

  @Test
  void extractsWholeHighCountStack() {
    HighStackHandler handler = new HighStackHandler();
    handler.setStackInSlot(0, new ItemStack(Items.DIRT, 256));
    HighStackCountItemHandlerSlot slot = new HighStackCountItemHandlerSlot(handler, 0, 0, 0);

    ItemStack extracted = slot.safeTake(256, Integer.MAX_VALUE, null);

    assertThat(extracted.getCount()).isEqualTo(256);
    assertThat(handler.getStackInSlot(0).isEmpty()).isTrue();
  }

  @Test
  void preservesItemSpecificHandlerLimit() {
    ItemStackHandler handler = new ItemStackHandler(1) {
      @Override
      public int getSlotLimit(int slot) {
        return 1024;
      }
    };
    HighStackCountItemHandlerSlot slot = new HighStackCountItemHandlerSlot(handler, 0, 0, 0);

    assertThat(slot.getMaxStackSize(new ItemStack(Items.DIRT))).isEqualTo(64);
  }

  @Test
  void reportsCapacityAboveVanillaLimit() {
    HighStackHandler handler = new HighStackHandler();
    HighStackCountItemHandlerSlot slot = new HighStackCountItemHandlerSlot(handler, 0, 0, 0);

    assertThat(slot.getMaxStackSize(new ItemStack(Items.DIRT))).isEqualTo(1024);
  }

  @Test
  void capacityCountsExistingStack() {
    HighStackHandler handler = new HighStackHandler();
    handler.setStackInSlot(0, new ItemStack(Items.DIRT, 256));
    HighStackCountItemHandlerSlot slot = new HighStackCountItemHandlerSlot(handler, 0, 0, 0);

    assertThat(slot.getMaxStackSize(new ItemStack(Items.DIRT))).isEqualTo(1024);
  }

  @Test
  void capacityAllowsSwappingDifferentStack() {
    HighStackHandler handler = new HighStackHandler();
    handler.setStackInSlot(0, new ItemStack(Items.STICK, 512));
    HighStackCountItemHandlerSlot slot = new HighStackCountItemHandlerSlot(handler, 0, 0, 0);

    assertThat(slot.getMaxStackSize(new ItemStack(Items.DIRT, 200))).isEqualTo(1024);
  }

  @Test
  void primaryPickupCapsAtOneStack() {
    HighStackHandler handler = new HighStackHandler();
    handler.setStackInSlot(0, new ItemStack(Items.DIRT, 300));
    HighStackCountItemHandlerSlot slot = new HighStackCountItemHandlerSlot(handler, 0, 0, 0);

    ItemStack taken = slot.tryRemove(300, Integer.MAX_VALUE, null).orElse(ItemStack.EMPTY);

    assertThat(taken.getCount()).isEqualTo(64);
    assertThat(handler.getStackInSlot(0).getCount()).isEqualTo(236);
  }

  @Test
  void secondaryPickupTakesHalfUntouched() {
    HighStackHandler handler = new HighStackHandler();
    handler.setStackInSlot(0, new ItemStack(Items.DIRT, 300));
    HighStackCountItemHandlerSlot slot = new HighStackCountItemHandlerSlot(handler, 0, 0, 0);

    ItemStack taken = slot.tryRemove(150, Integer.MAX_VALUE, null).orElse(ItemStack.EMPTY);

    assertThat(taken.getCount()).isEqualTo(150);
    assertThat(handler.getStackInSlot(0).getCount()).isEqualTo(150);
  }

  @Test
  void vanillaSizedPickupUntouched() {
    HighStackHandler handler = new HighStackHandler();
    handler.setStackInSlot(0, new ItemStack(Items.DIRT, 64));
    HighStackCountItemHandlerSlot slot = new HighStackCountItemHandlerSlot(handler, 0, 0, 0);

    ItemStack taken = slot.tryRemove(64, Integer.MAX_VALUE, null).orElse(ItemStack.EMPTY);

    assertThat(taken.getCount()).isEqualTo(64);
    assertThat(handler.getStackInSlot(0).isEmpty()).isTrue();
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

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
      return super.extractItem(slot, Math.min(amount, 64), simulate);
    }
  }
}
