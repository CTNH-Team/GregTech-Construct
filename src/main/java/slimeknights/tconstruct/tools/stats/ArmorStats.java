package slimeknights.tconstruct.tools.stats;

import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStatId;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public final class ArmorStats {
  public static final FloatToolStat ARMOR_STRENGTH = ToolStats.register(new FloatToolStat(name("armor_strength"), 0xFF8547CC, 0, 0, 2048, TinkerTags.Items.ARMOR));
  public static final FloatToolStat PRE_REDUCTION = ToolStats.register(new FloatToolStat(name("pre_reduction"), 0xFFD76464, 0, 0, 2048, TinkerTags.Items.ARMOR));
  public static final FloatToolStat PROTECTION = ToolStats.register(new FloatToolStat(name("protection"), 0xFF47CC47, 0, 0, 1, TinkerTags.Items.ARMOR));
  /** Post-reduction damage block, applied after armor absorption */
  public static final FloatToolStat POST_REDUCTION = ToolStats.register(new FloatToolStat(name("post_reduction"), 0xFF4788CC, 0, 0, 2048, TinkerTags.Items.ARMOR));
  public static final FloatToolStat SPEED_PENALTY = ToolStats.register(new FloatToolStat(name("speed_penalty"), 0xFF8547CC, 0, 0, 1024, TinkerTags.Items.ARMOR));

  private ArmorStats() {}

  public static void init() {}

  private static ToolStatId name(String name) {
    return new ToolStatId(TConstruct.MOD_ID, name);
  }
}
