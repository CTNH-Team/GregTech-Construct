package slimeknights.tconstruct.library.modifiers.hook.armor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;

public interface DamageToPersistentModifierHook {
  default void onDamageToPersistent(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, ArmorDamageStatsModifierHook.ArmorDamageStats stats) {}

  record AllMerger(Collection<DamageToPersistentModifierHook> modules) implements DamageToPersistentModifierHook {
    @Override
    public void onDamageToPersistent(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, ArmorDamageStatsModifierHook.ArmorDamageStats stats) {
      for (DamageToPersistentModifierHook module : modules) {
        module.onDamageToPersistent(tool, modifier, context, slotType, source, stats);
      }
    }
  }
}
