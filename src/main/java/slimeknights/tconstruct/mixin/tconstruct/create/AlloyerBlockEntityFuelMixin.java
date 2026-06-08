/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.mixin.tconstruct.create;

import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.plugin.create.burner.CreateHeatFuelModule;
import slimeknights.tconstruct.smeltery.block.entity.controller.AlloyerBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.SolidFuelModule;

@Mixin(AlloyerBlockEntity.class)
public class AlloyerBlockEntityFuelMixin {
  @Redirect(
      remap = false,
      method = "<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
      at = @At(value = "NEW", target = "Lslimeknights/tconstruct/smeltery/block/entity/module/SolidFuelModule;"))
  private SolidFuelModule tconstruct$createFuelModule(MantleBlockEntity parent, BlockPos fuelPos) {
    return new CreateHeatFuelModule(parent, fuelPos);
  }
}
