package slimeknights.tconstruct.plugin.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

@EmiEntrypoint
public final class TConstructEmiStackProviderPlugin implements EmiPlugin {
  @Override
  public void register(EmiRegistry registry) {
    registry.addGenericStackProvider(new TConstructEmiStackProvider());
  }
}
