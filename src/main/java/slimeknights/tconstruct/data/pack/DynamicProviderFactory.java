package slimeknights.tconstruct.data.pack;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.Objects;
import java.util.function.Function;

/** Data provider factory with the registration entry name kept for diagnostics. */
public record DynamicProviderFactory(String name, Function<PackOutput, ? extends DataProvider> factory) implements Function<PackOutput, DataProvider> {
  public DynamicProviderFactory {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(factory, "factory");
  }

  public static DynamicProviderFactory unnamed(Function<PackOutput, ? extends DataProvider> factory) {
    return new DynamicProviderFactory("", factory);
  }

  @Override
  public DataProvider apply(PackOutput output) {
    return factory.apply(output);
  }
}
