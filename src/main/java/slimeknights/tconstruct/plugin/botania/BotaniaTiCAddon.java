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
import slimeknights.tconstruct.plugin.botania.recipe.TerrasteelHelmetPlatingIngredient;
import slimeknights.tconstruct.plugin.botania.smeltery.BotaniaSmelteryCompat;
import slimeknights.tconstruct.plugin.botania.tag.BotaniaFluidTagProvider;
import slimeknights.tconstruct.plugin.botania.tag.BotaniaMaterialTagProvider;
import slimeknights.tconstruct.plugin.botania.tag.BotaniaModifierTagProvider;

import slimeknights.tconstruct.library.addon.AddonFluidTextureProviderSet;
import slimeknights.tconstruct.library.addon.AddonSmelteryCompat;
import slimeknights.tconstruct.data.material.TiCDynamicMaterialGenerator;
import slimeknights.tconstruct.data.recipe.TiCDynamicRecipeGenerator;
import slimeknights.tconstruct.data.resource.TiCDynamicResourceGenerator;
import slimeknights.tconstruct.library.addon.DynamicResourceRegistrar;
import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.DynamicRecipeProviderRegistrar;
import slimeknights.tconstruct.library.addon.DynamicResourceProviderRegistrar;
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
        TerrasteelHelmetPlatingIngredient.register();
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
        registrar.register(BotaniaModifierIds.manafix.getPath(), ManaFixModifier::new);
        registrar.register(BotaniaModifierIds.terrarecover.getPath(), TerraRecoverModifier::new);
        //ancientWill
        registrar.register(BotaniaModifierIds.ancientWill.getPath(), TerraSetBonusModifier::new);
        registrar.register(BotaniaModifierIds.ancientWillAhrim.getPath(), () -> new AncientWillModifier(AncientWillModifier.Will.AHRIM));
        registrar.register(BotaniaModifierIds.ancientWillDharok.getPath(), () -> new AncientWillModifier(AncientWillModifier.Will.DHAROK));
        registrar.register(BotaniaModifierIds.ancientWillGuthan.getPath(), () -> new AncientWillModifier(AncientWillModifier.Will.GUTHAN));
        registrar.register(BotaniaModifierIds.ancientWillTorag.getPath(), () -> new AncientWillModifier(AncientWillModifier.Will.TORAG));
        registrar.register(BotaniaModifierIds.ancientWillVerac.getPath(), () -> new AncientWillModifier(AncientWillModifier.Will.VERAC));
        registrar.register(BotaniaModifierIds.ancientWillKaril.getPath(), () -> new AncientWillModifier(AncientWillModifier.Will.KARIL));
        BotaniaModifiersProvider.registerSetBonuses();
    }

    @Override
    public void registerDynamicRecipeProviders(DynamicRecipeProviderRegistrar registrar) {
        recipe(registrar, BotaniaModifierRecipeProvider.class, BotaniaModifierRecipeProvider::new);
        recipe(registrar, BotaniaMaterialRecipeProvider.class, BotaniaMaterialRecipeProvider::new);
    }

    @Override
    public void registerDynamicMaterialProviders(DynamicProviderRegistrar registrar) {
        material(registrar, BotaniaMaterialDataProvider.class, BotaniaMaterialDataProvider::new);
        material(registrar, BotaniaMaterialStatsDataProvider.class, BotaniaMaterialStatsDataProvider::new);
        material(registrar, BotaniaMaterialTraitsDataProvider.class, BotaniaMaterialTraitsDataProvider::new);
    }

    @Override
    public void registerDynamicResourceProviders(DynamicResourceProviderRegistrar registrar) {
        AddonFluidTextureProviderSet<BotaniaFluidTextureProvider> fluidTextures = AddonFluidTextureProviderSet
                .create(BotaniaFluidTextureProvider::new);
        resource(registrar, BotaniaMaterialRenderInfoProvider.class, BotaniaMaterialRenderInfoProvider::new);
        resource(registrar, BotaniaFluidTextureProvider.class, fluidTextures::addFluidTextures);
        resource(registrar, BotaniaFluidTextureCameraProvider.class, fluidTextures::addCameraTextures);
        resource(registrar, BotaniaMaterialPartTextureGenerator.class, BotaniaMaterialPartTextureGenerator::new);
        resource(registrar, BotaniaMaterialPaletteDebugGenerator.class, BotaniaMaterialPaletteDebugGenerator::new);
    }

    @Override
    public void registerDynamicTagProviders(DynamicTagProviderRegistrar registrar) {
        registrar.addFluidTags(BotaniaFluidTagProvider::addTags);
        registrar.addMaterialTags(BotaniaMaterialTagProvider::addTags);
        registrar.addModifierTags(BotaniaModifierTagProvider::addTags);
    }

    private static void recipe(DynamicRecipeProviderRegistrar registrar, Class<?> providerClass, TiCDynamicRecipeGenerator.RecipeProviderFactory factory) {
        registrar.addProvider(providerClass.getSimpleName(), TiCDynamicRecipeGenerator.recipeWriter(factory));
    }

    private static void material(DynamicProviderRegistrar registrar, Class<?> providerClass, TiCDynamicMaterialGenerator.RuntimeProviderFactory<?> factory) {
        registrar.addProvider(providerClass.getSimpleName(), TiCDynamicMaterialGenerator.dataWriter(factory));
    }

    private static void resource(DynamicResourceProviderRegistrar registrar, Class<?> providerClass, TiCDynamicResourceGenerator.RuntimeResourceProviderFactory<?> factory) {
        resource(registrar, providerClass, TiCDynamicResourceGenerator.runtime(factory));
    }

    private static void resource(DynamicResourceProviderRegistrar registrar, Class<?> providerClass, Consumer<DynamicResourceRegistrar> writer) {
        registrar.addProvider(providerClass.getSimpleName(), writer);
    }
}
