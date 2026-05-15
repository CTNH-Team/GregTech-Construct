package slimeknights.tconstruct.tables.block.entity.table;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.inventory.TinkerStationContainerWrapper;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class TinkerStationBlockEntitySocketExtractionTest extends BaseMcTest {
  @AfterEach
  void resetBridge() {
    ApotheosisBridge.resetSocketHooks();
  }

  @Test
  void gemModePreviewReturnsCurrentToolWithoutExtractionCommitState() {
    TinkerStationBlockEntity tile = allocateStation();
    setInventory(tile, new ItemStack(Items.DIAMOND_PICKAXE), 6);
    setField(tile, "craftingResult", new LazyResultContainer(tile));
    setField(tile, "inventoryWrapper", new TinkerStationContainerWrapper(tile));
    setField(tile, "level", Mockito.mock(net.minecraft.world.level.Level.class));
    setField(tile, "lastSoundTick", new HashMap<>());

    tile.enterGemMode();

    ItemStack result = tile.calcResult(null);

    assertThat(result.getItem()).isEqualTo(Items.DIAMOND_PICKAXE);
    assertThat(tile.getItem(TinkerStationBlockEntity.TINKER_SLOT).getItem()).isEqualTo(Items.DIAMOND_PICKAXE);
  }

  private static TinkerStationBlockEntity allocateStation() {
    try {
      Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
      return (TinkerStationBlockEntity) unsafe.allocateInstance(TinkerStationBlockEntity.class);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setInventory(TinkerStationBlockEntity tile, ItemStack tool, int size) {
    NonNullList<ItemStack> inventory = NonNullList.withSize(size, ItemStack.EMPTY);
    inventory.set(TinkerStationBlockEntity.TINKER_SLOT, tool);
    setField(tile, "inventory", inventory);
    setField(tile, "blockState", net.minecraft.world.level.block.Blocks.ANVIL.defaultBlockState());
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
