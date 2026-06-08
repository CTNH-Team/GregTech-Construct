/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.mixin.create;

import slimeknights.tconstruct.plugin.create.modifier.CreateModifierIds;
import slimeknights.tconstruct.plugin.create.modifier.CreateModifierLookup;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.wrench.WrenchEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.tools.item.IModifiable;

@Mixin(WrenchEventHandler.class)
public class WrenchEventHandlerMixin {

    @Inject(
            remap = false,
            method = "useOwnWrenchLogicForCreateBlocks",
            at = @At(
                     value = "INVOKE",
                     target = "Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$RightClickBlock;getLevel()Lnet/minecraft/world/level/Level;",
                     ordinal = 1),
            cancellable = true)
    private static void ctnh$requireWrenchModifier(PlayerInteractEvent.RightClickBlock event, CallbackInfo ci) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof IModifiable &&
                !CreateModifierLookup.hasModifier(stack, CreateModifierIds.WRENCH)) {
            ci.cancel();
        }
    }

    @Redirect(
              remap = false,
              method = "useOwnWrenchLogicForCreateBlocks",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/simibubi/create/AllTags$AllItemTags;matches(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean ctnh$acceptTinkerWrench(AllTags.AllItemTags tag, Item item) {
        if (item instanceof IModifiable) {
            return true;
        }
        return tag.matches(item);
    }
}
