package slimeknights.tconstruct.tables.menu;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.apache.commons.lang3.tuple.Pair;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.common.network.HighStackCountSynchronizer;
import slimeknights.tconstruct.shared.inventory.TriggeringMultiModuleContainerMenu;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.block.ITabbedBlock;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;
import slimeknights.tconstruct.tables.menu.module.SideInventoryContainer;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/** Base logic for containers with tabs on the menu */
public class TabbedContainerMenu<TILE extends BlockEntity> extends TriggeringMultiModuleContainerMenu<TILE> {
  private static final TinkerBlockComp COMPARATOR = new TinkerBlockComp();
  public final List<Pair<BlockPos, BlockState>> stationBlocks;
  /** Server-provided merged side inventory slot count; -1 means detect locally (server side) */
  protected int sideInventorySlotCount = -1;
  /** Server-provided first adjacent container tile, used by the client mirror so container-specific overlays keep working */
  @Nullable
  protected BlockEntity sideInventoryTile;
  /** Server-provided per-slot capacity limits of the merged side inventory, used by the client mirror */
  @Nullable
  protected int[] sideInventoryLimits;

  public TabbedContainerMenu(MenuType<?> containerType, int id, @Nullable Inventory inv, @Nullable TILE tile) {
    this(containerType, id, inv, tile, -1, null, null);
  }

  /** Menu constructor with a server-provided side inventory slot count, used when reconstructing on the client */
  protected TabbedContainerMenu(MenuType<?> containerType, int id, @Nullable Inventory inv, @Nullable TILE tile, int sideInventorySlotCount) {
    this(containerType, id, inv, tile, sideInventorySlotCount, null, null);
  }

  /** Menu constructor with server-provided side inventory data, used when reconstructing on the client */
  protected TabbedContainerMenu(MenuType<?> containerType, int id, @Nullable Inventory inv, @Nullable TILE tile, int sideInventorySlotCount, @Nullable BlockEntity sideInventoryTile) {
    this(containerType, id, inv, tile, sideInventorySlotCount, sideInventoryTile, null);
  }

  /** Menu constructor with server-provided side inventory data, used when reconstructing on the client */
  protected TabbedContainerMenu(MenuType<?> containerType, int id, @Nullable Inventory inv, @Nullable TILE tile, int sideInventorySlotCount, @Nullable BlockEntity sideInventoryTile, @Nullable int[] sideInventoryLimits) {
    super(containerType, id, inv, tile);
    this.sideInventorySlotCount = sideInventorySlotCount;
    this.sideInventoryTile = sideInventoryTile;
    this.sideInventoryLimits = sideInventoryLimits;

    this.stationBlocks = Lists.newLinkedList();

    if (tile != null && tile.getLevel() != null) {
      this.detectStationParts(tile.getLevel(), tile.getBlockPos());
    }
  }

  /**
   * Vanilla serializes stack counts as a byte. Workstation inventories can expose modded stacks
   * above that range, so use the wider synchronizer for the server-side menu projection.
   */
  @Override
  public void setSynchronizer(@Nullable ContainerSynchronizer synchronizer) {
    if (this.inv != null && this.inv.player instanceof ServerPlayer serverPlayer) {
      super.setSynchronizer(new HighStackCountSynchronizer(serverPlayer));
    } else {
      super.setSynchronizer(synchronizer);
    }
  }

  /**
   * Vanilla caps stack merging at the item's stack size, which is a single stack in normal
   * slots but wastes space in high capacity slots: a 200 stack would not merge 64 more items
   * as 264 exceeds 64, forcing the items into a new slot. Cap by the target slot's item aware
   * capacity instead, which equals the slot limit for high capacity slots and the item limit
   * for normal slots. Non-stackable items (buckets, tools) merge as well, since high capacity
   * slots can hold several of them, and multi count stacks are split across empty slots by
   * the target slot's item aware capacity.
   */
  @Override
  protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean reverse) {
    return moveItemStackTo(this.slots, stack, start, end, reverse);
  }

  /** Static core of {@link #moveItemStackTo(ItemStack, int, int, boolean)}, shared with the unit tests. */
  static boolean moveItemStackTo(List<Slot> slots, ItemStack stack, int start, int end, boolean reverse) {
    boolean moved = false;
    int index = reverse ? end - 1 : start;
    // merge into existing stacks of the same item first; also merges non-stackable items
    // as high capacity slots can hold several of them
    while (!stack.isEmpty() && (reverse ? index >= start : index < end)) {
      Slot slot = slots.get(index);
      ItemStack current = slot.getItem();
      if (!current.isEmpty() && ItemStack.isSameItemSameTags(stack, current)) {
        int limit = slot.getMaxStackSize(stack);
        int combined = current.getCount() + stack.getCount();
        if (combined <= limit) {
          stack.setCount(0);
          current.setCount(combined);
          slot.setChanged();
          moved = true;
        } else if (current.getCount() < limit) {
          stack.shrink(limit - current.getCount());
          current.setCount(limit);
          slot.setChanged();
          moved = true;
        }
      }
      index += reverse ? -1 : 1;
    }
    // place the remainder into empty slots, splitting across several slots when the stack
    // exceeds the target slot's item aware capacity (e.g. seven buckets into seven slots)
    if (!stack.isEmpty()) {
      index = reverse ? end - 1 : start;
      while (!stack.isEmpty() && (reverse ? index >= start : index < end)) {
        Slot slot = slots.get(index);
        if (slot.getItem().isEmpty() && slot.mayPlace(stack)) {
          int limit = slot.getMaxStackSize(stack);
          slot.setByPlayer(stack.getCount() > limit ? stack.split(limit) : stack.split(stack.getCount()));
          slot.setChanged();
          moved = true;
        }
        index += reverse ? -1 : 1;
      }
    }
    return moved;
  }

  /**
   * Shift clicking a side inventory slot moves straight to the player inventory. Mantle's
   * default first tops up the station's own tile slots, which for the crafting station is
   * the crafting grid, but those are not storage.
   */
  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (this.getSlotContainer(index) != this) {
      Slot slot = this.slots.get(index);
      if (!slot.hasItem()) {
        return ItemStack.EMPTY;
      }
      ItemStack stack = slot.getItem().copy();
      ItemStack moved = stack.copy();
      // Mantle helpers report "nothing done" as true, so compare the remainder instead
      this.moveToPlayerInventory(moved);
      if (moved.getCount() >= stack.getCount()) {
        return ItemStack.EMPTY;
      }
      return this.notifySlotAfterTransfer(player, moved, stack, slot);
    }
    return super.quickMoveStack(player, index);
  }

  /**
   * Detects the given station parts nearby the given position
   *
   * @param world the current world
   * @param start the current position of the tile entity
   */
  public void detectStationParts(Level world, BlockPos start) {
    Set<BlockPos> visited = Sets.newHashSet();

    // BFS for related blocks
    Queue<BlockPos> queue = new ArrayDeque<>();
    queue.add(start);

    while (!queue.isEmpty()) {
      BlockPos pos = queue.poll();
      // already visited between adding and call
      if (visited.contains(pos)) {
        continue;
      }

      BlockState state = world.getBlockState(pos);
      if (!(state.getBlock() instanceof ITabbedBlock)) {
        // not a valid block for us
        continue;
      }

      // found a part, add surrounding blocks that haven't been visited yet
      for (Direction direction : Direction.values()) {
        BlockPos offset = pos.relative(direction);
        if (!visited.contains(offset)) {
          queue.add(offset);
        }
      }

      // mark this block as visited to visited
      visited.add(pos);

      // save the thing
      this.stationBlocks.add(Pair.of(pos, state));

      // we only have space for 6 tabs, so stop after the first 6
      if (this.stationBlocks.size() >= 6) {
        break;
      }
    }

    // sort the found blocks by priority
    this.stationBlocks.sort(COMPARATOR);
  }

  /** Result of detecting adjacent side containers. */
  public record SideInventoryInfo(List<IItemHandlerModifiable> handlers, List<BlockEntity> tiles) {
    /** Total merged slot count across all detected containers. */
    public int slotCount() {
      int total = 0;
      for (IItemHandlerModifiable handler : handlers) {
        total += handler.getSlots();
      }
      return total;
    }
  }

  /** Detects every usable adjacent container around the given position, skipping other table parts. */
  public static SideInventoryInfo detectSideInventories(Level world, BlockPos pos, Player player) {
    List<IItemHandlerModifiable> handlers = new ArrayList<>();
    List<BlockEntity> tiles = new ArrayList<>();
    if (world == null) {
      return new SideInventoryInfo(handlers, tiles);
    }
    for (Direction dir : Direction.Plane.HORIZONTAL) {
      // skip any tables in this multiblock
      BlockPos neighbor = pos.relative(dir);
      if (world.getBlockState(neighbor).getBlock() instanceof ITabbedBlock) {
        continue;
      }

      // fetch tile entity
      BlockEntity te = world.getBlockEntity(neighbor);
      if (te == null || !isUsable(te, player)) {
        continue;
      }

      // try internal access first, then sided access from the station's side
      Direction accessDir = null;
      if (!hasItemHandler(te, null)) {
        Direction side = dir.getOpposite();
        if (!hasItemHandler(te, side)) {
          continue;
        }
        accessDir = side;
      }

      IItemHandlerModifiable handler = te.getCapability(ForgeCapabilities.ITEM_HANDLER, accessDir)
        .filter(cap -> cap instanceof IItemHandlerModifiable)
        .map(cap -> (IItemHandlerModifiable) cap)
        .orElse(null);
      if (handler != null) {
        handlers.add(handler);
        tiles.add(te);
      }
    }
    return new SideInventoryInfo(handlers, tiles);
  }

  /** Client-side side inventory data read from the menu-open buffer. */
  public record SideInventoryClientData(int slotCount, @Nullable BlockEntity tile, @Nullable int[] limits) {
    /** Reads the side inventory payload from the buffer, after the station position was consumed. */
    public static SideInventoryClientData read(FriendlyByteBuf buf) {
      if (buf == null) {
        return new SideInventoryClientData(-1, null, null);
      }
      int slotCount = buf.readVarInt();
      BlockEntity tile = buf.readBoolean()
        ? DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> BlockEntityHelper.get(BlockEntity.class, Minecraft.getInstance().level, buf.readBlockPos()).orElse(null))
        : null;
      int[] limits = slotCount > 0 ? new int[slotCount] : null;
      for (int i = 0; i < slotCount; i++) {
        limits[i] = buf.readVarInt();
      }
      return new SideInventoryClientData(slotCount, tile, limits);
    }
  }

  /** Item handler mirror for the client side panel, replicating the server's per-slot capacities. */
  private static final class MirrorSideInventoryHandler extends ItemStackHandler {
    private final int[] slotLimits;

    private MirrorSideInventoryHandler(int[] slotLimits) {
      super(slotLimits.length);
      this.slotLimits = slotLimits;
    }

    @Override
    public int getSlotLimit(int slot) {
      return this.slotLimits[slot];
    }

    @Override
    public int getStackLimit(int slot, ItemStack stack) {
      return this.slotLimits[slot];
    }
  }

  /** Adds a single side inventory merging every adjacent container to this container */
  protected void addChestSideInventory() {
    if (tile == null || inv == null) {
      return;
    }
    Level world = tile.getLevel();
    if (world == null) {
      return;
    }

    // client side: the server sent the merged slot count, mirror it so both sides stay aligned
    if (this.sideInventorySlotCount >= 0) {
      if (this.sideInventorySlotCount > 0) {
        // use the real first container as the panel tile so Sophisticated Storage overlays/search keep working
        BlockEntity displayTile = this.sideInventoryTile != null ? this.sideInventoryTile : tile;
        IItemHandlerModifiable mirror = this.sideInventoryLimits != null ? new MirrorSideInventoryHandler(this.sideInventoryLimits) : new ItemStackHandler(this.sideInventorySlotCount);
        int columns = Mth.clamp((this.sideInventorySlotCount - 1) / 9 + 1, 3, 6);
        this.addSubContainer(new SideInventoryContainer<BlockEntity>(TinkerTables.craftingStationContainer.get(), containerId, inv, displayTile, mirror, -6 - 18 * 6, 8, columns), false);
      }
      return;
    }

    // server side: detect every adjacent container and merge them into a single side inventory panel
    SideInventoryInfo info = detectSideInventories(world, tile.getBlockPos(), inv.player);
    if (info.handlers().isEmpty()) {
      return;
    }

    // one panel controlling all detected containers
    IItemHandlerModifiable combined = info.handlers().size() == 1 ? info.handlers().get(0) : new CombinedInvWrapper(info.handlers().toArray(new IItemHandlerModifiable[0]));
    int columns = Mth.clamp((info.slotCount() - 1) / 9 + 1, 3, 6);
    this.addSubContainer(new SideInventoryContainer<>(TinkerTables.craftingStationContainer.get(), containerId, inv, info.tiles().get(0), info.tiles(), combined, -6 - 18 * 6, 8, columns), false);
  }

  /**
   * Checks if the given tile entity is blacklisted
   * @param tileEntity  Tile to check
   * @return  True if blacklisted
   */
  @SuppressWarnings("deprecation")  // your tag utils are overkill
  private static boolean isUsable(BlockEntity tileEntity, Player player) {
    // must not be blacklisted and be usable
    return (Config.COMMON.disableSideInventoryWhitelist.get() || RegistryHelper.contains(BuiltInRegistries.BLOCK_ENTITY_TYPE, TinkerTags.TileEntityTypes.SIDE_INVENTORIES, tileEntity.getType()))
           && (!(tileEntity instanceof Container) || ((Container)tileEntity).stillValid(player));
  }

  /**
   * Checks to see if the given Tile Entity has an item handler that's compatible with the side inventory
   * The Tile Entity's item handler must be an instance of IItemHandlerModifiable
   * @param tileEntity Tile to check
   * @param direction the given direction
   * @return True if compatible.
   */
  private static boolean hasItemHandler(BlockEntity tileEntity, @Nullable Direction direction) {
    return tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).filter(cap -> cap instanceof IItemHandlerModifiable).isPresent();
  }


  /**
   * Sends a update to the client's current screen.
   */
  public void updateScreen() {
    if (this.tile != null) {
      if (this.tile.getLevel() != null) {
        if (this.tile.getLevel().isClientSide && FMLEnvironment.dist == Dist.CLIENT) {
          ClientOnly.clientScreenUpdate();
        }
      }
    }
  }

  /**
   * Tells the client to display the LOCALIZED error message
   */
  public void error(final MutableComponent message) {
    if (this.tile != null) {
      if (this.tile.getLevel() != null) {
        if (this.tile.getLevel().isClientSide && FMLEnvironment.dist == Dist.CLIENT) {
          ClientOnly.clientError(message);
        }
      }
    }
  }

  /**
   * Tells the client to display the LOCALIZED warning message
   */
  public void warning(final MutableComponent message) {
    if (this.tile != null) {
      if (this.tile.getLevel() != null) {
        if (this.tile.getLevel().isClientSide && FMLEnvironment.dist == Dist.CLIENT) {
          ClientOnly.clientWarning(message);
        }
      }
    }
  }

  /** Logic for comparing two blocks based on position and state */
  private static class TinkerBlockComp implements Comparator<Pair<BlockPos, BlockState>> {
    @Override
    public int compare(Pair<BlockPos, BlockState> o1, Pair<BlockPos, BlockState> o2) {
      // base location: lowest overall position
      BlockPos pos1 = o1.getLeft();
      BlockPos pos2 = o2.getLeft();
      int sum1 = pos1.getX() + pos1.getY() + pos1.getZ();
      int sum2 = pos2.getX() + pos2.getY() + pos2.getZ();
      if (sum1 != sum2) {
        return Integer.compare(sum1, sum2);
      }
      // so they have the same distance from 0,0,0, prefer lower y, then x, then z
      if (pos1.getY() != pos2.getY()) {
        return Integer.compare(pos1.getY(), pos2.getY());
      }
      if (pos1.getX() != pos2.getX()) {
        return Integer.compare(pos1.getX(), pos2.getX());
      }
      return Integer.compare(pos1.getZ(), pos2.getZ());
    }
  }

  /** Methods that only work on the client side */
  private static class ClientOnly {
    /** Updates the client's screen */
    private static void clientScreenUpdate() {
      Screen screen = Minecraft.getInstance().screen;
      if (screen instanceof BaseTabbedScreen) {
        ((BaseTabbedScreen<?,?>) screen).updateDisplay();
      }
    }

    /** Sends the error message from the container to the client's screen */
    private static void clientError(MutableComponent errorMessage) {
      Screen screen = Minecraft.getInstance().screen;
      if (screen instanceof BaseTabbedScreen) {
        ((BaseTabbedScreen<?,?>) screen).error(errorMessage);
      }
    }

    /** Sends the warning message from the container to the client's screen */
    private static void clientWarning(MutableComponent warningMessage) {
      Screen screen = Minecraft.getInstance().screen;
      if (screen instanceof BaseTabbedScreen) {
        ((BaseTabbedScreen<?,?>) screen).warning(warningMessage);
      }
    }
  }
}
