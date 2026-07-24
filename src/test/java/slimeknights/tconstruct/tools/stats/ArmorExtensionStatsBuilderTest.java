package slimeknights.tconstruct.tools.stats;

import net.minecraft.world.item.ArmorItem;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.modules.ArmorModuleBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试 ArmorExtensionStatsBuilder 生成的 stat bundle 与现有 ArmorDefaults 逻辑等价
 */
class ArmorExtensionStatsBuilderTest extends BaseMcTest {

  /**
   * 验证 builder 生成的 stat 数组结构和顺序
   */
  @Test
  void builderGenerates14Stats() {
    var stats = new ArmorExtensionStatsBuilder(32f)
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

    assertThat(stats).hasSize(14);

    // Verify all stats are the correct type
    assertThat(stats[0]).isInstanceOf(ArmorExtensionMaterialStats.ArmorLayerStats.class);
    assertThat(stats[1]).isInstanceOf(ArmorExtensionMaterialStats.ArmorLayerStats.class);
    assertThat(stats[2]).isInstanceOf(ArmorExtensionMaterialStats.ArmorPieceStats.class);
    assertThat(stats[3]).isInstanceOf(ArmorExtensionMaterialStats.ArmorPieceStats.class);
    assertThat(stats[4]).isInstanceOf(ArmorExtensionMaterialStats.ArmorPieceStats.class);
    assertThat(stats[5]).isInstanceOf(ArmorExtensionMaterialStats.ArmorPieceStats.class);
    assertThat(stats[6]).isInstanceOf(ArmorExtensionMaterialStats.ArmorFrameStats.class);
    assertThat(stats[7]).isInstanceOf(ArmorExtensionMaterialStats.ArmorFrameStats.class);
    assertThat(stats[8]).isInstanceOf(ArmorExtensionMaterialStats.ArmorFrameStats.class);
    assertThat(stats[9]).isInstanceOf(ArmorExtensionMaterialStats.ArmorFrameStats.class);
    assertThat(stats[10]).isInstanceOf(ArmorExtensionMaterialStats.ArmorPieceStats.class);
    assertThat(stats[11]).isInstanceOf(ArmorExtensionMaterialStats.ArmorPieceStats.class);
    assertThat(stats[12]).isInstanceOf(ArmorExtensionMaterialStats.ArmorPieceStats.class);
    assertThat(stats[13]).isInstanceOf(ArmorExtensionMaterialStats.ArmorPieceStats.class);

    // Verify the correct stat types via cast
    assertThat(((ArmorExtensionMaterialStats.ArmorLayerStats) stats[0]).getType())
      .isEqualTo(ArmorExtensionMaterialStats.ARMOR_PLATE);
    assertThat(((ArmorExtensionMaterialStats.ArmorLayerStats) stats[1]).getType())
      .isEqualTo(ArmorExtensionMaterialStats.ARMOR_MAIL);
    assertThat(((ArmorExtensionMaterialStats.ArmorPieceStats) stats[2]).getType())
      .isEqualTo(ArmorExtensionMaterialStats.CAST_HELMET);
    assertThat(((ArmorExtensionMaterialStats.ArmorPieceStats) stats[3]).getType())
      .isEqualTo(ArmorExtensionMaterialStats.CAST_CHESTPLATE);
  }

  /**
   * 验证 layer stats 的数值计算
   */
  @Test
  void layerStatsCalculatedCorrectly() {
    var stats = new ArmorExtensionStatsBuilder(15f)
      .armor(2.0f, 6.0f, 5.0f, 2.0f)
      .armorStrength(1.0f)
      .armorToughness(0.5f)
      .reduction(0.2f)
      .protection(0.012f)
      .durabilityMultiplier(0.1f)
      .armorMultiplier(0.08f)
      .armorStrengthMultiplier(0.0f)
      .armorToughnessMultiplier(-0.1f)
      .build();

    // armor_plate
    var armorPlate = (ArmorExtensionMaterialStats.ArmorLayerStats) stats[0];
    assertThat(armorPlate.durability()).isEqualTo(0.1f);
    assertThat(armorPlate.armor()).isEqualTo(0.08f);
    assertThat(armorPlate.armorStrength()).isEqualTo(1.0f);
    assertThat(armorPlate.toughness()).isEqualTo(0.5f);
    assertThat(armorPlate.reduction()).isEqualTo(0.2f * 0.5f); // reduction * smallReductionFactor
    assertThat(armorPlate.protection()).isEqualTo(0.012f);

    // armor_mail (no reduction)
    var armorMail = (ArmorExtensionMaterialStats.ArmorLayerStats) stats[1];
    assertThat(armorMail.durability()).isEqualTo(0.1f);
    assertThat(armorMail.armor()).isEqualTo(0.08f);
    assertThat(armorMail.reduction()).isEqualTo(0f);
    assertThat(armorMail.protection()).isEqualTo(0.012f);
  }

  /**
   * 验证 cast stats 的耐久和护甲值计算
   */
  @Test
  void castStatsUseDurabilityBaseAndArmorSlots() {
    var stats = new ArmorExtensionStatsBuilder(15f)
      .armor(2.0f, 6.0f, 5.0f, 2.0f)
      .armorStrength(1.0f)
      .armorToughness(0.5f)
      .reduction(0.2f)
      .protection(0.012f)
      .durabilityMultiplier(0.1f)
      .armorMultiplier(0.08f)
      .build();

    // cast_helmet
    var castHelmet = (ArmorExtensionMaterialStats.ArmorPieceStats) stats[2];
    assertThat(castHelmet.durability())
      .isEqualTo((int)(ArmorModuleBuilder.MAX_DAMAGE_ARRAY[ArmorItem.Type.HELMET.ordinal()] * 15f));
    assertThat(castHelmet.armor()).isEqualTo(2.0f);
    assertThat(castHelmet.armorStrength()).isEqualTo(1.0f);
    assertThat(castHelmet.toughness()).isEqualTo(0.5f);
    assertThat(castHelmet.reduction()).isEqualTo(0.2f * 0.5f); // scaled
    assertThat(castHelmet.protection()).isEqualTo(0.012f * 0.4f); // scaled with default smallProtectionFactor

    // cast_chestplate
    var castChestplate = (ArmorExtensionMaterialStats.ArmorPieceStats) stats[3];
    assertThat(castChestplate.durability())
      .isEqualTo((int)(ArmorModuleBuilder.MAX_DAMAGE_ARRAY[ArmorItem.Type.CHESTPLATE.ordinal()] * 15f));
    assertThat(castChestplate.armor()).isEqualTo(6.0f);
  }

  /**
   * 验证 frame stats 的倍率计算
   */
  @Test
  void frameStatsUseMultipliers() {
    var stats = new ArmorExtensionStatsBuilder(32f)
      .armor(2.5f, 7.5f, 6.0f, 2.0f)
      .armorStrength(2.0f)
      .armorToughness(2.0f)
      .reduction(0.7f)
      .protection(0.08f)
      .durabilityMultiplier(0.2f)
      .armorMultiplier(0.04f)
      .armorStrengthMultiplier(0.05f)
      .armorToughnessMultiplier(0.05f)
      .knockbackResistance(0.1f)
      .build();

    // frame_chestplate
    var frameChestplate = (ArmorExtensionMaterialStats.ArmorFrameStats) stats[7];
    assertThat(frameChestplate.durability())
      .isEqualTo((int)(ArmorModuleBuilder.MAX_DAMAGE_ARRAY[ArmorItem.Type.CHESTPLATE.ordinal()] * 32f));
    assertThat(frameChestplate.armor()).isEqualTo(7.5f);
    assertThat(frameChestplate.armorStrength()).isEqualTo(0.05f); // multiplier, not base strength
    assertThat(frameChestplate.toughness()).isEqualTo(0.05f); // multiplier, not base toughness
    assertThat(frameChestplate.knockbackResistance()).isEqualTo(0.1f);
  }

  /**
   * 验证 massive cast stats 不缩放 reduction/protection
   */
  @Test
  void massiveCastStatsUseFullReductionAndProtection() {
    var stats = new ArmorExtensionStatsBuilder(51.25f)
      .armor(2.5f, 7.0f, 6.5f, 2.0f)
      .armorStrength(3.0f)
      .armorToughness(1.25f)
      .reduction(0.75f)
      .protection(0.05f)
      .durabilityMultiplier(0.2f)
      .armorMultiplier(0.05f)
      .armorStrengthMultiplier(0.2f)
      .armorToughnessMultiplier(-0.1f)
      .knockbackResistance(0.15f)
      .smallReductionFactor(0.8f)
      .smallProtectionFactor(0.8f)
      .build();

    // massive_cast_helmet
    var massiveCastHelmet = (ArmorExtensionMaterialStats.ArmorPieceStats) stats[10];
    assertThat(massiveCastHelmet.reduction()).isEqualTo(0.75f); // NOT scaled
    assertThat(massiveCastHelmet.protection()).isEqualTo(0.05f); // NOT scaled

    // Compare with cast_helmet which should be scaled
    var castHelmet = (ArmorExtensionMaterialStats.ArmorPieceStats) stats[2];
    assertThat(castHelmet.reduction()).isEqualTo(0.75f * 0.8f); // scaled
    assertThat(castHelmet.protection()).isEqualTo(0.05f * 0.8f); // scaled
  }

  /**
   * 验证默认值
   */
  @Test
  void builderHasCorrectDefaults() {
    var stats = new ArmorExtensionStatsBuilder(10f)
      .armor(1f, 2f, 3f, 4f)
      .reduction(0.5f)
      .protection(0.1f)
      .durabilityMultiplier(0.1f)
      .armorMultiplier(0.1f)
      .armorStrengthMultiplier(0.1f)
      .armorToughnessMultiplier(0.1f)
      .build();

    // armorStrength and armorToughness default to 0
    var armorPlate = (ArmorExtensionMaterialStats.ArmorLayerStats) stats[0];
    assertThat(armorPlate.armorStrength()).isEqualTo(0f);
    assertThat(armorPlate.toughness()).isEqualTo(0f);

    // knockbackResistance defaults to 0
    var castHelmet = (ArmorExtensionMaterialStats.ArmorPieceStats) stats[2];
    assertThat(castHelmet.knockbackResistance()).isEqualTo(0f);

    // smallReductionFactor defaults to 0.5
    // smallProtectionFactor defaults to 0.4
    assertThat(castHelmet.reduction()).isEqualTo(0.5f * 0.5f); // 0.5 * default 0.5
    assertThat(castHelmet.protection()).isEqualTo(0.1f * 0.4f); // 0.1 * default 0.4
  }

  /**
   * 验证显式槽位命名避免顺序混淆
   */
  @Test
  void armorSlotOrderIsExplicit() {
    var stats = new ArmorExtensionStatsBuilder(10f)
      .armor(1f, 2f, 3f, 4f) // helmet, chestplate, leggings, boots
      .reduction(0.1f)
      .protection(0.01f)
      .durabilityMultiplier(0f)
      .armorMultiplier(0f)
      .armorStrengthMultiplier(0f)
      .armorToughnessMultiplier(0f)
      .build();

    var castHelmet = (ArmorExtensionMaterialStats.ArmorPieceStats) stats[2];
    var castChestplate = (ArmorExtensionMaterialStats.ArmorPieceStats) stats[3];
    var castLeggings = (ArmorExtensionMaterialStats.ArmorPieceStats) stats[4];
    var castBoots = (ArmorExtensionMaterialStats.ArmorPieceStats) stats[5];

    assertThat(castHelmet.armor()).isEqualTo(1f);
    assertThat(castChestplate.armor()).isEqualTo(2f);
    assertThat(castLeggings.armor()).isEqualTo(3f);
    assertThat(castBoots.armor()).isEqualTo(4f);
  }
}
