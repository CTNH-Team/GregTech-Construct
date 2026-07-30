package slimeknights.tconstruct.mixin.gtceu;

import com.gregtechceu.gtceu.api.recipe.ingredient.ToolIngredient;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;

@Mixin(value = ToolIngredient.class, remap = false)
public abstract class ToolIngredientMixin {
  @Inject(method = "test", at = @At("HEAD"), cancellable = true)
  private void tconstruct$rejectBrokenTool(ItemStack input, CallbackInfoReturnable<Boolean> cir) {
    if (input != null && input.getItem() instanceof ModifiableItem && ToolDamageUtil.isBroken(input)) {
      cir.setReturnValue(false);
    }
  }
}
