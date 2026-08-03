package slimeknights.tconstruct.smeltery.client.screen.module;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies the blaze burner flame texture only tints the three flame lines, never the neutral background */
public class BlazeFireTextureTest {
  private static final int FLAME_SIZE = 14;
  private static final int SOURCE_X = 176;
  private static final int SOURCE_Y = 136;

  private static BufferedImage load(String path) throws IOException {
    try (InputStream stream = BlazeFireTextureTest.class.getResourceAsStream(path)) {
      return ImageIO.read(Objects.requireNonNull(stream, "Missing texture " + path));
    }
  }

  /** Flame line pixels are saturated warm colors; the background is neutral gray/white */
  private static boolean isWarm(int rgb) {
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = rgb & 0xFF;
    return Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) > 0;
  }

  @Test
  void tintsOnlyTheThreeFlameLines() throws IOException {
    BufferedImage source = load("/assets/tconstruct/textures/gui/melter.png");
    BufferedImage fire = load("/assets/tconstruct/textures/gui/blaze_fire.png");

    int warm = 0;
    int neutral = 0;
    for (int y = 0; y < FLAME_SIZE; y++) {
      for (int x = 0; x < FLAME_SIZE; x++) {
        int src = source.getRGB(SOURCE_X + x, SOURCE_Y + y);
        int dim = fire.getRGB(x, y);
        int blue = fire.getRGB(x, y + FLAME_SIZE);
        if (isWarm(src)) {
          warm++;
          assertThat(dim).as("dim flame line pixel (%s,%s)", x, y).isNotEqualTo(src);
          assertThat(blue).as("blue flame line pixel (%s,%s)", x, y).isNotEqualTo(src);
          int blueR = (blue >> 16) & 0xFF;
          int blueB = blue & 0xFF;
          assertThat(blueB).as("blue flame line should be blue-ish (%s,%s)", x, y).isGreaterThan(blueR);
        } else {
          neutral++;
          assertThat(dim).as("dim background pixel (%s,%s)", x, y).isEqualTo(src);
          assertThat(blue).as("blue background pixel (%s,%s)", x, y).isEqualTo(src);
        }
      }
    }
    // locks the current sprite layout: 61 flame line pixels and 135 neutral background pixels
    assertThat(warm).isEqualTo(61);
    assertThat(neutral).isEqualTo(135);
  }
}
