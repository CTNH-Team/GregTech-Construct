package slimeknights.tconstruct.library.modifiers.hook.armor;

import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;

public interface PlayerLoginModifierHook {
  default void onPlayerLogin(IToolStackView tool, ModifierEntry modifier, Player player) {}

  record AllMerger(Collection<PlayerLoginModifierHook> modules) implements PlayerLoginModifierHook {
    @Override
    public void onPlayerLogin(IToolStackView tool, ModifierEntry modifier, Player player) {
      for (PlayerLoginModifierHook module : modules) {
        module.onPlayerLogin(tool, modifier, player);
      }
    }
  }
}
