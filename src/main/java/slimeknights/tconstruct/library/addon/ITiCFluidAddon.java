package slimeknights.tconstruct.library.addon;

import java.util.function.Consumer;

/**
 * Addon hooks for optional TiC fluid content.
 */
@SuppressWarnings("unused")
public interface ITiCFluidAddon {
  /** Registers addon-owned smeltery/fluid compat holders. */
  default void registerSmelteryCompat(Consumer<AddonSmelteryCompat> registrar) {}
}
