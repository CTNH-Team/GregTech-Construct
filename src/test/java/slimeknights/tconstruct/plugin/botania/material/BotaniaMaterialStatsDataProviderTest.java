package slimeknights.tconstruct.plugin.botania.material;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.data.pack.DynamicPackOutput;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.data.pack.TiCDynamicDataRegistrar;
import slimeknights.tconstruct.library.materials.stats.MaterialStatType;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsManager;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.stats.ArmorPartMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BotaniaMaterialStatsDataProviderTest extends BaseMcTest {
  @BeforeEach
  void clearDynamicPack() {
    TiCDynamicDataPack.clearServer();
  }

  @Test
  void terrasteelWritesCompleteArmorStats() throws IOException {
    new BotaniaMaterialStatsDataProvider(DynamicPackOutput.dummy())
      .addToDynamicPack(TiCDynamicDataRegistrar.INSTANCE);

    JsonObject stats = readStats();
    Set<String> expected = new HashSet<>();
    expected.addAll(PlatingMaterialStats.TYPES.stream().map(type -> type.getId().toString()).toList());
    expected.add(ArmorPartMaterialStats.MAILLE.getId().toString());
    expected.add(ArmorPartMaterialStats.ARMOR_PLATE.getId().toString());
    expected.add(ArmorPartMaterialStats.ARMOR_MAIL.getId().toString());
    expected.addAll(ArmorPartMaterialStats.CAST_TYPES.stream().map(type -> type.getId().toString()).toList());
    expected.addAll(ArmorPartMaterialStats.FRAME_TYPES.stream().map(type -> type.getId().toString()).toList());
    expected.addAll(ArmorPartMaterialStats.MASSIVE_CAST_TYPES.stream().map(type -> type.getId().toString()).toList());

    assertThat(stats.keySet()).containsAll(expected);

    assertThat(stat(stats, PlatingMaterialStats.HELMET).get("durability").getAsInt()).isEqualTo(550);
    assertThat(stat(stats, PlatingMaterialStats.CHESTPLATE).get("armor").getAsFloat()).isEqualTo(8f);
    assertThat(stat(stats, ArmorPartMaterialStats.ARMOR_PLATE).get("reduction").getAsFloat()).isEqualTo(0.2f);
    assertThat(stat(stats, ArmorPartMaterialStats.CAST_CHESTPLATE).get("protection").getAsFloat()).isEqualTo(0.0225f);
    assertThat(stat(stats, ArmorPartMaterialStats.FRAME_CHESTPLATE).get("armor_strength").getAsFloat()).isEqualTo(0.08f);
    assertThat(stat(stats, ArmorPartMaterialStats.MASSIVE_CAST_CHESTPLATE).get("reduction").getAsFloat()).isEqualTo(0.4f);
  }

  private JsonObject readStats() throws IOException {
    ResourceLocation location = new ResourceLocation(
      "tconstruct", MaterialStatsManager.FOLDER + "/terrasteel.json");
    var resource = new TiCDynamicDataPack("test").getResource(PackType.SERVER_DATA, location);
    assertThat(resource).isNotNull();
    try (var input = resource.get()) {
      return JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8))
        .getAsJsonObject().getAsJsonObject("stats");
    }
  }

  private static JsonObject stat(JsonObject stats, MaterialStatType<?> statsType) {
    return stats.getAsJsonObject(statsType.getId().toString());
  }
}
