package slimeknights.tconstruct.library.client.data.material;

import com.google.gson.JsonElement;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.data.resource.RuntimeExistingFileHelper;
import slimeknights.tconstruct.library.addon.DynamicResourceRegistrar;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToSpriteTransformer;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.data.sprite.TinkerMaterialSpriteProvider;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AbstractMaterialRenderInfoProviderTest extends BaseMcTest {
  @Test
  void explicitColorDoesNotRequireFallbackSpriteDuringDynamicRegistration() {
    GreyToSpriteTransformer.init();
    TestRegistrar registrar = new TestRegistrar();

    assertThatCode(() -> new ExplicitIceRenderInfoProvider().addToDynamicPack(registrar))
      .doesNotThrowAnyException();

    assertThat(registrar.resources).hasSize(1);
    assertThat(registrar.resources.values().iterator().next()).contains("74ABFE");
  }

  @Test
  void missingFallbackSpriteDoesNotStopDynamicRegistration() {
    GreyToSpriteTransformer.init();
    TestRegistrar registrar = new TestRegistrar();

    assertThatCode(() -> new RedWoolRenderInfoProvider().addToDynamicPack(registrar))
      .doesNotThrowAnyException();

    assertThat(registrar.resources).hasSize(1);
    assertThat(registrar.resources.values().iterator().next()).contains("red");
  }

  private static final class ExplicitIceRenderInfoProvider extends AbstractMaterialRenderInfoProvider {
    private ExplicitIceRenderInfoProvider() {
      super(new PackOutput(Path.of("build", "test-material-render-info")), new TinkerMaterialSpriteProvider(), RuntimeExistingFileHelper.INSTANCE);
    }

    @Override
    protected void addMaterialRenderInfo() {
      buildRenderInfo(MaterialIds.ice).color(0x74ABFE);
    }

    @Override
    public String getName() {
      return "Test explicit ice material render info provider";
    }
  }

  private static final class RedWoolRenderInfoProvider extends AbstractMaterialRenderInfoProvider {
    private RedWoolRenderInfoProvider() {
      super(new PackOutput(Path.of("build", "test-material-render-info")), new TinkerMaterialSpriteProvider(), RuntimeExistingFileHelper.INSTANCE);
    }

    @Override
    protected void addMaterialRenderInfo() {
      buildRenderInfo(MaterialVariantId.create(MaterialIds.wool, "red"));
    }

    @Override
    public String getName() {
      return "Test red wool material render info provider";
    }
  }

  private static final class TestRegistrar implements DynamicResourceRegistrar {
    private final Map<ResourceLocation, String> resources = new LinkedHashMap<>();

    @Override
    public void addResource(ResourceLocation location, byte[] bytes) {
      resources.put(location, new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public void addTexture(ResourceLocation location, byte[] bytes) {}

    @Override
    public void addModel(ResourceLocation location, JsonElement json) {}

    @Override
    public void addBlockModel(ResourceLocation location, JsonElement json) {}

    @Override
    public void addItemModel(ResourceLocation location, JsonElement json) {}

    @Override
    public void addBlockState(ResourceLocation location, JsonElement json) {}
  }
}
