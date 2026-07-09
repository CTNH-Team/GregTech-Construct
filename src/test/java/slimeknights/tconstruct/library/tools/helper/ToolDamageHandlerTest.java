package slimeknights.tconstruct.library.tools.helper;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ToolDamageHandlerTest extends BaseMcTest {
  @AfterEach
  void clearPendingQueue() {
    ToolDamageHandler.clearPendingDamageForTests();
  }

  @Test
  void firstQueuedHitAppliesExactlyOnce() {
    TrackingToolStack tool = new TrackingToolStack();
    ItemStack stack = new ItemStack(Items.IRON_CHESTPLATE);

    ToolDamageHandler.accumulate(stack, tool, null, 1);
    ToolDamageHandler.flushPendingDamage();

    assertThat(tool.damage.get()).isEqualTo(1);
  }

  @Test
  void deferredDamageDoesNotBroadcastBreakEvent() {
    TrackingToolStack tool = new TrackingToolStack(1);
    ItemStack stack = new ItemStack(Items.IRON_CHESTPLATE);
    LivingEntity holder = mock(LivingEntity.class);

    ToolDamageHandler.accumulate(stack, tool, holder, 1);
    ToolDamageHandler.flushPendingDamage();

    verify(holder, never()).broadcastBreakEvent(EquipmentSlot.CHEST);
  }

  private static class TrackingToolStack extends DummyToolStack {
    private final AtomicInteger damage = new AtomicInteger();

    private TrackingToolStack() {
      this(100);
    }

    private TrackingToolStack(int durability) {
      super(Items.IRON_CHESTPLATE, ModifierNBT.EMPTY, new ModDataNBT());
      this.durability = durability;
    }

    private final int durability;

    @Override
    public StatsNBT getStats() {
      return StatsNBT.builder().set(ToolStats.DURABILITY, durability).build();
    }

    @Override
    public int getDamage() {
      return damage.get();
    }

    @Override
    public int getCurrentDurability() {
      return durability - damage.get();
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
