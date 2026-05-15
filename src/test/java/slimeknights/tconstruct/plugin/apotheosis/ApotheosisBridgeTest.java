package slimeknights.tconstruct.plugin.apotheosis;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApotheosisBridgeTest extends BaseMcTest {
  @AfterEach
  void resetBridge() {
    ApotheosisBridge.setSocketHooks(ApotheosisBridge.SocketHooks.EMPTY);
  }

  @Test
  void emptyBridgeIsSafeNoOp() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    List<Component> tooltip = new ArrayList<>();

    assertThat(ApotheosisBridge.sockets().hasSocketedGems(tool)).isFalse();
    assertThat(ApotheosisBridge.sockets().getSocketedGems(tool)).isEmpty();
    ApotheosisBridge.sockets().appendTooltip(tool, tooltip::add);
    assertThat(tooltip).isEmpty();
    assertThat(ApotheosisBridge.sockets().removeGem(tool, 0).isEmpty()).isTrue();
    assertThat(ApotheosisBridge.sockets().copyGem(tool, 0).isEmpty()).isTrue();
  }

  @Test
  void installedBridgeIsUsedImmediately() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ItemStack gem = new ItemStack(Items.EMERALD);

    ApotheosisBridge.setSocketHooks(new ApotheosisBridge.SocketHooks() {
      @Override
      public boolean hasSocketedGems(ItemStack stack) {
        return true;
      }

      @Override
      public List<ItemStack> getSocketedGems(ItemStack stack) {
        return List.of(gem);
      }

      @Override
      public void appendTooltip(ItemStack stack, java.util.function.Consumer<Component> consumer) {
        consumer.accept(Component.literal("Test Gem Bonus"));
      }

      @Override
      public ItemStack removeGem(ItemStack stack, int socketIndex) {
        return stack.copy();
      }

      @Override
      public ItemStack copyGem(ItemStack stack, int socketIndex) {
        return gem.copy();
      }
    });

    List<Component> tooltip = new ArrayList<>();
    ApotheosisBridge.sockets().appendTooltip(tool, tooltip::add);

    assertThat(ApotheosisBridge.sockets().hasSocketedGems(tool)).isTrue();
    assertThat(ApotheosisBridge.sockets().getSocketedGems(tool)).hasSize(1);
    assertThat(tooltip).extracting(Component::getString).containsExactly("Test Gem Bonus");
    assertThat(ApotheosisBridge.sockets().copyGem(tool, 0).getItem()).isEqualTo(Items.EMERALD);
  }
}
