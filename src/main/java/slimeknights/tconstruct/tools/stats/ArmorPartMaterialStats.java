package slimeknights.tconstruct.tools.stats;

import net.minecraft.network.chat.Component;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.IRepairableMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatType;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

/**
 * Armor part 材料统计数据
 *
 * 定义所有 armor 构件的 material stat type：
 * - maille: 有数值的链甲内层 (durability/armor/armorStrength/toughness)
 * - layer (plate/mail): 护甲层，带 durability/armor multiplier、reduction、protection
 * - core (per-slot): 小型熔铸核心，带完整数值和缩放的 reduction/protection
 * - frame (per-slot): 框架，带 multiplier 和 knockback
 * - heavy_core (per-slot): 重型核心，带完整 reduction/protection
 *
 * 所有 stat type 都是普通 armor stat，通过 MaterialStatsDataProvider#addArmor() 直接写入。
 */
public final class ArmorPartMaterialStats {
  public static final MaterialStatType<MailleStats> MAILLE = mailleType("maille");
  public static final MailleStats MAILLE_DEFAULT = new MailleStats(MAILLE, 0f, 0f, 0f, 0f);
  public static final MaterialStatType<ArmorLayerStats> ARMOR_LAYER_PLATE = layerType("armor_layer_plate");
  public static final MaterialStatType<ArmorLayerStats> ARMOR_LAYER_MAIL = layerType("armor_layer_mail");
  public static final MaterialStatType<ArmorCoreStats> ARMOR_CORE_HELMET = coreType("armor_core_helmet");
  public static final MaterialStatType<ArmorCoreStats> ARMOR_CORE_CHESTPLATE = coreType("armor_core_chestplate");
  public static final MaterialStatType<ArmorCoreStats> ARMOR_CORE_LEGGINGS = coreType("armor_core_leggings");
  public static final MaterialStatType<ArmorCoreStats> ARMOR_CORE_BOOTS = coreType("armor_core_boots");
  public static final MaterialStatType<ArmorFrameStats> ARMOR_FRAME_HELMET = frameType("armor_frame_helmet");
  public static final MaterialStatType<ArmorFrameStats> ARMOR_FRAME_CHESTPLATE = frameType("armor_frame_chestplate");
  public static final MaterialStatType<ArmorFrameStats> ARMOR_FRAME_LEGGINGS = frameType("armor_frame_leggings");
  public static final MaterialStatType<ArmorFrameStats> ARMOR_FRAME_BOOTS = frameType("armor_frame_boots");
  public static final MaterialStatType<ArmorCoreStats> ARMOR_HEAVY_CORE_HELMET = coreType("armor_heavy_core_helmet");
  public static final MaterialStatType<ArmorCoreStats> ARMOR_HEAVY_CORE_CHESTPLATE = coreType("armor_heavy_core_chestplate");
  public static final MaterialStatType<ArmorCoreStats> ARMOR_HEAVY_CORE_LEGGINGS = coreType("armor_heavy_core_leggings");
  public static final MaterialStatType<ArmorCoreStats> ARMOR_HEAVY_CORE_BOOTS = coreType("armor_heavy_core_boots");
  public static final List<MaterialStatType<ArmorCoreStats>> CORE_TYPES = List.of(ARMOR_CORE_HELMET, ARMOR_CORE_CHESTPLATE, ARMOR_CORE_LEGGINGS, ARMOR_CORE_BOOTS);
  public static final List<MaterialStatType<ArmorFrameStats>> FRAME_TYPES = List.of(ARMOR_FRAME_HELMET, ARMOR_FRAME_CHESTPLATE, ARMOR_FRAME_LEGGINGS, ARMOR_FRAME_BOOTS);
  public static final List<MaterialStatType<ArmorCoreStats>> HEAVY_CORE_TYPES = List.of(ARMOR_HEAVY_CORE_HELMET, ARMOR_HEAVY_CORE_CHESTPLATE, ARMOR_HEAVY_CORE_LEGGINGS, ARMOR_HEAVY_CORE_BOOTS);

  private ArmorPartMaterialStats() {}

  private static Component formatProtection(float value) {
    return IToolStat.formatNumberPercent(
      ArmorStats.PROTECTION.getTranslationKey(), ArmorStats.PROTECTION.getColor(), value);
  }

  private static MaterialStatType<MailleStats> mailleType(String name) {
    return new MaterialStatType<MailleStats>(new MaterialStatsId(TConstruct.MOD_ID, name), (MaterialStatType<MailleStats> type) -> new MailleStats(type, 0f, 0f, 0f, 0f), MailleStats.LOADABLE);
  }

  public static MailleStats maille(float durability, float armor, float armorStrength, float toughness) {
    return new MailleStats(MAILLE, durability, armor, armorStrength, toughness);
  }

  private static MaterialStatType<ArmorLayerStats> layerType(String name) {
    return new MaterialStatType<ArmorLayerStats>(new MaterialStatsId(TConstruct.MOD_ID, name), (MaterialStatType<ArmorLayerStats> type) -> new ArmorLayerStats(type, 0f, 0f, 0f, 0f, 0f, 0f), ArmorLayerStats.LOADABLE);
  }

  private static MaterialStatType<ArmorCoreStats> coreType(String name) {
    return new MaterialStatType<ArmorCoreStats>(new MaterialStatsId(TConstruct.MOD_ID, name), (MaterialStatType<ArmorCoreStats> type) -> new ArmorCoreStats(type, 1, 0f, 0f, 0f, 0f, 0f, 0f), ArmorCoreStats.LOADABLE);
  }

  private static MaterialStatType<ArmorFrameStats> frameType(String name) {
    return new MaterialStatType<ArmorFrameStats>(new MaterialStatsId(TConstruct.MOD_ID, name), (MaterialStatType<ArmorFrameStats> type) -> new ArmorFrameStats(type, 1, 0f, 0f, 0f, 0f), ArmorFrameStats.LOADABLE);
  }

  /**
   * Maille 链甲统计数据（有数值版本）
   *
   * 提供 durability/armor/armorStrength/toughness 百分比加成。
   */
  public record MailleStats(MaterialStatType<?> getType, float durability, float armor, float armorStrength, float toughness) implements IMaterialStats {
    private static final LoadableField<Float, MailleStats> DURABILITY = FloatLoadable.ANY.defaultField("durability", 0f, MailleStats::durability);
    private static final LoadableField<Float, MailleStats> ARMOR = FloatLoadable.ANY.defaultField("armor", 0f, MailleStats::armor);
    private static final LoadableField<Float, MailleStats> ARMOR_STRENGTH = FloatLoadable.ANY.defaultField("armor_strength", 0f, MailleStats::armorStrength);
    private static final LoadableField<Float, MailleStats> TOUGHNESS = FloatLoadable.ANY.defaultField("toughness", 0f, MailleStats::toughness);
    private static final RecordLoadable<MailleStats> LOADABLE = RecordLoadable.create(MaterialStatType.CONTEXT_KEY.requiredField(), DURABILITY, ARMOR, ARMOR_STRENGTH, TOUGHNESS, MailleStats::new);
    private static final String DURABILITY_PREFIX = IMaterialStats.makeTooltipKey(TConstruct.getResource("durability"));
    private static final String ARMOR_PREFIX = IMaterialStats.makeTooltipKey(TConstruct.getResource("armor"));
    private static final String ARMOR_STRENGTH_PREFIX = IMaterialStats.makeTooltipKey(TConstruct.getResource("armor_strength"));
    private static final String ARMOR_TOUGHNESS_PREFIX = IMaterialStats.makeTooltipKey(TConstruct.getResource("armor_toughness"));
    private static final List<Component> DESCRIPTION = List.of(
      ToolStats.DURABILITY.getDescription(),
      ToolStats.ARMOR.getDescription(),
      ArmorStats.ARMOR_STRENGTH.getDescription(),
      ToolStats.ARMOR_TOUGHNESS.getDescription());

    @Override public List<Component> getLocalizedInfo() {
      return List.of(
        IToolStat.formatColoredPercentBoost(DURABILITY_PREFIX, durability),
        IToolStat.formatColoredPercentBoost(ARMOR_PREFIX, armor),
        IToolStat.formatColoredPercentBoost(ARMOR_STRENGTH_PREFIX, armorStrength),
        IToolStat.formatColoredPercentBoost(ARMOR_TOUGHNESS_PREFIX, toughness));
    }
    @Override public List<Component> getLocalizedDescriptions() { return DESCRIPTION; }
    @Override public void apply(ModifierStatsBuilder builder, float scale) {
      ToolStats.DURABILITY.percent(builder, durability * scale);
      ToolStats.ARMOR.percent(builder, armor * scale);
      ArmorStats.ARMOR_STRENGTH.percent(builder, armorStrength * scale);
      ToolStats.ARMOR_TOUGHNESS.percent(builder, toughness * scale);
    }
  }

  /**
   * Armor layer 统计数据（plate 和 mail 共用）
   *
   * plate 有 reduction，mail 的 reduction 为 0。
   */
  public record ArmorLayerStats(MaterialStatType<?> getType, float durability, float armor, float armorStrength, float toughness, float reduction, float protection) implements IMaterialStats {
    private static final LoadableField<Float, ArmorLayerStats> DURABILITY = FloatLoadable.ANY.defaultField("durability", 0f, ArmorLayerStats::durability);
    private static final LoadableField<Float, ArmorLayerStats> ARMOR = FloatLoadable.ANY.defaultField("armor", 0f, ArmorLayerStats::armor);
    private static final LoadableField<Float, ArmorLayerStats> ARMOR_STRENGTH = FloatLoadable.FROM_ZERO.defaultField("armor_strength", 0f, ArmorLayerStats::armorStrength);
    private static final LoadableField<Float, ArmorLayerStats> TOUGHNESS = FloatLoadable.FROM_ZERO.defaultField("toughness", 0f, ArmorLayerStats::toughness);
    private static final LoadableField<Float, ArmorLayerStats> REDUCTION = FloatLoadable.FROM_ZERO.defaultField("reduction", 0f, ArmorLayerStats::reduction);
    private static final LoadableField<Float, ArmorLayerStats> PROTECTION = FloatLoadable.PERCENT.defaultField("protection", 0f, ArmorLayerStats::protection);
    private static final RecordLoadable<ArmorLayerStats> LOADABLE = RecordLoadable.create(MaterialStatType.CONTEXT_KEY.requiredField(), DURABILITY, ARMOR, ARMOR_STRENGTH, TOUGHNESS, REDUCTION, PROTECTION, ArmorLayerStats::new);
    private static final String DURABILITY_PREFIX = IMaterialStats.makeTooltipKey(TConstruct.getResource("durability"));
    private static final String ARMOR_PREFIX = IMaterialStats.makeTooltipKey(TConstruct.getResource("armor"));
    private static final List<Component> ARMOR_MAIL_DESCRIPTION = List.of(
      ToolStats.DURABILITY.getDescription(),
      ToolStats.ARMOR.getDescription(),
      ArmorStats.ARMOR_STRENGTH.getDescription(),
      ToolStats.ARMOR_TOUGHNESS.getDescription(),
      ArmorStats.PROTECTION.getDescription());
    private static final List<Component> ARMOR_PLATE_DESCRIPTION = List.of(
      ToolStats.DURABILITY.getDescription(),
      ToolStats.ARMOR.getDescription(),
      ArmorStats.ARMOR_STRENGTH.getDescription(),
      ToolStats.ARMOR_TOUGHNESS.getDescription(),
      ArmorStats.PRE_REDUCTION.getDescription(),
      ArmorStats.PROTECTION.getDescription());

    @Override public List<Component> getLocalizedInfo() {
      List<Component> info = new java.util.ArrayList<>(List.of(
        IToolStat.formatColoredPercentBoost(DURABILITY_PREFIX, durability),
        IToolStat.formatColoredPercentBoost(ARMOR_PREFIX, armor),
        ArmorStats.ARMOR_STRENGTH.formatValue(armorStrength),
        ToolStats.ARMOR_TOUGHNESS.formatValue(toughness)));
      if (getType == ARMOR_LAYER_PLATE) {
        info.add(ArmorStats.PRE_REDUCTION.formatValue(reduction));
      }
      info.add(formatProtection(protection));
      return info;
    }
    @Override public List<Component> getLocalizedDescriptions() {
      return getType == ARMOR_LAYER_PLATE ? ARMOR_PLATE_DESCRIPTION : ARMOR_MAIL_DESCRIPTION;
    }
    @Override public void apply(ModifierStatsBuilder builder, float scale) {
      ToolStats.DURABILITY.percent(builder, durability * scale);
      ToolStats.ARMOR.percent(builder, armor * scale);
      ArmorStats.ARMOR_STRENGTH.update(builder, armorStrength * scale);
      ToolStats.ARMOR_TOUGHNESS.update(builder, toughness * scale);
      ArmorStats.PRE_REDUCTION.update(builder, reduction * scale);
      ArmorStats.PROTECTION.update(builder, protection * scale);
    }
  }

  /**
   * Armor core 统计数据（小型 core 和重型 heavy_core 共用）
   *
   * Core 描述构筑职责，区别在于 reduction/protection 的缩放：
   * - 小型 core: reduction * 0.5, protection * 0.4
   * - 重型 heavy_core: 完整 reduction 和 protection
   */
  public record ArmorCoreStats(MaterialStatType<?> getType, int durability, float armor, float armorStrength, float toughness, float reduction, float protection, float knockbackResistance) implements IRepairableMaterialStats {
    private static final LoadableField<Float, ArmorCoreStats> ARMOR = FloatLoadable.FROM_ZERO.defaultField("armor", 0f, ArmorCoreStats::armor);
    private static final LoadableField<Float, ArmorCoreStats> ARMOR_STRENGTH = FloatLoadable.FROM_ZERO.defaultField("armor_strength", 0f, ArmorCoreStats::armorStrength);
    private static final LoadableField<Float, ArmorCoreStats> TOUGHNESS = FloatLoadable.FROM_ZERO.defaultField("toughness", 0f, ArmorCoreStats::toughness);
    private static final LoadableField<Float, ArmorCoreStats> REDUCTION = FloatLoadable.FROM_ZERO.defaultField("reduction", 0f, ArmorCoreStats::reduction);
    private static final LoadableField<Float, ArmorCoreStats> PROTECTION = FloatLoadable.PERCENT.defaultField("protection", 0f, ArmorCoreStats::protection);
    private static final LoadableField<Float, ArmorCoreStats> KNOCKBACK_RESISTANCE = FloatLoadable.FROM_ZERO.defaultField("knockback_resistance", 0f, ArmorCoreStats::knockbackResistance);
    private static final RecordLoadable<ArmorCoreStats> LOADABLE = RecordLoadable.create(MaterialStatType.CONTEXT_KEY.requiredField(), IRepairableMaterialStats.DURABILITY_FIELD, ARMOR, ARMOR_STRENGTH, TOUGHNESS, REDUCTION, PROTECTION, KNOCKBACK_RESISTANCE, ArmorCoreStats::new);
    private static final List<Component> DESCRIPTION = List.of(
      ToolStats.DURABILITY.getDescription(),
      ToolStats.ARMOR.getDescription(),
      ArmorStats.ARMOR_STRENGTH.getDescription(),
      ToolStats.ARMOR_TOUGHNESS.getDescription(),
      ArmorStats.PRE_REDUCTION.getDescription(),
      ArmorStats.PROTECTION.getDescription(),
      ToolStats.KNOCKBACK_RESISTANCE.getDescription());

    @Override public List<Component> getLocalizedInfo() {
      return List.of(
        ToolStats.DURABILITY.formatValue(durability),
        ToolStats.ARMOR.formatValue(armor),
        ArmorStats.ARMOR_STRENGTH.formatValue(armorStrength),
        ToolStats.ARMOR_TOUGHNESS.formatValue(toughness),
        ArmorStats.PRE_REDUCTION.formatValue(reduction),
        formatProtection(protection),
        ToolStats.KNOCKBACK_RESISTANCE.formatValue(knockbackResistance * 10));
    }
    @Override public List<Component> getLocalizedDescriptions() { return DESCRIPTION; }
    @Override public void apply(ModifierStatsBuilder builder, float scale) {
      ToolStats.DURABILITY.update(builder, durability * scale);
      ToolStats.ARMOR.update(builder, armor * scale);
      ArmorStats.ARMOR_STRENGTH.update(builder, armorStrength * scale);
      ToolStats.ARMOR_TOUGHNESS.update(builder, toughness * scale);
      ArmorStats.PRE_REDUCTION.update(builder, reduction * scale);
      ArmorStats.PROTECTION.update(builder, protection * scale);
      ToolStats.KNOCKBACK_RESISTANCE.update(builder, knockbackResistance * scale);
    }
  }

  /**
   * Armor frame 统计数据
   *
   * Frame 提供 multiplier 和 knockback resistance。
   */
  public record ArmorFrameStats(MaterialStatType<?> getType, int durability, float armor, float armorStrength, float toughness, float knockbackResistance) implements IRepairableMaterialStats {
    private static final LoadableField<Float, ArmorFrameStats> ARMOR = FloatLoadable.FROM_ZERO.defaultField("armor", 0f, ArmorFrameStats::armor);
    private static final LoadableField<Float, ArmorFrameStats> ARMOR_STRENGTH = FloatLoadable.ANY.defaultField("armor_strength", 0f, ArmorFrameStats::armorStrength);
    private static final LoadableField<Float, ArmorFrameStats> TOUGHNESS = FloatLoadable.ANY.defaultField("toughness", 0f, ArmorFrameStats::toughness);
    private static final LoadableField<Float, ArmorFrameStats> KNOCKBACK_RESISTANCE = FloatLoadable.FROM_ZERO.defaultField("knockback_resistance", 0f, ArmorFrameStats::knockbackResistance);
    private static final RecordLoadable<ArmorFrameStats> LOADABLE = RecordLoadable.create(MaterialStatType.CONTEXT_KEY.requiredField(), IRepairableMaterialStats.DURABILITY_FIELD, ARMOR, ARMOR_STRENGTH, TOUGHNESS, KNOCKBACK_RESISTANCE, ArmorFrameStats::new);
    private static final String ARMOR_STRENGTH_PREFIX = IMaterialStats.makeTooltipKey(TConstruct.getResource("armor_strength"));
    private static final String ARMOR_TOUGHNESS_PREFIX = IMaterialStats.makeTooltipKey(TConstruct.getResource("armor_toughness"));
    private static final List<Component> DESCRIPTION = List.of(
      ToolStats.DURABILITY.getDescription(),
      ToolStats.ARMOR.getDescription(),
      ArmorStats.ARMOR_STRENGTH.getDescription(),
      ToolStats.ARMOR_TOUGHNESS.getDescription(),
      ToolStats.KNOCKBACK_RESISTANCE.getDescription());

    @Override public List<Component> getLocalizedInfo() {
      return List.of(
        ToolStats.DURABILITY.formatValue(durability),
        ToolStats.ARMOR.formatValue(armor),
        IToolStat.formatColoredPercentBoost(ARMOR_STRENGTH_PREFIX, armorStrength),
        IToolStat.formatColoredPercentBoost(ARMOR_TOUGHNESS_PREFIX, toughness),
        ToolStats.KNOCKBACK_RESISTANCE.formatValue(knockbackResistance));
    }
    @Override public List<Component> getLocalizedDescriptions() { return DESCRIPTION; }
    @Override public void apply(ModifierStatsBuilder builder, float scale) {
      ToolStats.DURABILITY.update(builder, durability * scale);
      ToolStats.ARMOR.update(builder, armor * scale);
      ArmorStats.ARMOR_STRENGTH.percent(builder, armorStrength * scale);
      ToolStats.ARMOR_TOUGHNESS.percent(builder, toughness * scale);
      ToolStats.KNOCKBACK_RESISTANCE.update(builder, knockbackResistance * scale);
    }
  }
}
