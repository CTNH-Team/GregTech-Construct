package slimeknights.tconstruct.data.advancement;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.data.AdvancementsProvider;
import slimeknights.tconstruct.test.BaseMcTest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicAdvancementRunDataTest extends BaseMcTest {
  @Test
  void gatherDataRegistersStaticAdvancementProvider() {
    DataGenerator generator = Mockito.mock(DataGenerator.class);
    GatherDataEvent event = Mockito.mock(GatherDataEvent.class);
    List<DataProvider> providers = new ArrayList<>();

    Mockito.when(event.getGenerator()).thenReturn(generator);
    Mockito.when(generator.getPackOutput()).thenReturn(new PackOutput(Path.of("build", "test-static-advancements")));
    Mockito.when(event.getLookupProvider()).thenReturn(CompletableFuture.completedFuture(net.minecraft.core.RegistryAccess.EMPTY));
    Mockito.when(event.getExistingFileHelper()).thenReturn(Mockito.mock(ExistingFileHelper.class));
    Mockito.when(event.includeServer()).thenReturn(true);
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

    assertThat(providers).anyMatch(AdvancementsProvider.class::isInstance);
  }
}
