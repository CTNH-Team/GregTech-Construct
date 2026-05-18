package slimeknights.tconstruct.plugin.botania.material;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.library.data.recipe.IMaterialRecipeHelper;
import slimeknights.tconstruct.plugin.botania.BotaniaTiCAddon;
import slimeknights.tconstruct.plugin.botania.smeltery.BotaniaSmelteryCompat;

import java.util.function.Consumer;

/**
 * Botania-owned material recipes.
 */
public class BotaniaMaterialRecipeProvider extends BaseRecipeProvider implements IMaterialRecipeHelper {
  public BotaniaMaterialRecipeProvider(PackOutput packOutput) {
    super(packOutput);
  }

  @Override
  protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
    Consumer<FinishedRecipe> wrapped = withCondition(consumer, new ModLoadedCondition(BotaniaTiCAddon.MOD_ID));
    String folder = "tools/materials/";
    metalMaterialRecipe(wrapped, BotaniaMaterialIds.manaSteel, folder, "manasteel", true);
    metalMaterialRecipe(wrapped, BotaniaMaterialIds.terraSteel, folder, "terrasteel", true);
    compatMeltingCasting(wrapped, BotaniaMaterialIds.manaSteel, BotaniaSmelteryCompat.moltenManaSteel, folder);
    compatMeltingCasting(wrapped, BotaniaMaterialIds.terraSteel, BotaniaSmelteryCompat.moltenTerraSteel, folder);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Botania Material Recipes";
  }
}
