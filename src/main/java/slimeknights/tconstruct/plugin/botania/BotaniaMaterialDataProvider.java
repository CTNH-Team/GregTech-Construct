package slimeknights.tconstruct.plugin.botania;

import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

/**
 * Botania-owned material definitions.
 */
public class BotaniaMaterialDataProvider extends AbstractMaterialDataProvider {
  public BotaniaMaterialDataProvider(PackOutput packOutput) {
    super(packOutput);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Botania Materials";
  }

  @Override
  protected void addMaterials() {
    addCompatMetalMaterial(MaterialIds.manaSteel, 2, ORDER_COMPAT + ORDER_GENERAL);
    addCompatMetalMaterial(MaterialIds.terraSteel, 3, ORDER_COMPAT + ORDER_GENERAL);
  }
}
