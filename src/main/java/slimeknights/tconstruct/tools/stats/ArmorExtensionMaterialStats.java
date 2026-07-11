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

public final class ArmorExtensionMaterialStats {
  public static final MaterialStatType<MailleStats> MAILLE = mailleType("maille");
  public static final MailleStats MAILLE_DEFAULT = new MailleStats(MAILLE, 0f, 0f, 0f, 0f);
  public static final MaterialStatType<ArmorLayerStats> ARMOR_PLATE = layerType("armor_plate");
  public static final MaterialStatType<ArmorLayerStats> ARMOR_MAIL = layerType("armor_mail");
  public static final MaterialStatType<ArmorPieceStats> CAST_HELMET = pieceType("cast_helmet");
  public static final MaterialStatType<ArmorPieceStats> CAST_CHESTPLATE = pieceType("cast_chestplate");
  public static final MaterialStatType<ArmorPieceStats> CAST_LEGGINGS = pieceType("cast_leggings");
  public static final MaterialStatType<ArmorPieceStats> CAST_BOOTS = pieceType("cast_boots");
  public static final MaterialStatType<ArmorFrameStats> FRAME_HELMET = frameType("frame_of_helmet");
  public static final MaterialStatType<ArmorFrameStats> FRAME_CHESTPLATE = frameType("frame_of_chestplate");
  public static final MaterialStatType<ArmorFrameStats> FRAME_LEGGINGS = frameType("frame_of_leggings");
  public static final MaterialStatType<ArmorFrameStats> FRAME_BOOTS = frameType("frame_of_boots");
  public static final MaterialStatType<ArmorPieceStats> MASSIVE_CAST_HELMET = pieceType("massive_cast_helmet");
  public static final MaterialStatType<ArmorPieceStats> MASSIVE_CAST_CHESTPLATE = pieceType("massive_cast_chestplate");
  public static final MaterialStatType<ArmorPieceStats> MASSIVE_CAST_LEGGINGS = pieceType("massive_cast_leggings");
  public static final MaterialStatType<ArmorPieceStats> MASSIVE_CAST_BOOTS = pieceType("massive_cast_boots");
  public static final List<MaterialStatType<ArmorPieceStats>> CAST_TYPES = List.of(CAST_HELMET, CAST_CHESTPLATE, CAST_LEGGINGS, CAST_BOOTS);
  public static final List<MaterialStatType<ArmorFrameStats>> FRAME_TYPES = List.of(FRAME_HELMET, FRAME_CHESTPLATE, FRAME_LEGGINGS, FRAME_BOOTS);
  public static final List<MaterialStatType<ArmorPieceStats>> MASSIVE_CAST_TYPES = List.of(MASSIVE_CAST_HELMET, MASSIVE_CAST_CHESTPLATE, MASSIVE_CAST_LEGGINGS, MASSIVE_CAST_BOOTS);

  private ArmorExtensionMaterialStats() {}

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

  private static MaterialStatType<ArmorPieceStats> pieceType(String name) {
    return new MaterialStatType<ArmorPieceStats>(new MaterialStatsId(TConstruct.MOD_ID, name), (MaterialStatType<ArmorPieceStats> type) -> new ArmorPieceStats(type, 1, 0f, 0f, 0f, 0f, 0f, 0f), ArmorPieceStats.LOADABLE);
  }

  private static MaterialStatType<ArmorFrameStats> frameType(String name) {
    return new MaterialStatType<ArmorFrameStats>(new MaterialStatsId(TConstruct.MOD_ID, name), (MaterialStatType<ArmorFrameStats> type) -> new ArmorFrameStats(type, 1, 0f, 0f, 0f, 0f), ArmorFrameStats.LOADABLE);
  }

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
      if (getType == ARMOR_PLATE) {
        info.add(ArmorStats.PRE_REDUCTION.formatValue(reduction));
      }
      info.add(formatProtection(protection));
      return info;
    }
    @Override public List<Component> getLocalizedDescriptions() {
      return getType == ARMOR_PLATE ? ARMOR_PLATE_DESCRIPTION : ARMOR_MAIL_DESCRIPTION;
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

  public record ArmorPieceStats(MaterialStatType<?> getType, int durability, float armor, float armorStrength, float toughness, float reduction, float protection, float knockbackResistance) implements IRepairableMaterialStats {
    private static final LoadableField<Float, ArmorPieceStats> ARMOR = FloatLoadable.FROM_ZERO.defaultField("armor", 0f, ArmorPieceStats::armor);
    private static final LoadableField<Float, ArmorPieceStats> ARMOR_STRENGTH = FloatLoadable.FROM_ZERO.defaultField("armor_strength", 0f, ArmorPieceStats::armorStrength);
    private static final LoadableField<Float, ArmorPieceStats> TOUGHNESS = FloatLoadable.FROM_ZERO.defaultField("toughness", 0f, ArmorPieceStats::toughness);
    private static final LoadableField<Float, ArmorPieceStats> REDUCTION = FloatLoadable.FROM_ZERO.defaultField("reduction", 0f, ArmorPieceStats::reduction);
    private static final LoadableField<Float, ArmorPieceStats> PROTECTION = FloatLoadable.PERCENT.defaultField("protection", 0f, ArmorPieceStats::protection);
    private static final LoadableField<Float, ArmorPieceStats> KNOCKBACK_RESISTANCE = FloatLoadable.FROM_ZERO.defaultField("knockback_resistance", 0f, ArmorPieceStats::knockbackResistance);
    private static final RecordLoadable<ArmorPieceStats> LOADABLE = RecordLoadable.create(MaterialStatType.CONTEXT_KEY.requiredField(), IRepairableMaterialStats.DURABILITY_FIELD, ARMOR, ARMOR_STRENGTH, TOUGHNESS, REDUCTION, PROTECTION, KNOCKBACK_RESISTANCE, ArmorPieceStats::new);
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
