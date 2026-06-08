/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create;

import slimeknights.tconstruct.plugin.create.modifier.CreateCrushingModifier;
import slimeknights.tconstruct.plugin.create.modifier.CreateExtendoModifier;
import slimeknights.tconstruct.plugin.create.modifier.CreateModifierIds;
import slimeknights.tconstruct.plugin.create.modifier.CreateModifierProvider;
import slimeknights.tconstruct.plugin.create.modifier.CreateModifierRecipeProvider;
import slimeknights.tconstruct.plugin.create.tag.CreateBlockTagProvider;
import slimeknights.tconstruct.plugin.create.tag.CreateItemTagProvider;
import slimeknights.tconstruct.plugin.create.tag.CreateModifierTagProvider;

import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.DynamicTagProviderRegistrar;
import slimeknights.tconstruct.library.addon.ITiCAddon;
import slimeknights.tconstruct.library.addon.ITiCStaticModifierAddon;
import slimeknights.tconstruct.library.addon.TiCAddon;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;

@TiCAddon(requiredMods = CreateTiCAddon.MOD_ID)
public class CreateTiCAddon implements ITiCAddon, ITiCStaticModifierAddon {

    public static final String MOD_ID = "create";

    @Override
    public String addonModId() {
        return MOD_ID;
    }

    @Override
    public void registerStaticModifiers(StaticModifierRegistrar registrar) {
        registrar.register(CreateModifierIds.CRUSHING.getPath(), CreateCrushingModifier::new);
        registrar.register(CreateModifierIds.EXTENDO.getPath(), CreateExtendoModifier::new);
        registrar.register(CreateModifierIds.GOGGLES.getPath(), NoLevelsModifier::new);
        registrar.register(CreateModifierIds.WRENCH.getPath(), NoLevelsModifier::new);
        registrar.register(CreateModifierIds.DIVING_WEIGHTS.getPath(), NoLevelsModifier::new);
    }

    @Override
    public void registerDynamicRecipeProviders(DynamicProviderRegistrar registrar) {
        registrar.addProvider("CreateModifierRecipeProvider", CreateModifierRecipeProvider::new);
    }

    @Override
    public void registerDynamicTinkeringProviders(DynamicProviderRegistrar registrar) {
        registrar.addProvider("CreateModifierProvider", CreateModifierProvider::new);
    }

    @Override
    public void registerDynamicTagProviders(DynamicTagProviderRegistrar registrar) {
        registrar.addProvider("CreateBlockTagProvider", CreateBlockTagProvider::new);
        registrar.addProvider("CreateItemTagProvider", CreateItemTagProvider::new);
        registrar.addModifierTags(CreateModifierTagProvider::addTags);
    }
}
