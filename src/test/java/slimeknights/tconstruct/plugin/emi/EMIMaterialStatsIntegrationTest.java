package slimeknights.tconstruct.plugin.emi;

import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.plugin.emi.material.MaterialStatsEmiConstants;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.stats.ArmorPartMaterialStats;
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
        ArmorPartMaterialStats.ARMOR_LAYER_PLATE.getId(),
        ArmorPartMaterialStats.ARMOR_LAYER_MAIL.getId(),
        ArmorPartMaterialStats.ARMOR_CORE_HELMET.getId(),
        ArmorPartMaterialStats.ARMOR_CORE_CHESTPLATE.getId(),
        ArmorPartMaterialStats.ARMOR_CORE_LEGGINGS.getId(),
        ArmorPartMaterialStats.ARMOR_CORE_BOOTS.getId(),
        ArmorPartMaterialStats.ARMOR_FRAME_HELMET.getId(),
        ArmorPartMaterialStats.ARMOR_FRAME_CHESTPLATE.getId(),
        ArmorPartMaterialStats.ARMOR_FRAME_LEGGINGS.getId(),
        ArmorPartMaterialStats.ARMOR_FRAME_BOOTS.getId(),
        ArmorPartMaterialStats.ARMOR_HEAVY_CORE_HELMET.getId(),
        ArmorPartMaterialStats.ARMOR_HEAVY_CORE_CHESTPLATE.getId(),
        ArmorPartMaterialStats.ARMOR_HEAVY_CORE_LEGGINGS.getId(),
        ArmorPartMaterialStats.ARMOR_HEAVY_CORE_BOOTS.getId(),
        StatlessMaterialStats.CUIRASS.getType().getId(),
        ArmorPartMaterialStats.MAILLE.getId(),
        StatlessMaterialStats.SHIELD_CORE.getType().getId(),
        StatlessMaterialStats.LINEAR.getType().getId());

    assertThat(MaterialStatsEmiConstants.ARMOR_STAT_IDS).containsExactlyElementsOf(expected);
    assertThat(MaterialStatsEmiConstants.ARMOR_STAT_IDS).doesNotHaveDuplicates();
  }
}
