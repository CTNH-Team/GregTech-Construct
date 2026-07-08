package slimeknights.tconstruct.library.modifiers.modules.build;

import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

public record StatCopyModule(INumericToolStat<?> source, INumericToolStat<?> target, float multiplier, ModifierCondition<IToolContext> condition)
  implements ToolStatsModifierHook, ModifierModule, ConditionalModule<IToolContext> {

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<StatCopyModule>defaultHooks(ModifierHooks.TOOL_STATS);

  public static final RecordLoadable<StatCopyModule> LOADER = RecordLoadable.create(
    ToolStats.NUMERIC_LOADER.requiredField("source", StatCopyModule::source),
    ToolStats.NUMERIC_LOADER.requiredField("target", StatCopyModule::target),
    FloatLoadable.ANY.defaultField("multiplier", 1.0f, StatCopyModule::multiplier),
    ModifierCondition.CONTEXT_FIELD,
    StatCopyModule::new
  );

  @Internal
  public StatCopyModule {}

  public static StatCopyModule copy(INumericToolStat<?> source, INumericToolStat<?> target, float multiplier) {
    return new StatCopyModule(source, target, multiplier, ModifierCondition.ANY_CONTEXT);
  }

  @Override
  public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
    if (!condition.matches(context, modifier)) {
      return;
    }

    if (!source.supports(context.getItem()) || !target.supports(context.getItem())) {
      return;
    }

    float sourceValue = builder.getStat(source);
    target.multiplyBase(builder, sourceValue * multiplier);
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
