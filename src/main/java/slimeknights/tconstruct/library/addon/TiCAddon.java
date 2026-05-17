package slimeknights.tconstruct.library.addon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an addon implementation for discovery by Tinkers' Construct.
 *
 * <p>License note: this addon discovery model is inspired by GregTech Modern's
 * {@code @GTAddon + IGTAddon} pattern. GregTech Modern is distributed under the
 * GNU Lesser General Public License v3.0, while this annotation and its
 * surrounding TiC addon API are an original implementation in this MIT-licensed
 * codebase.</p>
 *
 * <p>Annotate a concrete, zero-argument class that implements {@link ITiCAddon},
 * and {@link TiCAddonFinder} will discover and instantiate it during addon
 * collection.</p>
 *
 * @see ITiCAddon
 * @see TiCAddonFinder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TiCAddon {}
