package slimeknights.tconstruct.plugin.botania.material;

import slimeknights.tconstruct.plugin.botania.modifier.BotaniaModifierIds;

import net.minecraft.data.PackOutput;

import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.data.material.AbstractMaterialTraitDataProvider;

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
        addDefaultTraits(BotaniaMaterialIds.manaSteel, BotaniaModifierIds.manafix);
        addDefaultTraits(BotaniaMaterialIds.terraSteel, BotaniaModifierIds.manafix, BotaniaModifierIds.terrarecover);
    }
}
