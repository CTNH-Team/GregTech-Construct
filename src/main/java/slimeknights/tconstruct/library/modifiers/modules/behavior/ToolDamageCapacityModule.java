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
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.behavior.PriorityToolDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.special.CapacityBarHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.capacity.CapacitySourceModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Tool damage 模块，同时消耗 capacity bar。
 * 用于实现 plating 和 crystal_lattice 的护盾消耗机制。
 *
 * 工作流程：
 * 1. 计算需要消耗的 capacity（通过 capacityFormula）
 * 2. 从 capacity bar 中扣除
 * 3. 计算剩余的 tool damage（通过 damageFormula）
 */
public record ToolDamageCapacityModule(
    ResourceLocation damageFormula,
    ResourceLocation capacityFormula,
    int priority,
    @Nullable ModifierId owner,
    ModifierCondition<IToolStackView> condition)
  implements ModifierModule, PriorityToolDamageModifierHook, CapacitySourceModule, ConditionalModule<IToolStackView> {

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ToolDamageCapacityModule>defaultHooks(ModifierHooks.TOOL_DAMAGE);
  public static final RecordLoadable<ToolDamageCapacityModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("damage_formula", ToolDamageCapacityModule::damageFormula),
    Loadables.RESOURCE_LOCATION.requiredField("capacity_formula", ToolDamageCapacityModule::capacityFormula),
    IntLoadable.ANY_FULL.defaultField("priority", 0, ToolDamageCapacityModule::priority),
    OWNER_FIELD,
    ModifierCondition.TOOL_FIELD,
    ToolDamageCapacityModule::new);

  @Internal
  public ToolDamageCapacityModule {}

  public static ToolDamageCapacityModule of(ResourceLocation damageFormula, ResourceLocation capacityFormula, int priority) {
    return new ToolDamageCapacityModule(damageFormula, capacityFormula, priority, null, ModifierCondition.ANY_TOOL);
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

    ModifierEntry barModifier = barModifier(tool, modifier);
    CapacityBarHook bar = barModifier.getHook(ModifierHooks.CAPACITY_BAR);

    int capacity = bar.getCapacity(tool, barModifier);
    int barAmount = bar.getAmount(tool);
    int originalBarAmount = barAmount;

    IFormula capacityFormula = FormulaManager.getOrNull(this.capacityFormula);
    if (capacityFormula != null) {
      double consumed = capacityFormula.accept(modifier.getEffectiveLevel(), amount, capacity, barAmount);
      int consumedInt = (int) consumed;
      int roundedConsumed = consumedInt + (consumed - consumedInt > TConstruct.RANDOM.nextDouble() ? 1 : 0);

      int actualConsumed = Math.min(roundedConsumed, barAmount);
      if (actualConsumed > 0) {
        bar.setAmount(tool, barModifier, barAmount - actualConsumed);
        barAmount -= actualConsumed;
      }
    }

    IFormula damageFormula = FormulaManager.getOrNull(this.damageFormula);
    if (damageFormula == null) {
      return amount;
    }

      double damage = damageFormula.accept(modifier.getEffectiveLevel(), amount, capacity, originalBarAmount);
    int wholeDamage = (int) damage;
    int roundedDamage = wholeDamage + (damage - wholeDamage > TConstruct.RANDOM.nextDouble() ? 1 : 0);
    return Math.max(0, roundedDamage);
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<ToolDamageCapacityModule> getLoader() {
    return LOADER;
  }
}
