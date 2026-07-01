package slimeknights.tconstruct.library.addon;

import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.data.material.TiCDynamicMaterialGenerator;
import slimeknights.tconstruct.data.recipe.TiCDynamicRecipeGenerator;
import slimeknights.tconstruct.data.resource.TiCDynamicResourceGenerator;
import slimeknights.tconstruct.data.tinkering.TiCDynamicTinkeringGenerator;

/**
 * Internal addon so TiC core uses the same registration path as external addons.
 */
final class TConstructBuiltinAddon implements ITiCAddon {
  @Override
  public String addonModId() {
    return TConstruct.MOD_ID;
  }

  @Override
  public void registerDynamicRecipeProviders(DynamicRecipeProviderRegistrar registrar) {
    TiCDynamicRecipeGenerator.registerDefaultProviders(registrar);
  }

  @Override
  public void registerDynamicTinkeringProviders(DynamicProviderRegistrar registrar) {
    TiCDynamicTinkeringGenerator.registerDefaultProviders(registrar);
  }

  @Override
  public void registerDynamicMaterialProviders(DynamicProviderRegistrar registrar) {
    TiCDynamicMaterialGenerator.registerDefaultProviders(registrar);
  }

}
