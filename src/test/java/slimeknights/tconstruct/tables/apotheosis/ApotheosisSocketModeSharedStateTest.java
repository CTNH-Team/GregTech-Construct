package slimeknights.tconstruct.tables.apotheosis;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApotheosisSocketModeSharedStateTest extends BaseMcTest {
  private static final ItemStack TOOL = new ItemStack(Items.DIAMOND_PICKAXE);
  private static final ItemStack FILLED_GEM = new ItemStack(Items.EMERALD);
  private static final ItemStack INSERT_GEM = new ItemStack(Items.DIAMOND);

  @AfterEach
  void resetBridge() {
    ApotheosisBridge.resetSocketHooks();
  }

  @Test
  void buttonStateReflectsSocketAvailabilityAndOverflow() {
    ApotheosisBridge.installSocketHooks(new FakeHooks(0, List.of(), false));
    assertThat(ApotheosisSocketMode.buttonState(TOOL, 6).visible()).isTrue();
    assertThat(ApotheosisSocketMode.buttonState(TOOL, 6).enabled()).isFalse();
    assertThat(ApotheosisSocketMode.buttonState(TOOL, 6).reasonKey()).isEqualTo("no_sockets");

    ApotheosisBridge.installSocketHooks(new FakeHooks(6, List.of(), false));
    assertThat(ApotheosisSocketMode.buttonState(TOOL, 6).enabled()).isFalse();
    assertThat(ApotheosisSocketMode.buttonState(TOOL, 6).reasonKey()).isEqualTo("too_many_sockets");

    ApotheosisBridge.installSocketHooks(new FakeHooks(2, List.of(), false));
    assertThat(ApotheosisSocketMode.buttonState(TOOL, 6).enabled()).isTrue();
    assertThat(ApotheosisSocketMode.buttonState(TOOL, 6).reasonKey()).isEmpty();
  }

  @Test
  void visibleSocketViewMatchesSocketCountUpToFive() {
    ApotheosisBridge.installSocketHooks(new FakeHooks(3, List.of(
      new ApotheosisBridge.SocketGem(0, FILLED_GEM),
      ApotheosisBridge.SocketGem.empty(1),
      ApotheosisBridge.SocketGem.empty(2)
    ), true));

    List<ApotheosisBridge.SocketGem> sockets = ApotheosisSocketMode.getVisibleSockets(TOOL);

    assertThat(sockets).hasSize(3);
    assertThat(sockets.get(0).isFilled()).isTrue();
    assertThat(sockets.get(1).isFilled()).isFalse();
    assertThat(sockets.get(2).isFilled()).isFalse();
  }

  @Test
  void insertingAndRemovingGemMutatesPreviewImmediately() {
    FakeHooks hooks = new FakeHooks(2, List.of(
      ApotheosisBridge.SocketGem.empty(0),
      new ApotheosisBridge.SocketGem(1, FILLED_GEM)
    ), true);
    ApotheosisBridge.installSocketHooks(hooks);

    ItemStack insertedPreview = ApotheosisSocketMode.insertGem(TOOL, 0, INSERT_GEM);
    ItemStack removedPreview = ApotheosisSocketMode.removeGem(TOOL, 1);

    assertThat(insertedPreview.getItem()).isEqualTo(Items.NETHERITE_PICKAXE);
    assertThat(removedPreview.getItem()).isEqualTo(Items.IRON_PICKAXE);
    assertThat(hooks.insertCalls).containsExactly(0);
    assertThat(hooks.removeCalls).containsExactly(1);
  }

  private static final class FakeHooks implements ApotheosisBridge.SocketHooks {
    private final int socketCount;
    private final List<ApotheosisBridge.SocketGem> visibleSockets;
    private final boolean acceptsInsert;
    private final List<Integer> insertCalls = new java.util.ArrayList<>();
    private final List<Integer> removeCalls = new java.util.ArrayList<>();

    private FakeHooks(int socketCount, List<ApotheosisBridge.SocketGem> visibleSockets, boolean acceptsInsert) {
      this.socketCount = socketCount;
      this.visibleSockets = visibleSockets;
      this.acceptsInsert = acceptsInsert;
    }

    @Override
    public int getSocketCount(ItemStack stack) {
      return socketCount;
    }

    @Override
    public List<ApotheosisBridge.SocketGem> getSocketedGemData(ItemStack stack) {
      return visibleSockets;
    }

    @Override
    public boolean hasSocketedGems(ItemStack stack) {
      return visibleSockets.stream().anyMatch(ApotheosisBridge.SocketGem::isFilled);
    }

    @Override
    public boolean canInsertGem(ItemStack tool, int rawSocketIndex, ItemStack gem) {
      return acceptsInsert && rawSocketIndex == 0 && gem.getItem() == Items.DIAMOND;
    }

    @Override
    public ItemStack insertGem(ItemStack tool, int rawSocketIndex, ItemStack gem) {
      insertCalls.add(rawSocketIndex);
      return new ItemStack(Items.NETHERITE_PICKAXE);
    }

    @Override
    public ItemStack removeGem(ItemStack tool, int rawSocketIndex) {
      removeCalls.add(rawSocketIndex);
      return new ItemStack(Items.IRON_PICKAXE);
    }

    @Override
    public ItemStack copyGem(ItemStack stack, int socketIndex) {
      return ItemStack.EMPTY;
    }

    @Override
    public void appendTooltip(ItemStack stack, java.util.function.Consumer<Component> consumer) {}
  }
}
