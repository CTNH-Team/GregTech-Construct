package slimeknights.tconstruct.tables.client.inventory;

import org.junit.jupiter.api.Test;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import slimeknights.tconstruct.tables.network.TinkerStationSocketSelectionPacket;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class TinkerStationScreenPacketIntentTest extends BaseMcTest {
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
}
