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
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

public final class ArmorExtensionMaterialStats {
  public static final MaterialStatType<ArmorLayerStats> ARMOR_PLATE = layerType("armor_plate");
  public static final MaterialStatType<ArmorLayerStats> ARMOR_MAIL = layerType("armor_mail");
  public static final List<MaterialStatType<ArmorPieceStats>> CAST_TYPES = List.of(pieceType("cast_helmet"), pieceType("cast_chestplate"), pieceType("cast_leggings"), pieceType("cast_boots"));
  public static final List<MaterialStatType<ArmorFrameStats>> FRAME_TYPES = List.of(frameType("frame_of_helmet"), frameType("frame_of_chestplate"), frameType("frame_of_leggings"), frameType("frame_of_boots"));
  public static final List<MaterialStatType<ArmorPieceStats>> MASSIVE_CAST_TYPES = List.of(pieceType("massive_cast_helmet"), pieceType("massive_cast_chestplate"), pieceType("massive_cast_leggings"), pieceType("massive_cast_boots"));

  private ArmorExtensionMaterialStats() {}

  private static MaterialStatType<ArmorLayerStats> layerType(String name) {
    return new MaterialStatType<ArmorLayerStats>(new MaterialStatsId(TConstruct.MOD_ID, name), (MaterialStatType<ArmorLayerStats> type) -> new ArmorLayerStats(type, 0f, 0f, 0f, 0f, 0f, 0f), ArmorLayerStats.LOADABLE);
  }

  private static MaterialStatType<ArmorPieceStats> pieceType(String name) {
    return new MaterialStatType<ArmorPieceStats>(new MaterialStatsId(TConstruct.MOD_ID, name), (MaterialStatType<ArmorPieceStats> type) -> new ArmorPieceStats(type, 1, 0f, 0f, 0f, 0f, 0f, 0f), ArmorPieceStats.LOADABLE);
  }

  private static MaterialStatType<ArmorFrameStats> frameType(String name) {
    return new MaterialStatType<ArmorFrameStats>(new MaterialStatsId(TConstruct.MOD_ID, name), (MaterialStatType<ArmorFrameStats> type) -> new ArmorFrameStats(type, 1, 0f, 0f, 0f, 0f), ArmorFrameStats.LOADABLE);
  }

  private static List<Component> emptyInfo() {
    return List.of(Component.empty());
  }

  public record ArmorLayerStats(MaterialStatType<?> getType, float durability, float armor, float armorStrength, float toughness, float reduction, float protection) implements IMaterialStats {
    private static final LoadableField<Float, ArmorLayerStats> DURABILITY = FloatLoadable.PERCENT.defaultField("durability", 0f, ArmorLayerStats::durability);
    private static final LoadableField<Float, ArmorLayerStats> ARMOR = FloatLoadable.PERCENT.defaultField("armor", 0f, ArmorLayerStats::armor);
    private static final LoadableField<Float, ArmorLayerStats> ARMOR_STRENGTH = FloatLoadable.FROM_ZERO.defaultField("armor_strength", 0f, ArmorLayerStats::armorStrength);
    private static final LoadableField<Float, ArmorLayerStats> TOUGHNESS = FloatLoadable.FROM_ZERO.defaultField("toughness", 0f, ArmorLayerStats::toughness);
    private static final LoadableField<Float, ArmorLayerStats> REDUCTION = FloatLoadable.FROM_ZERO.defaultField("reduction", 0f, ArmorLayerStats::reduction);
    private static final LoadableField<Float, ArmorLayerStats> PROTECTION = FloatLoadable.PERCENT.defaultField("protection", 0f, ArmorLayerStats::protection);
    private static final RecordLoadable<ArmorLayerStats> LOADABLE = RecordLoadable.create(MaterialStatType.CONTEXT_KEY.requiredField(), DURABILITY, ARMOR, ARMOR_STRENGTH, TOUGHNESS, REDUCTION, PROTECTION, ArmorLayerStats::new);

    @Override public List<Component> getLocalizedInfo() { return emptyInfo(); }
    @Override public List<Component> getLocalizedDescriptions() { return emptyInfo(); }
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

    @Override public List<Component> getLocalizedInfo() { return emptyInfo(); }
    @Override public List<Component> getLocalizedDescriptions() { return emptyInfo(); }
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
    private static final LoadableField<Float, ArmorFrameStats> ARMOR_STRENGTH = FloatLoadable.PERCENT.defaultField("armor_strength", 0f, ArmorFrameStats::armorStrength);
    private static final LoadableField<Float, ArmorFrameStats> TOUGHNESS = FloatLoadable.PERCENT.defaultField("toughness", 0f, ArmorFrameStats::toughness);
    private static final LoadableField<Float, ArmorFrameStats> KNOCKBACK_RESISTANCE = FloatLoadable.FROM_ZERO.defaultField("knockback_resistance", 0f, ArmorFrameStats::knockbackResistance);
    private static final RecordLoadable<ArmorFrameStats> LOADABLE = RecordLoadable.create(MaterialStatType.CONTEXT_KEY.requiredField(), IRepairableMaterialStats.DURABILITY_FIELD, ARMOR, ARMOR_STRENGTH, TOUGHNESS, KNOCKBACK_RESISTANCE, ArmorFrameStats::new);

    @Override public List<Component> getLocalizedInfo() { return emptyInfo(); }
    @Override public List<Component> getLocalizedDescriptions() { return emptyInfo(); }
    @Override public void apply(ModifierStatsBuilder builder, float scale) {
      ToolStats.DURABILITY.update(builder, durability * scale);
      ToolStats.ARMOR.update(builder, armor * scale);
      ArmorStats.ARMOR_STRENGTH.percent(builder, armorStrength * scale);
      ToolStats.ARMOR_TOUGHNESS.percent(builder, toughness * scale);
      ToolStats.KNOCKBACK_RESISTANCE.update(builder, knockbackResistance * scale);
    }
  }
}
