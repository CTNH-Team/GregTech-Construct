package slimeknights.tconstruct.library.tools.item;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;

class ModifiableGTToolItemTest extends BaseMcTest {
  @Test
  void craftingRemainderDamagesToolInCreativeMode() {
    Player player = mock(Player.class);
    Mockito.when(player.isCreative()).thenReturn(true);
    ItemStack stack = new ItemStack(Items.IRON_PICKAXE);

    try (MockedStatic<ToolHelper> toolHelper = Mockito.mockStatic(ToolHelper.class)) {
      ModifiableGTToolItem.damageCraftingRemainder(stack, player);

      toolHelper.verify(() -> ToolHelper.damageItemWhenCrafting(same(stack), isNull()));
    }
  }
}
