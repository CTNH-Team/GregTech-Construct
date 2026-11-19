package slimeknights.tconstruct.library.addon;

import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Addon hooks for optional TiC fluid content.
 */
@SuppressWarnings("unused")
@Internal
public interface ITiCFluidAddon {
  /** Registers addon-owned smeltery/fluid compat holders. */
  default void registerSmelteryCompat(Consumer<AddonSmelteryCompat> registrar) {}
}
