package slimeknights.tconstruct.mixin;

import com.gregtechceu.gtceu.api.recipe.StrictShapedRecipe;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;

@Mixin(value = StrictShapedRecipe.class)
public abstract class StrictShapedRecipeMixin {
    /**
     * 拦截 GT 的 StrictShapedRecipe.matches 方法
     * 检查合成台中是否有损坏的工具
     */
    @Inject(
        method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tconstruct$matches(
        CraftingContainer inv,
        Level level,
        CallbackInfoReturnable<Boolean> cir
    ) {
        // 遍历合成台中所有物品
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ModifiableItem) {
                if (ToolDamageUtil.isBroken(stack)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
    }
}
