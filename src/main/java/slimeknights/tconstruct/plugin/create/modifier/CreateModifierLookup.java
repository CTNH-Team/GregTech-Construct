/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.modifier;

import net.minecraft.world.item.ItemStack;

import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public final class CreateModifierLookup {

    private CreateModifierLookup() {}

    public static boolean hasModifier(ItemStack stack, ModifierId modifierId) {
        if (!(stack.getItem() instanceof IModifiable)) {
            return false;
        }
        return ToolStack.from(stack.copy()).getModifier(modifierId).getLevel() > 0;
    }
}
