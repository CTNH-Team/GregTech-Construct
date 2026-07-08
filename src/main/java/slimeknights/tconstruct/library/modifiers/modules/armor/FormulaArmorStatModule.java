package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStat;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStats;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

public record FormulaArmorStatModule(ArmorDamageStat stat, ResourceLocation formula, IJsonPredicate<DamageSource> source,
                                     ModifierCondition<IToolStackView> condition)
  implements ModifierModule, ArmorDamageStatsModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FormulaArmorStatModule>defaultHooks(ModifierHooks.ARMOR_DAMAGE_STATS);
  public static final RecordLoadable<FormulaArmorStatModule> LOADER = RecordLoadable.create(
    new EnumLoadable<>(ArmorDamageStat.class).requiredField("stat", FormulaArmorStatModule::stat),
    Loadables.RESOURCE_LOCATION.requiredField("formula", FormulaArmorStatModule::formula),
    DamageSourcePredicate.LOADER.defaultField("damage_source", DamageSourcePredicate.ANY, FormulaArmorStatModule::source),
    ModifierCondition.TOOL_FIELD,
    FormulaArmorStatModule::new);

  @Internal
  public FormulaArmorStatModule {}

  public static FormulaArmorStatModule stat(ArmorDamageStat stat, ResourceLocation formula) {
    return new FormulaArmorStatModule(stat, formula, DamageSourcePredicate.ANY, ModifierCondition.ANY_TOOL);
  }

  @Override
  public void addArmorDamageStats(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, ArmorDamageStats stats) {
    if (!condition.matches(tool, modifier) || !this.source.matches(source)) {
      return;
    }

    IFormula resolved = FormulaManager.getOrNull(formula);
    if (resolved != null) {
      stats.add(stat, (float) resolved.accept(currentValue(stats), modifier.getEffectiveLevel(), capacity(tool), amount(tool)));
    }
  }

  private float currentValue(ArmorDamageStats stats) {
    return switch (stat) {
      case ARMOR_STRENGTH -> stats.armorStrength();
      case PRE_REDUCTION -> stats.preReduction();
      case POST_REDUCTION -> stats.postReduction();
      case ARMOR_PROTECTION -> stats.armorProtection();
      case ARMOR_ABSORPTION_CAP -> stats.armorAbsorptionCap();
    };
  }

  private static int capacity(IToolStackView tool) {
    return Math.max(1, tool.getStats().getInt(ToolStats.DURABILITY));
  }

  private static int amount(IToolStackView tool) {
    return Math.max(0, tool.getCurrentDurability());
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<FormulaArmorStatModule> getLoader() {
    return LOADER;
  }
}
