package slimeknights.tconstruct.library.addon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an addon implementation for discovery by Tinkers' Construct.
 *
 * <p>Annotate a concrete, zero-argument class that implements {@link ITiCAddon},
 * and {@link TiCAddonFinder} will instantiate it during addon collection.</p>
 *
 * @see ITiCAddon
 * @see TiCAddonFinder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TiCAddon {
  /**
   * Mods that must be loaded before this addon is instantiated.
   */
  String[] requiredMods() default {};
}
