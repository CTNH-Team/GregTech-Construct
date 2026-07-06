package slimeknights.tconstruct.data.resource;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.client.data.util.DataGenSpriteReader;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeExistingFileHelperTest extends BaseMcTest {
  @Test
  void defaultHelperTreatsMissingClientPngAsExisting() {
    assertThat(RuntimeExistingFileHelper.INSTANCE.exists(
      ResourceLocation.tryBuild("tconstruct", "generated_texture_that_should_not_exist"),
      PackType.CLIENT_RESOURCES,
      ".png",
      "textures"
    )).as("runtime datagen should not validate missing texture references unless active checking is requested")
      .isTrue();
  }

  @Test
  void activeHelperReportsMissingClientPng() {
    assertThat(RuntimeExistingFileHelper.INSTANCE.activeHelper().exists(
      ResourceLocation.tryBuild("tconstruct", "generated_texture_that_should_not_exist"),
      PackType.CLIENT_RESOURCES,
      ".png",
      "textures"
    )).as("active runtime datagen checks should still be able to detect missing generated textures")
      .isFalse();
  }

  @Test
  void activeHelperDoesNotUseDevelopmentResourcePacks() {
    assertThat(RuntimeExistingFileHelper.INSTANCE.activeHelper().exists(
      ResourceLocation.tryBuild("tconstruct", "block/ardite_ore"),
      PackType.CLIENT_RESOURCES,
      ".png",
      "textures"
    )).as("active runtime checks should match GTM by checking generated resources and the live resource manager only")
      .isFalse();
  }

  @Test
  void spriteReaderUsesActiveHelperForExistenceChecks() {
    DataGenSpriteReader reader = new DataGenSpriteReader(RuntimeExistingFileHelper.INSTANCE, "textures");

    assertThat(reader.exists(ResourceLocation.tryBuild("tconstruct", "generated_texture_that_should_not_exist")))
      .as("sprite generation should still generate missing textures with the permissive runtime helper")
      .isFalse();
    assertThat(reader.metadataExists(ResourceLocation.tryBuild("tconstruct", "generated_texture_that_should_not_exist")))
      .as("sprite generation should not treat every missing mcmeta as present")
      .isFalse();
  }

  @Test
  void spriteReaderCanReadPackagedSourceSpriteWhenActiveCheckCannotSeeIt() {
    DataGenSpriteReader reader = new DataGenSpriteReader(RuntimeExistingFileHelper.INSTANCE, "textures");
    ResourceLocation largePlate = ResourceLocation.tryBuild("tconstruct", "item/tool/parts/large_plate");

    assertThat(reader.exists(largePlate))
      .as("active output checks should not be relaxed by packaged development resources")
      .isFalse();

    NativeImage image = reader.readIfExists(largePlate);
    try {
      assertThat(image)
        .as("source sprite reads should still fall back to the packaged resource so dynamic texture generation can recolor it")
        .isNotNull();
    } finally {
      if (image != null) {
        image.close();
      }
    }
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
