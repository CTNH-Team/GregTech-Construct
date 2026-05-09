package slimeknights.tconstruct.library.tools.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.entity.LivingEntity;

/**
 * 空操作消费者，用于 hurtAndBreak 方法的回调
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NoOpConsumers {
    public static void LivingEntity(LivingEntity e) { }
}
