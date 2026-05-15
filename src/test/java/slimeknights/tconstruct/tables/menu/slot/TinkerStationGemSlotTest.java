package slimeknights.tconstruct.tables.menu.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.library.tools.layout.LayoutSlot;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TinkerStationGemSlotTest extends BaseMcTest {
  private static final ItemStack TOOL = new ItemStack(Items.DIAMOND_PICKAXE);

  @AfterEach
  void resetBridge() {
    ApotheosisBridge.resetSocketHooks();
  }

  @Test
  void emptySocketAcceptsValidGemOnlyInGemMode() {
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(
      List.of(ApotheosisBridge.SocketGem.empty(0)),
      rawSocketIndex -> rawSocketIndex == 0
    ));

    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    LazyResultContainer craftingResult = Mockito.mock(LazyResultContainer.class);
    TinkerStationContainerMenu menu = Mockito.mock(TinkerStationContainerMenu.class);
    LayoutSlot layout = Mockito.mock(LayoutSlot.class);
    TinkerStationSlot slot = new TinkerStationSlot(tile, TinkerStationBlockEntity.INPUT_SLOT, 0, 0);
    slot.setMenu(menu);
    slot.activate(layout);

    when(tile.getCraftingResult()).thenReturn(craftingResult);
    when(tile.getItem(TinkerStationBlockEntity.TINKER_SLOT)).thenReturn(TOOL);
    when(tile.isGemMode()).thenReturn(false, true, true);
    when(menu.isGemMode()).thenReturn(false, true, true);
    when(menu.getVisibleSocketCount()).thenReturn(1);
    when(menu.getCurrentTool()).thenReturn(TOOL);
    when(layout.isValid(any(ItemStack.class))).thenReturn(false);

    assertThat(slot.mayPlace(new ItemStack(Items.DIAMOND))).isFalse();
    assertThat(slot.mayPlace(new ItemStack(Items.DIAMOND))).isTrue();
    assertThat(slot.mayPlace(new ItemStack(Items.EMERALD))).isFalse();
  }

  @Test
  void filledSocketRejectsFurtherInsertions() {
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(
      List.of(new ApotheosisBridge.SocketGem(0, new ItemStack(Items.EMERALD))),
      rawSocketIndex -> true
    ));

    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    LazyResultContainer craftingResult = Mockito.mock(LazyResultContainer.class);
    TinkerStationContainerMenu menu = Mockito.mock(TinkerStationContainerMenu.class);
    LayoutSlot layout = Mockito.mock(LayoutSlot.class);
    TinkerStationSlot slot = new TinkerStationSlot(tile, TinkerStationBlockEntity.INPUT_SLOT, 0, 0);
    slot.setMenu(menu);
    slot.activate(layout);

    when(tile.getCraftingResult()).thenReturn(craftingResult);
    when(tile.getItem(TinkerStationBlockEntity.TINKER_SLOT)).thenReturn(TOOL);
    when(tile.isGemMode()).thenReturn(true);
    when(menu.isGemMode()).thenReturn(true);
    when(menu.getVisibleSocketCount()).thenReturn(1);
    when(menu.getCurrentTool()).thenReturn(TOOL);
    when(layout.isValid(any(ItemStack.class))).thenReturn(true);

    assertThat(slot.mayPlace(new ItemStack(Items.DIAMOND))).isFalse();
    assertThat(slot.hasItem()).isTrue();
    assertThat(slot.getItem().getItem()).isEqualTo(Items.EMERALD);
    assertThat(slot.mayPickup(Mockito.mock(Player.class))).isTrue();
  }

  @Test
  void hiddenReusedSocketSlotDoesNotAcceptOrdinaryLayoutItemsInGemMode() {
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(
      List.of(ApotheosisBridge.SocketGem.empty(0)),
      rawSocketIndex -> false
    ));

    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    LazyResultContainer craftingResult = Mockito.mock(LazyResultContainer.class);
    TinkerStationContainerMenu menu = Mockito.mock(TinkerStationContainerMenu.class);
    LayoutSlot layout = Mockito.mock(LayoutSlot.class);
    TinkerStationSlot slot = new TinkerStationSlot(tile, TinkerStationBlockEntity.INPUT_SLOT + 1, 0, 0);
    slot.setMenu(menu);
    slot.activate(layout);

    when(tile.getCraftingResult()).thenReturn(craftingResult);
    when(tile.getItem(TinkerStationBlockEntity.TINKER_SLOT)).thenReturn(TOOL);
    when(tile.isGemMode()).thenReturn(true);
    when(menu.getVisibleSocketCount()).thenReturn(1);
    when(menu.getCurrentTool()).thenReturn(TOOL);
    when(layout.isValid(any(ItemStack.class))).thenReturn(true);

    assertThat(slot.mayPlace(new ItemStack(Items.DIRT))).isFalse();
    assertThat(slot.hasItem()).isFalse();
  }

  private record FakeSocketHooks(List<ApotheosisBridge.SocketGem> sockets, java.util.function.IntPredicate acceptsSocket) implements ApotheosisBridge.SocketHooks {
    @Override
    public boolean hasSocketedGems(ItemStack stack) {
      return sockets.stream().anyMatch(ApotheosisBridge.SocketGem::isFilled);
    }

    @Override
    public int getSocketCount(ItemStack stack) {
      return sockets.size();
    }

    @Override
    public List<ApotheosisBridge.SocketGem> getSocketedGemData(ItemStack stack) {
      return sockets;
    }

    @Override
    public void appendTooltip(ItemStack stack, java.util.function.Consumer<net.minecraft.network.chat.Component> consumer) {}

    @Override
    public ItemStack removeGem(ItemStack stack, int socketIndex) {
      return ItemStack.EMPTY;
    }

    @Override
    public boolean canInsertGem(ItemStack tool, int rawSocketIndex, ItemStack gem) {
      return gem.getItem() == Items.DIAMOND && acceptsSocket.test(rawSocketIndex);
    }

    @Override
    public ItemStack insertGem(ItemStack tool, int rawSocketIndex, ItemStack gem) {
      return ItemStack.EMPTY;
    }

    @Override
    public ItemStack copyGem(ItemStack stack, int socketIndex) {
      if (socketIndex < 0 || socketIndex >= sockets.size()) {
        return ItemStack.EMPTY;
      }
      return sockets.get(socketIndex).gem().copy();
    }
  }
}
