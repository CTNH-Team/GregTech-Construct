package slimeknights.tconstruct.plugin.botania;

import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.ITiCAddon;
import slimeknights.tconstruct.library.addon.ITiCStaticModifierAddon;
import slimeknights.tconstruct.library.addon.ITiCTagAddon;
import slimeknights.tconstruct.library.addon.TiCAddon;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.modifiers.Botania.ManaFixModifier;
import slimeknights.tconstruct.tools.modifiers.Botania.TerraRecoverModifier;

/**
 * Centralized Botania compat wiring for TiC.
 */
@TiCAddon(requiredMods = "botania")
public class BotaniaTiCAddon implements ITiCAddon, ITiCStaticModifierAddon, ITiCTagAddon {
  public static final String MOD_ID = "botania";

  @Override
  public String addonModId() {
    return MOD_ID;
  }

  @Override
  public void registerStaticModifiers(StaticModifierRegistrar registrar) {
    registrar.register("manafix", ManaFixModifier::new);
    registrar.register("terrarecover", TerraRecoverModifier::new);
  }

  @Override
  public void registerDynamicRecipeProviders(DynamicProviderRegistrar registrar) {
    registrar.addProvider("BotaniaModifierRecipeProvider", BotaniaModifierRecipeProvider::new);
    registrar.addProvider("BotaniaMaterialRecipeProvider", BotaniaMaterialRecipeProvider::new);
  }

  @Override
  public void registerDynamicMaterialProviders(DynamicProviderRegistrar registrar) {
    registrar.addProvider("BotaniaMaterialDataProvider", BotaniaMaterialDataProvider::new);
    registrar.addProvider("BotaniaMaterialStatsDataProvider", output -> new BotaniaMaterialStatsDataProvider(output, new BotaniaMaterialDataProvider(output)));
    registrar.addProvider("BotaniaMaterialTraitsDataProvider", output -> new BotaniaMaterialTraitsDataProvider(output, new BotaniaMaterialDataProvider(output)));
  }

  @Override
  public void registerDynamicResourceProviders(DynamicProviderRegistrar registrar) {
    registrar.addProvider("BotaniaMaterialRenderInfoProvider", BotaniaMaterialRenderInfoProvider::new);
    registrar.addProvider("BotaniaMaterialPartTextureGenerator", BotaniaMaterialPartTextureGenerator::new);
    registrar.addProvider("BotaniaMaterialPaletteDebugGenerator", BotaniaMaterialPaletteDebugGenerator::new);
  }

  @Override
  public void registerMaterialTags(MaterialTagRegistrar registrar) {
    registrar.addOptional(TinkerTags.Materials.COMPATABILITY_METALS, MaterialIds.manaSteel, MaterialIds.terraSteel);
    registrar.addOptional(TinkerTags.Materials.HARD_METALS, MaterialIds.manaSteel, MaterialIds.terraSteel);
    registrar.addOptional(TinkerTags.Materials.LIGHT, MaterialIds.manaSteel, MaterialIds.terraSteel);
  }

  @Override
  public void registerModifierTags(ModifierTagRegistrar registrar) {
    registrar.add(TinkerTags.Modifiers.GENERAL_UPGRADES, ModifierIds.manafix, ModifierIds.terrarecover);
  }
}
