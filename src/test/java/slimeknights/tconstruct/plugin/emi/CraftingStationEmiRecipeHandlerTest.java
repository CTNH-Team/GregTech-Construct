package slimeknights.tconstruct.plugin.emi;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 工作站 EMI 配方填充 handler 的槽位映射,布局与 {@link CraftingStationContainerMenu}
 * 一致:前 9 槽为 3x3 合成格,第 10 槽为成品,之后是侧栏容器与玩家背包(末尾 36 槽)。
 */
class CraftingStationEmiRecipeHandlerTest extends BaseMcTest {
  private static final int GRID = 9;
  private static final int SIDE = 18;
  private static final int PLAYER = 36;

  /** 构造一个布局与真实工作站菜单一致、但没有 tile 的菜单实例 */
  private static CraftingStationContainerMenu createMenu(ItemStack sideStack) {
    CraftingStationContainerMenu menu = allocateMenu();
    NonNullList<Slot> slots = NonNullList.create();
    SimpleContainer grid = new SimpleContainer(GRID);
    SimpleContainer result = new SimpleContainer(1);
    SimpleContainer side = new SimpleContainer(SIDE);
    SimpleContainer player = new SimpleContainer(PLAYER);
    for (int i = 0; i < GRID; i++) {
      slots.add(new Slot(grid, i, 0, 0));
    }
    slots.add(new Slot(result, 0, 0, 0));
    for (int i = 0; i < SIDE; i++) {
      Slot slot = new Slot(side, i, 0, 0);
      if (sideStack != null && i == 0) {
        slot.set(sideStack);
      }
      slots.add(slot);
    }
    for (int i = 0; i < PLAYER; i++) {
      slots.add(new Slot(player, i, 0, 0));
    }
    setField(menu, "slots", slots);
    return menu;
  }

  @Test
  void craftingSlotsAreTheFirstNineGridSlots() {
    CraftingStationContainerMenu menu = createMenu(null);

    List<Slot> crafting = new CraftingStationEmiRecipeHandler().getCraftingSlots(menu);

    assertThat(crafting).hasSize(GRID);
    for (int i = 0; i < GRID; i++) {
      assertThat(crafting.get(i)).isSameAs(menu.slots.get(i));
    }
  }

  @Test
  void outputSlotIsTheResultSlotAfterTheGrid() {
    CraftingStationContainerMenu menu = createMenu(null);

    Slot output = new CraftingStationEmiRecipeHandler().getOutputSlot(menu);

    assertThat(output).isSameAs(menu.slots.get(GRID));
  }

  @Test
  void inputSourcesCoverGridFilledSideSlotsAndPlayerInventory() {
    ItemStack sideItem = new ItemStack(Items.IRON_INGOT, 3);
    CraftingStationContainerMenu menu = createMenu(sideItem);

    List<Slot> sources = new CraftingStationEmiRecipeHandler().getInputSources(menu);

    // 合成格全部在来源中
    for (int i = 0; i < GRID; i++) {
      assertThat(sources).contains(menu.slots.get(i));
    }
    // 成品槽不是来源
    assertThat(sources).doesNotContain(menu.slots.get(GRID));
    // 侧栏仅非空槽进入来源
    assertThat(sources).contains(menu.slots.get(GRID + 1));
    for (int i = 1; i < SIDE; i++) {
      assertThat(sources).doesNotContain(menu.slots.get(GRID + 1 + i));
    }
    // 玩家背包(末尾 36 槽)全部在来源中
    int playerStart = GRID + 1 + SIDE;
    for (int i = 0; i < PLAYER; i++) {
      assertThat(sources).contains(menu.slots.get(playerStart + i));
    }
    assertThat(sources).hasSize(GRID + 1 + PLAYER);
  }

  private static CraftingStationContainerMenu allocateMenu() {
    try {
      Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
      return (CraftingStationContainerMenu) unsafe.allocateInstance(CraftingStationContainerMenu.class);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setField(Object target, String name, Object value) {
    Class<?> type = target.getClass();
    while (type != null) {
      try {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
        return;
      } catch (NoSuchFieldException e) {
        type = type.getSuperclass();
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    throw new IllegalArgumentException("Missing field: " + name);
  }
}
