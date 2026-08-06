package slimeknights.tconstruct.common.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class SyncContainerStackCodecTest extends BaseMcTest {
  @Test
  void roundTripsCountsOutsideVanillaByteRange() {
    for (int count : new int[]{65, 127, 128, 256, 1024}) {
      ItemStack decoded = roundTrip(new ItemStack(Items.DIRT, count));
      assertThat(decoded.getItem()).isEqualTo(Items.DIRT);
      assertThat(decoded.getCount()).isEqualTo(count);
    }
  }

  @Test
  void preservesVanillaCountsAndEmptyStacks() {
    assertThat(roundTrip(ItemStack.EMPTY).isEmpty()).isTrue();
    for (int count : new int[]{1, 64}) {
      assertThat(roundTrip(new ItemStack(Items.DIRT, count)).getCount()).isEqualTo(count);
    }
  }

  @Test
  void repeatedSynchronizationDoesNotChangeCount() {
    ItemStack stack = new ItemStack(Items.DIRT, 1024);
    for (int sync = 0; sync < 100; sync++) {
      stack = roundTrip(stack);
    }
    assertThat(stack.getCount()).isEqualTo(1024);
  }

  private static ItemStack roundTrip(ItemStack stack) {
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    SyncContainerStackCodec.write(buffer, stack);
    return SyncContainerStackCodec.read(buffer);
  }
}
