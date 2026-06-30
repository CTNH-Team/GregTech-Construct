package slimeknights.tconstruct.library.addon;

/**
 * Addon hooks for Tinkers' Construct dynamic data.
 */
@SuppressWarnings("unused")
public interface ITiCAddon {
  /**
   * @return mod ID used for bookkeeping and diagnostics.
   */
  String addonModId();

  default void registerDynamicRecipeProviders(DynamicRecipeProviderRegistrar registrar) {}

  default void registerDynamicTinkeringProviders(DynamicProviderRegistrar registrar) {}

  default void registerDynamicTagProviders(DynamicTagProviderRegistrar registrar) {}

  default void registerDynamicMaterialProviders(DynamicProviderRegistrar registrar) {}

  default void registerDynamicResourceProviders(DynamicResourceProviderRegistrar registrar) {}
}
