package slimeknights.tconstruct.plugin.botania.material;

import net.minecraft.data.PackOutput;

import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;

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
        addCompatMetalMaterial(BotaniaMaterialIds.manaSteel, 2, ORDER_COMPAT + ORDER_GENERAL);
        addCompatMetalMaterial(BotaniaMaterialIds.terraSteel, 3, ORDER_COMPAT + ORDER_GENERAL);
    }
}
