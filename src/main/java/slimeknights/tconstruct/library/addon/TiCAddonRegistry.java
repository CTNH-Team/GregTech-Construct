package slimeknights.tconstruct.library.addon;

import net.minecraft.world.item.CreativeModeTab;

/**
 * Internal helper for wiring addon callbacks into dynamic generators.
 */
public final class TiCAddonRegistry {
  private TiCAddonRegistry() {}

  public static void registerStaticModifiers(ITiCStaticModifierAddon.StaticModifierRegistrar registrar) {
    TiCAddonFinder.getAddons().stream()
      .filter(ITiCStaticModifierAddon.class::isInstance)
      .map(ITiCStaticModifierAddon.class::cast)
      .forEach(addon -> addon.registerStaticModifiers(registrar));
  }

  public static void initFluidContent() {
    collectSmelteryCompat(AddonSmelteryCompat::init);
  }

  public static void addFluidTabItems(CreativeModeTab.Output output) {
    collectSmelteryCompat(compat -> compat.addCreativeTabItems(output));
  }

  public static void collectSmelteryCompat(java.util.function.Consumer<AddonSmelteryCompat> registrar) {
    TiCAddonFinder.getAddons().stream()
      .filter(ITiCFluidAddon.class::isInstance)
      .map(ITiCFluidAddon.class::cast)
      .forEach(addon -> addon.registerSmelteryCompat(registrar));
  }

  public static void collectRecipeProviders(DynamicProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDynamicRecipeProviders(registrar));
  }

  public static void collectTinkeringProviders(DynamicProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDynamicTinkeringProviders(registrar));
  }

  public static void collectTagProviders(DynamicTagProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDynamicTagProviders(registrar));
  }

  public static void collectAdvancementProviders(DynamicProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDynamicAdvancementProviders(registrar));
  }

  public static void collectMaterialProviders(DynamicProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDynamicMaterialProviders(registrar));
  }

  public static void collectResourceProviders(DynamicProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDynamicResourceProviders(registrar));
  }

}
