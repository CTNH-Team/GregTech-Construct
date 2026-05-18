package slimeknights.tconstruct.library.addon;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.objectweb.asm.Type;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

  @Test
  void addonRegistryInvokesStaticModifierHook() throws ReflectiveOperationException {
    injectAddon(new TestAddon());

    AtomicBoolean modifierRegistered = new AtomicBoolean(false);
    TiCAddonRegistry.registerStaticModifiers((name, supplier) -> {
      if ("addon_static_modifier".equals(name)) {
        modifierRegistered.set(true);
      }
    });

    assertThat(modifierRegistered).isTrue();
  }

  @Test
  void addonRegistryInvokesFluidHooks() throws ReflectiveOperationException {
    AtomicBoolean initialized = new AtomicBoolean(false);
    AtomicBoolean tabItemsAdded = new AtomicBoolean(false);
    injectAddon(new TestFluidAddon(initialized, tabItemsAdded));

    TiCAddonRegistry.initFluidContent();
    TiCAddonRegistry.addFluidTabItems(Mockito.mock(CreativeModeTab.Output.class));

    assertThat(initialized).isTrue();
    assertThat(tabItemsAdded).isTrue();
  }

  @Test
  void dynamicTagRegistrarCollectsFluidMaterialAndModifierHooksFromDynamicTagProvidersOnly() throws ReflectiveOperationException {
    AtomicBoolean dynamicTagProvidersCalled = new AtomicBoolean(false);
    injectAddon(new TestTagAddon(dynamicTagProvidersCalled));

    TagHookCapture capture = new TagHookCapture();
    DynamicTagProviderRegistrar registrar = new DynamicTagProviderRegistrar(capture::addProvider);
    TiCAddonRegistry.collectTagProviders(registrar);

    assertThat(dynamicTagProvidersCalled).isTrue();
    assertThat(capture.providerNames)
      .contains("MaterialTagProvider", "ModifierTagProvider")
      .endsWith("AddonTagProvider");

    registrar.applyFluidTags(fluid -> {
      if (fluid.getId().equals(TinkerFluids.moltenIron.getId())) {
        capture.fluidHookApplied.set(true);
      }
    });
    registrar.applyMaterialTags(new DynamicTagProviderRegistrar.MaterialTagRegistrar() {
      @Override
      public void add(TagKey<IMaterial> tag, net.minecraft.resources.ResourceLocation... ids) {
        captureMaterialTags(capture, tag, ids);
      }

      @Override
      public void addOptional(TagKey<IMaterial> tag, net.minecraft.resources.ResourceLocation... ids) {
        captureMaterialTags(capture, tag, ids);
      }
    });
    registrar.applyModifierTags(new DynamicTagProviderRegistrar.ModifierTagRegistrar() {
      @Override
      public void add(TagKey<Modifier> tag, net.minecraft.resources.ResourceLocation... ids) {
        captureModifierTags(capture, tag, ids);
      }

      @Override
      public void addOptional(TagKey<Modifier> tag, net.minecraft.resources.ResourceLocation... ids) {
        captureModifierTags(capture, tag, ids);
      }
    });

    assertThat(capture.fluidHookApplied).isTrue();
    assertThat(capture.materialHookApplied).isTrue();
    assertThat(capture.modifierHookApplied).isTrue();
  }

  private static void captureMaterialTags(TagHookCapture capture, TagKey<IMaterial> tag, net.minecraft.resources.ResourceLocation... ids) {
    if (tag.equals(TinkerTags.Materials.LIGHT) && List.of(ids).contains(MaterialIds.wood)) {
      capture.materialHookApplied.set(true);
    }
  }

  private static void captureModifierTags(TagHookCapture capture, TagKey<Modifier> tag, net.minecraft.resources.ResourceLocation... ids) {
    if (tag.equals(TinkerTags.Modifiers.GENERAL_UPGRADES) && List.of(ids).contains(ModifierIds.diamond)) {
      capture.modifierHookApplied.set(true);
    }
  }

  @Test
  void addonFinderRejectsBlankAddonModId() {
    withDiscoveredAddons(() ->
      assertThatThrownBy(TiCAddonFinder::getAddons)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(BlankAddon.class.getName())
        .hasMessageContaining("blank"),
      BlankAddon.class);
  }

  @Test
  void addonFinderRejectsNullAddonModId() {
    withDiscoveredAddons(() ->
      assertThatThrownBy(TiCAddonFinder::getAddons)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(NullAddon.class.getName())
        .hasMessageContaining("null"),
      NullAddon.class);
  }

  @Test
  void addonFinderRejectsDuplicateAddonModIds() {
    withDiscoveredAddons(() ->
      assertThatThrownBy(TiCAddonFinder::getAddons)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Duplicate")
        .hasMessageContaining("duplicateaddon")
        .hasMessageContaining(FirstDuplicateAddon.class.getName())
        .hasMessageContaining(SecondDuplicateAddon.class.getName()),
      FirstDuplicateAddon.class, SecondDuplicateAddon.class);
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

  @SafeVarargs
  private static void withDiscoveredAddons(Runnable assertions, Class<? extends ITiCAddon>... addonClasses) {
    ModFileScanData scanData = new ModFileScanData();
    for (Class<? extends ITiCAddon> addonClass : addonClasses) {
      scanData.getAnnotations().add(new ModFileScanData.AnnotationData(
        Type.getType(TiCAddon.class),
        ElementType.TYPE,
        Type.getType(addonClass),
        addonClass.getName(),
        Map.of()));
    }

    try (MockedStatic<ModList> modList = Mockito.mockStatic(ModList.class)) {
      ModList modListInstance = Mockito.mock(ModList.class);
      modList.when(ModList::get).thenReturn(modListInstance);
      Mockito.when(modListInstance.getAllScanData()).thenReturn(List.of(scanData));

      assertions.run();
    }
  }

  private static final class TestAddon implements ITiCAddon, ITiCStaticModifierAddon {
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
    public void registerDynamicTagProviders(DynamicTagProviderRegistrar registrar) {
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

    @Override
    public void registerStaticModifiers(StaticModifierRegistrar registrar) {
      registrar.register("addon_static_modifier", StubModifier::new);
    }
  }

  @TiCAddon
  private static final class BlankAddon implements ITiCAddon {
    @Override
    public String addonModId() {
      return " ";
    }
  }

  @TiCAddon
  private static final class NullAddon implements ITiCAddon {
    @Override
    public String addonModId() {
      return null;
    }
  }

  @TiCAddon
  private static final class FirstDuplicateAddon implements ITiCAddon {
    @Override
    public String addonModId() {
      return "duplicateaddon";
    }
  }

  @TiCAddon
  private static final class SecondDuplicateAddon implements ITiCAddon {
    @Override
    public String addonModId() {
      return "duplicateaddon";
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

  private static final class StubModifier extends Modifier {}

  private static final class TestTagAddon implements ITiCAddon {
    private final AtomicBoolean dynamicTagProvidersCalled;

    private TestTagAddon(AtomicBoolean dynamicTagProvidersCalled) {
      this.dynamicTagProvidersCalled = dynamicTagProvidersCalled;
    }

    @Override
    public String addonModId() {
      return "testtagaddon";
    }

    @Override
    public void registerDynamicTagProviders(DynamicTagProviderRegistrar registrar) {
      dynamicTagProvidersCalled.set(true);
      registrar.addProvider("AddonTagProvider", output -> new StubProvider("AddonTagProvider"));
      registrar.addFluidTags(addon -> addon.add(TinkerFluids.moltenIron));
      registrar.addMaterialTags(addon -> addon.addOptional(TinkerTags.Materials.LIGHT, MaterialIds.wood));
      registrar.addModifierTags(addon -> addon.add(TinkerTags.Modifiers.GENERAL_UPGRADES, ModifierIds.diamond));
    }
  }

  private static final class TagHookCapture {
    private final java.util.ArrayList<String> providerNames = new java.util.ArrayList<>();
    private final AtomicBoolean fluidHookApplied = new AtomicBoolean(false);
    private final AtomicBoolean materialHookApplied = new AtomicBoolean(false);
    private final AtomicBoolean modifierHookApplied = new AtomicBoolean(false);

    public void addProvider(String name, Function<PackOutput, ? extends DataProvider> factory) {
      providerNames.add(name);
    }
  }

  private record TestFluidAddon(AtomicBoolean initialized, AtomicBoolean tabItemsAdded) implements ITiCAddon, ITiCFluidAddon {
    @Override
    public String addonModId() {
      return "testfluidaddon";
    }

    @Override
    public void registerSmelteryCompat(java.util.function.Consumer<AddonSmelteryCompat> registrar) {
      registrar.accept(new AddonSmelteryCompat() {
        @Override
        public void init() {
          initialized.set(true);
        }

        @Override
        public java.util.List<Entry> entries() {
          return java.util.List.of();
        }

        @Override
        public void addCreativeTabItems(CreativeModeTab.Output output) {
          tabItemsAdded.set(true);
        }
      });
    }
  }
}
