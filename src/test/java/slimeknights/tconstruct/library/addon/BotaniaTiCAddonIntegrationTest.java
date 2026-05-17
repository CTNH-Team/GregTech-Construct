package slimeknights.tconstruct.library.addon;

import com.google.common.hash.HashCode;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.common.data.tags.MaterialTagProvider;
import slimeknights.tconstruct.common.data.tags.ModifierTagProvider;
import slimeknights.tconstruct.library.client.data.spritetransformer.FramesSpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToColorMapping;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToSpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.IColorMapping;
import slimeknights.tconstruct.library.client.data.spritetransformer.ISpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.OffsettingSpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.RecolorSpriteTransformer;
import slimeknights.tconstruct.plugin.botania.BotaniaMaterialDataProvider;
import slimeknights.tconstruct.plugin.botania.BotaniaMaterialRenderInfoProvider;
import slimeknights.tconstruct.plugin.botania.BotaniaMaterialTraitsDataProvider;
import slimeknights.tconstruct.plugin.botania.BotaniaTiCAddon;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class BotaniaTiCAddonIntegrationTest extends BaseMcTest {
  @BeforeAll
  static void initClientSerializers() {
    ISpriteTransformer.SERIALIZER.registerDeserializer(RecolorSpriteTransformer.NAME, RecolorSpriteTransformer.DESERIALIZER);
    GreyToSpriteTransformer.init();
    ISpriteTransformer.SERIALIZER.registerDeserializer(OffsettingSpriteTransformer.NAME, OffsettingSpriteTransformer.DESERIALIZER);
    ISpriteTransformer.SERIALIZER.registerDeserializer(FramesSpriteTransformer.NAME, FramesSpriteTransformer.DESERIALIZER);
    IColorMapping.SERIALIZER.registerDeserializer(GreyToColorMapping.NAME, GreyToColorMapping.DESERIALIZER);
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
  void addonRegistersClientResourceProviders() throws ReflectiveOperationException {
    injectAddon(new BotaniaTiCAddon());

    Class<?> generator = Class.forName("slimeknights.tconstruct.data.resource.TiCDynamicResourceGenerator");
    Method createProviders = generator.getDeclaredMethod("createProviders");
    createProviders.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<Function<PackOutput, ? extends DataProvider>> providers =
      (List<Function<PackOutput, ? extends DataProvider>>) createProviders.invoke(null);

    PackOutput output = new PackOutput(Path.of("build", "test-botania-addon-resource-generator"));
    assertThat(providers)
      .extracting(factory -> factory.apply(output).getName())
      .anyMatch("Tinkers' Construct Botania Material Render Info"::equals)
      .anyMatch(name -> name.contains("Botania Materials"))
      .anyMatch(name -> name.contains("Material Palette Debug"));
  }

  @Test
  void addonDoesNotRegisterStandaloneTagProviders() throws ReflectiveOperationException {
    injectAddon(new BotaniaTiCAddon());

    Class<?> generator = Class.forName("slimeknights.tconstruct.data.tag.TiCDynamicTagGenerator");
    Method createProviderEntries = generator.getDeclaredMethod("createProviderEntries");
    createProviderEntries.setAccessible(true);
    List<?> entries = (List<?>) createProviderEntries.invoke(null);
    Method nameMethod = entries.get(0).getClass().getDeclaredMethod("name");
    nameMethod.setAccessible(true);

    assertThat(entries.stream().map(entry -> {
      try {
        return (String) nameMethod.invoke(entry);
      } catch (ReflectiveOperationException exception) {
        throw new AssertionError(exception);
      }
    }).toList()).doesNotContain("BotaniaModifierTagProvider", "BotaniaMaterialTagProvider");
  }

  @Test
  void coreTagProvidersIncludeAddonEntries() {
    injectAddonUnchecked(new BotaniaTiCAddon());

    CapturingOutput materialOutput = new CapturingOutput();
    new MaterialTagProvider(new PackOutput(Path.of("build", "test-botania-material-tags")), new ExistingFileHelper(List.of(), java.util.Set.of(), false, null, null))
      .run(materialOutput)
      .join();
    assertThat(readFile(materialOutput, "tinkering/tags/materials/ranged/light.json")).contains("manasteel", "terrasteel");

    CapturingOutput modifierOutput = new CapturingOutput();
    new ModifierTagProvider(new PackOutput(Path.of("build", "test-botania-modifier-tags")), new ExistingFileHelper(List.of(), java.util.Set.of(), false, null, null))
      .run(modifierOutput)
      .join();
    assertThat(readFile(modifierOutput, "tinkering/tags/modifiers/upgrades/general.json")).contains("manafix", "terrarecover");
  }

  @Test
  void materialTraitsProviderOnlyGeneratesBotaniaMaterials() {
    CapturingOutput output = new CapturingOutput();
    new BotaniaMaterialTraitsDataProvider(
      new PackOutput(Path.of("build", "test-botania-material-traits")),
      new BotaniaMaterialDataProvider(new PackOutput(Path.of("build", "test-botania-material-traits-materials"))))
      .run(output)
      .join();

    assertThat(output.paths())
      .allMatch(path -> path.toString().replace('\\', '/').contains("manasteel") || path.toString().replace('\\', '/').contains("terrasteel"))
      .hasSize(2);
  }

  @Test
  void materialRenderInfoProviderOnlyGeneratesBotaniaMaterials() {
    CapturingOutput output = new CapturingOutput();
    new BotaniaMaterialRenderInfoProvider(new PackOutput(Path.of("build", "test-botania-material-render-info")))
      .run(output)
      .join();

    assertThat(output.paths())
      .allMatch(path -> path.toString().replace('\\', '/').contains("manasteel") || path.toString().replace('\\', '/').contains("terrasteel"))
      .hasSize(2);
    assertThat(readFile(output, "tinkering/materials/manasteel.json")).contains("\"generator\"");
    assertThat(readFile(output, "tinkering/materials/terrasteel.json")).contains("\"generator\"");
  }

  @Test
  void addonRenderInfoProviderDoesNotOverwriteCoreMaterialFiles() {
    CapturingOutput cache = new CapturingOutput();
    String ironJson = "{\"generator\":{\"sentinel\":true}}";
    cache.write(Path.of("build", "test-botania-dynamic-render-info", "assets", "tconstruct", "tinkering", "materials", "iron.json"), ironJson);

    new BotaniaMaterialRenderInfoProvider(new PackOutput(Path.of("build", "test-botania-dynamic-render-info")))
      .run(cache)
      .join();

    String iron = readFile(cache, "tinkering/materials/iron.json");
    String manasteel = readFile(cache, "tinkering/materials/manasteel.json");
    assertThat(iron).isEqualTo(ironJson);
    assertThat(manasteel).contains("\"generator\"");
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

  private static void injectAddonUnchecked(ITiCAddon addon) {
    try {
      injectAddon(addon);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }

  private static String readFile(CapturingOutput output, String suffix) {
    return output.contents().entrySet().stream()
      .filter(entry -> entry.getKey().toString().replace('\\', '/').endsWith(suffix))
      .findFirst()
      .map(entry -> new String(entry.getValue(), StandardCharsets.UTF_8))
      .orElseThrow(() -> new AssertionError("Missing generated file ending with " + suffix));
  }

  private static final class CapturingOutput implements CachedOutput {
    private final Map<Path, byte[]> contents = new ConcurrentHashMap<>();

    @Override
    public void writeIfNeeded(Path path, byte[] bytes, HashCode hashCode) {
      contents.put(path.normalize(), bytes);
    }

    public void write(Path path, String text) {
      contents.put(path.normalize(), text.getBytes(StandardCharsets.UTF_8));
    }

    public Map<Path, byte[]> contents() {
      return contents;
    }

    public List<Path> paths() {
      return new ArrayList<>(contents.keySet());
    }
  }
}
