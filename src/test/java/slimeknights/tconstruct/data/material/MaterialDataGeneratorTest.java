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
import slimeknights.tconstruct.library.materials.stats.MaterialStatsManager;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;
import slimeknights.tconstruct.tools.stats.SkullStats;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialDataGeneratorTest extends BaseMcTest {
  private final TiCDynamicDataPack pack = new TiCDynamicDataPack("test");

  @BeforeEach
  void clearDynamicPack() {
    TiCDynamicDataPack.clearServer();
  }

  @Test
  void materialStatsGeneratedInMultiplePassesMergeIntoOneFile() throws Exception {
    generateMaterialStats();

    JsonObject stats = readMaterialStats("iron");

    assertThat(stats.keySet()).contains(
      HeadMaterialStats.ID.toString(),
      PlatingMaterialStats.HELMET.getId().toString(),
      SkullStats.ID.toString()
    );
  }

  private static void generateMaterialStats() throws Exception {
    Method method = MaterialDataGenerator.class.getDeclaredMethod("addMaterialStats");
    method.setAccessible(true);
    method.invoke(null);
  }

  private JsonObject readMaterialStats(String material) throws Exception {
    ResourceLocation location = new ResourceLocation(TConstruct.MOD_ID, MaterialStatsManager.FOLDER + "/" + material + ".json");
    IoSupplier<InputStream> resource = pack.getResource(PackType.SERVER_DATA, location);
    assertThat(resource).isNotNull();
    try (InputStream inputStream = resource.get();
         InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      return JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("stats");
    }
  }
}
