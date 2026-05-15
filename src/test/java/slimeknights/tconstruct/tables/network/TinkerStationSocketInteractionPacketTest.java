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

class TinkerStationSocketInteractionPacketTest extends BaseMcTest {
  @Test
  void roundTripsModeToggleIntent() {
    FriendlyByteBuf written = new FriendlyByteBuf(Unpooled.buffer());
    TinkerStationSocketSelectionPacket.toggleMode(true).encode(written);

    TinkerStationSocketSelectionPacket decoded = new TinkerStationSocketSelectionPacket(new FriendlyByteBuf(written.copy()));

    assertThat(decoded.getInteractionType()).isEqualTo(TinkerStationSocketSelectionPacket.InteractionType.TOGGLE_MODE);
    assertThat(decoded.isGemModeEnabled()).isTrue();
    assertThat(decoded.getSocketIndex()).isEqualTo(-1);
  }

  @Test
  void roundTripsSocketInteractionIntent() {
    FriendlyByteBuf written = new FriendlyByteBuf(Unpooled.buffer());
    TinkerStationSocketSelectionPacket.removeToPlayer(3).encode(written);

    TinkerStationSocketSelectionPacket decoded = new TinkerStationSocketSelectionPacket(new FriendlyByteBuf(written.copy()));

    assertThat(decoded.getInteractionType()).isEqualTo(TinkerStationSocketSelectionPacket.InteractionType.REMOVE_TO_PLAYER);
    assertThat(decoded.isGemModeEnabled()).isFalse();
    assertThat(decoded.getSocketIndex()).isEqualTo(3);
  }

  @Test
  void handlingToggleIntentUpdatesSharedGemMode() {
    TinkerStationSocketSelectionPacket packet = TinkerStationSocketSelectionPacket.toggleMode(true);
    Context context = Mockito.mock(Context.class);
    ServerPlayer sender = Mockito.mock(ServerPlayer.class);
    TinkerStationContainerMenu menu = Mockito.mock(TinkerStationContainerMenu.class);

    when(context.getSender()).thenReturn(sender);
    setContainerMenu(sender, menu);

    packet.handleThreadsafe(context);

    verify(menu).setGemMode(true);
  }

  @Test
  void handlingSocketInteractionDelegatesToMenu() {
    TinkerStationSocketSelectionPacket packet = TinkerStationSocketSelectionPacket.insertFromCarried(2);
    Context context = Mockito.mock(Context.class);
    ServerPlayer sender = Mockito.mock(ServerPlayer.class);
    TinkerStationContainerMenu menu = Mockito.mock(TinkerStationContainerMenu.class);

    when(context.getSender()).thenReturn(sender);
    setContainerMenu(sender, menu);

    packet.handleThreadsafe(context);

    verify(menu).handleSocketInteraction(sender, TinkerStationSocketSelectionPacket.InteractionType.INSERT_FROM_CARRIED, 2);
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
