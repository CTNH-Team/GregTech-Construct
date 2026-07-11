package slimeknights.tconstruct.library.modifiers.modules.carriage;

import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

/**
 * Allows ToolStats to participate in weighted-stat building.
 */
public interface IToolStatExtra {
    void update(ModifierStatsBuilder builder, float value, float weight);
}
