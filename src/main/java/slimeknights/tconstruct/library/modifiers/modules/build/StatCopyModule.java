package slimeknights.tconstruct.library.modifiers.modules.build;

import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.capacity.StatCapacityBarManager;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;

public record StatCopyModule(
  INumericToolStat<?> source,
  @Nullable ModifierId targetModifierId,
  @Nullable INumericToolStat<?> targetStat,
  float multiplier,
  ModifierCondition<IToolContext> condition)
  implements ToolStatsModifierHook, ModifierModule, ConditionalModule<IToolContext> {

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<StatCopyModule>defaultHooks(ModifierHooks.TOOL_STATS);

  public static final RecordLoadable<StatCopyModule> LOADER = RecordLoadable.create(
    ToolStats.NUMERIC_LOADER.requiredField("source", StatCopyModule::source),
    ModifierId.PARSER.nullableField("target_modifier", StatCopyModule::targetModifierId),
    ToolStats.NUMERIC_LOADER.nullableField("target_stat", StatCopyModule::targetStat),
    FloatLoadable.ANY.defaultField("multiplier", 1.0f, StatCopyModule::multiplier),
    ModifierCondition.CONTEXT_FIELD,
    StatCopyModule::new
  );

  @Internal
  public StatCopyModule {}

  public static StatCopyModule copyToCapacity(INumericToolStat<?> source, ModifierId targetModifier, float multiplier) {
    return new StatCopyModule(source, targetModifier, null, multiplier, ModifierCondition.ANY_CONTEXT);
  }

  /**
   * 复制到固定 stat。
   */
  public static StatCopyModule copy(INumericToolStat<?> source, INumericToolStat<?> target, float multiplier) {
    return new StatCopyModule(source, null, target, multiplier, ModifierCondition.ANY_CONTEXT);
  }

  @Override
  public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
    if (!condition.matches(context, modifier)) {
      return;
    }

    if (!source.supports(context.getItem())) {
      return;
    }

    INumericToolStat<?> target = targetStat;
    if (target == null && targetModifierId != null) {
      FloatToolStat capacityStat = StatCapacityBarManager.getStat(targetModifierId);
      if (capacityStat == null) {
        return;
      }
      target = capacityStat;
    }

    if (target == null || !target.supports(context.getItem())) {
      return;
    }

    Number sourceValue = builder.getStat(source);
    target.add(builder, sourceValue.floatValue() * multiplier);
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<StatCopyModule> getLoader() {
    return LOADER;
  }
}
