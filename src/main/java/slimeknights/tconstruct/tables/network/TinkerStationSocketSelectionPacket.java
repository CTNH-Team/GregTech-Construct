package slimeknights.tconstruct.tables.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

@RequiredArgsConstructor
@Getter
public class TinkerStationSocketSelectionPacket implements IThreadsafePacket {
  private final InteractionType interactionType;
  private final boolean gemModeEnabled;
  private final int socketIndex;

  public static TinkerStationSocketSelectionPacket toggleMode(boolean enabled) {
    return new TinkerStationSocketSelectionPacket(InteractionType.TOGGLE_MODE, enabled, -1);
  }

  public static TinkerStationSocketSelectionPacket insertFromCarried(int socketIndex) {
    return new TinkerStationSocketSelectionPacket(InteractionType.INSERT_FROM_CARRIED, false, socketIndex);
  }

  public static TinkerStationSocketSelectionPacket removeToPlayer(int socketIndex) {
    return new TinkerStationSocketSelectionPacket(InteractionType.REMOVE_TO_PLAYER, false, socketIndex);
  }

  /** @deprecated compatibility constructor for older callers pending client cleanup */
  @Deprecated(forRemoval = false)
  public TinkerStationSocketSelectionPacket(boolean enabled, int selectedSocket) {
    this(enabled ? InteractionType.REMOVE_TO_PLAYER : InteractionType.TOGGLE_MODE, enabled, selectedSocket);
  }

  public TinkerStationSocketSelectionPacket(FriendlyByteBuf buffer) {
    this.interactionType = buffer.readEnum(InteractionType.class);
    this.gemModeEnabled = buffer.readBoolean();
    this.socketIndex = buffer.readVarInt();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeEnum(this.interactionType);
    buffer.writeBoolean(this.gemModeEnabled);
    buffer.writeVarInt(this.socketIndex);
  }

  @Override
  public void handleThreadsafe(Context context) {
    ServerPlayer sender = context.getSender();
    if (sender != null) {
      AbstractContainerMenu container = sender.containerMenu;
      if (container instanceof TinkerStationContainerMenu tinker) {
        if (this.interactionType == InteractionType.TOGGLE_MODE) {
          tinker.setGemMode(this.gemModeEnabled);
        } else {
          tinker.handleSocketInteraction(sender, this.interactionType, this.socketIndex);
        }
      }
    }
  }

  public enum InteractionType {
    TOGGLE_MODE,
    INSERT_FROM_CARRIED,
    REMOVE_TO_PLAYER
  }
}
