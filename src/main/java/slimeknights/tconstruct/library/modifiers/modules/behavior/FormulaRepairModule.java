package slimeknights.tconstruct.library.modifiers.modules.behavior;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

public record FormulaRepairModule(ResourceLocation regenerateFormula, ResourceLocation cooldownFormula, ModifierCondition<IToolStackView> condition)
  implements ModifierModule, InventoryTickModifierHook, ConditionalModule<IToolStackView> {
  private static final String LAST_AMOUNT = "last_amount";
  private static final String NEXT_TICK = "next_tick";
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FormulaRepairModule>defaultHooks(ModifierHooks.INVENTORY_TICK);
  public static final RecordLoadable<FormulaRepairModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("regenerate_formula", FormulaRepairModule::regenerateFormula),
    Loadables.RESOURCE_LOCATION.requiredField("cooldown_formula", FormulaRepairModule::cooldownFormula),
    ModifierCondition.TOOL_FIELD,
    FormulaRepairModule::new);

  @Internal
  public FormulaRepairModule {}

  public static FormulaRepairModule repair(ResourceLocation regenerateFormula, ResourceLocation cooldownFormula) {
    return new FormulaRepairModule(regenerateFormula, cooldownFormula, ModifierCondition.ANY_TOOL);
  }

  @Override
  public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
    if (world.isClientSide || !isCorrectSlot || holder.getUseItem() == stack || !condition.matches(tool, modifier)) {
      return;
    }

    IFormula repair = FormulaManager.getOrNull(regenerateFormula);
    IFormula cooldown = FormulaManager.getOrNull(cooldownFormula);
    if (repair == null || cooldown == null) {
      return;
    }

    CompoundTag state = tool.getPersistentData().getCompound(modifier.getId());
    long time = world.getGameTime();
    if (state.getLong(NEXT_TICK) > time) {
      return;
    }

    int capacity = Math.max(1, tool.getStats().getInt(ToolStats.DURABILITY));
    int amount = Math.max(0, tool.getCurrentDurability());
    int lastAmount = state.contains(LAST_AMOUNT) ? Math.max(0, state.getInt(LAST_AMOUNT)) : amount;
    ToolDamageUtil.repair(tool, roundPositive(repair.accept(modifier.getEffectiveLevel(), amount, capacity, lastAmount)));

    int nextAmount = Math.max(0, tool.getCurrentDurability());
    state.putInt(LAST_AMOUNT, nextAmount);
    state.putLong(NEXT_TICK, time + Math.max(1, roundPositive(cooldown.accept(modifier.getEffectiveLevel(), nextAmount, capacity, amount))));
    tool.getPersistentData().put(modifier.getId(), state);
  }

  private static int roundPositive(double value) {
    if (value <= 0) {
      return 0;
    }
    int whole = (int) value;
    return whole + (value - whole > TConstruct.RANDOM.nextDouble() ? 1 : 0);
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<FormulaRepairModule> getLoader() {
    return LOADER;
  }
}
