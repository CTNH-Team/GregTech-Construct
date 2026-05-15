package slimeknights.tconstruct.tables.client.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TinkerStationExtractionViewStateTest extends BaseMcTest {
  @AfterEach
  void resetBridge() {
    ApotheosisBridge.resetSocketHooks();
  }

  @Test
  void hidesExtractionControlsWhenExtractionIsUnavailable() {
    TinkerStationExtractionViewState state = TinkerStationExtractionViewState.create(ItemStack.EMPTY, ItemStack.EMPTY, 4, true, 0);

    assertThat(state.visible()).isFalse();
    assertThat(state.extractionMode()).isFalse();
    assertThat(state.selectedSocket()).isEqualTo(-1);
    assertThat(state.gems()).isEmpty();
  }

  @Test
  void normalizesSelectionAgainstDisplayedSockets() {
    ItemStack firstGem = new ItemStack(Items.EMERALD);
    ItemStack secondGem = new ItemStack(Items.DIAMOND);
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(
      new ApotheosisBridge.SocketGem(0, firstGem),
      new ApotheosisBridge.SocketGem(3, secondGem)
    )));

    TinkerStationExtractionViewState state = TinkerStationExtractionViewState.create(new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.IRON_PICKAXE), 6, true, 1);

    assertThat(state.visible()).isTrue();
    assertThat(state.extractionMode()).isTrue();
    assertThat(state.selectedSocket()).isEqualTo(1);
    assertThat(state.gems()).containsExactly(firstGem, secondGem);
  }

  @Test
  void disablesExtractionModeWhenSelectedSocketFallsOutOfRange() {
    ItemStack gem = new ItemStack(Items.EMERALD);
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(new ApotheosisBridge.SocketGem(2, gem))));

    TinkerStationExtractionViewState state = TinkerStationExtractionViewState.create(new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.IRON_PICKAXE), 6, true, 5);

    assertThat(state.visible()).isTrue();
    assertThat(state.extractionMode()).isFalse();
    assertThat(state.selectedSocket()).isEqualTo(-1);
    assertThat(state.gems()).containsExactly(gem);
  }

  @Test
  void hidesActiveExtractionStateWhenDisplayedResultIsNotAnExtractionResult() {
    ItemStack gem = new ItemStack(Items.EMERALD);
    ItemStack rawTool = new ItemStack(Items.DIAMOND_PICKAXE);
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(new ApotheosisBridge.SocketGem(0, gem))));

    TinkerStationExtractionViewState state = TinkerStationExtractionViewState.create(rawTool, rawTool.copy(), 6, true, 0);

    assertThat(state.visible()).isTrue();
    assertThat(state.extractionMode()).isFalse();
    assertThat(state.selectedSocket()).isEqualTo(-1);
    assertThat(state.gems()).containsExactly(gem);
  }

  @Test
  void keepsControlsVisibleWhenToolHasOnlyEmptySockets() {
    ItemStack rawTool = new ItemStack(Items.DIAMOND_PICKAXE);
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(
      ApotheosisBridge.SocketGem.empty(0),
      ApotheosisBridge.SocketGem.empty(1)
    )));

    TinkerStationExtractionViewState state = TinkerStationExtractionViewState.create(rawTool, rawTool.copy(), 6, false, -1);

    assertThat(state.visible()).isTrue();
    assertThat(state.extractionMode()).isFalse();
    assertThat(state.selectedSocket()).isEqualTo(-1);
    assertThat(state.gems()).isEmpty();
  }

  private record FakeSocketHooks(List<ApotheosisBridge.SocketGem> gems) implements ApotheosisBridge.SocketHooks {
    @Override
    public boolean hasSocketedGems(ItemStack stack) {
      return !gems.isEmpty();
    }

    @Override
    public int getSocketCount(ItemStack stack) {
      return gems.size();
    }

    @Override
    public List<ItemStack> getSocketedGems(ItemStack stack) {
      return gems.stream().map(ApotheosisBridge.SocketGem::gem).toList();
    }

    @Override
    public List<ApotheosisBridge.SocketGem> getSocketedGemData(ItemStack stack) {
      return gems;
    }

    @Override
    public void appendTooltip(ItemStack stack, java.util.function.Consumer<net.minecraft.network.chat.Component> consumer) {}

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
  }
}
