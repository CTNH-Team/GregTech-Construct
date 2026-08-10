package slimeknights.tconstruct.plugin.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.world.inventory.Slot;
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

  /** 成品槽:菜单第 10 个槽 */
  @Override
  public Slot getOutputSlot(CraftingStationContainerMenu menu) {
    return menu.slots.get(9);
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
    for (int i = 9; i < playerStart; i++) {
      Slot slot = menu.getSlot(i);
      // 跳过空槽,避免把物品放入空槽造成困惑
      if (slot.hasItem()) {
        slots.add(slot);
      }
    }
    for (int i = playerStart; i < totalSize; i++) {
      slots.add(menu.getSlot(i));
    }
    return slots;
  }

  @Override
  public boolean supportsRecipe(EmiRecipe recipe) {
    return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING;
  }
}
