package slimeknights.tconstruct.library.addon;

/**
 * Optional addon integration hooks for Tinkers' Construct runtime dynamic data.
 */
@SuppressWarnings("unused")
public interface ITiCAddon {
  /**
   * @return addon mod ID used for bookkeeping and diagnostics.
   */
  String addonModId();

  default void registerDynamicRecipeProviders(DynamicProviderRegistrar registrar) {}

  default void registerDynamicTinkeringProviders(DynamicProviderRegistrar registrar) {}

  default void registerDynamicTagProviders(DynamicProviderRegistrar registrar) {}

  default void registerDynamicAdvancementProviders(DynamicProviderRegistrar registrar) {}

  default void registerDynamicMaterialProviders(DynamicProviderRegistrar registrar) {}

  default void registerDynamicResourceProviders(DynamicProviderRegistrar registrar) {}
}
