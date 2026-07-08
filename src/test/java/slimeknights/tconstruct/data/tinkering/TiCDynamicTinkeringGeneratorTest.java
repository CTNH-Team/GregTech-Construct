package slimeknights.tconstruct.data.tinkering;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.data.pack.TiCDynamicDataRegistrar;
import slimeknights.tconstruct.library.addon.DynamicDataRegistrar;
import slimeknights.tconstruct.library.data.RuntimeDataProvider;
import slimeknights.tconstruct.tools.ArmorDefinitions;
import slimeknights.tconstruct.tools.data.ArmorFormulaProvider;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicTinkeringGeneratorTest extends BaseMcTest {
  private final TiCDynamicDataPack pack = new TiCDynamicDataPack("test");

  @AfterEach
  void clearExternalProviders() throws ReflectiveOperationException {
    Field field = TiCDynamicTinkeringGenerator.class.getDeclaredField("ADDITIONAL_PROVIDER_ENTRIES");
    field.setAccessible(true);
    ((ArrayList<?>) field.get(null)).clear();
  }

  @BeforeEach
  void clearDynamicPack() {
    TiCDynamicDataPack.clearServer();
  }

  @Test
  void registerKeepsProviderOrder() {
    assertThat(TiCDynamicTinkeringGenerator.createProviderEntries()).extracting(TiCDynamicTinkeringGenerator.TinkeringProviderEntry::name).containsExactly(
      "ToolDefinitionDataProvider",
      "StationSlotLayoutProvider",
      "ModifierProvider",
      "FluidEffectProvider",
      "EnchantmentToModifierProvider",
      "MobEquipmentProvider",
      "ArmorFormulaProvider"
    );
  }

  @Test
  void addProviderAppendsExternalProviderEntry() {
    TiCDynamicTinkeringGenerator.addProvider("ExternalTinkeringWriter", registrar -> registrar.addData(new ResourceLocation("example", "tinkering/external.json"), "{}".getBytes()));

    assertThat(TiCDynamicTinkeringGenerator.createProviderEntries())
      .extracting(TiCDynamicTinkeringGenerator.TinkeringProviderEntry::name)
      .containsExactly(
        "ToolDefinitionDataProvider",
        "StationSlotLayoutProvider",
        "ModifierProvider",
        "FluidEffectProvider",
        "EnchantmentToModifierProvider",
        "MobEquipmentProvider",
        "ArmorFormulaProvider",
        "ExternalTinkeringWriter"
      );
  }

  @Test
  void registerStoresArmorFormulaDefaults() {
    TiCDynamicTinkeringGenerator.dataWriter(ArmorFormulaProvider::new).accept(TiCDynamicDataRegistrar.INSTANCE);

    assertThat(pack.getResource(PackType.SERVER_DATA, ResourceLocation.tryBuild("tconstruct", "formula/default/generic/per_armor_ratio.json"))).isNotNull();
    assertThat(pack.getResource(PackType.SERVER_DATA, ResourceLocation.tryBuild("tconstruct", "formula/default/damage_limit/finalizer/armor_damage_formula.json"))).isNotNull();
    assertThat(pack.getResource(PackType.SERVER_DATA, ResourceLocation.tryBuild("tconstruct", "formula/default/damage_limit/finalizer/overshield_damage_formula.json"))).isNotNull();
    assertThat(pack.getResource(PackType.SERVER_DATA, ResourceLocation.tryBuild("tconstruct", "formula/plating/tool_damage.json"))).isNotNull();
    assertThat(pack.getResource(PackType.SERVER_DATA, ResourceLocation.tryBuild("tconstruct", "formula/crystal_solidity/finalizer/overshield_damage_formula.json"))).isNotNull();
    assertThat(pack.getResource(PackType.SERVER_DATA, ResourceLocation.tryBuild("tconstruct", "formula/malleability/slot_formula.json"))).isNotNull();
    assertThat(pack.getNamespaces(PackType.SERVER_DATA)).contains("tconstruct");
    assertThat(pack.getNamespaces(PackType.SERVER_DATA)).doesNotContain("tconarmorex");
  }

  @Test
  void nativeArmorDefinitionsUseTconstructNamespace() {
    assertThat(ArmorDefinitions.STANDARD.getArmorDefinition(net.minecraft.world.item.ArmorItem.Type.HELMET).getId())
      .isEqualTo(ResourceLocation.tryBuild("tconstruct", "standard_helmet"));
    assertThat(ArmorDefinitions.KNIGHTS.getArmorDefinition(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE).getId())
      .isEqualTo(ResourceLocation.tryBuild("tconstruct", "knights_chestplate"));
    assertThat(ArmorDefinitions.MIX_FORGED_OTHER.getArmorDefinition(net.minecraft.world.item.ArmorItem.Type.LEGGINGS).getId())
      .isEqualTo(ResourceLocation.tryBuild("tconstruct", "mix_forged_other_leggings"));
  }

  @Test
  void dataWriterStoresDataDirectlyInMemory() {
    TiCDynamicTinkeringGenerator.dataWriter(TestRuntimeDataProvider::new).accept(TiCDynamicDataRegistrar.INSTANCE);

    ResourceLocation location = new ResourceLocation("example", "tinkering/generated.json");
    assertThat(pack.getResource(PackType.SERVER_DATA, location)).isNotNull();
    assertThat(pack.getNamespaces(PackType.SERVER_DATA)).contains("example");
  }

  private static final class TestRuntimeDataProvider implements RuntimeDataProvider {
    private TestRuntimeDataProvider(PackOutput output) {}

    @Override
    public void addToDynamicPack(DynamicDataRegistrar registrar) {
      registrar.addJson(new ResourceLocation("example", "tinkering/generated.json"), new com.google.gson.JsonObject());
    }
  }
}
