package slimeknights.tconstruct.library.addon;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface DynamicRecipeProviderRegistrar {
  void addProvider(String name, Consumer<DynamicDataRegistrar> writer);

  default void addProviderChecked(String name, Consumer<DynamicDataRegistrar> writer) {
    addProvider(Objects.requireNonNull(name, "name"), Objects.requireNonNull(writer, "writer"));
  }
}
