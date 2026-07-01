package slimeknights.tconstruct.library.addon;

import net.minecraft.data.PackOutput;

@FunctionalInterface
public interface DynamicPackProviderFactory<T> {
  T create(PackOutput output);
}
