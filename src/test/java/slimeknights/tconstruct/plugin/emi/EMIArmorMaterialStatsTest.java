package slimeknights.tconstruct.plugin.emi;

import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.plugin.emi.material.ArmorStatsEmiRecipe;
import slimeknights.tconstruct.test.BaseMcTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EMIArmorMaterialStatsTest extends BaseMcTest {
  @Test
  void armorRecipeUsesAuthoritativeTag() {
    assertThat(ArmorStatsEmiRecipe.PART_TAG).isEqualTo(TinkerTags.Items.ARMOR);
  }

  @Test
  void generatedArmorTagsContainAllCurrentFamilies() throws Exception {
    String tags = List.of(
        "src/generated/resources/data/forge/tags/items/armors/helmets.json",
        "src/generated/resources/data/forge/tags/items/armors/chestplates.json",
        "src/generated/resources/data/forge/tags/items/armors/leggings.json",
        "src/generated/resources/data/forge/tags/items/armors/boots.json")
        .stream()
        .map(Path::of)
        .map(path -> {
          try {
            return Files.readString(path);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .reduce("", String::concat);

    assertThat(tags).contains(
        "travelers_", "plate_", "slime_", "standard_", "knights_", "explorers_",
        "composite_", "forged_", "mix_");
  }
}
