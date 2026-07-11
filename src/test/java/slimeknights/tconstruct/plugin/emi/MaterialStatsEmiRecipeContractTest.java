package slimeknights.tconstruct.plugin.emi;

import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.plugin.emi.material.MaterialStatsEmiConstants;
import slimeknights.tconstruct.plugin.emi.material.MaterialStatsEmiRecipe;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialStatsEmiRecipeContractTest extends BaseMcTest {
  @Test
  void syntheticIdIsStableAndMaterialSpecific() {
    MaterialId material = new MaterialId("minecraft", "iron");

    assertThat(MaterialStatsEmiRecipe.syntheticId(MaterialStatsEmiConstants.HARVEST_STATS, material))
        .isEqualTo(TConstruct.getResource("/emi/harvest_stats/material/minecraft/iron"));
    assertThat(MaterialStatsEmiRecipe.syntheticId(MaterialStatsEmiConstants.HARVEST_STATS, material))
        .isEqualTo(MaterialStatsEmiRecipe.syntheticId(MaterialStatsEmiConstants.HARVEST_STATS, material));
    assertThat(MaterialStatsEmiRecipe.syntheticId(MaterialStatsEmiConstants.RANGED_STATS, material))
        .isNotEqualTo(MaterialStatsEmiRecipe.syntheticId(MaterialStatsEmiConstants.HARVEST_STATS, material));
  }
}
