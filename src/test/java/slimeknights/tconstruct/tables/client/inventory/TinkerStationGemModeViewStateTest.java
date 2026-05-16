package slimeknights.tconstruct.tables.client.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TinkerStationGemModeViewStateTest extends BaseMcTest {
  @AfterEach
  void resetBridge() {
    ApotheosisBridge.resetSocketHooks();
  }

  @Test
  void noSocketsKeepsGemModeButtonVisibleButDisabled() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(0, List.of()));

    TinkerStationGemModeViewState state = TinkerStationGemModeViewState.create(tool, 6, false);

    assertThat(state.buttonVisible()).isTrue();
    assertThat(state.buttonEnabled()).isFalse();
    assertThat(state.buttonTooltipKey()).isEqualTo("gui.tconstruct.tinker_station.gem_mode.no_sockets");
    assertThat(state.gemModeActive()).isFalse();
    assertThat(state.socketSlotsVisible()).isFalse();
    assertThat(state.outputLocked()).isFalse();
    assertThat(state.visibleSockets()).isEmpty();
  }

  @Test
  void missingApotheosisHidesGemModeButtonEntirely() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ApotheosisBridge.resetSocketHooks();

    TinkerStationGemModeViewState state = TinkerStationGemModeViewState.create(tool, 6, false);

    assertThat(state.buttonVisible()).isFalse();
    assertThat(state.buttonEnabled()).isFalse();
    assertThat(state.buttonTooltipKey()).isEmpty();
    assertThat(state.visibleSockets()).isEmpty();
  }

  @Test
  void tooManySocketsShowsOverflowDisabledState() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(6, List.of(
      ApotheosisBridge.SocketGem.empty(0),
      ApotheosisBridge.SocketGem.empty(1),
      ApotheosisBridge.SocketGem.empty(2),
      ApotheosisBridge.SocketGem.empty(3),
      ApotheosisBridge.SocketGem.empty(4),
      ApotheosisBridge.SocketGem.empty(5)
    )));

    TinkerStationGemModeViewState state = TinkerStationGemModeViewState.create(tool, 6, false);

    assertThat(state.buttonVisible()).isTrue();
    assertThat(state.buttonEnabled()).isFalse();
    assertThat(state.buttonTooltipKey()).isEqualTo("gui.tconstruct.tinker_station.gem_mode.too_many_sockets");
    assertThat(state.socketSlotsVisible()).isFalse();
    assertThat(state.visibleSockets()).hasSize(5);
  }

  @Test
  void sharedGemModeStateIsReflectedInTheClientViewModel() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ItemStack gem = new ItemStack(Items.EMERALD);
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(2, List.of(
      new ApotheosisBridge.SocketGem(0, gem),
      ApotheosisBridge.SocketGem.empty(1)
    )));

    TinkerStationGemModeViewState state = TinkerStationGemModeViewState.create(tool, 6, true);

    assertThat(state.buttonVisible()).isTrue();
    assertThat(state.buttonEnabled()).isTrue();
    assertThat(state.buttonTooltipKey()).isEqualTo("gui.tconstruct.tinker_station.gem_mode.help");
    assertThat(state.gemModeActive()).isTrue();
    assertThat(state.socketSlotsVisible()).isTrue();
    assertThat(state.outputLocked()).isTrue();
    assertThat(state.visibleSockets()).containsExactly(
      new ApotheosisBridge.SocketGem(0, gem),
      ApotheosisBridge.SocketGem.empty(1)
    );
  }

  @Test
  void emptySocketToolsCanShowTheSharedGemModeEntryPath() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(2, List.of(
      ApotheosisBridge.SocketGem.empty(0),
      ApotheosisBridge.SocketGem.empty(1)
    )));

    TinkerStationGemModeViewState state = TinkerStationGemModeViewState.create(tool, 6, false);

    assertThat(state.buttonVisible()).isTrue();
    assertThat(state.buttonEnabled()).isTrue();
    assertThat(state.buttonTooltipKey()).isEqualTo("gui.tconstruct.tinker_station.gem_mode.help");
    assertThat(state.gemModeActive()).isFalse();
    assertThat(state.socketSlotsVisible()).isFalse();
    assertThat(state.outputLocked()).isFalse();
    assertThat(state.visibleSockets()).containsExactly(
      ApotheosisBridge.SocketGem.empty(0),
      ApotheosisBridge.SocketGem.empty(1)
    );
  }

  @Test
  void smallInputStationsStillShowTheGemModeEntryPathWhenAToolIsPresent() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(2, List.of(
      ApotheosisBridge.SocketGem.empty(0),
      ApotheosisBridge.SocketGem.empty(1)
    )));

    TinkerStationGemModeViewState state = TinkerStationGemModeViewState.create(tool, 4, false);

    assertThat(state.buttonVisible()).isFalse();
    assertThat(state.buttonEnabled()).isFalse();
    assertThat(state.buttonTooltipKey()).isEmpty();
    assertThat(state.gemModeActive()).isFalse();
    assertThat(state.socketSlotsVisible()).isFalse();
    assertThat(state.outputLocked()).isFalse();
    assertThat(state.visibleSockets()).containsExactly(
      ApotheosisBridge.SocketGem.empty(0),
      ApotheosisBridge.SocketGem.empty(1)
    );
  }

  @Test
  void activeGemModeWithUnsupportedToolRequestsRecoveryExit() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(0, List.of()));

    TinkerStationGemModeResolution resolution = TinkerStationGemModeResolution.create(tool, 6, true);

    assertThat(resolution.shouldExitGemMode()).isTrue();
    assertThat(resolution.viewState().buttonVisible()).isTrue();
    assertThat(resolution.viewState().buttonEnabled()).isFalse();
    assertThat(resolution.viewState().buttonTooltipKey()).isEqualTo("gui.tconstruct.tinker_station.gem_mode.no_sockets");
    assertThat(resolution.viewState().gemModeActive()).isFalse();
    assertThat(resolution.viewState().socketSlotsVisible()).isFalse();
    assertThat(resolution.viewState().outputLocked()).isFalse();
  }

  @Test
  void recoveryResolutionAppliesPostExitUiStateImmediately() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(0, List.of()));
    TinkerStationGemModeViewState staleGemModeView = new TinkerStationGemModeViewState(
      true,
      true,
      "gui.tconstruct.tinker_station.gem_mode.help",
      true,
      true,
      true,
      List.of(
        ApotheosisBridge.SocketGem.empty(0),
        ApotheosisBridge.SocketGem.empty(1)
      )
    );
    TinkerStationGemModeResolution resolution = TinkerStationGemModeResolution.create(tool, 6, true);

    TinkerStationGemModeScreenState applied = TinkerStationGemModeScreenState.create(staleGemModeView, resolution.viewState(), 4, 6);

    assertThat(applied.viewState()).isEqualTo(resolution.viewState());
    assertThat(applied.toggleVisible()).isTrue();
    assertThat(applied.toggleActive()).isFalse();
    assertThat(applied.displayedInputCount()).isEqualTo(4);
    assertThat(applied.shouldUpdateLayout()).isTrue();
  }

  @Test
  void socketContentRefreshDoesNotRequestAnotherLayoutPassWhenVisibleLayoutIsUnchanged() {
    ItemStack firstGem = new ItemStack(Items.EMERALD);
    ItemStack refreshedGem = new ItemStack(Items.DIAMOND);
    TinkerStationGemModeViewState currentViewState = new TinkerStationGemModeViewState(
      true,
      true,
      "gui.tconstruct.tinker_station.gem_mode.help",
      true,
      true,
      true,
      List.of(
        new ApotheosisBridge.SocketGem(0, firstGem),
        ApotheosisBridge.SocketGem.empty(1)
      )
    );
    TinkerStationGemModeViewState refreshedViewState = new TinkerStationGemModeViewState(
      true,
      true,
      "gui.tconstruct.tinker_station.gem_mode.help",
      true,
      true,
      true,
      List.of(
        new ApotheosisBridge.SocketGem(0, refreshedGem),
        ApotheosisBridge.SocketGem.empty(1)
      )
    );

    TinkerStationGemModeScreenState applied = TinkerStationGemModeScreenState.create(currentViewState, refreshedViewState, 4, 6);

    assertThat(applied.viewState()).isEqualTo(refreshedViewState);
    assertThat(applied.displayedInputCount()).isEqualTo(2);
    assertThat(applied.shouldUpdateLayout()).isFalse();
    assertThat(applied.toggleVisible()).isTrue();
    assertThat(applied.toggleActive()).isTrue();
  }

  private record FakeSocketHooks(int socketCount, List<ApotheosisBridge.SocketGem> gems) implements ApotheosisBridge.SocketHooks {
    @Override
    public boolean hasSocketedGems(ItemStack stack) {
      return gems.stream().anyMatch(ApotheosisBridge.SocketGem::isFilled);
    }

    @Override
    public int getSocketCount(ItemStack stack) {
      return socketCount;
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
