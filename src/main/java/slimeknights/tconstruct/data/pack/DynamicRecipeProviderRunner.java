package slimeknights.tconstruct.data.pack;

import com.google.common.hash.HashCode;
import lombok.extern.log4j.Log4j2;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Log4j2
public class DynamicRecipeProviderRunner {
  private DynamicRecipeProviderRunner() {}

  public static void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers) {
    Path outputRoot = getOutputRoot();
    PackOutput output = new PackOutput(outputRoot);
    long startTime = System.nanoTime();
    int completedProviders = 0;
    int capturedResources = 0;

    for (Function<PackOutput, ? extends DataProvider> providerFactory : providers) {
      DataProvider provider;
      try {
        provider = providerFactory.apply(output);
      } catch (Exception exception) {
        log.error("Failed to create dynamic recipe provider for {}", owner, exception);
        throw new IllegalStateException("Failed to create dynamic recipe provider for " + owner, exception);
      }
      if (provider == null) {
        log.error("Dynamic recipe provider factory returned null for {}", owner);
        throw new IllegalStateException("Dynamic recipe provider factory returned null for " + owner);
      }

      CapturingOutput cache = new CapturingOutput(outputRoot);
      try {
        provider.run(cache).join();
        completedProviders++;
        capturedResources += cache.capturedResources.get();
      } catch (Exception exception) {
        log.error("Failed to run dynamic recipe provider '{}' for {}", provider.getName(), owner, exception);
        throw new IllegalStateException("Failed to run dynamic recipe provider '" + provider.getName() + "' for " + owner, exception);
      }
    }

    log.info("Captured {} dynamic recipe resources from {} providers for {} in {} ms",
      capturedResources, completedProviders, owner, (System.nanoTime() - startTime) / 1000000f);
  }

  private static class CapturingOutput implements CachedOutput {
    private final Path outputRoot;
    private final AtomicInteger capturedResources = new AtomicInteger();

    private CapturingOutput(Path outputRoot) {
      this.outputRoot = outputRoot;
    }

    @Override
    public void writeIfNeeded(Path path, byte[] bytes, HashCode hashCode) {
      ResourceLocation location = toServerDataLocation(outputRoot, path);
      if (location != null) {
        TiCDynamicDataPack.addData(location, bytes);
        addFilters(location);
        capturedResources.incrementAndGet();
      }
    }
  }

  private static Path getOutputRoot() {
    Path gameDir = net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get();
    if (gameDir != null) {
      return gameDir.resolve(".tconstruct-dynamic-datagen");
    }
    return Path.of("build", "dynamic-datagen-test");
  }

  private static ResourceLocation toServerDataLocation(Path outputRoot, Path path) {
    Path dataRoot = outputRoot.resolve(PackType.SERVER_DATA.getDirectory()).normalize();
    Path normalizedPath = path.normalize();
    if (!normalizedPath.startsWith(dataRoot)) {
      return null;
    }
    Path relative = dataRoot.relativize(normalizedPath);
    if (relative.getNameCount() < 2) {
      return null;
    }
    String namespace = relative.getName(0).toString();
    String resourcePath = String.join("/", relative.subpath(1, relative.getNameCount()).toString().split("\\\\"));
    if (namespace.isBlank() || resourcePath.isBlank()) {
      return null;
    }
    return ResourceLocation.tryBuild(namespace, resourcePath);
  }

  private static void addFilters(ResourceLocation location) {
    String path = location.getPath();
    if (path.startsWith("recipes/") && path.endsWith(".json")) {
      String recipePath = path.substring("recipes/".length(), path.length() - ".json".length());
      TiCDynamicDataPack.addRecipeFilter(ResourceLocation.tryBuild(location.getNamespace(), recipePath));
    } else if (path.startsWith("advancements/") && path.endsWith(".json")) {
      TiCDynamicDataPack.addFilter(location);
    }
  }
}
