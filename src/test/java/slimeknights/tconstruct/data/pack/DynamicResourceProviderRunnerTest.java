package slimeknights.tconstruct.data.pack;

import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicResourceProviderRunnerTest extends BaseMcTest {
  private final TiCDynamicResourcePack pack = new TiCDynamicResourcePack("test");

  @BeforeEach
  void clearDynamicPack() throws IOException {
    TiCDynamicResourcePack.clearClient();
    Path outputRoot = DynamicResourceProviderRunner.getOutputRootPath();
    if (Files.exists(outputRoot)) {
      try (var stream = Files.walk(outputRoot)) {
        stream.sorted((left, right) -> right.getNameCount() - left.getNameCount())
              .forEach(path -> {
                try {
                  Files.deleteIfExists(path);
                } catch (IOException exception) {
                  throw new RuntimeException(exception);
                }
              });
      }
    }
  }

  @Test
  void capturesGeneratedTexturesAndMetadata() throws IOException {
    DynamicResourceProviderRunner.run("test", List.of(output -> new TestTextureProvider(output)));

    ResourceLocation textureLocation = new ResourceLocation("example", "textures/generated.png");
    ResourceLocation metadataLocation = new ResourceLocation("example", "textures/generated.png.mcmeta");
    IoSupplier<InputStream> texture = pack.getResource(PackType.CLIENT_RESOURCES, textureLocation);
    IoSupplier<InputStream> metadata = pack.getResource(PackType.CLIENT_RESOURCES, metadataLocation);

    assertThat(texture).isNotNull();
    assertThat(metadata).isNotNull();
    assertThat(pack.getNamespaces(PackType.CLIENT_RESOURCES)).contains("example");

    Path outputRoot = DynamicResourceProviderRunner.getOutputRootPath();
    assertThat(outputRoot.resolve("assets/example/textures/generated.png")).exists();
    assertThat(outputRoot.resolve("assets/example/textures/generated.png.mcmeta")).exists();
  }

  private static final class TestTextureProvider implements DataProvider {
    private final PackOutput.PathProvider texturePath;

    private TestTextureProvider(PackOutput output) {
      this.texturePath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "textures");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
      try {
        byte[] pngBytes = "fake-png".getBytes(StandardCharsets.UTF_8);
        output.writeIfNeeded(
          texturePath.file(new ResourceLocation("example", "generated"), "png"),
          pngBytes,
          Hashing.sha1().hashBytes(pngBytes)
        );

        JsonObject metadata = new JsonObject();
        JsonObject animation = new JsonObject();
        animation.addProperty("frametime", 2);
        metadata.add("animation", animation);
        byte[] metaBytes = metadata.toString().getBytes(StandardCharsets.UTF_8);
        output.writeIfNeeded(
          texturePath.file(new ResourceLocation("example", "generated"), "png.mcmeta"),
          metaBytes,
          Hashing.sha1().hashBytes(metaBytes)
        );
      } catch (IOException exception) {
        throw new RuntimeException(exception);
      }
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
      return "Test Dynamic Texture Provider";
    }
  }
}
