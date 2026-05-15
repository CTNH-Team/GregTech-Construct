package slimeknights.tconstruct.tables.client.inventory;

import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.tables.network.TinkerStationSocketSelectionPacket;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class TinkerStationScreenPacketIntentTest extends BaseMcTest {
  @Test
  void togglingGemModeBuildsToggleIntentPacket() {
    TinkerStationSocketSelectionPacket packet = TinkerStationScreen.createSocketSelectionPacket(false, -1);

    assertThat(packet.getInteractionType()).isEqualTo(TinkerStationSocketSelectionPacket.InteractionType.TOGGLE_MODE);
    assertThat(packet.isGemModeEnabled()).isFalse();
    assertThat(packet.getSocketIndex()).isEqualTo(-1);
  }

  @Test
  void selectingSocketBuildsExplicitRemoveIntentPacket() {
    TinkerStationSocketSelectionPacket packet = TinkerStationScreen.createSocketSelectionPacket(true, 2);

    assertThat(packet.getInteractionType()).isEqualTo(TinkerStationSocketSelectionPacket.InteractionType.REMOVE_TO_PLAYER);
    assertThat(packet.isGemModeEnabled()).isFalse();
    assertThat(packet.getSocketIndex()).isEqualTo(2);
  }
}
