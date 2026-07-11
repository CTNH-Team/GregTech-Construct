package slimeknights.tconstruct.library.modifiers.modules.build;

import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.LevelingValue;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.capacity.StatCapacityBarManager;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModuleBuilder;
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
  @Nullable LevelingValue levelingMultiplier,
  boolean normalizeSourceMultiplier,
  ModifierCondition<IToolContext> condition)
  implements ToolStatsModifierHook, ModifierModule, ConditionalModule<IToolContext> {

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<StatCopyModule>defaultHooks(ModifierHooks.TOOL_STATS);

  public static final RecordLoadable<StatCopyModule> LOADER = RecordLoadable.create(
    ToolStats.NUMERIC_LOADER.requiredField("source", StatCopyModule::source),
    ModifierId.PARSER.nullableField("target_modifier", StatCopyModule::targetModifierId),
    ToolStats.NUMERIC_LOADER.nullableField("target_stat", StatCopyModule::targetStat),
    FloatLoadable.ANY.defaultField("multiplier", 1.0f, StatCopyModule::multiplier),
    LevelingValue.LOADABLE.nullableField("leveling_multiplier", StatCopyModule::levelingMultiplier),
    BooleanLoadable.INSTANCE.defaultField("normalize_source_multiplier", false, false, StatCopyModule::normalizeSourceMultiplier),
    ModifierCondition.CONTEXT_FIELD,
    StatCopyModule::new
  );

  @Internal
  public StatCopyModule {}

  /** Compatibility constructor for static copies used by dynamic modifier data. */
  public StatCopyModule(INumericToolStat<?> source, @Nullable ModifierId targetModifierId, @Nullable INumericToolStat<?> targetStat, float multiplier, ModifierCondition<IToolContext> condition) {
    this(source, targetModifierId, targetStat, multiplier, null, false, condition);
  }

  public static StatCopyModule copyToCapacity(INumericToolStat<?> source, ModifierId targetModifier, float multiplier) {
    return new StatCopyModule(source, targetModifier, null, multiplier, ModifierCondition.ANY_CONTEXT);
  }

  /**
   * 复制到固定 stat。
   */
  public static StatCopyModule copy(INumericToolStat<?> source, INumericToolStat<?> target, float multiplier) {
    return new StatCopyModule(source, null, target, multiplier, ModifierCondition.ANY_CONTEXT);
  }

  /**
   * Creates a level-scaled copy from source to target. The source multiplier is removed so the
   * copied value is based on the source stat before multiplier effects, matching legacy behavior.
   */
  public static Builder builder(INumericToolStat<?> target, INumericToolStat<?> source) {
    return new Builder(target, source);
  }

  @Nullable
  @Override
  public Integer getPriority() {
    // Level-scaled copies must run after earlier stat modifiers to copy their final source value.
    return levelingMultiplier != null ? 50 : null;
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

    float copyMultiplier = multiplier;
    if (levelingMultiplier != null) {
      copyMultiplier = levelingMultiplier.compute(modifier.getEffectiveLevel());
      if (normalizeSourceMultiplier) {
        copyMultiplier /= builder.getMultiplier(source);
      }
    }
    Number sourceValue = builder.getStat(source);
    target.add(builder, sourceValue.floatValue() * copyMultiplier);
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<StatCopyModule> getLoader() {
    return LOADER;
  }

  /** Builder for a level-scaled stat copy. */
  public static class Builder extends ModuleBuilder.Context<Builder> implements LevelingValue.Builder<StatCopyModule> {
    private final INumericToolStat<?> target;
    private final INumericToolStat<?> source;

    private Builder(INumericToolStat<?> target, INumericToolStat<?> source) {
      this.target = target;
      this.source = source;
    }

    @Override
    public StatCopyModule amount(float flat, float eachLevel) {
      return new StatCopyModule(source, null, target, 1.0f, new LevelingValue(flat, eachLevel), true, condition);
    }
  }
}
