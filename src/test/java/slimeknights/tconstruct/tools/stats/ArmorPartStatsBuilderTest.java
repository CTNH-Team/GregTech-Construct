package slimeknights.tconstruct.tools.stats;

import net.minecraft.world.item.ArmorItem;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.modules.ArmorModuleBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class ArmorPartStatsBuilderTest extends BaseMcTest {
  private static final int MAILLE_INDEX = 4;
  private static final int SHIELD_INDEX = 5;
  private static final int PART_STATS_START = 6;

  @Test
  void builderGeneratesCompleteStatBundle() {
    var stats = ArmorPartStatsBuilder.builder()
      .platingDurability(32f)
      .platingArmor(2.5f, 7.5f, 6.0f, 2.0f)
      .platingArmorStrength(0.75f)
      .platingToughness(2.0f)
      .platingKnockbackResistance(0.1f)
      .maille(0.1f, 0.2f, 0.3f, 0.4f)
      .partDurability(32f)
      .armor(2.5f, 7.5f, 6.0f, 2.0f)
      .armorStrength(2.0f)
      .armorToughness(2.0f)
      .reduction(0.7f)
      .protection(0.08f)
      .durabilityMultiplier(0.2f)
      .armorMultiplier(0.04f)
      .armorStrengthMultiplier(0.05f)
      .armorToughnessMultiplier(0.05f)
      .smallReductionFactor(0.5f)
      .smallProtectionFactor(0.6f)
      .build();

    assertThat(stats).hasSize(20);
    assertThat(stats).extracting(IMaterialStats::getIdentifier).containsExactly(
      PlatingMaterialStats.HELMET.getId(),
      PlatingMaterialStats.CHESTPLATE.getId(),
      PlatingMaterialStats.LEGGINGS.getId(),
      PlatingMaterialStats.BOOTS.getId(),
      ArmorPartMaterialStats.MAILLE.getId(),
      PlatingMaterialStats.SHIELD.getId(),
      ArmorPartMaterialStats.ARMOR_PLATE.getId(),
      ArmorPartMaterialStats.ARMOR_MAIL.getId(),
      ArmorPartMaterialStats.CAST_HELMET.getId(),
      ArmorPartMaterialStats.CAST_CHESTPLATE.getId(),
      ArmorPartMaterialStats.CAST_LEGGINGS.getId(),
      ArmorPartMaterialStats.CAST_BOOTS.getId(),
      ArmorPartMaterialStats.FRAME_HELMET.getId(),
      ArmorPartMaterialStats.FRAME_CHESTPLATE.getId(),
      ArmorPartMaterialStats.FRAME_LEGGINGS.getId(),
      ArmorPartMaterialStats.FRAME_BOOTS.getId(),
      ArmorPartMaterialStats.MASSIVE_CAST_HELMET.getId(),
      ArmorPartMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(),
      ArmorPartMaterialStats.MASSIVE_CAST_LEGGINGS.getId(),
      ArmorPartMaterialStats.MASSIVE_CAST_BOOTS.getId()
    );
  }

  @Test
  void platingStatsUseExplicitSlotOrder() {
    var stats = ArmorPartStatsBuilder.builder()
      .platingDurability(24f)
      .platingArmor(2f, 5f, 3f, 1f)
      .platingArmorStrength(0.75f)
      .platingToughness(1.5f)
      .platingKnockbackResistance(0.1f)
      .partDurability(27f)
      .build();

    var helmet = (PlatingMaterialStats) stats[0];
    var chestplate = (PlatingMaterialStats) stats[1];
    var leggings = (PlatingMaterialStats) stats[2];
    var boots = (PlatingMaterialStats) stats[3];
    var shield = (PlatingMaterialStats) stats[SHIELD_INDEX];

    assertThat(helmet.armor()).isEqualTo(2f);
    assertThat(chestplate.armor()).isEqualTo(5f);
    assertThat(leggings.armor()).isEqualTo(3f);
    assertThat(boots.armor()).isEqualTo(1f);
    assertThat(helmet.durability())
      .isEqualTo((int)(ArmorModuleBuilder.MAX_DAMAGE_ARRAY[ArmorItem.Type.HELMET.ordinal()] * 24f));
    assertThat(shield.durability()).isEqualTo((int)(ArmorModuleBuilder.SHIELD_DAMAGE * 24f));
    assertThat(helmet.armorStrength()).isEqualTo(0.75f);
    assertThat(helmet.toughness()).isEqualTo(1.5f);
    assertThat(helmet.knockbackResistance()).isEqualTo(0.1f);
  }

  @Test
  void partStatsUseConfiguredValues() {
    var stats = ArmorPartStatsBuilder.builder()
      .partDurability(32f)
      .armor(2.5f, 7.5f, 6.0f, 2.0f)
      .armorStrength(2.0f)
      .armorToughness(2.0f)
      .reduction(0.7f)
      .protection(0.08f)
      .knockbackResistance(0.1f)
      .durabilityMultiplier(0.2f)
      .armorMultiplier(0.04f)
      .armorStrengthMultiplier(0.05f)
      .armorToughnessMultiplier(0.05f)
      .smallReductionFactor(0.8f)
      .smallProtectionFactor(0.6f)
      .build();

    var armorPlate = (ArmorPartMaterialStats.ArmorLayerStats) stats[PART_STATS_START];
    var armorMail = (ArmorPartMaterialStats.ArmorLayerStats) stats[PART_STATS_START + 1];
    var castChestplate = (ArmorPartMaterialStats.ArmorCoreStats) stats[PART_STATS_START + 3];
    var frameChestplate = (ArmorPartMaterialStats.ArmorFrameStats) stats[PART_STATS_START + 7];
    var heavyCastHelmet = (ArmorPartMaterialStats.ArmorCoreStats) stats[PART_STATS_START + 10];

    assertThat(armorPlate.durability()).isEqualTo(0.2f);
    assertThat(armorPlate.armor()).isEqualTo(0.04f);
    assertThat(armorPlate.armorStrength()).isEqualTo(2.0f);
    assertThat(armorPlate.toughness()).isEqualTo(2.0f);
    assertThat(armorPlate.reduction()).isEqualTo(0.7f * 0.8f);
    assertThat(armorPlate.protection()).isEqualTo(0.08f);
    assertThat(armorMail.reduction()).isEqualTo(0f);
    assertThat(castChestplate.durability())
      .isEqualTo((int)(ArmorModuleBuilder.MAX_DAMAGE_ARRAY[ArmorItem.Type.CHESTPLATE.ordinal()] * 32f));
    assertThat(castChestplate.armor()).isEqualTo(7.5f);
    assertThat(castChestplate.protection()).isEqualTo(0.08f * 0.6f);
    assertThat(frameChestplate.armorStrength()).isEqualTo(0.05f);
    assertThat(frameChestplate.toughness()).isEqualTo(0.05f);
    assertThat(frameChestplate.knockbackResistance()).isEqualTo(0.1f);
    assertThat(heavyCastHelmet.reduction()).isEqualTo(0.7f);
    assertThat(heavyCastHelmet.protection()).isEqualTo(0.08f);
  }

  @Test
  void defaultsAreZero() {
    var stats = ArmorPartStatsBuilder.builder().build();

    var plating = (PlatingMaterialStats) stats[0];
    var maille = (ArmorPartMaterialStats.MailleStats) stats[MAILLE_INDEX];
    var castHelmet = (ArmorPartMaterialStats.ArmorCoreStats) stats[PART_STATS_START + 2];

    assertThat(plating.durability()).isZero();
    assertThat(plating.armor()).isZero();
    assertThat(plating.armorStrength()).isZero();
    assertThat(plating.toughness()).isZero();
    assertThat(plating.knockbackResistance()).isZero();
    assertThat(maille.durability()).isZero();
    assertThat(maille.armor()).isZero();
    assertThat(maille.armorStrength()).isZero();
    assertThat(maille.toughness()).isZero();
    assertThat(castHelmet.durability()).isZero();
    assertThat(castHelmet.armor()).isZero();
    assertThat(castHelmet.knockbackResistance()).isZero();
  }

  @Test
  void armorSlotOrderIsExplicit() {
    var stats = ArmorPartStatsBuilder.builder()
      .platingArmor(1f, 2f, 3f, 4f)
      .partDurability(10f)
      .armor(1f, 2f, 3f, 4f)
      .build();

    assertThat(((PlatingMaterialStats) stats[0]).armor()).isEqualTo(1f);
    assertThat(((PlatingMaterialStats) stats[1]).armor()).isEqualTo(2f);
    assertThat(((PlatingMaterialStats) stats[2]).armor()).isEqualTo(3f);
    assertThat(((PlatingMaterialStats) stats[3]).armor()).isEqualTo(4f);
    assertThat(((ArmorPartMaterialStats.ArmorCoreStats) stats[PART_STATS_START + 2]).armor()).isEqualTo(1f);
    assertThat(((ArmorPartMaterialStats.ArmorCoreStats) stats[PART_STATS_START + 3]).armor()).isEqualTo(2f);
    assertThat(((ArmorPartMaterialStats.ArmorCoreStats) stats[PART_STATS_START + 4]).armor()).isEqualTo(3f);
    assertThat(((ArmorPartMaterialStats.ArmorCoreStats) stats[PART_STATS_START + 5]).armor()).isEqualTo(4f);
  }
}
