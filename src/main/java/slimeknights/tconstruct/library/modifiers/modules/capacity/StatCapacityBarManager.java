package slimeknights.tconstruct.library.modifiers.modules.capacity;

import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.special.CapacityBarHook;
import slimeknights.tconstruct.library.tools.stat.CapacityToolStat;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStatId;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.HashMap;
import java.util.Map;

public class StatCapacityBarManager {
  /** Fallback color applied when a capacity stat is created on demand before its owning modifier data is decoded */
  public static final int DEFAULT_CAPACITY_COLOR = 0xFF9E9E9E;

  private static final Map<ModifierId, CapacityBarHook> CAPACITY_BARS = new HashMap<>();
  private static final Map<ModifierId, FloatToolStat> CAPACITY_STATS = new HashMap<>();

  public static void register(ModifierId id, CapacityBarHook hook) {
    CAPACITY_BARS.put(id, hook);
  }

  public static CapacityBarHook getCapacityBar(ModifierId id) {
    return CAPACITY_BARS.get(id);
  }

  public static FloatToolStat getOrCreateStat(ModifierId id, int color) {
    FloatToolStat stat = CAPACITY_STATS.get(id);
    if (stat == null) {
      stat = ToolStats.register(new CapacityToolStat(
        new ToolStatId(id),
        color,
        0,      // defaultValue
        0,      // minValue
        Integer.MAX_VALUE,
        TinkerTags.Items.DURABILITY
      ));
      CAPACITY_STATS.put(id, stat);
    } else if (stat instanceof CapacityToolStat capacity) {
      // created earlier via the decode-time fallback; the owning StatCapacityBarModule always passes the canonical color
      capacity.setColor(color);
    }
    return stat;
  }

  /**
   * On-demand creation used while decoding modifier data (e.g. StatBoostModule referencing another
   * modifier's capacity stat) before the owning StatCapacityBarModule was decoded. The canonical
   * color is fixed up later by {@link #getOrCreateStat(ModifierId, int)}.
   */
  public static FloatToolStat getOrCreateStat(ModifierId id) {
    return getOrCreateStat(id, DEFAULT_CAPACITY_COLOR);
  }

  public static FloatToolStat getStat(ModifierId id) {
    return CAPACITY_STATS.get(id);
  }

  private StatCapacityBarManager() {}
}
