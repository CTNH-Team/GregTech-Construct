package slimeknights.tconstruct.plugin.botania.material;

import slimeknights.tconstruct.library.client.data.material.AbstractMaterialSpriteProvider;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToColorMapping;

/**
 * Botania-owned material sprite definitions.
 */
public class BotaniaMaterialSpriteProvider extends AbstractMaterialSpriteProvider {
  @Override
  public String getName() {
    return "Tinkers' Construct Botania Materials";
  }

  @Override
  protected void addAllMaterials() {
    buildMaterial(BotaniaMaterialIds.manaSteel)
      .meleeHarvest().ranged().armor()
      .fallbacks("metal")
      .colorMapper(GreyToColorMapping.builderFromBlack()
        .addARGB(63, 0xFF160539)
        .addARGB(102, 0xFF14084f)
        .addARGB(140, 0xFF2e199f)
        .addARGB(178, 0xFF1f20b9)
        .addARGB(216, 0xFF3a63da)
        .addARGB(255, 0xFF67b9ee).build());

    buildMaterial(BotaniaMaterialIds.terraSteel)
      .meleeHarvest().ranged().armor()
      .fallbacks("metal")
      .colorMapper(GreyToColorMapping.builderFromBlack()
        .addARGB(63, 0xFF001e11)
        .addARGB(102, 0xFF043c1c)
        .addARGB(140, 0xFF0c7227)
        .addARGB(178, 0xFF2bb93b)
        .addARGB(216, 0xFF6ae862)
        .addARGB(255, 0xFFccffb5).build());
  }
}
