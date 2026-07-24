package slimeknights.tconstruct.data.material;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.data.pack.TiCDynamicDataRegistrar;
import slimeknights.tconstruct.data.pack.DynamicPackOutput;
import slimeknights.tconstruct.library.addon.DynamicDataRegistrar;
import slimeknights.tconstruct.library.data.RuntimeDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialStatsDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialTraitsDataProvider;
import slimeknights.tconstruct.test.BaseMcTest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicMaterialGeneratorTest extends BaseMcTest {
  private final TiCDynamicDataPack pack = new TiCDynamicDataPack("test");

  @AfterEach
  void clearExternalProviders() throws ReflectiveOperationException {
    Field field = TiCDynamicMaterialGenerator.class.getDeclaredField("ADDITIONAL_PROVIDER_ENTRIES");
    field.setAccessible(true);
    ((ArrayList<?>) field.get(null)).clear();
  }

  @BeforeEach
  void clearDynamicPack() {
    TiCDynamicDataPack.clearServer();
  }

  @Test
  void registerKeepsProviderOrder() {
    assertThat(TiCDynamicMaterialGenerator.createProviderEntries())
      .extracting(TiCDynamicMaterialGenerator.MaterialProviderEntry::name)
      .containsExactly(
        "MaterialDataProvider",
        "MaterialStatsDataProvider",
        "MaterialTraitsDataProvider"
      );
  }

  @Test
  void addProviderAppendsExternalProviderEntry() {
    TiCDynamicMaterialGenerator.addProvider("ExternalMaterialWriter", registrar -> registrar.addData(new ResourceLocation("example", "materials/external.json"), "{}".getBytes()));

    assertThat(TiCDynamicMaterialGenerator.createProviderEntries())
      .extracting(TiCDynamicMaterialGenerator.MaterialProviderEntry::name)
      .containsExactly(
        "MaterialDataProvider",
        "MaterialStatsDataProvider",
        "MaterialTraitsDataProvider",
        "ExternalMaterialWriter"
      );
  }

  @Test
  void dataWriterStoresDataDirectlyInMemory() {
    TiCDynamicMaterialGenerator.dataWriter(TestRuntimeDataProvider::new).accept(TiCDynamicDataRegistrar.INSTANCE);

    ResourceLocation location = new ResourceLocation("example", "materials/generated.json");
    assertThat(pack.getResource(PackType.SERVER_DATA, location)).isNotNull();
    assertThat(pack.getNamespaces(PackType.SERVER_DATA)).contains("example");
  }

  @Test
  void materialTraitsWriteNativeArmorPartTraits() throws IOException {
    MaterialDataProvider materials = new MaterialDataProvider(DynamicPackOutput.dummy());
    new MaterialTraitsDataProvider(DynamicPackOutput.dummy(), materials)
      .addToDynamicPack(TiCDynamicDataRegistrar.INSTANCE);

    String woolTraits = readString(new ResourceLocation("tconstruct", "tinkering/materials/traits/wool.json"));
    assertThat(woolTraits)
      .contains("tconstruct:armor", "tconstruct:cushion")
      .doesNotContain("tconarmorex");

    String whitestoneTraits = readString(new ResourceLocation("tconstruct", "tinkering/materials/traits/whitestone.json"));
    assertThat(whitestoneTraits)
      .contains("tconstruct:armor", "tconstruct:malleability")
      .doesNotContain("tconarmorex");

    String leatherTraits = readString(new ResourceLocation("tconstruct", "tinkering/materials/traits/leather.json"));
    assertThat(leatherTraits)
      .contains("tconstruct:tanned")
      .doesNotContain("tconarmorex");

    String amethystBronzeTraits = readString(new ResourceLocation("tconstruct", "tinkering/materials/traits/amethyst_bronze.json"));
    assertThat(amethystBronzeTraits)
      .contains("tconstruct:armor", "tconstruct:crystal_lattice", "tconstruct:crystalizing", "tconstruct:crystal_solidity")
      .doesNotContain("tconarmorex");

    String hepatizonTraits = readString(new ResourceLocation("tconstruct", "tinkering/materials/traits/hepatizon.json"));
    assertThat(hepatizonTraits)
      .contains("tconstruct:armor", "tconstruct:recurrence")
      .contains("tconstruct:cast_helmet", "{\"name\":\"tconstruct:recurrence\",\"level\":2}")
      .doesNotContain("tconstruct:recurrent_protection")
      .doesNotContain("tconarmorex");

    String knightmetalTraits = readString(new ResourceLocation("tconstruct", "tinkering/materials/traits/knightmetal.json"));
    assertThat(knightmetalTraits)
      .contains("tconstruct:cast_helmet", "tconstruct:guarding", "tconstruct:plating", "tconstruct:hardening")
      .contains("{\"name\":\"tconstruct:guarding\",\"level\":2}")
      .doesNotContain("tconstruct:stalwart")
      .doesNotContain("tconarmorex");

    assertThat(amethystBronzeTraits)
      .contains("tconstruct:cast_helmet", "tconstruct:crystal_lattice", "tconstruct:crystalizing", "tconstruct:crystal_solidity")
      .contains("{\"name\":\"tconstruct:crystal_lattice\",\"level\":2}")
      .doesNotContain("tconstruct:crystalstrike")
      .doesNotContain("tconarmorex");


  }

  @Test
  void materialStatsWriteNativeArmorLiningMaterials() throws IOException {
    MaterialDataProvider materials = new MaterialDataProvider(DynamicPackOutput.dummy());
    new MaterialStatsDataProvider(DynamicPackOutput.dummy(), materials)
      .addToDynamicPack(TiCDynamicDataRegistrar.INSTANCE);

    String leatherStats = readString(new ResourceLocation("tconstruct", "tinkering/materials/stats/leather.json"));
    assertThat(leatherStats)
      .contains("tconstruct:linear")
      .doesNotContain("tconarmorex");

    String woolStats = readString(new ResourceLocation("tconstruct", "tinkering/materials/stats/wool.json"));
    assertThat(woolStats)
      .contains("tconstruct:linear")
      .doesNotContain("tconarmorex");

    String dragonScaleStats = readString(new ResourceLocation("tconstruct", "tinkering/materials/stats/dragon_scale.json"));
    assertThat(dragonScaleStats)
      .contains("tconstruct:maille", "\"durability\":-0.1", "\"armor\":0.05", "\"armor_strength\":0.1")
      .doesNotContain("tconarmorex");


  }

  @Test
  void materialStatsWriteNativeArmorPartDefaults() throws IOException {
    MaterialDataProvider materials = new MaterialDataProvider(DynamicPackOutput.dummy());
    new MaterialStatsDataProvider(DynamicPackOutput.dummy(), materials)
      .addToDynamicPack(TiCDynamicDataRegistrar.INSTANCE);

    String manyullynStats = readString(new ResourceLocation("tconstruct", "tinkering/materials/stats/manyullyn.json"));
    assertThat(manyullynStats)
      .contains("tconstruct:plating_helmet", "\"durability\":563", "\"armor\":2.5", "\"armor_strength\":3.0", "\"toughness\":1.25")
      .contains("tconstruct:armor_plate", "\"durability\":0.2", "\"armor\":0.05", "\"reduction\":0.6", "\"protection\":0.05")
      .contains("tconstruct:massive_cast_chestplate", "\"durability\":820", "\"armor\":7.0", "\"reduction\":0.75")
      .doesNotContain("tconarmorex");


  }

  private String readString(ResourceLocation location) throws IOException {
    var resource = pack.getResource(PackType.SERVER_DATA, location);
    assertThat(resource).isNotNull();
    try (var input = resource.get()) {
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static final class TestRuntimeDataProvider implements RuntimeDataProvider {
    private TestRuntimeDataProvider(PackOutput output) {}

    @Override
    public void addToDynamicPack(DynamicDataRegistrar registrar) {
      registrar.addJson(new ResourceLocation("example", "materials/generated.json"), new com.google.gson.JsonObject());
    }
  }
}
