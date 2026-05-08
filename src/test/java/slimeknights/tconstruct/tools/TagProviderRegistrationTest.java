package slimeknights.tconstruct.tools;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.ModList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import slimeknights.tconstruct.common.data.tags.MaterialTagProvider;
import slimeknights.tconstruct.common.data.tags.ModifierTagProvider;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.data.FluidEffectProvider;
import slimeknights.tconstruct.tools.data.ModifierProvider;
import slimeknights.tconstruct.tools.data.StationSlotLayoutProvider;
import slimeknights.tconstruct.tools.data.ToolDefinitionDataProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class TagProviderRegistrationTest extends BaseMcTest {
  @Test
  void toolGatherDataDoesNotRegisterStaticMaterialTags(@TempDir Path outputRoot) {
    List<DataProvider> providers = collectToolProviders(outputRoot);

    assertThat(providers).anyMatch(ToolDefinitionDataProvider.class::isInstance);
    assertThat(providers).anyMatch(StationSlotLayoutProvider.class::isInstance);
    assertThat(providers).noneMatch(MaterialTagProvider.class::isInstance);
  }

  @Test
  void modifierGatherDataDoesNotRegisterStaticModifierTags(@TempDir Path outputRoot) {
    List<DataProvider> providers = collectProviders(outputRoot, event -> {
      TinkerModifiers modifiers = Mockito.mock(TinkerModifiers.class, Mockito.CALLS_REAL_METHODS);
      modifiers.gatherData(event);
    });

    assertThat(providers).anyMatch(ModifierProvider.class::isInstance);
    assertThat(providers).anyMatch(FluidEffectProvider.class::isInstance);
    assertThat(providers).noneMatch(ModifierTagProvider.class::isInstance);
  }

  private static List<DataProvider> collectToolProviders(Path outputRoot) {
    try (MockedStatic<ModList> modList = Mockito.mockStatic(ModList.class)) {
      ModList modListInstance = Mockito.mock(ModList.class);
      modList.when(ModList::get).thenReturn(modListInstance);
      Mockito.when(modListInstance.isLoaded(Mockito.anyString())).thenReturn(false);
      return collectProviders(outputRoot, event -> {
        TinkerTools tools = Mockito.mock(TinkerTools.class, Mockito.CALLS_REAL_METHODS);
        tools.gatherData(event);
      });
    }
  }

  private static List<DataProvider> collectProviders(Path outputRoot, Consumer<GatherDataEvent> gatherData) {
    PackOutput packOutput = new PackOutput(outputRoot);
    DataGenerator generator = Mockito.mock(DataGenerator.class);
    ExistingFileHelper existingFileHelper = Mockito.mock(ExistingFileHelper.class);
    GatherDataEvent event = Mockito.mock(GatherDataEvent.class);
    List<DataProvider> providers = new ArrayList<>();

    Mockito.when(event.getGenerator()).thenReturn(generator);
    Mockito.when(generator.getPackOutput()).thenReturn(packOutput);
    Mockito.when(event.getExistingFileHelper()).thenReturn(existingFileHelper);
    Mockito.when(event.includeServer()).thenReturn(true);
    Mockito.when(event.includeClient()).thenReturn(false);
    Mockito.doAnswer(invocation -> {
      DataProvider provider = invocation.getArgument(1);
      providers.add(provider);
      return provider;
    }).when(generator).addProvider(Mockito.anyBoolean(), Mockito.<DataProvider>any());

    gatherData.accept(event);
    return providers;
  }
}
