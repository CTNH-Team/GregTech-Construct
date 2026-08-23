package slimeknights.tconstruct.tables.block.entity.inventory;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link CraftingContainer} to use instead wrap an existing {@link Container}.
 * TODO: consider using an item handler instead?
 */
public class CraftingContainerWrapper implements CraftingContainer {
  private final Container crafter;
  private final int offset;
  @Getter
  private final int width;
  @Getter
  private final int height;
  public CraftingContainerWrapper(Container crafter, int width, int height) {
    this(crafter, width, height, 0);
  }

  /** Creates a view over a rectangular range in the backing container. */
  public CraftingContainerWrapper(Container crafter, int width, int height, int offset) {
    Preconditions.checkArgument(offset >= 0 && crafter.getContainerSize() >= offset + width * height,
      "Invalid width and height for inventory size");
    this.crafter = crafter;
    this.width = width;
    this.height = height;
    this.offset = offset;
  }

  /** Inventory redirection */

  @Override
  public ItemStack getItem(int index) {
    return crafter.getItem(index + offset);
  }

  @Override
  public int getContainerSize() {
    return crafter.getContainerSize();
  }

  @Override
  public boolean isEmpty() {
    return crafter.isEmpty();
  }

  @Override
  public ItemStack removeItemNoUpdate(int index) {
    return crafter.removeItemNoUpdate(index + offset);
  }

  @Override
  public ItemStack removeItem(int index, int count) {
    return crafter.removeItem(index + offset, count);
  }

  @Override
  public void setItem(int index, ItemStack stack) {
    crafter.setItem(index + offset, stack);
  }

  @Override
  public void setChanged() {
    crafter.setChanged();
  }

  @Override
  public boolean stillValid(Player pPlayer) {
    return true;
  }

  @Override
  public void clearContent() {
    crafter.clearContent();
  }

  @Override
  public void fillStackedContents(StackedContents helper) {
    for (int i = 0; i < getContainerSize(); i++) {
      helper.accountSimpleStack(crafter.getItem(i + offset));
    }
  }

  @Override
  public List<ItemStack> getItems() {
    // TODO: would rather use the internal list for the container, should make a custom item handler perhaps?
    List<ItemStack> stacks = new ArrayList<>(getContainerSize());
    for (int i = 0; i < getContainerSize(); i++) {
      stacks.add(getItem(i));
    }
    return stacks;
  }
}
