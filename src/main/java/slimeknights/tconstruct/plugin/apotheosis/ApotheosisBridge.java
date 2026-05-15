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

  public record SocketGem(int rawSocketIndex, ItemStack gem) {}

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
      public List<SocketGem> getSocketedGemData(ItemStack stack) {
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

    default List<SocketGem> getSocketedGemData(ItemStack stack) {
      List<ItemStack> gems = getSocketedGems(stack);
      if (gems.isEmpty()) {
        return Collections.emptyList();
      }
      return java.util.stream.IntStream.range(0, gems.size())
                                       .mapToObj(index -> new SocketGem(index, gems.get(index)))
                                       .toList();
    }

    void appendTooltip(ItemStack stack, Consumer<Component> consumer);

    ItemStack removeGem(ItemStack stack, int socketIndex);

    ItemStack copyGem(ItemStack stack, int socketIndex);
  }

  private static SocketHooks socketHooks = SocketHooks.EMPTY;

  public static SocketHooks sockets() {
    return socketHooks;
  }

  public static void installSocketHooks(SocketHooks hooks) {
    socketHooks = Objects.requireNonNullElse(hooks, SocketHooks.EMPTY);
  }

  public static void resetSocketHooks() {
    socketHooks = SocketHooks.EMPTY;
  }
}
