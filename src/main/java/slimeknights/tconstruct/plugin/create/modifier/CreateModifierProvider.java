/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.modifier;

import net.minecraft.data.PackOutput;

import slimeknights.tconstruct.library.data.tinkering.AbstractModifierProvider;
import slimeknights.tconstruct.library.modifiers.util.ModifierLevelDisplay;

public final class CreateModifierProvider extends AbstractModifierProvider {

    public CreateModifierProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public String getName() {
        return "Tinkers' Construct Create Modifiers";
    }

    @Override
    protected void addModifiers() {
        buildModifier(CreateModifierIds.CRUSHING)
                .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
                .build();
        buildModifier(CreateModifierIds.EXTENDO)
                .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
                .build();
        buildModifier(CreateModifierIds.GOGGLES)
                .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
                .build();
        buildModifier(CreateModifierIds.WRENCH)
                .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
                .build();
        buildModifier(CreateModifierIds.DIVING_WEIGHTS)
                .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
                .build();
    }
}
