package slimeknights.tconstruct.data.resource;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TiCDynamicResourceGeneratorTest extends BaseMcTest {
  @AfterEach
  void clearExternalProviders() throws ReflectiveOperationException {
    Field field = TiCDynamicResourceGenerator.class.getDeclaredField("ADDITIONAL_PROVIDERS");
    field.setAccessible(true);
    ((ArrayList<?>) field.get(null)).clear();
  }

  @Test
  void addProviderAppendsExternalProviderFactory() {
    PackOutput output = new PackOutput(Path.of("build", "test-dynamic-resource-generator"));

    TiCDynamicResourceGenerator.addProvider(ignored -> new StubProvider("ExternalResourceProvider"));

    assertThat(TiCDynamicResourceGenerator.createProviders())
      .extracting(factory -> factory.apply(output).getName())
      .endsWith("ExternalResourceProvider");
  }

  @Test
  void registerIncludesExternalProviderFactory() {
    RecordingRunner runner = new RecordingRunner();
    PackOutput output = new PackOutput(Path.of("build", "test-dynamic-resource-register"));

    TiCDynamicResourceGenerator.addProvider(ignored -> new StubProvider("ExternalResourceProvider"));
    TiCDynamicResourceGenerator.register(runner);

    assertThat(runner.providers).hasSize(19);
    assertThat(runner.providers)
      .extracting(factory -> factory.apply(output).getName())
      .endsWith("ExternalResourceProvider");
  }

  @Test
  void modelSpriteProviderRunsWithDynamicExistingFileHelper(@TempDir Path outputRoot) {
    List<Function<PackOutput, ? extends DataProvider>> providers = TiCDynamicResourceGenerator.createProviders();
    PackOutput output = new PackOutput(outputRoot);
    CachedOutput cachedOutput = Mockito.mock(CachedOutput.class);

    DataProvider modelSpriteProvider = providers.get(0).apply(output);

    assertThatCode(() -> modelSpriteProvider.run(cachedOutput).join())
      .doesNotThrowAnyException();
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

  private static final class RecordingRunner implements TiCDynamicResourceGenerator.ResourceRunner {
    private String owner;
    private List<Function<PackOutput, ? extends DataProvider>> providers = List.of();

    @Override
    public void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers) {
      this.owner = owner;
      this.providers = providers;
    }
  }
}
