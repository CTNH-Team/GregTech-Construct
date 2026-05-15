package slimeknights.tconstruct.tables.menu;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.slot.PlayerSensitiveLazyResultSlot;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TinkerStationContainerMenuSocketStateTest extends BaseMcTest {
  @Test
  void refreshDisablesExtractionWhenToolChangesOrSelectionIsInvalid() {
    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    LazyResultContainer craftingResult = Mockito.mock(LazyResultContainer.class);
    when(tile.getCraftingResult()).thenReturn(craftingResult);
    when(tile.getInputCount()).thenReturn(6);
    when(tile.getItem(TinkerStationBlockEntity.TINKER_SLOT)).thenReturn(new ItemStack(Items.DIAMOND_PICKAXE), ItemStack.EMPTY);

    TinkerStationContainerMenu menu = allocateMenu();
    setField(menu, "tile", tile);

    menu.setSocketExtractionState(true, 1);
    assertThat(menu.isSocketExtractionMode()).isFalse();
    assertThat(menu.getSelectedSocket()).isEqualTo(-1);

    setField(menu, "socketExtractionMode", true);
    setField(menu, "selectedSocket", 0);
    menu.refreshSocketExtractionState();

    assertThat(menu.isSocketExtractionMode()).isFalse();
    assertThat(menu.getSelectedSocket()).isEqualTo(-1);
  }

  @Test
  void socketStateChangesInvalidateCachedResult() {
    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    LazyResultContainer craftingResult = Mockito.mock(LazyResultContainer.class);
    when(tile.getCraftingResult()).thenReturn(craftingResult);
    when(tile.getInputCount()).thenReturn(6);
    when(tile.getItem(TinkerStationBlockEntity.TINKER_SLOT)).thenReturn(new ItemStack(Items.DIAMOND_PICKAXE));

    TinkerStationContainerMenu menu = allocateMenu();
    setField(menu, "tile", tile);

    menu.setSocketExtractionState(true, 0);
    menu.setSocketExtractionState(true, 1);
    menu.refreshSocketExtractionState();

    verify(craftingResult, Mockito.times(2)).clearContent();
  }

  @Test
  void socketStateChangesResetPlayerSensitiveSlotCache() {
    TinkerStationBlockEntity tile = Mockito.mock(TinkerStationBlockEntity.class);
    LazyResultContainer craftingResult = Mockito.mock(LazyResultContainer.class);
    when(tile.getCraftingResult()).thenReturn(craftingResult);
    when(tile.getInputCount()).thenReturn(6);
    when(tile.getItem(TinkerStationBlockEntity.TINKER_SLOT)).thenReturn(new ItemStack(Items.DIAMOND_PICKAXE));

    PlayerSensitiveLazyResultSlot resultSlot = Mockito.mock(PlayerSensitiveLazyResultSlot.class);
    TinkerStationContainerMenu menu = allocateMenu();
    setField(menu, "tile", tile);
    setField(menu, "resultSlot", resultSlot);

    menu.setSocketExtractionState(true, 0);
    menu.setSocketExtractionState(true, 1);

    verify(resultSlot, Mockito.times(2)).invalidatePlayerResult();
  }

  private static TinkerStationContainerMenu allocateMenu() {
    try {
      Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
      return (TinkerStationContainerMenu) unsafe.allocateInstance(TinkerStationContainerMenu.class);
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
