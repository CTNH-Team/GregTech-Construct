package slimeknights.tconstruct.data.gtceu;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.data.DynamicConditionSerializerRegistrar;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialManager;
import slimeknights.tconstruct.library.materials.json.MaterialJson;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialFluidRecipe;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GTConstructRecipesTest extends BaseMcTest {
  @Test
  void recipeTagFallsBackToLocalTagWhenCommonTagMissing() {
    net.minecraft.resources.ResourceLocation steeleafId = net.minecraft.resources.ResourceLocation.tryBuild("tconstruct", "steeleaf");
    TagKey<Fluid> localTag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), steeleafId);

    assertThat(GTConstructFluid.selectRecipeTag(null, localTag, steeleafId))
      .isEqualTo(localTag);
  }

  @Test
  void recipeTagPrefersCommonTagOverLocalTag() {
    ResourceLocation commonId = ResourceLocation.tryBuild("forge", "slime");
    ResourceLocation localId = ResourceLocation.tryBuild("tconstruct", "slime");
    TagKey<Fluid> commonTag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), commonId);
    TagKey<Fluid> localTag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), localId);

    assertThat(GTConstructFluid.selectRecipeTag(commonTag, localTag, localId))
      .isEqualTo(commonTag);
  }

  @Test
  void supportedPartsAreFilteredByMaterialCompatibility() {
    MaterialId material = new MaterialId("test", "material");

    GTConstructRecipes.SolidifierPart supported = new GTConstructRecipes.SolidifierPart(
      "supported",
      () -> mock(Item.class),
      2,
      null,
      checked -> checked.equals(material)
    );
    GTConstructRecipes.SolidifierPart unsupported = new GTConstructRecipes.SolidifierPart(
      "unsupported",
      () -> mock(Item.class),
      2,
      null,
      checked -> false
    );

    assertThat(GTConstructRecipes.getSupportedParts(material, List.of(supported, unsupported)))
      .extracting(GTConstructRecipes.SolidifierPart::path)
      .containsExactly("supported");
  }

  @Test
  void compatMaterialFluidsRemainTagOnlyInPublicMapButEnterCatalog() {
    assertThat(TinkerFluids.isCompatMaterialFluid(TinkerFluids.moltenIron)).isTrue();
    assertThat(TinkerFluids.moltenIron.getTag().location()).isEqualTo(ResourceLocation.tryBuild("forge", "iron"));
    assertThat(GTConstructFluid.getAllTinkersFluids())
      .doesNotContainKey(TinkerFluids.moltenIron.getId());
    assertThat(TinkersFluidCatalog.instance().containsTag(TinkerFluids.moltenCopper.getTag())).isTrue();
    assertThat(TinkersFluidCatalog.instance().containsTag(TinkerFluids.moltenSteel.getTag())).isTrue();
  }

  @Test
  void solidifierPartsUseDynamicCostsAndStats() {
    ToolPartItem item = mock(ToolPartItem.class);
    when(item.getStatType()).thenReturn(HeadMaterialStats.ID);
    CastItemObject cast = mock(CastItemObject.class);
    when(cast.getName()).thenReturn(ResourceLocation.tryBuild("tconstruct", "pick_head"));

    List<GTConstructRecipes.SolidifierPart> parts = TinkersPartScanner.build(
      List.of(new TinkersPartScanner.PartItem("pick_head", item, cast, HeadMaterialStats.ID)),
      Map.of(item, 7),
      GTConstructMaterialSupport.INSTANCE
    );

    assertThat(parts).singleElement().satisfies(part -> {
      assertThat(part.materialCost()).isEqualTo(7);
      assertThat(part.canUseMaterial(MaterialIds.iron)).isTrue();
      assertThat(part.cast()).isSameAs(cast);
    });
  }

  @Test
  void materialTagToolPartsRestrictMaterialsBeyondStats() {
    ToolPartItem item = mock(ToolPartItem.class);
    when(item.getStatType()).thenReturn(HeadMaterialStats.ID);
    CastItemObject cast = mock(CastItemObject.class);
    when(cast.getName()).thenReturn(ResourceLocation.tryBuild("tconstruct", "screwdriver_head"));

    List<GTConstructRecipes.SolidifierPart> parts = TinkersPartScanner.build(
      List.of(new TinkersPartScanner.PartItem(
        "screwdriver_head",
        item,
        cast,
        HeadMaterialStats.ID,
        TinkerTags.Materials.METALS
      )),
      Map.of(item, 2),
      GTConstructMaterialSupport.INSTANCE
    );

    assertThat(parts).singleElement().satisfies(part -> {
      assertThat(part.canUseMaterial(MaterialIds.iron)).isTrue();
      assertThat(part.canUseMaterial(MaterialIds.searedStone)).isFalse();
    });
  }

  @Test
  void fluidCatalogIsSortedAndImmutable() {
    ResourceLocation firstId = ResourceLocation.tryBuild("tconstruct", "a_fluid");
    ResourceLocation secondId = ResourceLocation.tryBuild("tconstruct", "b_fluid");
    Fluid first = mock(Fluid.class);
    Fluid second = mock(Fluid.class);
    TagKey<Fluid> firstTag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), firstId);
    TagKey<Fluid> secondTag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), secondId);

    TinkersFluidCatalog catalog = new TinkersFluidCatalog(List.of(
      new TinkersFluidScanner.FluidMapping(secondId, second, secondTag),
      new TinkersFluidScanner.FluidMapping(firstId, first, firstTag)
    ));

    assertThat(catalog.fluids().keySet()).containsExactly(firstId, secondId);
    assertThatThrownBy(() -> catalog.fluids().clear())
      .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void fluidCatalogFallsBackToForgeTagForUnknownRegisteredFluid() {
    assertThat(new TinkersFluidCatalog(List.of()).recipeTag(net.minecraft.world.level.material.Fluids.WATER))
      .isEqualTo(TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), ResourceLocation.tryBuild("forge", "water")));
  }

  @Test
  void materialFluidCatalogPreservesDynamicInputKindAndCompositeBase() {
    ResourceLocation tagId = ResourceLocation.tryBuild("tconstruct", "dynamic_test_fluid");
    TagKey<Fluid> tag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), tagId);
    TinkersFluidCatalog catalog = new TinkersFluidCatalog(List.of(
      new TinkersFluidScanner.FluidMapping(tagId, Fluids.WATER, tag)
    ));

    MaterialFluidRecipe exact = new MaterialFluidRecipe(
      ResourceLocation.tryBuild("tconstruct", "dynamic_exact"),
      FluidIngredient.of(Fluids.WATER, 144),
      1,
      null,
      MaterialIds.iron
    );
    MaterialFluidRecipe composite = new MaterialFluidRecipe(
      ResourceLocation.tryBuild("tconstruct", "dynamic_composite"),
      FluidIngredient.of(tag, 144),
      1,
      MaterialIds.leather,
      MaterialIds.skySlimeskin
    );
    try {
      List<TinkersMaterialFluidCatalog.MaterialFluidSpec> specs = TinkersMaterialFluidCatalog.discover(
        catalog,
        List.of(exact),
        List.of(composite),
        List.of(solidifierPart("pick_head"))
      );

      assertThat(specs).hasSize(2);
      assertThat(specs)
        .filteredOn(spec -> spec.outputMaterial().equals(MaterialIds.iron))
        .singleElement()
        .satisfies(spec -> {
          assertThat(spec.inputFluid()).isEqualTo(Fluids.WATER);
          assertThat(spec.inputFluidTag()).isNull();
          assertThat(spec.baseMaterial()).isNull();
        });
      assertThat(specs)
        .filteredOn(spec -> spec.outputMaterial().equals(MaterialIds.skySlimeskin))
        .singleElement()
        .satisfies(spec -> {
          assertThat(spec.inputFluid()).isNull();
          assertThat(spec.inputFluidTag()).isEqualTo(tag);
          assertThat(spec.baseMaterial()).isEqualTo(MaterialIds.leather);
        });
    } finally {
      MaterialCastingLookup.getAllCastingFluids().remove(exact);
      MaterialCastingLookup.getAllCompositeFluids().remove(composite);
    }
  }

  @Test
  void materialFluidCatalogPreservesMultipleCandidatesForOneInput() {
    ResourceLocation tagId = ResourceLocation.tryBuild("tconstruct", "shared_test_fluid");
    TagKey<Fluid> tag = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), tagId);
    TinkersFluidCatalog catalog = new TinkersFluidCatalog(List.of(
      new TinkersFluidScanner.FluidMapping(tagId, Fluids.WATER, tag)
    ));

    MaterialFluidRecipe casting = new MaterialFluidRecipe(
      ResourceLocation.tryBuild("tconstruct", "shared_casting"),
      FluidIngredient.of(tag, 144),
      1,
      null,
      MaterialIds.iron
    );
    MaterialFluidRecipe composite = new MaterialFluidRecipe(
      ResourceLocation.tryBuild("tconstruct", "shared_composite"),
      FluidIngredient.of(tag, 144),
      1,
      MaterialIds.leather,
      MaterialIds.skySlimeskin
    );

    try {
      List<TinkersMaterialFluidCatalog.MaterialFluidSpec> specs = TinkersMaterialFluidCatalog.discover(
        catalog,
        List.of(casting),
        List.of(composite),
        List.of(solidifierPart("pick_head"))
      );

      assertThat(specs)
        .extracting(TinkersMaterialFluidCatalog.MaterialFluidSpec::outputMaterial)
        .containsExactly(MaterialIds.iron, MaterialIds.skySlimeskin);
      assertThat(specs)
        .allSatisfy(spec -> assertThat(spec.inputFluidTag()).isEqualTo(tag));
      assertThat(specs)
        .filteredOn(spec -> spec.outputMaterial().equals(MaterialIds.skySlimeskin))
        .singleElement()
        .satisfies(spec -> assertThat(spec.baseMaterial()).isEqualTo(MaterialIds.leather));
    } finally {
      MaterialCastingLookup.getAllCastingFluids().remove(casting);
      MaterialCastingLookup.getAllCompositeFluids().remove(composite);
    }
  }

  @Test
  void materialFluidCatalogAcceptsCompatTagOnlyFluidRecipes() {
    MaterialFluidRecipe copper = new MaterialFluidRecipe(
      ResourceLocation.tryBuild("tconstruct", "dynamic_copper"),
      FluidIngredient.of(TinkerFluids.moltenCopper.getTag(), 144),
      1,
      null,
      MaterialIds.copper
    );
    try {
      List<TinkersMaterialFluidCatalog.MaterialFluidSpec> specs = TinkersMaterialFluidCatalog.discover(
        TinkersFluidCatalog.instance(),
        List.of(copper),
        List.of(),
        List.of(solidifierPart("pick_head")),
        material -> true
      );

      assertThat(specs).singleElement().satisfies(spec -> {
        assertThat(spec.inputFluid()).isNull();
        assertThat(spec.inputFluidTag()).isEqualTo(TinkerFluids.moltenCopper.getTag());
        assertThat(spec.outputMaterial()).isEqualTo(MaterialIds.copper);
      });
    } finally {
      MaterialCastingLookup.getAllCastingFluids().remove(copper);
    }
  }

  @Test
  void dynamicMaterialConditionControlsMaterialAvailability() {
    DynamicConditionSerializerRegistrar.registerCommonSerializers();
    MaterialId material = new MaterialId("test", "conditional_material");
    TagKey<Item> missingItems = TagKey.create(
      Registries.ITEM,
      ResourceLocation.tryBuild("test", "missing_items")
    );
    MaterialJson definition = new MaterialJson(
      new TagFilledCondition<>(missingItems),
      true,
      1,
      1,
      false,
      null
    );
    ResourceLocation location = ResourceLocation.tryBuild(
      material.getNamespace(),
      MaterialManager.FOLDER + "/" + material.getPath() + ".json"
    );
    TiCDynamicDataPack.addData(
      location,
      MaterialManager.GSON.toJson(definition).getBytes(StandardCharsets.UTF_8)
    );
    try {
      assertThat(GTConstructMaterialSupport.INSTANCE.isMaterialAvailable(material)).isFalse();
    } finally {
      TiCDynamicDataPack.clearServer();
    }
  }

  private static GTConstructRecipes.SolidifierPart solidifierPart(String path) {
    return new GTConstructRecipes.SolidifierPart(path, () -> mock(Item.class), 1, null, material -> true);
  }

}
