package slimeknights.tconstruct.data.tinkering;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.damagesource.DamageSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.data.pack.TiCDynamicDataRegistrar;
import slimeknights.tconstruct.library.addon.DynamicDataRegistrar;
import slimeknights.tconstruct.library.data.RuntimeDataProvider;
import slimeknights.tconstruct.library.data.tinkering.AbstractModifierProvider;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStat;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.ConditionalArmorStatModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.FormulaAreaEffectModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.FormulaArmorStatModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.FormulaDamageLimitModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.FormulaGuardingModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.FormulaRecurrenceModule;
import slimeknights.tconstruct.library.modifiers.modules.behavior.FormulaRepairModule;
import slimeknights.tconstruct.library.modifiers.modules.behavior.FormulaToolDamageModule;
import slimeknights.tconstruct.library.modifiers.modules.build.FormulaModifierSlotModule;
import slimeknights.tconstruct.library.modifiers.modules.build.ModifierSlotModule;
import slimeknights.tconstruct.library.modifiers.util.ModifierLevelDisplay;
import slimeknights.tconstruct.tools.ArmorDefinitions;
import slimeknights.tconstruct.tools.data.ArmorFormulaProvider;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.test.BaseMcTest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicTinkeringGeneratorTest extends BaseMcTest {
  private final TiCDynamicDataPack pack = new TiCDynamicDataPack("test");
  private static boolean registeredModifierSerializationLoaders;

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
    assertThat(pack.getResource(PackType.SERVER_DATA, ResourceLocation.tryBuild("tconstruct", "formula/cushion/formula.json"))).isNotNull();
    assertThat(pack.getResource(PackType.SERVER_DATA, ResourceLocation.tryBuild("tconstruct", "formula/tanned/formula.json"))).isNotNull();
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
  void modifierProviderStoresNativeArmorDefenseModifiers() throws IOException {
    registerModifierSerializationLoaders();
    TiCDynamicTinkeringGenerator.dataWriter(NativeArmorDefenseModifierProvider::new).accept(TiCDynamicDataRegistrar.INSTANCE);

    String melee = readServerData("tinkering/modifiers/melee_defense.json");
    String projectile = readServerData("tinkering/modifiers/projectile_defense.json");
    String blast = readServerData("tinkering/modifiers/blast_defense.json");
    String physics = readServerData("tinkering/modifiers/physics_defense.json");
    String guarding = readServerData("tinkering/modifiers/guarding.json");
    String cushion = readServerData("tinkering/modifiers/cushion.json");
    String plating = readServerData("tinkering/modifiers/plating.json");
    String hardening = readServerData("tinkering/modifiers/hardening.json");
    String crystalLattice = readServerData("tinkering/modifiers/crystal_lattice.json");
    String crystalizing = readServerData("tinkering/modifiers/crystalizing.json");
    String crystalSolidity = readServerData("tinkering/modifiers/crystal_solidity.json");
    String totem = readServerData("tinkering/modifiers/totem.json");
    String recurrence = readServerData("tinkering/modifiers/recurrence.json");
    String malleability = readServerData("tinkering/modifiers/malleability.json");

    assertThat(melee).contains("tconstruct:conditional_armor_stat", "pre_reduction", "tconstruct:protection/melee", "0.25", "0.75");
    assertThat(projectile).contains("tconstruct:conditional_armor_stat", "pre_reduction", "tconstruct:protection/projectile", "0.25", "0.75");
    assertThat(blast).contains("tconstruct:conditional_armor_stat", "pre_reduction", "tconstruct:protection/blast", "0.5", "1.0");
    assertThat(physics).contains("tconstruct:conditional_armor_stat", "post_reduction", "tconstruct:physics", "0.5");
    assertThat(guarding).contains("tconstruct:formula_guarding", "tconstruct:guarding/distance_factor_formula", "tconstruct:guarding/share_ratio_formula", "tconstruct:guarding/extra_protection_formula", "no_levels");
    assertThat(cushion).contains("tconstruct:formula_tool_damage", "tconstruct:cushion/formula", "no_levels");
    assertThat(plating).contains("tconstruct:formula_armor_stat", "post_reduction", "tconstruct:plating/stat_bonus", "tconstruct:formula_tool_damage", "tconstruct:plating/tool_damage", "no_levels");
    assertThat(hardening).contains("tconstruct:formula_repair", "tconstruct:hardening/regenerate_formula", "tconstruct:hardening/cool_down_formula", "no_levels");
    assertThat(crystalLattice).contains("tconstruct:formula_armor_stat", "post_reduction", "tconstruct:crystal_lattice/stat_bonus", "tconstruct:formula_tool_damage", "tconstruct:crystal_lattice/damage_capacity", "no_levels");
    assertThat(crystalizing).contains("tconstruct:formula_repair", "tconstruct:crystalizing/regenerate_formula", "tconstruct:crystalizing/cool_down_formula", "no_levels");
    assertThat(crystalSolidity).contains("tconstruct:formula_damage_limit", "tconstruct:crystal_solidity/cap_formula", "tconstruct:crystal_solidity/condition_formula", "tconstruct:crystal_solidity/per_armor_ratio", "tconstruct:crystal_solidity/finalizer/armor_damage_formula", "tconstruct:crystal_solidity/finalizer/overshield_damage_formula", "no_levels");
    assertThat(totem).contains("tconstruct:modifier_slot", "defense", "each_level", "1",
      "tconstruct:formula_area_effect", "tconstruct:triggered", "tconstruct:totem/range_formula",
      "tconstruct:totem/accumulator/duration_formula", "tconstruct:totem/accumulator/level_formula",
      "tconstruct:totem/finalizer/duration_formula", "tconstruct:totem/finalizer/level_formula", "tamed_only");
    assertThat(recurrence).contains("tconstruct:formula_recurrence", "tconstruct:recurrence/persistent_armor_stat/finalizer", "tconstruct:recurrence/damage_to_persistent/finalizer", "tconstruct:recurrence/persistent_tick/finalizer", "tconstruct:recurrence");
    assertThat(malleability).contains("tconstruct:formula_armor_stat", "armor_absorption_cap", "tconstruct:malleability/cap_formula", "tconstruct:formula_modifier_slot", "tconstruct:malleability/slot_formula", "defense");
    assertThat(melee + projectile + blast + physics + guarding + cushion + plating + hardening + crystalLattice + crystalizing + crystalSolidity + totem + recurrence + malleability).doesNotContain("tconarmorex");
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

  private static final class NativeArmorDefenseModifierProvider extends AbstractModifierProvider {
    private NativeArmorDefenseModifierProvider(PackOutput packOutput) {
      super(packOutput);
    }

    @Override
    public String getName() {
      return "Native Armor Defense Modifiers";
    }

    @Override
    protected void addModifiers() {
      buildModifier(ModifierIds.meleeDefense)
        .addModule(ConditionalArmorStatModule.stat(ArmorDamageStat.PRE_REDUCTION)
          .sources(DamageSourcePredicate.tag(TinkerTags.DamageTypes.MELEE_PROTECTION), DamageSourcePredicate.IS_INDIRECT.inverted())
          .eachLevel(0.25f).highestEachLevel(0.75f).build());
      buildModifier(ModifierIds.projectileDefense)
        .addModule(ConditionalArmorStatModule.stat(ArmorDamageStat.PRE_REDUCTION)
          .source(DamageSourcePredicate.tag(TinkerTags.DamageTypes.PROJECTILE_PROTECTION))
          .eachLevel(0.25f).highestEachLevel(0.75f).build());
      buildModifier(ModifierIds.blastDefense)
        .addModule(ConditionalArmorStatModule.stat(ArmorDamageStat.PRE_REDUCTION)
          .source(DamageSourcePredicate.tag(TinkerTags.DamageTypes.BLAST_PROTECTION))
          .eachLevel(0.5f).highestEachLevel(1.0f).build());
      buildModifier(ModifierIds.physicsDefense)
        .addModule(ConditionalArmorStatModule.stat(ArmorDamageStat.POST_REDUCTION)
          .source(DamageSourcePredicate.tag(TinkerTags.DamageTypes.PHYSICS))
          .eachLevel(0.5f).build());
      buildModifier(ModifierIds.guarding)
        .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
        .addModule(FormulaGuardingModule.guarding(
          TConstruct.getResource("guarding/distance_factor_formula"),
          TConstruct.getResource("guarding/share_ratio_formula"),
          TConstruct.getResource("guarding/extra_protection_formula")));
      buildModifier(ModifierIds.cushion)
        .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
        .addModule(FormulaToolDamageModule.formula(TConstruct.getResource("cushion/formula")));
      buildModifier(ModifierIds.plating)
        .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
        .addModule(FormulaArmorStatModule.stat(ArmorDamageStat.POST_REDUCTION, TConstruct.getResource("plating/stat_bonus")))
        .addModule(FormulaToolDamageModule.formula(TConstruct.getResource("plating/tool_damage")));
      buildModifier(ModifierIds.hardening)
        .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
        .addModule(FormulaRepairModule.repair(TConstruct.getResource("hardening/regenerate_formula"), TConstruct.getResource("hardening/cool_down_formula")));
      buildModifier(ModifierIds.crystalLattice)
        .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
        .addModule(FormulaArmorStatModule.stat(ArmorDamageStat.POST_REDUCTION, TConstruct.getResource("crystal_lattice/stat_bonus")))
        .addModule(FormulaToolDamageModule.formula(TConstruct.getResource("crystal_lattice/damage_capacity")));
      buildModifier(ModifierIds.crystalizing)
        .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
        .addModule(FormulaRepairModule.repair(TConstruct.getResource("crystalizing/regenerate_formula"), TConstruct.getResource("crystalizing/cool_down_formula")));
      buildModifier(ModifierIds.crystalSolidity)
        .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
        .addModule(FormulaDamageLimitModule.limit(
          TConstruct.getResource("crystal_solidity/cap_formula"),
          TConstruct.getResource("crystal_solidity/condition_formula"),
          TConstruct.getResource("crystal_solidity/per_armor_ratio"),
          TConstruct.getResource("crystal_solidity/finalizer/armor_damage_formula"),
          TConstruct.getResource("crystal_solidity/finalizer/overshield_damage_formula")));
      buildModifier(ModifierIds.totem)
        .addModule(ModifierSlotModule.slot(slimeknights.tconstruct.library.tools.SlotType.DEFENSE).eachLevel(1))
        .addModule(FormulaAreaEffectModule.tamed(
          TConstruct.getResource("triggered"),
          TConstruct.getResource("totem/range_formula"),
          TConstruct.getResource("totem/accumulator/duration_formula"),
          TConstruct.getResource("totem/accumulator/level_formula"),
          TConstruct.getResource("totem/finalizer/duration_formula"),
          TConstruct.getResource("totem/finalizer/level_formula")));
      buildModifier(ModifierIds.recurrence)
        .addModule(FormulaRecurrenceModule.recurrence(
          TConstruct.getResource("recurrence/persistent_armor_stat/finalizer"),
          TConstruct.getResource("recurrence/damage_to_persistent/finalizer"),
          TConstruct.getResource("recurrence/persistent_tick/finalizer"),
          TConstruct.getResource("recurrence")));
      buildModifier(ModifierIds.malleability)
        .addModule(FormulaArmorStatModule.stat(ArmorDamageStat.ARMOR_ABSORPTION_CAP, TConstruct.getResource("malleability/cap_formula")))
        .addModule(FormulaModifierSlotModule.slot(slimeknights.tconstruct.library.tools.SlotType.DEFENSE, TConstruct.getResource("malleability/slot_formula")));
    }
  }

  private static void registerModifierSerializationLoaders() {
    if (registeredModifierSerializationLoaders) {
      return;
    }
    tryRegisterLevelDisplay("default", ModifierLevelDisplay.DEFAULT.getLoader());
    tryRegisterLevelDisplay("no_levels", ModifierLevelDisplay.NO_LEVELS.getLoader());
    tryRegisterDamageSourcePredicate("is_indirect", DamageSourcePredicate.IS_INDIRECT.getLoader());
    tryRegisterModifierModule("conditional_armor_stat", ConditionalArmorStatModule.LOADER);
    tryRegisterModifierModule("formula_area_effect", FormulaAreaEffectModule.LOADER);
    tryRegisterModifierModule("formula_armor_stat", FormulaArmorStatModule.LOADER);
    tryRegisterModifierModule("formula_damage_limit", FormulaDamageLimitModule.LOADER);
    tryRegisterModifierModule("formula_guarding", FormulaGuardingModule.LOADER);
    tryRegisterModifierModule("formula_recurrence", FormulaRecurrenceModule.LOADER);
    tryRegisterModifierModule("formula_repair", FormulaRepairModule.LOADER);
    tryRegisterModifierModule("formula_tool_damage", FormulaToolDamageModule.LOADER);
    tryRegisterModifierModule("modifier_slot", ModifierSlotModule.LOADER);
    tryRegisterModifierModule("formula_modifier_slot", FormulaModifierSlotModule.LOADER);
    registeredModifierSerializationLoaders = true;
  }

  private static void tryRegisterLevelDisplay(String name, RecordLoadable<? extends ModifierLevelDisplay> loader) {
    try {
      ModifierLevelDisplay.LOADER.register(TConstruct.getResource(name), loader);
    } catch (IllegalStateException exception) {
      if (!isDuplicateRegistration(exception)) {
        throw exception;
      }
    }
  }

  private static void tryRegisterModifierModule(String name, RecordLoadable<? extends ModifierModule> loader) {
    try {
      ModifierModule.LOADER.register(TConstruct.getResource(name), loader);
    } catch (IllegalStateException exception) {
      if (!isDuplicateRegistration(exception)) {
        throw exception;
      }
    }
  }

  private static void tryRegisterDamageSourcePredicate(String name, RecordLoadable<? extends IJsonPredicate<DamageSource>> loader) {
    try {
      DamageSourcePredicate.LOADER.register(Mantle.getResource(name), loader);
    } catch (IllegalStateException exception) {
      if (!isDuplicateRegistration(exception)) {
        throw exception;
      }
    }
  }

  private static boolean isDuplicateRegistration(IllegalStateException exception) {
    String message = exception.getMessage();
    return message != null && (message.contains("Duplicate") || message.contains("duplicate") || message.contains("already"));
  }

  private String readServerData(String path) throws IOException {
    try (InputStream input = pack.getResource(PackType.SERVER_DATA, ResourceLocation.tryBuild("tconstruct", path)).get()) {
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
