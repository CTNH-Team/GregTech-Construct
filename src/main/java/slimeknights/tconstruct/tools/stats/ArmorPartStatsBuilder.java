package slimeknights.tconstruct.tools.stats;

import net.minecraft.world.item.ArmorItem;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.tools.modules.ArmorModuleBuilder;

/** Builds the complete material stat bundle for armor plating, maille, and armor parts. */
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

  /** Sets the plating durability factor used for armor pieces and shields. */
  public ArmorPartStatsBuilder platingDurability(float durabilityFactor) {
    plating.durabilityFactor(durabilityFactor);
    return this;
  }

  /** Sets plating armor in helmet, chestplate, leggings, and boots order. */
  public ArmorPartStatsBuilder platingArmor(float helmet, float chestplate, float leggings, float boots) {
    plating.armor(boots, leggings, chestplate, helmet);
    return this;
  }

  /** Sets the armor strength contribution from plating. */
  public ArmorPartStatsBuilder platingArmorStrength(float armorStrength) {
    plating.armorStrength(armorStrength);
    return this;
  }

  /** Sets the toughness contribution from plating. */
  public ArmorPartStatsBuilder platingToughness(float toughness) {
    plating.toughness(toughness);
    return this;
  }

  /** Sets the knockback resistance contribution from plating. */
  public ArmorPartStatsBuilder platingKnockbackResistance(float knockbackResistance) {
    plating.knockbackResistance(knockbackResistance);
    return this;
  }

  /** Sets the maille stat values. */
  public ArmorPartStatsBuilder maille(float durability, float armor, float armorStrength, float toughness) {
    this.mailleDurability = durability;
    this.mailleArmor = armor;
    this.mailleArmorStrength = armorStrength;
    this.mailleToughness = toughness;
    return this;
  }

  /** Sets the durability factor used for armor part stats. */
  public ArmorPartStatsBuilder partDurability(float durability) {
    this.partDurability = durability;
    return this;
  }

  /** Sets armor part values in helmet, chestplate, leggings, and boots order. */
  public ArmorPartStatsBuilder armor(float helmet, float chestplate, float leggings, float boots) {
    this.helmet = helmet;
    this.chestplate = chestplate;
    this.leggings = leggings;
    this.boots = boots;
    return this;
  }

  public ArmorPartStatsBuilder armorStrength(float armorStrength) {
    this.armorStrength = armorStrength;
    return this;
  }

  public ArmorPartStatsBuilder armorToughness(float armorToughness) {
    this.armorToughness = armorToughness;
    return this;
  }

  public ArmorPartStatsBuilder reduction(float reduction) {
    this.reduction = reduction;
    return this;
  }

  public ArmorPartStatsBuilder protection(float protection) {
    this.protection = protection;
    return this;
  }

  public ArmorPartStatsBuilder knockbackResistance(float knockbackResistance) {
    this.knockbackResistance = knockbackResistance;
    return this;
  }

  public ArmorPartStatsBuilder durabilityMultiplier(float durabilityMultiplier) {
    this.durabilityMultiplier = durabilityMultiplier;
    return this;
  }

  public ArmorPartStatsBuilder armorMultiplier(float armorMultiplier) {
    this.armorMultiplier = armorMultiplier;
    return this;
  }

  public ArmorPartStatsBuilder armorStrengthMultiplier(float armorStrengthMultiplier) {
    this.armorStrengthMultiplier = armorStrengthMultiplier;
    return this;
  }

  public ArmorPartStatsBuilder armorToughnessMultiplier(float armorToughnessMultiplier) {
    this.armorToughnessMultiplier = armorToughnessMultiplier;
    return this;
  }

  public ArmorPartStatsBuilder smallReductionFactor(float smallReductionFactor) {
    this.smallReductionFactor = smallReductionFactor;
    return this;
  }

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
