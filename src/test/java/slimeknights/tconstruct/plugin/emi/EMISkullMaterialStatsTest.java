package slimeknights.tconstruct.plugin.emi;

import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.plugin.emi.material.SkullStatsEmiRecipe;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class EMISkullMaterialStatsTest extends BaseMcTest {
  @Test
  void missingRecipeManagerProducesNoCastInputs() {
    assertThat(SkullStatsEmiRecipe.findSkullParts(IMaterial.UNKNOWN, null)).isEmpty();
  }
}
