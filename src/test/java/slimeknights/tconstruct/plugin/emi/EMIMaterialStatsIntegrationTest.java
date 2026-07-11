package slimeknights.tconstruct.plugin.emi;

import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.plugin.emi.material.MaterialStatsEmiConstants;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.stats.ArmorExtensionMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EMIMaterialStatsIntegrationTest extends BaseMcTest {
  @Test
  void materialStatsCategoriesCoverTconJeiViews() {
    assertThat(MaterialStatsEmiConstants.CATEGORY_IDS).containsExactly(
        MaterialStatsEmiConstants.HARVEST_STATS,
        MaterialStatsEmiConstants.RANGED_STATS,
        MaterialStatsEmiConstants.ARMOR_STATS,
        MaterialStatsEmiConstants.AMMO_STATS,
        MaterialStatsEmiConstants.SKULL_STATS);
    assertThat(MaterialStatsEmiConstants.HARVEST_STAT_IDS).hasSize(3);
    assertThat(MaterialStatsEmiConstants.RANGED_STAT_IDS).hasSize(3);
    assertThat(MaterialStatsEmiConstants.AMMO_STAT_IDS).hasSize(3);
    assertThat(MaterialStatsEmiConstants.SKULL_STAT_IDS).hasSize(1);
  }

  @Test
  void armorStatsIncludeAllCurrentGtcFamilies() {
    List<MaterialStatsId> expected = List.of(
        PlatingMaterialStats.HELMET.getId(),
        PlatingMaterialStats.CHESTPLATE.getId(),
        PlatingMaterialStats.LEGGINGS.getId(),
        PlatingMaterialStats.BOOTS.getId(),
        PlatingMaterialStats.SHIELD.getId(),
        ArmorExtensionMaterialStats.ARMOR_PLATE.getId(),
        ArmorExtensionMaterialStats.ARMOR_MAIL.getId(),
        ArmorExtensionMaterialStats.CAST_HELMET.getId(),
        ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(),
        ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(),
        ArmorExtensionMaterialStats.CAST_BOOTS.getId(),
        ArmorExtensionMaterialStats.FRAME_HELMET.getId(),
        ArmorExtensionMaterialStats.FRAME_CHESTPLATE.getId(),
        ArmorExtensionMaterialStats.FRAME_LEGGINGS.getId(),
        ArmorExtensionMaterialStats.FRAME_BOOTS.getId(),
        ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(),
        ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(),
        ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(),
        ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(),
        StatlessMaterialStats.CUIRASS.getType().getId(),
        ArmorExtensionMaterialStats.MAILLE.getId(),
        StatlessMaterialStats.SHIELD_CORE.getType().getId(),
        StatlessMaterialStats.LINEAR.getType().getId());

    assertThat(MaterialStatsEmiConstants.ARMOR_STAT_IDS).containsExactlyElementsOf(expected);
    assertThat(MaterialStatsEmiConstants.ARMOR_STAT_IDS).doesNotHaveDuplicates();
  }
}
