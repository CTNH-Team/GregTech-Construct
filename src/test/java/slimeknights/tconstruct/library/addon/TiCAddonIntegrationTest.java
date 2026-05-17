package slimeknights.tconstruct.library.addon;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class TiCAddonIntegrationTest extends BaseMcTest {
  @Test
  void finderAlwaysIncludesBuiltInTiCAddon() {
    assertThat(TiCAddonFinder.getAddons())
      .isNotEmpty()
      .anyMatch(addon -> addon.addonModId().equals("tconstruct"));
  }

  @AfterEach
  void clearAddonCache() throws ReflectiveOperationException {
    Field cache = TiCAddonFinder.class.getDeclaredField("cache");
    cache.setAccessible(true);
    cache.set(null, null);

    Field modIdMap = TiCAddonFinder.class.getDeclaredField("modIdMap");
    modIdMap.setAccessible(true);
    modIdMap.set(null, new LinkedHashMap<>());
  }

  @Test
  void namedDynamicGeneratorsIncludeTiCAddonProviders() throws ReflectiveOperationException {
    injectAddon(new TestAddon());

    assertThat(entryNames("slimeknights.tconstruct.data.recipe.TiCDynamicRecipeGenerator"))
      .endsWith("AddonRecipeProvider");
    assertThat(entryNames("slimeknights.tconstruct.data.tinkering.TiCDynamicTinkeringGenerator"))
      .endsWith("AddonTinkeringProvider");
    assertThat(entryNames("slimeknights.tconstruct.data.tag.TiCDynamicTagGenerator"))
      .endsWith("AddonTagProvider");
    assertThat(entryNames("slimeknights.tconstruct.data.advancement.TiCDynamicAdvancementGenerator"))
      .endsWith("AddonAdvancementProvider");
    assertThat(entryNames("slimeknights.tconstruct.data.material.TiCDynamicMaterialGenerator"))
      .endsWith("AddonMaterialProvider");
  }

  @Test
  void resourceGeneratorIncludesTiCAddonProvider() throws ReflectiveOperationException {
    injectAddon(new TestAddon());

    Class<?> generator = Class.forName("slimeknights.tconstruct.data.resource.TiCDynamicResourceGenerator");
    Method createProviders = generator.getDeclaredMethod("createProviders");
    createProviders.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<Function<PackOutput, ? extends DataProvider>> providers =
      (List<Function<PackOutput, ? extends DataProvider>>) createProviders.invoke(null);

    PackOutput output = new PackOutput(Path.of("build", "test-tic-addon-resource-generator"));
    assertThat(providers)
      .extracting(factory -> factory.apply(output).getName())
      .endsWith("AddonResourceProvider");
  }

  private static List<String> entryNames(String className) throws ReflectiveOperationException {
    Class<?> generator = Class.forName(className);
    Method createProviderEntries = generator.getDeclaredMethod("createProviderEntries");
    createProviderEntries.setAccessible(true);
    List<?> entries = (List<?>) createProviderEntries.invoke(null);
    Method nameMethod = entries.get(0).getClass().getDeclaredMethod("name");
    nameMethod.setAccessible(true);
    return entries.stream().map(entry -> {
      try {
        return (String) nameMethod.invoke(entry);
      } catch (ReflectiveOperationException exception) {
        throw new AssertionError(exception);
      }
    }).toList();
  }

  private static void injectAddon(ITiCAddon addon) throws ReflectiveOperationException {
    List<ITiCAddon> current = TiCAddonFinder.getAddons();
    Field cache = TiCAddonFinder.class.getDeclaredField("cache");
    cache.setAccessible(true);
    cache.set(null, java.util.stream.Stream.concat(current.stream(), java.util.stream.Stream.of(addon)).toList());

    Map<String, ITiCAddon> addonsById = new LinkedHashMap<>();
    for (ITiCAddon existing : current) {
      addonsById.put(existing.addonModId(), existing);
    }
    addonsById.put(addon.addonModId(), addon);

    Field modIdMap = TiCAddonFinder.class.getDeclaredField("modIdMap");
    modIdMap.setAccessible(true);
    modIdMap.set(null, addonsById);
  }

  private static final class TestAddon implements ITiCAddon {
    @Override
    public String addonModId() {
      return "testaddon";
    }

    @Override
    public void registerDynamicRecipeProviders(DynamicProviderRegistrar registrar) {
      registrar.addProvider("AddonRecipeProvider", output -> new StubProvider("AddonRecipeProvider"));
    }

    @Override
    public void registerDynamicTinkeringProviders(DynamicProviderRegistrar registrar) {
      registrar.addProvider("AddonTinkeringProvider", output -> new StubProvider("AddonTinkeringProvider"));
    }

    @Override
    public void registerDynamicTagProviders(DynamicProviderRegistrar registrar) {
      registrar.addProvider("AddonTagProvider", output -> new StubProvider("AddonTagProvider"));
    }

    @Override
    public void registerDynamicAdvancementProviders(DynamicProviderRegistrar registrar) {
      registrar.addProvider("AddonAdvancementProvider", output -> new StubProvider("AddonAdvancementProvider"));
    }

    @Override
    public void registerDynamicMaterialProviders(DynamicProviderRegistrar registrar) {
      registrar.addProvider("AddonMaterialProvider", output -> new StubProvider("AddonMaterialProvider"));
    }

    @Override
    public void registerDynamicResourceProviders(DynamicProviderRegistrar registrar) {
      registrar.addProvider("AddonResourceProvider", output -> new StubProvider("AddonResourceProvider"));
    }
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
}
