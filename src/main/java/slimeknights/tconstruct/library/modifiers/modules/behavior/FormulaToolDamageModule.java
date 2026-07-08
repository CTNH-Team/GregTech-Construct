package slimeknights.tconstruct.library.modifiers.modules.behavior;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;

public record FormulaToolDamageModule(ResourceLocation formula, ModifierCondition<IToolStackView> condition)
  implements ModifierModule, ToolDamageModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FormulaToolDamageModule>defaultHooks(ModifierHooks.TOOL_DAMAGE);
  public static final RecordLoadable<FormulaToolDamageModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("formula", FormulaToolDamageModule::formula),
    ModifierCondition.TOOL_FIELD,
    FormulaToolDamageModule::new);

  @Internal
  public FormulaToolDamageModule {}

  public static FormulaToolDamageModule formula(ResourceLocation formula) {
    return new FormulaToolDamageModule(formula, ModifierCondition.ANY_TOOL);
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
  public RecordLoadable<FormulaToolDamageModule> getLoader() {
    return LOADER;
  }
}
