package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.AroundEntityTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record FormulaAreaEffectModule(ResourceLocation effect, ResourceLocation rangeFormula, AreaEffectFormulaConfig accumulator,
                                      AreaEffectFormulaConfig finalizer, @Nullable IJsonPredicate<LivingEntity> entityFilter,
                                      boolean tamedOnly, int intervalTicks, ModifierCondition<IToolStackView> condition)
  implements ModifierModule, InventoryTickModifierHook, AroundEntityTickModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FormulaAreaEffectModule>defaultHooks(ModifierHooks.INVENTORY_TICK, ModifierHooks.AROUND_ENTITY_TICK);
  private static final int TARGET_COOLDOWN_STEP = 0x800;
  private static final int TARGET_COOLDOWN_TICKS = 9;
  private static final Map<UUID,Map<AreaEffectKey,PendingAreaEffect>> PENDING_TAMED_EFFECTS = new HashMap<>();
  public static final RecordLoadable<FormulaAreaEffectModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("effect", FormulaAreaEffectModule::effect),
    Loadables.RESOURCE_LOCATION.requiredField("range_formula", FormulaAreaEffectModule::rangeFormula),
    AreaEffectFormulaConfig.LOADER.requiredField("accumulator", FormulaAreaEffectModule::accumulator),
    AreaEffectFormulaConfig.LOADER.requiredField("finalizer", FormulaAreaEffectModule::finalizer),
    LivingEntityPredicate.LOADER.nullableField("entity_filter", FormulaAreaEffectModule::entityFilter),
    BooleanLoadable.INSTANCE.defaultField("tamed_only", false, FormulaAreaEffectModule::tamedOnly),
    IntLoadable.FROM_ONE.defaultField("interval_ticks", 10, FormulaAreaEffectModule::intervalTicks),
    ModifierCondition.TOOL_FIELD,
    FormulaAreaEffectModule::new);

  @Internal
  public FormulaAreaEffectModule {}

  public static FormulaAreaEffectModule tamed(ResourceLocation effect, ResourceLocation rangeFormula,
                                             ResourceLocation accumulatorDurationFormula, ResourceLocation accumulatorLevelFormula,
                                             ResourceLocation finalizerDurationFormula, ResourceLocation finalizerLevelFormula) {
    return new FormulaAreaEffectModule(effect, rangeFormula,
      new AreaEffectFormulaConfig(accumulatorDurationFormula, accumulatorLevelFormula),
      new AreaEffectFormulaConfig(finalizerDurationFormula, finalizerLevelFormula),
      null, true, 10, ModifierCondition.ANY_TOOL);
  }

  @Override
  public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
    if (tamedOnly) {
      return;
    }
    if (world.isClientSide || !isCorrectSlot || holder.tickCount % Math.max(1, intervalTicks) != 0 || !condition.matches(tool, modifier)) {
      return;
    }

    MobEffect mobEffect = resolveEffect();
    IFormula range = FormulaManager.getOrNull(rangeFormula);
    IFormula accumulatorDuration = FormulaManager.getOrNull(accumulator.durationFormula);
    IFormula accumulatorLevel = FormulaManager.getOrNull(accumulator.levelFormula);
    IFormula finalizerDuration = FormulaManager.getOrNull(finalizer.durationFormula);
    IFormula finalizerLevel = FormulaManager.getOrNull(finalizer.levelFormula);
    if (mobEffect == null || range == null || accumulatorDuration == null || accumulatorLevel == null || finalizerDuration == null || finalizerLevel == null) {
      return;
    }

    float level = modifier.getEffectiveLevel();
    float radius = Math.max(0, (float)range.accept(level));
    if (radius <= 0) {
      return;
    }

    for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, holder.getBoundingBox().inflate(radius), target -> matches(holder, target))) {
      applyToTarget(holder, tool, modifier, target, holder.distanceTo(target), radius, mobEffect, accumulatorDuration, accumulatorLevel, finalizerDuration, finalizerLevel);
    }
  }

  @Override
  public void onAroundEntityTickStart(IToolStackView tool, ModifierEntry modifier, net.minecraft.world.entity.EquipmentSlot slotType, net.minecraft.world.entity.player.Player player, LivingEntity target, double distance) {
    if (!tamedOnly || !condition.matches(tool, modifier)) {
      return;
    }
    MobEffect mobEffect = resolveEffect();
    IFormula range = FormulaManager.getOrNull(rangeFormula);
    IFormula accumulatorDuration = FormulaManager.getOrNull(accumulator.durationFormula);
    IFormula accumulatorLevel = FormulaManager.getOrNull(accumulator.levelFormula);
    IFormula finalizerDuration = FormulaManager.getOrNull(finalizer.durationFormula);
    IFormula finalizerLevel = FormulaManager.getOrNull(finalizer.levelFormula);
    if (mobEffect == null || range == null || accumulatorDuration == null || accumulatorLevel == null || finalizerDuration == null || finalizerLevel == null) {
      return;
    }

    float level = modifier.getEffectiveLevel();
    float radius = Math.max(0, (float)range.accept(level));
    if (radius <= 0) {
      return;
    }
    if (!matches(player, target) || distance > radius || isOnCooldown(target, modifier)) {
      return;
    }
    PendingAreaEffect pending = PENDING_TAMED_EFFECTS
      .computeIfAbsent(player.getUUID(), ignored -> new HashMap<>())
      .computeIfAbsent(new AreaEffectKey(this, modifier.getId()), ignored -> new PendingAreaEffect(mobEffect, finalizerDuration, finalizerLevel));
    pending.accumulate(target, modifier.getEffectiveLevel(), distance, accumulatorDuration, accumulatorLevel);
  }

  private boolean matches(LivingEntity holder, LivingEntity target) {
    if (target == holder || !target.isAlive()) {
      return false;
    }
    if (tamedOnly) {
      return target instanceof TamableAnimal tamable && tamable.getOwner() == holder;
    }
    return entityFilter == null || entityFilter.matches(target);
  }

  @Nullable
  private MobEffect resolveEffect() {
    return ForgeRegistries.MOB_EFFECTS.getValue(effect);
  }

  private void applyToTarget(LivingEntity holder, IToolStackView tool, ModifierEntry modifier, LivingEntity target, double distance, float radius, MobEffect mobEffect, IFormula accumulatorDuration, IFormula accumulatorLevel, IFormula finalizerDuration, IFormula finalizerLevel) {
    if (!matches(holder, target) || distance > radius) {
      return;
    }
    float level = modifier.getEffectiveLevel();
    float storedDuration = Math.max(0, (float)accumulatorDuration.accept(0, level, distance));
    float storedLevel = Math.max(0, (float)accumulatorLevel.accept(-1, level, distance));
    MobEffectInstance existing = target.getEffect(mobEffect);
    float currentDuration = existing == null ? 0 : existing.getDuration();
    float currentAmplifier = existing == null ? -1 : existing.getAmplifier();
    int duration = Mth.floor(Math.max(0, (float)finalizerDuration.accept(currentDuration, storedDuration, level)));
    int amplifier = Mth.floor(Math.max(-1, (float)finalizerLevel.accept(currentAmplifier, storedLevel, level)));
    if (duration > 0 && amplifier >= 0) {
      target.addEffect(new MobEffectInstance(mobEffect, duration, amplifier, false, true, true));
    }
  }

  private boolean isOnCooldown(LivingEntity target, ModifierEntry modifier) {
    String key = modifier.getId().toString();
    int cooldown = target.getPersistentData().getInt(key);
    int level = modifier.getLevel();
    if (cooldown == 0 || (cooldown & 0x7FF) != level) {
      target.getPersistentData().putInt(key, (TARGET_COOLDOWN_TICKS << 16) | level);
      return false;
    }
    cooldown = cooldown < TARGET_COOLDOWN_STEP ? 0 : cooldown - TARGET_COOLDOWN_STEP;
    target.getPersistentData().putInt(key, cooldown);
    return true;
  }

  public static void flushPending(Player player) {
    Map<AreaEffectKey,PendingAreaEffect> pending = PENDING_TAMED_EFFECTS.remove(player.getUUID());
    if (pending == null) {
      return;
    }
    for (PendingAreaEffect effect : pending.values()) {
      effect.apply();
    }
  }

  private record AreaEffectKey(FormulaAreaEffectModule module, slimeknights.tconstruct.library.modifiers.ModifierId modifierId) {}

  private static final class PendingAreaEffect {
    private final MobEffect mobEffect;
    private final IFormula finalizerDuration;
    private final IFormula finalizerLevel;
    private final Set<LivingEntity> evaluated = new HashSet<>();
    private final List<LivingEntity> targets = new ArrayList<>();
    private float duration;
    private float amplifier;
    private float totalLevel;

    private PendingAreaEffect(MobEffect mobEffect, IFormula finalizerDuration, IFormula finalizerLevel) {
      this.mobEffect = mobEffect == null ? MobEffects.GLOWING : mobEffect;
      this.finalizerDuration = finalizerDuration;
      this.finalizerLevel = finalizerLevel;
    }

    private void accumulate(LivingEntity target, float level, double distance, IFormula accumulatorDuration, IFormula accumulatorLevel) {
      if (!evaluated.add(target)) {
        return;
      }
      targets.add(target);
      totalLevel += level;
      duration = Math.max(0, (float) accumulatorDuration.accept(duration, level, distance));
      amplifier = Math.max(0, (float) accumulatorLevel.accept(amplifier, level, distance));
    }

    private void apply() {
      for (LivingEntity target : targets) {
        MobEffectInstance existing = target.getEffect(mobEffect);
        float currentDuration = existing == null ? 0 : existing.getDuration();
        float currentAmplifier = existing == null ? -1 : existing.getAmplifier();
        int resolvedDuration = Mth.floor(Math.max(0, (float) finalizerDuration.accept(currentDuration, duration, totalLevel)));
        int resolvedAmplifier = Mth.floor(Math.max(-1, (float) finalizerLevel.accept(currentAmplifier, amplifier, totalLevel)));
        if (resolvedDuration > 0 && resolvedAmplifier >= 0) {
          target.addEffect(new MobEffectInstance(mobEffect, resolvedDuration, resolvedAmplifier, false, true, true));
        }
      }
    }
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<FormulaAreaEffectModule> getLoader() {
    return LOADER;
  }

  public record AreaEffectFormulaConfig(ResourceLocation durationFormula, ResourceLocation levelFormula) {
    private static final RecordLoadable<AreaEffectFormulaConfig> LOADER = RecordLoadable.create(
      Loadables.RESOURCE_LOCATION.requiredField("duration_formula", AreaEffectFormulaConfig::durationFormula),
      Loadables.RESOURCE_LOCATION.requiredField("level_formula", AreaEffectFormulaConfig::levelFormula),
      AreaEffectFormulaConfig::new);
  }
}
