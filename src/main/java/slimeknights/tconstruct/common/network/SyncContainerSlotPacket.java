package slimeknights.tconstruct.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;

/** Single menu slot update with an int-sized stack count. */
public class SyncContainerSlotPacket implements IThreadsafePacket {
  private final int windowId;
  private final int stateId;
  private final int slot;
  private final ItemStack stack;

  public SyncContainerSlotPacket(int windowId, int stateId, int slot, ItemStack stack) {
    this.windowId = windowId;
    this.stateId = stateId;
    this.slot = slot;
    this.stack = stack.copy();
  }

  public SyncContainerSlotPacket(FriendlyByteBuf buffer) {
    this.windowId = buffer.readUnsignedByte();
    this.stateId = buffer.readVarInt();
    this.slot = buffer.readVarInt();
    this.stack = SyncContainerStackCodec.read(buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeByte(windowId);
    buffer.writeVarInt(stateId);
    buffer.writeVarInt(slot);
    SyncContainerStackCodec.write(buffer, stack);
  }

  @Override
  public void handleThreadsafe(Context context) {
    HandleClient.handle(this);
  }

  private static class HandleClient {
    private static void handle(SyncContainerSlotPacket packet) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null) {
        return;
      }
      AbstractContainerMenu menu = minecraft.player.containerMenu;
      if (menu.containerId != packet.windowId || packet.slot < 0 || packet.slot >= menu.slots.size()) {
        return;
      }
      menu.setItem(packet.slot, packet.stateId, packet.stack.copy());
    }
  }
}
