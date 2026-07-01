package slimeknights.tconstruct.library.addon;

import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.data.pack.DynamicPackOutput;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface DynamicRecipeProviderRegistrar {
  void addProvider(String name, Consumer<DynamicDataRegistrar> writer);

  default void addProviderChecked(String name, Consumer<DynamicDataRegistrar> writer) {
    addProvider(Objects.requireNonNull(name, "name"), Objects.requireNonNull(writer, "writer"));
  }

  default <T extends BaseRecipeProvider> void addRecipeProvider(Class<T> providerClass) {
    addRecipeProvider(providerClass, DynamicProviderSupport.constructorFactory(providerClass));
  }

  default <T extends BaseRecipeProvider> void addRecipeProvider(Class<T> providerClass, DynamicPackProviderFactory<? extends T> factory) {
    addProviderChecked(DynamicProviderSupport.providerName(providerClass), registrar -> factory.create(DynamicPackOutput.dummy()).buildRecipesDirect(registrar::addRecipe));
  }
}
