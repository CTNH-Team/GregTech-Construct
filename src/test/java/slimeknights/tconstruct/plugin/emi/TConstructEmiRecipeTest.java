package slimeknights.tconstruct.plugin.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TConstructEmiRecipeTest extends BaseMcTest {
  @Test
  void nbtDisplayStacksDoNotCollapseIntoTags() {
    ItemStack first = new ItemStack(Items.IRON_HELMET);
    first.getOrCreateTag().putString("material", "botania:terrasteel");
    ItemStack second = new ItemStack(Items.GOLDEN_HELMET);
    second.getOrCreateTag().putString("material", "botania:terrasteel");

    EmiIngredient ingredient = TConstructEmiRecipe.itemIngredient(List.of(first, second));

    assertThat(ingredient).isInstanceOf(ListEmiIngredient.class);
    assertThat(ingredient.getEmiStacks()).containsExactly(EmiStack.of(first), EmiStack.of(second));
  }
}