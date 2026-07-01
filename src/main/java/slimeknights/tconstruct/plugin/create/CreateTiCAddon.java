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
import slimeknights.tconstruct.library.addon.DynamicRecipeProviderRegistrar;
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
        registrar.register(CreateModifierIds.CRUSHING, CreateCrushingModifier.class);
        registrar.register(CreateModifierIds.EXTENDO, CreateExtendoModifier.class);
        registrar.register(CreateModifierIds.GOGGLES, NoLevelsModifier.class);
        registrar.register(CreateModifierIds.WRENCH, NoLevelsModifier.class);
        registrar.register(CreateModifierIds.DIVING_WEIGHTS, NoLevelsModifier.class);
    }

    @Override
    public void registerDynamicRecipeProviders(DynamicRecipeProviderRegistrar registrar) {
        registrar.addRecipeProvider(CreateModifierRecipeProvider.class);
    }

    @Override
    public void registerDynamicTinkeringProviders(DynamicProviderRegistrar registrar) {
        registrar.addDataProvider(CreateModifierProvider.class);
    }

    @Override
    public void registerDynamicTagProviders(DynamicTagProviderRegistrar registrar) {
        registrar.addBlockTags(CreateBlockTagProvider::addTags);
        registrar.addItemTags(CreateItemTagProvider::addTags);
        registrar.addModifierTags(CreateModifierTagProvider::addTags);
    }
}
