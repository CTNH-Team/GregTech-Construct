package slimeknights.tconstruct.plugin.polymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.tconstruct.tables.block.entity.inventory.CraftingContainerWrapper;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Routes the station's recipe lookups through Polymorph so conflicting recipes can be resolved
 * by the player. On conflict it refreshes the candidate list for the Polymorph widget and picks
 * the player's selection, defaulting to the first match when no selection exists.
 */
public class PolymorphRecipeSelector implements CraftingStationBlockEntity.ICraftingRecipeSelector {
  @Nullable
  @Override
  public CraftingRecipe selectRecipe(CraftingStationBlockEntity tile, CraftingContainerWrapper inventory, List<CraftingRecipe> matches) {
    if (matches.size() <= 1) {
      return matches.get(0);
    }
    // conflicting recipes: let Polymorph pick the player's selection and push the candidates
    Optional<? extends IBlockEntityRecipeData> data = PolymorphApi.common().getRecipeData(tile);
    if (data.isPresent()) {
      IBlockEntityRecipeData recipeData = data.get();
      if (recipeData instanceof CraftingStationRecipeData stationData) {
        stationData.updateCandidates(matches);
      }
      return recipeData.getRecipe(RecipeType.CRAFTING, inventory, tile.getLevel(), matches).orElse(matches.get(0));
    }
    return matches.get(0);
  }
}
