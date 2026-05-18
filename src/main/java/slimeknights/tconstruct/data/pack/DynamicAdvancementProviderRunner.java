package slimeknights.tconstruct.data.pack;

import lombok.extern.log4j.Log4j2;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Function;

@Log4j2
public class DynamicAdvancementProviderRunner {
  private DynamicAdvancementProviderRunner() {}

  public static void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers) {
    runNamed(owner, providers.stream().map(DynamicAdvancementProviderRunner::toProviderFactory).toList());
  }

  public static void runNamed(String owner, List<DynamicProviderFactory> providers) {
    DynamicServerDataRunner.run(
      log,
      owner,
      "dynamic advancement provider",
      "dynamic advancement resources",
      providers,
      DynamicAdvancementProviderRunner::captureAdvancementResource
    );
  }

  private static void captureAdvancementResource(ResourceLocation location, byte[] bytes) {
    TiCDynamicDataPack.addData(location, bytes);
    String path = location.getPath();
    if (path.startsWith("advancements/") && path.endsWith(".json")) {
      TiCDynamicDataPack.addFilter(location);
    }
  }

  private static DynamicProviderFactory toProviderFactory(Function<PackOutput, ? extends DataProvider> factory) {
    if (factory instanceof DynamicProviderFactory providerFactory) {
      return providerFactory;
    }
    return DynamicProviderFactory.unnamed(factory);
  }
}
