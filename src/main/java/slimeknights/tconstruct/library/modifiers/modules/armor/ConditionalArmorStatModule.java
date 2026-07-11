package slimeknights.tconstruct.library.modifiers.modules.armor;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.tconstruct.library.json.LevelingValue;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStat;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStats;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModuleBuilder;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public record ConditionalArmorStatModule(ArmorDamageStat stat, IJsonPredicate<DamageSource> source, LevelingValue amount, LevelingValue highest, ModifierCondition<IToolStackView> condition)
  implements ModifierModule, ArmorDamageStatsModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ConditionalArmorStatModule>defaultHooks(ModifierHooks.ARMOR_DAMAGE_STATS);
  public static final RecordLoadable<ConditionalArmorStatModule> LOADER = RecordLoadable.create(
    new EnumLoadable<>(ArmorDamageStat.class).requiredField("stat", ConditionalArmorStatModule::stat),
    DamageSourcePredicate.LOADER.defaultField("damage_source", DamageSourcePredicate.ANY, ConditionalArmorStatModule::source),
    LevelingValue.LOADABLE.defaultField("amount", LevelingValue.ZERO, ConditionalArmorStatModule::amount),
    LevelingValue.LOADABLE.defaultField("highest", LevelingValue.ZERO, ConditionalArmorStatModule::highest),
    ModifierCondition.TOOL_FIELD,
    ConditionalArmorStatModule::new);

  @Internal
  public ConditionalArmorStatModule {}

  @Override
  public void addArmorDamageStats(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, ArmorDamageStats stats) {
    if (condition.matches(tool, modifier) && this.source.matches(source)) {
      float level = modifier.getEffectiveLevel();
      stats.add(stat, amount.compute(level));
      stats.addHighest(stat, highest.compute(level));
    }
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<ConditionalArmorStatModule> getLoader() {
    return LOADER;
  }

  public static Builder stat(ArmorDamageStat stat) {
    return new Builder(stat);
  }

  public static class Builder extends ModuleBuilder.Stack<Builder> {
    private final ArmorDamageStat stat;
    @Setter
    @Accessors(fluent = true)
    private IJsonPredicate<DamageSource> source = DamageSourcePredicate.ANY;
    private LevelingValue amount = LevelingValue.ZERO;
    private LevelingValue highest = LevelingValue.ZERO;

    private Builder(ArmorDamageStat stat) {
      this.stat = stat;
    }

    @SafeVarargs
    public final Builder sources(IJsonPredicate<DamageSource>... sources) {
      return source(DamageSourcePredicate.and(sources));
    }

    public Builder amount(float flat, float eachLevel) {
      this.amount = new LevelingValue(flat, eachLevel);
      return this;
    }

    public Builder eachLevel(float eachLevel) {
      return amount(0, eachLevel);
    }

    public Builder highest(float flat, float eachLevel) {
      this.highest = new LevelingValue(flat, eachLevel);
      return this;
    }

    public Builder highestEachLevel(float eachLevel) {
      return highest(0, eachLevel);
    }

    public ConditionalArmorStatModule build() {
      return new ConditionalArmorStatModule(stat, source, amount, highest, condition);
    }
  }
}
