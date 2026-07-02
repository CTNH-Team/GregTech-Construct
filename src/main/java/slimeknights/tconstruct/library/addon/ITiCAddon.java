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

  /**
   * Register runtime recipe providers that generate recipes dynamically at game startup.
   * Data is written to memory (TiCDynamicDataPack).
   */
  default void registerDynamicRecipeProviders(DynamicRecipeProviderRegistrar registrar) {}

  /**
   * Register runtime tinkering data providers (tool definitions, slot layouts, etc.).
   * Data is written to memory (TiCDynamicDataPack).
   */
  default void registerDynamicTinkeringProviders(DynamicProviderRegistrar registrar) {}

  /**
   * Register static tag providers for datagen (runData command).
   * Data is written to files in src/generated/resources/ during static datagen.
   */
  default void registerDatagenTagProviders(DatagenTagProviderRegistrar registrar) {}

  /**
   * Register runtime material providers that generate materials dynamically at game startup.
   * Data is written to memory (TiCDynamicDataPack).
   */
  default void registerDynamicMaterialProviders(DynamicProviderRegistrar registrar) {}

  /**
   * Register runtime resource providers that generate assets dynamically at game startup.
   * Data is written to memory (TiCDynamicResourcePack).
   */
  default void registerDynamicResourceProviders(DynamicResourceProviderRegistrar registrar) {}
}
