package slimeknights.tconstruct.library.tools.helper;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.tools.nbt.DummyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ToolDamageHandlerTest extends BaseMcTest {
  @AfterEach
  void clearPendingQueue() {
    ToolDamageHandler.clearPendingDamageForTests();
  }

  @Test
  void firstQueuedHitAppliesExactlyOnce() {
    TrackingToolStack tool = new TrackingToolStack();
    ItemStack stack = new ItemStack(Items.IRON_CHESTPLATE);

    ToolDamageHandler.accumulate(stack, tool, null, 1, EquipmentSlot.CHEST);
    ToolDamageHandler.flushPendingDamage();

    assertThat(tool.damage.get()).isEqualTo(1);
  }

  private static class TrackingToolStack extends DummyToolStack {
    private final AtomicInteger damage = new AtomicInteger();

    private TrackingToolStack() {
      super(Items.IRON_CHESTPLATE, ModifierNBT.EMPTY, new ModDataNBT());
    }

    @Override
    public StatsNBT getStats() {
      return StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build();
    }

    @Override
    public int getDamage() {
      return damage.get();
    }

    @Override
    public int getCurrentDurability() {
      return 100 - damage.get();
    }

    @Override
    public void setDamage(int damage) {
      this.damage.set(damage);
    }

    @Override
    public boolean hasTag(net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag) {
      return tag == slimeknights.tconstruct.common.TinkerTags.Items.DURABILITY;
    }
  }
}
