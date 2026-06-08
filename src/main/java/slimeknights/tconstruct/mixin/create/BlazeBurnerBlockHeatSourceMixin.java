/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.mixin.create;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import slimeknights.tconstruct.plugin.create.burner.BlazeBurnerHeat;
import slimeknights.tconstruct.plugin.create.burner.CreateBlockHeatSource;

@Mixin(BlazeBurnerBlock.class)
public class BlazeBurnerBlockHeatSourceMixin implements CreateBlockHeatSource {
  @Override
  public int tconstruct$getTemperature(Level level, BlockPos pos) {
    return BlazeBurnerHeat.getTemperature(getHeatLevel(level, pos));
  }

  @Override
  public int tconstruct$getRate(Level level, BlockPos pos) {
    return BlazeBurnerHeat.getRate(getHeatLevel(level, pos));
  }

  @Override
  public boolean tconstruct$isHeating(Level level, BlockPos pos) {
    return BlazeBurnerHeat.isHeating(getHeatLevel(level, pos));
  }

  private BlazeBurnerBlock.HeatLevel getHeatLevel(Level level, BlockPos pos) {
    BlockState state = level.getBlockState(pos);
    return state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
  }
}
