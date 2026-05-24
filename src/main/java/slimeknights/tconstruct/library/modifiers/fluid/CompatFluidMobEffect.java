package slimeknights.tconstruct.library.modifiers.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Variant of {@link FluidMobEffect} that serializes by effect ID and resolves at runtime. */
public record CompatFluidMobEffect(ResourceLocation effectId, int time, int level) {
  public static final RecordLoadable<CompatFluidMobEffect> LOADABLE = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("effect", CompatFluidMobEffect::effectId),
    IntLoadable.FROM_ONE.defaultField("time", -1, false, CompatFluidMobEffect::time),
    IntLoadable.FROM_ONE.defaultField("level", 1, true, CompatFluidMobEffect::level),
    CompatFluidMobEffect::new);

  /** Gets the amplifier for a mob effect. */
  public int amplifier() {
    return level - 1;
  }

  /** Checks if the duration is infinite. */
  public boolean isInfinite() {
    return time == MobEffectInstance.INFINITE_DURATION;
  }

  /** Resolves the effect from the built-in registry, returning null if missing. */
  public MobEffect resolveEffect() {
    return BuiltInRegistries.MOB_EFFECT.getOptional(effectId).orElse(null);
  }

  /** Creates the final effect instance for the given time if the effect exists. */
  public MobEffectInstance effectWithTime(int time) {
    MobEffect effect = resolveEffect();
    if (effect == null) {
      return null;
    }
    return new MobEffectInstance(effect, time, amplifier());
  }

  /** Applies the effect if it exists in the runtime registry. */
  public float apply(LivingEntity target, EffectLevel scale, TimeAction timeAction, FluidAction action) {
    FluidMobEffect resolved = asFluidMobEffect();
    if (resolved == null) {
      return 0;
    }
    return resolved.apply(target, scale, timeAction, action);
  }

  /** Gets the display name for the effect. */
  public Component getDisplayName(TimeAction action) {
    FluidMobEffect resolved = asFluidMobEffect();
    if (resolved == null) {
      return Component.literal(effectId.toString());
    }
    return resolved.getDisplayName(action);
  }

  private FluidMobEffect asFluidMobEffect() {
    MobEffect effect = resolveEffect();
    if (effect == null) {
      return null;
    }
    return new FluidMobEffect(effect, time, level, null);
  }
}
