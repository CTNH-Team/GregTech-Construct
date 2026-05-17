package slimeknights.tconstruct.library.addon;

import slimeknights.tconstruct.library.modifiers.Modifier;

import java.util.function.Supplier;

/**
 * Optional addon hook for registering static runtime modifiers.
 */
@SuppressWarnings("unused")
public interface ITiCStaticModifierAddon {
  void registerStaticModifiers(StaticModifierRegistrar registrar);

  @FunctionalInterface
  interface StaticModifierRegistrar {
    void register(String name, Supplier<? extends Modifier> supplier);
  }
}
