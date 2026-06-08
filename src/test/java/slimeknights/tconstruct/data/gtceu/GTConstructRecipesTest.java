package slimeknights.tconstruct.data.gtceu;

import net.minecraft.world.item.Item;
import org.junit.jupiter.api.Test;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GTConstructRecipesTest extends BaseMcTest {
  @Test
  void recipeTagFallsBackToLocalTagWhenCommonTagMissing() {
    net.minecraft.resources.ResourceLocation steeleafId = net.minecraft.resources.ResourceLocation.tryBuild("tconstruct", "steeleaf");
    TagKey<Fluid> localTag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), steeleafId);

    assertThat(GTConstructFluid.selectRecipeTag(null, localTag, steeleafId))
      .isEqualTo(localTag);
  }

  @Test
  void resolveOutputMaterialUsesCompatibilityAliases() {
    assertThat(GTConstructRecipes.resolveOutputMaterial("aluminium")).isEqualTo(MaterialIds.aluminum);
    assertThat(GTConstructRecipes.resolveOutputMaterial("ender")).isEqualTo(MaterialIds.enderPearl);
    assertThat(GTConstructRecipes.resolveOutputMaterial("fiery_liquid")).isEqualTo(MaterialIds.fiery);
  }

  @Test
  void supportedPartsAreFilteredByMaterialCompatibility() {
    MaterialId material = new MaterialId("test", "material");

    GTConstructRecipes.SolidifierPart supported = new GTConstructRecipes.SolidifierPart(
      "supported",
      () -> mock(Item.class),
      2,
      null,
      checked -> checked.equals(material)
    );
    GTConstructRecipes.SolidifierPart unsupported = new GTConstructRecipes.SolidifierPart(
      "unsupported",
      () -> mock(Item.class),
      2,
      null,
      checked -> false
    );

    assertThat(GTConstructRecipes.getSupportedParts(material, List.of(supported, unsupported)))
      .extracting(GTConstructRecipes.SolidifierPart::path)
      .containsExactly("supported");
  }

  @Test
  void defaultSolidifierPartsRemainAvailableForCoreMaterials() {
    assertThat(GTConstructRecipes.getSupportedParts(MaterialIds.iron, GTConstructRecipes.getDefaultSolidifierParts()))
      .extracting(GTConstructRecipes.SolidifierPart::path)
      .contains("pick_head", "tool_handle", "repair_kit");
  }
}
