package slimeknights.tconstruct.data.gtceu;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import net.minecraft.data.recipes.FinishedRecipe;
import slimeknights.tconstruct.TConstruct;

import java.util.function.Consumer;

import static slimeknights.tconstruct.TConstruct.MOD_ID;

@GTAddon
public class GTConstructAddon implements IGTAddon {
  @Override
  public GTRegistrate getRegistrate() {
    return GTRegistrate.create(MOD_ID);
  }

  @Override
  public void initializeAddon() {
  }

  @Override
  public String addonModId() {
    return TConstruct.MOD_ID;
  }

  @Override
  public void addRecipes(Consumer<FinishedRecipe> provider) {
    GTConstructRecipes.register(provider);
  }
}
