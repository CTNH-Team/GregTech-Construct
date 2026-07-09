package slimeknights.tconstruct.common.network;

import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class SyncPersistentDataPacketTest extends BaseMcTest {
  @Test
  void tinkerNetworkRegistersSyncPersistentDataPacket() throws Exception {
    String source = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/java/slimeknights/tconstruct/common/network/TinkerNetwork.java"));
    assertThat(source).contains("SyncPersistentDataPacket.class");
  }

  @Test
  void persistentDataCapabilityNoLongerSendsFullSyncOnPlayerLifecycle() throws Exception {
    String source = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/java/slimeknights/tconstruct/library/tools/capability/PersistentDataCapability.java"));
    assertThat(source).doesNotContain("new SyncPersistentDataPacket(data.getCopy())");
  }

  @Test
  void packetRoundTripsIncrementalPersistentDataPayload() {
    FriendlyByteBuf write = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
    java.util.UUID playerId = java.util.UUID.randomUUID();
    new SyncPersistentDataPacket(playerId, "tconstruct:test_key", 2.5f, 77L).encode(write);

    SyncPersistentDataPacket decoded = new SyncPersistentDataPacket(new FriendlyByteBuf(write.copy()));
    FriendlyByteBuf rewrite = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
    decoded.encode(rewrite);

    assertThat(rewrite.readUUID()).isEqualTo(playerId);
    assertThat(rewrite.readUtf()).isEqualTo("tconstruct:test_key");
    assertThat(rewrite.readFloat()).isEqualTo(2.5f);
    assertThat(rewrite.readLong()).isEqualTo(77L);
  }
}
