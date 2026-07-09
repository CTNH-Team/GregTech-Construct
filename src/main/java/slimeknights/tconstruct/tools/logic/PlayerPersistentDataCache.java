package slimeknights.tconstruct.tools.logic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.common.network.SyncPersistentDataPacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PlayerPersistentDataCache {
  private static final Map<UUID,Map<String,Entry>> CLIENT_CACHE = new HashMap<>();
  private static final Map<UUID,List<String>> MARKER = new HashMap<>();
  private static Function<Player,ModDataNBT> dataGetter = PersistentDataCapability::getOrWarn;

  private PlayerPersistentDataCache() {}

  public static void put(UUID playerId, String key, float value, long expiry) {
    CLIENT_CACHE.computeIfAbsent(playerId, unused -> new HashMap<>()).put(key, new Entry(value, expiry));
  }

  public static float get(UUID playerId, String key, long gameTime) {
    Map<String,Entry> playerCache = CLIENT_CACHE.get(playerId);
    if (playerCache == null) {
      return 0;
    }
    Entry entry = playerCache.get(key);
    if (entry == null) {
      return 0;
    }
    if (entry.expiry > 0 && gameTime > entry.expiry) {
      playerCache.remove(key);
      return 0;
    }
    return entry.value;
  }

  public static void mark(UUID playerId, String key) {
    List<String> keys = MARKER.computeIfAbsent(playerId, unused -> new ArrayList<>());
    if (!keys.contains(key)) {
      keys.add(key);
    }
  }

  public static void remove(UUID playerId) {
    CLIENT_CACHE.remove(playerId);
    MARKER.remove(playerId);
  }

  public static void syncFromEntity(LivingEntity entity) {
    if (entity.level().isClientSide || !(entity instanceof Player player)) {
      return;
    }
    sync(player.getUUID(), getEntityData(player), packet -> TinkerNetwork.getInstance().sendToTrackingAndSelf(packet, player));
  }

  public static ModDataNBT getEntityData(Player player) {
    return dataGetter.apply(player);
  }

  public static void sync(UUID playerId, ModDataNBT persistentData, Consumer<SyncPersistentDataPacket> sender) {
    List<String> keys = MARKER.remove(playerId);
    if (keys == null || keys.isEmpty()) {
      return;
    }
    for (String key : keys) {
      ResourceLocation dataKey = ResourceLocation.tryParse(key);
      if (dataKey == null) {
        continue;
      }
      ResourceLocation expiryKey = ResourceLocation.tryParse(key + "_expiry");
      if (expiryKey == null) {
        continue;
      }
      sender.accept(new SyncPersistentDataPacket(playerId, key, persistentData.getFloat(dataKey), persistentData.getLong(expiryKey)));
    }
  }

  public static void setDataGetter(Function<Player,ModDataNBT> getter) {
    dataGetter = getter;
  }

  public static void resetDataGetter() {
    dataGetter = PersistentDataCapability::getOrWarn;
  }

  private record Entry(float value, long expiry) {}
}
