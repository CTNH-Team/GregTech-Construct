package slimeknights.tconstruct.data.resource;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import slimeknights.tconstruct.test.BaseMcTest;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatCode;

class TiCDynamicResourceGeneratorTest extends BaseMcTest {
  @Test
  void modelSpriteProviderRunsWithDynamicExistingFileHelper(@TempDir Path outputRoot) {
    List<Function<PackOutput, ? extends DataProvider>> providers = TiCDynamicResourceGenerator.createProviders();
    PackOutput output = new PackOutput(outputRoot);
    CachedOutput cachedOutput = Mockito.mock(CachedOutput.class);

    DataProvider modelSpriteProvider = providers.get(0).apply(output);

    assertThatCode(() -> modelSpriteProvider.run(cachedOutput).join())
      .doesNotThrowAnyException();
  }
}
