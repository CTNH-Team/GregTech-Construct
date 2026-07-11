package slimeknights.tconstruct.library.modifiers.hook.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;

public interface AroundEntityTickModifierHook {
  default void onAroundEntityTickStart(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, Player player, LivingEntity target, double distance) {}
  default void onAroundEntityTickEnd(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, Player player, LivingEntity target, double distance) {}

  record AllMerger(Collection<AroundEntityTickModifierHook> modules) implements AroundEntityTickModifierHook {
    @Override
    public void onAroundEntityTickStart(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, Player player, LivingEntity target, double distance) {
      for (AroundEntityTickModifierHook module : modules) {
        module.onAroundEntityTickStart(tool, modifier, slotType, player, target, distance);
      }
    }

    @Override
    public void onAroundEntityTickEnd(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, Player player, LivingEntity target, double distance) {
      for (AroundEntityTickModifierHook module : modules) {
        module.onAroundEntityTickEnd(tool, modifier, slotType, player, target, distance);
      }
    }
  }
}
