package slimeknights.tconstruct.tables.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TinkerStationSocketSelectionPacketTest extends BaseMcTest {
  @Test
  void roundTripsInteractionState() {
    FriendlyByteBuf written = new FriendlyByteBuf(Unpooled.buffer());
    TinkerStationSocketSelectionPacket.insertFromCarried(3).encode(written);

    FriendlyByteBuf read = new FriendlyByteBuf(written.copy());
    TinkerStationSocketSelectionPacket decoded = new TinkerStationSocketSelectionPacket(read);

    FriendlyByteBuf rewritten = new FriendlyByteBuf(Unpooled.buffer());
    decoded.encode(rewritten);

    assertThat(rewritten.readableBytes()).isEqualTo(written.readableBytes());
    assertThat(rewritten.readEnum(TinkerStationSocketSelectionPacket.InteractionType.class))
      .isEqualTo(written.readEnum(TinkerStationSocketSelectionPacket.InteractionType.class));
    assertThat(rewritten.readBoolean()).isEqualTo(written.readBoolean());
    assertThat(rewritten.readVarInt()).isEqualTo(written.readVarInt());
  }

  @Test
  void handlingToggleModeUpdatesSharedGemMode() {
    TinkerStationSocketSelectionPacket packet = TinkerStationSocketSelectionPacket.toggleMode(true);
    Context context = Mockito.mock(Context.class);
    ServerPlayer sender = Mockito.mock(ServerPlayer.class);
    TinkerStationContainerMenu menu = Mockito.mock(TinkerStationContainerMenu.class);

    when(context.getSender()).thenReturn(sender);
    setContainerMenu(sender, menu);

    packet.handleThreadsafe(context);

    verify(menu).setGemMode(true);
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
