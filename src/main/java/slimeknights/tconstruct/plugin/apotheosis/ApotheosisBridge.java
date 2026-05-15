package slimeknights.tconstruct.plugin.apotheosis;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** No-op bridge around optional Apotheosis socket functionality. */
public final class ApotheosisBridge {
  private ApotheosisBridge() {}

  public interface SocketHooks {
    SocketHooks EMPTY = new SocketHooks() {
      @Override
      public boolean hasSocketedGems(ItemStack stack) {
        return false;
      }

      @Override
      public List<ItemStack> getSocketedGems(ItemStack stack) {
        return Collections.emptyList();
      }

      @Override
      public void appendTooltip(ItemStack stack, Consumer<Component> consumer) {}

      @Override
      public ItemStack removeGem(ItemStack stack, int socketIndex) {
        return ItemStack.EMPTY;
      }

      @Override
      public ItemStack copyGem(ItemStack stack, int socketIndex) {
        return ItemStack.EMPTY;
      }
    };

    boolean hasSocketedGems(ItemStack stack);

    List<ItemStack> getSocketedGems(ItemStack stack);

    void appendTooltip(ItemStack stack, Consumer<Component> consumer);

    ItemStack removeGem(ItemStack stack, int socketIndex);

    ItemStack copyGem(ItemStack stack, int socketIndex);
  }

  private static SocketHooks socketHooks = SocketHooks.EMPTY;

  public static SocketHooks sockets() {
    return socketHooks;
  }

  public static void setSocketHooks(SocketHooks hooks) {
    socketHooks = Objects.requireNonNullElse(hooks, SocketHooks.EMPTY);
  }
}
