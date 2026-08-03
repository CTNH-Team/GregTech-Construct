/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.mixin.tconstruct.create;

import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.plugin.create.burner.CreateBlockHeatSource;
import slimeknights.tconstruct.smeltery.block.controller.TinyMultiblockControllerBlock;

/** Lets tiny multiblock controllers (melter, alloyer) treat Create heat sources such as the Blaze Burner as valid fuel */
@Mixin(TinyMultiblockControllerBlock.class)
public class TinyMultiblockControllerBlockMixin {
  @Inject(method = "isValidFuelSource", at = @At("RETURN"), cancellable = true, remap = false)
  private void tconstruct$acceptCreateHeatSources(BlockState state, CallbackInfoReturnable<Boolean> cir) {
    if (!cir.getReturnValue() && state.getBlock() instanceof CreateBlockHeatSource) {
      cir.setReturnValue(true);
    }
  }
}
