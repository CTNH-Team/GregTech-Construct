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
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.hook.armor.PlayerLoginModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.special.CapacityBarHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ShareDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.logic.GuardingCache;

import java.util.List;

public record FormulaGuardingModule(float healthGround, int fullEffectRange, int effectiveRange,
                                    ResourceLocation distanceFactorFormula, ResourceLocation shareRatioFormula,
                                    ResourceLocation extraProtectionFormula, ModifierCondition<IToolStackView> condition)
  implements ModifierModule, ShareDamageModifierHook, EquipmentChangeModifierHook, PlayerLoginModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FormulaGuardingModule>defaultHooks(ModifierHooks.SHARE_DAMAGE, ModifierHooks.EQUIPMENT_CHANGE, ModifierHooks.PLAYER_LOGIN);
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

    int platingLevel = tool.getModifierLevel(ModifierIds.plating);
    if (platingLevel <= 0) {
      return false;
    }
    ModifierEntry plating = new ModifierEntry(ModifierManager.INSTANCE.get(ModifierIds.plating), Math.max(1, platingLevel));
    CapacityBarHook bar = plating.getHook(ModifierHooks.CAPACITY_BAR);
    if (bar == ModifierHooks.CAPACITY_BAR.getDefaultInstance()) {
      return false;
    }
    int capacity = Math.max(1, bar.getCapacity(tool, plating));
    int amount = Math.max(0, bar.getAmount(tool));
    double level = modifier.getEffectiveLevel();
    float distanceFactor = distanceFormula == null ? 1f : Mth.clamp((float)distanceFormula.accept(distance, effectiveRange, fullEffectRange), 0, 1);
    float shareRatio = shareFormula == null ? 0f : Mth.clamp((float)shareFormula.accept(0, level, capacity, amount), 0, 1);
    float extraProtection = protectionFormula == null ? 0f : Mth.clamp((float)protectionFormula.accept(0, level, capacity, amount), 0, 1);
    context.add(shareRatio, extraProtection, healthGround, distanceFactor);
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
