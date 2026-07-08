package slimeknights.tconstruct.library.modifiers.hook.armor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;

public interface ShareDamageModifierHook {
  float shareDamage(IToolStackView tool, ModifierEntry modifier, LivingEntity guardian, EquipmentSlot slotType, LivingEntity protectedEntity, DamageSource source, float damage);

  record AllMerger(Collection<ShareDamageModifierHook> modules) implements ShareDamageModifierHook {
    @Override
    public float shareDamage(IToolStackView tool, ModifierEntry modifier, LivingEntity guardian, EquipmentSlot slotType, LivingEntity protectedEntity, DamageSource source, float damage) {
      float shared = 0;
      float remaining = damage;
      for (ShareDamageModifierHook module : modules) {
        float amount = module.shareDamage(tool, modifier, guardian, slotType, protectedEntity, source, remaining);
        if (amount > 0) {
          shared += amount;
          remaining -= amount;
          if (remaining <= 0) {
            break;
          }
        }
      }
      return shared;
    }
  }
}
