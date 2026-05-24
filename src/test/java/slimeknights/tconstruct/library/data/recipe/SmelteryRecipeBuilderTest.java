package slimeknights.tconstruct.library.data.recipe;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer.OreRateType;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.core.registries.Registries.FLUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SmelteryRecipeBuilderTest extends BaseMcTest {
  @Test
  void conditionalOreByproductsDoNotResolveTagOutputForRecipeId() {
    List<FinishedRecipe> recipes = new ArrayList<>();
    ResourceLocation rawRecipeId = new ResourceLocation("tconstruct", "smeltery/melting/metal/test/raw");
    ResourceLocation rawBlockRecipeId = new ResourceLocation("tconstruct", "smeltery/melting/metal/test/raw_block");
    TagKey<Fluid> outputTag = TagKey.create(FLUID, new ResourceLocation("forge", "molten_test"));

    try (MockedStatic<TagPreference> tagPreference = Mockito.mockStatic(TagPreference.class)) {
      tagPreference.when(() -> TagPreference.getPreference(Mockito.any()))
        .thenThrow(new IllegalStateException("tag preference should not be resolved during recipe registration"));

      assertThatCode(() -> SmelteryRecipeBuilder.fluid(recipes::add, new ResourceLocation("tconstruct", "test"), outputTag)
        .temperature(700)
        .meltingFolder("smeltery/melting/metal")
        .oreRate(OreRateType.METAL)
        .baseUnit(FluidValues.INGOT)
        .ore(new TestByproduct())
        .rawOre()).doesNotThrowAnyException();
    }

    assertThat(recipes).hasSize(2);
    assertThat(recipes.get(0).getId()).isEqualTo(rawRecipeId);
    assertThat(recipes.get(1).getId()).isEqualTo(rawBlockRecipeId);
  }

  private static class TestByproduct implements IByproduct {
    @Override
    public String getName() {
      return "test_byproduct";
    }

    @Override
    public boolean isAlwaysPresent() {
      return false;
    }

    @Override
    public FluidOutput getFluid(float scale) {
      return FluidOutput.fromFluid(Fluids.LAVA, (int) (FluidValues.INGOT * scale));
    }

    @Override
    public OreRateType getOreRate() {
      return OreRateType.DEFAULT;
    }
  }
}
