package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ShareDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

public record FormulaGuardingModule(float healthGround, int fullEffectRange, int effectiveRange,
                                    ResourceLocation distanceFactorFormula, ResourceLocation shareRatioFormula,
                                    ResourceLocation extraProtectionFormula, ModifierCondition<IToolStackView> condition)
  implements ModifierModule, ShareDamageModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FormulaGuardingModule>defaultHooks(ModifierHooks.SHARE_DAMAGE);
  public static final RecordLoadable<FormulaGuardingModule> LOADER = RecordLoadable.create(
    FloatLoadable.FROM_ZERO.defaultField("health_ground", 10f, FormulaGuardingModule::healthGround),
    IntLoadable.FROM_ZERO.defaultField("full_effect_range", 4, FormulaGuardingModule::fullEffectRange),
    IntLoadable.FROM_ONE.defaultField("effective_range", 16, FormulaGuardingModule::effectiveRange),
    Loadables.RESOURCE_LOCATION.requiredField("distance_factor_formula", FormulaGuardingModule::distanceFactorFormula),
    Loadables.RESOURCE_LOCATION.requiredField("share_ratio_formula", FormulaGuardingModule::shareRatioFormula),
    Loadables.RESOURCE_LOCATION.requiredField("extra_protection_formula", FormulaGuardingModule::extraProtectionFormula),
    ModifierCondition.TOOL_FIELD,
    FormulaGuardingModule::new);

  @Internal
  public FormulaGuardingModule {}

  public static FormulaGuardingModule guarding(ResourceLocation distanceFactorFormula, ResourceLocation shareRatioFormula, ResourceLocation extraProtectionFormula) {
    return new FormulaGuardingModule(10f, 4, 16, distanceFactorFormula, shareRatioFormula, extraProtectionFormula, ModifierCondition.ANY_TOOL);
  }

  @Override
  public float shareDamage(IToolStackView tool, ModifierEntry modifier, LivingEntity guardian, EquipmentSlot slotType, LivingEntity protectedEntity, DamageSource source, float damage) {
    if (damage <= 0 || guardian.getHealth() <= healthGround || !condition.matches(tool, modifier)) {
      return 0;
    }

    double distance = guardian.distanceTo(protectedEntity);
    if (distance > effectiveRange) {
      return 0;
    }

    IFormula distanceFormula = FormulaManager.getOrNull(distanceFactorFormula);
    IFormula shareFormula = FormulaManager.getOrNull(shareRatioFormula);
    IFormula protectionFormula = FormulaManager.getOrNull(extraProtectionFormula);
    if (distanceFormula == null || shareFormula == null || protectionFormula == null) {
      return 0;
    }

    int capacity = Math.max(1, tool.getStats().getInt(ToolStats.DURABILITY));
    int amount = Math.max(0, tool.getCurrentDurability());
    double level = modifier.getEffectiveLevel();
    float distanceFactor = Mth.clamp((float)distanceFormula.accept(distance, effectiveRange, fullEffectRange), 0, 1);
    float shareRatio = Mth.clamp((float)shareFormula.accept(0, level, capacity, amount), 0, 1) * distanceFactor;
    if (shareRatio <= 0) {
      return 0;
    }

    float shared = damage * shareRatio;
    float guardianDamage = shared * (1 - Mth.clamp((float)protectionFormula.accept(0, level, capacity, amount), 0, 1));
    float maxGuardianDamage = Math.max(0, guardian.getHealth() - healthGround);
    if (guardianDamage > maxGuardianDamage) {
      float scale = maxGuardianDamage / guardianDamage;
      guardianDamage = maxGuardianDamage;
      shared *= scale;
    }
    if (guardianDamage <= 0 || shared <= 0) {
      return 0;
    }

    guardian.hurt(source, guardianDamage);
    return shared;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<FormulaGuardingModule> getLoader() {
    return LOADER;
  }
}
