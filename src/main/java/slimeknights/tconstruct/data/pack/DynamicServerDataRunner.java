package slimeknights.tconstruct.data.pack;

import com.google.common.hash.HashCode;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

final class DynamicServerDataRunner {
  private DynamicServerDataRunner() {}

  static void run(Logger log,
                  String owner,
                  String providerDescription,
                  String resourceDescription,
                  List<DynamicProviderFactory> providers,
                  CapturedResourceConsumer resourceConsumer) {
    Path outputRoot = getOutputRoot();
    PackOutput output = new PackOutput(outputRoot);
    long startTime = System.nanoTime();
    int completedProviders = 0;
    int capturedResources = 0;
    for (DynamicProviderFactory providerFactory : providers) {
      String providerEntry = providerFactory.name();
      DataProvider provider;
      try {
        provider = providerFactory.apply(output);
      } catch (Exception exception) {
        log.error("Failed to create {}{} for {}", providerDescription, providerEntrySuffix(providerEntry), owner, exception);
        throw new IllegalStateException("Failed to create " + providerDescription + providerEntrySuffix(providerEntry) + " for " + owner, exception);
      }
      if (provider == null) {
        log.error("{}{} factory returned null for {}", capitalize(providerDescription), providerEntrySuffix(providerEntry), owner);
        throw new IllegalStateException(capitalize(providerDescription) + providerEntrySuffix(providerEntry) + " factory returned null for " + owner);
      }
      CapturingOutput cache = new CapturingOutput(outputRoot, resourceConsumer);
      try {
        provider.run(cache).join();
        completedProviders++;
        capturedResources += cache.capturedResources.get();
      } catch (Exception exception) {
        String providerName = provider.getName();
        log.error("Failed to run {} '{}'{} for {}", providerDescription, providerName, differentProviderEntrySuffix(providerEntry, providerName), owner, exception);
        throw new IllegalStateException("Failed to run " + providerDescription + " '" + providerName + "'" + differentProviderEntrySuffix(providerEntry, providerName) + " for " + owner, exception);
      }
    }
    log.info("Captured {} {} from {} providers for {} in {} ms",
      capturedResources, resourceDescription, completedProviders, owner, (System.nanoTime() - startTime) / 1000000f);
  }

  private static String providerEntrySuffix(String providerEntry) {
    return providerEntry.isBlank() ? "" : " '" + providerEntry + "'";
  }

  private static String differentProviderEntrySuffix(String providerEntry, String providerName) {
    return providerEntry.isBlank() || providerEntry.equals(providerName) ? "" : " registered as '" + providerEntry + "'";
  }

  private static String capitalize(String text) {
    if (text.isEmpty()) {
      return text;
    }
    return Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }

  private static class CapturingOutput implements CachedOutput {
    private final Path outputRoot;
    private final CapturedResourceConsumer resourceConsumer;
    private final AtomicInteger capturedResources = new AtomicInteger();
    private final Map<Path,HashCode> writtenHashes = new ConcurrentHashMap<>();

    private CapturingOutput(Path outputRoot, CapturedResourceConsumer resourceConsumer) {
      this.outputRoot = outputRoot;
      this.resourceConsumer = resourceConsumer;
    }

    @Override
    public void writeIfNeeded(Path path, byte[] bytes, HashCode hashCode) {
      Path normalizedPath = path.normalize();
      HashCode previous = writtenHashes.put(normalizedPath, hashCode);
      if (hashCode.equals(previous)) {
        return;
      }
      ResourceLocation location = toServerDataLocation(outputRoot, path);
      if (location != null) {
        resourceConsumer.accept(location, bytes);
        capturedResources.incrementAndGet();
      }
    }
  }

  private static Path getOutputRoot() {
    Path gameDir = FMLPaths.GAMEDIR.get();
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

  @FunctionalInterface
  interface CapturedResourceConsumer {
    void accept(ResourceLocation location, byte[] bytes);
  }
}
