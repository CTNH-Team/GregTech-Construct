package slimeknights.tconstruct.plugin.emi.material;

import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.tools.stats.ArmorPartMaterialStats;
import slimeknights.tconstruct.tools.stats.GripMaterialStats;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;
import slimeknights.tconstruct.tools.stats.LimbMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;
import slimeknights.tconstruct.tools.stats.SkullStats;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;

import java.util.LinkedHashSet;
import java.util.List;

public final class MaterialStatsEmiConstants {
  public static final ResourceLocation HARVEST_STATS = TConstruct.getResource("harvest_stats");
  public static final ResourceLocation RANGED_STATS = TConstruct.getResource("ranged_stats");
  public static final ResourceLocation ARMOR_STATS = TConstruct.getResource("armor_stats");
  public static final ResourceLocation AMMO_STATS = TConstruct.getResource("ammo_stats");
  public static final ResourceLocation SKULL_STATS = TConstruct.getResource("skull_stats");

  public static final List<ResourceLocation> CATEGORY_IDS = List.of(
      HARVEST_STATS, RANGED_STATS, ARMOR_STATS, AMMO_STATS, SKULL_STATS);

  public static final List<MaterialStatsId> HARVEST_STAT_IDS = List.of(
      HeadMaterialStats.ID,
      StatlessMaterialStats.BINDING.getType().getId(),
      HandleMaterialStats.ID);
  public static final List<MaterialStatsId> RANGED_STAT_IDS = List.of(
      LimbMaterialStats.ID,
      GripMaterialStats.ID,
      StatlessMaterialStats.BOWSTRING.getType().getId());
  public static final List<MaterialStatsId> ARMOR_STAT_IDS = armorStatIds();
  public static final List<MaterialStatsId> AMMO_STAT_IDS = List.of(
      StatlessMaterialStats.ARROW_HEAD.getType().getId(),
      StatlessMaterialStats.ARROW_SHAFT.getType().getId(),
      StatlessMaterialStats.FLETCHING.getType().getId());
  public static final List<MaterialStatsId> SKULL_STAT_IDS = List.of(SkullStats.ID);

  private MaterialStatsEmiConstants() {}

  static String armorGroupKey(MaterialStatsId statsId) {
    String path = statsId.getPath();
    if (path.startsWith("plating_")) {
      return "pieces";
    }
    if (path.equals("armor_plate")) {
      return "plate";
    }
    if (path.equals("armor_mail")) {
      return "mail";
    }
    if (path.startsWith("cast_")) {
      return "cast";
    }
    if (path.startsWith("frame_of_")) {
      return "frame";
    }
    if (path.startsWith("massive_cast_")) {
      return "massive_cast";
    }
    if (path.equals("cuirass")) {
      return "cuirass";
    }
    if (path.equals("maille")) {
      return "maille";
    }
    if (path.equals("shield_core")) {
      return "shield_core";
    }
    if (path.equals("linear")) {
      return "linear";
    }
    return "other";
  }

  private static List<MaterialStatsId> armorStatIds() {
    LinkedHashSet<MaterialStatsId> ids = new LinkedHashSet<>();
    PlatingMaterialStats.TYPES.forEach(type -> ids.add(type.getId()));
    ids.add(ArmorPartMaterialStats.ARMOR_PLATE.getId());
    ids.add(ArmorPartMaterialStats.ARMOR_MAIL.getId());
    ArmorPartMaterialStats.CAST_TYPES.forEach(type -> ids.add(type.getId()));
    ArmorPartMaterialStats.FRAME_TYPES.forEach(type -> ids.add(type.getId()));
    ArmorPartMaterialStats.MASSIVE_CAST_TYPES.forEach(type -> ids.add(type.getId()));
    ids.add(StatlessMaterialStats.CUIRASS.getType().getId());
    ids.add(ArmorPartMaterialStats.MAILLE.getId());
    ids.add(StatlessMaterialStats.SHIELD_CORE.getType().getId());
    ids.add(StatlessMaterialStats.LINEAR.getType().getId());
    return List.copyOf(ids);
  }
}
