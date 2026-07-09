package slimeknights.tconstruct.common.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.tools.logic.HurtSoundHandler;

@RequiredArgsConstructor
public class SuppressHurtSoundPacket implements IThreadsafePacket {
  private final int entityId;

  public SuppressHurtSoundPacket(FriendlyByteBuf buffer) {
    this.entityId = buffer.readInt();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeInt(entityId);
  }

  @Override
  public void handleThreadsafe(Context context) {
    HurtSoundHandler.markRemote(entityId);
  }
}
