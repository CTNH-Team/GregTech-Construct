package slimeknights.tconstruct.tools.logic;

import net.minecraft.world.entity.LivingEntity;

import java.util.HashSet;
import java.util.Set;

public final class HurtSoundHandler {
  private static final Set<LivingEntity> MARKS = new HashSet<>();
  private static final Set<Integer> REMOTE_MARKS = new HashSet<>();

  private HurtSoundHandler() {}

  public static void mark(LivingEntity entity) {
    MARKS.add(entity);
  }

  public static void markRemote(int entityId) {
    REMOTE_MARKS.add(entityId);
  }

  public static boolean tryRemove(LivingEntity entity) {
    return MARKS.remove(entity);
  }

  public static boolean tryRemoveRemote(int entityId) {
    return REMOTE_MARKS.remove(entityId);
  }
}
