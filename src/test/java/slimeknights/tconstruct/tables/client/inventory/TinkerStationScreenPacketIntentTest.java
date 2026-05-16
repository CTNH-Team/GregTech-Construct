package slimeknights.tconstruct.tables.client.inventory;

import org.junit.jupiter.api.Test;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import slimeknights.tconstruct.tables.network.TinkerStationSocketSelectionPacket;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class TinkerStationScreenPacketIntentTest extends BaseMcTest {
  @Test
  void gemModeButtonUsesSquareDiamondIconBelowOutputSlot() {
    TinkerStationScreen.GemModeButtonSpec spec = TinkerStationScreen.createGemModeButtonSpec();
    TinkerStationScreen.GemModeButtonPosition position = TinkerStationScreen.resolveGemModeButtonPosition(100, 50, spec);
    TinkerStationScreen.GemModeButtonIconInset inset = TinkerStationScreen.createGemModeButtonIconInset(spec.size());

    assertThat(spec.x()).isPositive();
    assertThat(spec.y()).isPositive();
    assertThat(spec.size()).isEqualTo(16);
    assertThat(spec.iconItem()).isNotNull();
    assertThat(position.x()).isEqualTo(100 + spec.x());
    assertThat(position.y()).isEqualTo(50 + spec.y());
    assertThat(inset.x()).isEqualTo(0);
    assertThat(inset.y()).isEqualTo(0);
  }

  @Test
  void togglingWhileGemModeActiveBuildsDisablePacket() {
    TinkerStationSocketSelectionPacket packet = TinkerStationScreen.createTogglePacketForCurrentMode(true);

    assertThat(packet.getInteractionType()).isEqualTo(TinkerStationSocketSelectionPacket.InteractionType.TOGGLE_MODE);
    assertThat(packet.isGemModeEnabled()).isFalse();
    assertThat(packet.getSocketIndex()).isEqualTo(-1);
  }

  @Test
  void togglingGemModeBuildsToggleIntentPacket() {
    TinkerStationSocketSelectionPacket packet = TinkerStationScreen.createTogglePacket(true);

    assertThat(packet.getInteractionType()).isEqualTo(TinkerStationSocketSelectionPacket.InteractionType.TOGGLE_MODE);
    assertThat(packet.isGemModeEnabled()).isTrue();
    assertThat(packet.getSocketIndex()).isEqualTo(-1);
  }

  @Test
  void carriedGemInteractionBuildsInsertIntentPacket() {
    TinkerStationSocketSelectionPacket packet = TinkerStationScreen.createSocketInteractionPacket(2, new ItemStack(Items.DIAMOND), true);

    assertThat(packet.getInteractionType()).isEqualTo(TinkerStationSocketSelectionPacket.InteractionType.INSERT_FROM_CARRIED);
    assertThat(packet.isGemModeEnabled()).isFalse();
    assertThat(packet.getSocketIndex()).isEqualTo(2);
  }

  @Test
  void emptyHandSocketInteractionBuildsRemoveIntentPacket() {
    TinkerStationSocketSelectionPacket packet = TinkerStationScreen.createSocketInteractionPacket(2, ItemStack.EMPTY, true);

    assertThat(packet.getInteractionType()).isEqualTo(TinkerStationSocketSelectionPacket.InteractionType.REMOVE_TO_PLAYER);
    assertThat(packet.isGemModeEnabled()).isFalse();
    assertThat(packet.getSocketIndex()).isEqualTo(2);
  }

  @Test
  void emptyHandEmptySocketInteractionBuildsNoPacket() {
    TinkerStationSocketSelectionPacket packet = TinkerStationScreen.createSocketInteractionPacket(2, ItemStack.EMPTY, false);

    assertThat(packet).isNull();
  }

  @Test
  void gemModeTooltipRequiresActualHoverInsteadOfButtonFocus() {
    assertThat(TinkerStationScreen.shouldShowGemModeTooltip(true, false)).isFalse();
    assertThat(TinkerStationScreen.shouldShowGemModeTooltip(true, true)).isTrue();
    assertThat(TinkerStationScreen.shouldShowGemModeTooltip(false, true)).isFalse();
  }

  @Test
  void gemModeButtonVisualStateUsesHighlightOnlyWhenModeIsOn() {
    assertThat(TinkerStationScreen.getGemModeButtonVisualState(true, true)).isEqualTo(TinkerStationScreen.GemModeButtonVisualState.HIGHLIGHTED);
    assertThat(TinkerStationScreen.getGemModeButtonVisualState(false, true)).isEqualTo(TinkerStationScreen.GemModeButtonVisualState.NORMAL);
    assertThat(TinkerStationScreen.getGemModeButtonVisualState(false, false)).isEqualTo(TinkerStationScreen.GemModeButtonVisualState.DISABLED);
  }

  @Test
  void gemModeButtonDoesNotUseSeparateHoverVisualState() {
    assertThat(TinkerStationScreen.getGemModeButtonVisualState(true, true)).isEqualTo(TinkerStationScreen.GemModeButtonVisualState.HIGHLIGHTED);
    assertThat(TinkerStationScreen.getGemModeButtonVisualState(false, true)).isEqualTo(TinkerStationScreen.GemModeButtonVisualState.NORMAL);
  }

  @Test
  void gemModeTooltipStillShowsForDisabledNoSocketButton() {
    assertThat(TinkerStationScreen.shouldShowGemModeTooltip(true, true)).isTrue();
  }

  @Test
  void gemModeTooltipLinesShowModeFirstAndReasonSecond() {
    assertThat(TinkerStationScreen.createGemModeTooltipLines(true, "gui.tconstruct.tinker_station.gem_mode.help"))
      .extracting(Component::getString)
      .containsExactly("gui.tconstruct.tinker_station.gem_mode.on", "gui.tconstruct.tinker_station.gem_mode.help");

    assertThat(TinkerStationScreen.createGemModeTooltipLines(false, "gui.tconstruct.tinker_station.gem_mode.no_sockets"))
      .extracting(Component::getString)
      .containsExactly("gui.tconstruct.tinker_station.gem_mode.off", "gui.tconstruct.tinker_station.gem_mode.no_sockets");
  }

  @Test
  void gemModeDisabledButtonHoverUsesGeometryInsteadOfWidgetActiveState() {
    Button button = Button.builder(Component.empty(), b -> {}).pos(10, 20).size(16, 16).build();
    button.visible = true;
    button.active = false;

    assertThat(TinkerStationScreen.isGemModeButtonHovered(button, 12, 24)).isTrue();
    assertThat(TinkerStationScreen.isGemModeButtonHovered(button, 5, 24)).isFalse();
  }
}
