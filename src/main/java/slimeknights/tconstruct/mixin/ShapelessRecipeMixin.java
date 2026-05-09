package slimeknights.tconstruct.mixin;

import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ModifiableGTToolItem;

import java.util.List;

@SuppressWarnings("removal")
@Mixin(value = ShapelessRecipe.class)
public abstract class ShapelessRecipeMixin {
    @Inject(
        method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    private void tconstruct$matches(
        CraftingContainer inv,
        Level level,
        CallbackInfoReturnable<Boolean> cir,
        StackedContents stackedcontents,
        List<ItemStack> inputs,
        int i,
        int j,
        ItemStack stack
    ) {
        if (stack.getItem() instanceof ModifiableGTToolItem && ToolDamageUtil.isBroken(stack)) {
            cir.setReturnValue(false);
        }
    }
}
