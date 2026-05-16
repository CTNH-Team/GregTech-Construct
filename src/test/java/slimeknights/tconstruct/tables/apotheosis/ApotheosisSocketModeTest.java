package slimeknights.tconstruct.tables.apotheosis;

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

class ApotheosisSocketModeTest extends BaseMcTest {
  @AfterEach
  void resetBridge() {
    ApotheosisBridge.resetSocketHooks();
  }

  @Test
  void buttonHiddenWithoutToolOrRequiredInputs() {
    assertThat(ApotheosisSocketMode.buttonState(ItemStack.EMPTY, 6).visible()).isFalse();
    assertThat(ApotheosisSocketMode.buttonState(new ItemStack(Items.DIAMOND_PICKAXE), 4).visible()).isFalse();
  }

  @Test
  void legacyExtractionWrapperIgnoresGemModeOverflowRule() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ApotheosisBridge.installSocketHooks(FakeSocketHooks.withMappedGems(List.of(
      new ApotheosisBridge.SocketGem(5, new ItemStack(Items.EMERALD))
    ), List.of(), 6));

    assertThat(ApotheosisSocketMode.canExtract(tool, 5)).isTrue();
  }

  @Test
  void legacyExtractionWrapperAllowsEmptySocketsForGemModeEntry() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ApotheosisBridge.installSocketHooks(FakeSocketHooks.withMappedGems(List.of(
      ApotheosisBridge.SocketGem.empty(0),
      ApotheosisBridge.SocketGem.empty(1)
    ), List.of(), 2));

    assertThat(ApotheosisSocketMode.canExtract(tool, 5)).isTrue();
  }

  @Test
  void legacyExtractionWrappersUseFilledSocketBeyondVisibleCap() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ItemStack gem = new ItemStack(Items.EMERALD);
    FakeSocketHooks hooks = FakeSocketHooks.withMappedGems(List.of(
      ApotheosisBridge.SocketGem.empty(0),
      ApotheosisBridge.SocketGem.empty(1),
      ApotheosisBridge.SocketGem.empty(2),
      ApotheosisBridge.SocketGem.empty(3),
      ApotheosisBridge.SocketGem.empty(4),
      new ApotheosisBridge.SocketGem(5, gem)
    ), List.of(), 6);
    ApotheosisBridge.installSocketHooks(hooks);

    assertThat(ApotheosisSocketMode.getDisplayedGems(tool)).containsExactly(gem);
    assertThat(ApotheosisSocketMode.normalizeSelection(tool, 0)).isZero();
    assertThat(ApotheosisSocketMode.normalizeSelection(tool, 1)).isEqualTo(-1);
    assertThat(ApotheosisSocketMode.createResult(tool, 0).getItem()).isEqualTo(Items.IRON_PICKAXE);
    assertThat(ApotheosisSocketMode.createExtractedGem(tool, 0).getItem()).isEqualTo(Items.AMETHYST_SHARD);
    assertThat(hooks.removeCalls).containsExactly(5);
    assertThat(hooks.copyCalls).containsExactly(5);
  }

  @Test
  void visibleSocketsMapRawIndexesForInsertAndRemove() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ItemStack gem = new ItemStack(Items.EMERALD);
    FakeSocketHooks hooks = FakeSocketHooks.withMappedGems(List.of(
      ApotheosisBridge.SocketGem.empty(0),
      new ApotheosisBridge.SocketGem(2, gem)
    ), List.of(), 3);
    ApotheosisBridge.installSocketHooks(hooks);

    assertThat(ApotheosisSocketMode.getVisibleSockets(tool)).hasSize(3);
    assertThat(ApotheosisSocketMode.insertGem(tool, 0, new ItemStack(Items.DIAMOND)).getItem()).isEqualTo(Items.NETHERITE_PICKAXE);
    assertThat(ApotheosisSocketMode.removeGem(tool, 1).getItem()).isEqualTo(Items.IRON_PICKAXE);
    assertThat(hooks.insertCalls).containsExactly(0);
    assertThat(hooks.removeCalls).containsExactly(2);
  }

  @Test
  void tooltipAssemblyAddsHeaderAndBridgeLines() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    List<Component> tooltip = new ArrayList<>();

    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(), List.of(Component.literal("Bonus A"), Component.literal("Bonus B")), 0));

    ApotheosisSocketMode.appendTooltip(tool, tooltip);

    assertThat(tooltip).hasSize(4);
    assertThat(tooltip.get(0).getString()).isEmpty();
    assertThat(tooltip.get(1).getString()).isEqualTo("stat.tconstruct.socket_bonus");
    assertThat(tooltip.subList(2, 4)).extracting(Component::getString).containsExactly("Bonus A", "Bonus B");

    tooltip.clear();
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(), List.of(), 0));
    ApotheosisSocketMode.appendTooltip(tool, tooltip);
    assertThat(tooltip).isEmpty();
  }

  private static class FakeSocketHooks implements ApotheosisBridge.SocketHooks {
    private final List<ApotheosisBridge.SocketGem> gems;
    private final List<Component> tooltip;
    private final int socketCount;
    private final List<Integer> removeCalls = new ArrayList<>();
    private final List<Integer> insertCalls = new ArrayList<>();
    private final List<Integer> copyCalls = new ArrayList<>();

    private FakeSocketHooks(List<ItemStack> gems, List<Component> tooltip, int socketCount) {
      this.gems = gems.stream().map(gem -> new ApotheosisBridge.SocketGem(gems.indexOf(gem), gem)).toList();
      this.tooltip = tooltip;
      this.socketCount = socketCount;
    }

    private FakeSocketHooks(List<ApotheosisBridge.SocketGem> gems, List<Component> tooltip, int socketCount, boolean mapped) {
      this.gems = gems;
      this.tooltip = tooltip;
      this.socketCount = socketCount;
    }

    private static FakeSocketHooks withMappedGems(List<ApotheosisBridge.SocketGem> gems, List<Component> tooltip, int socketCount) {
      return new FakeSocketHooks(gems, tooltip, socketCount, true);
    }

    @Override
    public boolean hasSocketedGems(ItemStack stack) {
      return gems.stream().anyMatch(ApotheosisBridge.SocketGem::isFilled);
    }

    @Override
    public int getSocketCount(ItemStack stack) {
      return socketCount;
    }

    @Override
    public List<ApotheosisBridge.SocketGem> getSocketedGemData(ItemStack stack) {
      return gems;
    }

    @Override
    public void appendTooltip(ItemStack stack, java.util.function.Consumer<Component> consumer) {
      tooltip.forEach(consumer);
    }

    @Override
    public ItemStack removeGem(ItemStack stack, int socketIndex) {
      removeCalls.add(socketIndex);
      return socketIndex >= 0 ? new ItemStack(Items.IRON_PICKAXE) : ItemStack.EMPTY;
    }

    @Override
    public boolean canInsertGem(ItemStack tool, int rawSocketIndex, ItemStack gem) {
      return rawSocketIndex >= 0;
    }

    @Override
    public ItemStack insertGem(ItemStack tool, int rawSocketIndex, ItemStack gem) {
      insertCalls.add(rawSocketIndex);
      return rawSocketIndex >= 0 ? new ItemStack(Items.NETHERITE_PICKAXE) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack copyGem(ItemStack stack, int socketIndex) {
      copyCalls.add(socketIndex);
      return socketIndex >= 0 ? new ItemStack(Items.AMETHYST_SHARD) : ItemStack.EMPTY;
    }
  }
}
