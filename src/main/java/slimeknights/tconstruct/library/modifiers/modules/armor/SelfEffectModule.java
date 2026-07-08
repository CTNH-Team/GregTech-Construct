package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;
import slimeknights.tconstruct.library.json.LevelingValue;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

/**
 * 给装备者自己添加药水效果的模块。
 * 简化版本，不依赖 TCAE 的 AccumulatorHandler。
 */
public record SelfEffectModule(
    MobEffect effect,
    LevelingValue duration,
    LevelingValue amplifier,
    int intervalTicks,
    IJsonPredicate<LivingEntity> entityFilter,
    ModifierCondition<IToolStackView> condition
) implements ModifierModule, InventoryTickModifierHook, ConditionalModule<IToolStackView> {

    private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<SelfEffectModule>defaultHooks(ModifierHooks.INVENTORY_TICK);

    public static final RecordLoadable<SelfEffectModule> LOADER = RecordLoadable.create(
        Loadables.MOB_EFFECT.requiredField("effect", SelfEffectModule::effect),
        LevelingValue.LOADABLE.requiredField("duration", SelfEffectModule::duration),
        LevelingValue.LOADABLE.requiredField("amplifier", SelfEffectModule::amplifier),
        IntLoadable.FROM_ONE.defaultField("interval_ticks", 20, SelfEffectModule::intervalTicks),
        LivingEntityPredicate.LOADER.defaultField("entity_filter", LivingEntityPredicate.ANY, SelfEffectModule::entityFilter),
        ModifierCondition.TOOL_FIELD,
        SelfEffectModule::new
    );

    @Internal
    public SelfEffectModule {}

    public static SelfEffectModule selfEffect(MobEffect effect, LevelingValue duration, LevelingValue amplifier) {
        return new SelfEffectModule(effect, duration, amplifier, 20, LivingEntityPredicate.ANY, ModifierCondition.ANY_TOOL);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (world.isClientSide || !isCorrectSlot || !condition.matches(tool, modifier)) {
            return;
        }

        // 间隔 tick 检查
        if (holder.tickCount % Math.max(1, intervalTicks) != 0) {
            return;
        }

        // 实体过滤
        if (!entityFilter.matches(holder)) {
            return;
        }

        float level = modifier.getEffectiveLevel();
        int dur = (int) duration.compute(level);
        int amp = (int) amplifier.compute(level);

        if (dur > 0 && amp >= 0) {
            holder.addEffect(new MobEffectInstance(effect, dur, amp, false, false, true));
        }
    }

    @Override
    public List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }

    @Override
    public RecordLoadable<SelfEffectModule> getLoader() {
        return LOADER;
    }
}
