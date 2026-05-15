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
  void extractionRequiresAnvilAndSocketedGem() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);

    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(new ItemStack(Items.EMERALD)), List.of()));

    assertThat(ApotheosisSocketMode.canExtract(tool, 4)).isFalse();
    assertThat(ApotheosisSocketMode.canExtract(tool, 5)).isTrue();

    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(), List.of()));
    assertThat(ApotheosisSocketMode.canExtract(tool, 5)).isFalse();
  }

  @Test
  void selectionNormalizationRejectsInvalidIndexes() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ItemStack firstGem = new ItemStack(Items.EMERALD);
    ItemStack secondGem = new ItemStack(Items.DIAMOND);

    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(firstGem, secondGem), List.of()));

    assertThat(ApotheosisSocketMode.getDisplayedGems(tool)).containsExactly(firstGem, secondGem);
    assertThat(ApotheosisSocketMode.normalizeSelection(tool, -1)).isEqualTo(-1);
    assertThat(ApotheosisSocketMode.normalizeSelection(tool, 0)).isEqualTo(0);
    assertThat(ApotheosisSocketMode.normalizeSelection(tool, 1)).isEqualTo(1);
    assertThat(ApotheosisSocketMode.normalizeSelection(tool, 2)).isEqualTo(-1);
  }

  @Test
  void tooltipAssemblyAddsHeaderAndBridgeLines() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    List<Component> tooltip = new ArrayList<>();

    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(), List.of(Component.literal("Bonus A"), Component.literal("Bonus B"))));

    ApotheosisSocketMode.appendTooltip(tool, tooltip);

    assertThat(tooltip).hasSize(4);
    assertThat(tooltip.get(0).getString()).isEmpty();
    assertThat(tooltip.get(1).getString()).isEqualTo("stat.tconstruct.socket_bonus");
    assertThat(tooltip.subList(2, 4)).extracting(Component::getString).containsExactly("Bonus A", "Bonus B");

    tooltip.clear();
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(), List.of()));
    ApotheosisSocketMode.appendTooltip(tool, tooltip);
    assertThat(tooltip).isEmpty();
  }

  @Test
  void extractionDelegatesToBridge() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    FakeSocketHooks hooks = new FakeSocketHooks(List.of(new ItemStack(Items.EMERALD)), List.of());
    ApotheosisBridge.installSocketHooks(hooks);

    ItemStack result = ApotheosisSocketMode.createResult(tool, 0);
    ItemStack extractedGem = ApotheosisSocketMode.createExtractedGem(tool, 0);
    ItemStack invalidResult = ApotheosisSocketMode.createResult(tool, 5);
    ItemStack invalidExtractedGem = ApotheosisSocketMode.createExtractedGem(tool, -4);

    assertThat(result.getItem()).isEqualTo(Items.IRON_PICKAXE);
    assertThat(extractedGem.getItem()).isEqualTo(Items.AMETHYST_SHARD);
    assertThat(invalidResult.isEmpty()).isTrue();
    assertThat(invalidExtractedGem.isEmpty()).isTrue();
    assertThat(hooks.removeCalls).containsExactly(0, -1);
    assertThat(hooks.copyCalls).containsExactly(0, -1);
  }

  @Test
  void sparseSocketSelectionUsesRawSocketIndex() {
    ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
    ItemStack gem = new ItemStack(Items.EMERALD);
    FakeSocketHooks hooks = FakeSocketHooks.withMappedGems(List.of(new ApotheosisBridge.SocketGem(1, gem)), List.of());
    ApotheosisBridge.installSocketHooks(hooks);

    assertThat(ApotheosisSocketMode.getDisplayedGems(tool)).containsExactly(gem);
    assertThat(ApotheosisSocketMode.normalizeSelection(tool, 0)).isEqualTo(0);
    assertThat(ApotheosisSocketMode.createResult(tool, 0).getItem()).isEqualTo(Items.IRON_PICKAXE);
    assertThat(ApotheosisSocketMode.createExtractedGem(tool, 0).getItem()).isEqualTo(Items.AMETHYST_SHARD);
    assertThat(hooks.removeCalls).containsExactly(1);
    assertThat(hooks.copyCalls).containsExactly(1);
  }

  private static class FakeSocketHooks implements ApotheosisBridge.SocketHooks {
    private final List<ApotheosisBridge.SocketGem> gems;
    private final List<Component> tooltip;
    private final List<Integer> removeCalls = new ArrayList<>();
    private final List<Integer> copyCalls = new ArrayList<>();

    private FakeSocketHooks(List<ItemStack> gems, List<Component> tooltip) {
      this(gems.stream().map(gem -> new ApotheosisBridge.SocketGem(gems.indexOf(gem), gem)).toList(), tooltip, true);
    }

    private FakeSocketHooks(List<ApotheosisBridge.SocketGem> gems, List<Component> tooltip, boolean mapped) {
      this.gems = gems;
      this.tooltip = tooltip;
    }

    private static FakeSocketHooks withMappedGems(List<ApotheosisBridge.SocketGem> gems, List<Component> tooltip) {
      return new FakeSocketHooks(gems, tooltip, true);
    }

    @Override
    public boolean hasSocketedGems(ItemStack stack) {
      return !gems.isEmpty();
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
    public void appendTooltip(ItemStack stack, java.util.function.Consumer<Component> consumer) {
      tooltip.forEach(consumer);
    }

    @Override
    public ItemStack removeGem(ItemStack stack, int socketIndex) {
      removeCalls.add(socketIndex);
      return socketIndex >= 0 ? new ItemStack(Items.IRON_PICKAXE) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack copyGem(ItemStack stack, int socketIndex) {
      copyCalls.add(socketIndex);
      return socketIndex >= 0 ? new ItemStack(Items.AMETHYST_SHARD) : ItemStack.EMPTY;
    }
  }
}
