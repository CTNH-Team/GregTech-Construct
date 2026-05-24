package slimeknights.tconstruct.data.advancement;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.common.data.AdvancementsProvider;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicAdvancementGeneratorTest extends BaseMcTest {
  @AfterEach
  void clearExternalProviders() throws ReflectiveOperationException {
    Field field = TiCDynamicAdvancementGenerator.class.getDeclaredField("ADDITIONAL_PROVIDER_ENTRIES");
    field.setAccessible(true);
    ((ArrayList<?>) field.get(null)).clear();
  }

  @Test
  void registerUsesSingleProviderFactory() {
    RecordingRunner runner = new RecordingRunner();

    TiCDynamicAdvancementGenerator.register(runner);

    assertThat(runner.owner).isEqualTo("tconstruct-advancements");
    assertThat(runner.providers).hasSize(1);
  }

  @Test
  void registerKeepsProviderOrder() {
    assertThat(TiCDynamicAdvancementGenerator.createProviderEntries()).extracting(TiCDynamicAdvancementGenerator.AdvancementProviderEntry::name).containsExactly(
      "AdvancementsProvider"
    );
  }

  @Test
  void addProviderAppendsExternalProviderEntry() {
    TiCDynamicAdvancementGenerator.addProvider("ExternalAdvancementProvider", output -> new StubProvider("ExternalAdvancementProvider"));

    assertThat(TiCDynamicAdvancementGenerator.createProviderEntries())
      .extracting(TiCDynamicAdvancementGenerator.AdvancementProviderEntry::name)
      .containsExactly(
        "AdvancementsProvider",
        "ExternalAdvancementProvider"
      );
  }

  @Test
  void registerIncludesExternalProviderFactory() {
    RecordingRunner runner = new RecordingRunner();
    PackOutput output = new PackOutput(Path.of("build", "test-dynamic-advancement-generator"));

    TiCDynamicAdvancementGenerator.addProvider("ExternalAdvancementProvider", ignored -> new StubProvider("ExternalAdvancementProvider"));
    TiCDynamicAdvancementGenerator.register(runner);

    assertThat(runner.providers).hasSize(2);
    assertThat(runner.providers)
      .extracting(factory -> factory.apply(output).getName())
      .endsWith("ExternalAdvancementProvider");
  }

  @Test
  void createProvidersUsesExpectedProviderType() {
    PackOutput output = new PackOutput(java.nio.file.Path.of("build", "test-advancements-providers"));
    List<String> providerTypes = TiCDynamicAdvancementGenerator.createProviders().stream()
      .map(factory -> factory.apply(output).getClass())
      .map(Class::getSimpleName)
      .toList();

    assertThat(providerTypes).containsExactly(
      AdvancementsProvider.class.getSimpleName()
    );
  }

  private static final class StubProvider implements DataProvider {
    private final String name;

    private StubProvider(String name) {
      this.name = name;
    }

    @Override
    public CompletableFuture<?> run(net.minecraft.data.CachedOutput output) {
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
      return name;
    }
  }

  private static final class RecordingRunner implements TiCDynamicAdvancementGenerator.AdvancementRunner {
    private String owner;
    private List<Function<PackOutput, ? extends DataProvider>> providers = List.of();

    @Override
    public void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers) {
      this.owner = owner;
      this.providers = providers;
    }
  }
}
