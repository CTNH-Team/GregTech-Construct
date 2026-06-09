package slimeknights.tconstruct.mixin.botania;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.plugin.botania.modifier.AncientWillAccess;
import slimeknights.tconstruct.plugin.botania.modifier.AncientWillModifier;

@Mixin(Player.class)
public abstract class PlayerAncientWillMixin extends LivingEntity implements AncientWillAccess {
    @Unique
    private LivingEntity tconstruct$ancientWillCritTarget;

    protected PlayerAncientWillMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tconstruct$setAncientWillCritTarget(LivingEntity target) {
        this.tconstruct$ancientWillCritTarget = target;
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private DamageSource tconstruct$applyAncientWill(DamageSource source, float amount) {
        LivingEntity target = this.tconstruct$ancientWillCritTarget;
        if (target != null) {
            this.tconstruct$ancientWillCritTarget = null;
            return AncientWillModifier.onCriticalAttack(source, amount, (Player) (Object) this, target);
        }
        return source;
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void tconstruct$clearAncientWillTarget(Entity target, CallbackInfo ci) {
        this.tconstruct$ancientWillCritTarget = null;
    }
}
