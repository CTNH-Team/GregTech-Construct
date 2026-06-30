package slimeknights.tconstruct.data.tag;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.data.tags.BiomeTagProvider;
import slimeknights.tconstruct.common.data.tags.BlockTagProvider;
import slimeknights.tconstruct.common.data.tags.FluidTagProvider;
import slimeknights.tconstruct.common.data.tags.ItemTagProvider;
import slimeknights.tconstruct.common.data.tags.MaterialTagProvider;
import slimeknights.tconstruct.common.data.tags.ModifierTagProvider;
import slimeknights.tconstruct.library.addon.DynamicTagProviderRegistrar;
import slimeknights.tconstruct.test.BaseMcTest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicTagGeneratorTest extends BaseMcTest {
  @Test
  void addDatagenProvidersRegistersTagProviders(@TempDir Path outputRoot) {
    DataGenerator generator = Mockito.mock(DataGenerator.class);
    ExistingFileHelper existingFileHelper = Mockito.mock(ExistingFileHelper.class);
    PackOutput packOutput = new PackOutput(outputRoot);
    CompletableFuture<net.minecraft.core.HolderLookup.Provider> lookupProvider = CompletableFuture.completedFuture(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    List<DataProvider> providers = new ArrayList<>();

    Mockito.doAnswer(invocation -> {
      DataProvider provider = invocation.getArgument(1);
      providers.add(provider);
      return provider;
    }).when(generator).addProvider(Mockito.anyBoolean(), Mockito.<DataProvider>any());

    TiCDynamicTagGenerator.addDatagenProviders(generator, packOutput, lookupProvider, existingFileHelper, true);

    assertThat(providers).anyMatch(BlockTagProvider.class::isInstance);
    assertThat(providers).anyMatch(ItemTagProvider.class::isInstance);
    assertThat(providers).anyMatch(FluidTagProvider.class::isInstance);
    assertThat(providers).anyMatch(MaterialTagProvider.class::isInstance);
    assertThat(providers).anyMatch(ModifierTagProvider.class::isInstance);
  }

  @Test
  void gatherDataRegistersBiomeAndDynamicTagDatagenProviders(@TempDir Path outputRoot) {
    DataGenerator generator = Mockito.mock(DataGenerator.class);
    ExistingFileHelper existingFileHelper = Mockito.mock(ExistingFileHelper.class);
    GatherDataEvent event = Mockito.mock(GatherDataEvent.class);
    PackOutput packOutput = new PackOutput(outputRoot);
    List<DataProvider> providers = new ArrayList<>();

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
    assertThat(providers).anyMatch(BlockTagProvider.class::isInstance);
  }

  @Test
  void registrarAppliesHooksWithoutRuntimePackOutputProviders() {
    DynamicTagProviderRegistrar registrar = new DynamicTagProviderRegistrar();
    AtomicBoolean block = new AtomicBoolean(false);
    AtomicBoolean item = new AtomicBoolean(false);
    AtomicBoolean fluid = new AtomicBoolean(false);
    AtomicBoolean material = new AtomicBoolean(false);
    AtomicBoolean modifier = new AtomicBoolean(false);

    registrar.addBlockTags(tags -> block.set(true));
    registrar.addItemTags(tags -> item.set(true));
    registrar.addFluidTags(tags -> fluid.set(true));
    registrar.addMaterialTags(tags -> material.set(true));
    registrar.addModifierTags(tags -> modifier.set(true));

    registrar.applyBlockTags(new NoopBlockTagRegistrar());
    registrar.applyItemTags(new NoopItemTagRegistrar());
    registrar.applyFluidTags(new NoopFluidTagRegistrar());
    registrar.applyMaterialTags(new NoopMaterialTagRegistrar());
    registrar.applyModifierTags(new NoopModifierTagRegistrar());

    assertThat(block).isTrue();
    assertThat(item).isTrue();
    assertThat(fluid).isTrue();
    assertThat(material).isTrue();
    assertThat(modifier).isTrue();
  }

  private static final class NoopBlockTagRegistrar implements DynamicTagProviderRegistrar.BlockTagRegistrar {
    @Override
    public void add(net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> tag, ResourceLocation... ids) {}

    @Override
    public void addOptional(net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> tag, ResourceLocation... ids) {}
  }

  private static final class NoopItemTagRegistrar implements DynamicTagProviderRegistrar.ItemTagRegistrar {
    @Override
    public void add(net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag, ResourceLocation... ids) {}

    @Override
    public void addOptional(net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag, ResourceLocation... ids) {}
  }

  private static final class NoopFluidTagRegistrar implements DynamicTagProviderRegistrar.FluidTagRegistrar {
    @Override
    public void add(slimeknights.mantle.registration.object.FlowingFluidObject<?> fluid) {}
  }

  private static final class NoopMaterialTagRegistrar implements DynamicTagProviderRegistrar.MaterialTagRegistrar {
    @Override
    public void add(net.minecraft.tags.TagKey<slimeknights.tconstruct.library.materials.definition.IMaterial> tag, ResourceLocation... ids) {}

    @Override
    public void addOptional(net.minecraft.tags.TagKey<slimeknights.tconstruct.library.materials.definition.IMaterial> tag, ResourceLocation... ids) {}
  }

  private static final class NoopModifierTagRegistrar implements DynamicTagProviderRegistrar.ModifierTagRegistrar {
    @Override
    public void add(net.minecraft.tags.TagKey<slimeknights.tconstruct.library.modifiers.Modifier> tag, ResourceLocation... ids) {}

    @Override
    public void addOptional(net.minecraft.tags.TagKey<slimeknights.tconstruct.library.modifiers.Modifier> tag, ResourceLocation... ids) {}
  }
}
