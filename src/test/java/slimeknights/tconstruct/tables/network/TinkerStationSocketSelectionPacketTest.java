package slimeknights.tconstruct.tables.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class TinkerStationSocketSelectionPacketTest extends BaseMcTest {
  @Test
  void roundTripsSelectionState() {
    FriendlyByteBuf written = new FriendlyByteBuf(Unpooled.buffer());
    new TinkerStationSocketSelectionPacket(true, 3).encode(written);

    FriendlyByteBuf read = new FriendlyByteBuf(written.copy());
    TinkerStationSocketSelectionPacket decoded = new TinkerStationSocketSelectionPacket(read);

    FriendlyByteBuf rewritten = new FriendlyByteBuf(Unpooled.buffer());
    decoded.encode(rewritten);

    assertThat(rewritten.readableBytes()).isEqualTo(written.readableBytes());
    assertThat(rewritten.readBoolean()).isEqualTo(written.readBoolean());
    assertThat(rewritten.readVarInt()).isEqualTo(written.readVarInt());
  }
}
