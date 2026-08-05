package slimeknights.tconstruct.library.tools.stat;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public class CapacityToolStat extends FloatToolStat {

  public CapacityToolStat(ToolStatId name, int color, float defaultValue, float minValue, float maxValue, @Nullable TagKey<Item> tag) {
    super(name, color, defaultValue, minValue, maxValue, tag);
  }

  /** Sets the display color. Public so {@code StatCapacityBarManager} can apply the canonical
   * color of the owning modifier when this stat was created earlier via the decode-time fallback.
   */
  public void setColor(int color) {
    super.setColor(color);
  }

  @Override
  public Float build(ModifierStatsBuilder parent, Object builderObj) {
    return super.build(parent, builderObj) * parent.getMultiplier(ToolStats.DURABILITY);
  }
}
