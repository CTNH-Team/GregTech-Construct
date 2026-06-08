/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create;

import net.minecraft.resources.ResourceLocation;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;

import java.util.Map;

public final class CreateFanCastingAcceleration {

    private static final double DEFAULT_FACTOR = 1.0D;
    private static final double BASE_SPEED = 64.0D;
    private static final Map<ResourceLocation, Double> FACTORS = Map.of(
            ResourceLocation.tryBuild("create", "smoking"), -0.5D,
            ResourceLocation.tryBuild("create", "haunting"), -0.75D,
            ResourceLocation.tryBuild("create", "blasting"), -1.0D,
            ResourceLocation.tryBuild("create", "splashing"), 1.5D,
            ResourceLocation.tryBuild("create_henry", "seething"), -2.0D,
            ResourceLocation.tryBuild("create_henry", "freezing"), 2.0D);

    private CreateFanCastingAcceleration() {}

    public static double ticksFor(FanProcessingType fanProcessingType, float speed) {
        if (speed <= 0.0F) {
            return 0.0D;
        }
        return Math.sqrt(speed / BASE_SPEED) * factorFor(fanProcessingType);
    }

    private static double factorFor(FanProcessingType fanProcessingType) {
        if (fanProcessingType == null) {
            return DEFAULT_FACTOR;
        }
        ResourceLocation id = CreateBuiltInRegistries.FAN_PROCESSING_TYPE.getKey(fanProcessingType);
        return FACTORS.getOrDefault(id, DEFAULT_FACTOR);
    }
}
