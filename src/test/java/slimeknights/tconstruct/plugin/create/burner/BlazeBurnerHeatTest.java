package slimeknights.tconstruct.plugin.create.burner;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies the GUI flame display mapping per blaze burner heat level */
public class BlazeBurnerHeatTest {
  @Test
  void mapsHeatLevelsToFlameDisplays() {
    assertThat(BlazeBurnerHeat.getGuiHeatLevel(BlazeBurnerBlock.HeatLevel.NONE)).isZero();
    assertThat(BlazeBurnerHeat.getGuiHeatLevel(BlazeBurnerBlock.HeatLevel.SMOULDERING)).isEqualTo(1);
    assertThat(BlazeBurnerHeat.getGuiHeatLevel(BlazeBurnerBlock.HeatLevel.FADING)).isEqualTo(1);
    assertThat(BlazeBurnerHeat.getGuiHeatLevel(BlazeBurnerBlock.HeatLevel.KINDLED)).isZero();
    assertThat(BlazeBurnerHeat.getGuiHeatLevel(BlazeBurnerBlock.HeatLevel.SEETHING)).isEqualTo(2);
  }
}
