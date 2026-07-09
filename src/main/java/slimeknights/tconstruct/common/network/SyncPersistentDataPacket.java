package slimeknights.tconstruct.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.tools.logic.PlayerPersistentDataCache;

import java.util.UUID;

/** Packet to sync player persistent data to the client */
public class SyncPersistentDataPacket implements IThreadsafePacket {
  private final UUID playerId;
  private final String key;
  private final float value;
  private final long expiry;

  public SyncPersistentDataPacket(UUID playerId, String key, float value, long expiry) {
    this.playerId = playerId;
    this.key = key;
    this.value = value;
    this.expiry = expiry;
  }

  public SyncPersistentDataPacket(FriendlyByteBuf buffer) {
    this.playerId = buffer.readUUID();
    this.key = buffer.readUtf();
    this.value = buffer.readFloat();
    this.expiry = buffer.readLong();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeUUID(playerId);
    buffer.writeUtf(key);
    buffer.writeFloat(value);
    buffer.writeLong(expiry);
  }

  @Override
  public void handleThreadsafe(Context context) {
    HandleClient.handle(this);
  }

  /** Handles client side only code safely */
  private static class HandleClient {
    private static void handle(SyncPersistentDataPacket packet) {
      PlayerPersistentDataCache.put(packet.playerId, packet.key, packet.value, packet.expiry);
    }
  }
}
