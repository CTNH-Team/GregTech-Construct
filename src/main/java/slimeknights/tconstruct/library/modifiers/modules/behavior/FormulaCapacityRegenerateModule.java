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
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.special.CapacityBarHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.capacity.CapacitySourceModule;
import slimeknights.tconstruct.library.modifiers.modules.capacity.StatCapacityBarManager;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

public record FormulaCapacityRegenerateModule(
    ModifierId targetModifier,
    ResourceLocation regenerateFormula,
    ResourceLocation duraConsumeFormula,
    int intervalTicks,
    ResourceLocation cooldownFormula,
    @Nullable ModifierId owner,
    ModifierCondition<IToolStackView> condition)
  implements ModifierModule, InventoryTickModifierHook, CapacitySourceModule, ConditionalModule<IToolStackView> {

  private static final String KEY_LAST_AMOUNT = "_reg_last";
  private static final String KEY_COOLDOWN_END = "_reg_cooldown";
  private static final String KEY_REM_GAIN = "_reg_rem";
  private static final String KEY_REM_DURA = "_reg_rem_dur";
  private static final String KEY_INTERVAL_OFFSET = "_reg_offset";

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FormulaCapacityRegenerateModule>defaultHooks(ModifierHooks.INVENTORY_TICK);

  public static final RecordLoadable<FormulaCapacityRegenerateModule> LOADER = RecordLoadable.create(
    ModifierId.PARSER.requiredField("target_modifier", FormulaCapacityRegenerateModule::targetModifier),
    Loadables.RESOURCE_LOCATION.requiredField("regenerate_formula", FormulaCapacityRegenerateModule::regenerateFormula),
    Loadables.RESOURCE_LOCATION.requiredField("dura_consume_formula", FormulaCapacityRegenerateModule::duraConsumeFormula),
    IntLoadable.FROM_ONE.defaultField("interval_ticks", 20, FormulaCapacityRegenerateModule::intervalTicks),
    Loadables.RESOURCE_LOCATION.requiredField("cooldown_formula", FormulaCapacityRegenerateModule::cooldownFormula),
    OWNER_FIELD,
    ModifierCondition.TOOL_FIELD,
    FormulaCapacityRegenerateModule::new
  );

  @Internal
  public FormulaCapacityRegenerateModule {}

  public static FormulaCapacityRegenerateModule regenerate(
      ModifierId targetModifier,
      ResourceLocation regenerateFormula,
      ResourceLocation duraConsumeFormula,
      ResourceLocation cooldownFormula) {
    return new FormulaCapacityRegenerateModule(targetModifier, regenerateFormula, duraConsumeFormula, 20, cooldownFormula, null, ModifierCondition.ANY_TOOL);
  }

  @Override
  public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
    if (world.isClientSide || tool.isBroken() || !isCorrectSlot || !condition.matches(tool, modifier)) {
      return;
    }

    CompoundTag persistentData = tool.getPersistentData();
    CompoundTag state = persistentData.getCompound(modifier.getId());

    // 间隔 tick 检查（避免每 tick 都计算）
    int intervalOffset = getIntervalOffset(state);
    if (holder.tickCount % intervalTicks != intervalOffset) {
      return;
    }

    int durability = tool.getCurrentDurability();
    if (durability <= 1) {
      return; // 没有足够的耐久度
    }

    // 获取目标 capacity bar
    ModifierEntry barModifier = barModifier(tool, modifier);
    CapacityBarHook capacityBar = StatCapacityBarManager.getCapacityBar(targetModifier);
    if (capacityBar == null) {
      return;
    }

    int amount = capacityBar.getAmount(tool);
    int capacity = capacityBar.getCapacity(tool, barModifier);
    int lastAmount = state.getInt(KEY_LAST_AMOUNT);
    long cooldownEnd = state.getLong(KEY_COOLDOWN_END);
    float level = modifier.getEffectiveLevel();

    // 加载 formulas
    IFormula regenFormula = FormulaManager.getOrNull(regenerateFormula);
    IFormula duraCostFormula = FormulaManager.getOrNull(duraConsumeFormula);
    IFormula coolFormula = FormulaManager.getOrNull(cooldownFormula);
    if (regenFormula == null || duraCostFormula == null || coolFormula == null) {
      return;
    }

    // 计算 cooldown
    long cooldownTicks = (long) coolFormula.accept(level, amount, capacity, lastAmount);
    if (cooldownTicks > 0 && amount < lastAmount) {
      cooldownEnd = world.getGameTime() + cooldownTicks;
      state.putLong(KEY_COOLDOWN_END, cooldownEnd);
    }
    state.putInt(KEY_LAST_AMOUNT, amount);

    // 检查是否在 cooldown 中
    if (world.getGameTime() < cooldownEnd) {
      persistentData.put(modifier.getId(), state);
      return;
    }

    // 已经满了
    if (amount >= capacity) {
      if (amount > capacity) {
        capacityBar.setAmount(tool, barModifier, capacity);
      }
      persistentData.put(modifier.getId(), state);
      return;
    }

    // 计算恢复量和消耗
    double gain = regenFormula.accept(level, amount, capacity, lastAmount);
    double duraCost = duraCostFormula.accept(level, gain, capacity, amount);

    if (gain <= 0) {
      persistentData.put(modifier.getId(), state);
      return;
    }

    // 消耗 durability
    if (duraCost > 0) {
      float duraRem = state.getFloat(KEY_REM_DURA) + (float) duraCost;
      int cost = (int) duraRem;
      if (cost > 0) {
        if (cost >= durability) {
          persistentData.put(modifier.getId(), state);
          return; // 耐久度不够
        }
        duraRem -= cost;
        ToolDamageUtil.directDamage(tool, cost, holder, stack);
      }
      state.putFloat(KEY_REM_DURA, duraRem);
    }

    // 增加 capacity（带小数累积）
    float rem = state.getFloat(KEY_REM_GAIN) + (float) gain;
    int intGain = (int) rem;
    if (intGain > 0) {
      rem -= intGain;
      capacityBar.setAmount(tool, barModifier, amount + intGain);
    }
    state.putFloat(KEY_REM_GAIN, rem);

    persistentData.put(modifier.getId(), state);
  }

  private int getIntervalOffset(CompoundTag state) {
    if (intervalTicks <= 4) {
      return 0;
    }
    int offset = state.getInt(KEY_INTERVAL_OFFSET);
    if (offset == 0) {
      offset = TConstruct.RANDOM.nextInt(intervalTicks) + 1;
      state.putInt(KEY_INTERVAL_OFFSET, offset);
    }
    if (offset == intervalTicks) {
      offset = 0;
    }
    return offset;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<FormulaCapacityRegenerateModule> getLoader() {
    return LOADER;
  }
}
