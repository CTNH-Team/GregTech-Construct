package slimeknights.tconstruct.data.pack;

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

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class DynamicDataProviderRunnerTest extends BaseMcTest {
    private final TiCDynamicDataPack pack = new TiCDynamicDataPack("test");

    @BeforeEach
    void clearDynamicPack() {
        TiCDynamicDataPack.clearServer();
    }

    @Test
    void generatedServerDataIsCaptured() {
        DynamicDataProviderRunner.run("test", output -> new TestTagProvider(output));

        ResourceLocation location = new ResourceLocation("example", "tags/items/generated.json");
        IoSupplier<InputStream> resource = pack.getResource(PackType.SERVER_DATA, location);

        assertThat(resource).isNotNull();
        assertThat(pack.getNamespaces(PackType.SERVER_DATA)).contains("example");
    }

    @Test
    void failingProviderDoesNotStopLaterProviders() {
        DynamicDataProviderRunner.run(
            "test",
            output -> {
                throw new IllegalStateException("expected provider construction failure");
            },
            output -> new TestTagProvider(output)
        );

        ResourceLocation location = new ResourceLocation("example", "tags/items/generated.json");
        assertThat(pack.getResource(PackType.SERVER_DATA, location)).isNotNull();
    }

    @Test
    void failingProviderRunDoesNotStopLaterProviders() {
        DynamicDataProviderRunner.run(
            "test",
            output -> new TestTagProvider(output, "first"),
            output -> new FailingRunProvider(),
            output -> new TestTagProvider(output, "later")
        );

        ResourceLocation firstLocation = new ResourceLocation("example", "tags/items/first.json");
        ResourceLocation laterLocation = new ResourceLocation("example", "tags/items/later.json");
        assertThat(pack.getResource(PackType.SERVER_DATA, firstLocation)).isNotNull();
        assertThat(pack.getResource(PackType.SERVER_DATA, laterLocation)).isNotNull();
    }

    @Test
    void nullProviderDoesNotStopLaterProviders() {
        DynamicDataProviderRunner.run(
            "test",
            output -> null,
            output -> new TestTagProvider(output)
        );

        ResourceLocation location = new ResourceLocation("example", "tags/items/generated.json");
        assertThat(pack.getResource(PackType.SERVER_DATA, location)).isNotNull();
    }

    @Test
    void malformedServerDataPathIsIgnored() {
        assertThatCode(() -> DynamicDataProviderRunner.run("test", output -> new InvalidPathProvider(output)))
            .doesNotThrowAnyException();

        ResourceLocation location = new ResourceLocation("malformed", "tags/items/generated.json");
        assertThat(pack.getResource(PackType.SERVER_DATA, location)).isNull();
        assertThat(pack.getNamespaces(PackType.SERVER_DATA)).doesNotContain("malformed");
    }

    private static class TestTagProvider implements DataProvider {
        private final PackOutput.PathProvider paths;
        private final String path;

        private TestTagProvider(PackOutput output) {
            this(output, "generated");
        }

        private TestTagProvider(PackOutput output, String path) {
            this.paths = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/items");
            this.path = path;
        }

        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            JsonObject json = new JsonObject();
            json.addProperty("replace", false);
            json.add("values", new com.google.gson.JsonArray());
            return DataProvider.saveStable(output, json, paths.json(new ResourceLocation("example", path)));
        }

        @Override
        public String getName() {
            return "Test Dynamic Tag Provider";
        }
    }

    private static class FailingRunProvider implements DataProvider {
        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            throw new IllegalStateException("expected provider run failure");
        }

        @Override
        public String getName() {
            return "Failing Run Provider";
        }
    }

    private static class InvalidPathProvider implements DataProvider {
        private final PackOutput.PathProvider paths;

        private InvalidPathProvider(PackOutput output) {
            this.paths = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/items");
        }

        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            JsonObject json = new JsonObject();
            json.addProperty("replace", false);
            json.add("values", new com.google.gson.JsonArray());
            try {
                byte[] bytes = json.toString().getBytes();
                output.writeIfNeeded(
                    paths.json(new ResourceLocation("malformed", "generated")).getParent().resolve("bad path").resolve("generated.json"),
                    bytes,
                    com.google.common.hash.Hashing.sha1().hashBytes(bytes)
                );
            } catch (java.io.IOException exception) {
                throw new RuntimeException(exception);
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public String getName() {
            return "Invalid Path Provider";
        }
    }
}
