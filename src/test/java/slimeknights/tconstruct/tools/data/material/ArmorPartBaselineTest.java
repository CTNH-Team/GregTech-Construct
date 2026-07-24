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

  /** 期望拥有完整 14 个 armor part stat 的材料（本体材料） */
  private static final List<String> EXPECTED_FULL_SUPPORT_MATERIALS = List.of(
    "tconstruct:aluminum", "tconstruct:amethyst_bronze", "tconstruct:ancient", "tconstruct:bronze",
    "tconstruct:cinderslime", "tconstruct:cobalt", "tconstruct:constantan", "tconstruct:copper",
    "tconstruct:fiery", "tconstruct:gold", "tconstruct:hepatizon", "tconstruct:invar",
    "tconstruct:iron", "tconstruct:wrought_iron", "tconstruct:knightmetal", "tconstruct:lead",
    "tconstruct:manyullyn", "tconstruct:obsidian", "tconstruct:osmium", "tconstruct:pewter",
    "tconstruct:pig_iron", "tconstruct:queens_slime", "tconstruct:rose_gold", "tconstruct:scorched_stone",
    "tconstruct:seared_stone", "tconstruct:silver", "tconstruct:slimesteel", "tconstruct:steel", "tconstruct:steeleaf"
  );

  private Set<MaterialStatsId> allArmorPartStatIds;
  private final TiCDynamicDataPack pack = new TiCDynamicDataPack("armorpart-test");

  @BeforeAll
  void generateStats() {
    // 在 Bootstrap 之后才能安全访问 ArmorPartMaterialStats 常量
    this.allArmorPartStatIds = Set.of(
      ArmorPartMaterialStats.ARMOR_LAYER_PLATE.getId(),
      ArmorPartMaterialStats.ARMOR_LAYER_MAIL.getId(),
      ArmorPartMaterialStats.ARMOR_CORE_HELMET.getId(),
      ArmorPartMaterialStats.ARMOR_CORE_CHESTPLATE.getId(),
      ArmorPartMaterialStats.ARMOR_CORE_LEGGINGS.getId(),
      ArmorPartMaterialStats.ARMOR_CORE_BOOTS.getId(),
      ArmorPartMaterialStats.ARMOR_FRAME_HELMET.getId(),
      ArmorPartMaterialStats.ARMOR_FRAME_CHESTPLATE.getId(),
      ArmorPartMaterialStats.ARMOR_FRAME_LEGGINGS.getId(),
      ArmorPartMaterialStats.ARMOR_FRAME_BOOTS.getId(),
      ArmorPartMaterialStats.ARMOR_HEAVY_CORE_HELMET.getId(),
      ArmorPartMaterialStats.ARMOR_HEAVY_CORE_CHESTPLATE.getId(),
      ArmorPartMaterialStats.ARMOR_HEAVY_CORE_LEGGINGS.getId(),
      ArmorPartMaterialStats.ARMOR_HEAVY_CORE_BOOTS.getId()
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
    assertThat(ArmorPartMaterialStats.CORE_TYPES).hasSize(4);
    assertThat(ArmorPartMaterialStats.CORE_TYPES).containsExactly(
      ArmorPartMaterialStats.ARMOR_CORE_HELMET,
      ArmorPartMaterialStats.ARMOR_CORE_CHESTPLATE,
      ArmorPartMaterialStats.ARMOR_CORE_LEGGINGS,
      ArmorPartMaterialStats.ARMOR_CORE_BOOTS
    );

    // 4 frame types
    assertThat(ArmorPartMaterialStats.FRAME_TYPES).hasSize(4);
    assertThat(ArmorPartMaterialStats.FRAME_TYPES).containsExactly(
      ArmorPartMaterialStats.ARMOR_FRAME_HELMET,
      ArmorPartMaterialStats.ARMOR_FRAME_CHESTPLATE,
      ArmorPartMaterialStats.ARMOR_FRAME_LEGGINGS,
      ArmorPartMaterialStats.ARMOR_FRAME_BOOTS
    );

    // 4 massive cast types
    assertThat(ArmorPartMaterialStats.HEAVY_CORE_TYPES).hasSize(4);
    assertThat(ArmorPartMaterialStats.HEAVY_CORE_TYPES).containsExactly(
      ArmorPartMaterialStats.ARMOR_HEAVY_CORE_HELMET,
      ArmorPartMaterialStats.ARMOR_HEAVY_CORE_CHESTPLATE,
      ArmorPartMaterialStats.ARMOR_HEAVY_CORE_LEGGINGS,
      ArmorPartMaterialStats.ARMOR_HEAVY_CORE_BOOTS
    );

    // Verify stat IDs are correctly named
    assertThat(ArmorPartMaterialStats.ARMOR_LAYER_PLATE.getId().toString())
      .isEqualTo("tconstruct:armor_layer_plate");
    assertThat(ArmorPartMaterialStats.ARMOR_LAYER_MAIL.getId().toString())
      .isEqualTo("tconstruct:armor_layer_mail");
    assertThat(ArmorPartMaterialStats.MAILLE.getId().toString())
      .isEqualTo("tconstruct:maille");
    assertThat(StatlessMaterialStats.LINEAR.getIdentifier().toString())
      .isEqualTo("tconstruct:linear");
  }

  /**
   * 验证所有 29 个本体全支持材料都有完整的 14 个 armor part stat
   *
   * 此测试防止迁移遗漏（如 P1 中兼容材料被遗漏）。
   */
  @Test
  void allFullSupportMaterialsHaveCompleteArmorPartStats() throws IOException {
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
      for (MaterialStatsId id : allArmorPartStatIds) {
        expectedStatIdStrings.add(id.toString());
      }

      assertThat(statIds)
        .withFailMessage("Material %s is missing some armor part stat IDs. Expected 14 armor part stats in: %s",
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

    // 验证 armor_layer_plate stat
    JsonObject armorPlate = copperStats.getAsJsonObject("tconstruct:armor_layer_plate");
    assertThat(armorPlate)
      .withFailMessage("copper should have armor_layer_plate stat")
      .isNotNull();

    // copper 的 ArmorPartStatsBuilder(13f).armor(1.0f, 3.0f, 2.0f, 1.0f).reduction(0.05f).protection(0.018f)...
    // armor_layer_plate: reduction=0.05f*0.5=0.025f, protection=0.018f
    assertThat(armorPlate.get("reduction").getAsFloat()).isEqualTo(0.025f);
    assertThat(armorPlate.get("protection").getAsFloat()).isEqualTo(0.018f);

    // 验证 armor_core_helmet stat
    JsonObject castHelmet = copperStats.getAsJsonObject("tconstruct:armor_core_helmet");
    assertThat(castHelmet)
      .withFailMessage("copper should have armor_core_helmet stat")
      .isNotNull();

    // armor_core_helmet: durability=11*13=143, armor=1.0f, reduction=0.05*0.5=0.025, protection=0.018*0.4=0.0072
    assertThat(castHelmet.get("durability").getAsInt()).isEqualTo(143);
    assertThat(castHelmet.get("armor").getAsFloat()).isEqualTo(1.0f);
    assertThat(castHelmet.get("reduction").getAsFloat()).isEqualTo(0.025f);
    assertThat(castHelmet.get("protection").getAsFloat()).isEqualTo(0.0072f);
  }
}
