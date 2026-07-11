package slimeknights.tconstruct.plugin.emi.material;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;
import slimeknights.tconstruct.library.tools.nbt.MaterialIdNBT;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.List;

public final class SkullStatsEmiRecipe extends MaterialStatsEmiRecipe {
  public SkullStatsEmiRecipe(EMIConstants.TConstructEmiCategory category, IMaterial material,
                             List<MaterialStatsId> statIds, RecipeManager recipeManager) {
    super(category, material, statIds, null, recipeManager,
        findFluid(material.getIdentifier()),
        findSkullParts(material, recipeManager));
  }

  public static List<ItemStack> findSkullParts(IMaterial material, RecipeManager recipeManager) {
    if (recipeManager == null) {
      return List.of();
    }
    return recipeManager.getAllRecipesFor(TinkerRecipeTypes.CASTING_BASIN.get()).stream()
        .filter(recipe -> recipe instanceof IDisplayableCastingRecipe)
        .map(recipe -> (IDisplayableCastingRecipe)recipe)
        .filter(recipe -> recipe.getOutputs().stream().anyMatch(output ->
            output.getItem() == TinkerTools.slimesuit.get(ArmorItem.Type.HELMET)
                && MaterialIdNBT.from(output).getMaterial(0).getId().equals(material.getIdentifier())))
        .findFirst()
        .map(recipe -> recipe.getCastItems().stream().map(ItemStack::copy).toList())
        .orElse(List.of());
  }
}
