package slimeknights.tconstruct.tables.menu;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.slot.PlayerSensitiveLazyResultSlot;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TinkerStationContainerMenuQuickMoveTest extends BaseMcTest {
  @Test
  void quickMoveUsesDisplayedExtractionResultAndConsumesToolState() {
    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    LazyResultContainer craftingResult = Mockito.mock(LazyResultContainer.class);
    Player player = Mockito.mock(Player.class, Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
    PlayerSensitiveLazyResultSlot resultSlot = Mockito.mock(PlayerSensitiveLazyResultSlot.class);
    ItemStack displayed = new ItemStack(Items.IRON_PICKAXE);

    when(tile.getCraftingResult()).thenReturn(craftingResult);
    when(resultSlot.hasItem()).thenReturn(true);
    when(resultSlot.getItem()).thenReturn(displayed);

    TestMenu menu = allocateMenu();
    setField(menu, "tile", tile);
    setField(menu, "resultSlot", resultSlot);
    setField(menu, "slots", NonNullList.create());
    setField(menu, "subContainers", new ArrayList<>());
    menu.slots.clear();
    menu.slots.add(resultSlot);

    TestMenu spy = Mockito.spy(menu);
    doReturn(false).when(spy).testMoveToPlayerInventory(any(ItemStack.class));

    ItemStack moved = spy.quickMoveStack(player, 0);

    assertThat(moved.getItem()).isEqualTo(Items.IRON_PICKAXE);
    verify(tile).onCraft(eq(player), argThat(stack -> stack.getItem() == Items.IRON_PICKAXE && stack.getCount() == 1), eq(displayed.getCount()));
    verify(craftingResult).clearContent();
    verify(resultSlot, never()).remove(any(Integer.class));
  }

  @Test
  void quickMovePassesOriginalCraftedStackEvenWhenTransferConsumesWorkingStack() {
    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    LazyResultContainer craftingResult = Mockito.mock(LazyResultContainer.class);
    Player player = Mockito.mock(Player.class, Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
    PlayerSensitiveLazyResultSlot resultSlot = Mockito.mock(PlayerSensitiveLazyResultSlot.class);
    ItemStack displayed = new ItemStack(Items.IRON_PICKAXE);

    when(tile.getCraftingResult()).thenReturn(craftingResult);
    when(resultSlot.hasItem()).thenReturn(true);
    when(resultSlot.getItem()).thenReturn(displayed);

    TestMenu menu = allocateMenu();
    setField(menu, "tile", tile);
    setField(menu, "resultSlot", resultSlot);
    setField(menu, "slots", NonNullList.create());
    setField(menu, "subContainers", new ArrayList<>());
    menu.slots.clear();
    menu.slots.add(resultSlot);

    TestMenu spy = Mockito.spy(menu);
    doReturn(false).when(spy).testMoveToPlayerInventory(any(ItemStack.class));
    Mockito.doAnswer(invocation -> {
      ItemStack moved = invocation.getArgument(0);
      moved.setCount(0);
      return false;
    }).when(spy).moveToPlayerInventory(any(ItemStack.class));

    ItemStack moved = spy.quickMoveStack(player, 0);

    assertThat(moved.getItem()).isEqualTo(Items.IRON_PICKAXE);
    verify(tile).onCraft(eq(player), argThat(stack -> stack.getItem() == Items.IRON_PICKAXE && stack.getCount() == 1), eq(1));
  }

  @Test
  void quickMoveRejectsResultSlotWhileGemModeIsActive() {
    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    LazyResultContainer craftingResult = Mockito.mock(LazyResultContainer.class);
    Player player = Mockito.mock(Player.class, Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
    PlayerSensitiveLazyResultSlot resultSlot = Mockito.mock(PlayerSensitiveLazyResultSlot.class);

    when(tile.getCraftingResult()).thenReturn(craftingResult);
    when(tile.isGemMode()).thenReturn(true);
    when(resultSlot.hasItem()).thenReturn(true);
    when(resultSlot.getItem()).thenReturn(new ItemStack(Items.DIAMOND_PICKAXE));

    TestMenu menu = allocateMenu();
    setField(menu, "tile", tile);
    setField(menu, "resultSlot", resultSlot);
    setField(menu, "slots", NonNullList.create());
    setField(menu, "subContainers", new ArrayList<>());
    menu.slots.clear();
    menu.slots.add(resultSlot);

    ItemStack moved = menu.quickMoveStack(player, 0);

    assertThat(moved.isEmpty()).isTrue();
    verify(tile, never()).onCraft(any(), any(), anyInt());
    verify(craftingResult, never()).clearContent();
  }

  @Test
  void quickMoveRejectsPlayerInventoryShiftClickWhileGemModeIsActive() {
    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    Player player = Mockito.mock(Player.class, Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
    Slot playerSlot = Mockito.mock(Slot.class);

    when(tile.isGemMode()).thenReturn(true);
    when(playerSlot.hasItem()).thenReturn(true);
    when(playerSlot.getItem()).thenReturn(new ItemStack(Items.DIAMOND));

    TestMenu menu = allocateMenu();
    setField(menu, "tile", tile);
    setField(menu, "slots", NonNullList.create());
    setField(menu, "subContainers", new ArrayList<>());
    setField(menu, "slotContainerMap", new java.util.HashMap<>());
    menu.slots.clear();
    menu.slots.add(playerSlot);

    TestMenu spy = Mockito.spy(menu);

    ItemStack moved = spy.quickMoveStack(player, 0);

    assertThat(moved.isEmpty()).isTrue();
    verify(spy, never()).testMoveToPlayerInventory(any(ItemStack.class));
    verify(spy, never()).moveToPlayerInventory(any(ItemStack.class));
  }

  private static TestMenu allocateMenu() {
    try {
      Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
      return (TestMenu) unsafe.allocateInstance(TestMenu.class);
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

  public static class TestMenu extends TinkerStationContainerMenu {
    private TestMenu() {
      super(0, (net.minecraft.world.entity.player.Inventory) null, (slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity) null);
    }

    @Override
    protected boolean moveToPlayerInventory(ItemStack stack) {
      return testMoveToPlayerInventory(stack);
    }

    boolean testMoveToPlayerInventory(ItemStack stack) {
      return super.moveToPlayerInventory(stack);
    }
  }
}
