/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.mixin.create;

import slimeknights.tconstruct.plugin.create.modifier.CreateModifierIds;
import slimeknights.tconstruct.plugin.create.modifier.CreateModifierLookup;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DivingBootsItem.class)
public class DivingBootsItemMixin {

    @Inject(remap = false, method = "getWornItem", at = @At(value = "RETURN"), cancellable = true)
    private static void ctnh$getTinkerBoots(Entity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.FEET);
            if (CreateModifierLookup.hasModifier(stack, CreateModifierIds.DIVING_WEIGHTS)) {
                cir.setReturnValue(stack);
            }
        }
    }
}
