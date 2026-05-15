package slimeknights.tconstruct.tables.block.entity.table;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.inventory.TinkerStationContainerWrapper;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class TinkerStationGemModeStateTest extends BaseMcTest {
  @Test
  void enteringGemModeCachesOrdinaryInputsAndClearsLiveInputs() {
    TinkerStationBlockEntity tile = createStation(6);
    tile.setItem(TinkerStationBlockEntity.TINKER_SLOT, new ItemStack(Items.DIAMOND_PICKAXE));
    tile.setItem(TinkerStationBlockEntity.INPUT_SLOT, new ItemStack(Items.DIRT, 3));
    tile.setItem(TinkerStationBlockEntity.INPUT_SLOT + 1, new ItemStack(Items.COBBLESTONE, 2));

    tile.enterGemMode();

    assertThat(tile.isGemMode()).isTrue();
    assertThat(tile.getCachedOrdinaryInputs().get(0).getItem()).isEqualTo(Items.DIRT);
    assertThat(tile.getCachedOrdinaryInputs().get(1).getItem()).isEqualTo(Items.COBBLESTONE);
    assertThat(tile.getItem(TinkerStationBlockEntity.INPUT_SLOT).isEmpty()).isTrue();
    assertThat(tile.getItem(TinkerStationBlockEntity.INPUT_SLOT + 1).isEmpty()).isTrue();
  }

  @Test
  void leavingGemModeRestoresOrdinaryInputs() {
    TinkerStationBlockEntity tile = createStation(6);
    tile.setItem(TinkerStationBlockEntity.TINKER_SLOT, new ItemStack(Items.DIAMOND_PICKAXE));
    tile.setItem(TinkerStationBlockEntity.INPUT_SLOT, new ItemStack(Items.DIRT, 3));

    tile.enterGemMode();
    tile.exitGemMode();

    assertThat(tile.isGemMode()).isFalse();
    assertThat(tile.getItem(TinkerStationBlockEntity.INPUT_SLOT).getItem()).isEqualTo(Items.DIRT);
    assertThat(tile.getCachedOrdinaryInputs()).allMatch(ItemStack::isEmpty);
  }

  @Test
  void gemModeStatePersistsAcrossSaveAndLoad() {
    TinkerStationBlockEntity original = createStation(6);
    original.setItem(TinkerStationBlockEntity.TINKER_SLOT, new ItemStack(Items.DIAMOND_PICKAXE));
    original.setItem(TinkerStationBlockEntity.INPUT_SLOT, new ItemStack(Items.DIRT, 3));
    original.setItem(TinkerStationBlockEntity.INPUT_SLOT + 1, new ItemStack(Items.COBBLESTONE, 2));
    original.enterGemMode();

    var tag = new net.minecraft.nbt.CompoundTag();
    original.saveSynced(tag);

    TinkerStationBlockEntity restored = createStation(6);
    restored.load(tag);

    assertThat(restored.isGemMode()).isTrue();
    assertThat(restored.getCachedOrdinaryInputs().get(0).getItem()).isEqualTo(Items.DIRT);
    assertThat(restored.getCachedOrdinaryInputs().get(1).getItem()).isEqualTo(Items.COBBLESTONE);
  }

  private static TinkerStationBlockEntity createStation(int size) {
    TinkerStationBlockEntity tile = allocateStation();
    setField(tile, "inventory", NonNullList.withSize(size, ItemStack.EMPTY));
    setField(tile, "cachedOrdinaryInputs", NonNullList.withSize(5, ItemStack.EMPTY));
    setField(tile, "craftingResult", new LazyResultContainer(tile));
    setField(tile, "inventoryWrapper", new TinkerStationContainerWrapper(tile));
    setField(tile, "material", IMaterial.UNKNOWN_ID);
    setField(tile, "stackSizeLimit", 64);
    setField(tile, "worldPosition", BlockPos.ZERO);
    return tile;
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
