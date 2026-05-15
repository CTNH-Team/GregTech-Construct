package slimeknights.tconstruct.tables.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  @Test
  void handlingUpdatesMenuStateAndRefreshesInitiatingClient() {
    TinkerStationSocketSelectionPacket packet = new TinkerStationSocketSelectionPacket(true, 2);
    Context context = Mockito.mock(Context.class);
    ServerPlayer sender = Mockito.mock(ServerPlayer.class);
    TinkerStationContainerMenu menu = Mockito.mock(TinkerStationContainerMenu.class);
    TinkerNetwork network = Mockito.mock(TinkerNetwork.class);

    when(context.getSender()).thenReturn(sender);
    setContainerMenu(sender, menu);

    try (MockedStatic<TinkerNetwork> tinkerNetwork = Mockito.mockStatic(TinkerNetwork.class)) {
      tinkerNetwork.when(TinkerNetwork::getInstance).thenReturn(network);

      packet.handleThreadsafe(context);
    }

    verify(menu).setSocketExtractionState(true, 2);
    verify(network).sendTo(UpdateStationScreenPacket.INSTANCE, sender);
  }

  private static void setContainerMenu(ServerPlayer sender, TinkerStationContainerMenu menu) {
    try {
      java.lang.reflect.Field field = net.minecraft.world.entity.player.Player.class.getField("containerMenu");
      field.set(sender, menu);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
