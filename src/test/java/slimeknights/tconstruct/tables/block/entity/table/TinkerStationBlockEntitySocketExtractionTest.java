package slimeknights.tconstruct.tables.block.entity.table;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.inventory.TinkerStationContainerWrapper;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class TinkerStationBlockEntitySocketExtractionTest extends BaseMcTest {
  @AfterEach
  void resetBridge() {
    ApotheosisBridge.resetSocketHooks();
  }

  @Test
  void activeExtractionModeShortCircuitsRecipeEvaluationEvenWhenExtractionFails() {
    TinkerStationBlockEntity tile = allocateStation();
    setInventory(tile, new ItemStack(Items.DIAMOND_PICKAXE), 6);
    setField(tile, "craftingResult", new LazyResultContainer(tile));
    setField(tile, "inventoryWrapper", new TinkerStationContainerWrapper(tile));
    setField(tile, "level", Mockito.mock(net.minecraft.world.level.Level.class));

    Player player = Mockito.mock(Player.class, Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
    TinkerStationContainerMenu menu = Mockito.mock(TinkerStationContainerMenu.class);
    when(menu.hasActiveSocketExtraction()).thenReturn(true);
    when(menu.getSelectedSocket()).thenReturn(4);
    setField(player, "containerMenu", menu);

    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(new ItemStack(Items.EMERALD)), List.of()));

    ItemStack result = tile.calcResult(player);

    assertThat(result).isSameAs(ItemStack.EMPTY);
    assertThat(tile.getResult()).isNull();
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

  private static class FakeSocketHooks implements ApotheosisBridge.SocketHooks {
    private final List<ApotheosisBridge.SocketGem> gems;
    private final List<Component> tooltip;

    private FakeSocketHooks(List<ItemStack> gems, List<Component> tooltip) {
      this.gems = new ArrayList<>();
      for (int i = 0; i < gems.size(); i++) {
        this.gems.add(new ApotheosisBridge.SocketGem(i, gems.get(i)));
      }
      this.tooltip = tooltip;
    }

    @Override
    public boolean hasSocketedGems(ItemStack stack) {
      return !gems.isEmpty();
    }

    @Override
    public List<ItemStack> getSocketedGems(ItemStack stack) {
      return gems.stream().map(ApotheosisBridge.SocketGem::gem).toList();
    }

    @Override
    public List<ApotheosisBridge.SocketGem> getSocketedGemData(ItemStack stack) {
      return gems;
    }

    @Override
    public void appendTooltip(ItemStack stack, java.util.function.Consumer<Component> consumer) {
      tooltip.forEach(consumer);
    }

    @Override
    public ItemStack removeGem(ItemStack stack, int socketIndex) {
      return socketIndex >= 0 && socketIndex < gems.size() ? new ItemStack(Items.IRON_PICKAXE) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack copyGem(ItemStack stack, int socketIndex) {
      return socketIndex >= 0 && socketIndex < gems.size() ? gems.get(socketIndex).gem().copy() : ItemStack.EMPTY;
    }
  }
}
