package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ParameterProviderHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.PlayerLoginModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ShareDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.parameter.Parameter;
import slimeknights.tconstruct.library.modifiers.modules.parameter.ParameterProvider;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.tools.logic.GuardingCache;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record FormulaGuardingModule(float healthGround, int fullEffectRange, int effectiveRange,
                                    ResourceLocation distanceFactorFormula, ResourceLocation shareRatioFormula,
                                    ResourceLocation extraProtectionFormula,
                                    @Nullable List<String> parameter,
                                    @Nullable List<String> carrier,
                                    ModifierCondition<IToolStackView> condition)
  implements ModifierModule, ShareDamageModifierHook, EquipmentChangeModifierHook, PlayerLoginModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FormulaGuardingModule>defaultHooks(ModifierHooks.SHARE_DAMAGE, ModifierHooks.EQUIPMENT_CHANGE, ModifierHooks.PLAYER_LOGIN);
  public static final RecordLoadable<FormulaGuardingModule> LOADER = RecordLoadable.create(
    FloatLoadable.FROM_ZERO.defaultField("health_ground", 10f, FormulaGuardingModule::healthGround),
    IntLoadable.FROM_ZERO.defaultField("full_effect_range", 4, FormulaGuardingModule::fullEffectRange),
    IntLoadable.FROM_ONE.defaultField("effective_range", 16, FormulaGuardingModule::effectiveRange),
    Loadables.RESOURCE_LOCATION.requiredField("distance_factor_formula", FormulaGuardingModule::distanceFactorFormula),
    Loadables.RESOURCE_LOCATION.requiredField("share_ratio_formula", FormulaGuardingModule::shareRatioFormula),
    Loadables.RESOURCE_LOCATION.requiredField("extra_protection_formula", FormulaGuardingModule::extraProtectionFormula),
    StringLoadable.DEFAULT.list(0).nullableField("parameter", FormulaGuardingModule::parameter),
    StringLoadable.DEFAULT.list(0).nullableField("carrier", FormulaGuardingModule::carrier),
    ModifierCondition.TOOL_FIELD,
    FormulaGuardingModule::new);

  @Internal
  public FormulaGuardingModule {}

  public static FormulaGuardingModule guarding(ResourceLocation distanceFactorFormula, ResourceLocation shareRatioFormula, ResourceLocation extraProtectionFormula) {
    return new FormulaGuardingModule(10f, 4, 16, distanceFactorFormula, shareRatioFormula, extraProtectionFormula, null, null, ModifierCondition.ANY_TOOL);
  }

  @Override
  public boolean collectShareDamage(IToolStackView tool, ModifierEntry modifier, LivingEntity guardian, EquipmentSlot slotType,
                                    LivingEntity protectedEntity, DamageSource source, ShareDamageContext context) {
    if (guardian.getHealth() <= healthGround || !condition.matches(tool, modifier)) {
      return false;
    }
    if (protectedEntity instanceof Player protectedPlayer && GuardingCache.hasHook(protectedPlayer.getUUID(), modifier.getId())) {
      return false;
    }

    double distance = guardian.distanceTo(protectedEntity);
    if (distance > effectiveRange) {
      return false;
    }

    IFormula distanceFormula = FormulaManager.getOrNull(distanceFactorFormula);
    IFormula shareFormula = FormulaManager.getOrNull(shareRatioFormula);
    IFormula protectionFormula = FormulaManager.getOrNull(extraProtectionFormula);

    // resolve parameter provider inputs into a double[] for formulas
    double[] params = new double[4];
    params[0] = 0;           // stored_value (accumulator placeholder)
    params[1] = modifier.getEffectiveLevel();
    params[2] = 1;           // capacity (default)
    params[3] = 0;           // amount (default)
	  if (parameter != null && !parameter.isEmpty()) {
	      ParameterProviderHook hook = modifier.getHook(ModifierHooks.PARAMETER_PROVIDER);
      if (hook != null) {
        java.util.List<ParameterProvider> providers = new java.util.ArrayList<>();
        hook.collectProviders(parameter.get(0), providers);
        int extraLen = 0;
        for (var p : providers) extraLen = p.fieldCount(extraLen);
        if (extraLen > 0) {
          var pa = new Parameter(extraLen);
          for (var p : providers) p.pushTo(pa, tool, modifier, null, false);
          var extraArr = pa.get();
          params = new double[4 + extraLen];
          params[0] = 0;
          params[1] = modifier.getEffectiveLevel();
          params[2] = 1;
          params[3] = 0;
          System.arraycopy(extraArr, 0, params, 4, extraLen);
        }
      }
    }

    double level = modifier.getEffectiveLevel();
    float distanceFactor = distanceFormula == null ? 1f : Mth.clamp((float)distanceFormula.accept(distance, effectiveRange, fullEffectRange), 0, 1);
    float shareRatio = shareFormula == null ? 0f : Mth.clamp((float)shareFormula.accept(params[0], level, params[2], params[3]), 0, 1);
    float extraProtection = protectionFormula == null ? 0f : Mth.clamp((float)protectionFormula.accept(params[0], level, params[2], params[3]), 0, 1);
    context.add(shareRatio, extraProtection, healthGround, distanceFactor, modifier.getModifier().getColor());
    return true;
  }

  @Override
  public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (!(context.getEntity() instanceof Player player) || context.getLevel().isClientSide()) {
      return;
    }
    IToolStackView replacement = context.getReplacementTool();
    if (replacement == null || replacement.getItem() != tool.getItem()) {
      GuardingCache.addHook(player.getUUID(), modifier.getId());
    }
  }

  @Override
  public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (!(context.getEntity() instanceof Player player) || context.getLevel().isClientSide()) {
      return;
    }
    IToolStackView replacement = context.getReplacementTool();
    if (replacement == null || replacement.getItem() != tool.getItem()) {
      GuardingCache.removeHook(player.getUUID(), modifier.getId());
    }
  }

  @Override
  public void onPlayerLogin(IToolStackView tool, ModifierEntry modifier, Player player) {
    if (!player.level().isClientSide()) {
      GuardingCache.addHook(player.getUUID(), modifier.getId());
    }
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
