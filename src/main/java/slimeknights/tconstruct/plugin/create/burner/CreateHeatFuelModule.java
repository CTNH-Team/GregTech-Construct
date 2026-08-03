/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.burner;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
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
        // large buffer so the flame does not flicker while refilling
        fuel = 100;
        fuelQuality = 100;
        temperature = heatSource.temperature();
        rate = heatSource.rate();
        heatLevel = heatSource.heatLevel();
        parent.setChangedFast();
      }
      return heatSource.temperature();
    }
    heatLevel = 0;
    return super.findFuel(consume);
  }

  @Override
  public void decreaseFuel(int amount) {
    HeatSourceData heatSource = findHeatSource();
    if (heatSource == null || !heatSource.isHeating() || heatSource.temperature() <= 0) {
      // the burner stopped providing heat: drop the buffered blaze heat immediately,
      // so the next tick refinds fuel from whatever source is below now
      fuel = 0;
      fuelQuality = 0;
      heatLevel = 0;
      parent.setChangedFast();
      return;
    }
    super.decreaseFuel(amount);
  }

  @Override
  public boolean isFreeHeat() {
    HeatSourceData heatSource = findHeatSource();
    return heatSource != null && heatSource.isHeating() && heatSource.temperature() > 0;
  }

  /* Live heat state */
  // the burner heat level can change while the melter still has buffered fuel, so read it live
  // instead of waiting for the next fuel refill to sync the stored values

  @Override
  public int getTemperature() {
    HeatSourceData heatSource = findHeatSource();
    if (heatSource != null && heatSource.isHeating()) {
      return heatSource.temperature();
    }
    return super.getTemperature();
  }

  @Override
  public int getRate() {
    HeatSourceData heatSource = findHeatSource();
    if (heatSource != null && heatSource.isHeating()) {
      return heatSource.rate();
    }
    return super.getRate();
  }

  @Override
  public int getHeatLevel() {
    HeatSourceData heatSource = findHeatSource();
    if (heatSource != null) {
      return heatSource.heatLevel();
    }
    // never show the burner UI without a burner below, even if a stale value is still buffered
    return 0;
  }

  private HeatSourceData findHeatSource() {
    Level level = getLevel();
    Block block = level.getBlockState(fuelPos).getBlock();
    if (block instanceof CreateBlockHeatSource heatSource) {
      int guiHeatLevel = 0;
      if (block instanceof BlazeBurnerBlock) {
        guiHeatLevel = BlazeBurnerHeat.getGuiHeatLevel(level.getBlockState(fuelPos).getValue(BlazeBurnerBlock.HEAT_LEVEL));
      }
      return new HeatSourceData(heatSource.tconstruct$getTemperature(level, fuelPos), heatSource.tconstruct$getRate(level, fuelPos),
          heatSource.tconstruct$isHeating(level, fuelPos), guiHeatLevel);
    }
    return null;
  }

  private record HeatSourceData(int temperature, int rate, boolean isHeating, int heatLevel) {}
}
