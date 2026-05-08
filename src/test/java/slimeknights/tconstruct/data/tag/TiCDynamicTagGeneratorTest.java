package slimeknights.tconstruct.data.tag;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.data.tags.BiomeTagProvider;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TiCDynamicTagGeneratorTest extends BaseMcTest {
    @Test
    void registerSendsElevenProviderFactories() {
        RecordingRunner runner = new RecordingRunner();

        TiCDynamicTagGenerator.register(runner);

        assertThat(runner.owner).isEqualTo("tconstruct-tags");
        assertThat(runner.providers).hasSize(11);
    }

    @Test
    void registerKeepsProviderOrder() {
        assertThat(TiCDynamicTagGenerator.createProviderEntries()).extracting(TiCDynamicTagGenerator.TagProviderEntry::name).containsExactly(
            "BlockTagProvider",
            "ItemTagProvider",
            "FluidTagProvider",
            "EntityTypeTagProvider",
            "BlockEntityTypeTagProvider",
            "EnchantmentTagProvider",
            "MenuTypeTagProvider",
            "PotionTagProvider",
            "DamageTypeTagProvider",
            "MaterialTagProvider",
            "ModifierTagProvider"
        );
    }

    @Test
    void gatherDataRegistersBiomeTagProvider(@TempDir Path outputRoot) {
        DataGenerator generator = Mockito.mock(DataGenerator.class);
        ExistingFileHelper existingFileHelper = Mockito.mock(ExistingFileHelper.class);
        GatherDataEvent event = Mockito.mock(GatherDataEvent.class);
        PackOutput packOutput = new PackOutput(outputRoot);
        List<DataProvider> providers = new java.util.ArrayList<>();

        Mockito.when(event.getGenerator()).thenReturn(generator);
        Mockito.when(generator.getPackOutput()).thenReturn(packOutput);
        Mockito.when(event.getLookupProvider()).thenReturn(CompletableFuture.completedFuture(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)));
        Mockito.when(event.getExistingFileHelper()).thenReturn(existingFileHelper);
        Mockito.when(event.includeServer()).thenReturn(true);
        Mockito.when(event.includeClient()).thenReturn(false);
        Mockito.doAnswer(invocation -> {
            DataProvider provider = invocation.getArgument(1);
            providers.add(provider);
            return provider;
        }).when(generator).addProvider(Mockito.anyBoolean(), Mockito.<DataProvider>any());

        try {
            var gatherData = TConstruct.class.getDeclaredMethod("gatherData", GatherDataEvent.class);
            gatherData.setAccessible(true);
            gatherData.invoke(null, event);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }

        assertThat(providers).anyMatch(BiomeTagProvider.class::isInstance);
    }

    @Test
    void runtimeMaterialAndModifierProvidersRunWithExistingFileHelper(@TempDir Path outputRoot) throws Exception {
        Object state = newTagProviderState();
        Class<?> stateClass = state.getClass();
        ExistingFileHelper helper = existingFileHelper(state);

        assertThat(helper).isNotNull();

        PackOutput output = new PackOutput(outputRoot);
        CachedOutput cachedOutput = Mockito.mock(CachedOutput.class);
        assertThatCode(() -> {
            Method createMaterialTags = stateClass.getDeclaredMethod("createMaterialTags", PackOutput.class);
            Method createModifierTags = stateClass.getDeclaredMethod("createModifierTags", PackOutput.class);
            createMaterialTags.setAccessible(true);
            createModifierTags.setAccessible(true);

            DataProvider materialProvider = (DataProvider) createMaterialTags.invoke(state, output);
            DataProvider modifierProvider = (DataProvider) createModifierTags.invoke(state, output);
            materialProvider.run(cachedOutput).join();
            modifierProvider.run(cachedOutput).join();
        }).doesNotThrowAnyException();
    }

    @Test
    void runtimeExistingFileHelperAllowsRequiredForgeTagReferences() throws Exception {
        ExistingFileHelper helper = existingFileHelper(newTagProviderState());
        ExistingFileHelper.ResourceType blockTagType = new ExistingFileHelper.ResourceType(PackType.SERVER_DATA, ".json", "tags/blocks");
        ExistingFileHelper.ResourceType itemTagType = new ExistingFileHelper.ResourceType(PackType.SERVER_DATA, ".json", "tags/items");
        ExistingFileHelper.ResourceType fluidTagType = new ExistingFileHelper.ResourceType(PackType.SERVER_DATA, ".json", "tags/fluids");

        assertThat(helper.exists(new ResourceLocation("forge", "storage_blocks/netherite"), blockTagType)).isTrue();
        assertThat(helper.exists(new ResourceLocation("forge", "ore_rates/dense"), itemTagType)).isTrue();
        assertThat(helper.exists(new ResourceLocation("forge", "ore_rates/sparse"), itemTagType)).isTrue();
        assertThat(helper.exists(new ResourceLocation("forge", "milk"), fluidTagType)).isTrue();
    }

    private static Object newTagProviderState() throws ReflectiveOperationException {
        Class<?> stateClass = Class.forName("slimeknights.tconstruct.data.tag.TiCDynamicTagGenerator$TagProviderState");
        Constructor<?> constructor = stateClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static ExistingFileHelper existingFileHelper(Object state) throws ReflectiveOperationException {
        Field helperField = state.getClass().getDeclaredField("existingFileHelper");
        helperField.setAccessible(true);
        return (ExistingFileHelper) helperField.get(state);
    }

    private static class RecordingRunner implements TiCDynamicTagGenerator.TagRunner {
        private String owner;
        private List<Function<PackOutput, ? extends DataProvider>> providers = List.of();

        @Override
        public void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers) {
            this.owner = owner;
            this.providers = providers;
        }
    }
}
