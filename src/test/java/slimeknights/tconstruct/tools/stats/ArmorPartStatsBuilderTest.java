package slimeknights.tconstruct.tools.stats;

import net.minecraft.world.item.ArmorItem;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.modules.ArmorModuleBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试 ArmorPartStatsBuilder 生成的 stat bundle 与现有 ArmorDefaults 逻辑等价
 */
class ArmorPartStatsBuilderTest extends BaseMcTest {

  /**
   * 验证 builder 生成的 stat 数组结构和顺序
   */
  @Test
  void builderGenerates14Stats() {
    var stats = new ArmorPartStatsBuilder(32f)
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
    assertThat(stats[0]).isInstanceOf(ArmorPartMaterialStats.ArmorLayerStats.class);
    assertThat(stats[1]).isInstanceOf(ArmorPartMaterialStats.ArmorLayerStats.class);
    assertThat(stats[2]).isInstanceOf(ArmorPartMaterialStats.ArmorCoreStats.class);
    assertThat(stats[3]).isInstanceOf(ArmorPartMaterialStats.ArmorCoreStats.class);
    assertThat(stats[4]).isInstanceOf(ArmorPartMaterialStats.ArmorCoreStats.class);
    assertThat(stats[5]).isInstanceOf(ArmorPartMaterialStats.ArmorCoreStats.class);
    assertThat(stats[6]).isInstanceOf(ArmorPartMaterialStats.ArmorFrameStats.class);
    assertThat(stats[7]).isInstanceOf(ArmorPartMaterialStats.ArmorFrameStats.class);
    assertThat(stats[8]).isInstanceOf(ArmorPartMaterialStats.ArmorFrameStats.class);
    assertThat(stats[9]).isInstanceOf(ArmorPartMaterialStats.ArmorFrameStats.class);
    assertThat(stats[10]).isInstanceOf(ArmorPartMaterialStats.ArmorCoreStats.class);
    assertThat(stats[11]).isInstanceOf(ArmorPartMaterialStats.ArmorCoreStats.class);
    assertThat(stats[12]).isInstanceOf(ArmorPartMaterialStats.ArmorCoreStats.class);
    assertThat(stats[13]).isInstanceOf(ArmorPartMaterialStats.ArmorCoreStats.class);

    // Verify the correct stat types via cast
    assertThat(((ArmorPartMaterialStats.ArmorLayerStats) stats[0]).getType())
      .isEqualTo(ArmorPartMaterialStats.ARMOR_LAYER_PLATE);
    assertThat(((ArmorPartMaterialStats.ArmorLayerStats) stats[1]).getType())
      .isEqualTo(ArmorPartMaterialStats.ARMOR_LAYER_MAIL);
    assertThat(((ArmorPartMaterialStats.ArmorCoreStats) stats[2]).getType())
      .isEqualTo(ArmorPartMaterialStats.ARMOR_CORE_HELMET);
    assertThat(((ArmorPartMaterialStats.ArmorCoreStats) stats[3]).getType())
      .isEqualTo(ArmorPartMaterialStats.ARMOR_CORE_CHESTPLATE);
  }

  /**
   * 验证 layer stats 的数值计算
   */
  @Test
  void layerStatsCalculatedCorrectly() {
    var stats = new ArmorPartStatsBuilder(15f)
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
    var armorPlate = (ArmorPartMaterialStats.ArmorLayerStats) stats[0];
    assertThat(armorPlate.durability()).isEqualTo(0.1f);
    assertThat(armorPlate.armor()).isEqualTo(0.08f);
    assertThat(armorPlate.armorStrength()).isEqualTo(1.0f);
    assertThat(armorPlate.toughness()).isEqualTo(0.5f);
    assertThat(armorPlate.reduction()).isEqualTo(0.2f * 0.5f); // reduction * smallReductionFactor
    assertThat(armorPlate.protection()).isEqualTo(0.012f);

    // armor_mail (no reduction)
    var armorMail = (ArmorPartMaterialStats.ArmorLayerStats) stats[1];
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
    var stats = new ArmorPartStatsBuilder(15f)
      .armor(2.0f, 6.0f, 5.0f, 2.0f)
      .armorStrength(1.0f)
      .armorToughness(0.5f)
      .reduction(0.2f)
      .protection(0.012f)
      .durabilityMultiplier(0.1f)
      .armorMultiplier(0.08f)
      .build();

    // cast_helmet
    var castHelmet = (ArmorPartMaterialStats.ArmorCoreStats) stats[2];
    assertThat(castHelmet.durability())
      .isEqualTo((int)(ArmorModuleBuilder.MAX_DAMAGE_ARRAY[ArmorItem.Type.HELMET.ordinal()] * 15f));
    assertThat(castHelmet.armor()).isEqualTo(2.0f);
    assertThat(castHelmet.armorStrength()).isEqualTo(1.0f);
    assertThat(castHelmet.toughness()).isEqualTo(0.5f);
    assertThat(castHelmet.reduction()).isEqualTo(0.2f * 0.5f); // scaled
    assertThat(castHelmet.protection()).isEqualTo(0.012f * 0.4f); // scaled with default smallProtectionFactor

    // cast_chestplate
    var castChestplate = (ArmorPartMaterialStats.ArmorCoreStats) stats[3];
    assertThat(castChestplate.durability())
      .isEqualTo((int)(ArmorModuleBuilder.MAX_DAMAGE_ARRAY[ArmorItem.Type.CHESTPLATE.ordinal()] * 15f));
    assertThat(castChestplate.armor()).isEqualTo(6.0f);
  }

  /**
   * 验证 frame stats 的倍率计算
   */
  @Test
  void frameStatsUseMultipliers() {
    var stats = new ArmorPartStatsBuilder(32f)
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
    var frameChestplate = (ArmorPartMaterialStats.ArmorFrameStats) stats[7];
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
    var stats = new ArmorPartStatsBuilder(51.25f)
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
    var massiveCastHelmet = (ArmorPartMaterialStats.ArmorCoreStats) stats[10];
    assertThat(massiveCastHelmet.reduction()).isEqualTo(0.75f); // NOT scaled
    assertThat(massiveCastHelmet.protection()).isEqualTo(0.05f); // NOT scaled

    // Compare with cast_helmet which should be scaled
    var castHelmet = (ArmorPartMaterialStats.ArmorCoreStats) stats[2];
    assertThat(castHelmet.reduction()).isEqualTo(0.75f * 0.8f); // scaled
    assertThat(castHelmet.protection()).isEqualTo(0.05f * 0.8f); // scaled
  }

  /**
   * 验证默认值
   */
  @Test
  void builderHasCorrectDefaults() {
    var stats = new ArmorPartStatsBuilder(10f)
      .armor(1f, 2f, 3f, 4f)
      .reduction(0.5f)
      .protection(0.1f)
      .durabilityMultiplier(0.1f)
      .armorMultiplier(0.1f)
      .armorStrengthMultiplier(0.1f)
      .armorToughnessMultiplier(0.1f)
      .build();

    // armorStrength and armorToughness default to 0
    var armorPlate = (ArmorPartMaterialStats.ArmorLayerStats) stats[0];
    assertThat(armorPlate.armorStrength()).isEqualTo(0f);
    assertThat(armorPlate.toughness()).isEqualTo(0f);

    // knockbackResistance defaults to 0
    var castHelmet = (ArmorPartMaterialStats.ArmorCoreStats) stats[2];
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
    var stats = new ArmorPartStatsBuilder(10f)
      .armor(1f, 2f, 3f, 4f) // helmet, chestplate, leggings, boots
      .reduction(0.1f)
      .protection(0.01f)
      .durabilityMultiplier(0f)
      .armorMultiplier(0f)
      .armorStrengthMultiplier(0f)
      .armorToughnessMultiplier(0f)
      .build();

    var castHelmet = (ArmorPartMaterialStats.ArmorCoreStats) stats[2];
    var castChestplate = (ArmorPartMaterialStats.ArmorCoreStats) stats[3];
    var castLeggings = (ArmorPartMaterialStats.ArmorCoreStats) stats[4];
    var castBoots = (ArmorPartMaterialStats.ArmorCoreStats) stats[5];

    assertThat(castHelmet.armor()).isEqualTo(1f);
    assertThat(castChestplate.armor()).isEqualTo(2f);
    assertThat(castLeggings.armor()).isEqualTo(3f);
    assertThat(castBoots.armor()).isEqualTo(4f);
  }
}
