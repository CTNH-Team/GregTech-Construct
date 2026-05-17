package slimeknights.tconstruct.library.addon;

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

  public static void collectMaterialTagHooks(ITiCTagAddon.MaterialTagRegistrar registrar) {
    TiCAddonFinder.getAddons().stream()
      .filter(ITiCTagAddon.class::isInstance)
      .map(ITiCTagAddon.class::cast)
      .forEach(addon -> addon.registerMaterialTags(registrar));
  }

  public static void collectModifierTagHooks(ITiCTagAddon.ModifierTagRegistrar registrar) {
    TiCAddonFinder.getAddons().stream()
      .filter(ITiCTagAddon.class::isInstance)
      .map(ITiCTagAddon.class::cast)
      .forEach(addon -> addon.registerModifierTags(registrar));
  }
}
