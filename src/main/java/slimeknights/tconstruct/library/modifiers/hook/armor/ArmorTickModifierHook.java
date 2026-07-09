package slimeknights.tconstruct.library.modifiers.hook.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;

public interface ArmorTickModifierHook {
  default void onArmorTick(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, LivingEntity entity) {}

  record AllMerger(Collection<ArmorTickModifierHook> modules) implements ArmorTickModifierHook {
    @Override
    public void onArmorTick(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, LivingEntity entity) {
      for (ArmorTickModifierHook module : modules) {
        module.onArmorTick(tool, modifier, slotType, entity);
      }
    }
  }
}
