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
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class ThermalDecompositeModifier extends Modifier implements OnAttackedModifierHook {
    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context,
                           EquipmentSlot slotType, DamageSource source,
                           float amount, boolean isDirectDamage) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            LivingEntity target = context.getEntity();
            int damageAmount = modifier.getLevel() * 5;

            target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, modifier.getLevel(), false, false, true));
            tool.setDamage(tool.getDamage() + damageAmount);


        }
    }
}
