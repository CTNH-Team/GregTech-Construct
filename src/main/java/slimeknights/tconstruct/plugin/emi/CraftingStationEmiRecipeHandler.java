package slimeknights.tconstruct.plugin.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.ToolIngredient;
import net.minecraft.world.inventory.Slot;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;
import slimeknights.tconstruct.tables.menu.slot.PlayerSensitiveLazyResultSlot;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作站(3x3 合成)的 EMI 配方填充 handler。EMI 按 MenuType 精确匹配填充 handler,
 * 工作站使用自己的 MenuType,原版 EMI 只为 CraftingMenu 注册 handler,未注册时
 * 填充按钮对所有工作台配方显示 "emi.inapplicable"(当前工作方块不支持该配方)。
 */
public class CraftingStationEmiRecipeHandler implements StandardRecipeHandler<CraftingStationContainerMenu> {
  /** 合成格槽位:菜单前 9 个槽 */
  @Override
  public List<Slot> getCraftingSlots(CraftingStationContainerMenu menu) {
    return menu.slots.subList(0, 9);
  }

  /** Maps GT tool ingredients to the dedicated tool slots during EMI transfer. */
  @Override
  public List<Slot> getCraftingSlots(EmiRecipe emiRecipe, CraftingStationContainerMenu menu) {
    if (!(emiRecipe.getBackingRecipe() instanceof CraftingRecipe recipe)) {
      return getCraftingSlots(menu);
    }
    List<Ingredient> ingredients = recipe.getIngredients();
    int inputCount = emiRecipe.getInputs().size();
    List<Slot> slots = new ArrayList<>(inputCount);
    boolean[] claimedTools = new boolean[CraftingStationBlockEntity.TOOL_SLOT_COUNT];
    int toolIndex = 0;
    int normalIndex = 0;
    boolean shaped = recipe instanceof ShapedRecipe;
    for (int i = 0; i < inputCount; i++) {
      Ingredient ingredient = i < ingredients.size() ? ingredients.get(i) : Ingredient.EMPTY;
      if (ingredient instanceof ToolIngredient) {
        if (toolIndex >= CraftingStationBlockEntity.TOOL_SLOT_COUNT) return getCraftingSlots(menu);
        int destination = findToolDestination(menu, ingredient, claimedTools);
        if (destination < 0) return getCraftingSlots(menu);
        claimedTools[destination] = true;
        slots.add(menu.getSlot(CraftingStationBlockEntity.TOOL_SLOT_START + destination));
        toolIndex++;
      } else if (shaped) {
        slots.add(menu.getSlot(i));
      } else {
        // Shapeless EMI inputs are compacted; map ordinary ingredients to the first
        // available grid slots while tools are routed to tool slots above.
        while (normalIndex < 9 && menu.getSlot(normalIndex).hasItem()) normalIndex++;
        slots.add(menu.getSlot(Math.min(normalIndex++, 8)));
      }
    }
    return slots;
  }

  /** Keep an already matching tool in place; otherwise use an empty slot. */
  private static int findToolDestination(CraftingStationContainerMenu menu, Ingredient requirement,
                                         boolean[] claimed) {
    for (int pass = 0; pass < 2; pass++) {
      for (int i = 0; i < claimed.length; i++) {
        if (claimed[i]) continue;
        Slot slot = menu.getSlot(CraftingStationBlockEntity.TOOL_SLOT_START + i);
        boolean matching = !slot.getItem().isEmpty() && requirement.test(slot.getItem().copy());
        boolean empty = slot.getItem().isEmpty();
        if ((pass == 0 && matching) || (pass == 1 && empty)) return i;
      }
    }
    return -1;
  }

  /** 成品槽:工具槽之后的惰性结果槽 */
  @Override
  public Slot getOutputSlot(CraftingStationContainerMenu menu) {
    for (Slot slot : menu.slots) {
      if (slot instanceof PlayerSensitiveLazyResultSlot) return slot;
    }
    return menu.slots.get(CraftingStationBlockEntity.CRAFTING_SLOT_COUNT + CraftingStationBlockEntity.TOOL_SLOT_COUNT);
  }

  /**
   * 输入来源:合成格 + 侧栏容器(仅非空槽) + 玩家背包(末尾 36 槽),
   * 与旧 JEI 传输的槽位划分一致
   */
  @Override
  public List<Slot> getInputSources(CraftingStationContainerMenu menu) {
    List<Slot> slots = new ArrayList<>();
    int totalSize = menu.slots.size();
    int playerStart = totalSize - 36;
    for (int i = 0; i < 9; i++) {
      slots.add(menu.getSlot(i));
    }
    // Side inventories remain additional sources. Installed tool slots are appended only
    // as a fallback source, after player/connected inventories, so EMI does not pull an
    // already-installed tool out of its slot when switching between recipes.
    int sideStart = CraftingStationBlockEntity.CRAFTING_SLOT_COUNT + CraftingStationBlockEntity.TOOL_SLOT_COUNT + 1;
    for (int i = sideStart; i < playerStart; i++) {
      Slot slot = menu.getSlot(i);
      // 跳过空槽,避免把物品放入空槽造成困惑
      if (slot.hasItem()) {
        slots.add(slot);
      }
    }
    for (int i = playerStart; i < totalSize; i++) {
      slots.add(menu.getSlot(i));
    }
    for (int i = 0; i < CraftingStationBlockEntity.TOOL_SLOT_COUNT; i++) {
      Slot slot = menu.getSlot(CraftingStationBlockEntity.TOOL_SLOT_START + i);
      if (slot.hasItem()) slots.add(slot);
    }
    return slots;
  }

  /** Includes installed tools in EMI's availability and transfer calculation. */
  @Override
  public EmiPlayerInventory getInventory(AbstractContainerScreen<CraftingStationContainerMenu> screen) {
    List<EmiStack> stacks = new ArrayList<>();
    for (Slot slot : getInputSources(screen.getMenu())) {
      if (slot.hasItem()) stacks.add(EmiStack.of(slot.getItem()));
    }
    return new EmiPlayerInventory(stacks);
  }

  @Override
  public boolean supportsRecipe(EmiRecipe recipe) {
    return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING;
  }
}
