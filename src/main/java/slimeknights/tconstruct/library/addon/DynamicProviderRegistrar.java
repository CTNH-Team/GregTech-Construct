package slimeknights.tconstruct.library.addon;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.function.Function;

/**
 * Registers addon data providers.
 */
@FunctionalInterface
public interface DynamicProviderRegistrar {
  void addProvider(String name, Function<PackOutput, ? extends DataProvider> factory);
}
