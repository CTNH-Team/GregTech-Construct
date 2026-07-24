package slimeknights.tconstruct.tools.stats;

import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class ArmorPartMaterialStatsTest extends BaseMcTest {
  @Test
  void armorPartStatsExposeLocalizedTooltipValues() {
    var maille = ArmorPartMaterialStats.maille(0.2f, 0.05f, 0.1f, 0.25f);
    var armorMail = new ArmorPartMaterialStats.ArmorLayerStats(
      ArmorPartMaterialStats.ARMOR_MAIL, 0.2f, 0.05f, 3f, 1.25f, 0f, 0.05f);
    var armorPlate = new ArmorPartMaterialStats.ArmorLayerStats(
      ArmorPartMaterialStats.ARMOR_PLATE, 0.2f, 0.05f, 3f, 1.25f, 0.6f, 0.05f);
    var cast = new ArmorPartMaterialStats.ArmorCoreStats(
      ArmorPartMaterialStats.CAST_CHESTPLATE, 820, 7f, 3f, 1.25f, 0.6f, 0.04f, 0.05f);
    var frame = new ArmorPartMaterialStats.ArmorFrameStats(
      ArmorPartMaterialStats.FRAME_CHESTPLATE, 820, 7f, 0.2f, -0.1f, 0.05f);

    assertThat(maille.getLocalizedInfo()).hasSize(4);
    assertThat(armorMail.getLocalizedInfo()).hasSize(5);
    assertThat(armorPlate.getLocalizedInfo()).hasSize(6);
    assertThat(cast.getLocalizedInfo()).hasSize(7);
    assertThat(frame.getLocalizedInfo()).hasSize(5);

    assertThat(maille.getLocalizedDescriptions()).hasSameSizeAs(maille.getLocalizedInfo());
    assertThat(armorMail.getLocalizedDescriptions()).hasSameSizeAs(armorMail.getLocalizedInfo());
    assertThat(armorPlate.getLocalizedDescriptions()).hasSameSizeAs(armorPlate.getLocalizedInfo());
    assertThat(cast.getLocalizedDescriptions()).hasSameSizeAs(cast.getLocalizedInfo());
    assertThat(frame.getLocalizedDescriptions()).hasSameSizeAs(frame.getLocalizedInfo());
  }
}
