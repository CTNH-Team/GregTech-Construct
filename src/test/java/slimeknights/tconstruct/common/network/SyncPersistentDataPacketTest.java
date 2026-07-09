package slimeknights.tconstruct.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class SyncPersistentDataPacketTest extends BaseMcTest {
  @Test
  void packetRoundTripsPersistentDataPayload() {
    CompoundTag tag = new CompoundTag();
    tag.putFloat("tconstruct:recurrence", 3.5f);
    tag.putInt("tconstruct:totem", 42);

    FriendlyByteBuf write = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
    new SyncPersistentDataPacket(tag).encode(write);

    SyncPersistentDataPacket decoded = new SyncPersistentDataPacket(new FriendlyByteBuf(write.copy()));
    FriendlyByteBuf rewrite = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
    decoded.encode(rewrite);

    assertThat(rewrite.readNbt()).isEqualTo(tag);
  }

  @Test
  void tinkerNetworkRegistersSyncPersistentDataPacket() throws Exception {
    String source = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/java/slimeknights/tconstruct/common/network/TinkerNetwork.java"));
    assertThat(source).contains("SyncPersistentDataPacket.class");
  }
}
