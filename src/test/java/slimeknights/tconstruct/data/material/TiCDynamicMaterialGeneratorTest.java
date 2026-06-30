package slimeknights.tconstruct.data.material;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.data.pack.TiCDynamicDataRegistrar;
import slimeknights.tconstruct.library.addon.DynamicDataRegistrar;
import slimeknights.tconstruct.library.data.RuntimeDataProvider;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
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

  private static final class TestRuntimeDataProvider implements RuntimeDataProvider {
    private TestRuntimeDataProvider(PackOutput output) {}

    @Override
    public void addToDynamicPack(DynamicDataRegistrar registrar) {
      registrar.addJson(new ResourceLocation("example", "materials/generated.json"), new com.google.gson.JsonObject());
    }
  }
}
