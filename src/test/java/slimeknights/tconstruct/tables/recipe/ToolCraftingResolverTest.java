package slimeknights.tconstruct.tables.recipe;

import com.gregtechceu.gtceu.api.item.CustomToolIngredientHelper;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("DataFlowIssue")
class ToolCraftingResolverTest extends BaseMcTest {
  @Test
  void creativeCraftingStillWritesDamagedCopyBackToDedicatedSlot() {
    CraftingStationBlockEntity station = mock(CraftingStationBlockEntity.class);
    Player player = mock(Player.class);
    Mockito.when(player.isCreative()).thenReturn(true);
    ItemStack original = new ItemStack(Items.IRON_PICKAXE);
    try (MockedStatic<CustomToolIngredientHelper> customTools = Mockito.mockStatic(CustomToolIngredientHelper.class);
         MockedStatic<ToolHelper> toolHelper = Mockito.mockStatic(ToolHelper.class)) {
      customTools.when(() -> CustomToolIngredientHelper.damageTool(
        any(ItemStack.class), isNull(), isNull(), eq(1))).thenReturn(false);
      toolHelper.when(() -> ToolHelper.damageItemWhenCrafting(any(ItemStack.class), isNull()))
        .thenAnswer(invocation -> {
          ItemStack damaged = invocation.getArgument(0);
          damaged.getOrCreateTag().putBoolean("TestDamage", true);
          return null;
        });

      ToolCraftingResolver.damageToolStack(station, 2, original, null, player);

      ArgumentCaptor<ItemStack> damagedCaptor = ArgumentCaptor.forClass(ItemStack.class);
      verify(station).setItem(eq(CraftingStationBlockEntity.TOOL_SLOT_START + 2), damagedCaptor.capture());
      toolHelper.verify(() -> ToolHelper.damageItemWhenCrafting(any(ItemStack.class), isNull()), times(1));
      assertThat(damagedCaptor.getValue()).isNotSameAs(original);
      assertThat(damagedCaptor.getValue().getOrCreateTag().getBoolean("TestDamage")).isTrue();
      assertThat(original.getOrCreateTag().getBoolean("TestDamage")).isFalse();
    }
  }

  @Test
  void craftingWithGTToolPlaysSoundUsingStoredDamagedCopy() {
    CraftingStationBlockEntity station = mock(CraftingStationBlockEntity.class);
    Player player = mock(Player.class);
    Item item = mock(Item.class, Mockito.withSettings().extraInterfaces(IGTTool.class));
    IGTTool tool = (IGTTool) item;
    ItemStack original = mock(ItemStack.class);
    ItemStack damaged = mock(ItemStack.class);
    Mockito.when(original.copy()).thenReturn(damaged);
    Mockito.when(damaged.getItem()).thenReturn(item);

    try (MockedStatic<CustomToolIngredientHelper> customTools = Mockito.mockStatic(CustomToolIngredientHelper.class);
         MockedStatic<ToolHelper> toolHelper = Mockito.mockStatic(ToolHelper.class)) {
      customTools.when(() -> CustomToolIngredientHelper.damageTool(damaged, null, player, 1)).thenReturn(false);

      ToolCraftingResolver.damageToolStack(station, 4, original, null, player);

      toolHelper.verify(() -> ToolHelper.damageItemWhenCrafting(damaged, player), times(1));
      verify(tool).playCraftingSound(player, damaged);
      verify(station).setItem(CraftingStationBlockEntity.TOOL_SLOT_START + 4, damaged);
    }
  }
}
