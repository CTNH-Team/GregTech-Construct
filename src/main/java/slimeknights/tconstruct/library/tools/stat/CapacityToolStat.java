package slimeknights.tconstruct.library.tools.stat;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public class CapacityToolStat extends FloatToolStat {

  public CapacityToolStat(ToolStatId name, int color, float defaultValue, float minValue, float maxValue, @Nullable TagKey<Item> tag) {
    super(name, color, defaultValue, minValue, maxValue, tag);
  }

  @Override
  public Float build(ModifierStatsBuilder parent, Object builderObj) {
    return super.build(parent, builderObj) * parent.getMultiplier(ToolStats.DURABILITY);
  }
}
