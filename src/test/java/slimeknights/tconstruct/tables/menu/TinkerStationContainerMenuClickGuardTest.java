package slimeknights.tconstruct.tables.menu;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.slot.TinkerStationSlot;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class TinkerStationContainerMenuClickGuardTest extends BaseMcTest {
  @Test
  void gemModeSocketSlotsAreBlockedFromGenericContainerClicks() {
    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    when(tile.isGemMode()).thenReturn(true);
    when(tile.getCraftingResult()).thenReturn(Mockito.mock(LazyResultContainer.class));
    when(tile.getItem(TinkerStationBlockEntity.TINKER_SLOT)).thenReturn(new ItemStack(Items.DIAMOND_PICKAXE));

    TestMenu menu = allocateMenu();
    setMenuTile(menu, tile);
    setField(menu, "slots", NonNullList.create());
    menu.slots.clear();
    menu.slots.add(new net.minecraft.world.inventory.Slot(new SimpleContainer(1), 0, 0, 0));
    TinkerStationSlot gemSlot = new TinkerStationSlot(tile, TinkerStationBlockEntity.INPUT_SLOT, 0, 0);
    gemSlot.setMenu(menu);
    menu.slots.add(gemSlot);

    assertThat(menu.isGemModeSocketSlot(1)).isTrue();
  }

  @Test
  void nonSocketSlotsAreNotBlockedByGemModeGuard() {
    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    when(tile.isGemMode()).thenReturn(true);

    TestMenu menu = allocateMenu();
    setMenuTile(menu, tile);
    setField(menu, "slots", NonNullList.create());
    menu.slots.clear();
    menu.slots.add(new net.minecraft.world.inventory.Slot(new SimpleContainer(1), 0, 0, 0));

    assertThat(menu.isGemModeSocketSlot(0)).isFalse();
  }

  @Test
  void gemModeQuickMoveClickUsesMenuQuickMoveHandler() {
    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    Player player = Mockito.mock(Player.class, Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));

    when(tile.isGemMode()).thenReturn(true);

    TestMenu menu = allocateMenu();
    setMenuTile(menu, tile);
    setField(menu, "slots", NonNullList.create());
    menu.slots.clear();
    menu.slots.add(new net.minecraft.world.inventory.Slot(new SimpleContainer(1), 0, 0, 0));

    TestMenu spy = Mockito.spy(menu);

    spy.clicked(0, 0, ClickType.QUICK_MOVE, player);

    verify(spy).quickMoveStack(player, 0);
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
    try {
      Field field = net.minecraft.world.inventory.AbstractContainerMenu.class.getDeclaredField(name);
      field.setAccessible(true);
      field.set(target, value);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setMenuTile(Object target, Object value) {
    Class<?> type = target.getClass();
    while (type != null) {
      try {
        Field field = type.getDeclaredField("tile");
        field.setAccessible(true);
        field.set(target, value);
        return;
      } catch (NoSuchFieldException e) {
        type = type.getSuperclass();
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    throw new IllegalArgumentException("Missing field: tile");
  }

  static class TestMenu extends TinkerStationContainerMenu {
    private TestMenu() {
      super(0, (net.minecraft.world.entity.player.Inventory) null, (slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity) null);
    }
  }
}
