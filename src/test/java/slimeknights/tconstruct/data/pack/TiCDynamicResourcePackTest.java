package slimeknights.tconstruct.data.pack;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicResourcePackTest extends BaseMcTest {
  private final TiCDynamicResourcePack pack = new TiCDynamicResourcePack("test");

  @BeforeEach
  void clearDynamicPack() {
    TiCDynamicResourcePack.clearClient();
  }

  @Test
  void addResourceStoresBytesInMemory() {
    TiCDynamicResourcePack.addResource(new ResourceLocation("example", "raw/generated.txt"), "ok".getBytes());

    assertThat(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("example", "raw/generated.txt"))).isNotNull();
    assertThat(pack.getNamespaces(PackType.CLIENT_RESOURCES)).contains("example");
  }

  @Test
  void addModelBlockstateAndTextureUseResourcePackPaths() {
    JsonObject json = new JsonObject();

    TiCDynamicResourcePack.addItemModel(new ResourceLocation("example", "generated"), json);
    TiCDynamicResourcePack.addBlockModel(new ResourceLocation("example", "generated_block"), json);
    TiCDynamicResourcePack.addBlockState(new ResourceLocation("example", "generated_state"), json);
    TiCDynamicResourcePack.addTexture(new ResourceLocation("example", "generated_texture"), "png".getBytes());

    assertThat(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("example", "models/item/generated.json"))).isNotNull();
    assertThat(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("example", "models/block/generated_block.json"))).isNotNull();
    assertThat(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("example", "blockstates/generated_state.json"))).isNotNull();
    assertThat(pack.getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("example", "textures/generated_texture.png"))).isNotNull();
  }

  @Test
  void listResourcesFindsRootPathEntries() {
    TiCDynamicResourcePack.addResource(new ResourceLocation("example", "raw/generated.txt"), "ok".getBytes());

    List<ResourceLocation> resources = new ArrayList<>();
    pack.listResources(PackType.CLIENT_RESOURCES, "example", "", (location, supplier) -> resources.add(location));

    assertThat(resources).containsExactly(new ResourceLocation("example", "raw/generated.txt"));
  }

  @Test
  void clearClientResetsDynamicNamespaces() {
    TiCDynamicResourcePack.addResource(new ResourceLocation("example", "raw/generated.txt"), "ok".getBytes());

    TiCDynamicResourcePack.clearClient();

    assertThat(pack.getNamespaces(PackType.CLIENT_RESOURCES))
      .containsExactlyInAnyOrder("tconstruct", "minecraft", "forge", "c");
  }
}
