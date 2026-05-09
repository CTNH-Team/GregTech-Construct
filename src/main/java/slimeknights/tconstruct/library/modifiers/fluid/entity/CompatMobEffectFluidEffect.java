package slimeknights.tconstruct.library.modifiers.fluid.entity;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.fluid.CompatFluidMobEffect;
import slimeknights.tconstruct.library.modifiers.fluid.EffectLevel;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.fluid.TimeAction;

/** Fluid effect that stores a mob effect by ID for compat-only datagen entries. */
public record CompatMobEffectFluidEffect(CompatFluidMobEffect effect, TimeAction action) implements FluidEffect<FluidEffectContext.Entity> {
  public static final RecordLoadable<CompatMobEffectFluidEffect> LOADER = RecordLoadable.create(
    CompatFluidMobEffect.LOADABLE.directField(CompatMobEffectFluidEffect::effect),
    TimeAction.LOADABLE.requiredField("action", CompatMobEffectFluidEffect::action),
    CompatMobEffectFluidEffect::new);

  @Override
  public RecordLoadable<CompatMobEffectFluidEffect> getLoader() {
    return LOADER;
  }

  @Override
  public float apply(FluidStack fluid, EffectLevel scale, FluidEffectContext.Entity context, FluidAction action) {
    if (context.getLivingTarget() == null) {
      return 0;
    }
    return effect.apply(context.getLivingTarget(), scale, this.action, action);
  }

  @Override
  public Component getDescription(RegistryAccess registryAccess) {
    return effect.getDisplayName(action);
  }
}
