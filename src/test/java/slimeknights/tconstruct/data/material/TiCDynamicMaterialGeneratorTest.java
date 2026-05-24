package slimeknights.tconstruct.data.material;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicMaterialGeneratorTest extends BaseMcTest {
  @AfterEach
  void clearExternalProviders() throws ReflectiveOperationException {
    Field field = TiCDynamicMaterialGenerator.class.getDeclaredField("ADDITIONAL_PROVIDER_ENTRIES");
    field.setAccessible(true);
    ((ArrayList<?>) field.get(null)).clear();
  }

  @Test
  void registerUsesThreeProviderFactories() {
    RecordingRunner runner = new RecordingRunner();

    TiCDynamicMaterialGenerator.register(runner);

    assertThat(runner.owner).isEqualTo("tconstruct-materials");
    assertThat(runner.providers).hasSize(3);
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
    TiCDynamicMaterialGenerator.addProvider("ExternalMaterialProvider", output -> new StubProvider("ExternalMaterialProvider"));

    assertThat(TiCDynamicMaterialGenerator.createProviderEntries())
      .extracting(TiCDynamicMaterialGenerator.MaterialProviderEntry::name)
      .containsExactly(
        "MaterialDataProvider",
        "MaterialStatsDataProvider",
        "MaterialTraitsDataProvider",
        "ExternalMaterialProvider"
      );
  }

  @Test
  void registerIncludesExternalProviderFactory() {
    RecordingRunner runner = new RecordingRunner();
    PackOutput output = new PackOutput(Path.of("build", "test-dynamic-material-generator"));

    TiCDynamicMaterialGenerator.addProvider("ExternalMaterialProvider", ignored -> new StubProvider("ExternalMaterialProvider"));
    TiCDynamicMaterialGenerator.register(runner);

    assertThat(runner.providers).hasSize(4);
    assertThat(runner.providers)
      .extracting(factory -> factory.apply(output).getName())
      .endsWith("ExternalMaterialProvider");
  }

  private static final class StubProvider implements DataProvider {
    private final String name;

    private StubProvider(String name) {
      this.name = name;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
      return name;
    }
  }

  private static final class RecordingRunner implements TiCDynamicMaterialGenerator.MaterialRunner {
    private String owner;
    private List<Function<PackOutput, ? extends DataProvider>> providers = List.of();

    @Override
    public void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers) {
      this.owner = owner;
      this.providers = providers;
    }
  }
}
