package slimeknights.tconstruct.common.network;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;

/**
 * Container synchronizer that keeps stack counts wider than vanilla's byte field.
 *
 * <p>Vanilla still handles the menu click packet, but the server remains authoritative for
 * every mutation. The custom packets only replace the server-to-client stack projection.</p>
 */
public class HighStackCountSynchronizer implements ContainerSynchronizer {
  private final ServerPlayer player;

  public HighStackCountSynchronizer(ServerPlayer player) {
    this.player = player;
  }

  @Override
  public void sendInitialData(AbstractContainerMenu menu, NonNullList<ItemStack> items, ItemStack carried, int[] dataSlots) {
    TinkerNetwork.getInstance().sendTo(new SyncContainerStacksPacket(menu.containerId, menu.incrementStateId(), items, carried), player);
    // Data slots are already integer based, so retain the vanilla packet for those values.
    for (int index = 0; index < dataSlots.length; index++) {
      sendDataChange(menu, index, dataSlots[index]);
    }
  }

  @Override
  public void sendSlotChange(AbstractContainerMenu menu, int slot, ItemStack stack) {
    TinkerNetwork.getInstance().sendTo(new SyncContainerSlotPacket(menu.containerId, menu.incrementStateId(), slot, stack), player);
  }

  @Override
  public void sendCarriedChange(AbstractContainerMenu menu, ItemStack carried) {
    TinkerNetwork.getInstance().sendTo(new SyncContainerCarriedPacket(menu.containerId, menu.incrementStateId(), carried), player);
  }

  @Override
  public void sendDataChange(AbstractContainerMenu menu, int index, int value) {
    player.connection.send(new ClientboundContainerSetDataPacket(menu.containerId, index, value));
  }
}
