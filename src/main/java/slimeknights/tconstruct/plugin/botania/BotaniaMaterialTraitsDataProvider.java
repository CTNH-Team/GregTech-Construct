package slimeknights.tconstruct.plugin.botania;

import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.data.material.AbstractMaterialTraitDataProvider;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

/**
 * Botania-owned material traits.
 */
public class BotaniaMaterialTraitsDataProvider extends AbstractMaterialTraitDataProvider {
  public BotaniaMaterialTraitsDataProvider(PackOutput packOutput) {
    this(packOutput, new BotaniaMaterialDataProvider(packOutput));
  }

  public BotaniaMaterialTraitsDataProvider(PackOutput packOutput, AbstractMaterialDataProvider materials) {
    super(packOutput, materials);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Botania Material Traits";
  }

  @Override
  protected void addMaterialTraits() {
    addDefaultTraits(MaterialIds.manaSteel, ModifierIds.manafix);
    addDefaultTraits(MaterialIds.terraSteel, ModifierIds.manafix, ModifierIds.terrarecover);
  }
}
