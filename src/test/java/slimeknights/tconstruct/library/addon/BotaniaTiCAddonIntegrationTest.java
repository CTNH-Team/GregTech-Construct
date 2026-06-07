package slimeknights.tconstruct.library.addon;

import com.google.common.hash.HashCode;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.FluidType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.fluid.texture.AbstractFluidTextureProvider;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.tconstruct.library.client.data.spritetransformer.FramesSpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToColorMapping;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToSpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.IColorMapping;
import slimeknights.tconstruct.library.client.data.spritetransformer.ISpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.OffsettingSpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.RecolorSpriteTransformer;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.plugin.botania.fluid.BotaniaFluidTextureProvider;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialDataProvider;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialRenderInfoProvider;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialTraitsDataProvider;
import slimeknights.tconstruct.plugin.botania.smeltery.BotaniaSmelteryCompat;
import slimeknights.tconstruct.plugin.botania.BotaniaTiCAddon;
import slimeknights.tconstruct.plugin.botania.tag.BotaniaFluidTagProvider;
import slimeknights.tconstruct.plugin.botania.tag.BotaniaMaterialTagProvider;
import slimeknights.tconstruct.plugin.botania.tag.BotaniaModifierTagProvider;
import slimeknights.tconstruct.smeltery.data.SmelteryCompat;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
      .anyMatch("Tinkers' Construct Botania Fluid Texture Providers"::equals)
      .anyMatch("Tinkers' Construct Botania Fluid Texture Cameras"::equals)
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
    }).toList())
      .doesNotContain("BotaniaFluidTagProvider", "BotaniaModifierTagProvider", "BotaniaMaterialTagProvider");
  }

  @Test
  void botaniaSmelteryCompatIsAddonOwned() {
    assertThat(BotaniaSmelteryCompat.INSTANCE.entries())
      .extracting(slimeknights.tconstruct.library.addon.AddonSmelteryCompat.Entry::name)
      .containsExactly("mana_steel", "terra_steel");
    assertThat(BotaniaSmelteryCompat.INSTANCE.entries())
      .extracting(entry -> entry.fluid().getId().getPath())
      .containsExactly("mana_steel", "terra_steel");
    assertThat(SmelteryCompat.values())
      .extracting(SmelteryCompat::getName)
      .doesNotContain("mana_steel", "terra_steel");
  }

  @Test
  void coreTagProvidersIncludeAddonEntries() throws ReflectiveOperationException {
    injectAddonUnchecked(new BotaniaTiCAddon());

    CapturingOutput materialOutput = new CapturingOutput();
    dynamicTagProvider("MaterialTagProvider", new PackOutput(Path.of("build", "test-botania-material-tags")))
      .run(materialOutput)
      .join();
    assertThat(readFile(materialOutput, "tinkering/tags/materials/ranged/light.json")).contains("manasteel", "terrasteel");

    CapturingOutput modifierOutput = new CapturingOutput();
    dynamicTagProvider("ModifierTagProvider", new PackOutput(Path.of("build", "test-botania-modifier-tags")))
      .run(modifierOutput)
      .join();
    assertThat(readFile(modifierOutput, "tinkering/tags/modifiers/upgrades/general.json")).contains("manafix", "terrarecover");
  }

  @Test
  void dynamicFluidTagHookIncludesBotaniaFluidEntries() {
    injectAddonUnchecked(new BotaniaTiCAddon());

    DynamicTagProviderRegistrar registrar = new DynamicTagProviderRegistrar((name, factory) -> {});
    TiCAddonRegistry.collectTagProviders(registrar);

    CapturingFluidTags fluidTags = new CapturingFluidTags();
    registrar.applyFluidTags(fluidTags);

    assertThat(fluidTags.names).containsExactly("mana_steel", "terra_steel");
  }

  @Test
  void botaniaTagHelpersExposeAddonTagEntries() {
    CapturingFluidTags fluidTags = new CapturingFluidTags();
    BotaniaFluidTagProvider.addTags(fluidTags);
    assertThat(fluidTags.names).containsExactly("mana_steel", "terra_steel");

    CapturingMaterialTags materialTags = new CapturingMaterialTags();
    BotaniaMaterialTagProvider.addTags(materialTags);
    assertThat(materialTags.optional.get(TinkerTags.Materials.COMPATABILITY_METALS.location()))
      .containsExactly("tconstruct:manasteel", "tconstruct:terrasteel");
    assertThat(materialTags.optional.get(TinkerTags.Materials.HARD_METALS.location()))
      .containsExactly("tconstruct:manasteel", "tconstruct:terrasteel");
    assertThat(materialTags.optional.get(TinkerTags.Materials.LIGHT.location()))
      .containsExactly("tconstruct:manasteel", "tconstruct:terrasteel");

    CapturingModifierTags modifierTags = new CapturingModifierTags();
    BotaniaModifierTagProvider.addTags(modifierTags);
    assertThat(modifierTags.required.get(TinkerTags.Modifiers.GENERAL_UPGRADES.location()))
      .containsExactly("tconstruct:manafix", "tconstruct:terrarecover");
  }

  @Test
  void botaniaFluidTextureProviderIsAddonOwned() {
    assertThat(new BotaniaFluidTextureProvider(new PackOutput(Path.of("build", "test-botania-fluid-textures"))).getName())
      .isEqualTo("Tinkers' Construct Botania Fluid Texture Providers");
  }

  @Test
  void botaniaFluidTextureProviderDoesNotValidateAllTconstructFluids() throws ReflectiveOperationException {
    Field modId = AbstractFluidTextureProvider.class.getDeclaredField("modId");
    modId.setAccessible(true);

    assertThat(modId.get(new BotaniaFluidTextureProvider(new PackOutput(Path.of("build", "test-botania-fluid-texture-validation")))))
      .isNull();
  }

  @Test
  void registeredSmelteryCompatSkipsAddonFluidsForNamespaceValidation() throws ReflectiveOperationException {
    FluidType manaSteel = new FluidType(FluidType.Properties.create()) {};
    FluidType terraSteel = new FluidType(FluidType.Properties.create()) {};
    injectAddonUnchecked(new TestFluidAddon("test-botania-fluid-addon", List.of(
      addonEntry("manasteel", manaSteel),
      addonEntry("terrasteel", terraSteel)
    )));

    CapturingFluidTextureProvider provider = new CapturingFluidTextureProvider();
    TiCAddonRegistry.collectSmelteryCompat(compat -> compat.skipFluidTextures(provider));

    Field ignoredFluidTypes = AbstractFluidTextureProvider.class.getDeclaredField("ignore");
    ignoredFluidTypes.setAccessible(true);
    @SuppressWarnings("unchecked")
    Set<FluidType> ignored = (Set<FluidType>) ignoredFluidTypes.get(provider);

    assertThat(provider.getAllTextures()).isEmpty();
    assertThat(ignored)
      .containsExactlyInAnyOrder(manaSteel, terraSteel);
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

  private static DataProvider dynamicTagProvider(String name, PackOutput output) throws ReflectiveOperationException {
    Class<?> generator = Class.forName("slimeknights.tconstruct.data.tag.TiCDynamicTagGenerator");
    Method createProviderEntries = generator.getDeclaredMethod("createProviderEntries");
    createProviderEntries.setAccessible(true);
    List<?> entries = (List<?>) createProviderEntries.invoke(null);
    Method nameMethod = entries.get(0).getClass().getDeclaredMethod("name");
    Method factoryMethod = entries.get(0).getClass().getDeclaredMethod("factory");
    nameMethod.setAccessible(true);
    factoryMethod.setAccessible(true);

    for (Object entry : entries) {
      if (name.equals(nameMethod.invoke(entry))) {
        @SuppressWarnings("unchecked")
        Function<PackOutput, ? extends DataProvider> factory = (Function<PackOutput, ? extends DataProvider>) factoryMethod.invoke(entry);
        return factory.apply(output);
      }
    }
    throw new AssertionError("Missing dynamic tag provider " + name);
  }

  private static AddonSmelteryCompat.Entry addonEntry(String name, FluidType type) {
    return new AddonSmelteryCompat.Entry(name, new FlowingFluidObject<>(
      ResourceLocation.tryBuild("tconstruct", name),
      null,
      () -> type,
      () -> null,
      () -> null,
      null
    ), AddonSmelteryCompat.CompatType.NONE, null);
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

  private static final class CapturingFluidTags implements DynamicTagProviderRegistrar.FluidTagRegistrar {
    private final List<String> names = new ArrayList<>();

    @Override
    public void add(slimeknights.mantle.registration.object.FlowingFluidObject<?> fluid) {
      names.add(fluid.getId().getPath());
    }
  }

  private static final class CapturingMaterialTags implements DynamicTagProviderRegistrar.MaterialTagRegistrar {
    private final Map<net.minecraft.resources.ResourceLocation, List<String>> required = new LinkedHashMap<>();
    private final Map<net.minecraft.resources.ResourceLocation, List<String>> optional = new LinkedHashMap<>();

    @Override
    public void add(net.minecraft.tags.TagKey<slimeknights.tconstruct.library.materials.definition.IMaterial> tag, net.minecraft.resources.ResourceLocation... ids) {
      required.put(tag.location(), java.util.Arrays.stream(ids).map(net.minecraft.resources.ResourceLocation::toString).toList());
    }

    @Override
    public void addOptional(net.minecraft.tags.TagKey<slimeknights.tconstruct.library.materials.definition.IMaterial> tag, net.minecraft.resources.ResourceLocation... ids) {
      optional.put(tag.location(), java.util.Arrays.stream(ids).map(net.minecraft.resources.ResourceLocation::toString).toList());
    }
  }

  private static final class CapturingModifierTags implements DynamicTagProviderRegistrar.ModifierTagRegistrar {
    private final Map<net.minecraft.resources.ResourceLocation, List<String>> required = new LinkedHashMap<>();
    private final Map<net.minecraft.resources.ResourceLocation, List<String>> optional = new LinkedHashMap<>();

    @Override
    public void add(net.minecraft.tags.TagKey<slimeknights.tconstruct.library.modifiers.Modifier> tag, net.minecraft.resources.ResourceLocation... ids) {
      required.put(tag.location(), java.util.Arrays.stream(ids).map(net.minecraft.resources.ResourceLocation::toString).toList());
    }

    @Override
    public void addOptional(net.minecraft.tags.TagKey<slimeknights.tconstruct.library.modifiers.Modifier> tag, net.minecraft.resources.ResourceLocation... ids) {
      optional.put(tag.location(), java.util.Arrays.stream(ids).map(net.minecraft.resources.ResourceLocation::toString).toList());
    }
  }

  private static final class CapturingFluidTextureProvider extends AbstractFluidTextureProvider {
    private CapturingFluidTextureProvider() {
      super(new PackOutput(Path.of("build", "test-addon-fluid-texture-skips")), "tconstruct");
    }

    @Override
    public void addTextures() {}

    @Override
    public String getName() {
      return "Capturing Fluid Texture Provider";
    }
  }

  private record TestFluidAddon(String addonModId, List<AddonSmelteryCompat.Entry> entries) implements ITiCAddon, ITiCFluidAddon {
    @Override
    public void registerSmelteryCompat(java.util.function.Consumer<AddonSmelteryCompat> registrar) {
      registrar.accept(() -> entries);
    }
  }

}
