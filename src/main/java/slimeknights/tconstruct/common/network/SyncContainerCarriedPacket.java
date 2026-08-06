package slimeknights.tconstruct.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;

/** Carried-stack update with an int-sized stack count. */
public class SyncContainerCarriedPacket implements IThreadsafePacket {
  private final int windowId;
  private final int stateId;
  private final ItemStack carried;

  public SyncContainerCarriedPacket(int windowId, int stateId, ItemStack carried) {
    this.windowId = windowId;
    this.stateId = stateId;
    this.carried = carried.copy();
  }

  public SyncContainerCarriedPacket(FriendlyByteBuf buffer) {
    this.windowId = buffer.readUnsignedByte();
    this.stateId = buffer.readVarInt();
    this.carried = SyncContainerStackCodec.read(buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeByte(windowId);
    buffer.writeVarInt(stateId);
    SyncContainerStackCodec.write(buffer, carried);
  }

  @Override
  public void handleThreadsafe(Context context) {
    HandleClient.handle(this);
  }

  private static class HandleClient {
    private static void handle(SyncContainerCarriedPacket packet) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null) {
        return;
      }
      AbstractContainerMenu menu = minecraft.player.containerMenu;
      if (menu.containerId != packet.windowId) {
        return;
      }
      // Reapply the complete projection so carried updates retain the same state ordering as slot updates.
      NonNullList<ItemStack> contents = NonNullList.withSize(menu.slots.size(), ItemStack.EMPTY);
      for (int index = 0; index < menu.slots.size(); index++) {
        contents.set(index, menu.getSlot(index).getItem().copy());
      }
      menu.initializeContents(packet.stateId, contents, packet.carried.copy());
    }
  }
}
