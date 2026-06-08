package slimeknights.tconstruct.plugin.botania;

import net.minecraftforge.common.MinecraftForge;
import slimeknights.tconstruct.plugin.botania.fluid.BotaniaFluidTextureCameraProvider;
import slimeknights.tconstruct.plugin.botania.fluid.BotaniaFluidTextureProvider;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialDataProvider;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialPaletteDebugGenerator;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialPartTextureGenerator;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialRecipeProvider;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialRenderInfoProvider;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialStatsDataProvider;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialTraitsDataProvider;
import slimeknights.tconstruct.plugin.botania.modifier.AncientWillModifier;
import slimeknights.tconstruct.plugin.botania.modifier.BotaniaModifierIds;
import slimeknights.tconstruct.plugin.botania.modifier.BotaniaModifiersProvider;
import slimeknights.tconstruct.plugin.botania.modifier.BotaniaModifierRecipeProvider;
import slimeknights.tconstruct.plugin.botania.modifier.ManaFixModifier;
import slimeknights.tconstruct.plugin.botania.modifier.TerraRecoverModifier;
import slimeknights.tconstruct.plugin.botania.smeltery.BotaniaSmelteryCompat;
import slimeknights.tconstruct.plugin.botania.tag.BotaniaFluidTagProvider;
import slimeknights.tconstruct.plugin.botania.tag.BotaniaMaterialTagProvider;
import slimeknights.tconstruct.plugin.botania.tag.BotaniaModifierTagProvider;

import slimeknights.tconstruct.library.addon.AddonFluidTextureProviderSet;
import slimeknights.tconstruct.library.addon.AddonSmelteryCompat;
import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.DynamicTagProviderRegistrar;
import slimeknights.tconstruct.library.addon.ITiCAddon;
import slimeknights.tconstruct.library.addon.ITiCFluidAddon;
import slimeknights.tconstruct.library.addon.ITiCStaticModifierAddon;
import slimeknights.tconstruct.library.addon.TiCAddon;

import java.util.function.Consumer;

/**
 * Centralized Botania compat wiring for TiC.
 */
@TiCAddon(requiredMods = BotaniaTiCAddon.MOD_ID)
public class BotaniaTiCAddon implements ITiCAddon, ITiCFluidAddon, ITiCStaticModifierAddon {

    public static final String MOD_ID = "botania";

    public BotaniaTiCAddon() {
        MinecraftForge.EVENT_BUS.addListener(AncientWillModifier::onManaDiscount);
        MinecraftForge.EVENT_BUS.addListener(AncientWillModifier::onPlayerTick);
    }

    @Override
    public String addonModId() {
        return MOD_ID;
    }

    @Override
    public void registerSmelteryCompat(Consumer<AddonSmelteryCompat> registrar) {
        registrar.accept(BotaniaSmelteryCompat.INSTANCE);
    }

    @Override
    public void registerStaticModifiers(StaticModifierRegistrar registrar) {
        registrar.register(BotaniaModifierIds.manafix.getPath(), ManaFixModifier::new);
        registrar.register(BotaniaModifierIds.terrarecover.getPath(), TerraRecoverModifier::new);
        registrar.register(BotaniaModifierIds.ancientWill.getPath(), AncientWillModifier::new);
        BotaniaModifiersProvider.registerSetBonuses();
    }

    @Override
    public void registerDynamicRecipeProviders(DynamicProviderRegistrar registrar) {
        registrar.addProvider("BotaniaModifierRecipeProvider", BotaniaModifierRecipeProvider::new);
        registrar.addProvider("BotaniaMaterialRecipeProvider", BotaniaMaterialRecipeProvider::new);
    }

    @Override
    public void registerDynamicMaterialProviders(DynamicProviderRegistrar registrar) {
        registrar.addProvider("BotaniaMaterialDataProvider", BotaniaMaterialDataProvider::new);
        registrar.addProvider("BotaniaMaterialStatsDataProvider", BotaniaMaterialStatsDataProvider::new);
        registrar.addProvider("BotaniaMaterialTraitsDataProvider", BotaniaMaterialTraitsDataProvider::new);
    }

    @Override
    public void registerDynamicResourceProviders(DynamicProviderRegistrar registrar) {
        AddonFluidTextureProviderSet<BotaniaFluidTextureProvider> fluidTextures = AddonFluidTextureProviderSet
                .create(BotaniaFluidTextureProvider::new);
        registrar.addProvider("BotaniaMaterialRenderInfoProvider", BotaniaMaterialRenderInfoProvider::new);
        registrar.addProvider("BotaniaFluidTextureProvider", fluidTextures::fluidTextures);
        registrar.addProvider("BotaniaFluidTextureCameraProvider",
                output -> fluidTextures.cameraProvider(output, BotaniaFluidTextureCameraProvider::new));
        registrar.addProvider("BotaniaMaterialPartTextureGenerator", BotaniaMaterialPartTextureGenerator::new);
        registrar.addProvider("BotaniaMaterialPaletteDebugGenerator", BotaniaMaterialPaletteDebugGenerator::new);
    }

    @Override
    public void registerDynamicTagProviders(DynamicTagProviderRegistrar registrar) {
        registrar.addFluidTags(BotaniaFluidTagProvider::addTags);
        registrar.addMaterialTags(BotaniaMaterialTagProvider::addTags);
        registrar.addModifierTags(BotaniaModifierTagProvider::addTags);
    }
}
