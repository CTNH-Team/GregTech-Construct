package slimeknights.tconstruct.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.tools.logic.HurtSoundHandler;

@Mixin(Player.class)
public abstract class PlayerHurtSoundMixin {
  @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
  private void tconstruct$replaceGetHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
    Player self = (Player)(Object)this;
    if (self.level().isClientSide) {
      if (HurtSoundHandler.tryRemoveRemote(self.getId())) {
        cir.setReturnValue(Sounds.FULLY_REDUCTED.getSound());
      }
      return;
    }
    if (HurtSoundHandler.tryRemove(self)) {
      cir.setReturnValue(Sounds.FULLY_REDUCTED.getSound());
    }
  }
}
