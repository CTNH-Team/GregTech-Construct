package slimeknights.tconstruct.library.modifiers.hook.armor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;

public interface ShareDamageModifierHook {
  boolean collectShareDamage(IToolStackView tool, ModifierEntry modifier, LivingEntity guardian, EquipmentSlot slotType,
                             LivingEntity protectedEntity, DamageSource source, ShareDamageContext context);

  interface ShareDamageContext {
    void add(float shareRatio, float extraProtection, float healthGround, float distanceFactor);
    void add(float shareRatio, float extraProtection, float healthGround, float distanceFactor, int color);
  }

  record AllMerger(Collection<ShareDamageModifierHook> modules) implements ShareDamageModifierHook {
    @Override
    public boolean collectShareDamage(IToolStackView tool, ModifierEntry modifier, LivingEntity guardian, EquipmentSlot slotType,
                                      LivingEntity protectedEntity, DamageSource source, ShareDamageContext context) {
      boolean claimed = false;
      for (ShareDamageModifierHook module : modules) {
        claimed |= module.collectShareDamage(tool, modifier, guardian, slotType, protectedEntity, source, context);
      }
      return claimed;
    }
  }
}
