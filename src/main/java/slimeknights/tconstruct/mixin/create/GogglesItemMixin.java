/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.mixin.create;

import slimeknights.tconstruct.plugin.create.modifier.CreateModifierIds;
import slimeknights.tconstruct.plugin.create.modifier.CreateModifierLookup;

import net.minecraft.world.entity.EquipmentSlot;

import com.simibubi.create.content.equipment.goggles.GogglesItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GogglesItem.class)
public class GogglesItemMixin {

    @Inject(remap = false, method = "<clinit>", at = @At("TAIL"))
    private static void ctnh$acceptTinkerGoggles(CallbackInfo ci) {
        GogglesItem.addIsWearingPredicate(player -> CreateModifierLookup
                .hasModifier(player.getItemBySlot(EquipmentSlot.HEAD), CreateModifierIds.GOGGLES));
    }
}
