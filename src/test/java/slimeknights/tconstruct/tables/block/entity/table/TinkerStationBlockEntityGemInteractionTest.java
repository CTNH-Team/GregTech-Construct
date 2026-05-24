package slimeknights.tconstruct.tables.block.entity.table;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.inventory.TinkerStationContainerWrapper;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TinkerStationBlockEntityGemInteractionTest extends BaseMcTest {
  @AfterEach
  void resetBridge() {
    ApotheosisBridge.resetSocketHooks();
  }

  @Test
  void successfulInsertUpdatesToolPreviewImmediately() {
    TinkerStationBlockEntity tile = createStation(6);
    tile.setItem(TinkerStationBlockEntity.TINKER_SLOT, new ItemStack(Items.DIAMOND_PICKAXE));
    tile.enterGemMode();
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(
      ApotheosisBridge.SocketGem.empty(0),
      ApotheosisBridge.SocketGem.empty(1)
    ), new ItemStack(Items.NETHERITE_PICKAXE), ItemStack.EMPTY));

    ItemStack remainder = tile.insertGem(0, new ItemStack(Items.DIAMOND));
    ItemStack preview = tile.calcResult(null);

    assertThat(remainder.isEmpty()).isTrue();
    assertThat(tile.getItem(TinkerStationBlockEntity.TINKER_SLOT).getItem()).isEqualTo(Items.NETHERITE_PICKAXE);
    assertThat(preview.getItem()).isEqualTo(Items.NETHERITE_PICKAXE);
  }

  @Test
  void successfulRemoveReturnsGemImmediately() {
    TinkerStationBlockEntity tile = createStation(6);
    tile.setItem(TinkerStationBlockEntity.TINKER_SLOT, new ItemStack(Items.DIAMOND_PICKAXE));
    tile.enterGemMode();
    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(
      new ApotheosisBridge.SocketGem(0, new ItemStack(Items.EMERALD)),
      ApotheosisBridge.SocketGem.empty(1)
    ), ItemStack.EMPTY, new ItemStack(Items.IRON_PICKAXE)));

    ItemStack removed = tile.removeGem(0);
    ItemStack preview = tile.calcResult(null);

    assertThat(removed.getItem()).isEqualTo(Items.EMERALD);
    assertThat(tile.getItem(TinkerStationBlockEntity.TINKER_SLOT).getItem()).isEqualTo(Items.IRON_PICKAXE);
    assertThat(preview.getItem()).isEqualTo(Items.IRON_PICKAXE);
  }

  @Test
  void gemModeOutputRemainsPreviewOnly() {
    TinkerStationBlockEntity tile = createStation(6);
    tile.setItem(TinkerStationBlockEntity.TINKER_SLOT, new ItemStack(Items.DIAMOND_PICKAXE));
    tile.enterGemMode();

    ItemStack preview = tile.calcResult(null);

    assertThat(preview.getItem()).isEqualTo(Items.DIAMOND_PICKAXE);
    assertThat(tile.getItem(TinkerStationBlockEntity.TINKER_SLOT).getItem()).isEqualTo(Items.DIAMOND_PICKAXE);
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
    setField(tile, "blockState", net.minecraft.world.level.block.Blocks.ANVIL.defaultBlockState());
    setField(tile, "level", Mockito.mock(net.minecraft.world.level.Level.class));
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

  private static final class FakeSocketHooks implements ApotheosisBridge.SocketHooks {
    private final List<ApotheosisBridge.SocketGem> sockets;
    private final ItemStack insertResult;
    private final ItemStack removeResult;

    private FakeSocketHooks(List<ApotheosisBridge.SocketGem> sockets, ItemStack insertResult, ItemStack removeResult) {
      this.sockets = new ArrayList<>(sockets);
      this.insertResult = insertResult;
      this.removeResult = removeResult;
    }

    @Override
    public boolean hasSocketedGems(ItemStack stack) {
      return this.sockets.stream().anyMatch(ApotheosisBridge.SocketGem::isFilled);
    }

    @Override
    public int getSocketCount(ItemStack stack) {
      return this.sockets.size();
    }

    @Override
    public List<ApotheosisBridge.SocketGem> getSocketedGemData(ItemStack stack) {
      return this.sockets;
    }

    @Override
    public void appendTooltip(ItemStack stack, java.util.function.Consumer<Component> consumer) {}

    @Override
    public ItemStack removeGem(ItemStack stack, int socketIndex) {
      return socketIndex == 0 ? this.removeResult.copy() : ItemStack.EMPTY;
    }

    @Override
    public boolean canInsertGem(ItemStack tool, int rawSocketIndex, ItemStack gem) {
      return rawSocketIndex == 0 && gem.getItem() == Items.DIAMOND;
    }

    @Override
    public ItemStack insertGem(ItemStack tool, int rawSocketIndex, ItemStack gem) {
      return rawSocketIndex == 0 ? this.insertResult.copy() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack copyGem(ItemStack stack, int socketIndex) {
      return socketIndex == 0 && this.sockets.get(0).isFilled() ? this.sockets.get(0).gem().copy() : ItemStack.EMPTY;
    }
  }
}
