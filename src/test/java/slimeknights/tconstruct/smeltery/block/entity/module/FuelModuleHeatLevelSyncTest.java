package slimeknights.tconstruct.smeltery.block.entity.module;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies the heat level display value syncs as the fifth container slot and defaults to standard fuel */
public class FuelModuleHeatLevelSyncTest {
  private static class StubFuelModule extends FuelModule {
    StubFuelModule() {
      super(null);
    }

    @Override
    public int findFuel(boolean consume) {
      return 0;
    }
  }

  @Test
  void heatLevelSyncsAsFifthContainerSlot() {
    FuelModule module = new StubFuelModule();
    assertThat(module.getCount()).isEqualTo(5);
    assertThat(module.getHeatLevel()).isZero();
    module.set(4, 2);
    assertThat(module.get(4)).isEqualTo(2);
    assertThat(module.getHeatLevel()).isEqualTo(2);
  }

  @Test
  void freeHeatDefaultsToFalse() {
    assertThat(new StubFuelModule().isFreeHeat()).isFalse();
  }
}
