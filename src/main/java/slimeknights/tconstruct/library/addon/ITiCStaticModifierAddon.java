package slimeknights.tconstruct.library.addon;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
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

    default void register(ModifierId id, Supplier<? extends Modifier> supplier) {
      register(Objects.requireNonNull(id, "id").getPath(), supplier);
    }

    default <T extends Modifier> void register(String name, Class<T> modifierClass) {
      register(name, () -> createModifier(modifierClass));
    }

    default <T extends Modifier> void register(ModifierId id, Class<T> modifierClass) {
      register(id, () -> createModifier(modifierClass));
    }

    private static <T extends Modifier> T createModifier(Class<T> modifierClass) {
      Objects.requireNonNull(modifierClass, "modifierClass");
      try {
        Constructor<T> constructor = modifierClass.getConstructor();
        return constructor.newInstance();
      } catch (NoSuchMethodException exception) {
        throw new IllegalArgumentException(modifierClass.getName() + " must expose a public no-arg constructor", exception);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
        throw new IllegalStateException("Failed to create static modifier " + modifierClass.getName(), exception);
      }
    }
  }
}
