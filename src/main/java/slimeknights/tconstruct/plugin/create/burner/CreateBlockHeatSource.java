/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.burner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface CreateBlockHeatSource {
  int tconstruct$getTemperature(Level level, BlockPos pos);

  int tconstruct$getRate(Level level, BlockPos pos);

  boolean tconstruct$isHeating(Level level, BlockPos pos);
}
