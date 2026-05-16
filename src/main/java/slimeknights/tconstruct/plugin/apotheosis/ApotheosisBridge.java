package slimeknights.tconstruct.plugin.apotheosis;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** No-op bridge around optional Apotheosis socket functionality. */
public final class ApotheosisBridge {
  private ApotheosisBridge() {}

  public record SocketGem(int rawSocketIndex, ItemStack gem) {
    public static SocketGem empty(int rawSocketIndex) {
      return new SocketGem(rawSocketIndex, ItemStack.EMPTY);
    }

    public boolean isEmpty() {
      return this.gem.isEmpty();
    }

    public boolean isFilled() {
      return !isEmpty();
    }
  }

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
      public int getSocketCount(ItemStack stack) {
        return 0;
      }

      @Override
      public List<SocketGem> getSocketedGemData(ItemStack stack) {
        return Collections.emptyList();
      }

      @Override
      public void appendTooltip(ItemStack stack, Consumer<Component> consumer) {}

      @Override
      public void addAttributeModifiers(ItemStack stack, EquipmentSlot slot, BiConsumer<Attribute,AttributeModifier> consumer) {}

      @Override
      public ItemStack removeGem(ItemStack stack, int socketIndex) {
        return ItemStack.EMPTY;
      }

      @Override
      public boolean canInsertGem(ItemStack tool, int rawSocketIndex, ItemStack gem) {
        return false;
      }

      @Override
      public ItemStack insertGem(ItemStack tool, int rawSocketIndex, ItemStack gem) {
        return ItemStack.EMPTY;
      }

      @Override
      public ItemStack copyGem(ItemStack stack, int socketIndex) {
        return ItemStack.EMPTY;
      }
    };

    boolean hasSocketedGems(ItemStack stack);

    default List<ItemStack> getSocketedGems(ItemStack stack) {
      return getSocketedGemData(stack).stream().filter(SocketGem::isFilled).map(SocketGem::gem).toList();
    }

    int getSocketCount(ItemStack stack);

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

    default void addAttributeModifiers(ItemStack stack, EquipmentSlot slot, BiConsumer<Attribute,AttributeModifier> consumer) {}

    ItemStack removeGem(ItemStack stack, int socketIndex);

    boolean canInsertGem(ItemStack tool, int rawSocketIndex, ItemStack gem);

    ItemStack insertGem(ItemStack tool, int rawSocketIndex, ItemStack gem);

    ItemStack copyGem(ItemStack stack, int socketIndex);
  }

  private static SocketHooks socketHooks = SocketHooks.EMPTY;

  public static boolean hasSocketIntegration() {
    return socketHooks != SocketHooks.EMPTY;
  }

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
