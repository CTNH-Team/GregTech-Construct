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

  public static void collectRecipeProviders(DynamicRecipeProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDynamicRecipeProviders(registrar));
  }

  public static void collectTinkeringProviders(DynamicProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDynamicTinkeringProviders(registrar));
  }

  /**
   * Collects static tag providers from all addons for datagen (runData).
   * Tags are written to files during static datagen, not generated dynamically at runtime.
   */
  public static void collectDatagenTagProviders(DatagenTagProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDatagenTagProviders(registrar));
  }

  public static void collectMaterialProviders(DynamicProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDynamicMaterialProviders(registrar));
  }

  public static void collectResourceProviders(DynamicResourceProviderRegistrar registrar) {
    TiCAddonFinder.getAddons().forEach(addon -> addon.registerDynamicResourceProviders(registrar));
  }

}
