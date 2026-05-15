package slimeknights.tconstruct.plugin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Installs optional Apotheosis socket hooks when Apotheosis is present. */
public class ApotheosisPlugin {
  public static void onConstruct() {
    ApotheosisBridge.installSocketHooks(new ApotheosisBridge.SocketHooks() {
      @Override
      public boolean hasSocketedGems(ItemStack stack) {
        return SocketHelper.getGems(stack).stream().anyMatch(GemInstance::isValid);
      }

      @Override
      public List<ItemStack> getSocketedGems(ItemStack stack) {
        return SocketHelper.getGems(stack).stream()
                           .filter(GemInstance::isValid)
                           .map(inst -> inst.gemStack().copy())
                           .toList();
      }

      @Override
      public void appendTooltip(ItemStack stack, java.util.function.Consumer<Component> consumer) {
        for (GemInstance gem : SocketHelper.getGems(stack)) {
          if (gem.isValid()) {
            consumer.accept(Component.translatable("text.apotheosis.dot_prefix", gem.getSocketBonusTooltip()).withStyle(ChatFormatting.GOLD));
          }
        }
      }

      @Override
      public ItemStack removeGem(ItemStack stack, int socketIndex) {
        SocketedGems socketed = SocketHelper.getGems(stack);
        if (socketIndex < 0 || socketIndex >= socketed.size() || !socketed.get(socketIndex).isValid()) {
          return ItemStack.EMPTY;
        }
        List<GemInstance> gems = new ArrayList<>(socketed.gems());
        ItemStack result = stack.copy();
        result.setCount(1);
        gems.set(socketIndex, GemInstance.EMPTY);
        SocketHelper.setGems(result, new SocketedGems(gems));
        return result;
      }

      @Override
      public ItemStack copyGem(ItemStack stack, int socketIndex) {
        SocketedGems socketed = SocketHelper.getGems(stack);
        if (socketIndex < 0 || socketIndex >= socketed.size() || !socketed.get(socketIndex).isValid()) {
          return ItemStack.EMPTY;
        }
        ItemStack gem = socketed.get(socketIndex).gemStack().copy();
        gem.removeTagKey(GemItem.UUID_ARRAY);
        return gem;
      }
    });
  }
}
