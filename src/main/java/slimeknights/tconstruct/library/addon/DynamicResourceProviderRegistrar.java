package slimeknights.tconstruct.library.addon;

import slimeknights.tconstruct.data.pack.DynamicPackOutput;
import slimeknights.tconstruct.library.data.RuntimeResourceProvider;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface DynamicResourceProviderRegistrar {
  void addProvider(String name, Consumer<DynamicResourceRegistrar> writer);

  default void addProviderChecked(String name, Consumer<DynamicResourceRegistrar> writer) {
    addProvider(Objects.requireNonNull(name, "name"), Objects.requireNonNull(writer, "writer"));
  }

  default <T extends RuntimeResourceProvider> void addResourceProvider(Class<T> providerClass) {
    addResourceProvider(providerClass, DynamicProviderSupport.constructorFactory(providerClass));
  }

  default <T extends RuntimeResourceProvider> void addResourceProvider(Class<T> providerClass, DynamicPackProviderFactory<? extends T> factory) {
    addProviderChecked(DynamicProviderSupport.providerName(providerClass), registrar -> factory.create(DynamicPackOutput.dummy()).addToDynamicPack(registrar));
  }

  default void addResourceWriter(Class<?> providerClass, Consumer<DynamicResourceRegistrar> writer) {
    addProviderChecked(DynamicProviderSupport.providerName(providerClass), writer);
  }
}
