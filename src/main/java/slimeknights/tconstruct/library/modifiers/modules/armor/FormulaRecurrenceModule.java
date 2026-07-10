package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStat;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageToPersistentModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.tools.logic.PlayerPersistentDataCache;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record FormulaRecurrenceModule(ResourceLocation persistentArmorStatFormula, ResourceLocation damageToPersistentFormula,
                                      ResourceLocation persistentTickFormula, ResourceLocation key,
                                      int intervalTicks, ModifierCondition<IToolStackView> condition)
  implements ModifierModule, ArmorDamageStatsModifierHook, DamageToPersistentModifierHook, ArmorTickModifierHook, TooltipModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FormulaRecurrenceModule>defaultHooks(ModifierHooks.ARMOR_DAMAGE_STATS, ModifierHooks.DAMAGE_TO_PERSISTENT, ModifierHooks.ARMOR_TICK, ModifierHooks.TOOLTIP);
  private static final IJsonPredicate<DamageSource> RECURRENCE_DAMAGE_SOURCES = DamageSourcePredicate.or(
    DamageSourcePredicate.and(
      DamageSourcePredicate.tag(TinkerTags.DamageTypes.MELEE_PROTECTION),
      DamageSourcePredicate.IS_INDIRECT.inverted()),
    DamageSourcePredicate.tag(TinkerTags.DamageTypes.PROJECTILE_PROTECTION),
    DamageSourcePredicate.tag(TinkerTags.DamageTypes.BLAST_PROTECTION),
    DamageSourcePredicate.tag(TinkerTags.DamageTypes.FIRE_PROTECTION),
    DamageSourcePredicate.tag(TinkerTags.DamageTypes.MAGIC_PROTECTION));
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

  private static final ThreadLocal<Deque<DamageState>> DAMAGE_STATES = ThreadLocal.withInitial(ArrayDeque::new);
  private static final Map<Integer,TickState> TICK_STATES = new HashMap<>();
  private static Function<LivingEntity,ModDataNBT> persistentDataGetter = PersistentDataCapability::getOrWarn;

  static void setPersistentDataGetter(Function<LivingEntity,ModDataNBT> getter) {
    persistentDataGetter = getter;
  }

  static void resetPersistentDataGetter() {
    persistentDataGetter = PersistentDataCapability::getOrWarn;
  }

  public static void beginDamageEvent(LivingEntity entity) {
    DAMAGE_STATES.get().push(new DamageState(entity));
  }

  public static void finishDamageEvent(ArmorDamageStats stats) {
    finishDamageEvent(stats, true);
  }

  public static void finishBypassDamageEvent(ArmorDamageStats stats) {
    finishDamageEvent(stats, false);
  }

  private static void finishDamageEvent(ArmorDamageStats stats, boolean applyPersistentArmorStat) {
    Deque<DamageState> states = DAMAGE_STATES.get();
    if (states.isEmpty()) {
      return;
    }
    DamageState state = states.pop();
    if (states.isEmpty()) {
      DAMAGE_STATES.remove();
    }
    float reduced = Math.max(0, stats.originalDamage() - stats.preReduction());
    for (DamageEntry entry : state.entries.values()) {
      IFormula damageToPersistent = FormulaManager.getOrNull(entry.module.damageToPersistentFormula);
      if (damageToPersistent == null) {
        continue;
      }
      float value = Math.max(0, entry.data.getFloat(entry.module.key));
      if (applyPersistentArmorStat) {
        IFormula persistentArmorStat = FormulaManager.getOrNull(entry.module.persistentArmorStatFormula);
        if (persistentArmorStat == null) {
          continue;
        }
        value = Math.max(0, (float)persistentArmorStat.accept(value, 0, entry.totalLevel, reduced));
      }
      value = Math.max(0, (float)damageToPersistent.accept(value, entry.maxLevel, entry.totalLevel, reduced));
      store(entry.data, entry.module.key, value, state.entity);
    }
  }

  public static void flushArmorTicks(LivingEntity entity) {
    TickState state = TICK_STATES.remove(entity.getId());
    if (state == null || state.entity != entity) {
      return;
    }
    for (TickEntry entry : state.entries.values()) {
      if (!shouldTick(entry.data, entry.module.key, entry.module.intervalTicks, entity.tickCount)) {
        continue;
      }
      IFormula persistentTick = FormulaManager.getOrNull(entry.module.persistentTickFormula);
      if (persistentTick == null) {
        continue;
      }
      float value = Math.max(0, entry.data.getFloat(entry.module.key));
      if (value > 0) {
        value = Math.max(0, (float)persistentTick.accept(value, 0, entry.totalLevel));
        store(entry.data, entry.module.key, value, entity);
      }
    }
  }

  @Override
  public void addArmorDamageStats(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, ArmorDamageStats stats) {
    if (source == null || !RECURRENCE_DAMAGE_SOURCES.matches(source) || !condition.matches(tool, modifier)) {
      return;
    }
    DamageState state = currentDamageState(context);
    if (state == null) {
      return;
    }
    DamageEntry entry = state.entries.computeIfAbsent(this, module -> new DamageEntry(module, persistentDataGetter.apply(state.entity)));
    entry.totalLevel += modifier.getEffectiveLevel();
    entry.maxLevel = Math.max(entry.maxLevel, modifier.getEffectiveLevel());
    if (!entry.applied) {
      float value = Math.max(0, entry.data.getFloat(key));
      if (value > 0) {
        stats.add(ArmorDamageStat.PRE_REDUCTION, value);
      }
      entry.applied = true;
    }
  }

  @Override
  public void onDamageToPersistent(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, ArmorDamageStats stats) {
    if (source == null || !RECURRENCE_DAMAGE_SOURCES.matches(source) || !condition.matches(tool, modifier)) {
      return;
    }
    DamageState state = currentDamageState(context);
    if (state == null) {
      return;
    }
    DamageEntry entry = state.entries.computeIfAbsent(this, module -> new DamageEntry(module, persistentDataGetter.apply(state.entity)));
    if (!entry.applied) {
      entry.totalLevel += modifier.getEffectiveLevel();
      entry.maxLevel = Math.max(entry.maxLevel, modifier.getEffectiveLevel());
    }
  }

  @Override
  public void onArmorTick(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, LivingEntity entity) {
    if (entity.level().isClientSide || !condition.matches(tool, modifier)) {
      return;
    }
    TickState state = TICK_STATES.computeIfAbsent(entity.getId(), ignored -> new TickState(entity));
    if (state.entity != entity) {
      state = new TickState(entity);
      TICK_STATES.put(entity.getId(), state);
    }
    TickEntry entry = state.entries.computeIfAbsent(this, module -> new TickEntry(module, persistentDataGetter.apply(entity)));
    entry.totalLevel += modifier.getEffectiveLevel();
  }

  private static DamageState currentDamageState(EquipmentContext context) {
    if (context == null) {
      return null;
    }
    LivingEntity entity = context.getEntity();
    Deque<DamageState> states = DAMAGE_STATES.get();
    if (states.isEmpty() || states.peek().entity != entity) {
      return null;
    }
    return states.peek();
  }

  private static boolean shouldTick(ModDataNBT data, ResourceLocation key, int intervalTicks, int tickCount) {
    int interval = Math.max(1, intervalTicks);
    if (interval == 1) {
      return true;
    }
    ResourceLocation offsetKey = key.withSuffix("_tick_offset");
    int offset = data.getInt(offsetKey);
    if (offset == 0) {
      offset = TConstruct.RANDOM.nextInt(interval) + 1;
      data.putInt(offsetKey, offset);
    }
    if (offset == interval) {
      offset = 0;
    }
    return tickCount % interval == offset;
  }

  private static void store(ModDataNBT data, ResourceLocation key, float value, LivingEntity holder) {
    if (value < 0.01f) {
      data.remove(key);
    } else {
      data.putFloat(key, value);
    }
    if (holder instanceof Player player && !player.level().isClientSide()) {
      PlayerPersistentDataCache.mark(player.getUUID(), key.toString());
    }
  }

  private static final class DamageState {
    private final LivingEntity entity;
    private final Map<FormulaRecurrenceModule,DamageEntry> entries = new HashMap<>();

    private DamageState(LivingEntity entity) {
      this.entity = entity;
    }
  }

  private static final class DamageEntry {
    private final FormulaRecurrenceModule module;
    private final ModDataNBT data;
    private float totalLevel;
    private float maxLevel;
    private boolean applied;

    private DamageEntry(FormulaRecurrenceModule module, ModDataNBT data) {
      this.module = module;
      this.data = data;
    }
  }

  private static final class TickState {
    private final LivingEntity entity;
    private final Map<FormulaRecurrenceModule,TickEntry> entries = new HashMap<>();

    private TickState(LivingEntity entity) {
      this.entity = entity;
    }
  }

  private static final class TickEntry {
    private final FormulaRecurrenceModule module;
    private final ModDataNBT data;
    private float totalLevel;

    private TickEntry(FormulaRecurrenceModule module, ModDataNBT data) {
      this.module = module;
      this.data = data;
    }
  }

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry modifier, Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    if (player == null || !condition.matches(tool, modifier)) {
      return;
    }
    float storedVal = PlayerPersistentDataCache.get(player.getUUID(), key.toString(), player.level().getGameTime());
    if (storedVal != 0) {
      tooltip.add(modifier.getModifier().applyStyle(Component.translatable(
        "modifier.tconstruct.persistent_armor_stat.accumulated",
        Component.translatable(modifier.getModifier().getTranslationKey() + ".condition"),
        storedVal,
        Component.translatable(modifier.getModifier().getTranslationKey() + ".source"),
        key.getPath()
      )));
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
