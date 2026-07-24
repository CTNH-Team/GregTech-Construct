package slimeknights.tconstruct.plugin.emi.material;

import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;

import static org.assertj.core.api.Assertions.assertThat;

class ArmorStatsEmiRecipeLayoutTest extends BaseMcTest {
  @Test
  void pageCountFitsArmorLinesIntoVisibleRows() {
    assertThat(ArmorStatsEmiRecipe.pageCount(11, 6)).isEqualTo(2);
    assertThat(ArmorStatsEmiRecipe.pageCount(0, 6)).isZero();
  }

  @Test
  void pageManagerWrapsWhenMovingPastEitherEnd() {
    ArmorStatsEmiRecipe.PageManager manager = new ArmorStatsEmiRecipe.PageManager(3);

    manager.scroll(-1);
    assertThat(manager.page()).isEqualTo(2);

    manager.scroll(1);
    assertThat(manager.page()).isZero();
  }

  @Test
  void visibleLinesLeaveRoomAboveTheBottomBorder() {
    assertThat(ArmorStatsEmiRecipe.visibleLineCount(200, 97)).isEqualTo(8);
  }

  @Test
  void armorStatsUseDistinctPagesForEachExtensionModule() {
    assertThat(MaterialStatsEmiConstants.armorGroupKey(new MaterialStatsId("tconstruct", "armor_plate")))
        .isEqualTo("plate");
    assertThat(MaterialStatsEmiConstants.armorGroupKey(new MaterialStatsId("tconstruct", "cast_helmet")))
        .isEqualTo("cast");
    assertThat(MaterialStatsEmiConstants.armorGroupKey(new MaterialStatsId("tconstruct", "frame_of_boots")))
        .isEqualTo("frame");
    assertThat(MaterialStatsEmiConstants.armorGroupKey(new MaterialStatsId("tconstruct", "linear")))
        .isEqualTo("linear");
  }

  @Test
  void compactArmorSummaryShowsDurabilityAndArmor() {
    PlatingMaterialStats stats = new PlatingMaterialStats(
        PlatingMaterialStats.HELMET, 151, 1.5f, 0f, 0f, 0f);

    assertThat(ArmorStatsEmiRecipe.compactSummary(stats).getString())
        .contains("151", "1.5");
  }
}
