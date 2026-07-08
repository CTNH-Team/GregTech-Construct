package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

public record FormulaDamageLimitModule(ResourceLocation capFormula, ResourceLocation conditionFormula, ResourceLocation perArmorRatioFormula,
                                       ResourceLocation armorDamageFormula, ResourceLocation overshieldDamageFormula,
                                       ModifierCondition<IToolStackView> condition)
  implements ModifierModule, ArmorDamageStatsModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FormulaDamageLimitModule>defaultHooks(ModifierHooks.ARMOR_DAMAGE_STATS);
  public static final RecordLoadable<FormulaDamageLimitModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("cap_formula", FormulaDamageLimitModule::capFormula),
    Loadables.RESOURCE_LOCATION.requiredField("condition_formula", FormulaDamageLimitModule::conditionFormula),
    Loadables.RESOURCE_LOCATION.requiredField("per_armor_ratio_formula", FormulaDamageLimitModule::perArmorRatioFormula),
    Loadables.RESOURCE_LOCATION.requiredField("armor_damage_formula", FormulaDamageLimitModule::armorDamageFormula),
    Loadables.RESOURCE_LOCATION.requiredField("overshield_damage_formula", FormulaDamageLimitModule::overshieldDamageFormula),
    ModifierCondition.TOOL_FIELD,
    FormulaDamageLimitModule::new);

  @Internal
  public FormulaDamageLimitModule {}

  public static FormulaDamageLimitModule limit(ResourceLocation capFormula, ResourceLocation conditionFormula, ResourceLocation perArmorRatioFormula,
                                               ResourceLocation armorDamageFormula, ResourceLocation overshieldDamageFormula) {
    return new FormulaDamageLimitModule(capFormula, conditionFormula, perArmorRatioFormula, armorDamageFormula, overshieldDamageFormula, ModifierCondition.ANY_TOOL);
  }

  @Override
  public void addArmorDamageStats(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, ArmorDamageStats stats) {
    if (source.is(DamageTypeTags.BYPASSES_ARMOR) || !condition.matches(tool, modifier)) {
      return;
    }

    IFormula condition = FormulaManager.getOrNull(conditionFormula);
    IFormula cap = FormulaManager.getOrNull(capFormula);
    if (condition == null || cap == null) {
      return;
    }

    int capacity = Math.max(1, tool.getStats().getInt(ToolStats.DURABILITY));
    int amount = Math.max(0, tool.getCurrentDurability());
    float level = modifier.getEffectiveLevel();
    if (condition.accept(level, stats.originalDamage(), capacity, amount) > 0) {
      stats.addDamageLimit(tool, slotType, level, (float)cap.accept(level, stats.originalDamage(), capacity, amount),
        perArmorRatioFormula, armorDamageFormula, overshieldDamageFormula);
    }
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<FormulaDamageLimitModule> getLoader() {
    return LOADER;
  }
}
