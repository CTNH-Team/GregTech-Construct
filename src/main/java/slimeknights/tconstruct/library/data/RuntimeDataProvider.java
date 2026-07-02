package slimeknights.tconstruct.library.data;

import slimeknights.tconstruct.library.addon.DynamicDataRegistrar;

public interface RuntimeDataProvider {
  void addToDynamicPack(DynamicDataRegistrar registrar);
}
