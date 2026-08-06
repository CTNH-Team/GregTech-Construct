package slimeknights.tconstruct.common.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Shared stack codec for menu synchronization. Counts use a VarInt instead of vanilla's byte. */
final class SyncContainerStackCodec {
  private SyncContainerStackCodec() {}

  static void write(FriendlyByteBuf buffer, ItemStack stack) {
    if (stack.isEmpty()) {
      buffer.writeBoolean(false);
      return;
    }
    buffer.writeBoolean(true);
    Item item = stack.getItem();
    buffer.writeVarInt(BuiltInRegistries.ITEM.getId(item));
    buffer.writeVarInt(stack.getCount());
    CompoundTag tag = item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt() ? stack.getShareTag() : null;
    buffer.writeNbt(tag);
  }

  static ItemStack read(FriendlyByteBuf buffer) {
    if (!buffer.readBoolean()) {
      return ItemStack.EMPTY;
    }
    Item item = BuiltInRegistries.ITEM.byId(buffer.readVarInt());
    int count = buffer.readVarInt();
    ItemStack stack = new ItemStack(item, count);
    stack.readShareTag(buffer.readNbt());
    return stack;
  }
}
