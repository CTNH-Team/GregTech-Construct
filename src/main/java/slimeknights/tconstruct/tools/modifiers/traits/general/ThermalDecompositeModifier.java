package slimeknights.tconstruct.tools.modifiers.traits.general;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class ThermalDecompositeModifier extends Modifier implements ModifyDamageModifierHook {
    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MODIFY_HURT);
    }

    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context,
                                   EquipmentSlot slotType, DamageSource source,
                                   float amount, boolean isDirectDamage) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            LivingEntity target = context.getEntity();
            int damageAmount = modifier.getLevel() * 20;
            int duration = modifier.getLevel() * 100;

            target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, modifier.getLevel() - 1, false, false, true));
            tool.setDamage(tool.getDamage() + damageAmount);
        }
        return amount;
    }
}
