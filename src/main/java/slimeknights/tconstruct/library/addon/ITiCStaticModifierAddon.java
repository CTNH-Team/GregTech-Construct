package slimeknights.tconstruct.library.addon;

import slimeknights.tconstruct.library.modifiers.Modifier;

import java.util.function.Supplier;

/**
 * Addon hook for static modifiers.
 */
@SuppressWarnings("unused")
public interface ITiCStaticModifierAddon {
  void registerStaticModifiers(StaticModifierRegistrar registrar);

  @FunctionalInterface
  interface StaticModifierRegistrar {
    void register(String name, Supplier<? extends Modifier> supplier);
  }
}
