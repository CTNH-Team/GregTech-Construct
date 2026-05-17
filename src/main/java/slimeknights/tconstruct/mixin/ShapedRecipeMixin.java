package slimeknights.tconstruct.mixin;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;

@SuppressWarnings("removal")
@Mixin(value = ShapedRecipe.class)
public abstract class ShapedRecipeMixin {
    @Inject(
        method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;IIZ)Z",
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/crafting/Ingredient;EMPTY:Lnet/minecraft/world/item/crafting/Ingredient;", opcode = 178),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    private void tconstruct$matches(
        CraftingContainer craftingInventory,
        int width,
        int height,
        boolean mirrored,
        CallbackInfoReturnable<Boolean> cir,
        int i,
        int j,
        int k,
        int l
    ) {
        ItemStack stack = craftingInventory.getItem(i + j * craftingInventory.getWidth());
        if (stack.getItem() instanceof ModifiableItem && ToolDamageUtil.isBroken(stack)) {
            cir.setReturnValue(false);
        }
    }
}
