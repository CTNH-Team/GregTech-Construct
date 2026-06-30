package slimeknights.tconstruct.library.addon;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface DynamicResourceProviderRegistrar {
  void addProvider(String name, Consumer<DynamicResourceRegistrar> writer);

  default void addProviderChecked(String name, Consumer<DynamicResourceRegistrar> writer) {
    addProvider(Objects.requireNonNull(name, "name"), Objects.requireNonNull(writer, "writer"));
  }
}
