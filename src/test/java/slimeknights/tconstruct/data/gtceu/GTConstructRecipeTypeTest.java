package slimeknights.tconstruct.data.gtceu;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import net.minecraftforge.fml.loading.FMLPaths;
import org.mockito.MockedStatic;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.MV;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GTConstructRecipeTypeTest extends BaseMcTest {
  private static final MaterialId TEST_BASE_MATERIAL = new MaterialId("tconstruct", "wood");
  private static MockedStatic<ModLoadingContext> modLoadingContext;

  static {
    FMLPaths.loadAbsolutePaths(Path.of("build", "test-game"));
  }

  @SuppressWarnings("deprecation")
  @BeforeAll
  static void enableGtmRegistryBootstrap() {
    ModLoadingContext context = mock(ModLoadingContext.class);
    ModContainer container = mock(ModContainer.class);
    when(container.getModId()).thenReturn("gtceu");
    when(context.getActiveContainer()).thenReturn(container);
    modLoadingContext = org.mockito.Mockito.mockStatic(ModLoadingContext.class);
    modLoadingContext.when(ModLoadingContext::get).thenReturn(context);
    if (GTRegistries.RECIPE_CAPABILITIES.isFrozen()) {
      GTRegistries.RECIPE_CAPABILITIES.unfreeze();
    }
    registerCapability(GTRecipeCapabilities.ITEM);
    registerCapability(GTRecipeCapabilities.FLUID);
    registerCapability(GTRecipeCapabilities.EU);
    registerCapability(GTRecipeCapabilities.CWU);
    GTRegistries.RECIPE_CAPABILITIES.freeze();
    try {
      CraftingHelper.register(ResourceLocation.tryBuild("forge", "nbt"), StrictNBTIngredient.Serializer.INSTANCE);
    } catch (IllegalArgumentException ignored) {
      // The shared test JVM may already have Forge's serializer registered.
    }
  }

  private static void registerCapability(RecipeCapability<?> capability) {
    if (!GTRegistries.RECIPE_CAPABILITIES.containKey(capability.name)) {
      GTRegistries.RECIPE_CAPABILITIES.register(capability.name, capability);
    }
  }

  @AfterAll
  static void disableGtmRegistryBootstrap() {
    if (modLoadingContext != null) {
      modLoadingContext.close();
    }
  }

  @Test
  void tagInputPreservesGtmFluidTagAndMaterialNbt() {
    TagKey<Fluid> fluidTag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), ResourceLocation.tryBuild("forge", "water"));
    GTConstructRecipes.SolidifierPart part = part("pick_head", 2);
    List<FinishedRecipe> recipes = new ArrayList<>();

    GTConstructRecipeType.builder()
      .inputFluidTag(fluidTag)
      .baseMaterial(TEST_BASE_MATERIAL)
      .outputMaterial(MaterialIds.iron)
      .duration(3)
      .register(recipes::add, List.of(part));

    assertThat(recipes).hasSize(1);
    assertThat(recipes.get(0).getId()).isEqualTo(ResourceLocation.tryBuild("gtceu", "fluid_solidifier/solidify_water_to_pick_head"));
    JsonObject json = serialize(recipes.get(0));
    assertThat(json.toString()).contains("forge:water", "tconstruct:iron", "tconstruct:wood");
    assertThat(json.get("duration").getAsInt()).isEqualTo(120);
  }

  @Test
  void sourceRecipeIdDisambiguatesRecipesWithSharedInput() {
    TagKey<Fluid> fluidTag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), ResourceLocation.tryBuild("forge", "water"));
    GTConstructRecipes.SolidifierPart part = part("pick_head", 1);
    List<FinishedRecipe> recipes = new ArrayList<>();

    GTConstructRecipeType.builder()
      .inputFluidTag(fluidTag)
      .baseMaterial(TEST_BASE_MATERIAL)
      .outputMaterial(MaterialIds.iron)
      .sourceRecipeId(ResourceLocation.tryBuild("tconstruct", "foo/bar"))
      .register(recipes::add, List.of(part));
    GTConstructRecipeType.builder()
      .inputFluidTag(fluidTag)
      .baseMaterial(TEST_BASE_MATERIAL)
      .outputMaterial(MaterialIds.iron)
      .sourceRecipeId(ResourceLocation.tryBuild("tconstruct", "foo_bar"))
      .register(recipes::add, List.of(part));

    assertThat(recipes).hasSize(2);
    assertThat(recipes.get(0).getId()).isNotEqualTo(recipes.get(1).getId());
  }

  @Test
  void exactFluidInputUsesFluidStackAndExplicitVacuumMode() {
    GTConstructRecipes.SolidifierPart part = part("pick_head", 2);
    List<FinishedRecipe> recipes = new ArrayList<>();

    GTConstructRecipeType.builder()
      .inputFluids(Fluids.WATER)
      .baseMaterial(TEST_BASE_MATERIAL)
      .outputMaterial(MaterialIds.iron)
      .voltage(LV)
      .inVacuumFreezer()
      .register(recipes::add, List.of(part));

    assertThat(recipes).hasSize(1);
    assertThat(recipes.get(0).getId()).isEqualTo(ResourceLocation.tryBuild("gtceu", "vacuum_freezer/vacuum_freeze_water_to_pick_head"));
    assertThat(serialize(recipes.get(0)).toString()).contains("minecraft:water");
  }

  @Test
  void variantBaseMaterialIsPreservedInMaterialNbt() {
    GTConstructRecipes.SolidifierPart part = part("pick_head", 2);
    List<FinishedRecipe> recipes = new ArrayList<>();

    GTConstructRecipeType.builder()
      .inputFluids(Fluids.WATER)
      .baseMaterialVariant(MaterialIds.skySlimeskin)
      .outputMaterial(MaterialIds.iron)
      .register(recipes::add, List.of(part));

    assertThat(serialize(recipes.get(0)).toString()).contains("tconstruct:skyslime_vine#slimeskin");
  }

  @Test
  void higherVoltageSelectsVacuumAndSolidifierOverrideRestoresSolidifier() {
    GTConstructRecipes.SolidifierPart part = part("pick_head", 1);
    List<FinishedRecipe> recipes = new ArrayList<>();

    GTConstructRecipeType.builder()
      .inputFluids(Fluids.WATER)
      .baseMaterial(TEST_BASE_MATERIAL)
      .outputMaterial(MaterialIds.iron)
      .voltage(MV)
      .inSolidifier()
      .register(recipes::add, List.of(part));

    assertThat(recipes).extracting(FinishedRecipe::getId)
      .containsExactly(ResourceLocation.tryBuild("gtceu", "fluid_solidifier/solidify_water_to_pick_head"));
  }

  @Test
  void conflictingInputSourcesFailAtRegistration() {
    TagKey<Fluid> fluidTag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), ResourceLocation.tryBuild("forge", "water"));

    assertThatThrownBy(() -> GTConstructRecipeType.builder()
      .inputFluids(Fluids.WATER)
      .inputFluidTag(fluidTag)
      .outputMaterial(MaterialIds.iron)
      .register(recipe -> {}, List.of(part("pick_head", 1))))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Exactly one input fluid or fluid tag");
  }

  @Test
  void invalidDurationAndVoltageFailAtRegistration() {
    assertThatThrownBy(() -> GTConstructRecipeType.builder()
      .inputFluids(Fluids.WATER)
      .outputMaterial(MaterialIds.iron)
      .duration(0)
      .register(recipe -> {}, List.of(part("pick_head", 1))))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Duration multiplier");

    assertThatThrownBy(() -> GTConstructRecipeType.builder()
      .inputFluids(Fluids.WATER)
      .outputMaterial(MaterialIds.iron)
      .voltage(com.gregtechceu.gtceu.api.GTValues.VA.length)
      .register(recipe -> {}, List.of(part("pick_head", 1))))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("voltage table");
  }

  @Test
  void noBaseMaterialRequiresCast() {
    assertThatThrownBy(() -> GTConstructRecipeType.builder()
      .inputFluids(Fluids.WATER)
      .outputMaterial(MaterialIds.iron)
      .register(recipe -> {}, List.of(part("pick_head", 1))))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("requires a cast");
  }

  @Test
  void unregisteredInputAndPartFailAtRegistration() {
    Fluid unregisteredFluid = mock(Fluid.class);
    assertThatThrownBy(() -> GTConstructRecipeType.builder()
      .inputFluids(unregisteredFluid)
      .baseMaterial(TEST_BASE_MATERIAL)
      .outputMaterial(MaterialIds.iron)
      .register(recipe -> {}, List.of(part("pick_head", 1))))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Input fluid is not registered");

    Item unregisteredPart = mock(Item.class);
    GTConstructRecipes.SolidifierPart customPart = new GTConstructRecipes.SolidifierPart(
      "custom",
      () -> unregisteredPart,
      1,
      null,
      material -> true
    );
    assertThatThrownBy(() -> GTConstructRecipeType.builder()
      .inputFluids(Fluids.WATER)
      .baseMaterial(TEST_BASE_MATERIAL)
      .outputMaterial(MaterialIds.iron)
      .register(recipe -> {}, List.of(customPart)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("unregistered item");
  }

  @Test
  void unregisteredCastFailsAtRegistration() {
    Item unregisteredCastItem = mock(Item.class);
    CastItemObject unregisteredCast = mock(CastItemObject.class);
    when(unregisteredCast.get()).thenReturn(unregisteredCastItem);
    GTConstructRecipes.SolidifierPart customPart = new GTConstructRecipes.SolidifierPart(
      "custom",
      () -> Items.STICK,
      1,
      unregisteredCast,
      material -> true
    );

    assertThatThrownBy(() -> GTConstructRecipeType.builder()
      .inputFluids(Fluids.WATER)
      .outputMaterial(MaterialIds.iron)
      .register(recipe -> {}, List.of(customPart)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("unregistered cast");
  }

  private static GTConstructRecipes.SolidifierPart part(String path, int materialCost) {
    return new GTConstructRecipes.SolidifierPart(path, () -> Items.STICK, materialCost, null, material -> true);
  }

  private static JsonObject serialize(FinishedRecipe recipe) {
    JsonObject json = new JsonObject();
    recipe.serializeRecipeData(json);
    return json;
  }
}
