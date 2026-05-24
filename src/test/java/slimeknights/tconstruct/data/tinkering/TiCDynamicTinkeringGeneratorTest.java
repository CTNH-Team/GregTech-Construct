package slimeknights.tconstruct.data.tinkering;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.fml.ModList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.data.EnchantmentToModifierProvider;
import slimeknights.tconstruct.tools.data.FluidEffectProvider;
import slimeknights.tconstruct.tools.data.ModifierProvider;
import slimeknights.tconstruct.tools.data.StationSlotLayoutProvider;
import slimeknights.tconstruct.tools.data.ToolDefinitionDataProvider;
import slimeknights.tconstruct.world.data.MobEquipmentProvider;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicTinkeringGeneratorTest extends BaseMcTest {
  @AfterEach
  void clearExternalProviders() throws ReflectiveOperationException {
    Field field = TiCDynamicTinkeringGenerator.class.getDeclaredField("ADDITIONAL_PROVIDER_ENTRIES");
    field.setAccessible(true);
    ((ArrayList<?>) field.get(null)).clear();
  }

  @Test
  void registerUsesSixProviderFactories() {
    RecordingRunner runner = new RecordingRunner();

    TiCDynamicTinkeringGenerator.register(runner);

    assertThat(runner.owner).isEqualTo("tconstruct-tinkering");
    assertThat(runner.providers).hasSize(6);
  }

  @Test
  void registerKeepsProviderOrder() {
    assertThat(TiCDynamicTinkeringGenerator.createProviderEntries()).extracting(TiCDynamicTinkeringGenerator.TinkeringProviderEntry::name).containsExactly(
      "ToolDefinitionDataProvider",
      "StationSlotLayoutProvider",
      "ModifierProvider",
      "FluidEffectProvider",
      "EnchantmentToModifierProvider",
      "MobEquipmentProvider"
    );
  }

  @Test
  void addProviderAppendsExternalProviderEntry() {
    TiCDynamicTinkeringGenerator.addProvider("ExternalTinkeringProvider", output -> new StubProvider("ExternalTinkeringProvider"));

    assertThat(TiCDynamicTinkeringGenerator.createProviderEntries())
      .extracting(TiCDynamicTinkeringGenerator.TinkeringProviderEntry::name)
      .containsExactly(
        "ToolDefinitionDataProvider",
        "StationSlotLayoutProvider",
        "ModifierProvider",
        "FluidEffectProvider",
        "EnchantmentToModifierProvider",
        "MobEquipmentProvider",
        "ExternalTinkeringProvider"
      );
  }

  @Test
  void registerIncludesExternalProviderFactory() {
    RecordingRunner runner = new RecordingRunner();
    PackOutput output = new PackOutput(Path.of("build", "test-dynamic-tinkering-generator"));

    TiCDynamicTinkeringGenerator.addProvider("ExternalTinkeringProvider", ignored -> new StubProvider("ExternalTinkeringProvider"));
    TiCDynamicTinkeringGenerator.register(runner);

    assertThat(runner.providers).hasSize(7);
    assertThat(runner.providers)
      .extracting(factory -> factory.apply(output).getName())
      .endsWith("ExternalTinkeringProvider");
  }

  @Test
  void createProvidersUsesExpectedProviderTypes() {
    try (MockedStatic<ModList> modList = Mockito.mockStatic(ModList.class)) {
      ModList modListInstance = Mockito.mock(ModList.class);
      modList.when(ModList::get).thenReturn(modListInstance);
      Mockito.when(modListInstance.isLoaded(Mockito.anyString())).thenReturn(false);

      PackOutput output = new PackOutput(Path.of("build", "test-tinkering-providers"));
      List<String> providerTypes = TiCDynamicTinkeringGenerator.createProviders().stream()
        .map(factory -> factory.apply(output).getClass())
        .map(Class::getSimpleName)
        .toList();

      assertThat(providerTypes).containsExactly(
        ToolDefinitionDataProvider.class.getSimpleName(),
        StationSlotLayoutProvider.class.getSimpleName(),
        ModifierProvider.class.getSimpleName(),
        FluidEffectProvider.class.getSimpleName(),
        EnchantmentToModifierProvider.class.getSimpleName(),
        MobEquipmentProvider.class.getSimpleName()
      );
    }
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

  private static final class RecordingRunner implements TiCDynamicTinkeringGenerator.TinkeringRunner {
    private String owner;
    private List<Function<PackOutput, ? extends DataProvider>> providers = List.of();

    @Override
    public void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers) {
      this.owner = owner;
      this.providers = providers;
    }
  }
}
