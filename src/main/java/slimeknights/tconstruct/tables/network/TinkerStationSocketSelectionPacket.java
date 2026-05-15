package slimeknights.tconstruct.tables.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

@RequiredArgsConstructor
@Getter
public class TinkerStationSocketSelectionPacket implements IThreadsafePacket {
  private final boolean enabled;
  private final int selectedSocket;

  public TinkerStationSocketSelectionPacket(FriendlyByteBuf buffer) {
    this.enabled = buffer.readBoolean();
    this.selectedSocket = buffer.readVarInt();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBoolean(this.enabled);
    buffer.writeVarInt(this.selectedSocket);
  }

  @Override
  public void handleThreadsafe(Context context) {
    ServerPlayer sender = context.getSender();
    if (sender != null) {
      AbstractContainerMenu container = sender.containerMenu;
      if (container instanceof TinkerStationContainerMenu tinker) {
        tinker.setSocketExtractionState(this.enabled, this.selectedSocket);
        TinkerNetwork.getInstance().sendTo(UpdateStationScreenPacket.INSTANCE, sender);
      }
    }
  }
}
