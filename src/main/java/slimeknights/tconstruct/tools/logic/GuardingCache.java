package slimeknights.tconstruct.tools.logic;

import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.common.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GuardingCache {
  private static final Map<UUID,Map<ModifierId,Integer>> MODIFIER_MARKS = new HashMap<>();
  private static final Map<UUID,Map<UUID,Long>> HOSTILITY = new HashMap<>();

  private GuardingCache() {}

  public static boolean hasAnyHook(UUID player) {
    Map<ModifierId,Integer> hooks = MODIFIER_MARKS.get(player);
    return hooks != null && !hooks.isEmpty();
  }

  public static boolean hasHook(UUID player, ModifierId id) {
    Map<ModifierId,Integer> hooks = MODIFIER_MARKS.get(player);
    return hooks != null && hooks.getOrDefault(id, 0) > 0;
  }

  public static void addHook(UUID player, ModifierId id) {
    MODIFIER_MARKS.computeIfAbsent(player, unused -> new HashMap<>()).merge(id, 1, Integer::sum);
  }

  public static void removeHook(UUID player, ModifierId id) {
    Map<ModifierId,Integer> hooks = MODIFIER_MARKS.get(player);
    if (hooks == null) {
      return;
    }
    int remaining = hooks.getOrDefault(id, 0) - 1;
    if (remaining > 0) {
      hooks.put(id, remaining);
    } else {
      hooks.remove(id);
      if (hooks.isEmpty()) {
        MODIFIER_MARKS.remove(player);
      }
    }
  }

  public static void removePlayer(UUID player) {
    MODIFIER_MARKS.remove(player);
  }

  public static void recordHostility(UUID first, UUID second, long gameTime) {
    long expiry = gameTime + 20L * Config.guardingHostilityDurationSeconds();
    HOSTILITY.computeIfAbsent(first, unused -> new HashMap<>()).put(second, expiry);
    HOSTILITY.computeIfAbsent(second, unused -> new HashMap<>()).put(first, expiry);
  }

  public static boolean isHostile(UUID self, UUID other, long gameTime) {
    Map<UUID,Long> relations = HOSTILITY.get(self);
    if (relations == null) {
      return false;
    }
    Long expiry = relations.get(other);
    if (expiry == null) {
      return false;
    }
    if (expiry <= gameTime) {
      relations.remove(other);
      if (relations.isEmpty()) {
        HOSTILITY.remove(self);
      }
      return false;
    }
    return true;
  }

  public static void clearHostilityFor(UUID player) {
    HOSTILITY.remove(player);
  }

  public static void clearForTests() {
    MODIFIER_MARKS.clear();
    HOSTILITY.clear();
  }
}
