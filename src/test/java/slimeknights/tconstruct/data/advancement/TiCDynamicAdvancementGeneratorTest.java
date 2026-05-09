package slimeknights.tconstruct.data.advancement;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.common.data.AdvancementsProvider;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicAdvancementGeneratorTest extends BaseMcTest {
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
