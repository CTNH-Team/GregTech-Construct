/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.burner;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

public final class BlazeBurnerHeat {
  private BlazeBurnerHeat() {}

  public static int getTemperature(BlazeBurnerBlock.HeatLevel level) {
    return switch (level) {
      case SMOULDERING -> 400;
      case FADING, KINDLED -> 800;
      case SEETHING -> 1600;
      default -> 0;
    };
  }

  public static int getRate(BlazeBurnerBlock.HeatLevel level) {
    return switch (level) {
      case SMOULDERING -> 5;
      case FADING, KINDLED -> 10;
      case SEETHING -> 20;
      default -> 0;
    };
  }

  public static boolean isHeating(BlazeBurnerBlock.HeatLevel level) {
    return level.isAtLeast(BlazeBurnerBlock.HeatLevel.SMOULDERING);
  }

  /** Maps a heat level to the GUI flame display, 0 = standard, 1 = dim, 2 = blue (seething) */
  public static int getGuiHeatLevel(BlazeBurnerBlock.HeatLevel level) {
    return switch (level) {
      case SMOULDERING, FADING -> 1;
      case SEETHING -> 2;
      default -> 0;
    };
  }
}
