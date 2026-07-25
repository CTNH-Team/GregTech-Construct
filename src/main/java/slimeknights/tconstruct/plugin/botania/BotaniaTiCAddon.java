package slimeknights.tconstruct.plugin.botania;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
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
import slimeknights.tconstruct.plugin.botania.modifier.TerraSetBonusModifier;
import slimeknights.tconstruct.plugin.botania.smeltery.BotaniaSmelteryCompat;
import slimeknights.tconstruct.plugin.botania.tag.BotaniaFluidTagProvider;
import slimeknights.tconstruct.plugin.botania.tag.BotaniaMaterialTagProvider;
import slimeknights.tconstruct.plugin.botania.tag.BotaniaModifierTagProvider;

import slimeknights.tconstruct.library.addon.AddonFluidTextureProviderSet;
import slimeknights.tconstruct.library.addon.AddonSmelteryCompat;
import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.DynamicRecipeProviderRegistrar;
import slimeknights.tconstruct.library.addon.DynamicResourceProviderRegistrar;
import slimeknights.tconstruct.library.addon.DatagenTagProviderRegistrar;
import slimeknights.tconstruct.library.addon.ITiCAddon;
import slimeknights.tconstruct.library.addon.ITiCFluidAddon;
import slimeknights.tconstruct.library.addon.ITiCStaticModifierAddon;
import slimeknights.tconstruct.library.addon.TiCAddon;

import java.util.function.Consumer;

/**
 * Centralized Botania compat wiring for TiC.
 */
@TiCAddon(modID = BotaniaTiCAddon.MOD_ID)
public class BotaniaTiCAddon implements ITiCAddon, ITiCFluidAddon, ITiCStaticModifierAddon {

    public static final String MOD_ID = "botania";

    public BotaniaTiCAddon() {
        MinecraftForge.EVENT_BUS.addListener(TerraSetBonusModifier::onManaDiscount);
        MinecraftForge.EVENT_BUS.addListener(TerraSetBonusModifier::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, AncientWillModifier::onCriticalHit);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, AncientWillModifier::onLivingAttack);
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
        registrar.register(BotaniaModifierIds.manafix, ManaFixModifier.class);
        registrar.register(BotaniaModifierIds.terrarecover, TerraRecoverModifier.class);
        registrar.register(BotaniaModifierIds.ancientWill, TerraSetBonusModifier.class);
        for (AncientWillModifier.Will will : AncientWillModifier.Will.values()) {
            registrar.register(will.modifierId(), () -> new AncientWillModifier(will));
        }
        BotaniaModifiersProvider.registerSetBonuses();
    }

    @Override
    public void registerDynamicRecipeProviders(DynamicRecipeProviderRegistrar registrar) {
        registrar.addRecipeProvider(BotaniaModifierRecipeProvider.class);
        registrar.addRecipeProvider(BotaniaMaterialRecipeProvider.class);
    }

    @Override
    public void registerDynamicMaterialProviders(DynamicProviderRegistrar registrar) {
        registrar.addDataProvider(BotaniaMaterialDataProvider.class);
        registrar.addDataProvider(BotaniaMaterialStatsDataProvider.class);
        registrar.addDataProvider(BotaniaMaterialTraitsDataProvider.class);
    }

    @Override
    public void registerDynamicResourceProviders(DynamicResourceProviderRegistrar registrar) {
        AddonFluidTextureProviderSet<BotaniaFluidTextureProvider> fluidTextures = AddonFluidTextureProviderSet
                .create(BotaniaFluidTextureProvider::new);
        registrar.addResourceProvider(BotaniaMaterialRenderInfoProvider.class);
        registrar.addResourceWriter(BotaniaFluidTextureProvider.class, fluidTextures::addFluidTextures);
        registrar.addResourceWriter(BotaniaFluidTextureCameraProvider.class, fluidTextures::addCameraTextures);
        registrar.addResourceProvider(BotaniaMaterialPartTextureGenerator.class);
        registrar.addResourceProvider(BotaniaMaterialPaletteDebugGenerator.class);
    }

    @Override
    public void registerDatagenTagProviders(DatagenTagProviderRegistrar registrar) {
        registrar.addFluidTags(BotaniaFluidTagProvider::addTags);
        registrar.addMaterialTags(BotaniaMaterialTagProvider::addTags);
        registrar.addModifierTags(BotaniaModifierTagProvider::addTags);
    }
}
