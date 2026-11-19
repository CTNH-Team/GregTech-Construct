package slimeknights.tconstruct.library.addon;

import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.ApiStatus.Internal;

@FunctionalInterface
@Internal
public interface DynamicPackProviderFactory<T> {
  T create(PackOutput output);
}
