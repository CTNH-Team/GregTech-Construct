/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.mixin.create;

import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.tconstruct.plugin.create.tag.CreateBlockTags;

@Mixin(BlazeBurnerBlockEntity.class)
public class BlazeBurnerBlockEntityTargetMixin {
  @Redirect(
      remap = false,
      method = "isValidBlockAbove",
      at = @At(
          value = "INVOKE",
          target = "Lcom/simibubi/create/content/processing/basin/BasinBlock;isBasin(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
  private boolean tconstruct$acceptTinkerBurnerTargets(LevelReader level, BlockPos pos) {
    return BasinBlock.isBasin(level, pos) || level.getBlockState(pos).is(CreateBlockTags.BLAZE_BURNER_TARGETS);
  }
}
