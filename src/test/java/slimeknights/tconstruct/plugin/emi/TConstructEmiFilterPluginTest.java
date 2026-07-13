package slimeknights.tconstruct.plugin.emi;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.casting.DisplayCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.container.ContainerFillingRecipe;
import slimeknights.tconstruct.plugin.emi.casting.CastingEmiRecipe;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TConstructEmiFilterPluginTest extends BaseMcTest {
  private static final RecipeType<?> TEST_TYPE = new RecipeType<>() {};

  @Test
  void collectsContainerFillingRecipeIds() {
    ResourceLocation containerFillingId = new ResourceLocation("tconstruct", "smeltery/casting/filling/bucket");
    ContainerFillingRecipe recipe = new ContainerFillingRecipe(
        null, containerFillingId, "", 1000, Items.BUCKET);

    assertThat(TConstructEmiFilterPlugin.getContainerFillingIds(List.of(recipe))).containsExactly(containerFillingId);
  }

  @Test
  void hidesContainerFillingDisplayRecipes() {
    ResourceLocation containerFillingId = new ResourceLocation("tconstruct", "smeltery/casting/filling/bucket");
    IDisplayableCastingRecipe recipe = displayRecipe(containerFillingId);

    CastingEmiRecipe emiRecipe = new CastingEmiRecipe(
        TConstruct.getResource("test/emi/container_filling"), null, recipe, false);

    assertThat(TConstructEmiFilterPlugin.isContainerFillingRecipe(emiRecipe, Set.of(containerFillingId))).isTrue();
  }

  @Test
  void keepsOrdinaryCastingDisplayRecipes() {
    ResourceLocation ordinaryCastingId = new ResourceLocation("tconstruct", "smeltery/casting/seared_brick");
    IDisplayableCastingRecipe recipe = displayRecipe(ordinaryCastingId);

    CastingEmiRecipe emiRecipe = new CastingEmiRecipe(
        TConstruct.getResource("test/emi/ordinary_casting"), null, recipe, false);

    assertThat(TConstructEmiFilterPlugin.isContainerFillingRecipe(emiRecipe, Set.of(
        new ResourceLocation("tconstruct", "smeltery/casting/filling/bucket")))).isFalse();
  }

  @Test
  void keepsDisplayRecipesWithoutParentIds() {
    IDisplayableCastingRecipe recipe = displayRecipe(null);
    CastingEmiRecipe emiRecipe = new CastingEmiRecipe(
        TConstruct.getResource("test/emi/no_parent"), null, recipe, false);

    assertThat(TConstructEmiFilterPlugin.isContainerFillingRecipe(emiRecipe, Set.of())).isFalse();
  }

  private static IDisplayableCastingRecipe displayRecipe(ResourceLocation id) {
    return new DisplayCastingRecipe(id, TEST_TYPE, List.of(new ItemStack(Items.BUCKET)),
        List.of(new FluidStack(Fluids.WATER, 1000)), new ItemStack(Items.WATER_BUCKET), 5, true);
  }
}
