package slimeknights.tconstruct.tools.stats;

import net.minecraft.world.item.ArmorItem;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.tools.modules.ArmorModuleBuilder;

/**
 * 构建 Plating、Maille 和护甲部件的完整材料统计包。
 *
 * <p>每个设定方法均注明作用部件，以及它提供直接数值还是倍率。</p>
 */
public final class ArmorPartStatsBuilder {
  private final PlatingMaterialStats.Builder plating = PlatingMaterialStats.builder();
  private float mailleDurability;
  private float mailleArmor;
  private float mailleArmorStrength;
  private float mailleToughness;
  private float partDurability;
  private float helmet;
  private float chestplate;
  private float leggings;
  private float boots;
  private float armorStrength;
  private float armorToughness;
  private float reduction;
  private float protection;
  private float knockbackResistance;
  private float durabilityMultiplier;
  private float armorMultiplier;
  private float armorStrengthMultiplier;
  private float armorToughnessMultiplier;
  private float smallReductionFactor = 0.5f;
  private float smallProtectionFactor = 0.4f;

  private ArmorPartStatsBuilder() {}

  /** Creates a builder for a complete armor material stat bundle. */
  public static ArmorPartStatsBuilder builder() {
    return new ArmorPartStatsBuilder();
  }

  /**
   * 调整 Plating 的耐久值直接数值，作用于对应槽位的护甲部件和盾牌。
   *
   * <p>该值作为对应槽位基础耐久值的系数，而非百分比倍率统计。</p>
   */
  public ArmorPartStatsBuilder platingDurability(float durabilityFactor) {
    plating.durabilityFactor(durabilityFactor);
    return this;
  }

  /**
   * 调整 Plating 的护甲值直接数值。
   *
   * <p>参数顺序为头盔、胸甲、护腿、靴子。</p>
   */
  public ArmorPartStatsBuilder platingArmor(float helmet, float chestplate, float leggings, float boots) {
    plating.armor(boots, leggings, chestplate, helmet);
    return this;
  }

  /** 调整 Plating 的护甲强度直接数值。 */
  public ArmorPartStatsBuilder platingArmorStrength(float armorStrength) {
    plating.armorStrength(armorStrength);
    return this;
  }

  /** 调整 Plating 的护甲韧性直接数值。 */
  public ArmorPartStatsBuilder platingToughness(float toughness) {
    plating.toughness(toughness);
    return this;
  }

  /** 调整 Plating 的击退抗性直接数值。 */
  public ArmorPartStatsBuilder platingKnockbackResistance(float knockbackResistance) {
    plating.knockbackResistance(knockbackResistance);
    return this;
  }

  /**
   * 调整 Maille 的耐久值、护甲值、护甲强度和护甲韧性倍率。
   *
   * <p>参数顺序与上述统计顺序一致，所有值均按百分比倍率应用。</p>
   */
  public ArmorPartStatsBuilder maille(float durability, float armor, float armorStrength, float toughness) {
    this.mailleDurability = durability;
    this.mailleArmor = armor;
    this.mailleArmorStrength = armorStrength;
    this.mailleToughness = toughness;
    return this;
  }

  /**
   * 调整 Cast、Massive Cast 和 Frame 的耐久值直接数值。
   *
   * <p>该值按对应装备槽位的基础耐久值换算。</p>
   */
  public ArmorPartStatsBuilder partDurability(float durability) {
    this.partDurability = durability;
    return this;
  }

  /**
   * 调整 Cast、Massive Cast 和 Frame 的护甲值直接数值。
   *
   * <p>参数顺序为头盔、胸甲、护腿、靴子。</p>
   */
  public ArmorPartStatsBuilder armor(float helmet, float chestplate, float leggings, float boots) {
    this.helmet = helmet;
    this.chestplate = chestplate;
    this.leggings = leggings;
    this.boots = boots;
    return this;
  }

  /** 调整 Armor Plate、Armor Mail、Cast 和 Massive Cast 的护甲强度直接数值。 */
  public ArmorPartStatsBuilder armorStrength(float armorStrength) {
    this.armorStrength = armorStrength;
    return this;
  }

  /** 调整 Armor Plate、Armor Mail、Cast 和 Massive Cast 的护甲韧性直接数值。 */
  public ArmorPartStatsBuilder armorToughness(float armorToughness) {
    this.armorToughness = armorToughness;
    return this;
  }

  /**
   * 调整 Armor Plate、Cast 和 Massive Cast 的伤害预削减直接数值。
   *
   * <p>Armor Plate 和 Cast 受 {@link #smallReductionFactor(float)} 缩放；Massive Cast 使用未缩放值。</p>
   */
  public ArmorPartStatsBuilder reduction(float reduction) {
    this.reduction = reduction;
    return this;
  }

  /**
   * 调整 Armor Plate、Armor Mail、Cast 和 Massive Cast 的保护值直接数值。
   *
   * <p>仅 Cast 受 {@link #smallProtectionFactor(float)} 缩放。</p>
   */
  public ArmorPartStatsBuilder protection(float protection) {
    this.protection = protection;
    return this;
  }

  /** 调整 Cast、Massive Cast 和 Frame 的击退抗性直接数值。 */
  public ArmorPartStatsBuilder knockbackResistance(float knockbackResistance) {
    this.knockbackResistance = knockbackResistance;
    return this;
  }

  /** 调整 Armor Plate 和 Armor Mail 的耐久值倍率。 */
  public ArmorPartStatsBuilder durabilityMultiplier(float durabilityMultiplier) {
    this.durabilityMultiplier = durabilityMultiplier;
    return this;
  }

  /** 调整 Armor Plate 和 Armor Mail 的护甲值倍率。 */
  public ArmorPartStatsBuilder armorMultiplier(float armorMultiplier) {
    this.armorMultiplier = armorMultiplier;
    return this;
  }

  /** 调整 Frame 的护甲强度倍率。 */
  public ArmorPartStatsBuilder armorStrengthMultiplier(float armorStrengthMultiplier) {
    this.armorStrengthMultiplier = armorStrengthMultiplier;
    return this;
  }

  /** 调整 Frame 的护甲韧性倍率。 */
  public ArmorPartStatsBuilder armorToughnessMultiplier(float armorToughnessMultiplier) {
    this.armorToughnessMultiplier = armorToughnessMultiplier;
    return this;
  }

  /** 调整 Armor Plate 和 Cast 的伤害预削减直接值缩放系数。 */
  public ArmorPartStatsBuilder smallReductionFactor(float smallReductionFactor) {
    this.smallReductionFactor = smallReductionFactor;
    return this;
  }

  /** 调整 Cast 的保护值直接值缩放系数。 */
  public ArmorPartStatsBuilder smallProtectionFactor(float smallProtectionFactor) {
    this.smallProtectionFactor = smallProtectionFactor;
    return this;
  }

  /** Builds four plating stats, maille, shield plating, and fourteen armor part stats. */
  public IMaterialStats[] build() {
    IMaterialStats[] stats = new IMaterialStats[20];
    int index = 0;
    for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
      stats[index++] = plating.build(slotType);
    }
    stats[index++] = ArmorPartMaterialStats.maille(mailleDurability, mailleArmor, mailleArmorStrength, mailleToughness);
    stats[index++] = plating.buildShield();
    stats[index++] = new ArmorPartMaterialStats.ArmorLayerStats(
      ArmorPartMaterialStats.ARMOR_PLATE,
      durabilityMultiplier,
      armorMultiplier,
      armorStrength,
      armorToughness,
      reduction * smallReductionFactor,
      protection
    );
    stats[index++] = new ArmorPartMaterialStats.ArmorLayerStats(
      ArmorPartMaterialStats.ARMOR_MAIL,
      durabilityMultiplier,
      armorMultiplier,
      armorStrength,
      armorToughness,
      0f,
      protection
    );
    for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
      stats[index++] = new ArmorPartMaterialStats.ArmorCoreStats(
        ArmorPartMaterialStats.CAST_TYPES.get(slotType.ordinal()),
        durability(slotType),
        armor(slotType),
        armorStrength,
        armorToughness,
        reduction * smallReductionFactor,
        protection * smallProtectionFactor,
        knockbackResistance
      );
    }
    for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
      stats[index++] = new ArmorPartMaterialStats.ArmorFrameStats(
        ArmorPartMaterialStats.FRAME_TYPES.get(slotType.ordinal()),
        durability(slotType),
        armor(slotType),
        armorStrengthMultiplier,
        armorToughnessMultiplier,
        knockbackResistance
      );
    }
    for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
      stats[index++] = new ArmorPartMaterialStats.ArmorCoreStats(
        ArmorPartMaterialStats.MASSIVE_CAST_TYPES.get(slotType.ordinal()),
        durability(slotType),
        armor(slotType),
        armorStrength,
        armorToughness,
        reduction,
        protection,
        knockbackResistance
      );
    }
    return stats;
  }

  private int durability(ArmorItem.Type slot) {
    return (int)(ArmorModuleBuilder.MAX_DAMAGE_ARRAY[slot.ordinal()] * partDurability);
  }

  private float armor(ArmorItem.Type slot) {
    return switch (slot) {
      case HELMET -> helmet;
      case CHESTPLATE -> chestplate;
      case LEGGINGS -> leggings;
      case BOOTS -> boots;
    };
  }
}
