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
    ApotheosisBridge.resetSocketHooks();
  }

  @Test
  void socketGemEmptyHelperMatchesFilledState() {
    ApotheosisBridge.SocketGem empty = ApotheosisBridge.SocketGem.empty(2);
    ApotheosisBridge.SocketGem filled = new ApotheosisBridge.SocketGem(1, new ItemStack(Items.EMERALD));

    assertThat(empty.isEmpty()).isTrue();
    assertThat(empty.isFilled()).isFalse();
    assertThat(filled.isEmpty()).isFalse();
    assertThat(filled.isFilled()).isTrue();
  }

  @Test
  void emptyBridgeIsSafeNoOp() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    List<Component> tooltip = new ArrayList<>();

    assertThat(ApotheosisBridge.sockets().hasSocketedGems(tool)).isFalse();
    assertThat(ApotheosisBridge.sockets().getSocketCount(tool)).isZero();
    assertThat(ApotheosisBridge.sockets().getSocketedGems(tool)).isEmpty();
    ApotheosisBridge.sockets().appendTooltip(tool, tooltip::add);
    assertThat(tooltip).isEmpty();
    assertThat(ApotheosisBridge.sockets().canInsertGem(tool, 0, new ItemStack(Items.EMERALD))).isFalse();
    assertThat(ApotheosisBridge.sockets().insertGem(tool, 0, new ItemStack(Items.EMERALD)).isEmpty()).isTrue();
    assertThat(ApotheosisBridge.sockets().removeGem(tool, 0).isEmpty()).isTrue();
    assertThat(ApotheosisBridge.sockets().copyGem(tool, 0).isEmpty()).isTrue();
  }

  @Test
  void installedBridgeIsUsedImmediately() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ItemStack gem = new ItemStack(Items.EMERALD);

    ApotheosisBridge.installSocketHooks(new ApotheosisBridge.SocketHooks() {
      @Override
      public boolean hasSocketedGems(ItemStack stack) {
        return true;
      }

      @Override
      public int getSocketCount(ItemStack stack) {
        return 1;
      }

      @Override
      public List<ApotheosisBridge.SocketGem> getSocketedGemData(ItemStack stack) {
        return List.of(new ApotheosisBridge.SocketGem(0, gem));
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
      public boolean canInsertGem(ItemStack tool, int rawSocketIndex, ItemStack gemStack) {
        return rawSocketIndex == 0;
      }

      @Override
      public ItemStack insertGem(ItemStack tool, int rawSocketIndex, ItemStack gemStack) {
        return tool.copy();
      }

      @Override
      public ItemStack copyGem(ItemStack stack, int socketIndex) {
        return gem.copy();
      }
    });

    List<Component> tooltip = new ArrayList<>();
    ApotheosisBridge.sockets().appendTooltip(tool, tooltip::add);

    assertThat(ApotheosisBridge.sockets().hasSocketedGems(tool)).isTrue();
    assertThat(ApotheosisBridge.sockets().getSocketCount(tool)).isEqualTo(1);
    assertThat(ApotheosisBridge.sockets().getSocketedGems(tool)).hasSize(1);
    assertThat(tooltip).extracting(Component::getString).containsExactly("Test Gem Bonus");
    assertThat(ApotheosisBridge.sockets().canInsertGem(tool, 0, gem)).isTrue();
    assertThat(ApotheosisBridge.sockets().copyGem(tool, 0).getItem()).isEqualTo(Items.EMERALD);
  }
}
