package slimeknights.tconstruct.data.resource;

import net.minecraft.data.PackOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.data.pack.TiCDynamicResourcePack;
import slimeknights.tconstruct.data.pack.TiCDynamicResourceRegistrar;
import slimeknights.tconstruct.library.addon.DynamicResourceRegistrar;
import slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.DyedArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.FirstArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.FixedArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.MaterialArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.TrimArmorTextureSupplier;
import slimeknights.tconstruct.library.data.RuntimeResourceProvider;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicResourceGeneratorTest extends BaseMcTest {
  private final TiCDynamicResourcePack pack = new TiCDynamicResourcePack("test");
  private static boolean armorTextureLoadersRegistered;

  @AfterEach
  void clearExternalProviders() throws ReflectiveOperationException {
    Field field = TiCDynamicResourceGenerator.class.getDeclaredField("ADDITIONAL_PROVIDER_ENTRIES");
    field.setAccessible(true);
    ((ArrayList<?>) field.get(null)).clear();
  }

  @BeforeEach
  void clearDynamicPack() {
    TiCDynamicResourcePack.clearClient();
  }

  @Test
  void registerKeepsProviderOrder() {
    assertThat(TiCDynamicResourceGenerator.createProviderEntries())
      .extracting(TiCDynamicResourceGenerator.ResourceProviderEntry::name)
      .containsExactly(
        "ModelSpriteProvider",
        "TinkerSpriteSourceProvider",
        "TinkerItemModelProvider",
        "TinkerBlockStateProvider",
        "RenderFluidProvider",
        "RenderItemProvider",
        "FluidTooltipProvider",
        "FluidTextureProvider",
        "FluidTextureCameraProvider",
        "FluidBucketModelProvider",
        "FluidBlockstateModelProvider",
        "ToolItemModelProvider",
        "MaterialRenderInfoProvider",
        "GeneratorPartTextureJsonGenerator",
        "MaterialPartTextureGenerator",
        "MaterialPaletteDebugGenerator",
        "ArmorModelProvider",
        "TrimMaterialPaletteGenerator"
      );
  }

  @Test
  void addProviderAppendsExternalWriter() {
    TiCDynamicResourceGenerator.addProvider("ExternalResourceWriter", registrar -> registrar.addResource(new ResourceLocation("example", "raw/generated.txt"), "ok".getBytes()));

    assertThat(TiCDynamicResourceGenerator.createProviderEntries())
      .extracting(TiCDynamicResourceGenerator.ResourceProviderEntry::name)
      .endsWith("ExternalResourceWriter");
  }

  @Test
  void registerIncludesExternalWriterAndStoresInMemory() {
    TiCDynamicResourceGenerator.addProvider("ExternalResourceWriter", registrar -> registrar.addResource(new ResourceLocation("example", "raw/generated.txt"), "ok".getBytes()));

    TiCDynamicResourceGenerator.createProviderEntries().stream()
      .filter(entry -> entry.name().equals("ExternalResourceWriter"))
      .forEach(entry -> entry.writer().accept(TiCDynamicResourceRegistrar.INSTANCE));

    assertThat(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("example", "raw/generated.txt"))).isNotNull();
    assertThat(pack.getNamespaces(PackType.CLIENT_RESOURCES)).contains("example");
  }

  @Test
  void runtimeProviderStoresModelBlockstateTextureAndRawResourceInMemory() {
    TiCDynamicResourceGenerator.runtime(TestRuntimeResourceProvider::new).accept(TiCDynamicResourceRegistrar.INSTANCE);

    assertThat(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("example", "models/item/generated.json"))).isNotNull();
    assertThat(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("example", "blockstates/generated.json"))).isNotNull();
    assertThat(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("example", "textures/generated.png"))).isNotNull();
    assertThat(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("example", "raw/generated.txt"))).isNotNull();
  }

  @Test
  void nativeArmorExtensionResourcesAreWrittenToTconstructNamespace() throws IOException {
    runProvider("TinkerItemModelProvider");
    registerArmorTextureSerializers();
    runProvider("ArmorModelProvider");

    assertThat(readClientResource(new ResourceLocation("tconstruct", "models/item/standard_helmet.json")))
      .contains("\"loader\":\"tconstruct:tool\"")
      .contains("tconstruct:item/armor/standard/helmet/linear")
      .doesNotContain("tconarmorex");
    assertThat(readClientResource(new ResourceLocation("tconstruct", "models/item/armor/standard/helmet_broken.json")))
      .contains("linear_broken")
      .doesNotContain("tconarmorex");
    assertThat(readClientResource(new ResourceLocation("tconstruct", "tinkering/armor_models/standard/helmet.json")))
      .contains("linear_")
      .doesNotContain("tconarmorex");
    assertThat(readClientResource(new ResourceLocation("tconstruct", "tinkering/armor_models/mix_forged_other/leggings.json")))
      .contains("armor_plate_1_")
      .contains("armor_mail_2_")
      .doesNotContain("tconarmorex");

    assertThat(pack.getNamespaces(PackType.CLIENT_RESOURCES)).doesNotContain("tconarmorex");
  }

  private static void runProvider(String name) {
    TiCDynamicResourceGenerator.createProviderEntries().stream()
      .filter(entry -> entry.name().equals(name))
      .findFirst()
      .orElseThrow()
      .writer()
      .accept(TiCDynamicResourceRegistrar.INSTANCE);
  }

  private static void registerArmorTextureSerializers() {
    if (armorTextureLoadersRegistered) {
      return;
    }
    ArmorTextureSupplier.LOADER.register(TConstruct.getResource("fixed"), FixedArmorTextureSupplier.LOADER);
    ArmorTextureSupplier.LOADER.register(TConstruct.getResource("dyed"), DyedArmorTextureSupplier.LOADER);
    ArmorTextureSupplier.LOADER.register(TConstruct.getResource("first_present"), FirstArmorTextureSupplier.LOADER);
    ArmorTextureSupplier.LOADER.register(TConstruct.getResource("material"), MaterialArmorTextureSupplier.Material.LOADER);
    ArmorTextureSupplier.LOADER.register(TConstruct.getResource("persistent_data"), MaterialArmorTextureSupplier.PersistentData.LOADER);
    ArmorTextureSupplier.LOADER.register(TConstruct.getResource("trim"), TrimArmorTextureSupplier.INSTANCE.getLoader());
    armorTextureLoadersRegistered = true;
  }

  private String readClientResource(ResourceLocation location) throws IOException {
    var supplier = pack.getResource(PackType.CLIENT_RESOURCES, location);
    assertThat(supplier).as(location.toString()).isNotNull();
    try (var input = supplier.get()) {
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static final class TestRuntimeResourceProvider implements RuntimeResourceProvider {
    private TestRuntimeResourceProvider(PackOutput output) {}

    @Override
    public void addToDynamicPack(DynamicResourceRegistrar registrar) {
      com.google.gson.JsonObject json = new com.google.gson.JsonObject();
      registrar.addItemModel(new ResourceLocation("example", "generated"), json);
      registrar.addBlockState(new ResourceLocation("example", "generated"), json);
      registrar.addTexture(new ResourceLocation("example", "generated"), "png".getBytes());
      registrar.addResource(new ResourceLocation("example", "raw/generated.txt"), "raw".getBytes());
    }
  }
}
