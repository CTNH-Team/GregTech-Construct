package slimeknights.tconstruct.library.addon;

import slimeknights.tconstruct.data.pack.DynamicPackOutput;
import slimeknights.tconstruct.library.data.RuntimeDataProvider;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface DynamicProviderRegistrar {
  void addProvider(String name, Consumer<DynamicDataRegistrar> writer);

  default void addProviderChecked(String name, Consumer<DynamicDataRegistrar> writer) {
    addProvider(Objects.requireNonNull(name, "name"), Objects.requireNonNull(writer, "writer"));
  }

  default <T extends RuntimeDataProvider> void addDataProvider(Class<T> providerClass) {
    addDataProvider(providerClass, DynamicProviderSupport.constructorFactory(providerClass));
  }

  default <T extends RuntimeDataProvider> void addDataProvider(Class<T> providerClass, DynamicPackProviderFactory<? extends T> factory) {
    addProviderChecked(DynamicProviderSupport.providerName(providerClass), registrar -> factory.create(DynamicPackOutput.dummy()).addToDynamicPack(registrar));
  }

  default void addDataWriter(Class<?> providerClass, Consumer<DynamicDataRegistrar> writer) {
    addProviderChecked(DynamicProviderSupport.providerName(providerClass), writer);
  }
}
