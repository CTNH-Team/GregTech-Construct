package slimeknights.tconstruct.library.modifiers.hook.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;

public interface SelfTickModifierHook {
  default void onSelfTick(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, LivingEntity entity) {}

  record AllMerger(Collection<SelfTickModifierHook> modules) implements SelfTickModifierHook {
    @Override
    public void onSelfTick(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, LivingEntity entity) {
      for (SelfTickModifierHook module : modules) {
        module.onSelfTick(tool, modifier, slotType, entity);
      }
    }
  }
}
