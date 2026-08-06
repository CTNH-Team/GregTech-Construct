package slimeknights.tconstruct.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;

import java.util.ArrayList;
import java.util.List;

/** Full menu stack state, used when a menu is first synchronized. */
public class SyncContainerStacksPacket implements IThreadsafePacket {
  private final int windowId;
  private final int stateId;
  private final List<ItemStack> stacks;
  private final ItemStack carried;

  public SyncContainerStacksPacket(int windowId, int stateId, List<ItemStack> stacks, ItemStack carried) {
    this.windowId = windowId;
    this.stateId = stateId;
    this.stacks = new ArrayList<>(stacks.size());
    for (ItemStack stack : stacks) {
      this.stacks.add(stack.copy());
    }
    this.carried = carried.copy();
  }

  public SyncContainerStacksPacket(FriendlyByteBuf buffer) {
    this.windowId = buffer.readUnsignedByte();
    this.stateId = buffer.readVarInt();
    int size = buffer.readVarInt();
    this.stacks = new ArrayList<>(size);
    for (int index = 0; index < size; index++) {
      this.stacks.add(SyncContainerStackCodec.read(buffer));
    }
    this.carried = SyncContainerStackCodec.read(buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeByte(windowId);
    buffer.writeVarInt(stateId);
    buffer.writeVarInt(stacks.size());
    for (ItemStack stack : stacks) {
      SyncContainerStackCodec.write(buffer, stack);
    }
    SyncContainerStackCodec.write(buffer, carried);
  }

  @Override
  public void handleThreadsafe(Context context) {
    HandleClient.handle(this);
  }

  private static class HandleClient {
    private static void handle(SyncContainerStacksPacket packet) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null) {
        return;
      }
      AbstractContainerMenu menu = minecraft.player.containerMenu;
      if (menu.containerId != packet.windowId) {
        return;
      }
      NonNullList<ItemStack> contents = NonNullList.withSize(packet.stacks.size(), ItemStack.EMPTY);
      for (int index = 0; index < packet.stacks.size(); index++) {
        contents.set(index, packet.stacks.get(index).copy());
      }
      menu.initializeContents(packet.stateId, contents, packet.carried.copy());
    }
  }
}
