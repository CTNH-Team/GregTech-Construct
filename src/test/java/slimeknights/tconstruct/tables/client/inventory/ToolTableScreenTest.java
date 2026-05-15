package slimeknights.tconstruct.tables.client.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ToolTableScreenTest extends BaseMcTest {
  @AfterEach
  void resetBridge() {
    ApotheosisBridge.resetSocketHooks();
  }

  @Test
  void appendsSocketBonusesToExistingTooltipLines() {
    List<Component> tooltip = new ArrayList<>(List.of(Component.literal("Base line")));

    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(
      Component.literal("+10% speed"),
      Component.literal("+2 luck")
    )));

    ToolTableScreen.appendApotheosisSocketBonuses(new ItemStack(Items.DIAMOND_PICKAXE), tooltip);

    assertThat(tooltip).extracting(Component::getString)
                       .containsExactly("Base line", "", "stat.tconstruct.socket_bonus", "+10% speed", "+2 luck");
  }

  private record FakeSocketHooks(List<Component> tooltip) implements ApotheosisBridge.SocketHooks {
    @Override
    public boolean hasSocketedGems(ItemStack stack) {
      return !tooltip.isEmpty();
    }

    @Override
    public List<ItemStack> getSocketedGems(ItemStack stack) {
      return List.of();
    }

    @Override
    public List<ApotheosisBridge.SocketGem> getSocketedGemData(ItemStack stack) {
      return List.of();
    }

    @Override
    public void appendTooltip(ItemStack stack, java.util.function.Consumer<Component> consumer) {
      tooltip.forEach(consumer);
    }

    @Override
    public ItemStack removeGem(ItemStack stack, int socketIndex) {
      return ItemStack.EMPTY;
    }

    @Override
    public ItemStack copyGem(ItemStack stack, int socketIndex) {
      return ItemStack.EMPTY;
    }
  }
}
