package slimeknights.tconstruct.data.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeExistingFileHelperTest extends BaseMcTest {
  @Test
  void missingClientPngDoesNotPretendToExist() {
    assertThat(RuntimeExistingFileHelper.INSTANCE.exists(
      ResourceLocation.tryBuild("tconstruct", "generated_texture_that_should_not_exist"),
      PackType.CLIENT_RESOURCES,
      ".png",
      "textures"
    )).as("missing generated PNGs must be reported missing so runtime material textures are generated")
      .isFalse();
  }

  @Test
  void nonTextureReferencesRemainPermissive() {
    assertThat(RuntimeExistingFileHelper.INSTANCE.exists(
      ResourceLocation.tryBuild("tconstruct", "generated_model_reference"),
      PackType.CLIENT_RESOURCES,
      ".json",
      "models"
    )).as("runtime datagen still treats non-texture references as existing")
      .isTrue();
  }
}
