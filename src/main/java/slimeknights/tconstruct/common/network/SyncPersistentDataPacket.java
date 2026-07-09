package slimeknights.tconstruct.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.tools.logic.PlayerPersistentDataCache;

import java.util.UUID;

/** Packet to sync player persistent data to the client */
public class SyncPersistentDataPacket implements IThreadsafePacket {
  private final CompoundTag data;
  private final UUID playerId;
  private final String key;
  private final float value;
  private final long expiry;
  private final boolean incremental;

  public SyncPersistentDataPacket(CompoundTag data) {
    this.data = data;
    this.playerId = null;
    this.key = null;
    this.value = 0;
    this.expiry = 0;
    this.incremental = false;
  }

  public SyncPersistentDataPacket(UUID playerId, String key, float value, long expiry) {
    this.data = null;
    this.playerId = playerId;
    this.key = key;
    this.value = value;
    this.expiry = expiry;
    this.incremental = true;
  }

  public SyncPersistentDataPacket(FriendlyByteBuf buffer) {
    this.incremental = buffer.readBoolean();
    if (incremental) {
      this.playerId = buffer.readUUID();
      this.key = buffer.readUtf();
      this.value = buffer.readFloat();
      this.expiry = buffer.readLong();
      this.data = null;
    } else {
      this.data = buffer.readNbt();
      this.playerId = null;
      this.key = null;
      this.value = 0;
      this.expiry = 0;
    }
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBoolean(incremental);
    if (incremental) {
      buffer.writeUUID(playerId);
      buffer.writeUtf(key);
      buffer.writeFloat(value);
      buffer.writeLong(expiry);
    } else {
      buffer.writeNbt(data);
    }
  }

  @Override
  public void handleThreadsafe(Context context) {
    HandleClient.handle(this);
  }

  /** Handles client side only code safely */
  private static class HandleClient {
    private static void handle(SyncPersistentDataPacket packet) {
      Player player = Minecraft.getInstance().player;
      if (packet.incremental) {
        PlayerPersistentDataCache.put(packet.playerId, packet.key, packet.value, packet.expiry);
      } else if (player != null) {
        player.getCapability(PersistentDataCapability.CAPABILITY).ifPresent(data -> data.copyFrom(packet.data));
      }
    }
  }
}
