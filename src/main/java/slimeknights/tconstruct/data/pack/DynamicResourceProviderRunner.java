package slimeknights.tconstruct.data.pack;

import com.google.common.hash.HashCode;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.log4j.Log4j2;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Log4j2
public class DynamicResourceProviderRunner {
  private DynamicResourceProviderRunner() {}

  public static void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers) {
    runNamed(owner, providers.stream().map(DynamicResourceProviderRunner::toProviderFactory).toList());
  }

  public static void runNamed(String owner, List<DynamicProviderFactory> providers) {
    Path outputRoot = prepareOutputRoot();
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
        log.error("Failed to create dynamic resource provider{} for {}", providerEntrySuffix(providerEntry), owner, exception);
        throw new IllegalStateException("Failed to create dynamic resource provider" + providerEntrySuffix(providerEntry) + " for " + owner, exception);
      }
      if (provider == null) {
        log.error("Dynamic resource provider{} factory returned null for {}", providerEntrySuffix(providerEntry), owner);
        throw new IllegalStateException("Dynamic resource provider" + providerEntrySuffix(providerEntry) + " factory returned null for " + owner);
      }

      CapturingOutput cache = new CapturingOutput(outputRoot);
      try {
        provider.run(cache).join();
        completedProviders++;
        capturedResources += cache.capturedResources.get();
      } catch (Exception exception) {
        String providerName = provider.getName();
        log.error("Failed to run dynamic resource provider '{}'{} for {}", providerName, differentProviderEntrySuffix(providerEntry, providerName), owner, exception);
        throw new IllegalStateException("Failed to run dynamic resource provider '" + providerName + "'" + differentProviderEntrySuffix(providerEntry, providerName) + " for " + owner, exception);
      }
    }

    log.info("Captured {} dynamic client resources from {} providers for {} in {} ms",
      capturedResources, completedProviders, owner, (System.nanoTime() - startTime) / 1000000f);
  }

  private static String providerEntrySuffix(String providerEntry) {
    return providerEntry.isBlank() ? "" : " '" + providerEntry + "'";
  }

  private static String differentProviderEntrySuffix(String providerEntry, String providerName) {
    return providerEntry.isBlank() || providerEntry.equals(providerName) ? "" : " registered as '" + providerEntry + "'";
  }

  private static DynamicProviderFactory toProviderFactory(Function<PackOutput, ? extends DataProvider> factory) {
    if (factory instanceof DynamicProviderFactory providerFactory) {
      return providerFactory;
    }
    return DynamicProviderFactory.unnamed(factory);
  }

  private static class CapturingOutput implements CachedOutput {
    private final Path outputRoot;
    private final AtomicInteger capturedResources = new AtomicInteger();
    private final Map<Path,HashCode> writtenHashes = new ConcurrentHashMap<>();

    private CapturingOutput(Path outputRoot) {
      this.outputRoot = outputRoot;
    }

    @Override
    public void writeIfNeeded(Path path, byte[] bytes, HashCode hashCode) {
      Path normalizedPath = path.normalize();
      HashCode previous = writtenHashes.put(normalizedPath, hashCode);
      if (hashCode.equals(previous)) {
        return;
      }
      ResourceLocation location = toClientResourceLocation(outputRoot, path);
      if (location != null) {
        addDynamicResource(location, bytes);
        // Keep a real on-disk copy so later providers using ExistingFileHelper can read
        // textures and metadata generated earlier in the same dynamic run.
        writeCapturedResource(outputRoot, location, bytes);
        capturedResources.incrementAndGet();
      }
    }
  }

  public static Path getOutputRootPath() {
    Path gameDir = FMLPaths.GAMEDIR.get();
    if (gameDir != null) {
      return gameDir.resolve(".tconstruct-dynamic-datagen");
    }
    return Path.of("build", "dynamic-datagen-test");
  }

  private static Path prepareOutputRoot() {
    Path outputRoot = getOutputRootPath();
    try {
      if (Files.exists(outputRoot)) {
        deleteOutputRoot(outputRoot);
      }
      Files.createDirectories(outputRoot);
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to prepare dynamic resource output root: " + outputRoot, exception);
    }
    return outputRoot;
  }

  private static ResourceLocation toClientResourceLocation(Path outputRoot, Path path) {
    Path assetsRoot = outputRoot.resolve(PackType.CLIENT_RESOURCES.getDirectory()).normalize();
    Path normalizedPath = path.normalize();
    if (!normalizedPath.startsWith(assetsRoot)) {
      return null;
    }
    Path relative = assetsRoot.relativize(normalizedPath);
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

  private static void addDynamicResource(ResourceLocation fileLocation, byte[] bytes) {
    String path = fileLocation.getPath();
    if (path.startsWith("models/") && path.endsWith(".json")) {
      JsonElement json = parseJson(bytes, fileLocation);
      ResourceLocation modelId = TiCDynamicResourcePack.MODEL_ID_CONVERTER.fileToId(fileLocation);
      String modelPath = modelId.getPath();
      if (modelPath.startsWith("item/")) {
        TiCDynamicResourcePack.addItemModel(modelId, json);
        return;
      }
      if (modelPath.startsWith("block/")) {
        TiCDynamicResourcePack.addBlockModel(modelId, json);
        return;
      }
      TiCDynamicResourcePack.addModel(modelId, json);
      return;
    }
    if (path.startsWith("blockstates/") && path.endsWith(".json")) {
      TiCDynamicResourcePack.addBlockState(
        TiCDynamicResourcePack.BLOCKSTATE_ID_CONVERTER.fileToId(fileLocation),
        parseJson(bytes, fileLocation)
      );
      return;
    }
    if (path.startsWith("textures/") && path.endsWith(".png")) {
      TiCDynamicResourcePack.addTexture(toTextureId(fileLocation), bytes);
      return;
    }
    TiCDynamicResourcePack.addResource(fileLocation, bytes);
  }

  private static JsonElement parseJson(byte[] bytes, ResourceLocation fileLocation) {
    try {
      return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to parse generated resource JSON: " + fileLocation, exception);
    }
  }

  private static ResourceLocation toTextureId(ResourceLocation fileLocation) {
    String path = fileLocation.getPath();
    String texturePath = path.substring("textures/".length(), path.length() - ".png".length());
    return ResourceLocation.tryBuild(fileLocation.getNamespace(), texturePath);
  }

  private static void writeCapturedResource(Path outputRoot, ResourceLocation fileLocation, byte[] bytes) {
    Path file = outputRoot.resolve(PackType.CLIENT_RESOURCES.getDirectory())
                          .resolve(fileLocation.getNamespace())
                          .resolve(fileLocation.getPath());
    try {
      Files.createDirectories(file.getParent());
      Files.write(file, bytes);
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to write generated client resource: " + fileLocation, exception);
    }
  }

  private static void deleteOutputRoot(Path outputRoot) throws IOException {
    try (Stream<Path> walk = Files.walk(outputRoot)) {
      walk.sorted(Comparator.reverseOrder()).forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (IOException exception) {
          throw new IllegalStateException("Failed to delete dynamic resource output path: " + path, exception);
        }
      });
    }
  }
}
