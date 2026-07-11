package slimeknights.tconstruct.library.modifiers.modules.capacity;

import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.special.CapacityBarHook;
import slimeknights.tconstruct.library.tools.nbt.DummyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class CapacityBarValidatorTest extends BaseMcTest {
  @Test
  void validateClampsAmountToCapacity() {
    TestCapacityBar bar = new TestCapacityBar(10, 15);

    new CapacityBarValidator(bar).validate(new DummyToolStack(Items.AIR, ModifierNBT.EMPTY, new ModDataNBT()), ModifierEntry.EMPTY);

    assertThat(bar.amount).isEqualTo(10);
  }

  private static class TestCapacityBar implements CapacityBarHook {
    private final int capacity;
    private int amount;

    private TestCapacityBar(int capacity, int amount) {
      this.capacity = capacity;
      this.amount = amount;
    }

    @Override
    public int getAmount(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool) {
      return amount;
    }

    @Override
    public int getCapacity(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool, ModifierEntry entry) {
      return capacity;
    }

    @Override
    public void setAmount(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool, ModifierEntry entry, int amount) {
      this.amount = amount;
    }
  }
}
