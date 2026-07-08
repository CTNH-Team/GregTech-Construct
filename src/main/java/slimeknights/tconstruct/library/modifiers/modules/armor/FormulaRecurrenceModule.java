package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStat;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public record FormulaRecurrenceModule(ResourceLocation persistentArmorStatFormula, ResourceLocation damageToPersistentFormula,
                                      ResourceLocation persistentTickFormula, ResourceLocation key,
                                      int intervalTicks, ModifierCondition<IToolStackView> condition)
  implements ModifierModule, ArmorDamageStatsModifierHook, InventoryTickModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FormulaRecurrenceModule>defaultHooks(ModifierHooks.ARMOR_DAMAGE_STATS, ModifierHooks.INVENTORY_TICK);
  public static final RecordLoadable<FormulaRecurrenceModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("persistent_armor_stat_formula", FormulaRecurrenceModule::persistentArmorStatFormula),
    Loadables.RESOURCE_LOCATION.requiredField("damage_to_persistent_formula", FormulaRecurrenceModule::damageToPersistentFormula),
    Loadables.RESOURCE_LOCATION.requiredField("persistent_tick_formula", FormulaRecurrenceModule::persistentTickFormula),
    Loadables.RESOURCE_LOCATION.requiredField("key", FormulaRecurrenceModule::key),
    IntLoadable.FROM_ONE.defaultField("interval_ticks", 20, FormulaRecurrenceModule::intervalTicks),
    ModifierCondition.TOOL_FIELD,
    FormulaRecurrenceModule::new);

  @Internal
  public FormulaRecurrenceModule {}

  public static FormulaRecurrenceModule recurrence(ResourceLocation persistentArmorStatFormula, ResourceLocation damageToPersistentFormula,
                                                   ResourceLocation persistentTickFormula, ResourceLocation key) {
    return new FormulaRecurrenceModule(persistentArmorStatFormula, damageToPersistentFormula, persistentTickFormula, key, 20, ModifierCondition.ANY_TOOL);
  }

  @Override
  public void addArmorDamageStats(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, ArmorDamageStats stats) {
    if (source == null || source.is(DamageTypeTags.BYPASSES_ARMOR) || !condition.matches(tool, modifier)) {
      return;
    }

    IFormula persistentArmorStat = FormulaManager.getOrNull(persistentArmorStatFormula);
    IFormula damageToPersistent = FormulaManager.getOrNull(damageToPersistentFormula);
    if (persistentArmorStat == null || damageToPersistent == null) {
      return;
    }

    float value = Math.max(0, tool.getPersistentData().getFloat(key));
    if (value > 0) {
      stats.add(ArmorDamageStat.PRE_REDUCTION, value);
    }

    float level = modifier.getEffectiveLevel();
    float reduced = Math.max(0, stats.originalDamage() - stats.preReduction());
    float spent = Math.max(0, (float)persistentArmorStat.accept(value, 0, level, reduced));
    float gained = Math.max(0, (float)damageToPersistent.accept(spent, level, level, reduced));
    store(tool, gained);
  }

  @Override
  public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
    if (world.isClientSide || !isCorrectSlot || holder.tickCount % Math.max(1, intervalTicks) != 0 || !condition.matches(tool, modifier)) {
      return;
    }

    IFormula persistentTick = FormulaManager.getOrNull(persistentTickFormula);
    if (persistentTick == null) {
      return;
    }

    float value = Math.max(0, tool.getPersistentData().getFloat(key));
    if (value > 0) {
      store(tool, Math.max(0, (float)persistentTick.accept(value, 0, modifier.getEffectiveLevel())));
    }
  }

  private void store(IToolStackView tool, float value) {
    if (value < 0.01f) {
      tool.getPersistentData().remove(key);
    } else {
      tool.getPersistentData().putFloat(key, value);
    }
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<FormulaRecurrenceModule> getLoader() {
    return LOADER;
  }
}
