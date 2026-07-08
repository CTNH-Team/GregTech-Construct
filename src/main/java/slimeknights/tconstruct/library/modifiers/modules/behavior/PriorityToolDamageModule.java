package slimeknights.tconstruct.library.modifiers.modules.behavior;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.PriorityToolDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 基于 formula 的 tool damage 模块，支持自定义优先级。
 * 高优先级的模块先处理 damage。
 */
public record PriorityToolDamageModule(ResourceLocation formula, int priority, ModifierCondition<IToolStackView> condition)
  implements ModifierModule, PriorityToolDamageModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<PriorityToolDamageModule>defaultHooks(ModifierHooks.TOOL_DAMAGE);
  public static final RecordLoadable<PriorityToolDamageModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("formula", PriorityToolDamageModule::formula),
    IntLoadable.ANY_FULL.defaultField("priority", 0, PriorityToolDamageModule::priority),
    ModifierCondition.TOOL_FIELD,
    PriorityToolDamageModule::new);

  @Internal
  public PriorityToolDamageModule {}

  public static PriorityToolDamageModule formula(ResourceLocation formula, int priority) {
    return new PriorityToolDamageModule(formula, priority, ModifierCondition.ANY_TOOL);
  }

  @Override
  public int getToolDamagePriority() {
    return priority;
  }

  @Override
  public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
    if (!condition.matches(tool, modifier)) {
      return amount;
    }

    IFormula resolved = FormulaManager.getOrNull(formula);
    if (resolved == null) {
      return amount;
    }

    double damage = resolved.accept(modifier.getEffectiveLevel(), amount, capacity(tool), barAmount(tool));
    int wholeDamage = (int) damage;
    int roundedDamage = wholeDamage + (damage - wholeDamage > TConstruct.RANDOM.nextDouble() ? 1 : 0);
    return Math.max(0, roundedDamage);
  }

  private static int capacity(IToolStackView tool) {
    return Math.max(1, tool.getStats().getInt(ToolStats.DURABILITY));
  }

  private static int barAmount(IToolStackView tool) {
    return Math.max(0, tool.getCurrentDurability());
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<PriorityToolDamageModule> getLoader() {
    return LOADER;
  }
}
