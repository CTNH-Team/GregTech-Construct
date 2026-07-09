package slimeknights.tconstruct.tools.logic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class HurtSoundSuppressionIntegrationTest extends BaseMcTest {
  @Test
  void mixinConfigRegistersPlayerHurtSoundMixin() throws Exception {
    String json = Files.readString(Path.of("src/main/resources/tconstruct.mixins.json"));
    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
    assertThat(root.getAsJsonArray("mixins").toString()).contains("PlayerHurtSoundMixin");
  }

  @Test
  void tinkerNetworkRegistersSuppressHurtSoundPacket() throws Exception {
    String java = Files.readString(Path.of("src/main/java/slimeknights/tconstruct/common/network/TinkerNetwork.java"));
    assertThat(java).contains("SuppressHurtSoundPacket");
  }
}
