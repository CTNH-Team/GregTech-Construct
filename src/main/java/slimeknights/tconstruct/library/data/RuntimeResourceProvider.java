package slimeknights.tconstruct.library.data;

import slimeknights.tconstruct.library.addon.DynamicResourceRegistrar;

public interface RuntimeResourceProvider {
  void addToDynamicPack(DynamicResourceRegistrar registrar);
}
