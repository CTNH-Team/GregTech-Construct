package slimeknights.tconstruct.library.addon;

import net.minecraft.data.PackOutput;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

final class DynamicProviderSupport {
  private DynamicProviderSupport() {}

  static String providerName(Class<?> providerClass) {
    return Objects.requireNonNull(providerClass, "providerClass").getSimpleName();
  }

  static <T> DynamicPackProviderFactory<T> constructorFactory(Class<T> providerClass) {
    Objects.requireNonNull(providerClass, "providerClass");
    Constructor<T> constructor;
    try {
      constructor = providerClass.getConstructor(PackOutput.class);
    } catch (NoSuchMethodException exception) {
      throw new IllegalArgumentException(providerClass.getName() + " must expose a public PackOutput constructor", exception);
    }
    return output -> create(providerClass, constructor, output);
  }

  private static <T> T create(Class<T> providerClass, Constructor<T> constructor, PackOutput output) {
    try {
      return constructor.newInstance(output);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
      throw new IllegalStateException("Failed to create dynamic provider " + providerClass.getName(), exception);
    }
  }
}
