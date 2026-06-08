/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.burner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.SolidFuelModule;

public class CreateHeatFuelModule extends SolidFuelModule {
  private final BlockPos fuelPos;

  public CreateHeatFuelModule(MantleBlockEntity parent, BlockPos fuelPos) {
    super(parent, fuelPos);
    this.fuelPos = fuelPos;
  }

  @Override
  public int findFuel(boolean consume) {
    HeatSourceData heatSource = findHeatSource();
    if (heatSource != null && heatSource.isHeating() && heatSource.temperature() > 0) {
      if (consume) {
        fuel = 1;
        fuelQuality = 1;
        temperature = heatSource.temperature();
        rate = heatSource.rate();
        parent.setChangedFast();
      }
      return heatSource.temperature();
    }
    return super.findFuel(consume);
  }

  private HeatSourceData findHeatSource() {
    Level level = getLevel();
    Block block = level.getBlockState(fuelPos).getBlock();
    if (block instanceof CreateBlockHeatSource heatSource) {
      return new HeatSourceData(heatSource.tconstruct$getTemperature(level, fuelPos), heatSource.tconstruct$getRate(level, fuelPos),
          heatSource.tconstruct$isHeating(level, fuelPos));
    }
    return null;
  }

  private record HeatSourceData(int temperature, int rate, boolean isHeating) {}
}
