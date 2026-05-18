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

  default void registerDynamicRecipeProviders(DynamicProviderRegistrar registrar) {}

  default void registerDynamicTinkeringProviders(DynamicProviderRegistrar registrar) {}

  default void registerDynamicTagProviders(DynamicTagProviderRegistrar registrar) {}

  default void registerDynamicAdvancementProviders(DynamicProviderRegistrar registrar) {}

  default void registerDynamicMaterialProviders(DynamicProviderRegistrar registrar) {}

  /**
   * Registers client resource providers.
   *
   * <p>For providers that need an {@code ExistingFileHelper}, prefer using
   * {@link slimeknights.tconstruct.data.resource.TiCDynamicResourceGenerator#createExistingFileHelperForAddons()}.
   * That keeps addons aligned with TiC's resource generation setup.</p>
   */
  default void registerDynamicResourceProviders(DynamicProviderRegistrar registrar) {}
}
