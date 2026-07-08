package slimeknights.tconstruct.data.material;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.library.materials.definition.MaterialManager;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsManager;
import slimeknights.tconstruct.library.materials.traits.MaterialTraitsManager;
import slimeknights.tconstruct.test.BaseMcTest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialDynamicPackTest extends BaseMcTest {
  private final TiCDynamicDataPack pack = new TiCDynamicDataPack("test");

  @BeforeEach
  void clearDynamicPack() {
    TiCDynamicDataPack.clearServer();
  }

  @Test
  void registerCapturesMaterialDefinitionStatsAndTraits() throws Exception {
    TiCDynamicMaterialGenerator.register();

    assertThat(readJson(new ResourceLocation(TConstruct.MOD_ID, MaterialManager.FOLDER + "/wood.json"))).isNotNull();
    JsonObject ironStats = readJson(new ResourceLocation(TConstruct.MOD_ID, MaterialStatsManager.FOLDER + "/iron.json"));
    assertThat(ironStats).isNotNull();
    assertThat(ironStats.getAsJsonObject("stats").keySet())
      .contains("tconstruct:armor_plate", "tconstruct:armor_mail", "tconstruct:cast_helmet", "tconstruct:frame_of_helmet", "tconstruct:massive_cast_helmet");
    JsonObject ironTraits = readJson(new ResourceLocation(TConstruct.MOD_ID, MaterialTraitsManager.FOLDER + "/iron.json"));
    assertThat(ironTraits).isNotNull();
    JsonObject perStatTraits = ironTraits.getAsJsonObject("perStat");
    assertThat(perStatTraits.getAsJsonArray("tconstruct:cast_helmet").get(0).getAsJsonObject().get("name").getAsString()).isEqualTo("tconstruct:projectile_protection");
    assertThat(perStatTraits.getAsJsonArray("tconstruct:cast_helmet").get(0).getAsJsonObject().get("level").getAsInt()).isEqualTo(2);
    assertThat(perStatTraits.getAsJsonArray("tconstruct:massive_cast_helmet").get(0).getAsJsonObject().get("name").getAsString()).isEqualTo("tconstruct:projectile_protection");
    assertThat(perStatTraits.getAsJsonArray("tconstruct:massive_cast_helmet").get(0).getAsJsonObject().get("level").getAsInt()).isEqualTo(2);
  }

  private JsonObject readJson(ResourceLocation location) throws Exception {
    IoSupplier<InputStream> resource = pack.getResource(PackType.SERVER_DATA, location);
    assertThat(resource).as(location.toString()).isNotNull();
    try (InputStream inputStream = resource.get();
         InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      return JsonParser.parseReader(reader).getAsJsonObject();
    }
  }
}
