/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.mixin.tconstruct.create;

import slimeknights.tconstruct.plugin.create.CreateFanCastingAcceleration;
import slimeknights.tconstruct.plugin.create.CreateFanProcessingTarget;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;

@Mixin(CastingBlockEntity.class)
public class CastingBlockEntityFanProcessingMixin implements CreateFanProcessingTarget {

    @Shadow(remap = false)
    private int timer;

    @Shadow(remap = false)
    private int coolingTime;

    @Shadow(remap = false)
    private ICastingRecipe currentRecipe;

    @Unique
    private double ctnh$fanProcessingRemainder;

    @Override
    public boolean ctnh$canProcess(FanProcessingType fanProcessingType) {
        return currentRecipe != null;
    }

    @Override
    public void ctnh$process(FanProcessingType fanProcessingType, float speed) {
        double ticks = CreateFanCastingAcceleration.ticksFor(fanProcessingType, speed);
        if (ticks == 0.0D) {
            return;
        }

        int sign = ticks < 0.0D ? -1 : 1;
        double absoluteTicks = Math.abs(ticks);
        int wholeTicks = (int) absoluteTicks;
        ctnh$fanProcessingRemainder += absoluteTicks - wholeTicks;
        if (ctnh$fanProcessingRemainder >= 1.0D) {
            ctnh$fanProcessingRemainder -= 1.0D;
            wholeTicks++;
        }

        ctnh$adjustCooling(sign * wholeTicks);
    }

    @Unique
    private void ctnh$adjustCooling(int ticks) {
        if (ticks == 0 || timer <= 0) {
            return;
        }
        timer = Math.max(0, Math.min(timer + ticks, coolingTime));
    }
}
