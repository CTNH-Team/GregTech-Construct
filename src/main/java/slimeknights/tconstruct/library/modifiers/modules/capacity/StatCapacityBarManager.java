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
  private static final Map<ModifierId, CapacityBarHook> CAPACITY_BARS = new HashMap<>();
  private static final Map<ModifierId, FloatToolStat> CAPACITY_STATS = new HashMap<>();

  public static void register(ModifierId id, CapacityBarHook hook) {
    CAPACITY_BARS.put(id, hook);
  }

  public static CapacityBarHook getCapacityBar(ModifierId id) {
    return CAPACITY_BARS.get(id);
  }

  public static FloatToolStat getOrCreateStat(ModifierId id, int color) {
    return CAPACITY_STATS.computeIfAbsent(id, k ->
      ToolStats.register(new CapacityToolStat(
        new ToolStatId(id),
        color,
        0,      // defaultValue
        0,      // minValue
        Integer.MAX_VALUE,
        TinkerTags.Items.DURABILITY
      ))
    );
  }

  public static FloatToolStat getStat(ModifierId id) {
    return CAPACITY_STATS.get(id);
  }

  private StatCapacityBarManager() {}
}
