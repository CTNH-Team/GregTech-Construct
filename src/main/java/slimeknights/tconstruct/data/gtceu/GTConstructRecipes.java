package slimeknights.tconstruct.data.gtceu;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.LV;

public class GTConstructRecipes {
  private static final Logger LOGGER = Util.getLogger("GTRecipes");

  public static void register(Consumer<FinishedRecipe> provider) {
    List<SolidifierPart> parts = GTConstructSolidifierParts.create();
    List<TinkersMaterialFluidCatalog.MaterialFluidSpec> recipes = TinkersMaterialFluidCatalog.discover(parts);
    LOGGER.info("Registering {} GTM recipes from Tinkers' Construct material fluids.", recipes.size());
    for (TinkersMaterialFluidCatalog.MaterialFluidSpec recipe : recipes) {
      GTConstructRecipeType.DynamicRecipeBuilder builder = GTConstructRecipeType.builder().voltage(LV);
      if (recipe.inputFluidTag() != null) {
        builder.inputFluidTag(recipe.inputFluidTag());
      } else {
        builder.inputFluids(recipe.inputFluid());
      }
      if (recipe.baseMaterial() != null) {
        builder.baseMaterialVariant(recipe.baseMaterial());
      }
      builder.sourceRecipeId(recipe.recipeId());
      registerMaterialRecipe(provider, builder, recipe.outputMaterial(), parts);
    }
  }

  private static void registerMaterialRecipe(Consumer<FinishedRecipe> provider,
                                             GTConstructRecipeType.DynamicRecipeBuilder builder,
                                             MaterialVariantId outputMaterial,
                                             List<SolidifierPart> parts) {
    List<SolidifierPart> supportedParts = getSupportedParts(outputMaterial.getId(), parts);
    if (supportedParts.isEmpty()) {
      return;
    }
    builder.outputMaterial(outputMaterial).register(provider, supportedParts);
  }

  static List<SolidifierPart> getDefaultSolidifierParts() {
    return GTConstructSolidifierParts.create();
  }

  static List<SolidifierPart> getSupportedParts(MaterialId material, List<SolidifierPart> parts) {
    return parts.stream()
      .filter(part -> part.canUseMaterial(material))
      .toList();
  }

  record SolidifierPart(String path, Supplier<? extends Item> part, int materialCost,
                        @Nullable CastItemObject cast, Predicate<MaterialId> support,
                        boolean useToSeparator) {
    SolidifierPart(String path, Supplier<? extends Item> part, int materialCost,
                   @Nullable CastItemObject cast, Predicate<MaterialId> support) {
      this(path, part, materialCost, cast, support, true);
    }

    boolean canUseMaterial(MaterialId material) {
      return support.test(material);
    }

    String recipePath(String recipeTypeName, String fluidNamePath) {
      return recipeTypeName + "_" + fluidNamePath + (useToSeparator ? "_to_" : "") + path;
    }
  }
}
