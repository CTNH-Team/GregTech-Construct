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
      .contains("tconstruct:armor_layer_plate", "tconstruct:armor_layer_mail", "tconstruct:armor_core_helmet", "tconstruct:armor_frame_helmet", "tconstruct:armor_heavy_core_helmet");
    JsonObject zincStats = readJson(new ResourceLocation("tinkersinnovation", MaterialStatsManager.FOLDER + "/zinc.json"));
    assertThat(zincStats).isNotNull();
    assertThat(zincStats.getAsJsonObject("stats").keySet())
      .contains("tconstruct:plating_helmet", "tconstruct:armor_layer_mail", "tconstruct:armor_core_helmet");
    JsonObject ironTraits = readJson(new ResourceLocation(TConstruct.MOD_ID, MaterialTraitsManager.FOLDER + "/iron.json"));
    assertThat(ironTraits).isNotNull();
    JsonObject perStatTraits = ironTraits.getAsJsonObject("perStat");
    JsonObject armorTrait = perStatTraits.getAsJsonArray("tconstruct:armor").get(0).getAsJsonObject();
    assertThat(armorTrait.get("name").getAsString()).isEqualTo("tconstruct:projectile_protection");
    assertThat(armorTrait.get("level").getAsInt()).isEqualTo(1);
    JsonObject castHelmetTrait = perStatTraits.getAsJsonArray("tconstruct:armor_core_helmet").get(0).getAsJsonObject();
    assertThat(castHelmetTrait.get("name").getAsString()).isEqualTo("tconstruct:projectile_protection");
    assertThat(castHelmetTrait.get("level").getAsInt()).isEqualTo(2);
    JsonObject massiveCastHelmetTrait = perStatTraits.getAsJsonArray("tconstruct:armor_heavy_core_helmet").get(0).getAsJsonObject();
    assertThat(massiveCastHelmetTrait.get("name").getAsString()).isEqualTo("tconstruct:projectile_protection");
    assertThat(massiveCastHelmetTrait.get("level").getAsInt()).isEqualTo(2);
    JsonObject amethystTraits = readJson(new ResourceLocation(TConstruct.MOD_ID, MaterialTraitsManager.FOLDER + "/amethyst_bronze.json"));
    assertThat(amethystTraits).isNotNull();
    JsonObject amethystPerStatTraits = amethystTraits.getAsJsonObject("perStat");
    assertThat(amethystPerStatTraits.keySet()).contains("tconstruct:armor_core_helmet");
    JsonObject woolTraits = readJson(new ResourceLocation(TConstruct.MOD_ID, MaterialTraitsManager.FOLDER + "/wool.json"));
    assertThat(woolTraits).isNotNull();
    JsonObject woolPerStatTraits = woolTraits.getAsJsonObject("perStat");
    assertThat(woolPerStatTraits.keySet()).doesNotContain("tconstruct:armor_core_helmet");
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
