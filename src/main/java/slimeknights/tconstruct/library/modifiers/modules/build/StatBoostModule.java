package slimeknights.tconstruct.library.modifiers.modules.build;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
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
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStatId;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;
import java.util.Locale;

/** Module that boosts a tool stat */
public record StatBoostModule(INumericToolStat<?> stat, StatOperation operation, LevelingValue amount, ModifierCondition<IToolContext> condition) implements ToolStatsModifierHook, ModifierModule, ConditionalModule<IToolContext> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<StatBoostModule>defaultHooks(ModifierHooks.TOOL_STATS);

  /**
   * Loader for the stat field. Dynamic capacity stats (e.g. {@code tconstruct:crystal_lattice}) are
   * registered by their owning modifier's StatCapacityBarModule; the modifier sync packet iterates a
   * HashMap so the owning modifier may decode after this reference. Create the stat on demand instead
   * of failing with "Unknown stat type" so client/server decoding is order-independent.
   */
  private static final StringLoadable<INumericToolStat<?>> STAT_LOADER = ToolStatId.PARSER.comapFlatMap((id, error) -> {
    IToolStat<?> stat = ToolStats.getToolStat(id);
    if (stat != null) {
      if (stat instanceof INumericToolStat<?> numeric) {
        return numeric;
      }
      throw error.create("Invalid tool stat " + stat.getName() + ", must be a numeric stat");
    }
    return StatCapacityBarManager.getOrCreateStat(new ModifierId(id));
  }, INumericToolStat::getName);

  public static RecordLoadable<StatBoostModule> LOADER = RecordLoadable.create(
    STAT_LOADER.requiredField("stat", StatBoostModule::stat),
    new EnumLoadable<>(StatOperation.class).requiredField("operation", StatBoostModule::operation),
    LevelingValue.LOADABLE.directField(StatBoostModule::amount),
    ModifierCondition.CONTEXT_FIELD,
    StatBoostModule::new);

  /** @apiNote Internal constructor, use {@link #stat()} */
  @Internal
  public StatBoostModule {}

  @Override
  public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
    if (condition.matches(context, modifier) && stat.supports(context.getItem())) {
      operation.apply(builder, stat, amount.compute(modifier.getEffectiveLevel()));
    }
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<StatBoostModule> getLoader() {
    return LOADER;
  }


  /* Builder */

  /** Creates a builder for adding stats */
  public static Builder add(INumericToolStat<?> stat) {
    return new Builder(stat, StatOperation.ADD);
  }

  /** Creates a builder for adding stats */
  public static Builder multiplyBase(INumericToolStat<?> stat) {
    return new Builder(stat, StatOperation.MULTIPLY_BASE);
  }

  /** Creates a builder for adding stats */
  public static Builder multiplyConditional(INumericToolStat<?> stat) {
    return new Builder(stat, StatOperation.MULTIPLY_CONDITIONAL);
  }

  /** Creates a builder for adding stats */
  public static Builder multiplyAll(INumericToolStat<?> stat) {
    return new Builder(stat, StatOperation.MULTIPLY_ALL);
  }

  /** enum representing a single stat boost */
  public enum StatOperation {
    ADD {
      @Override
      public void apply(ModifierStatsBuilder builder, INumericToolStat<?> stat, float value) {
        stat.add(builder, value);
      }
    },
    PERCENT {
      @Override
      public void apply(ModifierStatsBuilder builder, INumericToolStat<?> stat, float value) {
        stat.percent(builder, value);
      }
    },
    MULTIPLY_BASE {
      @Override
      public void apply(ModifierStatsBuilder builder, INumericToolStat<?> stat, float value) {
        stat.multiply(builder, 1 + value);
      }
    },
    MULTIPLY_CONDITIONAL {
      @Override
      public void apply(ModifierStatsBuilder builder, INumericToolStat<?> stat, float value) {
        builder.multiplier(stat, 1 + value);
      }
    },
    MULTIPLY_ALL {
      @Override
      public void apply(ModifierStatsBuilder builder, INumericToolStat<?> stat, float value) {
        stat.multiplyAll(builder, 1 + value);
      }
    };

    @Getter
    private final String name = name().toLowerCase(Locale.ROOT);

    /** Applies this boost type for the given values. */
    public abstract void apply(ModifierStatsBuilder builder, INumericToolStat<?> stat, float value);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder extends ModuleBuilder.Context<Builder> implements LevelingValue.Builder<StatBoostModule> {
    private final INumericToolStat<?> stat;
    private final StatOperation operation;

    @Override
    public StatBoostModule amount(float flat, float eachLevel) {
      return new StatBoostModule(stat, operation, new LevelingValue(flat, eachLevel), condition);
    }
  }
}
