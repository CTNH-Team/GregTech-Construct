package slimeknights.tconstruct.tables.menu.slot;

import com.gregtechceu.gtceu.api.item.CustomToolIngredientHelper;
import com.gregtechceu.gtceu.api.item.IGTTool;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;

/** Dedicated GT crafting-tool slot. Tools remain outside the vanilla 3x3 grid. */
public class CraftingStationToolSlot extends Slot {
  public CraftingStationToolSlot(CraftingStationBlockEntity container, int index, int x, int y) {
    super(container, index, x, y);
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return stack.isEmpty() || isCraftingTool(stack);
  }

  @Override
  public int getMaxStackSize() {
    return 1;
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return 1;
  }

  @Override
  public boolean mayPickup(Player player) {
    return true;
  }

  /** Fast broad filter; the recipe resolver performs the exact ToolIngredient check. */
  public static boolean isCraftingTool(ItemStack stack) {
    return !stack.isEmpty() && (stack.getItem() instanceof IGTTool || CustomToolIngredientHelper.isCustomTool(stack));
  }
}
