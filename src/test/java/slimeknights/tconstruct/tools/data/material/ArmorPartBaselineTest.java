package slimeknights.tconstruct.tools.data.material;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import slimeknights.tconstruct.data.pack.DynamicPackOutput;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.data.pack.TiCDynamicDataRegistrar;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.stats.ArmorPartMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 阶段 A 基线测试：验证 Armor Part 重构前的行为基线
 *
 * 此测试固化以下内容：
 * 1. Stat ID 结构和数量
 * 2. Stat type 常量正确性
 * 3. 材料→stat 映射完整性（29 个本体全支持材料）
 *
 * 重构后，这些测试必须继续通过，证明行为等价性。
 *
 * 注意：
 * - Phase C 删除了 getArmorPartSpriteStats，sprite 声明现在在 TinkerMaterialSpriteProvider 中
 * - 材料支持矩阵通过 MaterialStatsDataProvider 的显式 addMaterialStats 调用建立
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArmorPartBaselineTest extends BaseMcTest {

  /** 期望拥有完整 armor stat bundle 的材料（本体材料） */
  private static final List<String> EXPECTED_FULL_SUPPORT_MATERIALS = List.of(
    "tconstruct:aluminum", "tconstruct:amethyst_bronze", "tconstruct:ancient", "tconstruct:bronze",
    "tconstruct:cinderslime", "tconstruct:cobalt", "tconstruct:constantan", "tconstruct:copper",
    "tconstruct:fiery", "tconstruct:gold", "tconstruct:hepatizon", "tconstruct:invar",
    "tconstruct:iron", "tconstruct:wrought_iron", "tconstruct:knightmetal", "tconstruct:lead",
    "tconstruct:manyullyn", "tconstruct:obsidian", "tconstruct:osmium", "tconstruct:pewter",
    "tconstruct:pig_iron", "tconstruct:queens_slime", "tconstruct:rose_gold", "tconstruct:scorched_stone",
    "tconstruct:seared_stone", "tconstruct:silver", "tconstruct:slimesteel", "tconstruct:steel", "tconstruct:steeleaf"
  );

  private Set<MaterialStatsId> allFullArmorStatIds;
  private final TiCDynamicDataPack pack = new TiCDynamicDataPack("armorpart-test");

  @BeforeAll
  void generateStats() {
    // 在 Bootstrap 之后才能安全访问 ArmorPartMaterialStats 常量
    this.allFullArmorStatIds = Set.of(
      PlatingMaterialStats.HELMET.getId(),
      PlatingMaterialStats.CHESTPLATE.getId(),
      PlatingMaterialStats.LEGGINGS.getId(),
      PlatingMaterialStats.BOOTS.getId(),
      PlatingMaterialStats.SHIELD.getId(),
      ArmorPartMaterialStats.MAILLE.getId(),
      ArmorPartMaterialStats.ARMOR_PLATE.getId(),
      ArmorPartMaterialStats.ARMOR_MAIL.getId(),
      ArmorPartMaterialStats.CAST_HELMET.getId(),
      ArmorPartMaterialStats.CAST_CHESTPLATE.getId(),
      ArmorPartMaterialStats.CAST_LEGGINGS.getId(),
      ArmorPartMaterialStats.CAST_BOOTS.getId(),
      ArmorPartMaterialStats.FRAME_HELMET.getId(),
      ArmorPartMaterialStats.FRAME_CHESTPLATE.getId(),
      ArmorPartMaterialStats.FRAME_LEGGINGS.getId(),
      ArmorPartMaterialStats.FRAME_BOOTS.getId(),
      ArmorPartMaterialStats.MASSIVE_CAST_HELMET.getId(),
      ArmorPartMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(),
      ArmorPartMaterialStats.MASSIVE_CAST_LEGGINGS.getId(),
      ArmorPartMaterialStats.MASSIVE_CAST_BOOTS.getId()
    );

    // 生成stats到TiCDynamicDataRegistrar供测试读取
    MaterialDataProvider materials = new MaterialDataProvider(DynamicPackOutput.dummy());
    new MaterialStatsDataProvider(DynamicPackOutput.dummy(), materials)
      .addToDynamicPack(TiCDynamicDataRegistrar.INSTANCE);
  }

  /**
   * 验证 armor part stat 类型总数和结构
   */
  @Test
  void armorPartHasCorrectStatStructure() {
    // 4 cast types (helmet, chestplate, leggings, boots)
    assertThat(ArmorPartMaterialStats.CAST_TYPES).hasSize(4);
    assertThat(ArmorPartMaterialStats.CAST_TYPES).containsExactly(
      ArmorPartMaterialStats.CAST_HELMET,
      ArmorPartMaterialStats.CAST_CHESTPLATE,
      ArmorPartMaterialStats.CAST_LEGGINGS,
      ArmorPartMaterialStats.CAST_BOOTS
    );

    // 4 frame types
    assertThat(ArmorPartMaterialStats.FRAME_TYPES).hasSize(4);
    assertThat(ArmorPartMaterialStats.FRAME_TYPES).containsExactly(
      ArmorPartMaterialStats.FRAME_HELMET,
      ArmorPartMaterialStats.FRAME_CHESTPLATE,
      ArmorPartMaterialStats.FRAME_LEGGINGS,
      ArmorPartMaterialStats.FRAME_BOOTS
    );

    // 4 massive cast types
    assertThat(ArmorPartMaterialStats.MASSIVE_CAST_TYPES).hasSize(4);
    assertThat(ArmorPartMaterialStats.MASSIVE_CAST_TYPES).containsExactly(
      ArmorPartMaterialStats.MASSIVE_CAST_HELMET,
      ArmorPartMaterialStats.MASSIVE_CAST_CHESTPLATE,
      ArmorPartMaterialStats.MASSIVE_CAST_LEGGINGS,
      ArmorPartMaterialStats.MASSIVE_CAST_BOOTS
    );

    // Verify stat IDs are correctly named
    assertThat(ArmorPartMaterialStats.ARMOR_PLATE.getId().toString())
      .isEqualTo("tconstruct:armor_plate");
    assertThat(ArmorPartMaterialStats.ARMOR_MAIL.getId().toString())
      .isEqualTo("tconstruct:armor_mail");
    assertThat(ArmorPartMaterialStats.MAILLE.getId().toString())
      .isEqualTo("tconstruct:maille");
    assertThat(StatlessMaterialStats.LINEAR.getIdentifier().toString())
      .isEqualTo("tconstruct:linear");
  }

  /**
   * 验证所有 29 个本体全支持材料都有完整的 20 个 armor stat
   *
   * 此测试防止迁移遗漏（如 P1 中兼容材料被遗漏）。
   */
  @Test
  void allFullSupportMaterialsHaveCompleteArmorStats() throws IOException {
    for (String materialId : EXPECTED_FULL_SUPPORT_MATERIALS) {
      String[] parts = materialId.split(":");
      ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
        parts[0],
        "tinkering/materials/stats/" + parts[1] + ".json"
      );

      var resource = pack.getResource(PackType.SERVER_DATA, location);
      assertThat(resource)
        .withFailMessage("Material %s is missing from generated stats", materialId)
        .isNotNull();

      String jsonString;
      try (var input = resource.get()) {
        jsonString = new String(input.readAllBytes(), StandardCharsets.UTF_8);
      }
      JsonObject root = JsonParser.parseString(jsonString).getAsJsonObject();
      JsonObject statsJson = root.getAsJsonObject("stats");

      Set<String> statIds = new HashSet<>(statsJson.keySet());

      Set<String> expectedStatIdStrings = new HashSet<>();
      for (MaterialStatsId id : allFullArmorStatIds) {
        expectedStatIdStrings.add(id.toString());
      }

      assertThat(statIds)
        .withFailMessage("Material %s is missing some armor stat IDs. Expected 20 armor stats in: %s",
                         materialId, statIds)
        .containsAll(expectedStatIdStrings);
    }
  }

  /**
   * 验证代表性材料的 stat 值正确性
   *
   * 抽查 copper 的 armor_plate 和 cast_helmet，确保值符合预期。
   */
  @Test
  void representativeMaterialHasCorrectStatValues() throws IOException {
    ResourceLocation copperLocation = ResourceLocation.fromNamespaceAndPath(
      "tconstruct",
      "tinkering/materials/stats/copper.json"
    );

    var resource = pack.getResource(PackType.SERVER_DATA, copperLocation);
    assertThat(resource).isNotNull();

    String jsonString;
    try (var input = resource.get()) {
      jsonString = new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
    JsonObject root = JsonParser.parseString(jsonString).getAsJsonObject();
    JsonObject copperStats = root.getAsJsonObject("stats");

    // 验证 armor_plate stat
    JsonObject armorPlate = copperStats.getAsJsonObject("tconstruct:armor_plate");
    assertThat(armorPlate)
      .withFailMessage("copper should have armor_plate stat")
      .isNotNull();

    // copper 的复合 ArmorPartStatsBuilder 配置保留原有 part stat 数值。
    // armor_plate: reduction=0.05f*0.5=0.025f, protection=0.018f
    assertThat(armorPlate.get("reduction").getAsFloat()).isEqualTo(0.025f);
    assertThat(armorPlate.get("protection").getAsFloat()).isEqualTo(0.018f);

    // 验证 cast_helmet stat
    JsonObject castHelmet = copperStats.getAsJsonObject("tconstruct:cast_helmet");
    assertThat(castHelmet)
      .withFailMessage("copper should have cast_helmet stat")
      .isNotNull();

    // cast_helmet: durability=11*13=143, armor=1.0f, reduction=0.05*0.5=0.025, protection=0.018*0.4=0.0072
    assertThat(castHelmet.get("durability").getAsInt()).isEqualTo(143);
    assertThat(castHelmet.get("armor").getAsFloat()).isEqualTo(1.0f);
    assertThat(castHelmet.get("reduction").getAsFloat()).isEqualTo(0.025f);
    assertThat(castHelmet.get("protection").getAsFloat()).isEqualTo(0.0072f);

    JsonObject platingHelmet = copperStats.getAsJsonObject(PlatingMaterialStats.HELMET.getId().toString());
    JsonObject platingChestplate = copperStats.getAsJsonObject(PlatingMaterialStats.CHESTPLATE.getId().toString());
    JsonObject platingLeggings = copperStats.getAsJsonObject(PlatingMaterialStats.LEGGINGS.getId().toString());
    JsonObject platingBoots = copperStats.getAsJsonObject(PlatingMaterialStats.BOOTS.getId().toString());
    JsonObject platingShield = copperStats.getAsJsonObject(PlatingMaterialStats.SHIELD.getId().toString());
    JsonObject maille = copperStats.getAsJsonObject(ArmorPartMaterialStats.MAILLE.getId().toString());

    assertThat(platingHelmet).isNotNull();
    assertThat(platingChestplate).isNotNull();
    assertThat(platingLeggings).isNotNull();
    assertThat(platingBoots).isNotNull();
    assertThat(platingShield).isNotNull();
    assertThat(maille).isNotNull();
    assertThat(platingHelmet.get("armor").getAsFloat()).isEqualTo(1f);
    assertThat(platingChestplate.get("armor").getAsFloat()).isEqualTo(3f);
    assertThat(platingLeggings.get("armor").getAsFloat()).isEqualTo(2f);
    assertThat(platingBoots.get("armor").getAsFloat()).isEqualTo(1f);
  }
}
