package slimeknights.tconstruct.library.addon;

/**
 * Internal helper for wiring addon callbacks into dynamic generators.
 */
public final class TiCAddonRegistry {
  private TiCAddonRegistry() {}

  public static void collectRecipeProviders(DynamicProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDynamicRecipeProviders(registrar));
  }

  public static void collectTinkeringProviders(DynamicProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDynamicTinkeringProviders(registrar));
  }

  public static void collectTagProviders(DynamicProviderRegistrar registrar) {
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
