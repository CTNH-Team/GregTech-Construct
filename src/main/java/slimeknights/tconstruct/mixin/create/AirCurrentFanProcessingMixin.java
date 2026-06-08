/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.mixin.create;

import slimeknights.tconstruct.plugin.create.CreateFanProcessingTarget;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AirCurrent.class)
public abstract class AirCurrentFanProcessingMixin {

    @Shadow(remap = false)
    @Final
    public IAirCurrentSource source;

    @Shadow(remap = false)
    public Direction direction;

    @Shadow(remap = false)
    protected abstract int getLimit();

    @Shadow(remap = false)
    public abstract FanProcessingType getTypeAt(float offset);

    @Inject(
            remap = false,
            method = "tick",
            at = @At(
                     value = "INVOKE",
                     target = "Lcom/simibubi/create/content/kinetics/fan/AirCurrent;tickAffectedHandlers()V"))
    private void ctnh$processTargets(CallbackInfo ci) {
        Level level = source.getAirCurrentWorld();
        if (level == null) {
            return;
        }

        BlockPos start = source.getAirCurrentPos();
        float speed = source.getSpeed();
        int limit = getLimit();
        for (int offset = 1; offset <= limit; offset++) {
            FanProcessingType fanProcessingType = getTypeAt(offset - 1);
            BlockEntity blockEntity = level.getBlockEntity(start.relative(direction, offset));
            if (blockEntity instanceof CreateFanProcessingTarget target && target.ctnh$canProcess(fanProcessingType)) {
                target.ctnh$process(fanProcessingType, speed);
            }
        }
    }
}
