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

  /**
   * Registers client resource providers for TiC's dynamic resource pack.
   *
   * <p>For providers that need an {@code ExistingFileHelper}, prefer using
   * {@link slimeknights.tconstruct.data.resource.TiCDynamicResourceGenerator#createExistingFileHelperForAddons()}.
   * That keeps external addons aligned with TiC's own dynamic resource generation setup.</p>
   */
  default void registerDynamicResourceProviders(DynamicProviderRegistrar registrar) {}
}
