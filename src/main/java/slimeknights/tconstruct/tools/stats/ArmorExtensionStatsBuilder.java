package slimeknights.tconstruct.tools.stats;

import net.minecraft.world.item.ArmorItem;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.tools.modules.ArmorModuleBuilder;

/**
 * ArmorExtension 材料统计数据的构建器
 *
 * 用于在 MaterialStatsDataProvider#addArmor() 中显式声明每个支持 ArmorExtension 的材料的完整 stat bundle。
 *
 * 职责：
 * - 将命名字段转换为既有的 IMaterialStats[] 数组
 * - 提供清晰的具名 API 以避免槽位顺序混淆
 * - 定义可选字段的默认值
 *
 * 不负责：
 * - 保存 MaterialId 或材料注册表
 * - 注册 item/cast/sprite
 * - 任何运行时行为
 *
 * 输出保持现有 stat ID、序列化字段和数值不变。
 */
public final class ArmorExtensionStatsBuilder {
  private final float durabilityBase;
  private float helmet;
  private float chestplate;
  private float leggings;
  private float boots;
  private float armorStrength = 0f;
  private float armorToughness = 0f;
  private float reduction;
  private float protection;
  private float knockbackResistance = 0f;
  private float durabilityMultiplier;
  private float armorMultiplier;
  private float armorStrengthMultiplier;
  private float armorToughnessMultiplier;
  private float smallReductionFactor = 0.5f;  // DEFAULT_SMALL_REDUCTION_FACTOR
  private float smallProtectionFactor = 0.4f; // DEFAULT_SMALL_PROTECTION_FACTOR

  /**
   * 创建新的 builder
   *
   * @param durabilityBase 基础耐久倍率
   */
  public ArmorExtensionStatsBuilder(float durabilityBase) {
    this.durabilityBase = durabilityBase;
  }

  /**
   * 设置各槽位护甲值
   *
   * 使用显式参数名以避免与 PlatingMaterialStats.Builder#armor(boots, leggings, chestplate, helmet) 的顺序混淆
   *
   * @param helmet 头盔护甲值
   * @param chestplate 胸甲护甲值
   * @param leggings 护腿护甲值
   * @param boots 靴子护甲值
   */
  public ArmorExtensionStatsBuilder armor(float helmet, float chestplate, float leggings, float boots) {
    this.helmet = helmet;
    this.chestplate = chestplate;
    this.leggings = leggings;
    this.boots = boots;
    return this;
  }

  public ArmorExtensionStatsBuilder armorStrength(float armorStrength) {
    this.armorStrength = armorStrength;
    return this;
  }

  public ArmorExtensionStatsBuilder armorToughness(float armorToughness) {
    this.armorToughness = armorToughness;
    return this;
  }

  /**
   * 设置 reduction（armor_plate 的伤害减免）
   */
  public ArmorExtensionStatsBuilder reduction(float reduction) {
    this.reduction = reduction;
    return this;
  }

  /**
   * 设置 protection（额外伤害保护百分比）
   */
  public ArmorExtensionStatsBuilder protection(float protection) {
    this.protection = protection;
    return this;
  }

  public ArmorExtensionStatsBuilder knockbackResistance(float knockbackResistance) {
    this.knockbackResistance = knockbackResistance;
    return this;
  }

  /**
   * 设置 durabilityMultiplier（layer 部件的耐久倍率修正）
   */
  public ArmorExtensionStatsBuilder durabilityMultiplier(float durabilityMultiplier) {
    this.durabilityMultiplier = durabilityMultiplier;
    return this;
  }

  /**
   * 设置 armorMultiplier（layer 部件的护甲值倍率修正）
   */
  public ArmorExtensionStatsBuilder armorMultiplier(float armorMultiplier) {
    this.armorMultiplier = armorMultiplier;
    return this;
  }

  /**
   * 设置 armorStrengthMultiplier（frame 部件的护甲强度倍率修正）
   */
  public ArmorExtensionStatsBuilder armorStrengthMultiplier(float armorStrengthMultiplier) {
    this.armorStrengthMultiplier = armorStrengthMultiplier;
    return this;
  }

  /**
   * 设置 armorToughnessMultiplier（frame 部件的韧性倍率修正）
   */
  public ArmorExtensionStatsBuilder armorToughnessMultiplier(float armorToughnessMultiplier) {
    this.armorToughnessMultiplier = armorToughnessMultiplier;
    return this;
  }

  /**
   * 设置 smallReductionFactor（cast 部件的 reduction 缩放因子）
   *
   * 默认：0.5f
   */
  public ArmorExtensionStatsBuilder smallReductionFactor(float smallReductionFactor) {
    this.smallReductionFactor = smallReductionFactor;
    return this;
  }

  /**
   * 设置 smallProtectionFactor（cast 部件的 protection 缩放因子）
   *
   * 默认：0.4f
   */
  public ArmorExtensionStatsBuilder smallProtectionFactor(float smallProtectionFactor) {
    this.smallProtectionFactor = smallProtectionFactor;
    return this;
  }

  /**
   * 构建 ArmorExtension stat bundle
   *
   * 返回 14 个 IMaterialStats：
   * - stats[0]: armor_plate (ArmorLayerStats)
   * - stats[1]: armor_mail (ArmorLayerStats)
   * - stats[2-5]: cast_helmet/chestplate/leggings/boots (ArmorPieceStats)
   * - stats[6-9]: frame_helmet/chestplate/leggings/boots (ArmorFrameStats)
   * - stats[10-13]: massive_cast_helmet/chestplate/leggings/boots (ArmorPieceStats)
   *
   * 顺序与 MaterialStatsDataProvider.ArmorDefaults#extensionStats() 一致
   */
  public IMaterialStats[] build() {
    IMaterialStats[] stats = new IMaterialStats[14];

    // Layer stats
    stats[0] = new ArmorExtensionMaterialStats.ArmorLayerStats(
      ArmorExtensionMaterialStats.ARMOR_PLATE,
      durabilityMultiplier,
      armorMultiplier,
      armorStrength,
      armorToughness,
      reduction * smallReductionFactor,
      protection
    );
    stats[1] = new ArmorExtensionMaterialStats.ArmorLayerStats(
      ArmorExtensionMaterialStats.ARMOR_MAIL,
      durabilityMultiplier,
      armorMultiplier,
      armorStrength,
      armorToughness,
      0f, // armor_mail 没有 reduction
      protection
    );

    // Cast stats (4 slots)
    int index = 2;
    for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
      stats[index++] = new ArmorExtensionMaterialStats.ArmorPieceStats(
        ArmorExtensionMaterialStats.CAST_TYPES.get(slotType.ordinal()),
        durability(slotType),
        armor(slotType),
        armorStrength,
        armorToughness,
        reduction * smallReductionFactor,
        protection * smallProtectionFactor,
        knockbackResistance
      );
    }

    // Frame stats (4 slots)
    for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
      stats[index++] = new ArmorExtensionMaterialStats.ArmorFrameStats(
        ArmorExtensionMaterialStats.FRAME_TYPES.get(slotType.ordinal()),
        durability(slotType),
        armor(slotType),
        armorStrengthMultiplier,
        armorToughnessMultiplier,
        knockbackResistance
      );
    }

    // Massive cast stats (4 slots)
    for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
      stats[index++] = new ArmorExtensionMaterialStats.ArmorPieceStats(
        ArmorExtensionMaterialStats.MASSIVE_CAST_TYPES.get(slotType.ordinal()),
        durability(slotType),
        armor(slotType),
        armorStrength,
        armorToughness,
        reduction,  // massive cast 使用完整 reduction，不缩放
        protection, // massive cast 使用完整 protection，不缩放
        knockbackResistance
      );
    }

    return stats;
  }

  private int durability(ArmorItem.Type slot) {
    return (int)(ArmorModuleBuilder.MAX_DAMAGE_ARRAY[slot.ordinal()] * durabilityBase);
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
