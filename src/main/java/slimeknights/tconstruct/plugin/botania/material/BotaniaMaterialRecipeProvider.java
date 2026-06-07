package slimeknights.tconstruct.plugin.botania.material;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import slimeknights.mantle.recipe.data.ItemNameIngredient;
import slimeknights.mantle.recipe.data.ItemNameOutput;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.library.data.recipe.IMaterialRecipeHelper;
import slimeknights.tconstruct.library.data.recipe.ISmelteryRecipeHelper;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipeBuilder;
import slimeknights.tconstruct.plugin.botania.BotaniaTiCAddon;
import slimeknights.tconstruct.plugin.botania.smeltery.BotaniaSmelteryCompat;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.function.Consumer;

/**
 * Botania-owned material recipes.
 */
public class BotaniaMaterialRecipeProvider extends BaseRecipeProvider implements IMaterialRecipeHelper, ISmelteryRecipeHelper {
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
    addBotaniaItemMelting(wrapped);
    addBotaniaItemCasting(wrapped);
  }

  private void addBotaniaItemCasting(Consumer<FinishedRecipe> consumer) {
    castingWithCast(consumer, BotaniaSmelteryCompat.moltenManaSteel, FluidValues.INGOT, TinkerSmeltery.ingotCast,
      ItemNameOutput.fromName(botaniaId("manasteel_ingot")), "smeltery/casting/botania/mana_steel/ingot");
    castingWithCast(consumer, BotaniaSmelteryCompat.moltenTerraSteel, FluidValues.INGOT, TinkerSmeltery.ingotCast,
      ItemNameOutput.fromName(botaniaId("terrasteel_ingot")), "smeltery/casting/botania/terra_steel/ingot");
    ItemCastingRecipeBuilder.basinRecipe(ItemNameOutput.fromName(botaniaId("manasteel_block")))
      .setFluidAndTime(BotaniaSmelteryCompat.moltenManaSteel, FluidValues.METAL_BLOCK)
      .save(consumer, location("smeltery/casting/botania/mana_steel/block"));
    ItemCastingRecipeBuilder.basinRecipe(ItemNameOutput.fromName(botaniaId("terrasteel_block")))
      .setFluidAndTime(BotaniaSmelteryCompat.moltenTerraSteel, FluidValues.METAL_BLOCK)
      .save(consumer, location("smeltery/casting/botania/terra_steel/block"));
  }

  private void addBotaniaItemMelting(Consumer<FinishedRecipe> consumer) {
    addBotaniaMetalMelting(consumer, BotaniaSmelteryCompat.moltenManaSteel, "mana_steel", "manasteel");
    addBotaniaMetalMelting(consumer, BotaniaSmelteryCompat.moltenTerraSteel, "terra_steel", "terrasteel");
  }

  private void addBotaniaMetalMelting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, String fluidName, String itemName) {
    String folder = "smeltery/melting/botania/" + fluidName + "/";
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(botaniaId(itemName + "_ingot")), fluid, FluidValues.INGOT)
      .save(consumer, location(folder + "ingot"));
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(botaniaId(itemName + "_block")), fluid, FluidValues.METAL_BLOCK)
      .save(consumer, location(folder + "block"));
  }

  private static ResourceLocation botaniaId(String path) {
    return ResourceLocation.tryBuild(BotaniaTiCAddon.MOD_ID, path);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Botania Material Recipes";
  }
}
