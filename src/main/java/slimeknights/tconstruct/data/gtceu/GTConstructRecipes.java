package slimeknights.tconstruct.data.gtceu;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.data.material.MaterialStatsDataProvider;
import slimeknights.tconstruct.tools.stats.GripMaterialStats;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;
import slimeknights.tconstruct.tools.stats.LimbMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;

import static com.gregtechceu.gtceu.api.GTValues.*;

public class GTConstructRecipes {

  private static final Logger LOGGER = Util.getLogger("GTRecipes");
  private static final MaterialSupportIndex MATERIAL_SUPPORT = new MaterialSupportIndex();

  private static final Map<String, String> OUTPUT_MATERIAL_ALIASES = Map.of(
    "aluminium", "aluminum",
    "ender", "ender_pearl",
    "fiery_liquid", "fiery"
  );

  private static final List<SolidifierPart> DEFAULT_SOLIDIFIER_PARTS = List.of(
    new SolidifierPart("repair_kit", TinkerToolParts.repairKit::get, 2, TinkerSmeltery.repairKitCast, MATERIAL_SUPPORT::hasRepairKitSupport),
    new SolidifierPart("pick_head", TinkerToolParts.pickHead::get, 2, TinkerSmeltery.pickHeadCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("hammer_head", TinkerToolParts.hammerHead::get, 8, TinkerSmeltery.hammerHeadCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("small_axe_head", TinkerToolParts.smallAxeHead::get, 2, TinkerSmeltery.smallAxeHeadCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("broad_axe_head", TinkerToolParts.broadAxeHead::get, 8, TinkerSmeltery.broadAxeHeadCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("small_blade", TinkerToolParts.smallBlade::get, 2, TinkerSmeltery.smallBladeCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("broad_blade", TinkerToolParts.broadBlade::get, 8, TinkerSmeltery.broadBladeCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("bow_limb", TinkerToolParts.bowLimb::get, 2, TinkerSmeltery.bowLimbCast, material -> MATERIAL_SUPPORT.hasStat(material, LimbMaterialStats.ID)),
    new SolidifierPart("bow_grip", TinkerToolParts.bowGrip::get, 2, TinkerSmeltery.bowGripCast, material -> MATERIAL_SUPPORT.hasStat(material, GripMaterialStats.ID)),
    new SolidifierPart("tool_binding", TinkerToolParts.toolBinding::get, 1, TinkerSmeltery.toolBindingCast, material -> MATERIAL_SUPPORT.hasStat(material, StatlessMaterialStats.BINDING.getIdentifier())),
    new SolidifierPart("tough_binding", TinkerToolParts.toughBinding::get, 3, TinkerSmeltery.toughBindingCast, material -> MATERIAL_SUPPORT.hasStat(material, StatlessMaterialStats.BINDING.getIdentifier())),
    new SolidifierPart("adze_head", TinkerToolParts.adzeHead::get, 2, TinkerSmeltery.adzeHeadCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("large_plate", TinkerToolParts.largePlate::get, 4, TinkerSmeltery.largePlateCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("tool_handle", TinkerToolParts.toolHandle::get, 1, TinkerSmeltery.toolHandleCast, material -> MATERIAL_SUPPORT.hasStat(material, HandleMaterialStats.ID)),
    new SolidifierPart("tough_handle", TinkerToolParts.toughHandle::get, 3, TinkerSmeltery.toughHandleCast, material -> MATERIAL_SUPPORT.hasStat(material, HandleMaterialStats.ID)),
    new SolidifierPart("helmet_plating", () -> TinkerToolParts.plating.get(ArmorItem.Type.HELMET), 3, TinkerSmeltery.helmetPlatingCast, material -> MATERIAL_SUPPORT.hasStat(material, PlatingMaterialStats.HELMET.getId()), false),
    new SolidifierPart("chestplate_plating", () -> TinkerToolParts.plating.get(ArmorItem.Type.CHESTPLATE), 6, TinkerSmeltery.chestplatePlatingCast, material -> MATERIAL_SUPPORT.hasStat(material, PlatingMaterialStats.CHESTPLATE.getId()), false),
    new SolidifierPart("leggings_plating", () -> TinkerToolParts.plating.get(ArmorItem.Type.LEGGINGS), 5, TinkerSmeltery.leggingsPlatingCast, material -> MATERIAL_SUPPORT.hasStat(material, PlatingMaterialStats.LEGGINGS.getId()), false),
    new SolidifierPart("boots_plating", () -> TinkerToolParts.plating.get(ArmorItem.Type.BOOTS), 2, TinkerSmeltery.bootsPlatingCast, material -> MATERIAL_SUPPORT.hasStat(material, PlatingMaterialStats.BOOTS.getId()), false),
    new SolidifierPart("maille", TinkerToolParts.maille::get, 2, TinkerSmeltery.mailleCast, material -> MATERIAL_SUPPORT.hasStat(material, StatlessMaterialStats.MAILLE.getIdentifier())),
    // gt tool heads
    new SolidifierPart("wrench_head", TinkerToolParts.wrenchHead::get, 2, TinkerSmeltery.wrenchHeadCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("wire_cutter_head", TinkerToolParts.wireCutterHead::get, 2, TinkerSmeltery.wireCutterHeadCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("file_head", TinkerToolParts.fileHead::get, 2, TinkerSmeltery.fileHeadCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("screwdriver_head", TinkerToolParts.screwdriverHead::get, 2, TinkerSmeltery.screwdriverHeadCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("saw_blade", TinkerToolParts.sawBlade::get, 2, TinkerSmeltery.sawBladeCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("crowbar_head", TinkerToolParts.crowbarHead::get, 2, TinkerSmeltery.crowbarHeadCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("mortar_head", TinkerToolParts.mortarHead::get, 2, TinkerSmeltery.mortarHeadCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID)),
    new SolidifierPart("mortar_bowl", TinkerToolParts.mortarBowl::get, 2, TinkerSmeltery.mortarBowlCast, material -> MATERIAL_SUPPORT.hasStat(material, HeadMaterialStats.ID))
  );

  public static void register(Consumer<FinishedRecipe> provider) {
    Map<ResourceLocation, Fluid> tinkersFluids = GTConstructFluid.getAllTinkersFluids();

    registerSpecialRecipes(provider);
    registerDirectFluidRecipes(provider);

    Set<Fluid> processedSpecialFluids = Set.of(
      TinkerFluids.skySlime.get(),
      TinkerFluids.enderSlime.get(),
      TinkerFluids.earthSlime.get(),
      TinkerFluids.blazingBlood.get(),
      TinkerFluids.scorchedStone.get(),
      TinkerFluids.searedStone.get(),
      TinkerFluids.moltenPigIron.get(),
      TinkerFluids.moltenCinderslime.get(),
      TinkerFluids.moltenSlimesteel.get(),
      TinkerFluids.moltenQueensSlime.get(),
      TinkerFluids.moltenKnightmetal.get(),
      TinkerFluids.moltenPlatinum.get()
    );

    for (Map.Entry<ResourceLocation, Fluid> entry : tinkersFluids.entrySet()) {
      ResourceLocation fluidId = entry.getKey();
      Fluid fluid = entry.getValue();

      if (processedSpecialFluids.contains(fluid)) {
        continue;
      }

      MaterialVariantId outputMaterialVariantId = resolveOutputMaterial(fluidId.getPath());
      if (outputMaterialVariantId == null) {
        continue;
      }

      generateStandardSolidifierRecipes(provider, fluid, outputMaterialVariantId);
    }
  }

  private static void registerSpecialRecipes(Consumer<FinishedRecipe> provider) {
    LOGGER.info("Registering hardcoded special/hot fluid recipes...");

    registerMaterialRecipe(provider, GTConstructRecipeType.builder()
      .inputFluids(TinkerFluids.skySlime.get())
      .baseMaterial(MaterialIds.wood)
      .voltage(LV), MaterialIds.skySlimeskin);

    registerMaterialRecipe(provider, GTConstructRecipeType.builder()
      .inputFluids(TinkerFluids.enderSlime.get())
      .baseMaterial(MaterialIds.leather)
      .voltage(LV), MaterialIds.enderSlimeskin);

    registerMaterialRecipe(provider, GTConstructRecipeType.builder()
      .inputFluids(TinkerFluids.earthSlime.get())
      .baseMaterial(MaterialIds.wood)
      .voltage(LV), MaterialIds.slimewoodComposite);

    registerMaterialRecipe(provider, GTConstructRecipeType.builder()
      .inputFluids(TinkerFluids.blazingBlood.get())
      .baseMaterial(MaterialIds.necroticBone)
      .voltage(LV), MaterialIds.blazingBone);
  }

  private static void registerDirectFluidRecipes(Consumer<FinishedRecipe> provider) {
    registerMaterialRecipe(provider, GTConstructRecipeType.builder()
      .inputFluids(TinkerFluids.scorchedStone.get())
      .voltage(LV), MaterialIds.scorchedStone);

    registerMaterialRecipe(provider, GTConstructRecipeType.builder()
      .inputFluids(TinkerFluids.searedStone.get())
      .voltage(LV), MaterialIds.searedStone);

    registerMaterialRecipe(provider, GTConstructRecipeType.builder()
      .inputFluids(TinkerFluids.moltenPigIron.get())
      .voltage(LV), MaterialIds.pigIron);

    registerMaterialRecipe(provider, GTConstructRecipeType.builder()
      .inputFluids(TinkerFluids.moltenCinderslime.get())
      .voltage(LV), MaterialIds.cinderslime);

    registerMaterialRecipe(provider, GTConstructRecipeType.builder()
      .inputFluids(TinkerFluids.moltenSlimesteel.get())
      .voltage(LV), MaterialIds.slimesteel);

    registerMaterialRecipe(provider, GTConstructRecipeType.builder()
      .inputFluids(TinkerFluids.moltenQueensSlime.get())
      .voltage(LV), MaterialIds.queensSlime);

    registerMaterialRecipe(provider, GTConstructRecipeType.builder()
      .inputFluids(TinkerFluids.moltenKnightmetal.get())
      .voltage(LV), MaterialIds.knightmetal);
  }

  private static void generateStandardSolidifierRecipes(Consumer<FinishedRecipe> provider, Fluid fluid, MaterialVariantId outputMaterialVariantId) {
    TagKey<Fluid> fluidTag = GTConstructFluid.getAutoTag(fluid);
    registerMaterialRecipe(provider, GTConstructRecipeType.builder()
      .inputFluidTag(fluidTag)
      .voltage(LV), outputMaterialVariantId);
  }

  private static void registerMaterialRecipe(Consumer<FinishedRecipe> provider, GTConstructRecipeType.DynamicRecipeBuilder builder, MaterialVariantId outputMaterial) {
    List<SolidifierPart> supportedParts = getSupportedParts(outputMaterial.getId(), DEFAULT_SOLIDIFIER_PARTS);
    if (supportedParts.isEmpty()) {
      return;
    }
    builder
      .outputMaterial(outputMaterial)
      .register(provider, supportedParts);
  }

  @Nullable
  static MaterialVariantId resolveOutputMaterial(String fluidPath) {
    String materialName = GTConstructFluid.extractMaterialName(fluidPath);
    String normalizedName = OUTPUT_MATERIAL_ALIASES.getOrDefault(materialName, materialName);
    return MaterialVariantId.tryParse(TConstruct.MOD_ID + ":" + normalizedName);
  }

  static List<SolidifierPart> getDefaultSolidifierParts() {
    return DEFAULT_SOLIDIFIER_PARTS;
  }

  static List<SolidifierPart> getSupportedParts(IMaterial material, List<SolidifierPart> parts) {
    return getSupportedParts(material.getIdentifier(), parts);
  }

  static List<SolidifierPart> getSupportedParts(MaterialId material, List<SolidifierPart> parts) {
    return parts.stream()
      .filter(part -> part.canUseMaterial(material))
      .toList();
  }

  record SolidifierPart(String path, Supplier<? extends Item> part, int materialCost, @Nullable CastItemObject cast, Predicate<MaterialId> support, boolean useToSeparator) {
    SolidifierPart(String path, Supplier<? extends Item> part, int materialCost, @Nullable CastItemObject cast, Predicate<MaterialId> support) {
      this(path, part, materialCost, cast, support, true);
    }

    boolean canUseMaterial(MaterialId material) {
      return support.test(material);
    }

    String recipePath(String recipeTypeName, String fluidNamePath) {
      return recipeTypeName + "_" + fluidNamePath + (useToSeparator ? "_to_" : "") + path;
    }
  }

  private static final class MaterialSupportIndex extends MaterialStatsDataProvider {
    private static final PackOutput OUTPUT = new PackOutput(Path.of("build", "tmp", "gtconstruct-material-support"));

    private final Map<MaterialId, Set<MaterialStatsId>> supportedStats = new HashMap<>();
    private final Set<MaterialId> repairKitMaterials = new HashSet<>();
    private boolean initialized = false;

    private MaterialSupportIndex() {
      super(OUTPUT, new EmptyMaterialDataProvider(OUTPUT));
    }

    private synchronized void ensureInitialized() {
      if (initialized) {
        return;
      }
      super.addMaterialStats();
      initialized = true;
    }

    boolean hasStat(MaterialId material, MaterialStatsId statId) {
      ensureInitialized();
      return supportedStats.getOrDefault(material, Set.of()).contains(statId);
    }

    boolean hasRepairKitSupport(MaterialId material) {
      ensureInitialized();
      return repairKitMaterials.contains(material);
    }

    @Override
    protected void addMaterialStats(MaterialId location, IMaterialStats... stats) {
      Set<MaterialStatsId> statIds = supportedStats.computeIfAbsent(location, ignored -> new HashSet<>());
      for (IMaterialStats stat : stats) {
        statIds.add(stat.getIdentifier());
        if (stat.getType().canRepair() || stat.getIdentifier().equals(StatlessMaterialStats.REPAIR_KIT.getIdentifier())) {
          repairKitMaterials.add(location);
        }
      }
    }

    @Override
    protected void addOptionalStats(MaterialId location, IMaterialStats... stats) {
      addMaterialStats(location, stats);
    }
  }

  private static final class EmptyMaterialDataProvider extends AbstractMaterialDataProvider {
    private EmptyMaterialDataProvider(PackOutput packOutput) {
      super(packOutput);
    }

    @Override
    public String getName() {
      return "GTConstruct Empty Material Provider";
    }

    @Override
    protected void addMaterials() {}
  }
}
